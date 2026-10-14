package com.juans.inspeccion.Mundo.Storage;

import android.content.Context;
import android.util.AtomicFile;
import com.juans.inspeccion.Mundo.Album;
import com.juans.inspeccion.Interfaz.Gallery.GalleryMediaItem;
import org.json.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Local sidecar only. Album identity and independent video ordering are preserved; uploads are retired. */
public class LocalMediaIndex {
    private static final Object LOCK = new Object();
    private final AtomicFile file;
    public LocalMediaIndex(Context context) { file = new AtomicFile(context.getFileStreamPath("local-media-index.json")); }
    private JSONObject read() throws IOException {
        try {
            if (!file.getBaseFile().exists() && !new File(file.getBaseFile()+".bak").exists()) {
                if(new File(file.getBaseFile()+".new").exists()) throw new IOException("Índice local interrumpido; se conserva .new para recuperación");
                return new JSONObject().put("version", 1).put("albums", new JSONObject());
            }
            JSONObject root = new JSONObject(new String(file.readFully(), StandardCharsets.UTF_8));
            if (root.getInt("version") != 1 || !(root.get("albums") instanceof JSONObject)) throw new IOException("Versión del índice desconocida");
            return root;
        } catch (JSONException | RuntimeException e) { throw new IOException("No se pudo leer el índice local; el archivo se conserva", e); }
    }
    protected void write(JSONObject root) throws IOException {
        FileOutputStream out = null;
        try {
            out = file.startWrite(); out.write(root.toString().getBytes(StandardCharsets.UTF_8)); out.flush(); out.getFD().sync(); file.finishWrite(out);
        } catch (IOException | RuntimeException e) { if (out != null) file.failWrite(out); throw new IOException("No se pudo guardar el índice local; requiere recuperación", e); }
    }
    private static String key(String inspection, String album) { return inspection + "/" + album; }
    private JSONArray reconcile(JSONObject root, String inspection, Album album) throws JSONException {
        JSONObject albums = root.getJSONObject("albums"); String key = key(inspection, album.getId());
        JSONArray previous = albums.optJSONArray(key), current = new JSONArray(); Set<String> seen = new HashSet<>();
        if (previous != null) for (int i = 0; i < previous.length(); i++) {
            JSONObject row = previous.getJSONObject(i); String reference = row.getString("reference"), kind = row.getString("kind");
            if (!kind.equals("PHOTO") && !kind.equals("VIDEO")) throw new JSONException("Tipo de archivo desconocido");
            if (kind.equals("VIDEO") || album.getFotos().contains(reference)) { if (seen.add(reference)) current.put(row); }
        }
        for (String reference : album.getFotos()) if (seen.add(reference)) current.put(row(reference, "image/jpeg", "PHOTO", 0));
        albums.put(key, current); return current;
    }
    private static JSONObject row(String reference, String mime, String kind, long duration) throws JSONException {
        return new JSONObject().put("reference", reference).put("mime", mime).put("kind", kind).put("duration", duration);
    }
    public List<GalleryMediaItem> items(String inspection, Album album) throws IOException {
        synchronized (LOCK) {
            try {
                JSONObject root = read(); String before = root.toString(); JSONArray rows = reconcile(root, inspection, album);
                if (!before.equals(root.toString())) write(root);
                List<GalleryMediaItem> result = new ArrayList<>();
                for (int i = 0; i < rows.length(); i++) {
                    JSONObject row = rows.getJSONObject(i);
                    result.add(new GalleryMediaItem(row.getString("reference"), row.getString("mime"),
                            GalleryMediaItem.Kind.valueOf(row.getString("kind")), row.getLong("duration")));
                }
                return result;
            } catch (JSONException | IllegalArgumentException e) { throw new IOException("Índice local inválido; el archivo se conserva", e); }
        }
    }
    public void addVideo(String inspection, Album album, String reference, long duration) throws IOException {
        if (!reference.startsWith("content://") || duration <= 0) throw new IOException("Video inválido");
        synchronized (LOCK) {
            try {
                JSONObject root = read(); JSONArray rows = reconcile(root, inspection, album);
                for (int i = 0; i < rows.length(); i++) if (rows.getJSONObject(i).getString("reference").equals(reference)) return;
                rows.put(row(reference, "video/mp4", "VIDEO", duration)); write(root);
            } catch (JSONException e) { throw new IOException("No se pudo asociar el video", e); }
        }
    }
    public void remove(String inspection, String album, String reference) throws IOException {
        synchronized (LOCK) {
            try {
                JSONObject root = read(); JSONObject albums = root.getJSONObject("albums"); String key = key(inspection, album);
                JSONArray previous = albums.optJSONArray(key); if (previous == null) return;
                JSONArray keep = new JSONArray();
                for (int i = 0; i < previous.length(); i++) {
                    JSONObject row = previous.getJSONObject(i); if (!row.getString("reference").equals(reference)) keep.put(row);
                }
                albums.put(key, keep); write(root);
            } catch (JSONException e) { throw new IOException("No se pudo actualizar el índice local", e); }
        }
    }
}
