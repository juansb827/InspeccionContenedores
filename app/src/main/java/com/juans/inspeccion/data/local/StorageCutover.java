package com.juans.inspeccion.data.local;

import android.content.Context;
import com.juans.inspeccion.data.local.StorageFormatProbe;
import com.juans.inspeccion.data.local.StorageFormatProbe.Outcome;
import org.json.*;
import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;

/** Retires positively recognized old namespaces, never deserializes or deletes media. */
public final class StorageCutover {
    public static final String STATE="storage-cutover-v1.json";
    public static final String ARCHIVE="retired-storage";
    private static final String[] SOURCES={"listaPendientes","fotosPendientes","pending-photo-capture","pending-video-capture","pending-media-deletion","inspection-drafts-v1.json"};
    private StorageCutover() {}
    public interface Boundary { void afterIsolation(File source) throws IOException; }
    public static synchronized Status prepare(Context context) throws IOException { return prepare(context,source->{}); }
    public static synchronized Status prepare(Context context, Boundary boundary) throws IOException { return prepare(context,boundary,()->{}); }
    public static synchronized Status prepare(Context context, Boundary boundary, AtomicJson.Boundary markerBoundary) throws IOException {
        File state=context.getFileStreamPath(STATE);
        byte[] previous=AtomicJson.read(state,StorageCutover::validateState);
        JSONObject marker;
        try { marker=previous==null ? new JSONObject().put("format","inspection-storage-cutover").put("schemaVersion",1).put("acknowledged",false).put("isolated",new JSONArray()) : StrictJson.object(previous); }
        catch(JSONException e) { throw new IOException(e); }
        LinkedHashSet<String> isolated=new LinkedHashSet<>();
        try { JSONArray old=marker.getJSONArray("isolated"); for(int n=0;n<old.length();n++) isolated.add(old.getString(n)); }
        catch(JSONException e) { throw new IOException(e); }
        File archive=new File(context.getFilesDir(),ARCHIVE);
        LinkedHashMap<String,Outcome> unresolved=new LinkedHashMap<>();
        if(archive.exists()) {
            File[] files=archive.listFiles(); if(files==null) unresolved.put(ARCHIVE,Outcome.IO_ERROR);
            if(files!=null) for(File f:files) if(recognizedArchiveName(f.getName()) && legacy(f,archiveSource(f.getName()))==Outcome.UNSUPPORTED_OLDER) isolated.add(f.getName());
        }
        for(String source:SOURCES) for(String suffix:new String[]{"",".bak",".new"}) {
            String name=source+suffix; File file=context.getFileStreamPath(name);
            Outcome outcome=legacy(file,source);
            if(source.equals("inspection-drafts-v1.json")&&outcome==Outcome.SUPPORTED_SCHEMA) continue;
            if(outcome==Outcome.MISSING) continue;
            if(outcome!=Outcome.UNSUPPORTED_OLDER) { unresolved.put(name,outcome); continue; }
            try {
                if(!archive.isDirectory() && !archive.mkdirs()) throw new IOException("No se pudo aislar el almacenamiento anterior");
                String destination=name+"."+hash(file); File target=new File(archive,destination);
                if(!target.exists()) {
                    Files.move(file.toPath(),target.toPath(),StandardCopyOption.ATOMIC_MOVE);
                    boundary.afterIsolation(file);
                } else if(!hash(file).equals(hash(target))) throw new IOException("Conflicto en archivos aislados");
                // Duplicate evidence is retained in place, but no reader uses the old namespace.
                isolated.add(destination);
            } catch(IOException e) {
                if(source.equals("listaPendientes")||source.equals("inspection-drafts-v1.json")) throw e;
                // Retired queue / unresolved old operations cannot block a healthy inspection store.
                unresolved.put(name,Outcome.IO_ERROR);
            }
        }
        try {
            JSONArray names=new JSONArray(); for(String s:isolated) names.put(s);
            JSONArray old=marker.getJSONArray("isolated");
            boolean changed=previous==null||old.length()!=names.length();
            if(old.length()<names.length()) marker.put("acknowledged",false);
            marker.put("isolated",names);
            if(changed) AtomicJson.write(state,marker.toString().getBytes(StandardCharsets.UTF_8),markerBoundary);
            return new Status(!marker.getBoolean("acknowledged")&&!isolated.isEmpty(),isolated,unresolved);
        } catch(JSONException e) { throw new IOException(e); }
    }
    public static synchronized void acknowledge(Context context) throws IOException {
        prepare(context); File state=context.getFileStreamPath(STATE);
        byte[] bytes=AtomicJson.read(state,StorageCutover::validateState);
        try { JSONObject marker=StrictJson.object(bytes); marker.put("acknowledged",true); AtomicJson.write(state,marker.toString().getBytes(StandardCharsets.UTF_8)); }
        catch(JSONException e) { throw new IOException(e); }
    }
    public static File currentJournal(Context context,String oldName) {
        try { prepare(context); }
        catch(StrictJson.Invalid e) { throw new com.juans.inspeccion.data.local.StoredInspections.StorageException(e.outcome,e.getMessage(),e); }
        catch(IOException e) { throw new com.juans.inspeccion.data.local.StoredInspections.StorageException(Outcome.IO_ERROR,"No se pudo preparar la recuperación local. Los archivos se conservan.",e); }
        if(!Arrays.asList(SOURCES).contains(oldName)||!oldName.startsWith("pending-")) throw new IllegalArgumentException(oldName);
        return context.getFileStreamPath(oldName+"-v1");
    }
    private static Outcome legacy(File file,String source) {
        // Only these exact audited names reach this function. A serialization header establishes
        // an encoding generation, not an exact originating app version or media ownership.
        return StorageFormatProbe.inspect(file,source.equals("inspection-drafts-v1.json")?source:"listaPendientes");
    }
    private static String archiveSource(String name) { for(String source:SOURCES) if(name.startsWith(source+".")) return source; return ""; }
    private static boolean recognizedArchiveName(String name) {
        for(String source:SOURCES) for(String suffix:new String[]{"",".bak",".new"}) if(name.matches(java.util.regex.Pattern.quote(source+suffix)+"\\.[0-9a-f]{64}")) return true;
        return false;
    }
    private static String hash(File file) throws IOException {
        try { MessageDigest digest=MessageDigest.getInstance("SHA-256"); try(InputStream in=Files.newInputStream(file.toPath())) { byte[] b=new byte[8192]; int n; while((n=in.read(b))!=-1) digest.update(b,0,n); }
            StringBuilder s=new StringBuilder(); for(byte b:digest.digest()) s.append(String.format(Locale.ROOT,"%02x",b&255)); return s.toString();
        } catch(NoSuchAlgorithmException e) { throw new AssertionError(e); }
    }
    private static void validateState(byte[] bytes) {
        JSONObject o=StrictJson.object(bytes);
        try {
            if(!"inspection-storage-cutover".equals(o.get("format"))||DraftCodec.integer(o.get("schemaVersion"),1,1)!=1||!(o.get("acknowledged") instanceof Boolean)||o.length()!=4) throw new JSONException("marker");
            JSONArray list=o.getJSONArray("isolated"); if(list.length()>10000) throw new JSONException("limit");
            Set<String> names=new HashSet<>(); for(int n=0;n<list.length();n++) if(!(list.get(n) instanceof String)||!recognizedArchiveName(list.getString(n))||!names.add(list.getString(n))) throw new JSONException("name");
        } catch(JSONException e) { throw new StrictJson.Invalid(Outcome.CORRUPT,"Estado de almacenamiento inválido; se conserva para revisión"); }
    }
    public static final class Status {
        public final boolean noticePending;
        public final Set<String> isolated;
        public final Map<String,Outcome> unresolved;
        Status(boolean pending,Set<String> isolated,Map<String,Outcome> unresolved) { this.noticePending=pending; this.isolated=Collections.unmodifiableSet(isolated); this.unresolved=Collections.unmodifiableMap(unresolved); }
        public String description() {
            StringBuilder text=new StringBuilder("Archivos aislados: ").append(isolated.size());
            if(unresolved.isEmpty()) return text.append("\nNo hay archivos sin resolver.").toString();
            text.append("\nArchivos sin resolver:");
            for(Map.Entry<String,Outcome> entry:unresolved.entrySet()) text.append("\n").append(entry.getKey()).append(": ").append(label(entry.getValue()));
            return text.toString();
        }
        private static String label(Outcome outcome) {
            switch(outcome) {
                case CORRUPT: return "datos dañados o incompletos";
                case UNKNOWN: return "formato desconocido";
                case UNSUPPORTED_NEWER: return "formato más reciente no admitido";
                case IO_ERROR: return "error de acceso; archivos conservados";
                default: return "formato fuera del almacenamiento activo";
            }
        }
    }
}
