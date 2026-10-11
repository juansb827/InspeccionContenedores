package com.juans.inspeccion.Mundo.Storage;

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
    @Test public void releasedDraftsAndUploadQueueRemainReadable() throws Exception {
        assertEquals(4164294007417506670L, ObjectStreamClass.lookup(Album.class).getSerialVersionUID());
        assertEquals(7140579573600875404L, ObjectStreamClass.lookup(Inspeccion.class).getSerialVersionUID());
        for (String name : new String[]{StoredInspections.DRAFTS, StoredInspections.UPLOADS}) {
            released(name);
            ArrayList<Inspeccion> values = StoredInspections.read(context, name);
            Inspeccion inspection = values.get(0);
            assertNotNull(inspection.getPhotoId());
            assertEquals(2, inspection.allAlbums().size());
            assertNotEquals(inspection.allAlbums().get(0).getId(), inspection.allAlbums().get(1).getId());
            assertTrue(inspection.allAlbums().get(0).getFotos().get(0).contains("/Inspecciones/"));
            assertEquals(inspection.getPhotoId(), StoredInspections.read(context, name).get(0).getPhotoId());
            StoredInspections.write(context, name, values);
            assertEquals(inspection.getPhotoId(), StoredInspections.read(context, name).get(0).getPhotoId());
        }
    }
    @Test public void emptyQueueIsPersisted() throws Exception {
        released(StoredInspections.UPLOADS);
        StoredInspections.write(context, StoredInspections.UPLOADS, new ArrayList<>());
        assertTrue(StoredInspections.read(context, StoredInspections.UPLOADS).isEmpty());
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
