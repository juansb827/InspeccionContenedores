package com.juans.inspeccion.Mundo;

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
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
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

    public static boolean openCamera(Fragment c,String fileName,String folder,String subFolder)
    {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(c.getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile(fileName,folder,subFolder);
            } catch (IOException ex) {
                // Error occurred while creating the File

                ex.printStackTrace();
                return false;
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));

                filePath=photoFile.getPath();
                if(filePath==null) return false;
                c.startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);

                return true;
            }
        }
        filePath=null;
        return false;
    }

    public static boolean openCamera(Activity c,String fileName,String folder,String subFolder)
    {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(c.getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile(fileName,folder,subFolder);
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                 Uri.fromFile(photoFile));

                 filePath=photoFile.getPath();
                if(filePath==null) return false;
                c.startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);

                return true;
            }
        }
        filePath=null;
        return false;
    }

    public static String getFilePath() {
        return filePath;
    }

    /*Folder-Carpeta donde van a ir todas las fotos que guarda la aplicacion
        * subFolder-Carpeta donde van las fotos de una inspeccion-Sera eliminada despues de ser enviadas
        * */
    public static File createImageFile(String fileName,String Folder,String subFolder) throws IOException {
        // Create an image file name

        String storageDir = Environment.getExternalStorageDirectory() + "/"+Folder+"/"+subFolder;
        //String storageDir=Environment.getExternalStorageDirectory()+"/Inspe";
        File dir = new File(storageDir);
        //The File.mkdirs() method will create all needed directories; mkdir() will only create the last directory in the pathname.
        if (!dir.exists()) dir.mkdirs();
        File image = new File(storageDir + "/" + fileName + ".jpg");
        // Save a file: path for use with ACTION_VIEW intents

        return image;
    }


    public static void reducirCalidad(String imgPath) throws Exception {
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
