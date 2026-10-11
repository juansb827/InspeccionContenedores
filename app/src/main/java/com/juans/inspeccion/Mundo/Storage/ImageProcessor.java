package com.juans.inspeccion.Mundo.Storage;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import androidx.exifinterface.media.ExifInterface;
import java.io.*;

public final class ImageProcessor {
    public interface Source { InputStream open() throws IOException; }
    private ImageProcessor() {}

    public static Bitmap decode(Source source, int width, int height) throws IOException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        try (InputStream in = source.open()) { BitmapFactory.decodeStream(in, null, options); }
        if (options.outWidth <= 0 || options.outHeight <= 0) throw new IOException("La foto está vacía o dañada");
        int orientation;
        try (InputStream in = source.open()) {
            orientation = new ExifInterface(in).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        }
        boolean swapped = orientation >= ExifInterface.ORIENTATION_TRANSPOSE && orientation <= ExifInterface.ORIENTATION_ROTATE_270;
        int decodedWidth = swapped ? options.outHeight : options.outWidth;
        int decodedHeight = swapped ? options.outWidth : options.outHeight;
        options.inSampleSize = 1;
        while (decodedWidth / (options.inSampleSize * 2) >= width
                && (height == Integer.MAX_VALUE || decodedHeight / (options.inSampleSize * 2) >= height)) options.inSampleSize *= 2;
        options.inJustDecodeBounds = false;
        Bitmap bitmap;
        try (InputStream in = source.open()) { bitmap = BitmapFactory.decodeStream(in, null, options); }
        if (bitmap == null) throw new IOException("No se pudo decodificar la foto");
        Matrix matrix = orientationMatrix(orientation);
        Bitmap rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        if (rotated != bitmap) bitmap.recycle();
        float scale = Math.min(1f, Math.min((float) width / rotated.getWidth(), (float) height / rotated.getHeight()));
        if (scale == 1f) return rotated;
        Bitmap resized = Bitmap.createScaledBitmap(rotated, Math.max(1, Math.round(rotated.getWidth() * scale)),
                Math.max(1, Math.round(rotated.getHeight() * scale)), true);
        if (resized != rotated) rotated.recycle();
        return resized;
    }

    static Matrix orientationMatrix(int orientation) {
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL: matrix.setScale(-1, 1); break;
            case ExifInterface.ORIENTATION_ROTATE_180: matrix.setRotate(180); break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL: matrix.setScale(1, -1); break;
            case ExifInterface.ORIENTATION_TRANSPOSE: matrix.setRotate(90); matrix.postScale(-1, 1); break;
            case ExifInterface.ORIENTATION_ROTATE_90: matrix.setRotate(90); break;
            case ExifInterface.ORIENTATION_TRANSVERSE: matrix.setRotate(-90); matrix.postScale(-1, 1); break;
            case ExifInterface.ORIENTATION_ROTATE_270: matrix.setRotate(-90); break;
            default: break;
        }
        return matrix;
    }

    public static File process(File original) throws IOException {
        Bitmap bitmap = decode(() -> new FileInputStream(original), 1280, Integer.MAX_VALUE);
        File result = File.createTempFile("processed-", ".jpg", original.getParentFile());
        boolean success = false;
        try (FileOutputStream out = new FileOutputStream(result)) {
            if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)) throw new IOException("No se pudo comprimir la foto");
            out.flush();
            out.getFD().sync();
            success = true;
            return result;
        } finally {
            bitmap.recycle();
            if (!success) result.delete();
        }
    }
}
