package com.juans.inspeccion.Mundo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Juan on 04/05/2015.
 */
public class Album implements Serializable{
    private int fotos_tomadas;

    public ArrayList<String> getFotos() {
        return fotos;
    }

    public void setFotos(ArrayList<String> fotos) {
        this.fotos = fotos;
    }

    public int getFotos_tomadas() {
        return fotos_tomadas;
    }

    public void setFotos_tomadas(int fotos_tomadas) {
        this.fotos_tomadas = fotos_tomadas;
    }

    private ArrayList<String> fotos;
    public Album()
    {
        fotos=new ArrayList<>();
        fotos_tomadas=0;

    }

    public void agregarFoto(String foto) throws Exception {

        Bitmap bitmap= BitmapFactory.decodeFile(foto);
        Bitmap resized= MyCameraHelper.getResizedWidth(bitmap,1280);
        File file=new File(foto);
        file.delete();
        FileOutputStream fos=new FileOutputStream(file);
        resized.compress(Bitmap.CompressFormat.JPEG, 90, fos);


        Log.e("Album:agrego",foto);
        fotos.add(foto);
        fotos_tomadas++;
    }

    public void eliminarFoto(int i) throws IOException
    {
       String fotoPath=fotos.get(i);
        File foto=new File(fotoPath);
        foto.delete();
        Log.e("Album:elimino",fotoPath);
        fotos.remove(i);
    }

    public void borrarAlbum() throws IOException {

        while(fotos.size()!=0) {
            eliminarFoto(0);
            fotos_tomadas--;
        }


    }

}
