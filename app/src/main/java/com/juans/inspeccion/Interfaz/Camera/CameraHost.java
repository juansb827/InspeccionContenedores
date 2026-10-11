package com.juans.inspeccion.Interfaz.Camera;

import com.juans.inspeccion.Interfaz.Gallery.GalleryHost;
import com.juans.inspeccion.Mundo.Inspeccion;
import java.io.IOException;

public interface CameraHost extends GalleryHost {
    Inspeccion cameraInspection();
    boolean canCaptureMedia(String inspectionId, String albumId);
    void cameraMediaAssociated() throws IOException;
}
