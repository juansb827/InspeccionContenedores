package com.juans.inspeccion.Interfaz.Gallery;

import android.os.Looper;
import com.juans.inspeccion.Mundo.Album;
import com.juans.inspeccion.Mundo.Storage.PhotoStore;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;
import java.io.*;
import java.lang.reflect.Proxy;
import java.util.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 29, application = android.app.Application.class)
@LooperMode(LooperMode.Mode.PAUSED)
public class GalleryDeletionModelTest {
    private final List<Runnable> work = new ArrayList<>();
    private final List<String> deleted = new ArrayList<>();
    private Host host;
    private GalleryDeletionModel model;
    private boolean fail;
    @Before public void setup() throws Exception {
        host = new Host(); host.album.agregarFoto("content://media/1"); host.album.agregarFoto("content://media/2");
        host.album.setRemoteName("content://media/2", "remote.jpg");
        PhotoStore store = (PhotoStore) Proxy.newProxyInstance(PhotoStore.class.getClassLoader(), new Class[]{PhotoStore.class},
                (proxy, method, args) -> {
                    if (!method.getName().equals("delete")) throw new AssertionError(method.getName());
                    if (fail) throw new IOException("storage denied");
                    deleted.add((String) args[0]); return null;
                });
        model = new GalleryDeletionModel(RuntimeEnvironment.getApplication(), store, work::add);
    }
    private void finish() { work.remove(0).run(); Shadows.shadowOf(Looper.getMainLooper()).idle(); }

    @Test public void deletionUsesReferenceEvenIfPhotoPositionsChangedWhileWorking() {
        assertTrue(model.delete(host, "inspection", host.album.getId(), "content://media/2"));
        Collections.reverse(host.album.getFotos());
        assertFalse(model.delete(host, "inspection", host.album.getId(), "content://media/1"));
        finish(); model.consume(host);
        assertEquals(Collections.singletonList("content://media/2"), deleted);
        assertEquals(Collections.singletonList("content://media/1"), host.album.getFotos());
        assertNull(host.album.recordedRemoteName("content://media/2")); assertEquals(1, host.persisted);
        assertNull(model.consume(host)); assertEquals(1, host.persisted);
    }

    @Test public void failedStorageLeavesReferenceAndRemoteMappingIntact() {
        fail = true; model.delete(host, "inspection", host.album.getId(), "content://media/2");
        finish(); assertEquals("storage denied", model.consume(host).error);
        assertEquals(2, host.album.getFotos().size()); assertEquals("remote.jpg", host.album.recordedRemoteName("content://media/2"));
        assertEquals(0, host.persisted); assertFalse(model.busy());
    }

    @Test public void handlerRejectsRestrictedMissingOrWrongAlbumRequests() {
        host.allowed = false; assertFalse(model.delete(host, "inspection", host.album.getId(), "content://media/1"));
        host.allowed = true; assertFalse(model.delete(host, "wrong-inspection", host.album.getId(), "content://media/1"));
        assertFalse(model.delete(host, "inspection", "wrong-album", "content://media/1"));
        assertFalse(model.delete(host, "inspection", host.album.getId(), "content://media/missing"));
        assertTrue(work.isEmpty()); assertTrue(deleted.isEmpty());
    }

    @Test public void completionResolvesRecreatedOwnersAlbumRatherThanOldInstance() throws Exception {
        model.delete(host, "inspection", host.album.getId(), "content://media/2");
        Host recreated = new Host();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) { out.writeObject(host.album); }
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { recreated.album = (Album) in.readObject(); }
        finish(); model.consume(recreated);
        assertEquals(2, host.album.getFotos().size()); assertEquals(1, recreated.album.getFotos().size());
        assertEquals(0, host.persisted); assertEquals(1, recreated.persisted);
    }

    @Test public void pendingCompletionCannotBeReplacedByAnotherRequest() {
        model.delete(host, "inspection", host.album.getId(), "content://media/1"); finish();
        assertFalse(model.delete(host, "inspection", host.album.getId(), "content://media/2"));
        model.consume(host); assertTrue(model.delete(host, "inspection", host.album.getId(), "content://media/2"));
        finish(); model.consume(host); assertTrue(host.album.getFotos().isEmpty());
    }

    @Test public void metadataFailureIsRetriedAfterRecreationWithoutDeletingBytesAgain() throws Exception {
        host.failSave=true; model.delete(host,"inspection",host.album.getId(),"content://media/1"); finish();
        assertNotNull(model.consume(host).error); assertTrue(model.needsMetadataRetry()); assertEquals(1,deleted.size());
        GalleryDeletionModel recreated=new GalleryDeletionModel(RuntimeEnvironment.getApplication(),null,work::add);
        host.failSave=false; assertNull(recreated.consume(host).error); assertEquals(1,deleted.size());
        assertFalse(recreated.busy()); assertEquals(1,host.album.getFotos().size());
    }
    private static final class Host implements GalleryHost {
        Album album = new Album(); boolean allowed = true, failSave; int persisted;
        public Album resolveGalleryAlbum(String inspection, String id) { return "inspection".equals(inspection) && album.getId().equals(id) ? album : null; }
        public String galleryAlbumLabel(String id) { return "Información"; }
        public boolean canDeleteGalleryPhoto(String inspection, String id) { return allowed && resolveGalleryAlbum(inspection, id) != null; }
        public void galleryMediaDeleted(String i,String a,GalleryMediaItem item) throws IOException {
            if(failSave)throw new IOException("metadata full"); galleryPhotoDeleted(i,a,item.reference);
        }
        public void galleryPhotoDeleted(String inspection, String id, String reference) {
            Album current = resolveGalleryAlbum(inspection, id);
            if (current != null && current.removePhotoReference(reference)) persisted++;
        }
    }
}
