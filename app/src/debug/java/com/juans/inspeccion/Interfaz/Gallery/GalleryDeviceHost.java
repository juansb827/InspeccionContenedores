package com.juans.inspeccion.Interfaz.Gallery;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.widget.TextView;
import androidx.fragment.app.FragmentActivity;
import com.juans.inspeccion.Mundo.*;
import com.juans.inspeccion.Mundo.Storage.MediaPhotoStore;
import java.io.*;
import java.util.UUID;

/** Debug-only local fixture. No credentials, network or printer are used. */
public class GalleryDeviceHost extends FragmentActivity implements com.juans.inspeccion.Interfaz.Camera.CameraHost {
    public Inspeccion inspection;
    public Album editing;
    public boolean readOnly;
    public int persisted;
    public com.juans.inspeccion.Interfaz.Camera.CameraCaptureModel capture;
    public Inspeccion cameraInspection() { return inspection; }
    public boolean canCaptureMedia(String i, String a) { return GalleryAlbums.canDelete(inspection, editing, i, a, readOnly, null, capture.busy()); }
    public void cameraMediaAssociated() { persisted++; Pendientes.upsert(inspection, this); }

    @Override protected void onCreate(Bundle saved) {
        // Resolve restored fragments against the current album before super restores them.
        if (saved != null) {
            inspection = (Inspeccion) saved.getSerializable("inspection");
            editing = (Album) saved.getSerializable("editing");
            persisted = saved.getInt("persisted");
        }
        readOnly = getIntent().getBooleanExtra("readOnly", false);
        super.onCreate(saved);
        capture = new androidx.lifecycle.ViewModelProvider(this).get(com.juans.inspeccion.Interfaz.Camera.CameraCaptureModel.class);
        try { capture.attachVideo(new com.juans.inspeccion.Mundo.Storage.VideoCaptureCoordinator(new com.juans.inspeccion.Mundo.Storage.MediaVideoStore(this), getFileStreamPath("fixture-video-capture"))); capture.attach(new com.juans.inspeccion.Mundo.Storage.CaptureCoordinator(new MediaPhotoStore(this), getFileStreamPath("fixture-photo-capture"))); }
        catch (IOException e) { throw new IllegalStateException(e); }
        capture.states().observe(this, state -> {
            if (state.phase == com.juans.inspeccion.Interfaz.Camera.CameraCaptureModel.Phase.READY) capture.commit(this);
        });
        TextView underlying = new TextView(this); underlying.setText("Inspección de prueba local"); setContentView(underlying);
        if (saved != null) {
            return;
        } else {
            inspection = new Inspeccion(); inspection.setTipoDocumento(getIntent().getStringExtra("document"));
            inspection.setTipoInspeccion(getIntent().getStringExtra("direction"));
            editing = new Album(); inspection.getDaniosManager().getListaDanios().add(new Danio());
            Album target = selectedAlbum();
            if (getIntent().getBooleanExtra("cameraOnly", false)) {
                com.juans.inspeccion.Interfaz.Camera.CameraCaptureFragment.newInstance(inspection.getPhotoId(), target.getId(), -1)
                        .show(getSupportFragmentManager(), "camera");
                return;
            }
            new Thread(() -> {
            try {
                MediaPhotoStore store = new MediaPhotoStore(this);
                Bitmap image = Bitmap.createBitmap(768, 768, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(image); Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                for (int i = 0; i < getIntent().getIntExtra("count", 48); i++) {
                    canvas.drawColor(Color.HSVToColor(new float[]{(i * 37) % 360, 0.5f, 0.65f}));
                    paint.setColor(Color.WHITE); paint.setTextSize(80); canvas.drawText("Foto " + (i + 1), 70, 200, paint);
                    paint.setStyle(Paint.Style.STROKE); paint.setStrokeWidth(10);
                    canvas.drawRect(80, 300, 680, 650, paint); paint.setStyle(Paint.Style.FILL);
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream(); image.compress(Bitmap.CompressFormat.JPEG, 85, bytes);
                    String capture = UUID.randomUUID().toString();
                    target.agregarFoto(store.importPhoto(new ByteArrayInputStream(bytes.toByteArray()),
                            inspection.getPhotoId(), "fixture-" + i + ".jpg", capture));
                }
                image.recycle();
                if (getIntent().getBooleanExtra("unavailable", true)) target.getFotos().add("content://media/external/images/media/2147483647");
            } catch (IOException e) { throw new IllegalStateException(e); }
            runOnUiThread(() -> PhotoGalleryFragment.newInstance(inspection.getPhotoId(), target.getId()).show(getSupportFragmentManager(), "photos"));
            }, "gallery-fixtures").start();
        }
    }

    public Album selectedAlbum() {
        String kind = getIntent().getStringExtra("kind");
        return "editing".equals(kind) ? editing : "damage".equals(kind)
                ? inspection.getDaniosManager().getListaDanios().get(0).getAlbum() : inspection.getFotosGenerales();
    }
    @Override protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state); state.putSerializable("inspection", inspection);
        state.putSerializable("editing", editing); state.putInt("persisted", persisted);
    }
    public Album resolveGalleryAlbum(String inspectionId, String albumId) { return GalleryAlbums.resolve(inspection, editing, inspectionId, albumId); }
    public String galleryAlbumLabel(String albumId) { return inspection == null ? "" : GalleryAlbums.label(inspection, albumId); }
    public boolean canDeleteGalleryPhoto(String inspectionId, String albumId) {
        return GalleryAlbums.canDelete(inspection, editing, inspectionId, albumId, readOnly, null, false);
    }
    public java.util.List<GalleryMediaItem> galleryMediaItems(String i, String a) throws IOException {
        Album album = resolveGalleryAlbum(i, a);
        return album == null ? new java.util.ArrayList<>() : capture.index().items(i, album);
    }
    public void galleryMediaDeleted(String i, String a, GalleryMediaItem item) throws IOException {
        capture.index().remove(i, a, item.reference);
        if (item.kind == GalleryMediaItem.Kind.PHOTO) resolveGalleryAlbum(i, a).removePhotoReference(item.reference);
        cameraMediaAssociated();
    }
    public void galleryPhotoDeleted(String inspectionId, String albumId, String reference) {
        Album current = resolveGalleryAlbum(inspectionId, albumId);
        if (current != null && current.removePhotoReference(reference)) {
            persisted++; Pendientes.upsert(inspection, this);
        }
    }
}
