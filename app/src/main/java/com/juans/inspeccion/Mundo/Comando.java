package com.juans.inspeccion.Mundo;

import android.graphics.Bitmap;

/**
 * Created by Juan on 05/03/2015.
 */
public class Comando {


    public String getComando() {
        return comando;
    }

    String comando;

    public Bitmap getImagen() {
        return imagen;
    }

    Bitmap imagen;

    public int getTipo() {
        return tipo;
    }

    int tipo;


    public Comando(int tipo,String comando)
    {
       this.tipo=tipo;
        this.comando=comando;

    }

    public void setImagen(Bitmap imagen)
    {
        this.imagen=imagen;
    }

   public int darTipo()
   {
       return tipo;
   }


    public String darComando()
    {
        return comando;
    }

    public Bitmap darBitMap()
    {
        return imagen;
    }



}
