package com.juans.inspeccion.Interfaz.Gallery;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.juans.inspeccion.Mundo.Storage.*;
import java.io.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/** Completion survives recreation; a separate journal retains metadata recovery after byte deletion. */
public class GalleryDeletionModel extends AndroidViewModel {
    private static final Executor WORKER = Executors.newSingleThreadExecutor();
    private final PhotoStore store;
    private final VideoStore videos;
    private final Executor worker;
    private final File journal;
    private final Handler main = new Handler(Looper.getMainLooper());
    private final MutableLiveData<Result> result = new MutableLiveData<>();
    private Result pending;
    private boolean journalDamaged;
    public static final class Result implements Serializable {
        private static final long serialVersionUID = 1L;
        public final String inspectionId, albumId, reference, error;
        public final boolean busy, bytesDeleted;
        public final GalleryMediaItem.Kind kind;
        public final long duration;
        Result(String i, String a, GalleryMediaItem item, boolean busy, boolean bytesDeleted, String error) {
            inspectionId=i; albumId=a; reference=item.reference; kind=item.kind; duration=item.durationMillis;
            this.busy=busy; this.bytesDeleted=bytesDeleted; this.error=error;
        }
        GalleryMediaItem item() { return new GalleryMediaItem(reference, kind==GalleryMediaItem.Kind.PHOTO?"image/jpeg":"video/mp4",kind,duration); }
    }
    public GalleryDeletionModel(Application app) { this(app,new MediaPhotoStore(app),WORKER); }
    GalleryDeletionModel(Application app, PhotoStore store, Executor worker) {
        super(app); this.store=store; this.videos=new MediaVideoStore(app); this.worker=worker;
        journal=app.getFileStreamPath("pending-media-deletion");
        try {
            Result saved=(Result)AtomicObjects.read(journal);
            if(saved!=null) {
                if(saved.busy) {
                    // The process may have died between delete() and recording its completion.
                    try(android.database.Cursor rows=app.getContentResolver().query(android.net.Uri.parse(saved.reference),new String[]{"_id"},null,null,null)) {
                        if(rows==null) throw new IOException("No se pudo comprobar el borrado interrumpido");
                        saved=new Result(saved.inspectionId,saved.albumId,saved.item(),false,rows.getCount()==0,
                            rows.getCount()==0 ? null : "Borrado interrumpido; puedes reintentarlo");
                    }
                }
                if(saved.bytesDeleted) pending=saved;
                result.setValue(saved);
            }
        } catch(Exception e) { journalDamaged=true; /* Preserve the journal rather than overwrite unknown recovery state. */ }
    }
    public LiveData<Result> results() { return result; }
    public boolean busy() { return pending!=null || (result.getValue()!=null && result.getValue().busy); }
    public boolean needsMetadataRetry() { return pending!=null && pending.bytesDeleted; }
    public boolean delete(GalleryHost host,String i,String a,String reference) {
        if(journalDamaged || result.getValue()!=null || pending!=null || !host.canDeleteGalleryPhoto(i,a)) return false;
        GalleryMediaItem selected=null;
        try { for(GalleryMediaItem item:host.galleryMediaItems(i,a)) if(item.reference.equals(reference)) selected=item; }
        catch(IOException e) { return false; }
        if(selected==null) return false;
        GalleryMediaItem target=selected;
        Result working=new Result(i,a,target,true,false,null);
        result.setValue(working);
        worker.execute(() -> {
            Result completed; boolean bytesDeleted=false;
            try {
                // Journal identity before mutation, and completion before unlinking metadata.
                AtomicObjects.write(journal,working);
                if(target.kind==GalleryMediaItem.Kind.VIDEO) videos.delete(reference); else store.delete(reference);
                bytesDeleted=true;
                completed=new Result(i,a,target,false,true,null);
                AtomicObjects.write(journal,completed);
            } catch(Exception e) { completed=new Result(i,a,target,false,bytesDeleted,e.getMessage()==null ? "No se pudo borrar el archivo" : e.getMessage()); }
            Result finalResult=completed;
            main.post(() -> result.setValue(finalResult));
        });
        return true;
    }
    public Result consume(GalleryHost host) {
        Result current=pending!=null?pending:result.getValue();
        if(current==null || current.busy) return null;
        if(current.bytesDeleted) {
            pending=current;
            try {
                host.galleryMediaDeleted(current.inspectionId,current.albumId,current.item());
                AtomicObjects.write(journal,null);
            } catch(Exception e) {
                // Keep completion without notifying recursively; an explicit retry never deletes bytes twice.
                return new Result(current.inspectionId,current.albumId,current.item(),false,true,"Los cambios requieren recuperación: "+e.getMessage());
            }
        }
        if(!current.bytesDeleted) { try { AtomicObjects.write(journal,null); } catch(IOException e) { journalDamaged=true; } }
        pending=null; result.setValue(null); return current;
    }
}
