package com.juans.inspeccion.Mundo.Storage;

import android.content.*;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowContentResolver;
import java.io.*;
import java.util.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 29, application = android.app.Application.class)
public class MediaPhotoStoreTest {
    private Context context;
    private MediaPhotoStore store;
    private MediaProvider provider;
    @Before public void setup() {
        context = RuntimeEnvironment.getApplication();
        provider = new MediaProvider();
        android.content.pm.ProviderInfo info = new android.content.pm.ProviderInfo();
        info.authority = "media"; info.exported = true;
        provider.attachInfo(context, info);
        ShadowContentResolver.registerProviderInternal("media", provider);
        store = new MediaPhotoStore(context);
    }
    @Test public void publicationIsPendingUntilCopyCompletesAndRetryReusesRow() throws Exception {
        String uri = store.importPhoto(new ByteArrayInputStream(new byte[]{1,2,3}), "inspection", "photo.jpg", "capture");
        assertEquals(1, provider.rows.size());
        assertEquals("Pictures/Inspecciones/inspection/", provider.rows.get(1L).getAsString(MediaStore.Images.Media.RELATIVE_PATH));
        assertEquals(Integer.valueOf(0), provider.rows.get(1L).getAsInteger(MediaStore.Images.Media.IS_PENDING));
        assertEquals(uri, new MediaPhotoStore(context).importPhoto(new ByteArrayInputStream(new byte[]{9}), "inspection", "photo.jpg", "capture"));
        assertEquals(1, provider.rows.size());
        try (InputStream in = store.open(uri)) { assertEquals(1, in.read()); }
    }
    @Test public void failedCopyRemainsPendingAndRetryDoesNotCreateDuplicate() throws Exception {
        InputStream bad = new InputStream() { public int read() throws IOException { throw new IOException("storage failure"); } };
        try { store.importPhoto(bad, "inspection", "photo.jpg", "failed"); fail(); } catch (IOException expected) { }
        assertEquals(Integer.valueOf(1), provider.rows.get(1L).getAsInteger(MediaStore.Images.Media.IS_PENDING));
        store.importPhoto(new ByteArrayInputStream(new byte[]{4}), "inspection", "photo.jpg", "failed");
        assertEquals(1, provider.rows.size());
        assertEquals(Integer.valueOf(0), provider.rows.get(1L).getAsInteger(MediaStore.Images.Media.IS_PENDING));
    }
    @Test @Config(sdk = 28) public void oldAndroidRequiresStorageGrant() throws Exception {
        assertTrue(MediaPhotoStore.needsStoragePermission(context));
        try { store.importPhoto(new ByteArrayInputStream(new byte[]{1}), "inspection", "photo.jpg", "denied"); fail(); }
        catch (IOException expected) { assertTrue(provider.rows.isEmpty()); }
    }
    @Test @Config(sdk = 28) public void oldAndroidUsesPublicPicturesAfterStorageGrant() throws Exception {
        org.robolectric.Shadows.shadowOf((android.app.Application) context).grantPermissions(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        String uri = store.importPhoto(new ByteArrayInputStream(new byte[]{1}), "inspection", "photo.jpg", "old");
        assertTrue(uri.startsWith("content://media/"));
        assertTrue(provider.rows.get(1L).getAsString(MediaStore.Images.Media.DATA).contains("Pictures/Inspecciones/inspection/photo.jpg"));
        assertFalse(provider.rows.get(1L).containsKey(MediaStore.Images.Media.IS_PENDING));
        try (InputStream in = store.open(uri)) { assertEquals(1, in.read()); }
    }
    @Test public void failedExplicitDeletionKeepsAlbumReference() throws Exception {
        com.juans.inspeccion.Mundo.Album album = new com.juans.inspeccion.Mundo.Album();
        album.agregarFoto("content://media/external/images/media/99");
        try { album.deletePhoto(context, 0); fail(); } catch (IOException expected) { }
        assertEquals(1, album.getFotos().size());
    }
    public static class MediaProvider extends ContentProvider {
        final Map<Long, ContentValues> rows = new LinkedHashMap<>();
        final Map<Long, File> files = new HashMap<>();
        public boolean onCreate() { return true; }
        public String getType(Uri uri) { return "image/jpeg"; }
        public Uri insert(Uri uri, ContentValues values) {
            long id = rows.size() + 1; rows.put(id, new ContentValues(values));
            String data = values.getAsString(MediaStore.Images.Media.DATA);
            File file = data == null ? new File(getContext().getFilesDir(), "media-" + id) : new File(data);
            if (data != null) assertTrue("Public photo must be fully written before registration", file.length() > 0);
            files.put(id, file);
            return Uri.parse("content://media/external/images/media/" + id);
        }
        public Cursor query(Uri uri, String[] projection, String selection, String[] args, String sort) {
            MatrixCursor cursor = new MatrixCursor(projection);
            for (Map.Entry<Long, ContentValues> entry : rows.entrySet()) {
                if (selection != null) {
                    String path = entry.getValue().getAsString(MediaStore.Images.Media.RELATIVE_PATH);
                    if (path == null) path = entry.getValue().getAsString(MediaStore.Images.Media.DATA);
                    if (!Objects.equals(path, args[0]) || !Objects.equals(entry.getValue().getAsString(MediaStore.Images.Media.DISPLAY_NAME), args[1])) continue;
                } else if (!uri.getLastPathSegment().equals("media") && !uri.getLastPathSegment().equals(entry.getKey().toString())) continue;
                Object[] row = new Object[projection.length];
                for (int i = 0; i < row.length; i++) row[i] = "_id".equals(projection[i]) ? entry.getKey() : entry.getValue().get(projection[i]);
                cursor.addRow(row);
            }
            return cursor;
        }
        public int update(Uri uri, ContentValues values, String selection, String[] args) {
            ContentValues row = rows.get(Long.valueOf(uri.getLastPathSegment()));
            if (row == null) return 0; row.putAll(values); return 1;
        }
        public int delete(Uri uri, String selection, String[] args) { return rows.remove(Long.valueOf(uri.getLastPathSegment())) == null ? 0 : 1; }
        public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
            return ParcelFileDescriptor.open(files.get(Long.valueOf(uri.getLastPathSegment())), ParcelFileDescriptor.parseMode(mode));
        }
    }
}
