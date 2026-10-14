package com.juans.inspeccion.Mundo.Storage;

import com.juans.inspeccion.data.local.AtomicObjects;
import com.juans.inspeccion.data.local.StorageFormatProbe;
import com.juans.inspeccion.data.local.StoredInspections;

import com.juans.inspeccion.Mundo.Album;
import com.juans.inspeccion.Mundo.Inspeccion;
import com.juans.inspeccion.data.local.Pendientes;

import android.content.Context;
import com.juans.inspeccion.Mundo.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import java.io.*;
import java.nio.file.Files;
import java.util.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, application = android.app.Application.class)
public class StorageCompatibilityTest {
    private Context context;
    @Before public void setup() { context = RuntimeEnvironment.getApplication(); }

    private void released(String name) throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/released-d9c52d3/" + name)) {
            assertNotNull(in);
            Files.copy(in, context.getFileStreamPath(name).toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
    }
    // DRAFT-006/UPLOAD-004: intentional cutover replaces old deserialization expectations.
    @Test public void releasedDraftsAndUploadQueueAreIsolatedWithoutDeserialization() throws Exception {
        for(String name:new String[]{StoredInspections.LEGACY_DRAFTS,StoredInspections.UPLOADS}) released(name);
        assertTrue(StoredInspections.read(context,StoredInspections.DRAFTS).isEmpty());
        com.juans.inspeccion.data.local.StorageCutover.Status status=com.juans.inspeccion.data.local.StorageCutover.prepare(context);
        assertEquals(2,status.isolated.size()); assertTrue(status.noticePending);
        for(String name:new String[]{StoredInspections.LEGACY_DRAFTS,StoredInspections.UPLOADS}) assertFalse(context.getFileStreamPath(name).exists());
        Inspeccion current=new Inspeccion(); Pendientes.upsert(current,context);
        assertEquals(current.getPhotoId(),StoredInspections.read(context,StoredInspections.DRAFTS).get(0).getPhotoId());
    }
    @Test public void retiredQueueCannotBeReadOrWritten() throws Exception {
        released(StoredInspections.UPLOADS); byte[] before=Files.readAllBytes(context.getFileStreamPath(StoredInspections.UPLOADS).toPath());
        try { StoredInspections.write(context,StoredInspections.UPLOADS,new ArrayList<>()); fail(); }
        catch(StoredInspections.StorageException e) { assertEquals(StorageFormatProbe.Outcome.UNSUPPORTED_OLDER,e.outcome); }
        assertArrayEquals(before,Files.readAllBytes(context.getFileStreamPath(StoredInspections.UPLOADS).toPath()));
    }
    @Test public void corruptFileCannotBeOverwritten() throws Exception {
        File file = context.getFileStreamPath(StoredInspections.DRAFTS);
        Files.write(file.toPath(), new byte[]{1, 2, 3});
        try {
            StoredInspections.write(context, StoredInspections.DRAFTS, new ArrayList<>());
            fail("Corrupt data must remain recoverable");
        } catch (StoredInspections.StorageException expected) {
            assertArrayEquals(new byte[]{1, 2, 3}, Files.readAllBytes(file.toPath()));
        }
    }
    @Test public void serializationFailurePreservesLastCompleteFile() throws Exception {
        File file = context.getFileStreamPath("atomic-test");
        AtomicObjects.write(file, "complete");
        try { AtomicObjects.write(file, new BrokenObject()); fail(); } catch (IOException expected) { }
        assertEquals("complete", AtomicObjects.read(file));
    }
    @Test public void newInspectionsHaveDistinctIdsAndClearingAlbumsDoesNotDeletePhotos() throws Exception {
        Inspeccion one = new Inspeccion(), two = new Inspeccion();
        assertNotEquals(one.getPhotoId(), two.getPhotoId());
        Album album = one.getFotosGenerales();
        album.agregarFoto("content://photos/1");
        album.agregarFoto("content://photos/1");
        assertEquals(1, album.getFotos_tomadas());
        album.borrarAlbum();
        assertTrue(album.getFotos().isEmpty());
        assertEquals(1, album.getFotos_tomadas());
    }
    @Test public void uuidUpdatesRemainCorrectWhenAnotherDraftWasRemoved() {
        Inspeccion first = new Inspeccion(), second = new Inspeccion();
        Pendientes.upsert(first, context); Pendientes.upsert(second, context);
        Pendientes.remove(first.getPhotoId(), context);
        second.getInformacion().put("edited", "yes"); Pendientes.upsert(second, context);
        ArrayList<Inspeccion> values = StoredInspections.read(context, StoredInspections.DRAFTS);
        assertEquals(1, values.size()); assertEquals(second.getPhotoId(), values.get(0).getPhotoId());
        assertEquals("yes", values.get(0).getInformacion().get("edited"));
    }
    private static class BrokenObject implements Serializable {
        private void writeObject(ObjectOutputStream out) throws IOException { throw new IOException("simulated failure"); }
    }
}
