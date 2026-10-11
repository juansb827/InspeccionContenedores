package com.juans.inspeccion.Mundo.Storage;

import android.graphics.Bitmap;
import android.net.Uri;
import com.juans.inspeccion.Mundo.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import java.io.*;
import java.util.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, application = android.app.Application.class)
public class LegacyPhotoImporterTest {
    @Test public void importPreservesBytesNamesAndIsIdempotent() throws Exception {
        FakePhotos photos = new FakePhotos();
        LegacyPhotoImporter importer = new LegacyPhotoImporter(RuntimeEnvironment.getApplication(), photos);
        Inspeccion inspection = new Inspeccion();
        String path = "/storage/emulated/0/Inspecciones/EIR/old.jpg";
        inspection.getFotosGenerales().getFotos().add(path);
        ArrayList<Inspeccion> values = new ArrayList<>(); values.add(inspection);
        byte[] original = {1, 2, 3, 4};
        assertTrue(importer.importAll(relative -> new ByteArrayInputStream(original), values).isEmpty());
        String uri = inspection.getFotosGenerales().getFotos().get(0);
        assertArrayEquals(original, photos.bytes.get(uri));
        assertEquals("old.jpg", inspection.getFotosGenerales().remoteName(uri, photos));
        inspection.getFotosGenerales().getFotos().set(0, path); // Interrupted before saving the draft.
        assertTrue(importer.importAll(relative -> { throw new IOException("must reuse mapping"); }, values).isEmpty());
        assertEquals(1, photos.imports);
        assertEquals(uri, inspection.getFotosGenerales().getFotos().get(0));
    }
    @Test public void missingOrRevokedSourceKeepsOldReference() throws Exception {
        Inspeccion inspection = new Inspeccion();
        String path = "/sdcard/Inspecciones/EIR/missing.jpg";
        inspection.getFotosGenerales().getFotos().add(path);
        LegacyPhotoImporter importer = new LegacyPhotoImporter(RuntimeEnvironment.getApplication(), new FakePhotos());
        assertEquals(1, importer.importAll(relative -> { throw new IOException("access revoked"); }, Collections.singleton(inspection)).size());
        assertEquals(path, inspection.getFotosGenerales().getFotos().get(0));
    }
    @Test public void unreleasedAndTraversalPathsAreNotSupported() throws Exception {
        for (String path : new String[]{"/storage/emulated/0/Android/data/app/files/Pictures/Inspecciones/x.jpg", "/sdcard/Inspecciones/../secret"}) {
            try { LegacyPhotoImporter.relativePath(path); fail(); } catch (IOException expected) { }
        }
    }
    static class FakePhotos implements PhotoStore {
        final Map<String, byte[]> bytes = new HashMap<>(); int imports;
        public String importPhoto(InputStream in, String inspection, String name, String id) throws IOException {
            ByteArrayOutputStream out = new ByteArrayOutputStream(); byte[] buffer = new byte[64]; int n;
            while ((n = in.read(buffer)) != -1) out.write(buffer, 0, n);
            String uri = "content://photos/" + (++imports); bytes.put(uri, out.toByteArray()); return uri;
        }
        public InputStream open(String ref) throws IOException {
            if (!bytes.containsKey(ref)) throw new IOException("missing");
            return new ByteArrayInputStream(bytes.get(ref));
        }
        public File createCapture(String id) throws IOException { throw new UnsupportedOperationException(); }
        public Uri captureUri(File f) { throw new UnsupportedOperationException(); }
        public String publish(File f, String i, String n, String id) throws IOException { throw new UnsupportedOperationException(); }
        public String displayName(String ref) { return "old.jpg"; }
        public Bitmap thumbnail(String r, int w, int h) { throw new UnsupportedOperationException(); }
        public void rename(String r, String n) { throw new UnsupportedOperationException(); }
        public void delete(String r) { throw new AssertionError("Import must not delete originals"); }
    }
}
