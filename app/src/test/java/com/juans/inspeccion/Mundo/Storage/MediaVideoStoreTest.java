package com.juans.inspeccion.Mundo.Storage;

import com.juans.inspeccion.platform.media.VideoStore;
import android.content.*;
import android.provider.MediaStore;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowContentResolver;
import java.io.*;
import static org.junit.Assert.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=29,application=android.app.Application.class)
public class MediaVideoStoreTest {
    private Context context; private MediaVideoStore store; private MediaPhotoStoreTest.MediaProvider provider;
    @Before public void setup() {
        context=RuntimeEnvironment.getApplication(); store=new MediaVideoStore(context);
        provider=new MediaPhotoStoreTest.MediaProvider(); android.content.pm.ProviderInfo info=new android.content.pm.ProviderInfo(); info.authority="media"; info.exported=true;
        provider.attachInfo(context,info); ShadowContentResolver.registerProviderInternal("media",provider);
    }
    @Test public void streamsIntoMoviesAndRetryIsIdempotentWithoutPhotoPublication() throws Exception {
        File file=store.createCapture("video-test"); try(FileOutputStream out=new FileOutputStream(file)) {out.write(new byte[]{1,2,3});}
        String reference=store.publish(file,"inspection","clip","video-test");
        assertEquals(1,provider.rows.size()); assertEquals("Movies/Inspecciones/inspection/",provider.rows.get(1L).getAsString(MediaStore.MediaColumns.RELATIVE_PATH));
        assertEquals("video/mp4",provider.rows.get(1L).getAsString(MediaStore.MediaColumns.MIME_TYPE));
        assertEquals(Integer.valueOf(0),provider.rows.get(1L).getAsInteger(MediaStore.MediaColumns.IS_PENDING));
        assertEquals(reference,new MediaVideoStore(context).publish(file,"inspection","clip","video-test")); assertEquals(1,provider.rows.size());
        assertFalse(context.getFileStreamPath("photo-publications").exists());
    }
    @Test public void failedCopyRetainsPendingRowAndRetryDoesNotDuplicate() throws Exception {
        File missing=context.getFileStreamPath("missing.mp4");
        try {store.publish(missing,"inspection","clip","retry"); fail();}catch(IOException expected){}
        assertEquals(Integer.valueOf(1),provider.rows.get(1L).getAsInteger(MediaStore.MediaColumns.IS_PENDING));
        try(FileOutputStream out=new FileOutputStream(missing)) {out.write(1);}
        store.publish(missing,"inspection","clip","retry"); assertEquals(1,provider.rows.size());
        assertEquals(Integer.valueOf(0),provider.rows.get(1L).getAsInteger(MediaStore.MediaColumns.IS_PENDING));
    }
    @Test public void emptyOversizedAndIncompleteClipsAreNeverValidated() throws Exception {
        File file=store.createCapture("empty"); try {store.validate(file);fail();}catch(IOException expected){}
        try(RandomAccessFile bytes=new RandomAccessFile(file,"rw")) {bytes.setLength(VideoStore.MAX_BYTES+1);}
        try {store.validate(file);fail();}catch(IOException expected){}
        try(FileOutputStream out=new FileOutputStream(file)) {out.write(new byte[]{1,2,3});}
        try {store.validate(file);fail();}catch(IOException expected){}
        assertTrue(provider.rows.isEmpty());
    }
}
