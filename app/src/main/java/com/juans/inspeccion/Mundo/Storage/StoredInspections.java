package com.juans.inspeccion.Mundo.Storage;

import android.content.Context;
import com.juans.inspeccion.Mundo.Inspeccion;
import java.io.*;
import java.util.ArrayList;

/** The released Java-serialization format is retained; corrupt files are never overwritten. */
public final class StoredInspections {
    public static final String DRAFTS = "listaPendientes";
    public static final String UPLOADS = "fotosPendientes";
    private StoredInspections() {}

    public static synchronized ArrayList<Inspeccion> read(Context context, String name) {
        try {
            Object value = AtomicObjects.read(context.getFileStreamPath(name));
            if (value == null) {
                if (context.getFileStreamPath(name).exists()) throw new IOException("Colección inválida");
                return new ArrayList<>();
            }
            if (!(value instanceof ArrayList)) throw new IOException("Formato desconocido");
            ArrayList<Inspeccion> result = new ArrayList<>();
            boolean upgraded = false;
            for (Object item : (ArrayList<?>) value) {
                if (!(item instanceof Inspeccion)) throw new IOException("Inspección inválida");
                Inspeccion inspection = (Inspeccion) item;
                upgraded |= inspection.initializePhotoIds();
                result.add(inspection);
            }
            if (upgraded) AtomicObjects.write(context.getFileStreamPath(name), result);
            return result;
        } catch (IOException | ClassNotFoundException | RuntimeException e) {
            throw new StorageException("No se pudo leer " + name + ". El archivo se conserva para recuperación.", e);
        }
    }

    public static synchronized void write(Context context, String name, ArrayList<Inspeccion> items) {
        // Verify the old file before replacing it, even if a previous load failed.
        read(context, name);
        try {
            for (Inspeccion inspection : items) inspection.initializePhotoIds();
            AtomicObjects.write(context.getFileStreamPath(name), items);
        } catch (IOException e) {
            throw new StorageException("No se pudo guardar " + name + ". Los datos anteriores se conservan.", e);
        }
    }

    public static final class StorageException extends RuntimeException {
        public StorageException(String message, Throwable cause) { super(message, cause); }
    }
}
