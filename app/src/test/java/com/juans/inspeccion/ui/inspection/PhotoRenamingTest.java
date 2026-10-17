package com.juans.inspeccion.ui.inspection;

import android.content.Context;
import android.content.pm.ProviderInfo;
import com.juans.inspeccion.Mundo.Album;
import com.juans.inspeccion.Mundo.Danio;
import com.juans.inspeccion.Mundo.Inspeccion;
import com.juans.inspeccion.Mundo.Storage.MediaPhotoStore;
import com.juans.inspeccion.Mundo.Storage.MediaPhotoStoreTest;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowContentResolver;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 29, application = android.app.Application.class)
public class PhotoRenamingTest {
    @Test public void actualSubmissionNamingKeepsUrisAndRemoteNamesWithoutLocalUuidSuffixes() throws Exception {
        Context context = RuntimeEnvironment.getApplication();
        MediaPhotoStoreTest.MediaProvider provider = new MediaPhotoStoreTest.MediaProvider();
        ProviderInfo info = new ProviderInfo(); info.authority = "media"; info.exported = true;
        provider.attachInfo(context, info);
        ShadowContentResolver.registerProviderInternal("media", provider);
        MediaPhotoStore store = new MediaPhotoStore(context);
        com.juans.inspeccion.Mundo.Storage.MediaVideoStore videos = new com.juans.inspeccion.Mundo.Storage.MediaVideoStore(context);
        com.juans.inspeccion.platform.media.LocalMediaIndex index = new com.juans.inspeccion.platform.media.LocalMediaIndex(context);
        Inspeccion inspection = new Inspeccion();
        inspection.setTipoInspeccion("ENTRADA"); inspection.setTipoDocumento("EP");
        String owner = inspection.getPhotoId();
        String earlier = store.importPhoto(new ByteArrayInputStream(new byte[]{1}), owner, "ENTRADA_EP_123_ABCD_0.jpg", "earlier");
        String generalRef = store.importPhoto(new ByteArrayInputStream(new byte[]{2}), owner, "ENTRADA_EP_temp_ABCD_0.jpg", "general");
        Album general = inspection.getFotosGenerales(); general.agregarFoto(generalRef);
        Danio damage = new Danio(); inspection.getDaniosManager().getListaDanios().add(damage);
        String damageRef = store.importPhoto(new ByteArrayInputStream(new byte[]{3}), owner, "ENTRADA_EP_temp_ABCD_DANIO#0_0.jpg", "damage");
        damage.getAlbum().agregarFoto(damageRef);
        String otherTemp = store.importPhoto(new ByteArrayInputStream(new byte[]{4}), "other-owner", "ENTRADA_EP_temp_ABCD_0.jpg", "unrelated");
        assertEquals("Pictures/Inspecciones/ENTRADA_EP_temp_ABCD/", folder(context, generalRef));
        assertEquals(folder(context, generalRef), folder(context, damageRef));
        java.io.File clip=videos.createCapture("fixture");try(java.io.FileOutputStream out=new java.io.FileOutputStream(clip)){out.write(5);}
        videos.publish(clip,owner,"ENTRADA_EP_123_ABCD_0.mp4","existing-video");
        String generalVideo=videos.publish(clip,owner,"ENTRADA_EP_temp_ABCD_0.mp4","general-video");
        String damageVideo=videos.publish(clip,owner,"ENTRADA_EP_temp_ABCD_DANIO#0_0.mp4","damage-video");
        index.addVideo(owner,general,generalVideo,1000);index.addVideo(owner,damage.getAlbum(),damageVideo,1000);

        // Do not start the inspection lifecycle or any database/printing work.
        InspeccionActivity activity = Robolectric.buildActivity(InspeccionActivity.class).get();
        set(activity, "inspeccionObject", inspection); set(activity, "photoStore", store);
        set(activity,"mediaIndex",index);
        set(activity,"videoCoordinator",new com.juans.inspeccion.Mundo.Storage.VideoCaptureCoordinator(videos,context.getFileStreamPath("naming-videos")));
        activity.renombrarFotos("123", "ABCD");
        activity.renombrarFotos("123", "ABCD");

        assertEquals("ENTRADA_EP_123_ABCD_1.jpg", store.displayName(generalRef));
        assertEquals("ENTRADA_EP_123_ABCD_DANIO#0_0.jpg", store.displayName(damageRef));
        assertEquals("Pictures/Inspecciones/ENTRADA_EP_123_ABCD/", folder(context, generalRef));
        assertEquals(folder(context, generalRef), folder(context, damageRef));
        assertEquals("Pictures/Inspecciones/ENTRADA_EP_temp_ABCD/", folder(context, otherTemp));
        assertEquals("Movies/Inspecciones/ENTRADA_EP_123_ABCD/",folder(context,generalVideo));
        assertEquals(folder(context,generalVideo),folder(context,damageVideo));
        assertEquals("ENTRADA_EP_123_ABCD_1.mp4",store.displayName(generalVideo));
        assertEquals("ENTRADA_EP_123_ABCD_DANIO#0_0.mp4",store.displayName(damageVideo));
        assertEquals(1,general.getFotos().size());assertEquals(2,index.items(owner,general).size());
        assertEquals(generalRef, general.getFotos().get(0));
        assertEquals(damageRef, damage.getAlbum().getFotos().get(0));
        assertEquals("ENTRADA_EP_123_ABCD_0.jpg", general.recordedRemoteName(generalRef));
        assertEquals("ENTRADA_EP_123_ABCD_DANIO#0_0.jpg", damage.getAlbum().recordedRemoteName(damageRef));
        try (InputStream in = store.open(earlier)) { assertEquals(1, in.read()); }
        try (InputStream in = store.open(generalRef)) { assertEquals(2, in.read()); }
        try (InputStream in = store.open(damageRef)) { assertEquals(3, in.read()); }
        try (InputStream in = store.open(otherTemp)) { assertEquals(4, in.read()); }
    }

    private static String folder(Context context, String reference) {
        try (android.database.Cursor cursor = context.getContentResolver().query(android.net.Uri.parse(reference),
                new String[]{android.provider.MediaStore.Images.Media.RELATIVE_PATH}, null, null, null)) {
            assertNotNull(cursor); assertTrue(cursor.moveToFirst()); return cursor.getString(0);
        }
    }

    private static void set(InspeccionActivity activity, String name, Object value) throws Exception {
        Field field = InspeccionActivity.class.getDeclaredField(name);
        field.setAccessible(true); field.set(activity, value);
    }
}
