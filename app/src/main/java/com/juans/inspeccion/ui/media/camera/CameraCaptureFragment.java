package com.juans.inspeccion.ui.media.camera;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import androidx.activity.ComponentDialog;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.*;
import androidx.camera.core.*;
import androidx.camera.view.*;
import androidx.camera.video.*;
import androidx.camera.view.video.AudioConfig;
import androidx.appcompat.app.AlertDialog;
import com.juans.inspeccion.platform.media.VideoStore;
import java.util.List;
import java.util.Locale;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import com.juans.inspeccion.R;
import java.io.File;

/** Camera UI remains in the inspection activity; only IDs cross the screen boundary. */
public class CameraCaptureFragment extends DialogFragment {
    private CameraHost host;
    private CameraCaptureModel model;
    private String inspectionId, albumId;
    private int damageIndex;
    private LifecycleCameraController camera;
    private PreviewView preview;
    private TextView message;
    private Button shutter, permission, discard;
    private boolean requested, ready, permanentDenial, binding, cameraError;
    private boolean videoMode, microphonePermanent, videoSupported;
    private Recording recording;
    private RadioGroup mode;
    private TextView timer;
    private long elapsed;
    private AlertDialog recordingBack;
    private final ActivityResultLauncher<String> microphonePermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), granted -> {
                microphonePermanent = !granted && !shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO);
                render();
            });
    private final ActivityResultLauncher<String> cameraPermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), granted -> {
                permanentDenial = !granted && !shouldShowRequestPermissionRationale(Manifest.permission.CAMERA);
                if (granted && !model.busy()) bindCamera(); else render();
            });
    public static CameraCaptureFragment newInstance(String inspectionId, String albumId, int damageIndex) {
        CameraCaptureFragment fragment = new CameraCaptureFragment();
        Bundle args = new Bundle(); args.putString("inspection", inspectionId); args.putString("album", albumId); args.putInt("damage", damageIndex);
        fragment.setArguments(args); return fragment;
    }
    @Override public void onCreate(@Nullable Bundle state) {
        super.onCreate(state); setStyle(STYLE_NORMAL, R.style.CameraCaptureTheme); setCancelable(false);
        host = (CameraHost) requireActivity(); model = new ViewModelProvider(requireActivity()).get(CameraCaptureModel.class);
        inspectionId = requireArguments().getString("inspection"); albumId = requireArguments().getString("album"); damageIndex = requireArguments().getInt("damage");
        videoMode = state != null && state.getBoolean("videoMode"); microphonePermanent = state != null && state.getBoolean("microphonePermanent");
        if (model.isVideo() && model.busy()) videoMode = true;
        requested = state != null && state.getBoolean("requested"); permanentDenial = state != null && state.getBoolean("permanentDenial");
    }
    @NonNull @Override public Dialog onCreateDialog(@Nullable Bundle state) {
        Dialog dialog = super.onCreateDialog(state); dialog.setCanceledOnTouchOutside(false);
        if (dialog instanceof ComponentDialog) ((ComponentDialog) dialog).getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback(true) { public void handleOnBackPressed() { back(); } });
        else dialog.setOnKeyListener((d, key, event) -> {
            if (key != KeyEvent.KEYCODE_BACK) return false;
            if (event.getAction() == KeyEvent.ACTION_UP) back(); return true;
        });
        return dialog;
    }
    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        return inflater.inflate(R.layout.camera_capture, container, false);
    }
    @Override public void onViewCreated(@NonNull View root, @Nullable Bundle state) {
        preview = root.findViewById(R.id.cameraPreview); preview.setScaleType(PreviewView.ScaleType.FIT_CENTER);
        message = root.findViewById(R.id.cameraMessage); shutter = root.findViewById(R.id.cameraShutter);
        permission = root.findViewById(R.id.cameraPermission); discard = root.findViewById(R.id.cameraDiscard);
        mode = root.findViewById(R.id.cameraMode); timer = root.findViewById(R.id.cameraTimer);
        mode.check(videoMode ? R.id.cameraVideo : R.id.cameraPhoto);
        mode.setOnCheckedChangeListener((group, id) -> {
            if (model.busy()) return;
            videoMode = id == R.id.cameraVideo;
            if (videoMode && !hasMicrophonePermission()) microphonePermission.launch(Manifest.permission.RECORD_AUDIO);
            configureMode(); render();
        });
        ((TextView) root.findViewById(R.id.cameraContext)).setText(host.galleryAlbumLabel(albumId));
        root.findViewById(R.id.cameraBack).setOnClickListener(v -> back());
        shutter.setOnClickListener(v -> capture()); discard.setOnClickListener(v -> { model.discard(); render(); });
        permission.setOnClickListener(v -> {
            if (permanentDenial) startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + requireContext().getPackageName())));
            else { requested = true; cameraPermission.launch(Manifest.permission.CAMERA); }
        });
        model.states().observe(getViewLifecycleOwner(), stateNow -> {
            if (stateNow.phase == CameraCaptureModel.Phase.DONE) dismissAllowingStateLoss();
            else { if (!model.busy() && hasCameraPermission() && !ready && !binding) bindCamera(); render(); }
        });
        render();
    }
    @Override public void onStart() {
        super.onStart(); requireDialog().getWindow().setLayout(-1, -1);
        if (hasCameraPermission() && !model.busy()) bindCamera();
        else if (!hasCameraPermission() && !model.busy() && !requested) { requested = true; cameraPermission.launch(Manifest.permission.CAMERA); }
    }
    @Override public void onResume() { super.onResume(); if(hasCameraPermission()) permanentDenial=false; if(hasMicrophonePermission()) microphonePermanent=false; if (preview != null && !model.busy() && hasCameraPermission() && !ready && !binding) bindCamera(); render(); }
    private boolean hasCameraPermission() { return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED; }
    private void bindCamera() {
        if (preview == null || model.busy() || binding || ready) return;
        binding = true; cameraError=false;
        try {
            if(camera!=null) camera.unbind();
            camera = new LifecycleCameraController(requireContext());
            camera.setCameraSelector(CameraSelector.DEFAULT_BACK_CAMERA);
            camera.setEnabledUseCases(CameraController.IMAGE_CAPTURE);
            camera.setImageCaptureFlashMode(ImageCapture.FLASH_MODE_OFF);
            camera.setTapToFocusEnabled(true); camera.setPinchToZoomEnabled(true);
            preview.setController(camera); camera.bindToLifecycle(getViewLifecycleOwner());
            LifecycleCameraController initializing = camera;
            camera.getInitializationFuture().addListener(() -> {
                if (preview == null || camera != initializing) return;
                binding = false;
                try { camera.getInitializationFuture().get(); ready = true;
                    List<Quality> supported = QualitySelector.getSupportedQualities(camera.getCameraInfo());
                    videoSupported = supported.contains(Quality.HD) || supported.contains(Quality.SD);
                    if (videoSupported) {
                        camera.setVideoCaptureQualitySelector(QualitySelector.fromOrderedList(java.util.Arrays.asList(Quality.HD, Quality.SD)));
                        camera.setVideoCaptureDynamicRange(DynamicRange.SDR);
                    }
                    configureMode(); render(); }
                catch (Exception e) { ready = false; cameraError=true; render(); }
            }, ContextCompat.getMainExecutor(requireContext()));
        } catch (Exception e) { binding = false; ready = false; cameraError=true; render(); }
    }
    private void capture() {
        if (model.state().phase == CameraCaptureModel.Phase.RECORDING) { stopRecording(false, false); return; }
        if (!ready || camera == null || model.busy() || !hasCameraPermission()) return;
        if (videoMode) { record(); return; }
        try {
            File target = model.beginPhoto(host, inspectionId, albumId, damageIndex);
            camera.takePicture(new ImageCapture.OutputFileOptions.Builder(target).build(), ContextCompat.getMainExecutor(requireContext()),
                    new ImageCapture.OnImageSavedCallback() {
                        public void onImageSaved(@NonNull ImageCapture.OutputFileResults result) { model.photoSaved(); }
                        public void onError(@NonNull ImageCaptureException error) { model.photoFailed(error); }
                    });
        } catch (Exception e) { if (model.busy()) model.photoFailed(e); else Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show(); }
    }
    private boolean hasMicrophonePermission() { return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED; }
    private void configureMode() {
        if (camera == null || !ready || model.busy()) return;
        try { camera.setEnabledUseCases(videoMode && videoSupported ? CameraController.VIDEO_CAPTURE : CameraController.IMAGE_CAPTURE); }
        catch (Exception e) { videoSupported = false; camera.setEnabledUseCases(CameraController.IMAGE_CAPTURE); }
    }
    private void record() {
        if (!videoSupported || ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) return;
        try {
            File target = model.beginVideo(host, inspectionId, albumId);
            CameraCaptureModel completion = model;
            recording = camera.startRecording(new FileOutputOptions.Builder(target)
                    .setDurationLimitMillis(VideoStore.MAX_DURATION_MILLIS).setFileSizeLimit(VideoStore.MAX_BYTES).build(),
                    AudioConfig.create(true), ContextCompat.getMainExecutor(requireContext()), event -> {
                        if (event instanceof VideoRecordEvent.Finalize) {
                            recording = null; completion.videoFinalized(((VideoRecordEvent.Finalize) event).getError());
                        } else {
                            elapsed = event.getRecordingStats().getRecordedDurationNanos() / 1000000000L;
                            if (timer != null) render();
                        }
                    });
        } catch (Exception e) {
            if (model.busy()) model.fail(e); else Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    private void stopRecording(boolean discard, boolean interrupted) {
        if (recording == null || model.state().phase != CameraCaptureModel.Phase.RECORDING) return;
        model.stopVideo(discard, interrupted); recording.stop();
    }
    private void back() {
        if (model.state().phase == CameraCaptureModel.Phase.RECORDING) {
            if (recordingBack != null) return;
            recordingBack = new AlertDialog.Builder(requireContext()).setTitle(R.string.camera_recording_back)
                .setPositiveButton(R.string.camera_save, (d, w) -> stopRecording(false, false))
                .setNegativeButton(R.string.camera_discard_video, (d, w) -> stopRecording(true, false))
                .setNeutralButton(R.string.camera_continue, null).create();
            recordingBack.setOnDismissListener(d -> recordingBack = null); recordingBack.show(); return;
        }
        if (model.busy()) { Toast.makeText(requireContext(), R.string.camera_wait, Toast.LENGTH_SHORT).show(); return; }
        dismiss();
    }
    private void render() {
        if (message == null) return;
        boolean recovery = model.state().phase == CameraCaptureModel.Phase.ERROR;
        boolean recordingNow = model.state().phase == CameraCaptureModel.Phase.RECORDING;
        shutter.setText(recordingNow ? R.string.camera_stop : videoMode ? R.string.camera_record : R.string.camera_take_photo);
        shutter.setContentDescription(shutter.getText());
        shutter.setEnabled(recordingNow || (ready && !model.busy() && (!videoMode || (videoSupported && hasMicrophonePermission())) && host.canCaptureMedia(inspectionId, albumId)));
        for (int i = 0; i < mode.getChildCount(); i++) mode.getChildAt(i).setEnabled(!model.busy());
        timer.setVisibility(videoMode ? View.VISIBLE : View.GONE);
        timer.setText(getString(R.string.camera_video_limits, String.format(Locale.ROOT, "%02d:%02d", elapsed / 60, elapsed % 60)));
        discard.setVisibility(recovery ? View.VISIBLE : View.GONE);
        permission.setVisibility((!hasCameraPermission() || (videoMode && videoSupported && !hasMicrophonePermission())) || recovery || cameraError || (!ready && !binding && requested) ? View.VISIBLE : View.GONE);
        permission.setText(recovery ? R.string.camera_retry_save : (permanentDenial || (videoMode && microphonePermanent)) ? R.string.camera_settings : videoMode && !hasMicrophonePermission() && hasCameraPermission() ? R.string.camera_microphone_retry : R.string.camera_permission_retry);
        permission.setOnClickListener(v -> {
            if (recovery) model.retry();
            else if (permanentDenial || (videoMode && microphonePermanent && !hasMicrophonePermission())) startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + requireContext().getPackageName())));
            else if (!hasCameraPermission()) { requested = true; cameraPermission.launch(Manifest.permission.CAMERA); }
            else if (videoMode && !hasMicrophonePermission()) microphonePermission.launch(Manifest.permission.RECORD_AUDIO);
            else bindCamera();
        });
        String error = model.state().error;
        if(cameraError && !model.busy()) error=getString(R.string.camera_unavailable);
        if (!model.busy() && ready && videoMode) {
            if (!videoSupported) error = getString(R.string.camera_video_unavailable);
            else if (!hasMicrophonePermission()) error = getString(R.string.camera_microphone_denied);
        }
        message.setText(recovery ? error : model.busy() ? getString(R.string.camera_saving)
                : !hasCameraPermission() ? getString(R.string.camera_permission_denied) : binding ? getString(R.string.camera_loading) : error);
        message.setVisibility(recordingNow || (ready && !model.busy() && error == null) ? View.GONE : View.VISIBLE);
    }
    @Override public void onSaveInstanceState(@NonNull Bundle out) {
        super.onSaveInstanceState(out); out.putBoolean("videoMode", videoMode); out.putBoolean("microphonePermanent", microphonePermanent); out.putBoolean("requested", requested); out.putBoolean("permanentDenial", permanentDenial);
    }
    @Override public void onStop() { stopRecording(false, true); super.onStop(); }
    @Override public void onDestroyView() {
        stopRecording(false, true);
        if (recordingBack != null) recordingBack.dismiss();
        if (model.state().phase == CameraCaptureModel.Phase.CAPTURING)
            model.fail(new java.io.IOException("La captura se interrumpió; puedes reintentar el guardado"));
        if (camera != null) camera.unbind(); camera = null; ready = false; binding = false;
        mode = null; timer = null; preview = null; message = null; shutter = null; permission = null; discard = null; super.onDestroyView();
    }
    @Override public void onDetach() { host = null; super.onDetach(); }
}
