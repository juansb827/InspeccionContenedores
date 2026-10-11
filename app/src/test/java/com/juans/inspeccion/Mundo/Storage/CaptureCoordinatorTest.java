package com.juans.inspeccion.Mundo.Storage;

import android.net.Uri;
import com.juans.inspeccion.Mundo.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import java.io.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, application = android.app.Application.class)
public class CaptureCoordinatorTest {
    private File journal;
    private CapturePhotos photos;
    @Before public void setup() {
        journal = RuntimeEnvironment.getApplication().getFileStreamPath("capture-test");
        photos = new CapturePhotos();
    }
    @Test public void recreatedCoordinatorRoutesToOriginalDamageAlbum() throws Exception {
        Inspeccion inspection = new Inspeccion(); Danio damage = new Danio(); inspection.getDaniosManager().getListaDanios().add(damage);
        CaptureCoordinator coordinator = new CaptureCoordinator(photos, journal);
        coordinator.begin(inspection, damage.getAlbum(), "damage.jpg", "EIR_DANIO#0_0.jpg");
        coordinator = new CaptureCoordinator(photos, journal);
        coordinator.cameraResult(true);
        Inspeccion restored = coordinator.session().inspection;
        Album album = restored.findAlbum(coordinator.session().albumId);
        coordinator.complete(restored, album);
        assertTrue(restored.getFotosGenerales().getFotos().isEmpty());
        assertEquals(1, album.getFotos().size());
        assertEquals("EIR_DANIO#0_0.jpg", album.remoteName(album.getFotos().get(0), photos));
        assertFalse(coordinator.isBusy());
    }
    @Test public void duplicateResultsAndRetriesDoNotAddAnotherPhoto() throws Exception {
        Inspeccion inspection = new Inspeccion(); Album album = inspection.getFotosGenerales();
        CaptureCoordinator coordinator = new CaptureCoordinator(photos, journal);
        coordinator.begin(inspection, album, "general.jpg", "general.jpg"); coordinator.cameraResult(true);
        coordinator.complete(inspection, album); coordinator.cameraResult(true); coordinator.complete(inspection, album);
        assertEquals(1, album.getFotos().size()); assertEquals(1, photos.publishes);
    }
    @Test public void cancellationOnlyRemovesItsOwnPrivateFile() throws Exception {
        Inspeccion inspection = new Inspeccion(); CaptureCoordinator coordinator = new CaptureCoordinator(photos, journal);
        coordinator.begin(inspection, inspection.getFotosGenerales(), "general.jpg", "general.jpg");
        File path = new File(coordinator.session().path); assertTrue(path.exists());
        coordinator.cameraResult(false); assertFalse(path.exists()); assertFalse(coordinator.isBusy());
        assertNull(new CaptureCoordinator(photos, journal).session()); assertEquals(0, photos.publishes);
    }
    @Test public void failedPublicationPreservesCaptureForRecovery() throws Exception {
        Inspeccion inspection = new Inspeccion(); CaptureCoordinator coordinator = new CaptureCoordinator(photos, journal);
        coordinator.begin(inspection, inspection.getFotosGenerales(), "general.jpg", "general.jpg"); coordinator.cameraResult(true);
        photos.fail = true;
        try { coordinator.complete(inspection, inspection.getFotosGenerales()); fail(); } catch (IOException expected) { }
        assertTrue(new File(coordinator.session().path).exists()); assertTrue(coordinator.isBusy());
        photos.fail = false;
        coordinator = new CaptureCoordinator(photos, journal);
        coordinator.complete(inspection, inspection.getFotosGenerales());
        assertEquals(1, inspection.getFotosGenerales().getFotos().size());
    }
    @Test public void wrongAlbumCannotReceivePhoto() throws Exception {
        Inspeccion inspection = new Inspeccion(); CaptureCoordinator coordinator = new CaptureCoordinator(photos, journal);
        coordinator.begin(inspection, inspection.getFotosGenerales(), "general.jpg", "general.jpg"); coordinator.cameraResult(true);
        try { coordinator.complete(inspection, new Album()); fail(); } catch (IOException expected) { assertEquals(0, photos.publishes); }
    }
    @Test public void captureCannotBeReplacedWhileOutstanding() throws Exception {
        Inspeccion inspection = new Inspeccion(); CaptureCoordinator coordinator = new CaptureCoordinator(photos, journal);
        coordinator.begin(inspection, inspection.getFotosGenerales(), "general.jpg", "general.jpg");
        try { coordinator.begin(new Inspeccion(), new Album(), "other.jpg", "other.jpg"); fail(); } catch (IOException expected) { }
    }
    private static class CapturePhotos extends LegacyPhotoImporterTest.FakePhotos {
        boolean fail; int publishes;
        public File createCapture(String id) throws IOException {
            File file = new File(RuntimeEnvironment.getApplication().getFilesDir(), id); assertTrue(file.createNewFile()); return file;
        }
        public Uri captureUri(File file) { return Uri.parse("content://capture/" + file.getName()); }
        public String publish(File file, String inspection, String name, String id) throws IOException {
            if (fail) throw new IOException("disk full"); publishes++; return "content://photos/" + id;
        }
    }
}
