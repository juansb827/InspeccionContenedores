package com.juans.inspeccion.Other;


import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import com.juans.inspeccion.Interfaz.InspeccionActivity;
import com.juans.inspeccion.Mundo.Album;
import com.juans.inspeccion.Mundo.FotosPendientes;
import com.juans.inspeccion.Mundo.Inspeccion;
import com.juans.inspeccion.Mundo.Util.FTPUpload;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Juan on 04/05/2015.
 */
public class SimpleService extends Service {

    private HashMap<String, String> connectionParams;
    public static String PARAMS = "PARAMS";
    public static String INSPECCION = "INSPECCION";
    private FotosPendientes fotosPendientes;
    private MyThread myythread;

    public boolean isRunning = false;
    long interval = 5 * 1000;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        fotosPendientes = new FotosPendientes();
       super.onCreate();
        myythread = new MyThread(interval);
    }

    @Override
    public synchronized void onDestroy() {
        super.onDestroy();
        Log.e("Service","Destroyed");
        if (!isRunning) {
            myythread.interrupt();


        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("Service","onstartCommand"+intent);
        //intent es null cuando  es activado por el network receiver

        if (connectionParams == null) {
            connectionParams = (HashMap<String, String>) intent.getSerializableExtra(PARAMS);
        }
        Inspeccion fotos = (Inspeccion) intent.getSerializableExtra(INSPECCION);
        if(fotos!=null ) {
            fotosPendientes.agregarFotos(fotos);
        }
        if (!isRunning) {
            myythread.start();
            isRunning = true;
        }

        return super.onStartCommand(intent, flags, startId);

    }

    private void parar()
    {
        isRunning=false;
    }
    class MyThread extends Thread {
        long interval;

        public MyThread(long interval) {
            this.interval = interval;
        }

        @Override
        public void run() {
            while (isRunning) {
                Log.e("Service:", "running");
                try {

                    if (fotosPendientes.hayPendientes() &&NetWorkChangeReceiver.checkConnectionToFTP()  ) {
                        if (!fileUpload()) {
                            Log.e("Service:", "failed to upload files, retrying in 5seconds");
                            Thread.sleep(interval);

                        } else parar();

                    } else {
                        Log.e("FotosPendientes",fotosPendientes.hayPendientes()+"");
                        parar();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            stopSelf();
        }

        private boolean fileUpload() {

            try {


                FTPUpload ftpUpload = new FTPUpload("", 0, "", "");
                ftpUpload.conect();
                while (fotosPendientes.hayPendientes()) {
                    boolean subio = fotosPendientes.subirFotosServidor(ftpUpload);
                    if (!subio) return false;
                }


            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;

        }

    }
}

