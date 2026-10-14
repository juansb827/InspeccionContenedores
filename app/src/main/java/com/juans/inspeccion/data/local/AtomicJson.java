package com.juans.inspeccion.data.local;

import android.util.AtomicFile;
import java.io.*;
import java.nio.file.Files;
import com.juans.inspeccion.data.local.StorageFormatProbe.Outcome;

/** Validates the committed candidate before allowing Android AtomicFile to recover it. */
public final class AtomicJson {
    private AtomicJson() {}
    public interface Validator { void validate(byte[] bytes); }
    public interface Boundary { void beforeCommit() throws IOException; }
    public static byte[] read(File file, Validator validator) throws IOException {
        File backup=new File(file+".bak"), staging=new File(file+".new");
        File selected=backup.exists() ? backup : file;
        if(!selected.exists()) {
            if(staging.exists()) throw new StrictJson.Invalid(Outcome.CORRUPT,"Escritura interrumpida sin archivo confirmado; se conserva .new");
            // Opening distinguishes true absence from inaccessible directories / broken paths.
            try(InputStream in=Files.newInputStream(file.toPath())) { return StrictJson.bytes(in); }
            catch(java.nio.file.NoSuchFileException e) { return null; }
        }
        byte[] bytes; try(InputStream in=Files.newInputStream(selected.toPath())) { bytes=StrictJson.bytes(in); }
        validator.validate(bytes);
        if(backup.exists()) {
            // openRead recovers the validated backup; corrupt evidence is never discarded here.
            try(InputStream in=new AtomicFile(file).openRead()) { bytes=StrictJson.bytes(in); }
            validator.validate(bytes);
        }
        return bytes;
    }
    public static void write(File file, byte[] bytes) throws IOException { write(file,bytes,()->{}); }
    public static void write(File file, byte[] bytes, Boundary boundary) throws IOException {
        AtomicFile atomic=new AtomicFile(file); FileOutputStream out=null;
        try {
            out=atomic.startWrite(); out.write(bytes); out.flush(); out.getFD().sync(); boundary.beforeCommit(); atomic.finishWrite(out);
            // AtomicFile does not surface rename/fsync errors. Verify publication before acknowledging it.
            byte[] committed; try(InputStream in=Files.newInputStream(file.toPath())) { committed=StrictJson.bytes(in); }
            if(!java.util.Arrays.equals(bytes,committed)) throw new IOException("No se confirmó la escritura atómica");
        } catch(IOException | RuntimeException e) { if(out!=null) atomic.failWrite(out); throw e; }
    }
}
