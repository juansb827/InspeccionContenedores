package com.juans.inspeccion.data.local;

import org.json.*;
import java.io.*;
import java.nio.*;
import java.nio.charset.*;
import java.nio.file.Files;
import com.juans.inspeccion.Mundo.Storage.StorageFormatProbe.Outcome;

/** Bounded, strict UTF-8/JSON input. Android's JSONTokener alone accepts non-JSON syntax. */
public final class StrictJson {
    public static final int MAX_BYTES = 8 * 1024 * 1024;
    private StrictJson() {}
    public static byte[] bytes(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(); byte[] buffer = new byte[4096]; int n;
        while ((n = in.read(buffer)) != -1) {
            if (out.size() + n > MAX_BYTES) throw new Invalid(Outcome.CORRUPT, "Límite de almacenamiento excedido");
            out.write(buffer, 0, n);
        }
        return out.toByteArray();
    }
    public static JSONObject read(File file) throws IOException {
        try (InputStream in = Files.newInputStream(file.toPath())) { return object(bytes(in)); }
    }
    public static JSONObject object(byte[] bytes) {
        final String value;
        try { value = StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(bytes)).toString(); }
        catch (CharacterCodingException e) { throw new Invalid(Outcome.CORRUPT, "UTF-8 inválido"); }
        if (bytes.length > MAX_BYTES) throw new Invalid(Outcome.CORRUPT, "Límite de almacenamiento excedido");
        if (!value.trim().startsWith("{")) throw new Invalid(value.trim().isEmpty() ? Outcome.CORRUPT : Outcome.UNKNOWN, "Formato desconocido");
        try {
            Grammar grammar = new Grammar(value); grammar.value(0); grammar.space();
            if (grammar.p != value.length()) throw new IllegalArgumentException();
            return new JSONObject(value);
        } catch (JSONException | IllegalArgumentException e) { throw new Invalid(Outcome.CORRUPT, "JSON inválido"); }
    }
    public static final class Invalid extends RuntimeException {
        public final Outcome outcome;
        public Invalid(Outcome outcome, String message) { super(message); this.outcome = outcome; }
    }
    private static final class Grammar {
        final String s; int p;
        Grammar(String s) { this.s = s; }
        void space() { while (p < s.length() && " \n\r\t".indexOf(s.charAt(p)) >= 0) p++; }
        char take() { if (p >= s.length()) throw new IllegalArgumentException(); return s.charAt(p++); }
        void expect(char c) { space(); if (take() != c) throw new IllegalArgumentException(); }
        boolean end(char c) { space(); if (p < s.length() && s.charAt(p) == c) { p++; return true; } return false; }
        void value(int depth) throws JSONException {
            if (depth > 32) throw new IllegalArgumentException(); space(); if (p >= s.length()) throw new IllegalArgumentException();
            char c = s.charAt(p);
            if (c == '{') {
                p++; java.util.HashSet<String> keys = new java.util.HashSet<>(); if (end('}')) return;
                do { space(); int start = p; string(); String key = (String)new JSONTokener(s.substring(start,p)).nextValue();
                    if (!keys.add(key)) throw new IllegalArgumentException(); expect(':'); value(depth+1);
                    if (end('}')) return; expect(','); } while (true);
            } else if (c == '[') { p++; if (end(']')) return; do { value(depth+1); if (end(']')) return; expect(','); } while (true);
            } else if (c == '"') string();
            else if (c == 't') literal("true"); else if (c == 'f') literal("false"); else if (c == 'n') literal("null");
            else number();
        }
        void literal(String v) { if (!s.startsWith(v,p)) throw new IllegalArgumentException(); p += v.length(); }
        void string() {
            if (take() != '"') throw new IllegalArgumentException(); int start = p;
            while (true) {
                char c = take(); if (p-start > 32768) throw new IllegalArgumentException(); if (c == '"') return;
                if (c < 32) throw new IllegalArgumentException();
                if (c == '\\') { c=take(); if (c == 'u') {
                    for (int i=0;i<4;i++) if (Character.digit(take(),16) < 0) throw new IllegalArgumentException();
                } else if ("\"\\/bfnrt".indexOf(c) < 0) throw new IllegalArgumentException(); }
                // Reject unpaired UTF-16, including escaped pairs after JSON decoding in the codec.
                else if (Character.isHighSurrogate(c)) { if (!Character.isLowSurrogate(take())) throw new IllegalArgumentException(); }
                else if (Character.isLowSurrogate(c)) throw new IllegalArgumentException();
            }
        }
        void number() {
            int start=p; if (p<s.length() && s.charAt(p)=='-') p++;
            if (p<s.length() && s.charAt(p)=='0') p++;
            else { digits(true); }
            if (p<s.length() && s.charAt(p)=='.') { p++; digits(true); }
            if (p<s.length() && (s.charAt(p)=='e'||s.charAt(p)=='E')) { p++; if (p<s.length() && (s.charAt(p)=='+'||s.charAt(p)=='-')) p++; digits(true); }
            if (p==start) throw new IllegalArgumentException();
        }
        void digits(boolean required) { int start=p; while (p<s.length() && s.charAt(p)>='0'&&s.charAt(p)<='9') p++; if (required&&start==p) throw new IllegalArgumentException(); }
    }
}
