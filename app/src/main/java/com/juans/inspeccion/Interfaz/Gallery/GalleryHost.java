package com.juans.inspeccion.Interfaz.Gallery;

import com.juans.inspeccion.Mundo.Album;

/** IDs always resolve against the current activity's inspection, including editing albums. */
public interface GalleryHost {
    default java.util.List<GalleryMediaItem> galleryMediaItems(String inspectionId, String albumId) throws java.io.IOException {
        Album album = resolveGalleryAlbum(inspectionId, albumId);
        return album == null ? new java.util.ArrayList<>() : GalleryMediaItem.photos(album);
    }
    default void galleryMediaDeleted(String inspectionId, String albumId, GalleryMediaItem item) throws java.io.IOException {
        galleryPhotoDeleted(inspectionId, albumId, item.reference);
    }
    Album resolveGalleryAlbum(String inspectionId, String albumId);
    String galleryAlbumLabel(String albumId);
    boolean canDeleteGalleryPhoto(String inspectionId, String albumId);
    void galleryPhotoDeleted(String inspectionId, String albumId, String reference);
}
