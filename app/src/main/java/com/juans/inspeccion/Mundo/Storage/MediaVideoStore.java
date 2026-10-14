package com.juans.inspeccion.Mundo.Storage;

import com.juans.inspeccion.data.local.AtomicObjects;

import com.juans.inspeccion.platform.media.ImageProcessor;
import com.juans.inspeccion.platform.media.VideoStore;

import android.content.*;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import java.io.*;
import java.util.HashMap;

/** Videos are never sent through ImageProcessor or the photo publication journal. */
public class MediaVideoStore implements VideoStore {
    private static final Object LOCK = new Object();
    private final Context context;
    private final File journal;
    public MediaVideoStore(Context context) { this.context = context.getApplicationContext(); journal = context.getFileStreamPath("video-publications"); }
    public File createCapture(String id) throws IOException {
        File dir = new File(context.getFilesDir(), "camera");
        if (!dir.isDirectory() && !dir.mkdirs()) throw new IOException("No se pudo crear la carpeta de cámara");
        File file = new File(dir, MediaPhotoStore.safe(id)+".mp4");
        if (!file.createNewFile()) throw new IOException("La grabación ya existe"); return file;
    }
    public long validate(File file) throws IOException {
        if (file.length() <= 0 || file.length() > MAX_BYTES) throw new IOException("El video está vacío o supera 200 MB");
        try (MediaMetadataRetriever reader = new MediaMetadataRetriever()) {
            reader.setDataSource(file.getPath());
            long duration = Long.parseLong(reader.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
            if (duration <= 0 || duration > MAX_DURATION_MILLIS
                    || !"yes".equals(reader.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO))
                    || !"yes".equals(reader.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO)))
                throw new IOException("El video está incompleto, no tiene audio o supera cinco minutos");
            return duration;
        } catch (RuntimeException e) { throw new IOException("La grabación no está finalizada; puedes descartarla o reintentar", e); }
    }
    @SuppressWarnings("unchecked") private HashMap<String, Publication> publications() throws IOException {
        try {
            Object value = AtomicObjects.read(journal);
            return value == null ? new HashMap<>() : (HashMap<String, Publication>) value;
        } catch (ClassNotFoundException | RuntimeException e) { throw new IOException("El registro de videos se conserva para recuperación", e); }
    }
    private static Uri collection() { return MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY); }
    public String publish(File file, String inspection, String name, String id) throws IOException {
        synchronized (LOCK) {
            HashMap<String, Publication> records = publications(); Publication publication = records.get(id);
            if (publication != null && publication.complete) return publication.uri;
            String folder = "Movies/Inspecciones/" + MediaPhotoStore.safe(inspection) + "/";
            String display = MediaPhotoStore.safe(name.replaceFirst("(?i)\\.mp4$", "")) + "-" + id + ".mp4";
            Uri uri = publication == null ? find(folder, display) : Uri.parse(publication.uri);
            if (uri == null) {
                ContentValues values = new ContentValues(); values.put(MediaStore.MediaColumns.DISPLAY_NAME, display);
                values.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4"); values.put(MediaStore.MediaColumns.RELATIVE_PATH, folder);
                values.put(MediaStore.MediaColumns.IS_PENDING, 1);
                uri = context.getContentResolver().insert(collection(), values);
                if (uri == null) throw new IOException("No se pudo reservar el video");
            }
            publication = new Publication(uri.toString()); records.put(id, publication); AtomicObjects.write(journal, records);
            try (InputStream in = new FileInputStream(file); OutputStream out = context.getContentResolver().openOutputStream(uri, "wt")) {
                if (out == null) throw new IOException("No se pudo escribir el video");
                byte[] buffer = new byte[32768]; int count; long copied = 0;
                while ((count = in.read(buffer)) != -1) { out.write(buffer, 0, count); copied += count; }
                if (copied == 0) throw new IOException("El video está vacío"); out.flush();
            } catch (RuntimeException e) { throw new IOException("No se pudo publicar el video", e); }
            ContentValues ready = new ContentValues(); ready.put(MediaStore.MediaColumns.IS_PENDING, 0);
            if (context.getContentResolver().update(uri, ready, null, null) != 1) throw new IOException("No se pudo finalizar el video");
            publication.complete = true; AtomicObjects.write(journal, records); return publication.uri;
        }
    }
    private Uri find(String folder, String name) {
        try (Cursor cursor = context.getContentResolver().query(collection(), new String[]{MediaStore.MediaColumns._ID},
                MediaStore.MediaColumns.RELATIVE_PATH+"=? AND "+MediaStore.MediaColumns.DISPLAY_NAME+"=?", new String[]{folder,name}, null)) {
            return cursor != null && cursor.moveToFirst() ? Uri.withAppendedPath(collection(), cursor.getString(0)) : null;
        }
    }
    public void delete(String reference) throws IOException {
        try {
            if (reference == null || !reference.startsWith("content://") || context.getContentResolver().delete(Uri.parse(reference), null, null) != 1)
                throw new IOException("No se pudo borrar el video");
        } catch (RuntimeException e) { throw new IOException("No se pudo borrar el video", e); }
    }
    private static class Publication implements Serializable {
        private static final long serialVersionUID = 1L;
        final String uri; boolean complete;
        Publication(String uri) { this.uri = uri; }
    }
}
