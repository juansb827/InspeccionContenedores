package com.juans.inspeccion.Mundo;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.juans.inspeccion.Interfaz.MainActivity;
import com.juans.inspeccion.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by juan__000 on 10/31/2014.
 */
public class Pendientes {
     private static ArrayList<Inspeccion> listaPendientes;
     private static ArrayList<HashMap<String,String>> resumenTurnos;
     private final static String FILE_NAME = "listaPendientes";
    public final static String PENDIENTE="PENDIENTE";

     public static void guardarListaTurnos(Context c)
     {
         if(listaPendientes !=null) {
             try {

                 File file = c.getFileStreamPath(FILE_NAME);
                 if (file.exists()) {
                     file.delete();
                 }
                 FileOutputStream fos = MainActivity.context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
                 ObjectOutputStream os = new ObjectOutputStream(fos);
                 os.writeObject(listaPendientes);
                 os.close();
             }
             catch (IOException e)
             {
                 Toast.makeText(MainActivity.context, "Hubo un error guardando los turnos pendientes",Toast.LENGTH_SHORT).show();
                 e.printStackTrace();
             }
         }

     }

    public static void cargarListaTurnos(Context c)
    {
        try {

            File file = c.getFileStreamPath(FILE_NAME);
            if (file.exists()) {

                FileInputStream fis = MainActivity.context.openFileInput(FILE_NAME);
                ObjectInputStream is = new ObjectInputStream(fis);
                 listaPendientes =(ArrayList<Inspeccion>) is.readObject();
                is.close();
            }

        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        if(listaPendientes ==null){
            listaPendientes =new ArrayList();
        }

    }


    public static Inspeccion darPendiente(int i,Context c)
    {
        Inspeccion inspeccion=null;
        if(listaPendientes ==null)cargarListaTurnos(c);
        if(listaPendientes !=null && listaPendientes.size()>i)
        {
            inspeccion= listaPendientes.get(i);
        }
            return inspeccion;
    }
    @NonNull
    public static ArrayList<Inspeccion> darListaTurnos(Context c)
    {
       if(listaPendientes ==null)cargarListaTurnos(c);
        return listaPendientes;
    }

    public  static ArrayList<HashMap<String,String>> darResumenTurnos(Context c)
    {
        if(listaPendientes ==null) cargarListaTurnos(c);
        if(resumenTurnos==null )
        {
        resumenTurnos=new ArrayList();
        for(int i=0;i< listaPendientes.size();i++) {
            Inspeccion t = listaPendientes.get(i);
            HashMap<String, String> mapa = t.getInformacion();
            String Nturno = MainActivity.context.getResources().getString(R.string.NTURNO);
            String numeroTurno = mapa.get(Nturno);
            String fechaMostar = MainActivity.context.getResources().getString(R.string.FECHA_MOSTRAR);
            String fechaTurno = mapa.get(fechaMostar);
            String ctipoturno = MainActivity.context.getResources().getString(R.string.CTIPOTURNO);
            String tipoTurno = mapa.get(ctipoturno);

            HashMap<String, String> resumen = new HashMap<String, String>();
            resumen.put(Nturno, numeroTurno);
            resumen.put(fechaMostar, fechaTurno);
            resumen.put(ctipoturno, tipoTurno);
            resumenTurnos.add(resumen);

        }




        }
        return resumenTurnos;
    }

    public static void agregarInspeccion(Inspeccion inspeccion, Context c)
    {
       if(listaPendientes ==null) cargarListaTurnos(c);
        listaPendientes.add(inspeccion);
        resumenTurnos=null;
        guardarListaTurnos(c);
    }

    public static void remplazarTurno(int indice,Inspeccion inspeccion,Context c)
    {
        if(listaPendientes ==null) cargarListaTurnos(c);
        listaPendientes.set(indice, inspeccion);
        guardarListaTurnos(c);
    }

    public static void borrarTurno(int indice,Context c)
    {
      try {
          if (listaPendientes != null && indice < listaPendientes.size()) {
              listaPendientes.remove(indice);
              resumenTurnos = null;
              guardarListaTurnos(c);
          }
      }
      catch (Exception e)
      {
          e.printStackTrace();
      }

    }

    public static void borrarTodo()
    {
        File file = MainActivity.context.getFileStreamPath(FILE_NAME);
        if (file.exists()) {
            file.delete();
        }
        listaPendientes.clear();
        resumenTurnos.clear();

    }







}
