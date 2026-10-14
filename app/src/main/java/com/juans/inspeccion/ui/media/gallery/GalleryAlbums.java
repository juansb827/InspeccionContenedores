package com.juans.inspeccion.ui.media.gallery;

import com.juans.inspeccion.Mundo.Album;
import com.juans.inspeccion.Mundo.Danio;
import com.juans.inspeccion.Mundo.Inspeccion;

public final class GalleryAlbums {
    private GalleryAlbums() {}

    public static Album resolve(Inspeccion inspection, Album editing, String inspectionId, String albumId) {
        if (inspection == null || !inspection.getPhotoId().equals(inspectionId)) return null;
        Album saved = inspection.findAlbum(albumId);
        return saved != null ? saved : editing != null && editing.getId().equals(albumId) ? editing : null;
    }

    public static boolean isGeneral(Inspeccion inspection, String albumId) {
        return inspection.getFotosGenerales() != null && inspection.getFotosGenerales().getId().equals(albumId);
    }

    public static String label(Inspeccion inspection, String albumId) {
        if (isGeneral(inspection, albumId)) return "Información";
        if (inspection.getDaniosManager() == null) return "Daño en edición";
        int number = 1;
        for (Danio damage : inspection.getDaniosManager().getListaDanios()) {
            if (damage.getAlbum().getId().equals(albumId)) return "Daño " + number;
            number++;
        }
        return "Daño en edición";
    }

    public static boolean canDelete(Inspeccion inspection, Album editing, String inspectionId,
                                    String albumId, boolean readOnly, String action, boolean busy) {
        return resolve(inspection, editing, inspectionId, albumId) != null && !readOnly
                && action == null && !busy
                && (isGeneral(inspection, albumId) || !"SALIDA".equals(inspection.getTipoInspeccion()));
    }
}
