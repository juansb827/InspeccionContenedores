package com.juans.inspeccion.Mundo;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.juans.inspeccion.ConnectionException;
import com.juans.inspeccion.DataBaseException;
import com.juans.inspeccion.Interfaz.MainActivity;
import com.juans.inspeccion.R;

import org.apache.poi.util.ArrayUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by juan__000 on 11/3/2014.
 */
public class Listas {

    private static HashMap<String, Object> listas;
    public final static String LISTA_ESTADOS = "LISTA_ESTADOS";

    public final static String LISTAS_DE_UBICACIONES = "LISTAS_DE_UBICACIONES";

    public final static String LISTA_SECUENCIAS = "LISTA_SECUENCIAS";

    public final static String LISTA_COMPONENTES = "LISTA_COMPONENTES";

    public final static String LISTA_TIPOS_DE_DANIO = "LISTA_TIPOS_DE_DANIO";

    public final static String LISTA_METODOS_DE_REPARACION = "LISTA_METODOS_DE_REPARACION";

    public final static String LISTA_LINEAS = "LISTA_LINEAS";
    public final static String LISTAS_MOTONAVES_POR_LINEA="LISTAS_MOTONAVES_POR_LINEA";
    public final static String LISTA_TIPOS_INGRESO = "LISTA_TIPOS_INGRESO";

    public final static String LISTA_PUERTOS = "LISTA_PUERTOS";
    public final static String LISTA_USO_LOGICO = "LISTA_USO_LOGICO";

    public  final static String LISTA_TIPOS_CONTENEDOR = "LISTA_TIPOS_CONTENEDOR";
    public final static String LISTA_MATERIALES_CONTENEDOR = "LISTA_MATERIALES_CONTENEDOR";
    public final static String LISTA_TAMANOS_CONTENEDOR = "LISTA_TAMANOS_CONTENEDOR";
    public static boolean importantesEstanCargadas =false;
    public static boolean noInspeccionEstanCargadas=false;
    public Listas() {

    }

    public static void borrarListas(Context c) {
        String[] listas = c.getResources().getStringArray(R.array.Listas);
        String[] lista2=c.getResources().getStringArray(R.array.ListasNoInspeccion);

        for (int i = 0; i < listas.length; i++) {
            try {

                File file = c.getFileStreamPath(listas[i]);
                if (file.exists()) {
                    file.delete();
                }

            } catch (Exception e) {
                Log.e("LISTAS", "Hubo un error borrando las listas");
                e.printStackTrace();
            }
        }

        for (int i = 0; i < lista2.length; i++) {
            try {

                File file = c.getFileStreamPath(lista2[i]);
                if (file.exists()) {
                    file.delete();
                }

            } catch (Exception e) {
                Log.e("LISTAS", "Hubo un error borrando las listas");
                e.printStackTrace();
            }
        }
        noInspeccionEstanCargadas=false;
        importantesEstanCargadas=false;


    }

    public static void guardarLista(Object lista, String FILE_NAME) {

        try {

            File file = MainActivity.context.getFileStreamPath(FILE_NAME);
            if (file.exists()) {
                file.delete();
            }
            FileOutputStream fos = MainActivity.context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(lista);
            Log.e("GuardoLista", "Guardo Lsita");
            os.close();
        } catch (Exception e) {
            Log.e("LISTAS", "Hubo un error guardando las listas");
            e.printStackTrace();
        }

    }


    public static Object cargarArchivoLista(String FILE_NAME) {
        Object lista = null;
        try {

            File file = MainActivity.context.getFileStreamPath(FILE_NAME);
            if (file != null && file.exists()) {
                FileInputStream fis = MainActivity.context.openFileInput(FILE_NAME);
                ObjectInputStream is = new ObjectInputStream(fis);
                lista = is.readObject();
                is.close();
            }


        } catch (Exception e) {
            e.printStackTrace();


        }
        return lista;


    }


    public static Object darLista(String lista)
    {
       Object  laLista=null;
        //MainACtivity garantiza que listas!=null
        if (listas!=null){
            laLista=listas.get(lista);
        }
        //Si lalista es null es por que no esta en Listas, si no esta en Listas es por que debe ser cargada del archivo cada vez que se necesite
        //y no mantener en moria (Listas es Static)
        if(laLista==null) {
            laLista = cargarArchivoLista(lista);
            Log.e("Listas", "La lista "+lista+"no esta en Listas");
        }
        return laLista;
    }

