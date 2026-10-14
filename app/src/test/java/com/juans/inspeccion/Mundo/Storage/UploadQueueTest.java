package com.juans.inspeccion.Mundo.Storage;

import com.juans.inspeccion.Mundo.*;
import com.juans.inspeccion.Mundo.Util.FTPUpload;
import org.apache.commons.net.ftp.FTPClient;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import java.io.*;
import java.util.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, application = android.app.Application.class)
public class UploadQueueTest {
    @Test public void retiredQueueNeverEnqueuesOrCallsTransportAndRetainsGallery() throws Exception {
        LegacyPhotoImporterTest.FakePhotos photos=new LegacyPhotoImporterTest.FakePhotos();
        Inspeccion inspection=new Inspeccion(); inspection.getFotosGenerales().agregarFoto("content://photos/1");
        FotosPendientes queue=new FotosPendientes(RuntimeEnvironment.getApplication(),photos);
        try { queue.agregarFotos(inspection); fail(); } catch(StoredInspections.StorageException expected) {}
        Transport transport=new Transport();
        try(FTPUpload uploader=new FTPUpload("fake",21,"user","pass",transport)) {
            try { queue.subirFotosServidor(uploader); fail(); } catch(IOException expected) { assertTrue(expected.getMessage().contains("retirada")); }
        }
        assertFalse(queue.hayPendientes()); assertTrue(transport.paths.isEmpty()); assertFalse(transport.connected);
        assertEquals(Collections.singletonList("content://photos/1"),inspection.getFotosGenerales().getFotos());
    }
    @Test public void transferFailureClosesSourceAndConnection() throws Exception {
        Transport transport = new Transport(); transport.throwOnUpload = true;
        final boolean[] closed = {false};
        InputStream source = new ByteArrayInputStream(new byte[]{1}) { public void close() { closed[0] = true; } };
        try (FTPUpload uploader = new FTPUpload("fake",21,"user","pass",transport)) {
            uploader.conect();
            try { uploader.uploadSingleFile(source,"folder","photo.jpg"); fail(); } catch (IOException expected) { }
        }
        assertTrue(closed[0]); assertFalse(transport.connected);
    }
    @Test public void loginFailureDisconnectsActualClientField() throws Exception {
        Transport transport = new Transport(); transport.rejectLogin = true;
        try { new FTPUpload("fake",21,"user","pass",transport).conect(); fail(); } catch (IOException expected) { }
        assertFalse(transport.connected);
    }
    static class Transport extends FTPClient {
        boolean connected, logout, throwOnUpload, rejectLogin; int failAt = -1;
        final List<String> paths = new ArrayList<>();
        @Override public void connect(String host, int port) { assertEquals("fake", host); connected = true; }
        @Override public boolean isConnected() { return connected; }
        @Override public boolean login(String user, String pass) { return !rejectLogin; }
        @Override public void enterLocalPassiveMode() { }
        @Override public boolean setFileType(int type) { return true; }
        @Override public boolean makeDirectory(String name) { return true; }
        @Override public boolean storeFile(String path, InputStream source) throws IOException {
            paths.add(path); if (throwOnUpload) throw new IOException("failure");
            assertTrue(source.read() > 0); return paths.size() != failAt;
        }
        @Override public boolean logout() { logout = true; return true; }
        @Override public void disconnect() { connected = false; }
    }
}
