package com.juans.inspeccion.Mundo;

import android.Manifest;
import android.app.Activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import androidx.fragment.app.Fragment;
import android.util.Log;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;



/**
 * Created by Juan on 03/05/2015.
 */
public class MyCameraHelper {



    public static final int REQUEST_TAKE_PHOTO = 364;
    public static final String FILE_PATH="FILE_PATH";
    private static String filePath;

    /**
     * Prepares the camera intent and returns the Uri for the output file.
     * Permission and intent launching should be handled in the Activity/Fragment using ActivityResultContract.
     */
    public static CameraIntentResult buildCameraIntent(Context context, String fileName, String folder, String subFolder) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(context.getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile(context, fileName, folder, subFolder);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if (photoFile != null) {
                Uri photoURI = androidx.core.content.FileProvider.getUriForFile(
                        context,
                        context.getPackageName() + ".fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                filePath = photoFile.getPath();
                StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                StrictMode.setVmPolicy(builder.build());
                return new CameraIntentResult(takePictureIntent, photoURI, filePath);
            }
        }
        filePath = null;
        return null;
    }

    public static class CameraIntentResult {
        public final Intent intent;
        public final Uri uri;
        public final String filePath;
        public CameraIntentResult(Intent intent, Uri uri, String filePath) {
            this.intent = intent;
            this.uri = uri;
            this.filePath = filePath;
        }
    }



    public static String getFilePath() {
        return filePath;
    }

    /*Folder-Carpeta donde van a ir todas las fotos que guarda la aplicacion
        * subFolder-Carpeta donde van las fotos de una inspeccion-Sera eliminada despues de ser enviadas
        * */
    public static File createImageFile(Context context, String fileName, String Folder, String subFolder) throws IOException {
        // Use app-scoped external files directory for images
        if (context == null) {
            throw new IOException("Context required for getExternalFilesDir");
        }
        File baseDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (baseDir == null) {
            throw new IOException("Unable to access external files dir");
        }
        // Create app folder inside Pictures
        File appFolder = new File(baseDir, Folder);
        if (!appFolder.exists() && !appFolder.mkdirs()) {
            throw new IOException("Failed to create app folder: " + appFolder.getAbsolutePath());
        }
        File dir = appFolder;
        if (subFolder != null && !subFolder.isEmpty()) {
            dir = new File(appFolder, subFolder);
            if (!dir.exists() && !dir.mkdirs()) {
                throw new IOException("Failed to create subfolder: " + dir.getAbsolutePath());
            }
        }
        File image = new File(dir, fileName + ".jpg");
        try {
            if (!image.exists() && !image.createNewFile()) {
                throw new IOException("Could not create new file: " + image.getAbsolutePath());
            }
            return image;
        } catch (IOException e) {
            throw new IOException("Failed to create image file: " + image.getAbsolutePath() +
                " Reason: " + e.getMessage() +
                " Dir exists: " + dir.exists() +
                " Dir writable: " + dir.canWrite());
        }
    }


    public static void reducirCalidad(String imgPath) throws Exception {
        File imgFile = new File(imgPath);
        if (!imgFile.exists()) {
            Log.e("MyCameraHelper", "File does not exist: " + imgPath);
            throw new IOException("File does not exist: " + imgPath);
        }
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imgPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;
        if(photoW>1280 ||  photoH>720) {
            Bitmap bitmap = darMiniatura(imgPath, 1280, 720, false);
            FileOutputStream fos = new FileOutputStream(imgPath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
        }

    }


    public static Bitmap darMiniatura (String fullImage,int targetW,int targetH,boolean rotar) throws Exception {
        // Get the dimensions of the View

        File imgFile = new File(fullImage);
        if (!imgFile.exists()) {
            Log.e("MyCameraHelper", "File does not exist: " + fullImage);
            throw new IOException("File does not exist: " + fullImage);
        }
        Log.e("darMiniatura",""+fullImage);
        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(fullImage, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor << 1;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(fullImage, bmOptions);

        Matrix mtx = new Matrix();
        if (rotar) mtx.postRotate(90);
        else mtx.postRotate(0);
        // Rotating Bitmap
        Bitmap rotatedBMP = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), mtx, true);

        if (rotatedBMP != bitmap)
            bitmap.recycle();

        return rotatedBMP;

    }

    public static Bitmap getResizedWidth(Bitmap bm, int newWitdh) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scale = ((float) newWitdh) / width;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scale, scale);
        matrix.postRotate(90);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        return resizedBitmap;
    }

    public static Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleHeight = ((float) newHeight) / height;
        float scaleWidth = scaleHeight;
        //determina cual escala usar
        if( (width*scaleHeight)>newWidth) scaleWidth=((float) newWidth)/width;
                // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        return resizedBitmap;
    }
    public static Bitmap reducirBitmap (Bitmap originalImage,int targetW,int targetH,boolean rotar) throws Exception {


        if(originalImage.getHeight()==targetH && originalImage.getWidth()==targetW && rotar==false) return originalImage;
        //if(targetW>originalImage.getWidth()) targetW =originalImage.getWidth();
        Bitmap background = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.ARGB_8888);

        float originalWidth = originalImage.getWidth(), originalHeight = originalImage.getHeight();
        Canvas canvas = new Canvas(background);
        float scale = targetH/originalHeight;
        float yTranslation = 0.0f, xTranslation = (targetW - originalWidth * scale)/2.0f;
        Matrix transformation = new Matrix();
        transformation.postTranslate(xTranslation, yTranslation);
        transformation.preScale(scale, scale);
        Paint paint = new Paint();
        paint.setFilterBitmap(true);
        paint.setAlpha(255);
        canvas.drawBitmap(originalImage, transformation,paint);
        return background;
//        Matrix mtx = new Matrix();
//        if (rotar) mtx.postRotate(90);
//        else mtx.postRotate(0);

//        // Rotating Bitmap
//        Bitmap rotatedBMP = Bitmap.createBitmap(fullImage, 0, 0, targetW, targetH, mtx, true);
//        String file=Environment.getExternalStorageDirectory()+"lafotosocio.jpg";
//        FileOutputStream fos = new FileOutputStream(file);
//        rotatedBMP.compress(Bitmap.CompressFormat.JPEG, 90, fos);
//
//
//        if (rotatedBMP != fullImage)
//            fullImage.recycle();
//        return rotatedBMP;

    }



}
