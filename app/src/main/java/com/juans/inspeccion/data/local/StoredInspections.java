package com.juans.inspeccion.data.local;

import com.juans.inspeccion.data.local.AtomicJson;
import com.juans.inspeccion.data.local.DraftCodec;
import com.juans.inspeccion.data.local.StorageCutover;
import com.juans.inspeccion.data.local.StrictJson;

import android.content.Context;
import com.juans.inspeccion.Mundo.Inspeccion;
import com.juans.inspeccion.data.local.*;
import com.juans.inspeccion.data.local.StorageFormatProbe.Outcome;
import java.io.*;
import java.util.ArrayList;

/** Current drafts only. The retired upload namespace is deliberately inaccessible. */
public final class StoredInspections {
    public static final String DRAFTS="inspection-drafts-v1.json";
    public static final String LEGACY_DRAFTS="listaPendientes";
    public static final String UPLOADS="fotosPendientes";
    private StoredInspections() {}
    public static synchronized ArrayList<Inspeccion> read(Context context,String name) {
        if(!DRAFTS.equals(name)) throw new StorageException(Outcome.UNSUPPORTED_OLDER,"La cola de fotos y los borradores anteriores están retirados; no se envían.",null);
        try {
            StorageCutover.Status status=StorageCutover.prepare(context);
            // A valid older backup must never discard a positively recognized newer candidate.
            for(java.util.Map.Entry<String,Outcome> entry:status.unresolved.entrySet())
                if(entry.getKey().startsWith(DRAFTS)&&entry.getValue()==Outcome.UNSUPPORTED_NEWER)
                    throw new StorageException(Outcome.UNSUPPORTED_NEWER,message(Outcome.UNSUPPORTED_NEWER),null);
            byte[] bytes=AtomicJson.read(context.getFileStreamPath(DRAFTS),DraftCodec::decode);
            if(bytes!=null) return DraftCodec.decode(bytes);
            // An unrecognized old draft is evidence, never permission to declare the store empty.
            for(java.util.Map.Entry<String,Outcome> entry:status.unresolved.entrySet()) if(entry.getKey().startsWith(LEGACY_DRAFTS))
                throw new StorageException(entry.getValue(),"No se pudo reconocer el borrador anterior ("+entry.getValue()+"). El archivo se conserva.",null);
            return new ArrayList<>();
        } catch(StrictJson.Invalid e) { throw new StorageException(e.outcome,message(e.outcome),e); }
        catch(IOException|SecurityException e) { throw new StorageException(Outcome.IO_ERROR,message(Outcome.IO_ERROR),e); }
    }
    public static synchronized void write(Context context,String name,ArrayList<Inspeccion> items) { write(context,name,items,()->{}); }
    public static synchronized void write(Context context,String name,ArrayList<Inspeccion> items,AtomicJson.Boundary boundary) {
        read(context,name); // Never replace unrecognized/corrupt/current newer data.
        try { byte[] bytes=DraftCodec.encode(items); AtomicJson.write(context.getFileStreamPath(DRAFTS),bytes,boundary); }
        catch(StrictJson.Invalid e) { throw new StorageException(e.outcome,message(e.outcome),e); }
        catch(IOException|SecurityException e) { throw new StorageException(Outcome.IO_ERROR,message(Outcome.IO_ERROR),e); }
    }
    private static String message(Outcome outcome) {
        switch(outcome) {
            case UNSUPPORTED_OLDER: return "Formato anterior no admitido. Los archivos se conservan.";
            case UNSUPPORTED_NEWER: return "Borradores de un formato más reciente. Use una versión compatible; los archivos se conservan.";
            case UNKNOWN: return "Formato de borradores desconocido. El archivo se conserva para recuperación.";
            case CORRUPT: return "Borradores dañados o incompletos. El archivo se conserva para recuperación.";
            default: return "Error de acceso a los borradores. Los archivos se conservan; vuelva a intentarlo.";
        }
    }
    public static final class StorageException extends RuntimeException {
        public final Outcome outcome;
        public StorageException(String message,Throwable cause) { this(Outcome.IO_ERROR,message,cause); }
        public StorageException(Outcome outcome,String message,Throwable cause) { super(message,cause); this.outcome=outcome; }
    }
}
