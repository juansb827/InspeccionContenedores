package com.juans.inspeccion.Mundo.Storage;

import android.util.AtomicFile;
import java.io.*;

/** Preserves the last complete file, including when serialization or fsync fails. */
public final class AtomicObjects {
    private AtomicObjects() {}

    public static Object read(File file) throws IOException, ClassNotFoundException {
        AtomicFile atomic = new AtomicFile(file);
        if (!file.exists() && !new File(file + ".bak").exists()) return null;
        try (ObjectInputStream in = new ObjectInputStream(atomic.openRead())) {
            return in.readObject();
        }
    }

    public static void write(File file, Serializable value) throws IOException {
        AtomicFile atomic = new AtomicFile(file);
        FileOutputStream out = null;
        try {
            out = atomic.startWrite();
            ObjectOutputStream objects = new ObjectOutputStream(out);
            objects.writeObject(value);
            objects.flush();
            out.getFD().sync();
            atomic.finishWrite(out);
        } catch (IOException | RuntimeException e) {
            if (out != null) atomic.failWrite(out);
            throw e;
        }
    }
}
