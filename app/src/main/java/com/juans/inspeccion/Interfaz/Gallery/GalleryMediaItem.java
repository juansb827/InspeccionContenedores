package com.juans.inspeccion.Interfaz.Gallery;

import com.juans.inspeccion.Mundo.Album;
import java.util.ArrayList;
import java.util.List;

/** Presentation model only: never written into serialized inspections. */
public final class GalleryMediaItem {
    public enum Kind { PHOTO, VIDEO }
    public final String id;
    public final String reference;
    public final String mimeType;
    public final Kind kind;

    public GalleryMediaItem(String reference, String mimeType, Kind kind) {
        this.id = reference;
        this.reference = reference;
        this.mimeType = mimeType;
        this.kind = kind;
    }

    public static List<GalleryMediaItem> photos(Album album) {
        List<GalleryMediaItem> result = new ArrayList<>();
        for (String reference : album.getFotos())
            result.add(new GalleryMediaItem(reference, "image/jpeg", Kind.PHOTO));
        return result;
    }

    public static int position(List<GalleryMediaItem> items, String reference, int fallback) {
        for (int i = 0; i < items.size(); i++) if (items.get(i).reference.equals(reference)) return i;
        return items.isEmpty() ? -1 : Math.max(0, Math.min(fallback, items.size() - 1));
    }
}
