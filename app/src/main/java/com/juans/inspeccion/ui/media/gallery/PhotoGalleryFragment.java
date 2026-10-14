package com.juans.inspeccion.ui.media.gallery;

import com.juans.inspeccion.Interfaz.Gallery.GalleryDeletionModel;
import com.juans.inspeccion.Interfaz.Gallery.GalleryMediaItem;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.ComponentDialog;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.juans.inspeccion.Mundo.Album;
import com.juans.inspeccion.R;
import java.util.ArrayList;
import java.util.List;

/** A complete gallery screen in the inspection's lifecycle, not a floating modal. */
public class PhotoGalleryFragment extends DialogFragment {
    private String inspectionId, albumId, selectedReference;
    private boolean viewing;
    private int selectedPosition;
    private Parcelable gridState;
    private Bundle playbackState;
    private List<GalleryMediaItem> items = new ArrayList<>();
    private GalleryHost host;
    private GalleryDeletionModel deletion;
    private View root, toolbar, empty, deleting;
    private TextView title, context;
    private ImageButton remove;
    private RecyclerView grid;
    private GridLayoutManager layout;
    private ViewPager2 pager;
    private GalleryAdapter thumbnails, pages;
    private AlertDialog confirmation;
    private final ViewPager2.OnPageChangeCallback pageChanges = new ViewPager2.OnPageChangeCallback() {
        @Override public void onPageSelected(int position) {
            selectedPosition = position;
            if (position < items.size()) selectedReference = items.get(position).reference;
            resetPageZoom();
            if(pages!=null) pages.activate(viewing ? selectedReference : null);
            updateToolbar();
        }
    };

    public static PhotoGalleryFragment newInstance(String inspectionId, String albumId) {
        PhotoGalleryFragment fragment = new PhotoGalleryFragment();
        Bundle args = new Bundle(); args.putString("inspection", inspectionId); args.putString("album", albumId);
        fragment.setArguments(args); return fragment;
    }

    @Override public void onCreate(@Nullable Bundle state) {
        super.onCreate(state);
        setStyle(STYLE_NORMAL, R.style.PhotoGalleryTheme);
        host = (GalleryHost) requireActivity();
        inspectionId = requireArguments().getString("inspection");
        albumId = requireArguments().getString("album");
        deletion = new ViewModelProvider(this).get(GalleryDeletionModel.class);
        if (state != null) {
            viewing = state.getBoolean("viewing"); selectedPosition = state.getInt("position");
            selectedReference = state.getString("reference"); gridState = state.getParcelable("grid"); playbackState=state.getBundle("playback");
        }
    }

