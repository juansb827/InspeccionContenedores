package com.juans.inspeccion.Mundo.Storage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;

/** Unwired cutover preflight. Does not deserialize models, write files or validate record semantics. */
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
        catch (IOException | SecurityException e) { return Outcome.IO_ERROR; }
    }
    /** Caller owns the stream; null means absence, never a generic failed open. */
    public static Outcome inspect(InputStream in, String legacyStoreName) {
        if (in == null) return Outcome.MISSING;
        byte[] bytes;
        try {
            ByteArrayOutputStream out=new ByteArrayOutputStream();
            byte[] buffer=new byte[4096];int count;
            while ((count=in.read(buffer)) != -1) out.write(buffer,0,count);
            bytes=out.toByteArray();
        } catch (IOException | SecurityException e) { return Outcome.IO_ERROR; }
        boolean knownLegacy=StoredInspections.DRAFTS.equals(legacyStoreName)
                || StoredInspections.UPLOADS.equals(legacyStoreName);
        byte[] signature={(byte)0xac,(byte)0xed,0,5};
        boolean prefix=bytes.length>0;
        for (int i=0;i<Math.min(bytes.length,signature.length);i++) prefix &= bytes[i]==signature[i];
        if (prefix) {
            if (bytes.length<signature.length) return knownLegacy ? Outcome.CORRUPT : Outcome.UNKNOWN;
            return knownLegacy ? Outcome.UNSUPPORTED_OLDER : Outcome.UNKNOWN;
        }
        String json=new String(bytes,StandardCharsets.UTF_8).trim();
        if (json.isEmpty()) return Outcome.CORRUPT;
        if (!json.startsWith("{")) return Outcome.UNKNOWN;
        try {
            JSONTokener tokens=new JSONTokener(json);
            Object parsed=tokens.nextValue();
            if (!(parsed instanceof JSONObject) || tokens.nextClean()!=0) return Outcome.CORRUPT;
            JSONObject root=(JSONObject)parsed;
            if (!"inspection-drafts".equals(root.opt("format"))) return Outcome.UNKNOWN;
            Object value=root.get("schemaVersion");
            if (!(value instanceof Integer || value instanceof Long)) return Outcome.CORRUPT;
            long version=((Number)value).longValue();
            if (version<0) return Outcome.CORRUPT;
            // Schema 0 is explicitly retired in this proposed draft envelope contract.
            if (version==0) return Outcome.UNSUPPORTED_OLDER;
            if (version>1) return Outcome.UNSUPPORTED_NEWER;
            if (!(root.get("records") instanceof JSONArray)) return Outcome.CORRUPT;
            return Outcome.SUPPORTED_SCHEMA;
        } catch (JSONException e) { return Outcome.CORRUPT; }
    }
}
