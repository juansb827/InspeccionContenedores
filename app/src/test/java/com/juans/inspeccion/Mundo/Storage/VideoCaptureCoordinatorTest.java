package com.juans.inspeccion.Mundo.Storage;
import android.app.Application;
import androidx.camera.video.VideoRecordEvent;
import com.juans.inspeccion.Mundo.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import java.io.*;
import static org.junit.Assert.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=29,application=Application.class)
public class VideoCaptureCoordinatorTest {
    Application app; Fake store; VideoCaptureCoordinator capture; LocalMediaIndex index; Inspeccion inspection; Album album;
    @Before public void setup() throws Exception { app=RuntimeEnvironment.getApplication(); store=new Fake(); index=new LocalMediaIndex(app); inspection=new Inspeccion(); album=new Album(); capture=new VideoCaptureCoordinator(store,app.getFileStreamPath("video-test")); }
    @Test public void bothLimitsFinalizeValidClipsAndDuplicateCallbacksPublishOnce() throws Exception {
        assertEquals(300000,VideoStore.MAX_DURATION_MILLIS); assertEquals(200000000,VideoStore.MAX_BYTES);
        for(int error:new int[]{VideoRecordEvent.Finalize.ERROR_DURATION_LIMIT_REACHED,VideoRecordEvent.Finalize.ERROR_FILE_SIZE_LIMIT_REACHED}) {
            capture.begin(inspection,album); capture.finalized(error,false); capture.finalized(error,false);
            String ref=capture.publish(); assertEquals(ref,capture.publish()); capture.associate(index,inspection,album); capture.associate(index,inspection,album);
            assertTrue(album.getFotos().isEmpty()); assertTrue(capture.session().finished); capture.acknowledgeSavedState();
        }
        assertEquals(2,store.publishes); assertEquals(2,index.items(inspection.getPhotoId(),album).size());
    }
    @Test public void processDeathCanRecoverFinalizedBytesAndRejectIncompleteRecording() throws Exception {
        File file=capture.begin(inspection,album); VideoCaptureCoordinator restored=new VideoCaptureCoordinator(store,app.getFileStreamPath("video-test"));
        store.invalid=true; try { restored.publish(); fail(); } catch(IOException expected) {} assertTrue(file.exists());
        store.invalid=false; restored.publish(); restored.associate(index,inspection,album); assertFalse(file.exists());
    }
    @Test public void interruptedSourceSavesValidRecordingWithoutResuming() throws Exception {
        capture.begin(inspection,album); capture.finalized(VideoRecordEvent.Finalize.ERROR_SOURCE_INACTIVE,true); capture.publish(); capture.associate(index,inspection,album);
        assertTrue(capture.session().interrupted); assertTrue(capture.session().finished);
    }
    @Test public void discardRemovesOnlyPrivateRecording() throws Exception {
        File file=capture.begin(inspection,album); capture.requestDiscard(); capture.finalized(0,false); capture.discard();
        assertFalse(file.exists()); assertEquals(0,store.publishes); assertTrue(index.items(inspection.getPhotoId(),album).isEmpty());
    }
    @Test public void publicationAndIndexFailuresKeepRecoveryWithoutDuplicateEntries() throws Exception {
        File file=capture.begin(inspection,album); capture.finalized(0,false); store.failure=true;
        try { capture.publish(); fail(); } catch(IOException expected) {} assertTrue(file.exists());
        store.failure=false; capture.publish();
        LocalMediaIndex broken=new LocalMediaIndex(app) { protected void write(org.json.JSONObject root) throws IOException { throw new IOException("index failure"); } };
        try { capture.associate(broken,inspection,album); fail(); } catch(IOException expected) {} assertTrue(capture.isBusy()); assertTrue(file.exists());
        capture.publish(); capture.associate(index,inspection,album); assertEquals(1,store.publishes); assertEquals(1,index.items(inspection.getPhotoId(),album).size());
    }
    @Test public void wrongOwnerAndFailedFinalizationCannotMutateMetadata() throws Exception {
        capture.begin(inspection,album); capture.finalized(VideoRecordEvent.Finalize.ERROR_ENCODING_FAILED,false);
        try { capture.publish(); fail(); } catch(IOException expected) {} assertEquals(0,store.publishes);
        capture.discard(); capture.begin(inspection,album); capture.finalized(0,false); capture.publish();
        try { capture.associate(index,new Inspeccion(),album); fail(); } catch(IOException expected) {} assertTrue(index.items(inspection.getPhotoId(),album).isEmpty());
    }
    @Test public void recreatedCaptureNamesVideoFromInspectionAndEditingAlbumWithoutUuid() throws Exception {
        inspection.setTipoInspeccion("ENTRADA");inspection.setTipoDocumento("EP");
        inspection.getInformacion().put("NNUMDOC","123");inspection.getDatosContenedor().put("CCODCNTR","ABCD");
        capture.begin(inspection,album);capture.finalized(0,false);
        VideoCaptureCoordinator restored=new VideoCaptureCoordinator(store,app.getFileStreamPath("video-test"));
        restored.publish();
        assertEquals("ENTRADA_EP_123_ABCD_DANIO#0_0.mp4",store.name);
    }
    class Fake implements VideoStore {
        boolean invalid,failure; int publishes; String name;
        public File createCapture(String id) throws IOException { File file=app.getFileStreamPath(id); file.createNewFile(); return file; }
        public long validate(File f) throws IOException { if(invalid) throw new IOException("incomplete"); return 1000; }
        public String publish(File f,String i,String n,String id) throws IOException { if(failure)throw new IOException("full"); name=n; publishes++; return "content://videos/"+id; }
        public void delete(String ref) { throw new AssertionError("discard cannot delete published bytes"); }
    }
}
