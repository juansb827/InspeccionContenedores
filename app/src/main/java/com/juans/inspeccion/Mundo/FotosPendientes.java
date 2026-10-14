package com.juans.inspeccion.Mundo;

import android.content.Context;
import com.juans.inspeccion.Mundo.Storage.*;
import com.juans.inspeccion.Mundo.Util.FTPUpload;
import java.io.IOException;

/** Retired entry point: no deserialization, enqueue or transfer. Public media stays untouched. */
public class FotosPendientes {
    public FotosPendientes() {}
    public FotosPendientes(Context context) {}
    public FotosPendientes(Context context,PhotoStore photos) {}
    public void cargarArchivo() { throw retired(); }
    public void guardarArchivo() { throw retired(); }
    public static void invalidate() {}
    public synchronized boolean agregarFotos(Inspeccion inspection) { throw retired(); }
    public synchronized boolean hayPendientes() { return false; }
    public synchronized boolean subirFotosServidor(FTPUpload uploader) throws IOException { throw new IOException("La cola de fotos está retirada; no se envían archivos."); }
    private static StoredInspections.StorageException retired() { return new StoredInspections.StorageException(StorageFormatProbe.Outcome.UNSUPPORTED_OLDER,"La cola de fotos está retirada; no se envían archivos.",null); }
}
