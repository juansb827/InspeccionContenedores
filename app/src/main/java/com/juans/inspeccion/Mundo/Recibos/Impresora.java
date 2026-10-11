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
    public static final java.util.concurrent.ExecutorService IO = java.util.concurrent.Executors.newSingleThreadExecutor();

    private final String ARCHIVO_IMPRESORA="printerSettings";
    public final static int OK=0;
    public final static int ERROR_PRINTER_NOT_SET=1;
    public final static int ERROR_AL_CONECTAR=2;




    private String nombreImpresora;


    private String macAdress;

    private volatile boolean pudoConectar;
    interface Transport {
        boolean open(String address);
        boolean command(String command);
        boolean image(int x, int y, android.graphics.Bitmap bitmap);
        void close() throws IOException;
    }
    private final Transport transport;
    private final PrinterAccess.Gateway access;

    public static synchronized Impresora getInstance()
    {
        if(instance==null)
        {
            instance=new Impresora();
        }
        return instance;
    }

    private Impresora() {
        this(new Transport() {
            public boolean open(String address) { return Godex.open(address, 2); }
            public boolean command(String command) { return Godex.sendCommand(command); }
            public boolean image(int x, int y, android.graphics.Bitmap bitmap) { return Godex.putimage(x,y,bitmap); }
            public void close() throws IOException { Godex.close(); }
        }, PrinterAccess::isReady);
    }
    Impresora(Transport transport, PrinterAccess.Gateway access) { this.transport = transport; this.access = access; }

    public boolean conectado()
    {
        return pudoConectar;
    }

    public synchronized int conectar(Context context) {
        pudoConectar = false;
        if (!access.ready(context)) { desconectar(); return ERROR_AL_CONECTAR; }
        if (macAdress == null || !android.bluetooth.BluetoothAdapter.checkBluetoothAddress(macAdress)) return ERROR_PRINTER_NOT_SET;
        try { pudoConectar = transport.open(macAdress); }
        catch (RuntimeException e) { pudoConectar = false; }
        if (!pudoConectar) { desconectar(); return ERROR_AL_CONECTAR; }
        return OK;
    }

    public synchronized void desconectar() {
        pudoConectar = false;
        try { transport.close(); } catch (IOException | RuntimeException e) { Log.w("Impresora", "No se pudo cerrar la conexión", e); }
    }

    public synchronized void enviarComandosImpresora(ArrayList<Comando> commands, Context context) throws IOException {
        try {
            for (Comando command : commands) {
                // Check every command: permissions or adapter state can change during a receipt.
                if (!pudoConectar || !access.ready(context)) throw new IOException("La impresora no está conectada o se revocó el permiso");
                boolean sent;
                if (command.getTipo() == ComandosImpresora.IMG) {
                    String[] parameters = command.getComando().split(",");
                    sent = transport.image(Integer.parseInt(parameters[0]), Integer.parseInt(parameters[1]), command.darBitMap());
                } else sent = transport.command(command.getComando());
                if (!sent) throw new IOException("No se pudo enviar el recibo a la impresora");
            }
        } catch (IOException | RuntimeException e) { desconectar(); throw e; }
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
