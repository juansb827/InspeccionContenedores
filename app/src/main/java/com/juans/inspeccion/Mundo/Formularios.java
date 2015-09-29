package com.juans.inspeccion.Mundo;

import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;

import com.juans.inspeccion.CustomView.CustomView;
import com.juans.inspeccion.Varios;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by juan__000 on 10/19/2014.
 */
public class Formularios {

    public static boolean comprobarCamposObligatorios(ArrayList<CustomView> listaCampos)
    {
        for(int i=0;i<listaCampos.size();i++)
        {
            if(listaCampos.get(i).esObligatorio())
            {
                if(listaCampos.get(i).getTexto().trim().isEmpty())
                {
                    return false;
                }
            }
        }
        return true;
    }

    public static void asignarInputDialog(ArrayList<CustomView> listaCampos, View.OnFocusChangeListener focusChangeListener)
    {
        for(int i=0;i<listaCampos.size();i++)
        {
            CustomView cw=listaCampos.get(i);
            if (cw.getMyInputType()!=CustomView.NORMAL)
            {
                ((View ) cw).setOnFocusChangeListener(focusChangeListener);

            }
        }

    }
    // HashMap<String,String[]> secondCOnfig= mapa de los campos <NOmbrecampo, Arreglo de tamaño 2 con 0=tipo campo,1,nombre de la lista;
    public static void asignarSecondaryInput(ArrayList<CustomView> listaCampos, View.OnFocusChangeListener focusChangeListener,HashMap<String,String[]> secondaryConfig)
    {
        for(int i=0;i<listaCampos.size();i++)
        {
            CustomView cw=listaCampos.get(i);
            String[] config=secondaryConfig.get(cw.getNombreCampo());
            if (config!=null)
            {
                ((View ) cw).setOnFocusChangeListener(focusChangeListener);


            }
        }

    }

    public static void recorrerTableLayout(ViewGroup tabla, ArrayList<CustomView> listaCampos)
    {
        for (int i = 0, count = tabla.getChildCount(); i < count; ++i) {
            ViewGroup tableRow = (ViewGroup) tabla.getChildAt(i);
            for (int j = 0; j < tableRow.getChildCount(); j++) {
                View textField = tableRow.getChildAt(j);
                if (textField instanceof CustomView) {


                    listaCampos.add((CustomView) textField);




                }
            }
        }
    }

    public static void recorrerLinearLayour(ViewGroup linearLayour, ArrayList<CustomView> listaCampos)
    {
        for (int i = 0, count = linearLayour.getChildCount(); i < count; ++i) {

                View textField = linearLayour.getChildAt(i);
                if (textField instanceof CustomView) {


                    listaCampos.add((CustomView) textField);




                }

        }
    }

    public static String dateFromDbParser(String fechaEntera,String separador,boolean ponerHora) //from YYYY-MM-DD hh:mm:ss to yyyy/mm/dd;
    {
        String resultado="";
        try {
            String[] number = fechaEntera.split("\\D+");

                String anio=number[0];
                String mes=number[1];
                String dia=number[2];
                resultado = anio + separador + mes + separador + dia;
            if(ponerHora)
            {
                resultado+="  "+number[3]+":"+number[4];
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return resultado;

    }

    public static String dateToDbParser(String fecha) //from  yyyy/mm/dd to yyyymmdd;
    {
        String resultado="";
        try {
            String[] number = fecha.split("\\D+");

                String dia = number[2];
                String mes = number[1];
                mes = String.format("%02d", Integer.parseInt(mes));
                dia = String.format("%02d", Integer.parseInt(dia));
                String anio = number[0];

                resultado = anio + mes + dia;

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return resultado;
    }





    public static String myDateParser(String fecha) //from DD/MM/YYYY to YYYYMMDD
    {
        String resultado="";
        try {
            String[] number = fecha.split("\\D+");
            if (number.length == 3) {
                String dia = number[0];
                String mes = number[1];
                mes = String.format("%02d", Integer.parseInt(mes));
                dia = String.format("%02d", Integer.parseInt(dia));
                String anio = number[2];
                resultado = anio + mes + dia;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return resultado;
    }

    public static String[] fechaToArray(String texto)
    {
        String fecha[]=new String[7];
        String[] parts = texto.split("\\D+");
        fecha[Consultas.FECHA_DIA] = parts[2];
        fecha[Consultas.FECHA_MES] = parts[1];
        fecha[Consultas.FECHA_ANIO] = parts[0];
        fecha[Consultas.FECHA_HORA] =parts[3];
        fecha[Consultas.FECHA_MINUTOS] =parts[4];
        fecha[Consultas.FECHA_ENTERA]=texto;
        fecha[Consultas.FECHA_MOSTRAR]=parts[0]+"/"+parts[1]+"/"+parts[2];
//        fecha[Consultas.FECHA_HORA] = Varios.agregarCeros(hora, 2);
//        fecha[Consultas.FECHA_MINUTOS] = Varios.agregarCeros(minutos, 2);
//        fecha[Consultas.FECHA_ENTERA] = fecha;ntera;
        return fecha;
    }

    public static boolean fechaEsMismoDia(String[] fecha1,String[] fecha2)
    {
        boolean dia=fecha1[Consultas.FECHA_DIA].equals(fecha2[Consultas.FECHA_DIA]);
        boolean mes=fecha1[Consultas.FECHA_MES].equals(fecha2[Consultas.FECHA_MES]);
        boolean anio=fecha1[Consultas.FECHA_ANIO].equals(fecha2[Consultas.FECHA_ANIO]);
        return dia && mes && anio;
    }

    public static String editarFecha(String texto) {
        if(texto==null || texto.trim().isEmpty()) return "";
        String fecha = null;
        String[] parts = texto.split("\\D+");
        String anio=parts[0];
        String mes=parts[1];
        fecha=anio+mes;
        return fecha;
    }
    //Valida ej: 201206 =true
    public static String validarFecha(String texto) {
        String fecha = null;
        if (texto == null || texto.length() != 6) ;
        else {
            String mes = texto.substring(4);
            String anio = texto.substring(0, 4);

            if (Integer.parseInt(mes) <= 12) {
                fecha = anio + "/" + mes + "/01";
            }
        }
        return fecha;
    }





    public interface DataPass
    {
        public void onDataReceive(Object data, int autor);


    }

}


