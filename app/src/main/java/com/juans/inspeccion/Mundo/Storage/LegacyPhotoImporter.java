package com.juans.inspeccion.Mundo.Storage;

import android.content.Context;
import android.net.Uri;
import androidx.documentfile.provider.DocumentFile;
import com.juans.inspeccion.Mundo.Album;
import com.juans.inspeccion.Mundo.Inspeccion;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Imports only released /Inspecciones paths. Source documents are never modified. */
public final class LegacyPhotoImporter {
    public interface Source { InputStream open(String relativePath) throws IOException; }
    private final Context context;
    private final PhotoStore photos;
    private final File mappingFile;
    private final HashMap<String, String> verified = new HashMap<>();

    public LegacyPhotoImporter(Context context, PhotoStore photos) {
        this.context = context.getApplicationContext();
        this.photos = photos;
        mappingFile = context.getFileStreamPath("legacy-photo-imports");
    }

    public static String relativePath(String reference) throws IOException {
        if (reference == null || reference.contains("/Android/data/") || reference.contains("/../"))
            throw new IOException("La ruta no pertenece a la versión publicada");
        int marker = reference.indexOf("/Inspecciones/");
        if (marker < 0 || !(reference.startsWith("/storage/") || reference.startsWith("/sdcard/")))
            throw new IOException("La ruta no pertenece a la versión publicada");
        String path = reference.substring(marker + "/Inspecciones/".length());
        if (path.isEmpty() || path.startsWith("/") || path.contains("//")) throw new IOException("Ruta inválida");
        return path;
    }

    public static boolean hasLegacyReferences(Collection<Inspeccion> inspections) {
        for (Inspeccion inspection : inspections) for (Album album : inspection.allAlbums())
            for (String ref : album.getFotos()) if (!ref.startsWith("content://")) return true;
        return false;
    }

    public static Source selectedFolder(Context context, Uri tree) throws IOException {
        DocumentFile root = DocumentFile.fromTreeUri(context, tree);
        if (root == null || !root.isDirectory() || !"Inspecciones".equals(root.getName()))
            throw new IOException("Seleccione la carpeta antigua Inspecciones");
        return path -> {
            DocumentFile file = root;
            for (String part : path.split("/")) {
                if (file == null) break;
                file = file.findFile(part);
            }
            if (file == null || !file.isFile()) throw new FileNotFoundException("No se encontró " + path);
            try {
                InputStream in = context.getContentResolver().openInputStream(file.getUri());
                if (in == null) throw new IOException("No se pudo leer " + path);
                return in;
            } catch (SecurityException e) { throw new IOException("Vuelva a seleccionar la carpeta para conceder acceso", e); }
        };
    }

    public synchronized ArrayList<String> importAll(Source source, Collection<Inspeccion> inspections) throws IOException {
        HashMap<String, String> mapping = readMapping();
        verified.clear();
        ArrayList<String> failures = new ArrayList<>();
        for (Inspeccion inspection : inspections) for (Album album : inspection.allAlbums()) {
            for (String oldRef : new ArrayList<>(album.getFotos())) {
                if (oldRef.startsWith("content://")) continue;
                try {
                    String path = relativePath(oldRef);
                    String newRef = mapping.get(oldRef);
                    String name = path.substring(path.lastIndexOf('/') + 1);
                    if (newRef == null) {
                        String id = "legacy-" + UUID.nameUUIDFromBytes(oldRef.getBytes(StandardCharsets.UTF_8));
                        try (InputStream in = source.open(path)) {
                            newRef = photos.importPhoto(in, inspection.getPhotoId(), name, id);
                        }
                        // Reopen before changing references: a URI alone is not proof of a readable copy.
                        try (InputStream in = photos.open(newRef)) {
                            if (in.read() == -1) throw new IOException("La copia está vacía");
                        }
                        mapping.put(oldRef, newRef);
                        AtomicObjects.write(mappingFile, mapping);
                    } else {
                        try (InputStream in = photos.open(newRef)) {
                            if (in.read() == -1) throw new IOException("La copia no está disponible");
                        }
                    }
                    verified.put(oldRef, newRef);
                    String remoteName = album.recordedRemoteName(oldRef);
                    album.replaceReference(oldRef, newRef);
                    album.setRemoteName(newRef, remoteName == null ? name : remoteName);
                } catch (IOException | RuntimeException e) { failures.add(oldRef + ": " + e.getMessage()); }
            }
        }
        return failures;
    }

    /** Persist each store separately; the mapping makes recovery safe between the two writes. */
    public synchronized ArrayList<String> importSaved(Source source) throws IOException {
        if (source == null) return new ArrayList<>(); // A canceled selection must not even upgrade/write the stores.
        ArrayList<Inspeccion> drafts = StoredInspections.read(context, StoredInspections.DRAFTS);
        ArrayList<Inspeccion> uploads = StoredInspections.read(context, StoredInspections.UPLOADS);
        ArrayList<Inspeccion> all = new ArrayList<>(drafts);
        all.addAll(uploads);
        ArrayList<String> failures = importAll(source, all);
        commitVerified(StoredInspections.DRAFTS);
        commitVerified(StoredInspections.UPLOADS);
        return failures;
    }

    private void commitVerified(String name) {
        synchronized (StoredInspections.class) {
            // Copying may take minutes. Merge references into the latest data instead of overwriting its snapshot.
            ArrayList<Inspeccion> current = StoredInspections.read(context, name);
            boolean changed = false;
            for (Inspeccion inspection : current) for (Album album : inspection.allAlbums()) {
                for (String old : new ArrayList<>(album.getFotos())) {
                    String replacement = verified.get(old);
                    if (replacement == null) continue;
                    String remote = album.recordedRemoteName(old);
                    album.replaceReference(old, replacement);
                    album.setRemoteName(replacement, remote == null ? old.substring(old.lastIndexOf('/') + 1) : remote);
                    changed = true;
                }
            }
            if (changed) StoredInspections.write(context, name, current);
        }
    }

    @SuppressWarnings("unchecked") private HashMap<String, String> readMapping() throws IOException {
        try {
            Object value = AtomicObjects.read(mappingFile);
            return value == null ? new HashMap<>() : (HashMap<String, String>) value;
        } catch (ClassNotFoundException | RuntimeException e) { throw new IOException("No se pudo leer el registro de importación", e); }
    }
}
