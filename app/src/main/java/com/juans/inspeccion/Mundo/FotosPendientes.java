package com.juans.inspeccion.Mundo;

import android.content.Context;
import com.juans.inspeccion.Interfaz.MainActivity;
import com.juans.inspeccion.Mundo.Storage.*;
import com.juans.inspeccion.Mundo.Util.FTPUpload;
import java.io.*;
import java.util.ArrayList;

public class FotosPendientes {
    private final Context context;
    private final PhotoStore photos;
    public FotosPendientes() { this(MainActivity.context); }
    public FotosPendientes(Context context) { this(context, new MediaPhotoStore(context)); }
    public FotosPendientes(Context context, PhotoStore photos) {
        this.context = context.getApplicationContext(); this.photos = photos;
    }
    // Every operation reloads the queue so an import cannot leave a stale cached collection.
    public void cargarArchivo() { StoredInspections.read(context, StoredInspections.UPLOADS); }
    public void guardarArchivo() { cargarArchivo(); }
    public static void invalidate() { }
    public synchronized boolean agregarFotos(Inspeccion inspection) {
        ArrayList<Inspeccion> queue = StoredInspections.read(context, StoredInspections.UPLOADS);
        queue.add(snapshot(inspection));
        StoredInspections.write(context, StoredInspections.UPLOADS, queue);
        return true;
    }
    public synchronized boolean hayPendientes() { return !StoredInspections.read(context, StoredInspections.UPLOADS).isEmpty(); }
    public synchronized boolean subirFotosServidor(FTPUpload uploader) throws IOException {
        ArrayList<Inspeccion> queue = StoredInspections.read(context, StoredInspections.UPLOADS);
        if (queue.isEmpty()) return true;
        Inspeccion inspection = queue.get(queue.size() - 1);
        String parent = inspection.getParentFolder();
        String folder = parent == null ? inspection.getPhotoId() : new File(parent).getName();
        for (Album album : inspection.allAlbums()) {
            while (!album.getFotos().isEmpty()) {
                String reference = album.getFotos().get(0);
                String filename = album.remoteName(reference, photos);
                try (InputStream in = photos.open(reference)) {
                    if (!uploader.uploadSingleFile(in, folder, filename)) return false;
                }
                // Only the upload queue is acknowledged. Gallery media and the original album are retained.
                album.getFotos().remove(0);
                StoredInspections.write(context, StoredInspections.UPLOADS, queue);
            }
        }
        queue.remove(queue.size() - 1);
        StoredInspections.write(context, StoredInspections.UPLOADS, queue);
        return true;
    }
    private static Inspeccion snapshot(Inspeccion inspection) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (ObjectOutputStream out = new ObjectOutputStream(bytes)) { out.writeObject(inspection); }
            try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { return (Inspeccion) in.readObject(); }
        } catch (IOException | ClassNotFoundException e) { throw new StoredInspections.StorageException("No se pudo preparar la cola de fotos", e); }
    }
}
