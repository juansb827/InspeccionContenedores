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
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/** Activity-scoped capture completion. Workers hold neither an Activity nor an Album. */
public class CameraCaptureModel extends AndroidViewModel {
    public enum Phase { IDLE, CAPTURING, SAVING, READY, DONE, ERROR }
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
        return state().phase == Phase.CAPTURING || state().phase == Phase.SAVING
                || state().phase == Phase.READY || state().phase == Phase.ERROR;
    }
    public void reset() { if (!busy()) emit(Phase.IDLE, null); }
    private void emit(Phase phase, String error) { states.setValue(new State(phase, error)); }
    public File beginPhoto(CameraHost host, String inspectionId, String albumId, int damageIndex) throws IOException {
        if (coordinator == null || busy() || coordinator.isBusy() || !host.canCaptureMedia(inspectionId, albumId))
            throw new IOException("No se puede capturar en este momento");
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
        if (state().phase == Phase.CAPTURING || state().phase == Phase.SAVING || state().phase == Phase.READY) return;
        if (coordinator != null && coordinator.isBusy()) emit(Phase.ERROR, "Hay una captura interrumpida");
    }
    public void retry() {
        if (state().phase != Phase.ERROR || coordinator == null || coordinator.session() == null) return;
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
        CaptureCoordinator.Session session = coordinator.session();
        Album current = host.resolveGalleryAlbum(session.inspectionId, session.albumId);
        try {
            if (current == null) throw new IOException("No se encontró el álbum; la captura se conserva");
            coordinator.complete(host.cameraInspection(), current);
            host.cameraMediaAssociated();
            emit(Phase.DONE, null);
        } catch (Exception e) { fail(e); }
    }
    public void discard() {
        if (state().phase == Phase.SAVING || state().phase == Phase.READY) return;
        try { if (coordinator != null) coordinator.discard(); emit(Phase.IDLE, null); }
        catch (Exception e) { fail(e); }
    }
    public void fail(Exception error) { emit(Phase.ERROR, error.getMessage() == null ? "No se pudo guardar la captura" : error.getMessage()); }
}
