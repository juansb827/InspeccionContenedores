package com.juans.inspeccion.Mundo.Storage;

import android.content.Context;
import android.content.Intent;
import android.util.AtomicFile;
import androidx.core.content.FileProvider;
import java.io.*;

public final class ExportFiles {
    public interface Writer { void write(OutputStream out) throws Exception; }
    private ExportFiles() {}
    public static File destination(Context context, String name) throws IOException {
        File dir = new File(context.getFilesDir(), "exports");
        if (!dir.isDirectory() && !dir.mkdirs()) throw new IOException("No se pudo crear la carpeta de exportación");
        return new File(dir, MediaPhotoStore.safe(name));
    }
    public static File write(File file, Writer writer) throws IOException {
        AtomicFile atomic = new AtomicFile(file);
        FileOutputStream output = null;
        try {
            output = atomic.startWrite();
            OutputStream owned = output;
            // Document libraries may close their output; AtomicFile owns the actual stream.
            writer.write(new FilterOutputStream(owned) { @Override public void close() throws IOException { flush(); } });
            output.flush();
            output.getFD().sync();
            atomic.finishWrite(output);
            return file;
        } catch (Exception e) {
            if (output != null) atomic.failWrite(output);
            throw new IOException("No se pudo generar el archivo; el anterior se conserva", e);
        }
    }
    public static boolean open(Context context, File file, String mime) {
        try { context.startActivity(viewIntent(context, file, mime)); return true; }
        catch (android.content.ActivityNotFoundException e) {
            android.widget.Toast.makeText(context, "No hay una aplicación para abrir este archivo", android.widget.Toast.LENGTH_LONG).show();
            return false;
        }
    }
    public static Intent viewIntent(Context context, File file, String mime) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(FileProvider.getUriForFile(context, MediaPhotoStore.authority(context), file), mime);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        if (!(context instanceof android.app.Activity)) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }
}
