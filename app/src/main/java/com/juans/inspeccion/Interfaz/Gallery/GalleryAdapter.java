package com.juans.inspeccion.Interfaz.Gallery;

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.github.chrisbanes.photoview.PhotoView;
import com.juans.inspeccion.R;
import java.util.ArrayList;
import java.util.List;

final class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.Holder> {
    interface Click { void open(int position); }
    private final boolean viewer;
    private final RequestManager images;
    private final Click click;
    private List<GalleryMediaItem> items = new ArrayList<>();

    GalleryAdapter(boolean viewer, RequestManager images, Click click) {
        this.viewer = viewer; this.images = images; this.click = click;
        // Block restoration until the album has been resolved and supplied.
        setStateRestorationPolicy(StateRestorationPolicy.PREVENT_WHEN_EMPTY);
    }

    void submit(List<GalleryMediaItem> updated) { items = new ArrayList<>(updated); notifyDataSetChanged(); }
    @Override public int getItemCount() { return items.size(); }
    @Override public int getItemViewType(int position) { return items.get(position).kind.ordinal(); }

    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        FrameLayout frame = viewer ? new PhotoFrame(parent) : new SquareTile(parent);
        frame.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                viewer ? ViewGroup.LayoutParams.MATCH_PARENT : ViewGroup.LayoutParams.WRAP_CONTENT));
        if (!viewer) frame.setPadding(dp(parent, 4), dp(parent, 4), dp(parent, 4), dp(parent, 4));
        ImageView photo = viewer ? new PhotoView(parent.getContext()) : new ImageView(parent.getContext());
        photo.setScaleType(viewer ? ImageView.ScaleType.FIT_CENTER : ImageView.ScaleType.CENTER_CROP);
        frame.addView(photo, new FrameLayout.LayoutParams(-1, -1));
        TextView message = new TextView(parent.getContext());
        message.setGravity(android.view.Gravity.CENTER);
        message.setPadding(dp(parent, 8), dp(parent, 8), dp(parent, 8), dp(parent, 8));
        message.setTextColor(ContextCompat.getColor(parent.getContext(), viewer ? android.R.color.white : R.color.gallery_text));
        frame.addView(message, new FrameLayout.LayoutParams(-1, -1));
        if (viewer) {
            PhotoView zoom = (PhotoView) photo;
            zoom.setScaleLevels(1f, 2f, 4f);
            zoom.setOnDoubleTapListener(new GestureDetector.SimpleOnGestureListener() {
                @Override public boolean onDoubleTap(MotionEvent event) {
                    zoom.setScale(zoom.getScale() > 1.01f ? 1f : 2f, event.getX(), event.getY(), true);
                    return true;
                }
            });
        }
        Holder holder = new Holder(frame, photo, message);
        if (!viewer) frame.setOnClickListener(v -> {
            int position = holder.getBindingAdapterPosition();
            if (position != RecyclerView.NO_POSITION) click.open(position);
        });
        return holder;
    }

    @Override public void onBindViewHolder(@NonNull Holder holder, int position) {
        GalleryMediaItem item = items.get(position);
        images.clear(holder.photo);
        holder.photo.setImageDrawable(null);
        holder.message.setVisibility(View.VISIBLE);
        holder.message.setText(R.string.gallery_loading);
        holder.itemView.setContentDescription(holder.itemView.getContext().getString(R.string.gallery_photo, position + 1));
        if (holder.photo instanceof PhotoView) {
            ((PhotoView) holder.photo).setZoomable(false);
            ((PhotoView) holder.photo).setScale(1f);
        }
        // Legacy paths are deliberately unavailable until the existing importer converts them.
        if (item.kind != GalleryMediaItem.Kind.PHOTO || !item.reference.startsWith("content://")) {
            unavailable(holder); return;
        }
        images.load(Uri.parse(item.reference))
                .placeholder(new ColorDrawable(ContextCompat.getColor(holder.itemView.getContext(),
                        viewer ? R.color.gallery_viewer : R.color.gallery_placeholder)))
                .listener(new RequestListener<Drawable>() {
                    @Override public boolean onLoadFailed(@Nullable GlideException error, Object model,
                            Target<Drawable> target, boolean first) { unavailable(holder); return false; }
                    @Override public boolean onResourceReady(Drawable resource, Object model,
                            Target<Drawable> target, DataSource source, boolean first) {
                        if (holder.photo instanceof PhotoView) ((PhotoView) holder.photo).setZoomable(true);
                        holder.message.setVisibility(View.GONE); return false;
                    }
                }).into(holder.photo);
    }

    private void unavailable(Holder holder) {
        holder.message.setVisibility(View.VISIBLE);
        holder.message.setText(R.string.gallery_unavailable);
        holder.itemView.setContentDescription(holder.itemView.getContext().getString(R.string.gallery_unavailable));
    }

    @Override public void onViewRecycled(@NonNull Holder holder) {
        images.clear(holder.photo); holder.photo.setImageDrawable(null); super.onViewRecycled(holder);
    }

    static int dp(View view, int value) { return Math.round(value * view.getResources().getDisplayMetrics().density); }
    static final class Holder extends RecyclerView.ViewHolder {
        final ImageView photo; final TextView message;
        Holder(View root, ImageView photo, TextView message) { super(root); this.photo = photo; this.message = message; }
    }
    private static final class SquareTile extends FrameLayout {
        SquareTile(ViewGroup parent) { super(parent.getContext()); }
        @Override protected void onMeasure(int width, int height) {
            super.onMeasure(width, MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(width), MeasureSpec.EXACTLY));
        }
    }

    /** RecyclerView must not steal the first moving finger of a pinch gesture. */
    private static final class PhotoFrame extends FrameLayout {
        private boolean pinching, paging;
        private float downX, downY;
        private final int touchSlop;
        PhotoFrame(ViewGroup parent) {
            super(parent.getContext()); touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        }
        @Override public boolean dispatchTouchEvent(MotionEvent event) {
            int action = event.getActionMasked();
            if (action == MotionEvent.ACTION_DOWN) {
                pinching = false; paging = false; downX = event.getX(); downY = event.getY();
            }
            if (event.getPointerCount() > 1) {
                pinching = true; paging = false; requestDisallowInterceptTouchEvent(true);
            } else if (action == MotionEvent.ACTION_MOVE && !pinching && getChildCount() > 0) {
                PhotoView photo = (PhotoView) getChildAt(0);
                float dx = Math.abs(event.getX() - downX), dy = Math.abs(event.getY() - downY);
                if (photo.getScale() <= 1.01f && dx > touchSlop && dx > dy) {
                    paging = true; requestDisallowInterceptTouchEvent(false);
                }
            }
            boolean handled = super.dispatchTouchEvent(event);
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                pinching = false; paging = false;
            }
            return handled;
        }
        @Override public void requestDisallowInterceptTouchEvent(boolean disallow) {
            super.requestDisallowInterceptTouchEvent(pinching || (!paging && disallow));
        }
    }
}
