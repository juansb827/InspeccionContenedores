package com.juans.inspeccion.Interfaz.Camera;

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
    private boolean requested, ready, permanentDenial, binding;
    private final ActivityResultLauncher<String> cameraPermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), granted -> {
                permanentDenial = !granted && !shouldShowRequestPermissionRationale(Manifest.permission.CAMERA);
                if (granted) bindCamera(); else render();
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
        ((TextView) root.findViewById(R.id.cameraContext)).setText(host.galleryAlbumLabel(albumId));
        root.findViewById(R.id.cameraBack).setOnClickListener(v -> back());
        shutter.setOnClickListener(v -> capture()); discard.setOnClickListener(v -> { model.discard(); render(); });
        permission.setOnClickListener(v -> {
            if (permanentDenial) startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + requireContext().getPackageName())));
            else { requested = true; cameraPermission.launch(Manifest.permission.CAMERA); }
        });
        model.states().observe(getViewLifecycleOwner(), stateNow -> {
            if (stateNow.phase == CameraCaptureModel.Phase.DONE) dismissAllowingStateLoss(); else render();
        });
        render();
    }
    @Override public void onStart() {
        super.onStart(); requireDialog().getWindow().setLayout(-1, -1);
        if (hasCameraPermission() && !model.busy()) bindCamera();
        else if (!requested) { requested = true; cameraPermission.launch(Manifest.permission.CAMERA); }
    }
    @Override public void onResume() { super.onResume(); if (preview != null && !model.busy() && hasCameraPermission() && !ready && !binding) bindCamera(); }
    private boolean hasCameraPermission() { return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED; }
    private void bindCamera() {
        if (preview == null || binding || ready) return;
        binding = true;
        try {
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
                try { camera.getInitializationFuture().get(); ready = true; render(); }
                catch (Exception e) { ready = false; message.setText(R.string.camera_unavailable); permission.setVisibility(View.VISIBLE); }
            }, ContextCompat.getMainExecutor(requireContext()));
        } catch (Exception e) { binding = false; ready = false; message.setText(R.string.camera_unavailable); permission.setVisibility(View.VISIBLE); }
    }
    private void capture() {
        if (!ready || camera == null || model.busy() || !hasCameraPermission()) return;
        try {
            File target = model.beginPhoto(host, inspectionId, albumId, damageIndex);
            camera.takePicture(new ImageCapture.OutputFileOptions.Builder(target).build(), ContextCompat.getMainExecutor(requireContext()),
                    new ImageCapture.OnImageSavedCallback() {
                        public void onImageSaved(@NonNull ImageCapture.OutputFileResults result) { model.photoSaved(); }
                        public void onError(@NonNull ImageCaptureException error) { model.photoFailed(error); }
                    });
        } catch (Exception e) { if (model.busy()) model.photoFailed(e); else Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show(); }
    }
    private void back() {
        if (model.busy()) { Toast.makeText(requireContext(), R.string.camera_wait, Toast.LENGTH_SHORT).show(); return; }
        dismiss();
    }
    private void render() {
        if (message == null) return;
        boolean recovery = model.state().phase == CameraCaptureModel.Phase.ERROR;
        shutter.setEnabled(ready && !model.busy() && host.canCaptureMedia(inspectionId, albumId));
        discard.setVisibility(recovery ? View.VISIBLE : View.GONE);
        permission.setVisibility(!hasCameraPermission() || recovery || (!ready && !binding && requested) ? View.VISIBLE : View.GONE);
        permission.setText(recovery ? R.string.camera_retry_save : permanentDenial ? R.string.camera_settings : R.string.camera_permission_retry);
        permission.setOnClickListener(v -> {
            if (recovery) model.retry();
            else if (permanentDenial) startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + requireContext().getPackageName())));
            else if (!hasCameraPermission()) { requested = true; cameraPermission.launch(Manifest.permission.CAMERA); }
            else bindCamera();
        });
        String error = model.state().error;
        message.setText(recovery ? error : model.busy() ? getString(R.string.camera_saving)
                : !hasCameraPermission() ? getString(R.string.camera_permission_denied) : binding ? getString(R.string.camera_loading) : error);
        message.setVisibility(ready && !model.busy() && error == null ? View.GONE : View.VISIBLE);
    }
    @Override public void onSaveInstanceState(@NonNull Bundle out) {
        super.onSaveInstanceState(out); out.putBoolean("requested", requested); out.putBoolean("permanentDenial", permanentDenial);
    }
    @Override public void onDestroyView() {
        if (model.state().phase == CameraCaptureModel.Phase.CAPTURING)
            model.fail(new java.io.IOException("La captura se interrumpió; puedes reintentar el guardado"));
        if (camera != null) camera.unbind(); camera = null; ready = false; binding = false;
        preview = null; message = null; shutter = null; permission = null; discard = null; super.onDestroyView();
    }
    @Override public void onDetach() { host = null; super.onDetach(); }
}
