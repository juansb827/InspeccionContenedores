package com.juans.inspeccion.ui.media.camera;

import com.juans.inspeccion.ui.media.gallery.GalleryHost;
import com.juans.inspeccion.Mundo.Inspeccion;
import java.io.IOException;

public interface CameraHost extends GalleryHost {
    Inspeccion cameraInspection();
    boolean canCaptureMedia(String inspectionId, String albumId);
    void cameraMediaAssociated() throws IOException;
}
