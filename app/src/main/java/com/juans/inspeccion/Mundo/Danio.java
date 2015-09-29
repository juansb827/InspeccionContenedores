package com.juans.inspeccion.Mundo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Juan on 03/05/2015.
 */
public class Danio implements Serializable {

    private HashMap<String, String> detalles;
    private Album album;

    public Danio()
    {

        detalles=new HashMap<>();
        album=new Album();

    }

    public HashMap<String, String> getDetalles() {
        return detalles;
    }

    public void setDetalles(HashMap<String, String> detalles) {
        this.detalles = detalles;
    }

    public ArrayList<String> getFotos() {
        return album.getFotos();
    }

    public Album getAlbum()
    {
        return album;
    }
    public void  setAlbum(Album album)
    {
         this.album=album;
    }

    public void setFotos(ArrayList<String> fotos) {
        this.album.setFotos(fotos);
    }
}
