package com.juans.inspeccion.Mundo.Storage;
import android.app.Application;
import com.juans.inspeccion.Mundo.*;
import com.juans.inspeccion.Interfaz.Gallery.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import java.io.*;
import java.util.*;
import static org.junit.Assert.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=29,application=Application.class)
public class LocalMediaIndexTest {
    private Application app; private LocalMediaIndex index; private Inspeccion owner; private Album album;
    @Before public void setup() { app=RuntimeEnvironment.getApplication(); index=new LocalMediaIndex(app); owner=new Inspeccion(); album=owner.getFotosGenerales(); }
    @Test public void existingOrderSeedsThenCapturesAppendAndRemovedPhotosCannotReturn() throws Exception {
        album.agregarFoto("content://photos/2"); album.agregarFoto("content://photos/1");
        index.addVideo(owner.getPhotoId(),album,"content://videos/1",1000);
        album.agregarFoto("content://photos/3");
        assertEquals(Arrays.asList("content://photos/2","content://photos/1","content://videos/1","content://photos/3"), refs());
        album.removePhotoReference("content://photos/1"); assertEquals(3,refs().size()); assertFalse(refs().contains("content://photos/1"));
        index.addVideo(owner.getPhotoId(),album,"content://videos/1",1000); assertEquals(3,refs().size());
    }
    @Test public void unsavedAlbumHasIndependentIdentityAndUnavailableVideosRetainPosition() throws Exception {
        Album editing=new Album(); index.addVideo(owner.getPhotoId(),editing,"content://missing/9",2000);
        assertTrue(index.items(owner.getPhotoId(),album).isEmpty());
        GalleryMediaItem video=index.items(owner.getPhotoId(),editing).get(0);
        assertEquals(GalleryMediaItem.Kind.VIDEO,video.kind); assertEquals(2000,video.durationMillis);
        index.remove(owner.getPhotoId(),editing.getId(),video.reference); assertTrue(index.items(owner.getPhotoId(),editing).isEmpty());
    }
    @Test public void videoMetadataNeverChangesSerializedInspectionOrPhotoUploadInputs() throws Exception {
        album.agregarFoto("content://photo/1"); byte[] before=bytes(owner);
        index.addVideo(owner.getPhotoId(),album,"content://video/1",2000);
        assertArrayEquals(before,bytes(owner)); assertEquals(Collections.singletonList("content://photo/1"),album.getFotos());
        assertNull(album.recordedRemoteName("content://video/1"));
        StoredInspections.write(app,StoredInspections.DRAFTS,new ArrayList<>(Collections.singletonList(owner)));
        Inspeccion reopened=StoredInspections.read(app,StoredInspections.DRAFTS).get(0);
        assertEquals(Collections.singletonList("content://photo/1"),reopened.getFotosGenerales().getFotos());
        assertEquals(GalleryMediaItem.Kind.VIDEO,index.items(reopened.getPhotoId(),reopened.getFotosGenerales()).get(1).kind);
    }
    @Test public void failedAtomicWritePreservesLastIndexAndCorruptReadDoesNotOverwrite() throws Exception {
        index.addVideo(owner.getPhotoId(),album,"content://video/1",1000);
        LocalMediaIndex broken=new LocalMediaIndex(app) { @Override protected void write(org.json.JSONObject root) throws IOException { throw new IOException("disk full"); } };
        try { broken.addVideo(owner.getPhotoId(),album,"content://video/2",1000); fail(); } catch(IOException expected) {}
        assertEquals(Collections.singletonList("content://video/1"),refs());
        File file=app.getFileStreamPath("local-media-index.json"); try(FileOutputStream out=new FileOutputStream(file)){out.write("broken".getBytes());}
        try { refs(); fail(); } catch(IOException expected) {}
        assertEquals("broken",new String(java.nio.file.Files.readAllBytes(file.toPath())));
    }
    private List<String> refs() throws IOException { List<String> result=new ArrayList<>(); for(GalleryMediaItem item:index.items(owner.getPhotoId(),album)) result.add(item.reference); return result; }
    private static byte[] bytes(Serializable owner) throws IOException { ByteArrayOutputStream b=new ByteArrayOutputStream(); new ObjectOutputStream(b).writeObject(owner); return b.toByteArray(); }
}
