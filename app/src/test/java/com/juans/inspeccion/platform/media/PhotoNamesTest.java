package com.juans.inspeccion.platform.media;

import com.juans.inspeccion.Mundo.Album;
import com.juans.inspeccion.Mundo.Inspeccion;

import android.content.Context;
import com.juans.inspeccion.Mundo.*;
import com.juans.inspeccion.R;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, application = android.app.Application.class)
public class PhotoNamesTest {
    @Test public void containerComesFromContainerDataAndMissingDocumentUsesTemp() {
        Context context = RuntimeEnvironment.getApplication(); Inspeccion inspection = inspection(context);
        assertEquals("ENTRADA_EP_temp_ABCD_0.jpg",
                PhotoNames.capture(context, inspection, inspection.getFotosGenerales(), -1));
    }
    @Test public void generalAndDamageRemoteNamesKeepDocumentAndReleasedNumbering() throws Exception {
        Context context = RuntimeEnvironment.getApplication(); Inspeccion inspection = inspection(context);
        inspection.getInformacion().put(context.getString(R.string.NNUMDOC), "123");
        assertEquals("ENTRADA_EP_123_ABCD_0.jpg", PhotoNames.capture(context, inspection, inspection.getFotosGenerales(), -1));
        Album damage = new Album(); damage.agregarFoto("content://photos/1");
        assertEquals("ENTRADA_EP_123_ABCD_DANIO#2_1.jpg", PhotoNames.capture(context, inspection, damage, 2));
    }
    @Test public void generalAndDamageShareReadableFolderAcrossAllFourModes() {
        for (String direction : new String[]{"ENTRADA", "SALIDA"}) for (String document : new String[]{"EP", "ET"}) {
            String prefix = direction + "_" + document + "_123_ABCD";
            assertEquals(prefix, PhotoNames.folderForPhoto(prefix + "_0.jpg"));
            assertEquals(prefix, PhotoNames.folderForPhoto(prefix + "_DANIO#2_7.jpg"));
        }
        assertEquals("ENTRADA_EP_temp_", PhotoNames.folderForPhoto("ENTRADA_EP_temp__0.jpg"));
        assertEquals("ENTRADA_EP_123_AB_CD", PhotoNames.folderForPhoto("ENTRADA_EP_123_AB_CD_2.jpg"));
    }
    @Test public void arbitraryImportedNamesDoNotInventInspectionDetails() {
        assertNull(PhotoNames.folderForPhoto("photo_0.jpg"));
        assertNull(PhotoNames.folderForPhoto("pending-capture.jpg"));
        assertNull(PhotoNames.folderForPhoto("ENTRADA_EP_123_ABCD_0-old-uuid.jpg"));
    }
    private Inspeccion inspection(Context context) {
        Inspeccion inspection = new Inspeccion(); inspection.setTipoInspeccion("ENTRADA"); inspection.setTipoDocumento("EP");
        inspection.getDatosContenedor().put(context.getString(R.string.CCODCNTR), "ABCD");
        return inspection;
    }
}
