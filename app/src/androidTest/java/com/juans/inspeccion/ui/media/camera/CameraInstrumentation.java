package com.juans.inspeccion.ui.media.camera;

import android.app.*;
import android.content.Intent;
import android.os.*;
import com.juans.inspeccion.ui.media.gallery.GalleryDeviceHost;
import com.juans.inspeccion.R;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;

/** Actual CameraX capture on emulator hardware, without production credentials. */
public class CameraInstrumentation extends Instrumentation {
    private volatile GalleryDeviceHost current;
    protected int checks;
    private boolean videoSuite;
    @Override public void onCreate(Bundle args) { videoSuite=args!=null && "video".equals(args.getString("suite")); super.onCreate(args); start(); }
    @Override public void onStart() {
        Bundle report = new Bundle();
        try {
            ((Application) getTargetContext().getApplicationContext()).registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
                public void onActivityResumed(Activity a) { if (a instanceof GalleryDeviceHost) current = (GalleryDeviceHost) a; }
                public void onActivityCreated(Activity a, Bundle b) {} public void onActivityStarted(Activity a) {}
                public void onActivityPaused(Activity a) {} public void onActivityStopped(Activity a) {}
                public void onActivitySaveInstanceState(Activity a, Bundle b) {} public void onActivityDestroyed(Activity a) {}
            });
            if(videoSuite) {
                runVideoChecks(); report.putString("stream","PASS: "+checks+" video checks on API "+Build.VERSION.SDK_INT+"\n"); finish(Activity.RESULT_OK,report); return;
            }
            for (String document : new String[]{"EP", "ET"}) for (String direction : new String[]{"ENTRADA", "SALIDA"}) {
                launch(document, direction, "general");
                waitFor(() -> ui(() -> camera() != null && camera().requireView().findViewById(R.id.cameraShutter).isEnabled()));
                check(ui(() -> current.capture.coordinator().session() == null), "no destination before shutter");
                runOnMainSync(() -> camera().requireView().findViewById(R.id.cameraShutter).performClick());
                waitFor(() -> ui(() -> current.persisted == 1 && camera() == null));
                check(ui(() -> current.selectedAlbum().getFotos().size() == 1), document + "/" + direction + " photo associated");
                check(ui(() -> current.capture.coordinator().session().finished), "completed recovery snapshot");
            }
            launch("EP", "ENTRADA", "editing");
            waitFor(() -> ui(() -> camera() != null && camera().requireView().findViewById(R.id.cameraShutter).isEnabled()));
            GalleryDeviceHost old = current; runOnMainSync(() -> old.recreate());
            waitFor(() -> current != old && ui(() -> camera() != null && camera().requireView().findViewById(R.id.cameraShutter).isEnabled()));
            runOnMainSync(() -> camera().requireView().findViewById(R.id.cameraShutter).performClick());
            waitFor(() -> ui(() -> current.persisted == 1 && camera() == null));
            check(ui(() -> current.editing.getFotos().size() == 1 && current.inspection.getFotosGenerales().getFotos().isEmpty()), "editing album survives recreation");
            launch("ET", "ENTRADA", "general");
            waitFor(() -> ui(() -> camera() != null));
            runOnMainSync(() -> camera().requireView().findViewById(R.id.cameraBack).performClick());
            waitFor(() -> ui(() -> camera() == null));
            check(ui(() -> current.selectedAlbum().getFotos().isEmpty() && current.capture.coordinator().session() == null), "cancel without capture");
            report.putString("stream", "PASS: " + checks + " embedded camera checks on API " + Build.VERSION.SDK_INT + "\n"); finish(Activity.RESULT_OK, report);
        } catch (Throwable e) {
            report.putString("stream", android.util.Log.getStackTraceString(e)); finish(Activity.RESULT_CANCELED, report);
        }
    }
    private void runVideoChecks() throws Exception {
        for(String document:new String[]{"EP","ET"}) for(String direction:new String[]{"ENTRADA","SALIDA"}) {
            launch(document,direction,"general");
            waitFor(() -> ui(() -> camera()!=null && camera().requireView().findViewById(R.id.cameraShutter).isEnabled()));
            runOnMainSync(() -> camera().requireView().findViewById(R.id.cameraShutter).performClick());
            waitFor(() -> ui(() -> current.persisted==1 && camera()==null));
            openCamera(); switchVideo(); startVideo(); waitForVideoFrames();
            runOnMainSync(() -> camera().requireView().findViewById(R.id.cameraShutter).performClick());
            waitFor(() -> ui(() -> current.persisted==2 && camera()==null));
            check(ui(() -> current.selectedAlbum().getFotos().size()==1),"videos excluded from photo album/upload queue");
            java.util.List<com.juans.inspeccion.Interfaz.Gallery.GalleryMediaItem> items=ui(() -> current.galleryMediaItems(current.inspection.getPhotoId(),current.selectedAlbum().getId()));
            check(items.size()==2 && items.get(0).kind==com.juans.inspeccion.Interfaz.Gallery.GalleryMediaItem.Kind.PHOTO && items.get(1).kind==com.juans.inspeccion.Interfaz.Gallery.GalleryMediaItem.Kind.VIDEO,"mixed capture ordering");
            check(items.get(1).durationMillis>0,"validated duration/audio/video");
            String reference=items.get(1).reference;
            try(android.media.MediaMetadataRetriever reader=new android.media.MediaMetadataRetriever()) {
                reader.setDataSource(getTargetContext(),android.net.Uri.parse(reference));
                check("yes".equals(reader.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO)),"published MP4 includes audio track");
            }
            runOnMainSync(() -> com.juans.inspeccion.ui.media.gallery.PhotoGalleryFragment.newInstance(current.inspection.getPhotoId(),current.selectedAlbum().getId()).show(current.getSupportFragmentManager(),"photos"));
            waitFor(() -> ui(() -> gallery()!=null && gallery().getView()!=null));
            runOnMainSync(() -> ((androidx.recyclerview.widget.RecyclerView)gallery().requireView().findViewById(R.id.galleryGrid)).findViewHolderForAdapterPosition(1).itemView.performClick());
            waitFor(() -> ui(() -> player()!=null && player().getPlayer()!=null && player().getPlayer().getPlaybackState()==androidx.media3.common.Player.STATE_READY));
            check(ui(() -> !player().getPlayer().getPlayWhenReady()),"video starts paused");
            runOnMainSync(() -> player().getPlayer().play()); SystemClock.sleep(900);
            check(ui(() -> player().getPlayer().getCurrentPosition()>0),"video decodes and plays");
            GalleryDeviceHost old=current; runOnMainSync(() -> old.recreate());
            waitFor(() -> current!=old && ui(() -> player()!=null && player().getPlayer()!=null && player().getPlayer().getPlaybackState()==androidx.media3.common.Player.STATE_READY));
            check(ui(() -> !player().getPlayer().getPlayWhenReady() && player().getPlayer().getCurrentPosition()>0),"recreated playback restores position and pauses");
            if(document.equals("ET") && direction.equals("SALIDA")) {
                com.juans.inspeccion.data.local.Pendientes.invalidate();
                int draft=com.juans.inspeccion.data.local.Pendientes.findIndex(ui(() -> current.inspection.getPhotoId()),getTargetContext());
                com.juans.inspeccion.Mundo.Inspeccion reopened=com.juans.inspeccion.data.local.Pendientes.darPendiente(draft,getTargetContext());
                check(reopened!=null && reopened.getFotosGenerales().getFotos().size()==1,"mixed-album draft reopens with unchanged photo format");
                continue; // Keep this published clip for update/rollback verification.
            }
            runOnMainSync(() -> gallery().requireView().findViewById(R.id.galleryDelete).performClick());
            runOnMainSync(() -> confirmation().getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).performClick());
            waitFor(() -> ui(() -> current.persisted==3));
            check(ui(() -> current.galleryMediaItems(current.inspection.getPhotoId(),current.selectedAlbum().getId()).size()==1 && current.selectedAlbum().getFotos().size()==1),"video deletion leaves photo intact");
        }
        launch("EP","ENTRADA","editing"); switchVideo(); startVideo(); waitForVideoFrames();
        GalleryDeviceHost old=current; runOnMainSync(() -> old.recreate());
        waitFor(() -> current!=old && ui(() -> current.persisted==1 && camera()==null));
        check(ui(() -> current.editing.getFotos().isEmpty() && current.galleryMediaItems(current.inspection.getPhotoId(),current.editing.getId()).size()==1),"interrupted recording saves into recreated unsaved damage album");
        openCamera(); switchVideo(); startVideo(); waitForVideoFrames();
        runOnMainSync(() -> camera().requireView().findViewById(R.id.cameraBack).performClick());
        runOnMainSync(() -> recordingDialog().getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE).performClick());
        waitFor(() -> ui(() -> camera()==null));
        check(ui(() -> current.persisted==1 && current.galleryMediaItems(current.inspection.getPhotoId(),current.editing.getId()).size()==1),"discard affects only new private clip");
        runOnMainSync(() -> com.juans.inspeccion.ui.media.gallery.PhotoGalleryFragment.newInstance(current.inspection.getPhotoId(),current.editing.getId()).show(current.getSupportFragmentManager(),"photos"));
        waitFor(() -> ui(() -> gallery()!=null && gallery().getView()!=null));
        runOnMainSync(() -> ((androidx.recyclerview.widget.RecyclerView)gallery().requireView().findViewById(R.id.galleryGrid)).findViewHolderForAdapterPosition(0).itemView.performClick());
        runOnMainSync(() -> gallery().requireView().findViewById(R.id.galleryDelete).performClick());
        runOnMainSync(() -> confirmation().getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).performClick());
        waitFor(() -> ui(() -> current.persisted==2));
        check(ui(() -> gallery().requireView().findViewById(R.id.galleryEmpty).getVisibility()==android.view.View.VISIBLE),"final video deletion returns empty gallery");
        sendKeyDownUpSync(android.view.KeyEvent.KEYCODE_BACK);
        runOnMainSync(() -> { try {
            for(int i=0;i<60;i++) { current.editing.getFotos().add("/missing/photo-"+i); current.capture.index().items(current.inspection.getPhotoId(),current.editing);
                current.capture.index().addVideo(current.inspection.getPhotoId(),current.editing,"content://media/external_primary/video/media/"+(100000+i),1000); }
            com.juans.inspeccion.ui.media.gallery.PhotoGalleryFragment.newInstance(current.inspection.getPhotoId(),current.editing.getId()).show(current.getSupportFragmentManager(),"photos");
        } catch(java.io.IOException e){throw new AssertionError(e);} });
        waitFor(() -> ui(() -> gallery()!=null && gallery().getView()!=null));
        check(ui(() -> ((androidx.recyclerview.widget.RecyclerView)gallery().requireView().findViewById(R.id.galleryGrid)).getAdapter().getItemCount()==120),"large mixed album preserves unavailable items");
        runOnMainSync(() -> ((androidx.recyclerview.widget.RecyclerView)gallery().requireView().findViewById(R.id.galleryGrid)).findViewHolderForAdapterPosition(1).itemView.performClick());
        waitFor(() -> ui(() -> containsText(gallery().requireView(),"Video no disponible")));
        check(ui(() -> ((android.widget.TextView)gallery().requireView().findViewById(R.id.galleryTitle)).getText().toString().equals("2 de 120")),"unavailable video position preserved");
    }
    private boolean containsText(android.view.View v,String text) {
        if(v instanceof android.widget.TextView && ((android.widget.TextView)v).getText().toString().equals(text)) return true;
        if(v instanceof android.view.ViewGroup) for(int i=0;i<((android.view.ViewGroup)v).getChildCount();i++) if(containsText(((android.view.ViewGroup)v).getChildAt(i),text))return true;
        return false;
    }
    private void waitForVideoFrames() {
        waitFor(() -> ui(() -> { java.lang.reflect.Field field=CameraCaptureFragment.class.getDeclaredField("elapsed"); field.setAccessible(true); return field.getLong(camera())>=2; }));
    }
    private void openCamera() {
        runOnMainSync(() -> { current.capture.reset(); CameraCaptureFragment.newInstance(current.inspection.getPhotoId(),current.selectedAlbum().getId(),-1).show(current.getSupportFragmentManager(),"camera"); });
        waitFor(() -> ui(() -> camera()!=null && camera().requireView().findViewById(R.id.cameraShutter).isEnabled()));
    }
    private void switchVideo() {
        waitFor(() -> ui(() -> camera()!=null && camera().getView()!=null));
        runOnMainSync(() -> camera().requireView().findViewById(R.id.cameraVideo).performClick());
        waitFor(() -> ui(() -> camera().requireView().findViewById(R.id.cameraShutter).isEnabled()));
    }
    private void startVideo() { runOnMainSync(() -> camera().requireView().findViewById(R.id.cameraShutter).performClick()); waitFor(() -> ui(() -> current.capture.state().phase==CameraCaptureModel.Phase.RECORDING)); }
    private com.juans.inspeccion.ui.media.gallery.PhotoGalleryFragment gallery() { return (com.juans.inspeccion.ui.media.gallery.PhotoGalleryFragment) current.getSupportFragmentManager().findFragmentByTag("photos"); }
    private androidx.media3.ui.PlayerView player() { return gallery()==null || gallery().getView()==null ? null : findPlayer(gallery().requireView()); }
    private androidx.media3.ui.PlayerView findPlayer(android.view.View v) {
        if(v instanceof androidx.media3.ui.PlayerView && ((androidx.media3.ui.PlayerView)v).getPlayer()!=null) return (androidx.media3.ui.PlayerView)v;
        if(v instanceof android.view.ViewGroup) for(int i=0;i<((android.view.ViewGroup)v).getChildCount();i++) { androidx.media3.ui.PlayerView found=findPlayer(((android.view.ViewGroup)v).getChildAt(i)); if(found!=null)return found; }
        return null;
    }
    private androidx.appcompat.app.AlertDialog confirmation() { return dialogField(gallery(),"confirmation"); }
    private androidx.appcompat.app.AlertDialog recordingDialog() { return dialogField(camera(),"recordingBack"); }
    private androidx.appcompat.app.AlertDialog dialogField(Object target,String field) { try { java.lang.reflect.Field f=target.getClass().getDeclaredField(field); f.setAccessible(true); return (androidx.appcompat.app.AlertDialog)f.get(target); } catch(Exception e){throw new AssertionError(e);} }
    private void launch(String document, String direction, String kind) {
        // A new fixture and journal for each test; completed public photos are retained.
        getTargetContext().deleteFile("fixture-photo-capture"); getTargetContext().deleteFile("fixture-video-capture");
        Intent intent = new Intent(getTargetContext(), GalleryDeviceHost.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .putExtra("document", document).putExtra("direction", direction).putExtra("kind", kind).putExtra("cameraOnly", true);
        current = (GalleryDeviceHost) startActivitySync(intent); waitForIdleSync();
    }
    private CameraCaptureFragment camera() { return (CameraCaptureFragment) current.getSupportFragmentManager().findFragmentByTag("camera"); }
    private void waitFor(BooleanSupplier condition) {
        for (int i = 0; i < 300; i++) { if (condition.getAsBoolean()) return; SystemClock.sleep(100); }
        throw new AssertionError("Camera state timeout: " + ui(() -> current.capture.state().phase + " " + current.capture.state().error));
    }
    private void check(boolean okay, String label) { if (!okay) throw new AssertionError(label); checks++; }
    private <T> T ui(java.util.concurrent.Callable<T> action) {
        AtomicReference<T> value = new AtomicReference<>(); AtomicReference<Throwable> failure = new AtomicReference<>();
        runOnMainSync(() -> { try { value.set(action.call()); } catch (Throwable e) { failure.set(e); } });
        if (failure.get() != null) throw new AssertionError(failure.get()); return value.get();
    }
}
