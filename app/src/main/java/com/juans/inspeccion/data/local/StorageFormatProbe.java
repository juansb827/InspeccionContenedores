package com.juans.inspeccion.data.local;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;

/** Bounded recognition; current JSON includes complete record validation, legacy objects are never read. */
public final class StorageFormatProbe {
    private StorageFormatProbe() {}
    public enum Outcome {
        MISSING, SUPPORTED_SCHEMA, UNSUPPORTED_OLDER, UNSUPPORTED_NEWER,
        CORRUPT, UNKNOWN, IO_ERROR
    }
    public static Outcome inspect(File file, String legacyStoreName) {
        try (InputStream in = Files.newInputStream(file.toPath())) {
            return inspect(in,legacyStoreName);
        } catch (NoSuchFileException e) { return Outcome.MISSING; }
        catch (com.juans.inspeccion.data.local.StrictJson.Invalid e) { return e.outcome; }
        catch (IOException | SecurityException e) { return Outcome.IO_ERROR; }
    }
    /** Caller owns the stream; null means absence, never a generic failed open. */
    public static Outcome inspect(InputStream in, String legacyStoreName) {
        if (in == null) return Outcome.MISSING;
        byte[] bytes;
        try {
            // Recognition needs four bytes, not a legacy payload read. Large old stores and
            // unreadable payloads still have a recognized encoding; isolation performs its own I/O.
            PushbackInputStream buffered=new PushbackInputStream(in,4);
            ByteArrayOutputStream initial=new ByteArrayOutputStream();
            for(int n=0;n<4;n++) { int value=buffered.read(); if(value==-1) break; initial.write(value); }
            byte[] first=initial.toByteArray();
            byte[] legacy={(byte)0xac,(byte)0xed,0,5}; boolean matches=first.length>0;
            for(int n=0;n<first.length;n++) matches &= first[n]==legacy[n];
            boolean location=StoredInspections.LEGACY_DRAFTS.equals(legacyStoreName)||StoredInspections.UPLOADS.equals(legacyStoreName);
            if(matches) return first.length<4 ? (location?Outcome.CORRUPT:Outcome.UNKNOWN) : (location?Outcome.UNSUPPORTED_OLDER:Outcome.UNKNOWN);
            buffered.unread(first);
            bytes=com.juans.inspeccion.data.local.StrictJson.bytes(buffered);
        } catch (com.juans.inspeccion.data.local.StrictJson.Invalid e) { return e.outcome; }
        catch (IOException | SecurityException e) { return Outcome.IO_ERROR; }
        boolean knownLegacy=StoredInspections.LEGACY_DRAFTS.equals(legacyStoreName)
                || StoredInspections.UPLOADS.equals(legacyStoreName);
        byte[] signature={(byte)0xac,(byte)0xed,0,5};
        boolean prefix=bytes.length>0;
        for (int i=0;i<Math.min(bytes.length,signature.length);i++) prefix &= bytes[i]==signature[i];
        if (prefix) {
            if (bytes.length<signature.length) return knownLegacy ? Outcome.CORRUPT : Outcome.UNKNOWN;
            return knownLegacy ? Outcome.UNSUPPORTED_OLDER : Outcome.UNKNOWN;
        }
        if(bytes.length>0 && bytes[0]==(byte)0xac) return Outcome.UNKNOWN;
        try {
            com.juans.inspeccion.data.local.DraftCodec.decode(bytes);
            return Outcome.SUPPORTED_SCHEMA;
        } catch (com.juans.inspeccion.data.local.StrictJson.Invalid e) { return e.outcome; }
    }
}
