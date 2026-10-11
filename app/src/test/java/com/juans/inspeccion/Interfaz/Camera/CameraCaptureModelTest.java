package com.juans.inspeccion.Interfaz.Camera;

import android.app.Application;
import android.net.Uri;
import android.os.Looper;
import com.juans.inspeccion.Interfaz.Gallery.GalleryAlbums;
import com.juans.inspeccion.Mundo.*;
import com.juans.inspeccion.Mundo.Storage.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import java.io.*;
import java.util.ArrayList;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 29, application = Application.class)
@LooperMode(LooperMode.Mode.PAUSED)
public class CameraCaptureModelTest {
    private Application app;
    private CameraCaptureModel model;
    private CaptureCoordinator coordinator;
    private Photos photos;
    private Host host;
    private ArrayList<Runnable> work;
    @Before public void setup() throws Exception {
        app = RuntimeEnvironment.getApplication(); photos = new Photos(); host = new Host(); work = new ArrayList<>();
        coordinator = new CaptureCoordinator(photos, app.getFileStreamPath("camera-test"));
        model = new CameraCaptureModel(app, work::add); model.attach(coordinator);
    }
    private File begin(Album album) throws Exception { return model.beginPhoto(host, host.inspection.getPhotoId(), album.getId(), -1); }
    private void finishWorker() { work.remove(0).run(); Shadows.shadowOf(Looper.getMainLooper()).idle(); }
    @Test public void openingDoesNotAllocateCaptureAndCancelOnlyRemovesItsPrivateFile() throws Exception {
        assertNull(coordinator.session()); assertEquals(0, photos.creates);
        host.inspection.getFotosGenerales().getFotos().add("content://photos/existing");
        File file = begin(host.inspection.getFotosGenerales()); assertTrue(file.exists());
        model.photoFailed(new IOException("camera canceled"));
        assertFalse(file.exists()); assertEquals(0, photos.publishes); assertFalse(model.busy());
        assertEquals(1, host.inspection.getFotosGenerales().getFotos().size());
    }
    @Test public void readonlyActionSalidaDamageAndBusyMutationsAreDenied() throws Exception {
        for (int denied = 0; denied < 4; denied++) {
            host.readOnly = denied == 0; host.action = denied == 1 ? "MODIFICAR" : null; host.blocked = denied == 2;
            host.inspection.setTipoInspeccion(denied == 3 ? "SALIDA" : "ENTRADA");
            Album album = denied == 3 ? host.editing : host.inspection.getFotosGenerales();
            try { begin(album); fail(); } catch (IOException expected) { }
        }
        assertEquals(0, photos.creates);
    }
    @Test public void publicationUsesLatestRecreatedOwnerAndIgnoresDuplicateCallbacks() throws Exception {
        Album original = host.editing; begin(original); model.photoSaved(); model.photoSaved();
        assertEquals(1, work.size()); finishWorker(); assertTrue(original.getFotos().isEmpty());
        Host recreated = new Host(); recreated.inspection = copy(host.inspection); recreated.editing = copy(original);
        model.commit(recreated); model.commit(recreated); model.photoSaved();
        assertTrue(original.getFotos().isEmpty()); assertEquals(1, recreated.editing.getFotos().size());
        assertEquals(1, recreated.saved); assertEquals(1, photos.publishes); assertEquals(CameraCaptureModel.Phase.DONE, model.state().phase);
    }
    @Test public void failedPublicationKeepsBytesAndRetryReusesCapture() throws Exception {
        File file = begin(host.editing); photos.fail = true; model.photoSaved(); finishWorker();
        assertTrue(file.exists()); assertTrue(host.editing.getFotos().isEmpty()); assertEquals(CameraCaptureModel.Phase.ERROR, model.state().phase);
        photos.fail = false; model.retry(); finishWorker(); model.commit(host);
        assertFalse(file.exists()); assertEquals(1, host.editing.getFotos().size());
    }
    @Test public void wrongAlbumAndConcurrentCaptureCannotReplacePendingRequest() throws Exception {
        try { begin(new Album()); fail(); } catch (IOException expected) { }
        begin(host.editing); String id = coordinator.session().captureId;
        try { begin(host.inspection.getFotosGenerales()); fail(); } catch (IOException expected) { }
        assertEquals(id, coordinator.session().captureId);
    }
    @Test public void recoveryProcessesExistingBytesWithoutNewCapture() throws Exception {
        File file = begin(host.editing);
        CameraCaptureModel recovered = new CameraCaptureModel(app, work::add);
        recovered.attach(new CaptureCoordinator(photos, app.getFileStreamPath("camera-test"))); recovered.recover();
        assertEquals(CameraCaptureModel.Phase.ERROR, recovered.state().phase); assertTrue(file.exists());
        recovered.retry(); finishWorker(); recovered.commit(host);
        assertEquals(1, photos.creates); assertEquals(1, host.editing.getFotos().size());
    }
    @Test public void failedDraftPersistenceCanRetryWithoutRepublishing() throws Exception {
        begin(host.editing); model.photoSaved(); finishWorker(); host.failSave = true; model.commit(host);
        assertEquals(CameraCaptureModel.Phase.ERROR, model.state().phase);
        host.failSave = false; model.retry(); finishWorker(); model.commit(host);
        assertEquals(1, photos.publishes); assertEquals(1, host.editing.getFotos().size());
    }
    private void attachVideos() throws Exception {
        VideoStore videos = new VideoStore() {
            public File createCapture(String id) throws IOException { File file=app.getFileStreamPath(id); file.createNewFile(); return file; }
            public long validate(File file) { return 1500; }
            public String publish(File file,String i,String name,String id) { return "content://videos/"+id; }
            public void delete(String ref) { throw new AssertionError("discard must only remove private bytes"); }
        };
        model.attachVideo(new VideoCaptureCoordinator(videos,app.getFileStreamPath("model-video")));
    }
    @Test public void videoCompletionResolvesNewEditingOwnerAndIgnoresDuplicateCallbacks() throws Exception {
        attachVideos(); model.beginVideo(host,host.inspection.getPhotoId(),host.editing.getId());
        model.stopVideo(false,true); model.videoFinalized(0); model.videoFinalized(0); assertEquals(1,work.size()); finishWorker();
        Host recreated=new Host(); recreated.inspection=copy(host.inspection); recreated.editing=copy(host.editing);
        model.commit(recreated); model.commit(recreated);
        assertEquals(1,recreated.saved); assertEquals(0,host.saved); assertTrue(recreated.editing.getFotos().isEmpty());
        assertEquals(1,model.index().items(recreated.inspection.getPhotoId(),recreated.editing).size()); assertTrue(model.wasInterrupted());
    }
    @Test public void deniedVideoMutationCreatesNothingAndDiscardPublishesNothing() throws Exception {
        attachVideos(); host.readOnly=true;
        try { model.beginVideo(host,host.inspection.getPhotoId(),host.editing.getId()); fail(); } catch(IOException expected) {}
        assertNull(model.videos().session()); host.readOnly=false;
        File target=model.beginVideo(host,host.inspection.getPhotoId(),host.editing.getId()); model.stopVideo(true,false); model.videoFinalized(0);
        assertFalse(target.exists()); assertTrue(work.isEmpty()); assertEquals(0,host.saved); assertTrue(model.wasDiscarded());
    }
    @SuppressWarnings("unchecked") private static <T> T copy(T value) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(); new ObjectOutputStream(bytes).writeObject(value);
        return (T) new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray())).readObject();
    }
    static class Host implements CameraHost {
        Inspeccion inspection = new Inspeccion(); Album editing = new Album(); boolean readOnly, blocked, failSave; String action; int saved;
        public Inspeccion cameraInspection() { return inspection; }
        public Album resolveGalleryAlbum(String i, String a) { return GalleryAlbums.resolve(inspection, editing, i, a); }
        public String galleryAlbumLabel(String a) { return GalleryAlbums.label(inspection, a); }
        public boolean canCaptureMedia(String i, String a) { return GalleryAlbums.canDelete(inspection, editing, i, a, readOnly, action, blocked); }
        public boolean canDeleteGalleryPhoto(String i, String a) { return canCaptureMedia(i, a); }
        public void galleryPhotoDeleted(String i, String a, String ref) { }
        public void cameraMediaAssociated() throws IOException { if (failSave) throw new IOException("disk full"); saved++; }
    }
    static class Photos implements PhotoStore {
        public String importPhoto(InputStream in, String i, String name, String id) { throw new UnsupportedOperationException(); }
        public InputStream open(String ref) { throw new UnsupportedOperationException(); }
        public String displayName(String ref) { return "photo.jpg"; }
        public android.graphics.Bitmap thumbnail(String ref, int w, int h) { throw new UnsupportedOperationException(); }
        public void rename(String ref, String name) { }
        public void delete(String ref) { }
        int creates, publishes; boolean fail;
        public File createCapture(String id) throws IOException { creates++; File file = new File(RuntimeEnvironment.getApplication().getFilesDir(), id); file.createNewFile(); return file; }
        public Uri captureUri(File file) { return Uri.parse("content://capture/" + file.getName()); }
        public String publish(File file, String inspection, String name, String id) throws IOException {
            if (fail) throw new IOException("disk full"); publishes++; return "content://photos/" + id;
        }
    }
}