    @NonNull @Override public Dialog onCreateDialog(@Nullable Bundle state) {
        Dialog dialog = super.onCreateDialog(state);
        dialog.setCanceledOnTouchOutside(false);
        if (dialog instanceof ComponentDialog) {
            ((ComponentDialog) dialog).getOnBackPressedDispatcher().addCallback(this,
                    new OnBackPressedCallback(true) {
                        @Override public void handleOnBackPressed() { back(); }
                    });
        } else {
            dialog.setOnKeyListener((ignored, key, event) -> {
                if (key != KeyEvent.KEYCODE_BACK) return false;
                if (event.getAction() == KeyEvent.ACTION_UP) back();
                return true;
            });
        }
        return dialog;
    }

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container, @Nullable Bundle state) {
        return inflater.inflate(R.layout.photo_gallery, container, false);
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        super.onViewCreated(view, state);
        root = view; toolbar = view.findViewById(R.id.galleryToolbar);
        empty = view.findViewById(R.id.galleryEmpty); deleting = view.findViewById(R.id.galleryDeleting);
        title = view.findViewById(R.id.galleryTitle); context = view.findViewById(R.id.galleryContext);
        remove = view.findViewById(R.id.galleryDelete);
        grid = view.findViewById(R.id.galleryGrid); pager = view.findViewById(R.id.galleryPager);
        layout = new GridLayoutManager(requireContext(), 3);
        grid.setLayoutManager(layout);
        thumbnails = new GalleryAdapter(false, Glide.with(this), this::open);
        pages = new GalleryAdapter(true, Glide.with(this), ignored -> {}); pages.restorePlayback(playbackState);
        grid.setAdapter(thumbnails); pager.setAdapter(pages);
        pager.registerOnPageChangeCallback(pageChanges);
        grid.addOnLayoutChangeListener((v, l, t, r, b, oldL, oldT, oldR, oldB) -> {
            int widthDp = Math.round(grid.getWidth() / getResources().getDisplayMetrics().density);
            int columns = Math.max(3, widthDp / 118);
            if (columns != layout.getSpanCount()) layout.setSpanCount(columns);
        });
        view.findViewById(R.id.galleryBack).setOnClickListener(v -> back());
        remove.setOnClickListener(v -> { if(deletion.needsMetadataRetry()) { GalleryDeletionModel.Result completed=deletion.consume(host); if(completed!=null && completed.error!=null) toast(completed.error); else refresh(); updateToolbar(); } else confirmDeletion(); });
        refresh();
        deletion.results().observe(getViewLifecycleOwner(), result -> {
            if (result != null && !result.busy) {
                GalleryDeletionModel.Result completed = deletion.consume(host);
                if (completed != null) {
                    if (completed.error == null) {
                        refresh(); toast(getString(R.string.gallery_deleted));
                    } else toast(completed.error);
                }
            }
            updateToolbar();
        });
    }

    @Override public void onStart() {
        super.onStart();
        Window window = requireDialog().getWindow();
        if (window != null) window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        pages.activate(viewing ? selectedReference : null);
        updateToolbar();
    }

    public boolean deletionBusy() { return deletion != null && deletion.busy(); }

    private void refresh() {
        Album album = host.resolveGalleryAlbum(inspectionId, albumId);
        try { items=host.galleryMediaItems(inspectionId,albumId); }
        catch(java.io.IOException e) { items=album==null?new ArrayList<>():GalleryMediaItem.photos(album); toast("Los archivos requieren recuperación: "+e.getMessage()); }
        selectedPosition = GalleryMediaItem.position(items, selectedReference, selectedPosition);
        selectedReference = selectedPosition < 0 ? null : items.get(selectedPosition).reference;
        if (items.isEmpty()) viewing = false;
        thumbnails.submit(items); pages.submit(items);
        if (gridState != null) layout.onRestoreInstanceState(gridState);
        if (selectedPosition >= 0) pager.setCurrentItem(selectedPosition, false);
        ((TextView) empty).setText(album == null ? R.string.gallery_missing : R.string.gallery_empty);
        showMode();
    }

    private void open(int position) {
        if (deletion.busy() || position < 0 || position >= items.size()) return;
        gridState = layout.onSaveInstanceState();
        selectedPosition = position; selectedReference = items.get(position).reference;
        viewing = true; pager.setCurrentItem(position, false); resetPageZoom(); showMode();
    }

    private void back() {
        if (deletion.busy()) { toast(getString(R.string.gallery_wait)); return; }
        if (viewing) {
            viewing = false; resetPageZoom(); showMode();
            if (gridState != null) layout.onRestoreInstanceState(gridState);
        } else dismiss();
    }

    private void showMode() {
        grid.setVisibility(viewing || items.isEmpty() ? View.GONE : View.VISIBLE);
        pager.setVisibility(viewing ? View.VISIBLE : View.GONE);
        empty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
        toolbar.setBackgroundColor(ContextCompat.getColor(requireContext(), viewing ? R.color.gallery_viewer : R.color.AzulF));
        pages.activate(viewing ? selectedReference : null);
        updateToolbar();
    }

    private void updateToolbar() {
        if (root == null) return;
        title.setText(viewing && !items.isEmpty()
                ? getString(R.string.gallery_counter, selectedPosition + 1, items.size()) : getString(R.string.gallery_title));
        context.setText(getString(R.string.gallery_album_count, host.galleryAlbumLabel(albumId),
                getResources().getQuantityString(R.plurals.gallery_photo_count, items.size(), items.size())));
        boolean allowed = host.canDeleteGalleryPhoto(inspectionId, albumId);
        remove.setVisibility(viewing && allowed ? View.VISIBLE : View.GONE);
        remove.setEnabled(!deletion.busy() || deletion.needsMetadataRetry()); remove.setAlpha(deletion.busy() && !deletion.needsMetadataRetry() ? 0.4f : 1f);
        remove.setContentDescription(getString(deletion.needsMetadataRetry() ? R.string.camera_retry_save : selectedPosition>=0 && selectedPosition<items.size() && items.get(selectedPosition).kind==GalleryMediaItem.Kind.VIDEO ? R.string.gallery_delete_video : R.string.gallery_delete));
        pager.setUserInputEnabled(!deletion.busy());
        deleting.setVisibility(deletion.busy() ? View.VISIBLE : View.GONE);
        Window window = getDialog() == null ? null : getDialog().getWindow();
        if (window != null) {
            window.setStatusBarColor(ContextCompat.getColor(requireContext(), viewing ? R.color.gallery_viewer : R.color.AzulF));
            window.setNavigationBarColor(ContextCompat.getColor(requireContext(), viewing ? R.color.gallery_viewer : R.color.gallery_surface));
            window.getDecorView().setSystemUiVisibility(viewing ? 0 : View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }
    }

    private void confirmDeletion() {
        if (!viewing || selectedReference == null || deletion.busy()
                || !host.canDeleteGalleryPhoto(inspectionId, albumId) || confirmation != null) return;
        String reference = selectedReference;
        confirmation = new AlertDialog.Builder(requireContext())
                .setTitle(items.get(selectedPosition).kind==GalleryMediaItem.Kind.VIDEO ? R.string.gallery_delete_video : R.string.gallery_delete).setMessage(items.get(selectedPosition).kind==GalleryMediaItem.Kind.VIDEO ? R.string.gallery_delete_video_message : R.string.gallery_delete_message)
                .setNegativeButton(R.string.gallery_cancel, null)
                .setPositiveButton(R.string.gallery_delete_confirm, (dialog, which) -> {
                    if (!deletion.delete(host, inspectionId, albumId, reference)) toast(getString(R.string.gallery_delete_denied));
                }).create();
        confirmation.setOnDismissListener(dialog -> confirmation = null);
        confirmation.show();
    }

    private void resetPageZoom() {
        if (pager == null) return;
        RecyclerView children = (RecyclerView) pager.getChildAt(0);
        for (int i = 0; i < children.getChildCount(); i++) {
            View page = children.getChildAt(i);
            if (page instanceof ViewGroup) {
                View photo = ((ViewGroup) page).getChildAt(0);
                if (photo instanceof PhotoView) ((PhotoView) photo).setScale(1f);
            }
        }
    }

    private void toast(String message) { Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show(); }

    @Override public void onSaveInstanceState(@NonNull Bundle out) {
        super.onSaveInstanceState(out);
        out.putBundle("playback",pages!=null ? pages.playbackState() : playbackState);
        out.putBoolean("viewing", viewing); out.putInt("position", selectedPosition);
        out.putString("reference", selectedReference);
        out.putParcelable("grid", !viewing && layout != null ? layout.onSaveInstanceState() : gridState);
    }

    @Override public void onStop() { if(pages!=null) pages.releaseAll(); super.onStop(); }
    @Override public void onDestroyView() {
        playbackState=pages.playbackState(); pages.releaseAll();
        if (confirmation != null) confirmation.dismiss();
        pager.unregisterOnPageChangeCallback(pageChanges);
        pager.setAdapter(null); grid.setAdapter(null);
        root = null; pager = null; grid = null; layout = null;
        super.onDestroyView();
    }

    @Override public void onDetach() { host = null; super.onDetach(); }
}
