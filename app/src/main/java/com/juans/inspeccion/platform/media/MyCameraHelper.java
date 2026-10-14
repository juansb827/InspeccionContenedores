package com.juans.inspeccion.platform.media;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;





/**
 * Created by Juan on 03/05/2015.
 */
public class MyCameraHelper {



    public static Bitmap getResizedWidth(Bitmap bm, int newWitdh) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scale = Math.min(1f, ((float) newWitdh) / width);
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scale, scale);

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