     /**
    *Carga las listas que en algun momento pueden ser usadas pero
     * no es necesario que esten siempre en memoria, son leidas del archivo
     * cuando se necesiten
    */
    public static void cargarListasNoInspeccion(Context c) throws ConnectionException, DataBaseException {
        String[] array = c.getResources().getStringArray(R.array.ListasNoInspeccion);
        for(int i=0;i<array.length;i++)
        {
            String nombreLista = array[i];
            if (nombreLista.equals(LISTAS_MOTONAVES_POR_LINEA)) {
                //se carga diferente
            }
            else{
            ArrayList lista = (ArrayList) cargarArchivoLista(nombreLista);
            if(lista==null || lista.size()==0)
            {
                int id = c.getResources().getIdentifier(nombreLista, "string", c.getPackageName());
                if(id==0) Log.e("Listas:" ,"Error buscando el resource "+nombreLista);
                String sentencia = c.getResources().getString(id);
                lista= DAO.getInstance().cargarLista(sentencia, 2);
                guardarLista(lista,nombreLista);
            }}


        }


        //ASI SE CARGAN LAS MOTONAVES
        String listaLineas=LISTA_LINEAS;
        HashMap<String, ArrayList<FilaEnConsulta>> motonaves = (HashMap<String, ArrayList<FilaEnConsulta>>) cargarArchivoLista(LISTAS_MOTONAVES_POR_LINEA);

        if( motonaves==null ||  motonaves.size()==0) {
            motonaves=new HashMap<>();
            ArrayList<FilaEnConsulta> lineas = (ArrayList<FilaEnConsulta>) listas.get(listaLineas);
            //Saca las ubicaciones para cada secuencia
            for (int i = 0; i < lineas.size(); i++) {
                String linea = lineas.get(i).getDato(0);

                ArrayList<FilaEnConsulta> motonavesDeLinea =
                        DAO.getInstance().cargarLista(c.getString(R.string.LISTAS_MOTONAVES_POR_LINEA) + "'" + linea + "'", 2);
                motonaves.put(linea, motonavesDeLinea);

            }

            guardarLista(motonaves, LISTAS_MOTONAVES_POR_LINEA);
        }




        noInspeccionEstanCargadas=true;

    }
    public static void cargarListasInspecion(Context c) throws ConnectionException, DataBaseException {

        if (listas == null) listas = new HashMap<>();
        String[] array = c.getResources().getStringArray(R.array.Listas);
        for (int i = 0; i < array.length; i++) {
            String nombreLista = array[i];


            if (nombreLista.equals(LISTAS_DE_UBICACIONES)) {
                //nothing
            } else if (!listas.containsKey(nombreLista)) {
                ArrayList lista = (ArrayList) cargarArchivoLista(nombreLista);
                if (lista != null && lista.size() != 0) listas.put(nombreLista, lista);
                else {
                    int id = c.getResources().getIdentifier(nombreLista, "string", c.getPackageName());
                    if(id==0) Log.e("Listas:" ,"Error buscando el resource "+nombreLista);
                    String sentencia = c.getResources().getString(id);
                    ArrayList<FilaEnConsulta> temp = DAO.getInstance().cargarLista(sentencia, 2);
                    listas.put(nombreLista, temp);
                    guardarLista(temp, nombreLista);

                }
            }

        }


        String listSec=LISTA_SECUENCIAS;
        HashMap<String, ArrayList<FilaEnConsulta>> ubicaciones = (HashMap<String, ArrayList<FilaEnConsulta>>) cargarArchivoLista(LISTAS_DE_UBICACIONES);

        if( ubicaciones==null ||  ubicaciones.size()==0) {
            ubicaciones=new HashMap<>();
            ArrayList<FilaEnConsulta> secuencias = (ArrayList<FilaEnConsulta>) listas.get(listSec);
            //Saca las ubicaciones para cada secuencia
            for (int i = 0; i < secuencias.size(); i++) {
                String secuencia = secuencias.get(i).getDato(0);
                ArrayList<FilaEnConsulta> listaUbicaciones =
                        DAO.getInstance().cargarLista(c.getString(R.string.LISTA_UBICACIONES_CON_SECUENCIA) + "'" + secuencia + "'", 2);
                ubicaciones.put(secuencia, listaUbicaciones);

            }

            guardarLista(ubicaciones, LISTAS_DE_UBICACIONES);
        }
        listas.put(LISTAS_DE_UBICACIONES,ubicaciones);
         Log.e("Listas", "Cargaron las listas");


        listas.put(LISTA_USO_LOGICO,crearListaUsoLogico());
        importantesEstanCargadas =true;


    }


    private static ArrayList<FilaEnConsulta> crearListaUsoLogico()
    {
        ArrayList<FilaEnConsulta> lista=new ArrayList<>();
        FilaEnConsulta vacio=new FilaEnConsulta(2);

        vacio.setDato(0,"EMPTY");
        vacio.setDato(1,"VACIO");

        FilaEnConsulta lleno=new FilaEnConsulta(2);

        lleno.setDato(0,"FULL");
        lleno.setDato(1,"LLENO");

        lista.add(vacio);
        lista.add(lleno);
        return lista;
    }



}
