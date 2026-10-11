package com.juans.inspeccion.Interfaz.Gallery;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.juans.inspeccion.Mundo.Album;
import com.juans.inspeccion.Mundo.Storage.MediaPhotoStore;
import com.juans.inspeccion.Mundo.Storage.PhotoStore;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/** Holds storage completion through rotation; never holds an Activity or an Album. */
public class GalleryDeletionModel extends AndroidViewModel {
    private static final Executor WORKER = Executors.newSingleThreadExecutor();
    private final PhotoStore store;
    private final Executor worker;
    private final Handler main = new Handler(Looper.getMainLooper());
    private final MutableLiveData<Result> result = new MutableLiveData<>();

    public static final class Result {
        public final String inspectionId, albumId, reference, error;
        public final boolean busy;
        Result(String inspectionId, String albumId, String reference, boolean busy, String error) {
            this.inspectionId = inspectionId; this.albumId = albumId; this.reference = reference;
            this.busy = busy; this.error = error;
        }
    }

    public GalleryDeletionModel(Application app) { this(app, new MediaPhotoStore(app), WORKER); }
    GalleryDeletionModel(Application app, PhotoStore store, Executor worker) {
        super(app); this.store = store; this.worker = worker;
    }
    public LiveData<Result> results() { return result; }
    public boolean busy() { Result current = result.getValue(); return current != null && current.busy; }

    public boolean delete(GalleryHost host, String inspectionId, String albumId, String reference) {
        // Do not overwrite a completion that the recreated view has not consumed.
        if (result.getValue() != null || !host.canDeleteGalleryPhoto(inspectionId, albumId)) return false;
        Album album = host.resolveGalleryAlbum(inspectionId, albumId);
        if (album == null || !album.getFotos().contains(reference)) return false;
        result.setValue(new Result(inspectionId, albumId, reference, true, null));
        worker.execute(() -> {
            String failure = null;
            try { store.delete(reference); }
            catch (Exception e) { failure = e.getMessage() == null ? "No se pudo borrar la foto" : e.getMessage(); }
            String error = failure;
            main.post(() -> result.setValue(new Result(inspectionId, albumId, reference, false, error)));
        });
        return true;
    }

    /** Apply once to the latest owner after success; failed storage never changes album metadata. */
    public Result consume(GalleryHost host) {
        Result current = result.getValue();
        if (current == null || current.busy) return null;
        if (current.error == null) host.galleryPhotoDeleted(current.inspectionId, current.albumId, current.reference);
        result.setValue(null);
        return current;
    }
}
