package com.juans.inspeccion.Mundo.Storage;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import androidx.exifinterface.media.ExifInterface;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import java.io.*;
import static org.junit.Assert.*;

@org.robolectric.annotation.GraphicsMode(org.robolectric.annotation.GraphicsMode.Mode.NATIVE)
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, application = android.app.Application.class)
public class ImageProcessorTest {
    @Test public void corruptPhotoDoesNotDeleteOriginal() throws Exception {
        File source = File.createTempFile("bad-photo", ".jpg", RuntimeEnvironment.getApplication().getFilesDir());
        try (FileOutputStream out = new FileOutputStream(source)) { out.write(new byte[]{1, 2, 3}); }
        try { ImageProcessor.process(source); fail(); } catch (IOException expected) { assertEquals(3, source.length()); }
    }
    @Test public void smallPhotosAreNotUpscaled() throws Exception {
        File source = picture(200, 100, ExifInterface.ORIENTATION_NORMAL);
        Bitmap bitmap = ImageProcessor.decode(() -> new FileInputStream(source), 1280, Integer.MAX_VALUE);
        assertEquals(200, bitmap.getWidth()); assertEquals(100, bitmap.getHeight());
        File processed = ImageProcessor.process(source);
        assertTrue(source.exists()); assertTrue(processed.length() > 0);
    }
    @Test public void rotationIsAppliedBeforeWidthLimit() throws Exception {
        File source = picture(240, 120, ExifInterface.ORIENTATION_ROTATE_90);
        Bitmap bitmap = ImageProcessor.decode(() -> new FileInputStream(source), 1280, Integer.MAX_VALUE);
        assertEquals(120, bitmap.getWidth()); assertEquals(240, bitmap.getHeight());
    }
    @Test public void everyExifOrientationHasCorrectRotationAndMirroring() {
        for (int orientation = 1; orientation <= 8; orientation++) {
            Matrix m = ImageProcessor.orientationMatrix(orientation);
            float[] vectors = {1, 0, 0, 1}; m.mapVectors(vectors);
            float determinant = vectors[0] * vectors[3] - vectors[1] * vectors[2];
            boolean mirrored = orientation == 2 || orientation == 4 || orientation == 5 || orientation == 7;
            assertEquals(mirrored ? -1f : 1f, determinant, 0.001f);
            assertEquals(orientation >= 5 ? 0f : 1f, Math.abs(vectors[0]), 0.001f);
        }
    }
    private File picture(int w, int h, int orientation) throws Exception {
        File source = File.createTempFile("photo", ".jpg", RuntimeEnvironment.getApplication().getFilesDir());
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888); bitmap.eraseColor(Color.RED);
        try (FileOutputStream out = new FileOutputStream(source)) { assertTrue(bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)); }
        ExifInterface exif = new ExifInterface(source);
        exif.setAttribute(ExifInterface.TAG_ORIENTATION, Integer.toString(orientation)); exif.saveAttributes();
        return source;
    }
}
