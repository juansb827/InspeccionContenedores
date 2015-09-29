package com.juans.inspeccion.Mundo.Recibos;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.Log;

import com.godex.Godex;
import com.juans.inspeccion.Mundo.Comando;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Juan on 05/03/2015.
 */
public class Impresora {
    private static Impresora instance=null;

    private final String ARCHIVO_IMPRESORA="printerSettings";
    public final static int OK=0;
    public final static int ERROR_PRINTER_NOT_SET=1;
    public final static int ERROR_AL_CONECTAR=2;




    private String nombreImpresora;


    private String macAdress;

    private boolean pudoConectar;

    public static synchronized Impresora getInstance()
    {
        if(instance==null)
        {
            instance=new Impresora();
        }
        return instance;
    }

    private Impresora()
    {

    }

    public boolean conectado()
    {
        return pudoConectar;
    }

    public int conectar()
    {
        if(macAdress==null)
        {
            Log.e("Imoresora","macAdress null");
            return ERROR_PRINTER_NOT_SET;
        }
        pudoConectar= Godex.open(macAdress, 2);
        if(!pudoConectar)
        {
            Log.e("Impresora","Error conexion");
            return ERROR_AL_CONECTAR;
        }

        else {
            Log.e("Conecto","Impresora");
            return OK;
        }

    }

    public void desconectar()
    {
        synchronized (this) {
            pudoConectar = false;
            try {

                Godex.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    public  void enviarComandosImpresora(ArrayList<Comando> comandos,Resources res)
    {


        for(Comando comando:comandos)
        {
            if(comando.getTipo()== ComandosImpresora.IMG)
            {
                String[] parametros=comando.getComando().split(",");
                int x=Integer.parseInt(parametros[0]);
                int y=Integer.parseInt(parametros[1]);

              Godex.putimage(x,y,comando.darBitMap());



            }
            else {
                Godex.sendCommand(comando.getComando());
            }

        }


    }



    public void guardarDatosImpresora(Context c)
    {
        SharedPreferences sharedPref = c.getSharedPreferences(ARCHIVO_IMPRESORA, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("nombre", nombreImpresora);
        editor.putString("mac", macAdress);

        editor.commit();
    }

    public boolean cargarDatosImpresora(Context c)
    {
        try {
            SharedPreferences sharedPref = c.getSharedPreferences(ARCHIVO_IMPRESORA, Context.MODE_PRIVATE);
            nombreImpresora = sharedPref.getString("nombre", null);
            macAdress = sharedPref.getString("mac", null);

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
         return !(nombreImpresora==null && macAdress==null);

    }


    public String getNombreImpresora() {
        return nombreImpresora;
    }

    public void setNombreImpresora(String nombreImpresora) {
        this.nombreImpresora = nombreImpresora;
    }

    public String getMacAdress() {
        return macAdress;
    }

    public void setMacAdress(String macAdress) {
        this.macAdress = macAdress;
    }

}
