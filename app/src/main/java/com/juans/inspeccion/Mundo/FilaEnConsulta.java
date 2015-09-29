package com.juans.inspeccion.Mundo;

import java.io.Serializable;

/**
 * Created by juan__000 on 8/17/2014.
 */
public class FilaEnConsulta implements Serializable{


    private String[] datos;


    public FilaEnConsulta(int numColumnas)
    {

        datos=new String[numColumnas];

    }

    public int getNumColumnas()
    {
        return datos.length;
    }

    public String getDato(int numColumna)
    {
        return datos[numColumna];
    }

    public void setDato(int numColumna,String nDato)
    {
           datos[numColumna]=nDato;
    }
}
