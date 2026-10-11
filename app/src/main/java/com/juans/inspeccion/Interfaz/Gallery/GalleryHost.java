package com.juans.inspeccion.Interfaz.Gallery;

import com.juans.inspeccion.Mundo.Album;

/** IDs always resolve against the current activity's inspection, including editing albums. */
public interface GalleryHost {
    Album resolveGalleryAlbum(String inspectionId, String albumId);
    String galleryAlbumLabel(String albumId);
    boolean canDeleteGalleryPhoto(String inspectionId, String albumId);
    void galleryPhotoDeleted(String inspectionId, String albumId, String reference);
}
