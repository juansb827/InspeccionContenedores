package com.juans.inspeccion.data.local;

import com.juans.inspeccion.Mundo.*;
import com.juans.inspeccion.platform.media.LocalMediaIndex;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk={29,34},application=android.app.Application.class)
public class MediaStagingRecoveryTest {
    // MEDIA-005 / DRAFT-006 audit: active recovery/bookkeeping must not appear empty.
    @Test public void stagingOnlySerializedBookkeepingIsPreservedAndBlocksEmptyRead() throws Exception {
        File base=RuntimeEnvironment.getApplication().getFileStreamPath("photo-publications"); File staging=new File(base+".new");
        AtomicObjects.write(staging,new HashMap<String,String>()); byte[] before=Files.readAllBytes(staging.toPath());
        try { AtomicObjects.read(base); fail(); } catch(IOException expected) { assertTrue(expected.getMessage().contains(".new")); }
        assertFalse(base.exists()); assertArrayEquals(before,Files.readAllBytes(staging.toPath()));
    }
    @Test public void stagingOnlyMediaIndexCannotBeReplacedWithAnEmptyIndex() throws Exception {
        android.content.Context app=RuntimeEnvironment.getApplication(); File base=app.getFileStreamPath("local-media-index.json"), staging=new File(base+".new");
        byte[] bytes="{\"version\":1,\"albums\":{}}".getBytes(java.nio.charset.StandardCharsets.UTF_8); Files.write(staging.toPath(),bytes);
        Inspeccion owner=new Inspeccion();
        try { new LocalMediaIndex(app).addVideo(owner.getPhotoId(),owner.getFotosGenerales(),"content://fixture/video/1",1000); fail(); }
        catch(IOException expected) { assertTrue(expected.getMessage().contains(".new")); }
        assertFalse(base.exists()); assertArrayEquals(bytes,Files.readAllBytes(staging.toPath()));
    }
}
