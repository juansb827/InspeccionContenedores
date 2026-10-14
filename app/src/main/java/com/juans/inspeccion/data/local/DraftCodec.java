package com.juans.inspeccion.data.local;

import com.juans.inspeccion.Mundo.Album;
import com.juans.inspeccion.Mundo.Danio;
import com.juans.inspeccion.Mundo.Inspeccion;

import com.juans.inspeccion.Mundo.*;
import com.juans.inspeccion.data.local.StorageFormatProbe.Outcome;
import org.json.*;
import java.util.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

/** Explicit schema, never Java class names. Partial forms and characterized totals are preserved. */
public final class DraftCodec {
    public static final String FORMAT = "inspection-drafts";
    public static final int VERSION = 1;
    private DraftCodec() {}
    private static StrictJson.Invalid bad() { return new StrictJson.Invalid(Outcome.CORRUPT,"Registro de borrador inválido"); }
    public static byte[] encode(ArrayList<Inspeccion> records) {
        try {
            if(records==null||records.size()>1000) throw bad();
            preflight(records);
            JSONArray list = new JSONArray(); for (Inspeccion i : records) list.put(encodeRecord(i));
            JSONObject root = new JSONObject().put("format",FORMAT).put("schemaVersion",VERSION).put("records",list);
            byte[] bytes = root.toString().getBytes(StandardCharsets.UTF_8);
            decode(bytes); // Writer and reader enforce the same complete schema and limits.
            return bytes;
        } catch (JSONException | NullPointerException e) { throw bad(); }
    }
    public static ArrayList<Inspeccion> decode(byte[] bytes) {
        try {
            JSONObject root = StrictJson.object(bytes);
            if (!FORMAT.equals(root.opt("format"))) throw new StrictJson.Invalid(Outcome.UNKNOWN,"Formato desconocido");
            Object versionValue=root.get("schemaVersion");
            if(!(versionValue instanceof Integer||versionValue instanceof Long)||((Number)versionValue).longValue()<0) throw bad();
            long version=((Number)versionValue).longValue();
            if (version != VERSION) throw new StrictJson.Invalid(version < VERSION ? Outcome.UNSUPPORTED_OLDER : Outcome.UNSUPPORTED_NEWER,"Versión de almacenamiento no admitida");
            if(root.has("writerAppVersion")) { text(root.get("writerAppVersion")); root.remove("writerAppVersion"); }
            keys(root,"format","schemaVersion","records");
            JSONArray list = array(root.get("records"),1000);
            ArrayList<Inspeccion> result = new ArrayList<>(); Set<String> ids = new HashSet<>(); Set<String> albumIds = new HashSet<>();
            for (int j=0;j<list.length();j++) {
                Inspeccion i = decodeRecord(object(list.get(j)), albumIds);
                if (!ids.add(i.getPhotoId())) throw bad(); result.add(i);
            }
            return result;
        } catch (JSONException | ClassCastException e) { throw bad(); }
    }
    private static void preflight(ArrayList<Inspeccion> records) {
        Budget budget=new Budget();
        for(Inspeccion i:records) {
            if(i==null||i.getDaniosManager()==null||i.getDaniosManager().getListaDanios().size()>500||i.getFotosInspeccion()!=null&&i.getFotosInspeccion().size()>1000) throw bad();
            budget.add(1024); budget.string(i.getPhotoId()); budget.string(i.getTipoInspeccion()); budget.string(i.getTipoDocumento()); budget.string(i.getParentFolder());
            budget.map(i.getInformacion()); budget.map(i.getDatosContenedor()); budget.maps(i.getInfoBooking()); budget.maps(i.getTurnosPendientes());
            if(i.getFechaInspeccion()!=null) { if(i.getFechaInspeccion().length>7) throw bad(); for(String v:i.getFechaInspeccion()) budget.string(v); }
            Set<Album> seen=Collections.newSetFromMap(new IdentityHashMap<Album,Boolean>());
            for(Album a:i.allAlbums()) if(seen.add(a)) {
                if(seen.size()>1000||a.getFotos()==null||a.getFotos().size()>2000) throw bad();
                budget.add(256); budget.string(a.getId()); for(String ref:a.getFotos()) budget.string(ref); budget.map((HashMap<String,String>)field(a,"remoteNames"));
            }
            for(Danio d:i.getDaniosManager().getListaDanios()) { if(d==null||d.getAlbum()==null) throw bad(); budget.add(128); budget.map(d.getDetalles()); }
        }
    }
    private static final class Budget {
        long size;
        void add(long n) { size+=n; if(size>StrictJson.MAX_BYTES) throw bad(); }
        void string(String v) { if(v!=null) { text(v); add(JSONObject.quote(v).getBytes(StandardCharsets.UTF_8).length+2); } else add(6); }
        void map(HashMap<String,String> map) { if(map==null||map.size()>256) throw bad(); add(2); for(Map.Entry<String,String> e:map.entrySet()) { if(e.getKey()==null) throw bad(); string(e.getKey()); string(e.getValue()); } }
        void maps(ArrayList<HashMap<String,String>> maps) { if(maps==null) { add(4); return; } if(maps.size()>1000) throw bad(); for(HashMap<String,String> map:maps) map(map); }
    }
    private static JSONObject encodeRecord(Inspeccion i) throws JSONException {
        if(i==null||i.getDaniosManager()==null||i.getDaniosManager().getListaDanios().size()>500||i.getFotosInspeccion()!=null&&i.getFotosInspeccion().size()>1000) throw bad();
        JSONObject o = new JSONObject();
        o.put("id",i.getPhotoId()).put("inspectionType",nullable(i.getTipoInspeccion())).put("documentType",nullable(i.getTipoDocumento()));
        o.put("information",map(i.getInformacion())).put("container",map(i.getDatosContenedor()));
        o.put("date",i.getFechaInspeccion()==null ? JSONObject.NULL : new JSONArray(Arrays.asList(i.getFechaInspeccion())));
        o.put("inBooking",i.isPerteneceEnBooking()).put("inYard",i.estaEnPatio()).put("bookingBalance",i.isHaySaldoBooking()).put("usesTurn",i.isUsaTurno());
        o.put("bookingRows",maps(i.getInfoBooking())).put("pendingTurns",maps(i.getTurnosPendientes())).put("parentFolder",nullable(i.getParentFolder()));
        LinkedHashMap<String,Album> albums = new LinkedHashMap<>();
        for (Album a : i.allAlbums()) { Album previous = albums.put(a.getId(),a); if (previous != null && previous != a) throw bad(); }
        JSONArray table = new JSONArray();
        for (Album a : albums.values()) { if(a.getFotos()==null||a.getFotos().size()>2000) throw bad(); for(String ref:a.getFotos()) text(ref); table.put(new JSONObject().put("id",a.getId()).put("captureCount",a.getFotos_tomadas())
                .put("references",new JSONArray(a.getFotos())).put("remoteNames",map((HashMap<String,String>)field(a,"remoteNames")))); }
        o.put("albums",table).put("generalAlbum",i.getFotosGenerales()==null ? JSONObject.NULL : i.getFotosGenerales().getId());
        JSONArray extra = new JSONArray(); if (i.getFotosInspeccion()!=null) for (Album a:i.getFotosInspeccion()) extra.put(a.getId());
        o.put("extraAlbums",i.getFotosInspeccion()==null ? JSONObject.NULL : extra);
        JSONArray damages = new JSONArray();
        if (i.getDaniosManager()==null) throw bad();
        for (Danio d:i.getDaniosManager().getListaDanios()) damages.put(new JSONObject().put("details",map(d.getDetalles())).put("album",d.getAlbum().getId()));
        o.put("damages",damages).put("damageTotal",i.getDaniosManager().darValorTotal()); return o;
    }
    private static Inspeccion decodeRecord(JSONObject o, Set<String> allAlbumIds) throws JSONException {
        keys(o,"id","inspectionType","documentType","information","container","date","inBooking","inYard","bookingBalance","usesTurn","bookingRows","pendingTurns","parentFolder","albums","generalAlbum","extraAlbums","damages","damageTotal");
        Inspeccion i = new Inspeccion(); set(i,"photoId",id(o.get("id")));
        i.setTipoInspeccion(choice(o.get("inspectionType"),"ENTRADA","SALIDA")); i.setTipoDocumento(choice(o.get("documentType"),"EP","ET"));
        i.setInformacion(readMap(o.get("information"))); i.setDatosContenedor(readMap(o.get("container")));
        Object date=o.get("date"); if (date!=JSONObject.NULL) {
            JSONArray d=array(date,7); if (d.length()!=3&&d.length()!=5&&d.length()!=7) throw bad(); String[] v=new String[d.length()]; for(int n=0;n<d.length();n++) v[n]=text(d.get(n)); i.setFechaInspeccion(v);
        }
        i.setPerteneceEnBooking(bool(o.get("inBooking"))); i.setEstaEnPatio(bool(o.get("inYard"))); i.setHaySaldoBooking(bool(o.get("bookingBalance"))); i.setUsaTurno(bool(o.get("usesTurn")));
        i.setInfoBooking(readMaps(o.get("bookingRows"))); i.setTurnosPendientes(readMaps(o.get("pendingTurns"))); set(i,"parentFolder",optional(o.get("parentFolder")));
        LinkedHashMap<String,Album> albums=new LinkedHashMap<>(); JSONArray table=array(o.get("albums"),1000);
        for(int n=0;n<table.length();n++) {
            JSONObject a=object(table.get(n)); keys(a,"id","captureCount","references","remoteNames"); String aid=id(a.get("id"));
            if (!allAlbumIds.add(aid)) throw bad(); Album album=new Album(); set(album,"id",aid);
            album.setFotos_tomadas(integer(a.get("captureCount"),0,Integer.MAX_VALUE));
            JSONArray refs=array(a.get("references"),2000); ArrayList<String> photos=new ArrayList<>(); Set<String> unique=new HashSet<>();
            for(int r=0;r<refs.length();r++) { String ref=text(refs.get(r)); if (!validReference(ref)||!unique.add(ref)) throw bad(); photos.add(ref); }
            album.setFotos(photos); set(album,"remoteNames",readMap(a.get("remoteNames"))); albums.put(aid,album);
        }
        Set<String> used=new HashSet<>(); i.setFotosGenerales(reference(o.get("generalAlbum"),albums,used,true));
        Object extras=o.get("extraAlbums"); if(extras!=JSONObject.NULL) {
            JSONArray a=array(extras,1000); ArrayList<Album> extra=new ArrayList<>(); Set<String> unique=new HashSet<>();
            for(int n=0;n<a.length();n++) { Album album=reference(a.get(n),albums,used,false); if(!unique.add(album.getId())) throw bad(); extra.add(album); } i.setFotosInspeccion(extra);
        }
        JSONArray ds=array(o.get("damages"),500); ArrayList<Danio> damages=new ArrayList<>();
        for(int n=0;n<ds.length();n++) { JSONObject d=object(ds.get(n)); keys(d,"details","album"); Danio damage=new Danio(); damage.setDetalles(readMap(d.get("details"))); damage.setAlbum(reference(d.get("album"),albums,used,false)); damages.add(damage); }
        if(used.size()!=albums.size()) throw bad(); Object total=o.get("damageTotal"); if(!(total instanceof Number)) throw bad();
        float f=((Number)total).floatValue(); if(Float.isNaN(f)||Float.isInfinite(f)) throw bad();
        // Do not call setListaDanios: it accumulates totals and renumbering uses a live Activity.
        set(i.getDaniosManager(),"listaDanios",damages); set(i.getDaniosManager(),"valorTotal",f); return i;
    }
    private static Album reference(Object value, Map<String,Album> albums, Set<String> used, boolean nullable) {
        if(nullable&&value==JSONObject.NULL) return null; String key=id(value); Album a=albums.get(key); if(a==null) throw bad(); used.add(key); return a;
    }
    private static Object nullable(String s) { return s==null ? JSONObject.NULL : s; }
    private static JSONObject map(HashMap<String,String> map) throws JSONException {
        if(map==null||map.size()>256) throw bad(); JSONObject result=new JSONObject(); for(Map.Entry<String,String> e:map.entrySet()) { if(e.getKey()==null) throw bad(); result.put(text(e.getKey()),e.getValue()==null?JSONObject.NULL:text(e.getValue())); } return result;
    }
    private static Object maps(ArrayList<HashMap<String,String>> maps) throws JSONException { if(maps==null) return JSONObject.NULL; if(maps.size()>1000) throw bad(); JSONArray a=new JSONArray(); for(HashMap<String,String> m:maps) a.put(map(m)); return a; }
    private static HashMap<String,String> readMap(Object v) throws JSONException {
        JSONObject o=object(v); if(o.length()>256) throw bad(); HashMap<String,String> m=new HashMap<>(); Iterator<String> keys=o.keys();
        while(keys.hasNext()) { String key=text(keys.next()); if(key.isEmpty()) throw bad(); m.put(key,optional(o.get(key))); } return m;
    }
    private static ArrayList<HashMap<String,String>> readMaps(Object v) throws JSONException { if(v==JSONObject.NULL) return null; JSONArray a=array(v,1000); ArrayList<HashMap<String,String>> maps=new ArrayList<>(); for(int n=0;n<a.length();n++) maps.add(readMap(a.get(n))); return maps; }
    private static boolean validReference(String ref) {
        if(ref.startsWith("content://")) { try { java.net.URI uri=new java.net.URI(ref); return uri.getRawAuthority()!=null&&!uri.getRawAuthority().isEmpty()&&uri.getRawFragment()==null; } catch(java.net.URISyntaxException e) { return false; } }
        try { com.juans.inspeccion.platform.media.LegacyPhotoImporter.relativePath(ref); return true; } catch(java.io.IOException e) { return false; }
    }
    private static String choice(Object v,String... allowed) { String s=optional(v); if(s!=null&&!Arrays.asList(allowed).contains(s)) throw bad(); return s; }
    private static String optional(Object v) { return v==JSONObject.NULL ? null : text(v); }
    private static String text(Object v) {
        if(!(v instanceof String)||((String)v).length()>16384) throw bad(); String s=(String)v;
        for(int n=0;n<s.length();n++) { char c=s.charAt(n); if(Character.isHighSurrogate(c)) { if(++n>=s.length()||!Character.isLowSurrogate(s.charAt(n))) throw bad(); } else if(Character.isLowSurrogate(c)) throw bad(); }
        return s;
    }
    private static String id(Object v) { String s=text(v); try { if(!UUID.fromString(s).toString().equals(s)) throw bad(); } catch(IllegalArgumentException e) { throw bad(); } return s; }
    private static boolean bool(Object v) { if(!(v instanceof Boolean)) throw bad(); return (Boolean)v; }
    static int integer(Object v,int min,int max) { if(!(v instanceof Integer||v instanceof Long)) throw bad(); long n=((Number)v).longValue(); if(n<min||n>max) throw bad(); return (int)n; }
    private static JSONObject object(Object v) { if(!(v instanceof JSONObject)) throw bad(); return (JSONObject)v; }
    private static JSONArray array(Object v,int max) { if(!(v instanceof JSONArray)||((JSONArray)v).length()>max) throw bad(); return (JSONArray)v; }
    private static void keys(JSONObject o,String... expected) { Set<String> keys=new HashSet<>(Arrays.asList(expected)); Iterator<String> it=o.keys(); while(it.hasNext()) if(!keys.remove(it.next())) throw bad(); if(!keys.isEmpty()) throw bad(); }
    // Explicit adapter for legacy private state; this reflection is not a persisted class-name contract.
    private static Object field(Object target,String name) { try { Field f=target.getClass().getDeclaredField(name); f.setAccessible(true); return f.get(target); } catch(ReflectiveOperationException e) { throw new IllegalStateException(e); } }
    private static void set(Object target,String name,Object value) { try { Field f=target.getClass().getDeclaredField(name); f.setAccessible(true); f.set(target,value); } catch(ReflectiveOperationException e) { throw new IllegalStateException(e); } }
}
