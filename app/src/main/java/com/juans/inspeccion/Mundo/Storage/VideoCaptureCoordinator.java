package com.juans.inspeccion.Mundo.Storage;

import com.juans.inspeccion.data.local.AtomicObjects;

import com.juans.inspeccion.platform.media.LocalMediaIndex;
import com.juans.inspeccion.platform.media.VideoStore;
import com.juans.inspeccion.platform.media.PhotoNames;

import com.juans.inspeccion.Mundo.Album;
import com.juans.inspeccion.Mundo.Inspeccion;

import androidx.camera.video.VideoRecordEvent;
import com.juans.inspeccion.Mundo.*;
import java.io.*;
import java.util.UUID;

/** Separate journal: the photo checkpoint never attempts to decode a video as JPEG. */
public final class VideoCaptureCoordinator {
    private final VideoStore store;
    private final File journal;
    private Session session;
    public VideoCaptureCoordinator(VideoStore store, File journal) throws IOException {
        this.store = store; this.journal = journal;
        try { session = (Session) AtomicObjects.read(journal); }
        catch (ClassNotFoundException | RuntimeException e) { throw new IOException("El registro de grabación se conserva para recuperación", e); }
    }
    public synchronized Session session() { return session; }
    public synchronized boolean isBusy() { return session != null && !session.finished; }
    public synchronized File begin(Inspeccion inspection, Album album) throws IOException {
        if (isBusy()) throw new IOException("Hay un video pendiente de recuperación");
        String id = UUID.randomUUID().toString(); File file = store.createCapture(id);
        Session next = new Session(id, file.getPath(), inspection, album);
        try { AtomicObjects.write(journal, next); } catch (IOException e) { file.delete(); throw e; }
        session = next; return file;
    }
    public synchronized void requestDiscard() throws IOException { if (isBusy()) { session.discard = true; AtomicObjects.write(journal, session); } }
    public synchronized void finalized(int error, boolean interrupted) throws IOException {
        if (!isBusy() || session.finalized) return;
        session.finalized = true; session.interrupted = interrupted;
        session.finalizationError = error; AtomicObjects.write(journal, session);
    }
    public synchronized String publish() throws IOException {
        if (session == null || session.discard) throw new IOException("La grabación fue descartada");
        if (session.reference != null) return session.reference;
        int error = session.finalizationError;
        if (session.finalized && error != VideoRecordEvent.Finalize.ERROR_NONE
                && error != VideoRecordEvent.Finalize.ERROR_DURATION_LIMIT_REACHED
                && error != VideoRecordEvent.Finalize.ERROR_FILE_SIZE_LIMIT_REACHED
                && error != VideoRecordEvent.Finalize.ERROR_SOURCE_INACTIVE)
            throw new IOException("No se pudo finalizar el video (" + error + "); la captura privada se conserva");
        File file = new File(session.path); session.duration = store.validate(file);
        session.reference = store.publish(file, session.inspectionId, PhotoNames.video(session.inspection, session.album), session.id);
        session.finalized = true; AtomicObjects.write(journal, session); return session.reference;
    }
    public synchronized void renamePublished(String reference, String name) throws IOException { store.rename(reference, name); }
    public synchronized void associate(LocalMediaIndex index, Inspeccion inspection, Album album) throws IOException {
        if (session == null || !session.inspectionId.equals(inspection.getPhotoId()) || !session.albumId.equals(album.getId()) || session.reference == null)
            throw new IOException("El video pertenece a otra inspección o álbum");
        index.addVideo(session.inspectionId, album, session.reference, session.duration);
        session.inspection = inspection; session.album = album; session.finished = true;
        try { AtomicObjects.write(journal, session); } catch (IOException e) { session.finished = false; throw e; }
        new File(session.path).delete();
    }
    public synchronized void updateCompletedSnapshot(Inspeccion inspection, Album editing) throws IOException {
        if (session != null && session.finished && session.inspectionId.equals(inspection.getPhotoId())) {
            session.inspection = inspection; Album album = inspection.findAlbum(session.albumId);
            if (album != null) session.album = album; else if (editing != null && editing.getId().equals(session.albumId)) session.album = editing;
            AtomicObjects.write(journal, session);
        }
    }
    public synchronized void acknowledgeSavedState() throws IOException { if (session != null && session.finished) { AtomicObjects.write(journal, null); session = null; } }
    public synchronized void discard() throws IOException {
        if (session == null) return;
        Session previous = session;
        // Retain the inspection snapshot and any already-published video. Discard only private bytes.
        previous.finished = true; previous.discard = true; AtomicObjects.write(journal, previous);
        new File(previous.path).delete();
    }
    public static final class Session implements Serializable {
        private static final long serialVersionUID = 1L;
        public final String id, path, inspectionId, albumId;
        public Inspeccion inspection; public Album album;
        public boolean finalized, finished, discard, interrupted;
        public int finalizationError;
        public String reference; public long duration;
        Session(String id, String path, Inspeccion inspection, Album album) {
            this.id = id; this.path = path; this.inspection = inspection; this.album = album;
            inspectionId = inspection.getPhotoId(); albumId = album.getId();
        }
    }
}
