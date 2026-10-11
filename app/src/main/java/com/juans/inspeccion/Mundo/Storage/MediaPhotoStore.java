package com.juans.inspeccion.Mundo.Storage;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import java.io.*;
import java.util.HashMap;

/** All normal photo consumers use content URIs. Raw released paths belong to the importer. */
public class MediaPhotoStore implements PhotoStore {
    private final Context context;
    private final File journalFile;
    // Publication IDs make a retry reuse its destination, even after process death.
    private static final Object PUBLICATION_LOCK = new Object();

    public MediaPhotoStore(Context context) {
        this.context = context.getApplicationContext();
        journalFile = context.getFileStreamPath("photo-publications");
    }

    public static String authority(Context context) { return context.getPackageName() + ".fileprovider"; }

    public static boolean needsStoragePermission(Context context) {
        return Build.VERSION.SDK_INT <= 28 && ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED;
    }

    @Override public File createCapture(String id) throws IOException {
        File dir = new File(context.getFilesDir(), "camera");
        if (!dir.isDirectory() && !dir.mkdirs()) throw new IOException("No se pudo crear la carpeta de cámara");
        File file = new File(dir, safe(id) + ".jpg");
        if (!file.createNewFile()) throw new IOException("La captura ya existe");
        return file;
    }

    @Override public Uri captureUri(File file) {
        return FileProvider.getUriForFile(context, authority(context), file);
    }

    @Override public String publish(File original, String inspection, String name, String id) throws IOException {
        // A completed publication needs no second decode/compression.
        synchronized (PUBLICATION_LOCK) {
            Publication existing = journal().get(id);
            if (existing != null && existing.complete) return existing.uri;
        }
        File processed = ImageProcessor.process(original);
        try (InputStream in = new FileInputStream(processed)) {
            return importPhoto(in, inspection, name.replaceFirst("(?i)\\.jpg$", "") + "-" + id + ".jpg", id);
        } finally { processed.delete(); }
    }

    @Override public String importPhoto(InputStream source, String inspection, String name, String id) throws IOException {
        synchronized (PUBLICATION_LOCK) {
            HashMap<String, Publication> journal = journal();
            Publication entry = journal.get(id);
            if (entry != null && entry.complete) return entry.uri;
            if (needsStoragePermission(context)) throw new IOException("Se requiere permiso para guardar fotos en este Android");
            String folder = "Pictures/Inspecciones/" + safe(inspection) + "/";
            String base = safe(name.replaceFirst("(?i)\\.jpg$", ""));
            String display = base + ".jpg";
            if (Build.VERSION.SDK_INT <= 28) {
                File dir = new File(Environment.getExternalStorageDirectory(), folder);
                if (!dir.isDirectory() && !dir.mkdirs()) throw new IOException("No se pudo crear la carpeta pública");
                File destination = new File(dir, display);
                ExportFiles.write(destination, out -> copyPhoto(source, out));
                // Old MediaStore must register only the fully written public file.
                Uri registered = findDestination(folder, display);
                if (registered == null) registered = createDestination(folder, display);
                Publication complete = new Publication(registered.toString(), true);
                journal.put(id, complete);
                AtomicObjects.write(journalFile, journal);
                return complete.uri;
            }
            Uri uri = entry == null ? findDestination(folder, display) : Uri.parse(entry.uri);
            if (uri == null) uri = createDestination(folder, display);
            entry = new Publication(uri.toString(), false);
            journal.put(id, entry);
            AtomicObjects.write(journalFile, journal);
            try (OutputStream out = context.getContentResolver().openOutputStream(uri, "wt")) {
                if (out == null) throw new IOException("No se pudo abrir la foto para escritura");
                copyPhoto(source, out);
                out.flush();
            } catch (RuntimeException e) { throw new IOException("No se pudo publicar la foto", e); }
            if (Build.VERSION.SDK_INT >= 29) {
                ContentValues ready = new ContentValues();
                ready.put(MediaStore.Images.Media.IS_PENDING, 0);
                if (context.getContentResolver().update(uri, ready, null, null) != 1) throw new IOException("No se pudo publicar la foto");
            }
            entry.complete = true;
            AtomicObjects.write(journalFile, journal);
            return entry.uri;
        }
    }

