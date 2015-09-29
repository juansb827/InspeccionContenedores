package com.juans.inspeccion.Other;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;

import org.apache.http.client.methods.HttpGet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Observable;

public class NetWorkChangeReceiver extends BroadcastReceiver  {
    public NetWorkChangeReceiver() {
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        final ConnectivityManager connMgr = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        final android.net.NetworkInfo wifi = connMgr
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        final android.net.NetworkInfo mobile = connMgr
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        final BroadcastObserver bco = BroadcastObserver.instance();
//        if (wifi.isAvailable() || mobile.isAvailable()) {
//            // Do something
//
//            Thread thread=new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        boolean ftp=checkConnectionToFTP();
//                        boolean database=checkConnectionToDB();
//                        Log.e("LEPING", ""+ftp);
//                        if(ftp) {
//                            Log.e("NetWorkReceiver", "startService");
//                            Intent intent = new Intent(context, SimpleService.class);
//                            context.startService(intent);
//                        }
//                        if(database)
//                        {
//
//                            bco.change(database);
//                        }
//
//
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            });
//            thread.start();
//        }
//        else bco.change(false);
    }

    public static boolean checkConnectionToFTP() throws Exception {
        return NetWorkChangeReceiver.ping2("70.38.10.203");
    }

    public static boolean checkConnectionToDB() throws Exception {
        return NetWorkChangeReceiver.ping2("70.38.10.202");
    }
    //WORKS!!! :D
    public static boolean ping2(String laip) throws Exception {
        int timeout = 3000;
        InetAddress address = InetAddress.getByName(laip);

            if (address.isReachable(timeout))
            {      Log.e("Ping ","%s is reachable%n "+address); return true;}
            else
                Log.e("Ping ","%s could not be contacted%n"+address);
        return false;

    }

    public static String ping(String url) {
        String str = "";
        try {
            Process process = Runtime.getRuntime().exec(
                    "/system/bin/ping -c 4 " + url);
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    process.getInputStream()));
            int i;
            char[] buffer = new char[4096];
            StringBuffer output = new StringBuffer();
            while ((i = reader.read(buffer)) > 0)
                output.append(buffer, 0, i);
            reader.close();

            // body.append(output.toString()+"\n");
            str = output.toString();
             Log.e("Leping", str);
        } catch (IOException e) {
            // body.append("Error\n");
            e.printStackTrace();
        }
        return str;
    }


}
