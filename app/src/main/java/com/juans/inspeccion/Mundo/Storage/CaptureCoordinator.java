package com.juans.inspeccion.Mundo.Storage;

import android.net.Uri;
import com.juans.inspeccion.Mundo.Album;
import com.juans.inspeccion.Mundo.Inspeccion;
import java.io.*;
import java.util.UUID;

/** Durable capture state, independent of camera activities and lifecycle callbacks. */
public final class CaptureCoordinator {
    private final PhotoStore photos;
    private final File journal;
    private volatile Session session;

    public CaptureCoordinator(PhotoStore photos, File journal) throws IOException {
        this.photos = photos;
        this.journal = journal;
        try { session = (Session) AtomicObjects.read(journal); }
        catch (ClassNotFoundException | RuntimeException e) { throw new IOException("No se pudo recuperar la captura; el registro se conserva", e); }
    }

    public boolean isBusy() { return session != null && !session.finished; }
    public Session session() { return session; }

    public synchronized void updateCompletedSnapshot(Inspeccion inspection, Album editing) throws IOException {
        if (session != null && session.finished && session.inspectionId.equals(inspection.getPhotoId())) {
            session.inspection = inspection;
            Album album = inspection.findAlbum(session.albumId);
            if (album != null) session.album = album;
            else if (editing != null && editing.getId().equals(session.albumId)) session.album = editing;
            AtomicObjects.write(journal, session);
        }
    }

    public synchronized void acknowledgeSavedState() throws IOException {
        if (session != null && session.finished) {
            AtomicObjects.write(journal, null);
            session = null;
        }
    }

    public synchronized Uri begin(Inspeccion inspection, Album album, String name, String remoteName) throws IOException {
        if (isBusy()) throw new IOException("Hay una foto pendiente de recuperación");
        String id = UUID.randomUUID().toString();
        File destination = photos.createCapture(id);
        Session next = new Session(id, destination.getPath(), inspection, album, name, remoteName);
        try { AtomicObjects.write(journal, next); }
        catch (IOException e) { destination.delete(); throw e; }
        session = next;
        return photos.captureUri(destination);
    }

    public interface Camera { void launch(Uri destination); }
    public synchronized void launch(Camera camera) throws IOException {
        if (!isBusy() || session.cameraReturned) return;
        try { camera.launch(photos.captureUri(new File(session.path))); }
        catch (android.content.ActivityNotFoundException e) { discard(); throw e; }
    }

    public synchronized void cameraResult(boolean success) throws IOException {
        if (!isBusy() || session.cameraReturned) return; // A duplicate callback is not a second photo.
        if (!success) { discard(); return; }
        session.cameraReturned = true;
        AtomicObjects.write(journal, session);
    }

    /** Called on a worker, using the live album resolved by stable IDs. */
    public synchronized String complete(Inspeccion inspection, Album album) throws IOException {
        if (session == null || !session.cameraReturned) throw new IOException("La cámara todavía no ha terminado");
        if (!session.inspectionId.equals(inspection.getPhotoId()) || !session.albumId.equals(album.getId()))
            throw new IOException("La foto pertenece a otra inspección o álbum");
        if (session.finished) return session.publishedUri;
        publishCapture();
        album.agregarFoto(session.publishedUri);
        album.setRemoteName(session.publishedUri, session.remoteName);
        session.inspection = inspection;
        session.album = album;
        session.finished = true;
        try { AtomicObjects.write(journal, session); }
        catch (IOException e) { session.finished = false; throw e; }
        // The complete journal contains the album association before private originals are removed.
        new File(session.path).delete();
        return session.publishedUri;
    }

    /** Process bytes on a worker without retaining or mutating a view's Album. */
    public synchronized String publishCapture() throws IOException {
        if (session == null || !session.cameraReturned) throw new IOException("La cámara todavía no ha terminado");
        if (session.publishedUri == null) {
            session.publishedUri = photos.publish(new File(session.path), session.inspectionId, session.displayName, session.captureId);
            AtomicObjects.write(journal, session);
        }
        return session.publishedUri;
    }

    public synchronized void discard() throws IOException {
        if (!isBusy()) return;
        // Published copies are retained; discarding an interrupted capture must not delete gallery media.
        boolean hasPhotos = !session.album.getFotos().isEmpty();
        for (Album album : session.inspection.allAlbums()) hasPhotos |= !album.getFotos().isEmpty();
        Session previous = session;
        if (hasPhotos) {
            // Preserve earlier completed photos if a later camera request is canceled.
            session.finished = true;
            try { AtomicObjects.write(journal, session); }
            catch (IOException e) { session.finished = false; throw e; }
        } else { AtomicObjects.write(journal, null); session = null; }
        new File(previous.path).delete();
    }

    public static final class Session implements Serializable {
        private static final long serialVersionUID = 1L;
        public final String captureId, path, inspectionId, albumId, displayName, remoteName;
        public Inspeccion inspection;
        public Album album;
        public boolean cameraReturned, finished;
        public String publishedUri;
        Session(String id, String path, Inspeccion inspection, Album album, String displayName, String remoteName) {
            captureId = id; this.path = path; this.inspection = inspection; this.album = album;
            inspectionId = inspection.getPhotoId(); albumId = album.getId();
            this.displayName = displayName; this.remoteName = remoteName;
        }
    }
}
