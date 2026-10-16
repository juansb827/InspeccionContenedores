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
        assertTrue(uri.contains("/external_primary/"));
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
    @Test public void capturePublishesReleasedNameWithoutCaptureUuid() throws Exception {
        android.graphics.Bitmap bitmap = android.graphics.Bitmap.createBitmap(8, 8, android.graphics.Bitmap.Config.ARGB_8888);
        File capture = store.createCapture("12345678-1234-1234-1234-123456789012");
        try (OutputStream out = new FileOutputStream(capture)) { bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out); }
        bitmap.recycle();
        String uri = store.publish(capture, "inspection", "ENTRADA_EP_123_ABCD_0.jpg", "12345678-1234-1234-1234-123456789012");
        assertEquals("ENTRADA_EP_123_ABCD_0.jpg", store.displayName(uri));
        assertEquals(1, provider.rows.size());
    }
    @Test public void collisionsAdvancePhotoNumberAndPreserveExistingBytes() throws Exception {
        String first = store.importPhoto(new ByteArrayInputStream(new byte[]{1}), "inspection", "ENTRADA_EP_123_ABCD_0.jpg", "first");
        String second = store.importPhoto(new ByteArrayInputStream(new byte[]{2}), "inspection", "ENTRADA_EP_123_ABCD_0.jpg", "second");
        String third = store.importPhoto(new ByteArrayInputStream(new byte[]{3}), "inspection", "ENTRADA_EP_123_ABCD_0.jpg", "third");
        assertEquals("ENTRADA_EP_123_ABCD_0.jpg", store.displayName(first));
        assertEquals("ENTRADA_EP_123_ABCD_1.jpg", store.displayName(second));
        assertEquals("ENTRADA_EP_123_ABCD_2.jpg", store.displayName(third));
        try (InputStream in = store.open(first)) { assertEquals(1, in.read()); }
        try (InputStream in = store.open(second)) { assertEquals(2, in.read()); }
        try (InputStream in = store.open(third)) { assertEquals(3, in.read()); }
    }
    @Test public void damageCollisionsAdvanceOnlyPhotoNumberAndFoldersAreIndependent() throws Exception {
        String name = "ENTRADA_EP_123_ABCD_DANIO#2_0.jpg";
        String first = store.importPhoto(new ByteArrayInputStream(new byte[]{1}), "inspection", name, "first");
        String second = store.importPhoto(new ByteArrayInputStream(new byte[]{2}), "inspection", name, "second");
        String other = store.importPhoto(new ByteArrayInputStream(new byte[]{3}), "other-inspection", name, "other");
        assertEquals(name, store.displayName(first));
        assertEquals("ENTRADA_EP_123_ABCD_DANIO#2_1.jpg", store.displayName(second));
        assertEquals(name, store.displayName(other));
    }
    @Test public void collidingFailedCopyRetriesItsReservedRowAfterRecreation() throws Exception {
        String name = "ENTRADA_EP_123_ABCD_0.jpg";
        String first = store.importPhoto(new ByteArrayInputStream(new byte[]{1}), "inspection", name, "first");
        InputStream bad = new InputStream() { public int read() throws IOException { throw new IOException("storage failure"); } };
        try { store.importPhoto(bad, "inspection", name, "second"); fail(); } catch (IOException expected) { }
        assertEquals("ENTRADA_EP_123_ABCD_1.jpg", provider.rows.get(2L).getAsString(MediaStore.Images.Media.DISPLAY_NAME));
        String second = new MediaPhotoStore(context).importPhoto(new ByteArrayInputStream(new byte[]{2}), "inspection", name, "second");
        assertEquals(2, provider.rows.size());
        assertEquals("ENTRADA_EP_123_ABCD_1.jpg", store.displayName(second));
        try (InputStream in = store.open(first)) { assertEquals(1, in.read()); }
    }
    @Test public void unjournaledReservationIsRecoveredWithoutUsingUnrelatedNamedPhoto() throws Exception {
        String first = store.importPhoto(new ByteArrayInputStream(new byte[]{1}), "inspection", "photo_0.jpg", "first");
        ContentValues pending = new ContentValues();
        pending.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Inspecciones/inspection/");
        pending.put(MediaStore.Images.Media.DISPLAY_NAME, "pending-interrupted.jpg");
        pending.put(MediaStore.Images.Media.IS_PENDING, 1);
        Uri reserved = provider.insert(MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY), pending);
        String recovered = new MediaPhotoStore(context).importPhoto(new ByteArrayInputStream(new byte[]{2}), "inspection", "photo_0.jpg", "interrupted");
        assertEquals(reserved.toString(), recovered);
        assertEquals("photo_1.jpg", store.displayName(recovered));
        assertEquals(2, provider.rows.size());
        try (InputStream in = store.open(first)) { assertEquals(1, in.read()); }
    }
    @Test public void submissionRenameAvoidsCollisionsAndIsIdempotent() throws Exception {
        String existing = store.importPhoto(new ByteArrayInputStream(new byte[]{1}), "inspection", "ENTRADA_EP_123_ABCD_0.jpg", "first");
        String temporary = store.importPhoto(new ByteArrayInputStream(new byte[]{2}), "inspection", "ENTRADA_EP_temp_ABCD_0.jpg", "second");
        store.rename(temporary, "ENTRADA_EP_123_ABCD_0.jpg");
        assertEquals("ENTRADA_EP_123_ABCD_1.jpg", store.displayName(temporary));
        new MediaPhotoStore(context).rename(temporary, "ENTRADA_EP_123_ABCD_0.jpg");
        assertEquals("ENTRADA_EP_123_ABCD_1.jpg", store.displayName(temporary));
        assertEquals("ENTRADA_EP_123_ABCD_0.jpg", store.displayName(existing));
        assertEquals(2, provider.rows.size());
        try (InputStream in = store.open(existing)) { assertEquals(1, in.read()); }
        try (InputStream in = store.open(temporary)) { assertEquals(2, in.read()); }
    }
    @Test public void pendingPhotoAlsoReservesItsVisibleNumber() throws Exception {
        InputStream bad = new InputStream() { public int read() throws IOException { throw new IOException("storage failure"); } };
        try { store.importPhoto(bad, "inspection", "photo_0.jpg", "first"); fail(); } catch (IOException expected) { }
        String second = store.importPhoto(new ByteArrayInputStream(new byte[]{2}), "inspection", "photo_0.jpg", "second");
        assertEquals("photo_1.jpg", store.displayName(second));
        assertEquals(Integer.valueOf(1), provider.rows.get(1L).getAsInteger(MediaStore.Images.Media.IS_PENDING));
        assertEquals("photo_0.jpg", provider.rows.get(1L).getAsString(MediaStore.Images.Media.DISPLAY_NAME));
    }
    @Test public void completedPublicationWithOldUuidNameIsStillReusable() throws Exception {
        String original = store.importPhoto(new ByteArrayInputStream(new byte[]{1}), "inspection", "photo_0-old-capture-id.jpg", "old-capture");
        String retried = new MediaPhotoStore(context).importPhoto(new ByteArrayInputStream(new byte[]{2}), "inspection", "photo_0.jpg", "old-capture");
        assertEquals(original, retried);
        assertEquals("photo_0-old-capture-id.jpg", store.displayName(retried));
        assertEquals(1, provider.rows.size());
        try (InputStream in = store.open(original)) { assertEquals(1, in.read()); }
    }
    @Test public void unavailableCollisionQueryFailsWithoutTouchingExistingPhoto() throws Exception {
        String first = store.importPhoto(new ByteArrayInputStream(new byte[]{1}), "inspection", "photo_0.jpg", "first");
        provider.failQueries = true;
        try { store.importPhoto(new ByteArrayInputStream(new byte[]{2}), "inspection", "photo_0.jpg", "second"); fail(); }
        catch (IOException expected) { assertEquals(1, provider.rows.size()); }
        try (InputStream in = store.open(first)) { assertEquals(1, in.read()); }
    }
    @Test @Config(sdk = 28) public void oldAndroidRequiresStorageGrant() throws Exception {
        assertTrue(MediaPhotoStore.needsStoragePermission(context));
        try { store.importPhoto(new ByteArrayInputStream(new byte[]{1}), "inspection", "photo.jpg", "denied"); fail(); }
        catch (IOException expected) { assertTrue(provider.rows.isEmpty()); }
    }
    @Test @Config(sdk = 28) public void oldAndroidUsesPublicPicturesAfterStorageGrant() throws Exception {
        org.robolectric.Shadows.shadowOf((android.app.Application) context).grantPermissions(MediaPhotoStore.storagePermissions());
        String uri = store.importPhoto(new ByteArrayInputStream(new byte[]{1}), "inspection", "photo.jpg", "old");
        assertTrue(uri.startsWith("content://media/"));
        assertTrue(provider.rows.get(1L).getAsString(MediaStore.Images.Media.DATA).contains("Pictures/Inspecciones/inspection/photo.jpg"));
        assertFalse(provider.rows.get(1L).containsKey(MediaStore.Images.Media.IS_PENDING));
        try (InputStream in = store.open(uri)) { assertEquals(1, in.read()); }
    }
    @Test @Config(sdk = 28) public void historicalPublicFileCollisionDoesNotReplaceOriginal() throws Exception {
        org.robolectric.Shadows.shadowOf((android.app.Application) context).grantPermissions(MediaPhotoStore.storagePermissions());
        String first = store.importPhoto(new ByteArrayInputStream(new byte[]{1}), "inspection", "photo_0.jpg", "first");
        String second = store.importPhoto(new ByteArrayInputStream(new byte[]{2}), "inspection", "photo_0.jpg", "second");
        assertEquals("photo_0.jpg", store.displayName(first));
        assertEquals("photo_1.jpg", store.displayName(second));
        try (InputStream in = store.open(first)) { assertEquals(1, in.read()); }
        try (InputStream in = store.open(second)) { assertEquals(2, in.read()); }
    }
    @Test public void failedExplicitDeletionKeepsAlbumReference() throws Exception {
        com.juans.inspeccion.Mundo.Album album = new com.juans.inspeccion.Mundo.Album();
        album.agregarFoto("content://media/external/images/media/99");
        try { album.deletePhoto(context, 0); fail(); } catch (IOException expected) { }
        assertEquals(1, album.getFotos().size());
    }
    @Test @Config(sdk = 28) public void writeOnlyGrantDoesNotPretendAlbumReadsArePermitted() throws Exception {
        org.robolectric.Shadows.shadowOf((android.app.Application) context).grantPermissions(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        assertTrue(MediaPhotoStore.needsStoragePermission(context));
        try { store.importPhoto(new ByteArrayInputStream(new byte[]{1}), "inspection", "photo.jpg", "partial-grant"); fail(); }
        catch (IOException expected) { assertTrue(provider.rows.isEmpty()); }
    }
    public static class MediaProvider extends ContentProvider {
        final Map<Long, ContentValues> rows = new LinkedHashMap<>();
        final Map<Long, File> files = new HashMap<>();
        boolean failQueries;
        public boolean onCreate() { return true; }
        public String getType(Uri uri) { return "image/jpeg"; }
        public Uri insert(Uri uri, ContentValues values) {
            if (android.os.Build.VERSION.SDK_INT >= 29 && "external".equals(uri.getPathSegments().get(0)))
                throw new IllegalArgumentException("Synthetic external volume is read-only");
            long id = rows.size() + 1; rows.put(id, new ContentValues(values));
            String data = values.getAsString(MediaStore.Images.Media.DATA);
            File file = data == null ? new File(getContext().getFilesDir(), "media-" + id) : new File(data);
            if (data != null) assertTrue("Public photo must be fully written before registration", file.length() > 0);
            files.put(id, file);
            return Uri.withAppendedPath(uri, Long.toString(id));
        }
        public Cursor query(Uri uri, String[] projection, String selection, String[] args, String sort) {
            if (failQueries) return null;
            MatrixCursor cursor = new MatrixCursor(projection);
            for (Map.Entry<Long, ContentValues> entry : rows.entrySet()) {
                if (Integer.valueOf(1).equals(entry.getValue().getAsInteger(MediaStore.Images.Media.IS_PENDING))
                        && !"1".equals(uri.getQueryParameter("includePending"))) continue;
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
