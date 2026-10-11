package com.juans.inspeccion.Interfaz.Camera;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.juans.inspeccion.Mundo.Album;
import com.juans.inspeccion.Mundo.Storage.CaptureCoordinator;
import com.juans.inspeccion.Mundo.Storage.PhotoNames;
import com.juans.inspeccion.Mundo.Storage.*;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/** Activity-scoped capture completion. Workers hold neither an Activity nor an Album. */
public class CameraCaptureModel extends AndroidViewModel {
    public enum Phase { IDLE, CAPTURING, RECORDING, FINALIZING, SAVING, READY, DONE, ERROR }
    public static final class State {
        public final Phase phase;
        public final String error;
        State(Phase phase, String error) { this.phase = phase; this.error = error; }
    }
    private static final Executor WORKER = Executors.newSingleThreadExecutor();
    private final Executor worker;
    private final Handler main = new Handler(Looper.getMainLooper());
    private final MutableLiveData<State> states = new MutableLiveData<>(new State(Phase.IDLE, null));
    private CaptureCoordinator coordinator;
    private VideoCaptureCoordinator videos;
    private LocalMediaIndex index;
    private boolean video, interrupted, discarded;
    public boolean wasDiscarded() { return discarded; }
    public boolean isVideo() { return video; }
    public boolean wasInterrupted() { return interrupted; }
    public VideoCaptureCoordinator attachVideo(VideoCaptureCoordinator initial) {
        if (videos == null) videos = initial;
        if (index == null) index = new LocalMediaIndex(getApplication());
        return videos;
    }
    public VideoCaptureCoordinator videos() { return videos; }
    public LocalMediaIndex index() { return index; }
    public CameraCaptureModel(Application app) { this(app, WORKER); }
    CameraCaptureModel(Application app, Executor worker) { super(app); this.worker = worker; }
    public CaptureCoordinator attach(CaptureCoordinator initial) {
        if (coordinator == null) coordinator = initial;
        return coordinator;
    }
    public CaptureCoordinator coordinator() { return coordinator; }
    public LiveData<State> states() { return states; }
    public State state() { return states.getValue(); }
    public boolean busy() {
        return state().phase == Phase.RECORDING || state().phase == Phase.FINALIZING || state().phase == Phase.CAPTURING || state().phase == Phase.SAVING
                || state().phase == Phase.READY || state().phase == Phase.ERROR;
    }
    public void reset() { if (!busy()) { video = false; interrupted = false; discarded=false; emit(Phase.IDLE, null); } }
    private void emit(Phase phase, String error) { states.setValue(new State(phase, error)); }
    public File beginPhoto(CameraHost host, String inspectionId, String albumId, int damageIndex) throws IOException {
        if (coordinator == null || busy() || coordinator.isBusy() || (videos != null && videos.isBusy()) || !host.canCaptureMedia(inspectionId, albumId))
            throw new IOException("No se puede capturar en este momento");
        video = false;
        Album album = host.resolveGalleryAlbum(inspectionId, albumId);
        if (album == null) throw new IOException("El álbum ya no está disponible");
        String name = PhotoNames.capture(getApplication(), host.cameraInspection(), album, damageIndex);
        coordinator.begin(host.cameraInspection(), album, name, name);
        emit(Phase.CAPTURING, null);
        return new File(coordinator.session().path);
    }
    public void photoSaved() {
        if (state().phase != Phase.CAPTURING) return;
        try { coordinator.cameraResult(true); publish(); }
        catch (Exception e) { fail(e); }
    }
    public void photoFailed(Exception error) {
        if (state().phase != Phase.CAPTURING) return;
        try {
            if (coordinator.session() != null && new File(coordinator.session().path).length() > 0) { fail(error); return; }
            coordinator.cameraResult(false); emit(Phase.IDLE, error.getMessage());
        }
        catch (Exception e) { fail(e); }
    }
    public void recover() {
        if (busy() && state().phase != Phase.ERROR) return;
        if (videos != null && videos.isBusy()) { video = true; interrupted = true; emit(Phase.ERROR, "Hay una grabación interrumpida. Reintenta guardar o descártala si está incompleta"); }
        else if (coordinator != null && coordinator.isBusy()) { video = false; emit(Phase.ERROR, "Hay una captura interrumpida"); }
    }
    public void retry() {
        if (state().phase != Phase.ERROR) return;
        if (video) { publishVideo(); return; }
        if (coordinator == null || coordinator.session() == null) return;
        try { coordinator.cameraResult(true); publish(); }
        catch (Exception e) { fail(e); }
    }
    private void publish() {
        emit(Phase.SAVING, null);
        worker.execute(() -> {
            try { coordinator.publishCapture(); main.post(() -> emit(Phase.READY, null)); }
            catch (Exception e) { main.post(() -> fail(e)); }
        });
    }
    /** Consume only against the currently recreated inspection and editing album. */
    public void commit(CameraHost host) {
        if (state().phase != Phase.READY) return;
        if (video) { commitVideo(host); return; }
        CaptureCoordinator.Session session = coordinator.session();
        Album current = host.resolveGalleryAlbum(session.inspectionId, session.albumId);
        try {
            if (current == null) throw new IOException("No se encontró el álbum; la captura se conserva");
            coordinator.complete(host.cameraInspection(), current);
            if (index != null) index.items(session.inspectionId, current);
            host.cameraMediaAssociated();
            emit(Phase.DONE, null);
        } catch (Exception e) { fail(e); }
    }
    public void discard() {
        if (state().phase == Phase.SAVING || state().phase == Phase.READY) return;
        try { if (video && videos != null) videos.discard(); else if (coordinator != null) coordinator.discard(); emit(Phase.IDLE, null); }
        catch (Exception e) { fail(e); }
    }
    public File beginVideo(CameraHost host, String inspectionId, String albumId) throws IOException {
        if (videos == null || busy() || videos.isBusy() || (coordinator != null && coordinator.isBusy()) || !host.canCaptureMedia(inspectionId, albumId))
            throw new IOException("No se puede grabar en este momento");
        com.juans.inspeccion.Mundo.Album current = host.resolveGalleryAlbum(inspectionId, albumId);
        if (current == null) throw new IOException("El álbum ya no está disponible");
        index.items(inspectionId, current); // Seed photos before the new capture.
        File file = videos.begin(host.cameraInspection(), current);
        video = true; interrupted = false; emit(Phase.RECORDING, null); return file;
    }
    public void stopVideo(boolean discard, boolean interrupted) {
        if (state().phase != Phase.RECORDING) return;
        this.interrupted = interrupted;
        try { if (discard) videos.requestDiscard(); emit(Phase.FINALIZING, null); }
        catch (Exception e) { fail(e); }
    }
    public void videoFinalized(int error) {
        if (!video || (state().phase != Phase.RECORDING && state().phase != Phase.FINALIZING)) return;
        try {
            videos.finalized(error, interrupted);
            if (videos.session().discard) { discarded=true; videos.discard(); emit(Phase.DONE, null); }
            else publishVideo();
        } catch (Exception e) { fail(e); }
    }
    private void publishVideo() {
        emit(Phase.SAVING, null);
        worker.execute(() -> {
            try { videos.publish(); main.post(() -> emit(Phase.READY, null)); }
            catch (Exception e) { main.post(() -> fail(e)); }
        });
    }
    private void commitVideo(CameraHost host) {
        try {
            VideoCaptureCoordinator.Session session = videos.session();
            Album current = host.resolveGalleryAlbum(session.inspectionId, session.albumId);
            if (current == null) throw new IOException("No se encontró el álbum; el video se conserva para recuperación");
            videos.associate(index, host.cameraInspection(), current);
            host.cameraMediaAssociated();
            emit(Phase.DONE, null);
        } catch (Exception e) { fail(e); }
    }
    public void fail(Exception error) { emit(Phase.ERROR, error.getMessage() == null ? "No se pudo guardar la captura" : error.getMessage()); }
}
