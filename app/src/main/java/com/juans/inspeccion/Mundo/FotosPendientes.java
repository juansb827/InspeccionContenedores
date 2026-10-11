package com.juans.inspeccion.Mundo;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.juans.inspeccion.Interfaz.MainActivity;
import com.juans.inspeccion.Mundo.Util.FTPUpload;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

/**
 * Created by Juan on 05/05/2015.
 */
public class FotosPendientes {
     static ArrayList<Inspeccion> fotosPendientes;
    private final static String FILE_NAME = "fotosPendientes";

    public void cargarArchivo() {
        if (fotosPendientes == null) fotosPendientes = com.juans.inspeccion.Mundo.Storage.StoredInspections.read(
                MainActivity.context, com.juans.inspeccion.Mundo.Storage.StoredInspections.UPLOADS);
    }
    public void guardarArchivo() throws Exception {
        if (fotosPendientes != null) com.juans.inspeccion.Mundo.Storage.StoredInspections.write(
                MainActivity.context, com.juans.inspeccion.Mundo.Storage.StoredInspections.UPLOADS, fotosPendientes);
    }
    public static void invalidate() { fotosPendientes = null; }

    public  boolean agregarFotos(Inspeccion inspeccion)
    {
        boolean agrego=true;

        try {
            if(fotosPendientes==null)cargarArchivo();
            synchronized (fotosPendientes) {
                fotosPendientes.add(inspeccion);
                guardarArchivo();
            }


        } catch (Exception e) {
            e.printStackTrace();
            agrego=false;
        }
        return agrego;
    }



    public synchronized  boolean hayPendientes()
    {
        if(fotosPendientes==null) cargarArchivo();
        return fotosPendientes.size()!=0;
    }


    public boolean subirFotosServidor(FTPUpload ftpUpload) throws Exception {
        Inspeccion unaInspeccion;
        synchronized (fotosPendientes) {
            int ultimaInspeccion = fotosPendientes.size() - 1;
             unaInspeccion = fotosPendientes.remove(ultimaInspeccion);
            guardarArchivo();
        }

            File parent = new File (unaInspeccion.getParentFolder());
            for (Album album : unaInspeccion.getFotosInspeccion()) {
                ArrayList<String> fotosAlbum = album.getFotos();
                while (fotosAlbum.size() != 0) {
                    //Toma la ultima foto del album
                    String foto = album.getFotos().get(fotosAlbum.size() - 1);
                    File fotoFile = new File(foto);



                    boolean pudoSubir = ftpUpload.uploadSingleFile(fotoFile, parent.getName());
                    if (pudoSubir) {
                        fotoFile.delete();
                        Log.e("SubioFoto", fotosAlbum.remove(fotosAlbum.size() - 1));

                    } else {
                        //SI no puede ser subida, agrega las fotos de la inspeccion a  la lista de pendientes,actualiza el archivo y retorna false,
                        synchronized (fotosPendientes)
                        {
                          fotosPendientes.add(unaInspeccion);
                          guardarArchivo();
                        }
                        return false;
                    }
                }

            }
            //Si llego hasta aca es por que ya subio todas las fotos de la carpeta
            String carpeta = parent.getPath();
            boolean borro = parent.delete();
            Log.e("Borrando carpeta", carpeta + "=" + borro);
            return true;




    }


}
