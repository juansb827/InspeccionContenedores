package com.juans.inspeccion.platform.media;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

/** Stable Android entry point for the retired upload service. Never reads extras or opens FTP. */
public class SimpleService extends Service {
    public static String PARAMS="PARAMS", INSPECCION="INSPECCION";
    @Override public IBinder onBind(Intent intent) { return null; }
    @Override public int onStartCommand(Intent intent,int flags,int startId) {
        Toast.makeText(this,"La cola de fotos está retirada; no se envían archivos.",Toast.LENGTH_LONG).show();
        stopSelf(); return START_NOT_STICKY;
    }
}