    private static void copyPhoto(InputStream source, OutputStream out) throws IOException {
        byte[] buffer = new byte[32768]; long total = 0; int count;
        while ((count = source.read(buffer)) != -1) { out.write(buffer, 0, count); total += count; }
        if (total == 0) throw new IOException("La foto está vacía");
    }

    private Uri findDestination(String folder, String display) throws IOException {
        String column = Build.VERSION.SDK_INT >= 29 ? MediaStore.Images.Media.RELATIVE_PATH : MediaStore.Images.Media.DATA;
        String location = Build.VERSION.SDK_INT >= 29 ? folder : new File(Environment.getExternalStorageDirectory(), folder + display).getPath();
        try (Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID}, column + "=? AND " + MediaStore.Images.Media.DISPLAY_NAME + "=?",
                new String[]{location, display}, null)) {
            if (cursor != null && cursor.moveToFirst()) return Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cursor.getString(0));
            return null;
        } catch (RuntimeException e) { throw new IOException("No se pudo consultar la biblioteca", e); }
    }

    private Uri createDestination(String folder, String display) throws IOException {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, display);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        if (Build.VERSION.SDK_INT >= 29) {
            values.put(MediaStore.Images.Media.RELATIVE_PATH, folder);
            values.put(MediaStore.Images.Media.IS_PENDING, 1);
        } else {
            File dir = new File(Environment.getExternalStorageDirectory(), folder);
            if (!dir.isDirectory() && !dir.mkdirs()) throw new IOException("No se pudo crear la carpeta pública");
            values.put(MediaStore.Images.Media.DATA, new File(dir, display).getPath());
        }
        try {
            Uri result = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (result == null) throw new IOException("No se pudo reservar la foto");
            return result;
        } catch (RuntimeException e) { throw new IOException("No se pudo crear la foto", e); }
    }

    @SuppressWarnings("unchecked") private HashMap<String, Publication> journal() throws IOException {
        try {
            Object value = AtomicObjects.read(journalFile);
            return value == null ? new HashMap<>() : (HashMap<String, Publication>) value;
        } catch (ClassNotFoundException | RuntimeException e) { throw new IOException("No se pudo leer el registro de fotos", e); }
    }

    @Override public InputStream open(String reference) throws IOException {
        Uri uri = contentUri(reference);
        try {
            InputStream in = context.getContentResolver().openInputStream(uri);
            if (in == null) throw new IOException("Foto no disponible");
            return in;
        } catch (RuntimeException e) { throw new IOException("Foto no disponible; revise el acceso o importe las fotos antiguas", e); }
    }

    @Override public String displayName(String reference) throws IOException {
        try (Cursor cursor = context.getContentResolver().query(contentUri(reference),
                new String[]{MediaStore.Images.Media.DISPLAY_NAME}, null, null, null)) {
            if (cursor == null || !cursor.moveToFirst()) throw new IOException("Foto no disponible");
            return cursor.getString(0);
        } catch (RuntimeException e) { throw new IOException("No se pudo leer el nombre de la foto", e); }
    }

    @Override public Bitmap thumbnail(String reference, int width, int height) throws IOException {
        return ImageProcessor.decode(() -> open(reference), width, height);
    }

    @Override public void rename(String reference, String name) throws IOException {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, safe(name));
        try {
            if (context.getContentResolver().update(contentUri(reference), values, null, null) != 1) throw new IOException("No se pudo actualizar el nombre de la foto");
        } catch (RuntimeException e) { throw new IOException("No se pudo actualizar el nombre de la foto", e); }
    }

    @Override public void delete(String reference) throws IOException {
        try {
            if (context.getContentResolver().delete(contentUri(reference), null, null) != 1) throw new IOException("No se pudo borrar la foto");
        } catch (RuntimeException e) { throw new IOException("No se pudo borrar la foto", e); }
    }

    private static Uri contentUri(String reference) throws IOException {
        if (reference == null || !reference.startsWith("content://")) throw new IOException("Importe las fotos antiguas desde Configuración");
        return Uri.parse(reference);
    }

    public static String safe(String value) {
        String result = value == null ? "foto" : value.replaceAll("[^a-zA-Z0-9_#.-]", "_");
        return result.isEmpty() || result.equals(".") || result.equals("..") ? "foto" : result;
    }

    private static final class Publication implements Serializable {
        private static final long serialVersionUID = 1L;
        final String uri;
        boolean complete;
        Publication(String uri, boolean complete) { this.uri = uri; this.complete = complete; }
    }
}
