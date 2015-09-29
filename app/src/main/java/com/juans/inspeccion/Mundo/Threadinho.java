package com.juans.inspeccion.Mundo;

import android.util.Log;

import java.sql.Connection;

/**
 * Created by Juan on 3/2/2015.
 */
public class Threadinho extends Thread {
        
    Object object;
    Connection conn;
    String connURL;
    public Threadinho(Connection conn,String connURL){
        this.conn=conn;
        this.connURL=connURL;
        
        
    }

    public void run() {
        try {

            //conn= DriverManager.getConnection(connURL);
            this.wait();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (object==null)
        {
            Log.e("Timeoout","despues de tres seg la variable sigue null");
        }
    }
}
