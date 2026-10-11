package com.juans.inspeccion.Interfaz.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.*;
import androidx.fragment.app.DialogFragment;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import com.juans.inspeccion.Interfaz.CustomAdapters.GridViewAdapter;
import com.juans.inspeccion.Interfaz.CustomAdapters.ImageItem;
import com.juans.inspeccion.Interfaz.InspeccionActivity;
import com.juans.inspeccion.Mundo.Album;
import com.juans.inspeccion.Mundo.Storage.MediaPhotoStore;
import com.juans.inspeccion.Mundo.Storage.PhotoStore;
import com.juans.inspeccion.R;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class PhotoGridDialog extends DialogFragment {
    private static final ExecutorService WORKER = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());
    private Album album;
    private PhotoStore photos;
    private GridView grid;
    private ImageView full;
    private View view;
    private int selected = -1;
    private int deleteIndex = -1;
    private boolean deleting;
    private final ActivityResultLauncher<String[]> storagePermission = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(), grants -> {
                if (!isAdded()) return;
                boolean granted = !MediaPhotoStore.needsStoragePermission(requireContext());
                if (granted && deleteIndex >= 0) deletePhoto(deleteIndex);
                else if (isAdded()) Toast.makeText(requireContext(), "Permiso denegado; la foto se conserva", Toast.LENGTH_LONG).show();
            });

    public static PhotoGridDialog newInstance(String inspectionId, String albumId) {
        PhotoGridDialog dialog = new PhotoGridDialog();
        Bundle args = new Bundle(); args.putString("inspection", inspectionId); args.putString("album", albumId);
        dialog.setArguments(args); return dialog;
    }
    @Override public void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out); out.putInt("selected", selected); out.putInt("deleteIndex", deleteIndex);
    }
    @Override public Dialog onCreateDialog(Bundle state) {
        InspeccionActivity activity = (InspeccionActivity) requireActivity();
        if (state != null) { selected = state.getInt("selected", -1); deleteIndex = state.getInt("deleteIndex", -1); }
        String id = requireArguments().getString("inspection");
        album = id.equals(activity.getInspeccion().getPhotoId()) ? activity.findPhotoAlbum(requireArguments().getString("album")) : null;
        if (album == null) return new AlertDialog.Builder(activity).setMessage("El álbum ya no está disponible").setPositiveButton("Cerrar", null).create();
        photos = new MediaPhotoStore(activity);
        view = activity.getLayoutInflater().inflate(R.layout.gallery, null);
        grid = view.findViewById(R.id.gridView); full = view.findViewById(R.id.fullScreenPhoto);
        full.setVisibility(View.GONE);
        Button expand = view.findViewById(R.id.btnExpandir), remove = view.findViewById(R.id.btnBorrar);
        expand.setEnabled(selected >= 0); remove.setEnabled(selected >= 0);
        grid.setOnItemClickListener((parent, row, position, itemId) -> {
            selected = position;
            ((GridViewAdapter) grid.getAdapter()).setSelectedIndex(position);
            expand.setEnabled(true); remove.setEnabled(true);
        });
        expand.setOnClickListener(v -> {
            if (selected < 0 || selected >= album.getFotos().size()) return;
            String reference = album.getFotos().get(selected);
            WORKER.execute(() -> {
                try {
                    Bitmap bitmap = photos.thumbnail(reference, 1600, 1600);
                    main.post(() -> { if (isAdded()) { full.setImageBitmap(bitmap); full.setVisibility(View.VISIBLE); grid.setVisibility(View.GONE); } else bitmap.recycle(); });
                } catch (Exception e) { error(e); }
            });
        });
        remove.setOnClickListener(v -> {
            if (selected < 0 || selected >= album.getFotos().size()) return;
            new AlertDialog.Builder(activity).setTitle("Borrar foto")
                    .setMessage("Se borrará también de la galería del dispositivo. ¿Continuar?")
                    .setNegativeButton("Cancelar", null).setPositiveButton("Borrar", (d, which) -> {
                        deleteIndex = selected;
                        if (MediaPhotoStore.needsStoragePermission(activity)) storagePermission.launch(MediaPhotoStore.storagePermissions());
                        else deletePhoto(deleteIndex);
                    }).show();
        });
        load();
        return new AlertDialog.Builder(activity).setView(view).setPositiveButton("Cerrar", null).create();
    }
    private void load() {
        ArrayList<String> refs = new ArrayList<>(album.getFotos());
        WORKER.execute(() -> {
            ArrayList<ImageItem> data = new ArrayList<>();
            for (int i = 0; i < refs.size(); i++) {
                try { data.add(new ImageItem(photos.thumbnail(refs.get(i), 150, 150), "Foto " + (i+1), refs.get(i))); }
                catch (Exception e) {
                    // Keep indices aligned so an unreadable thumbnail cannot delete a different photo.
                    data.add(new ImageItem(null, "Foto no disponible", refs.get(i)));
                }
            }
            main.post(() -> { if (isAdded()) grid.setAdapter(new GridViewAdapter(requireActivity(), R.layout.grid_item_layout, data)); });
        });
    }
    private void deletePhoto(int index) {
        if (deleting || !isAdded()) return;
        deleting = true;
        view.findViewById(R.id.btnBorrar).setEnabled(false);
        android.content.Context context = requireContext().getApplicationContext();
        WORKER.execute(() -> {
            try {
                album.deletePhoto(context, index);
                main.post(() -> { if (isAdded()) { ((InspeccionActivity) requireActivity()).persistPhotoChanges(); Toast.makeText(requireContext(), "Foto borrada", Toast.LENGTH_SHORT).show(); dismiss(); } });
            } catch (Exception e) { main.post(() -> { deleting = false; if (isAdded()) view.findViewById(R.id.btnBorrar).setEnabled(true); }); error(e); }
        });
    }
    private void error(Exception e) {
        main.post(() -> { if (isAdded()) Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show(); });
    }
}
