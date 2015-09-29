package com.juans.inspeccion;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import com.juans.inspeccion.CustomView.MyEditText;
import com.juans.inspeccion.Mundo.Consultas;
import com.juans.inspeccion.R;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;
import java.io.InputStream;
import java.util.List;

/**
 * Created by juan__000 on 10/16/2014.
 */
public class Varios {


   public static void abrirExcel(Context c, File file)
   {
       PackageManager packageManager = c.getPackageManager();
       Intent testIntent = new Intent(Intent.ACTION_VIEW);
       testIntent.setType("application/vnd.ms-excel");
       List list = packageManager.queryIntentActivities(testIntent, PackageManager.MATCH_DEFAULT_ONLY);
       Intent intent = new Intent();
       intent.setAction(Intent.ACTION_VIEW);
       Uri uri = Uri.fromFile(file);
       intent.setDataAndType(uri, "application/vnd.ms-excel");
       c.startActivity(intent);
   }

    public static void abrirPDF(Context c, File file)
    {
        PackageManager packageManager = c.getPackageManager();
        Intent testIntent = new Intent(Intent.ACTION_VIEW);
        testIntent.setType("application/application/pdf");
        List list = packageManager.queryIntentActivities(testIntent, PackageManager.MATCH_DEFAULT_ONLY);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(file);
        intent.setDataAndType(uri, "application/pdf");
        c.startActivity(intent);
    }
    public static float floatCon2Decimales(float f)
    {
        return Float.parseFloat(String.format("%.2f", f));
    }

    public static String eliminarDecimales(String string)
    {
       String resp="";
        try {
           resp=string.replaceAll("\\.0*$", "");


        }catch (Exception e)
        {
            e.printStackTrace();
        }
        return resp;

    }



    public static String   fechaDAOtoString(String[] fechaDB)
    {
        String horaConMinutos = fechaDB[Consultas.FECHA_HORA] + ":" + fechaDB[Consultas.FECHA_MINUTOS];
        String fechaEntera = fechaDB[Consultas.FECHA_ANIO] + fechaDB[Consultas.FECHA_MES] + fechaDB[Consultas.FECHA_DIA] + " " + horaConMinutos;
        return fechaEntera;
    }

    public static String   fechaDAOtoHora(String[] fechaDB)
    {
        String horaConMinutos = fechaDB[Consultas.FECHA_HORA] + ":" + fechaDB[Consultas.FECHA_MINUTOS];

        return horaConMinutos;
    }
    public static float parseFloat(String string)
    {
        float res=0;

        if(!string.trim().equals(""))
        {
            String nstring=string;
//                if(string.contains(","))
//                {
//                    nstring=string.replace(",",".");
//
//                }

            res=Float.parseFloat(nstring);

//            res= Float.parseFloat(String.format("%.2f", res));



        }
        return  res;
    }

    public static String agregarCeros(String numero,int n)
    {
        int num;
        try
        {
           num =Integer.parseInt(numero);
        }
        catch(Exception e)
        {
            num=0;
        }

        return String.format("%0"+n+"d", num);
    }




    public static void onBackPressed(final Activity activity,String titulo,String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(titulo);
        builder.setMessage(msg);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(activity!=null) {
                    activity.finish();
                }
            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }

    public static void lockRotation(Resources r, Activity activity)
    {
      if(activity!=null && !activity.isFinishing()) {
          if (r.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
              activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
          } else {
              activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
          }
      }

    }

    public static void unlockRotation(Activity activity)
    {
        if (activity!=null && !activity.isFinishing())
        {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        }
    }


    public static boolean isActityEnding(Activity activity)
    {
        return activity==null || activity.isFinishing() ;
    }

    public static Bitmap downloadBitmap(String url) {
        // initilize the default HTTP client object
        final DefaultHttpClient client = new DefaultHttpClient();

        //forming a HttoGet request
        final HttpGet getRequest = new HttpGet(url);
        try {

            HttpResponse response = client.execute(getRequest);

            //check 200 OK for success
            final int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode != HttpStatus.SC_OK) {
                Log.w("ImageDownloader", "Error " + statusCode +
                        " while retrieving bitmap from " + url);
                return null;

            }

            final HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream inputStream = null;
                try {
                    // getting contents from the stream
                    inputStream = entity.getContent();

                    // decoding stream data back into image Bitmap that android understands
                    final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                    return bitmap;
                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    entity.consumeContent();
                }
            }
        } catch (Exception e) {
            // You Could provide a more explicit error message for IOException
            getRequest.abort();

            Log.e("ImageDownloader", "Something went wrong while" +
                    " retrieving bitmap from " + url + e.toString());

        }

        return null;
    }





}
