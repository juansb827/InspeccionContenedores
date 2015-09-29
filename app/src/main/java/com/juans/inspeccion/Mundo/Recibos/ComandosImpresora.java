package com.juans.inspeccion.Mundo.Recibos;

import android.graphics.Bitmap;
import android.util.Log;

import com.juans.inspeccion.Mundo.Comando;
import com.juans.inspeccion.Mundo.Inspeccion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by Juan on 05/03/2015.
 */
public class ComandosImpresora {
    int xActual,yAcual;
    private ArrayList<Comando> listaComandos;
    private ArrayList<Comando> pendientes;
     public final static String S1="A";

     public  final static String S2="B";
    private  static final HashMap<String,Integer> sizes=createMap();
    private static HashMap<String,Integer> createMap()
    {
        HashMap<String,Integer> map=new HashMap<>();
        map.put("A",6);
        map.put("B",8);
        map.put("C",10);
        map.put("D",12);
        map.put("E",14);
        map.put("F",18);
        map.put("G",24);
        map.put("H",30);
        return map;
    }
     public  final static String S3="C";

    public final static String S4="D";
    public final static String S5="E";


    public final static int CMD=0;
    public final static int IMG=1;

    // 2.8 inch print width, mx30 has a dpi of 203 (8dtos/mm)
    //so 72mm*8 = +- 576dots
    public final static int Max_Widh=576;


    public ComandosImpresora()
    {
        xActual=0;
        yAcual=0;
        listaComandos=new ArrayList<Comando>();
        pendientes=new ArrayList<Comando>();
    }

    public void  saltoLinea()
    {
        yAcual+=40;
    }

    public void  saltoLinea(int salto)
    {
        yAcual+=salto;
    }

    private void inicializar()
    {
        int largoDocumento= (int) (yAcual/8)+25;
        String largo="^Q"+largoDocumento+",0,0";
        listaComandos.add(new Comando(CMD, largo));
        String ancho="^W100";
        listaComandos.add(new Comando(CMD, ancho));
        String velocidad="^S6";
        listaComandos.add(new Comando(CMD, velocidad));
        String load=("^L");
        listaComandos.add(new Comando(CMD,load));
    }

    public void finalizar()
    {
        inicializar();
        for(Comando pendiente:pendientes)
        {
         listaComandos.add(pendiente);
        }
        listaComandos.add(new Comando(CMD, "E"));
    }


    public void escribir(String texto,String tamaño,int x,boolean negrilla)
    {
     String size="A"+tamaño;
     String bold=negrilla?"B":"";
     String formato="0"+bold+"E";
     String comando=   size+","+x+","+yAcual+",1,1,0,"+formato+","+texto;
     pendientes.add(new Comando(CMD, comando));
    }

    public void escribirCentrado(String texto,String tamaño,boolean negrilla)
    {
        String size="A"+tamaño;
        String bold=negrilla?"B":"";
        String formato="0"+bold+"E";
        //
        int letra= (int) (sizes.get(tamaño)*1.6);
        int centro=(Max_Widh-   (letra*texto.length())  )/2;
        if(centro<0) centro=0;
        //Log.e("Centro",""+centro);
        String comando=   size+","+centro+","+yAcual+",1,1,0,"+formato+","+texto;
        pendientes.add(new Comando(CMD, comando));
    }

    public void escribirMultiLinea(int x,String texto,String tamaño,boolean negrilla)
    {
        String size="A"+tamaño;
        String bold=negrilla?"B":"";
        String formato="0"+bold+"E";
        //
        double letra= sizes.get(tamaño)*1.55;
        int caben= (int) ((Max_Widh-x)/letra);

        String linea="";

        int lineas=0;
        for(int i=0;i<texto.length();i++)
        {
         if(lineas!=0)
         {
            letra=sizes.get(tamaño)*1.8;
            caben= (int) (Max_Widh/letra);
             x=0;

         }
         linea+=texto.charAt(i);
         if(linea.length()==caben-1 || i==texto.length()-1)
         {
             lineas++;
             String comando=   size+","+x+","+yAcual+",1,1,0,"+formato+","+linea;
             linea="";
             pendientes.add(new Comando(CMD, comando));
             saltoLinea();
         }

        }


    }

    public void ponerRectangulo(boolean saltar)
    {
        int xFinal=500;
        int yFinal=yAcual+200;
        String comando="R0,"+yAcual+","+xFinal+","+yFinal+",2,2";
        if(saltar) yAcual=yFinal;
        pendientes.add(new Comando(CMD,comando));
    }

    public void ponerRectanguloCordenadas(int yInicial,int yFinal)
    {
        int xFinal=575;
        String comando="R0,"+yInicial+","+xFinal+","+yFinal+",2,2";
        pendientes.add(new Comando(CMD,comando));
    }


    public int[] escribirParrafo(String parrafo,String size,int interlineado,int x ,boolean negrilla, int alturaMin)
    {
        int yInicial=yAcual;
        //Para S3 caben 33 letras en mayuscula y (Hacerlo dinamico luego)
        int anchoMaximo=33;
        String linea=new String();
        double agregadas=0;
        for(int i=0;i<parrafo.length();i++)
        {
            String letra=""+parrafo.charAt(i);
            if(agregadas==0 && letra.equals(" "))
            {
             //no imprime espacios al inicio del linea
                continue;
            }
            else{
                linea += letra;
            }

            //Si esta en mayuscula
            if(letra.toUpperCase().equals(letra)) {
                agregadas++;
            }
            else{
                String[] delgadas=new String[]{"l","i","í"};
                if(Arrays.asList(delgadas).contains(letra))
                {
                    agregadas+=(0.75)/2;
                }
                else {
                    agregadas += 0.75;
                }
            }



            if(agregadas>=anchoMaximo || i==parrafo.length()-1)
            {
                escribir(linea, size, x, negrilla);
                saltoLinea(interlineado);
                linea="";
                agregadas=0;
            }

        }

        int yFinal=yAcual;
        int alturaUsada=yFinal-yInicial;
        if(alturaUsada<alturaMin)
        {
            saltoLinea(alturaMin-alturaUsada);
        }
        return new int[]{yInicial,yFinal};
    }

    public void ponerLinea()
    {
        int xFinal=600;
        int yFinal=yAcual+3;
        String comando=("Lo,0,"+yAcual+","+xFinal+","+yFinal);
        pendientes.add(new Comando(CMD,comando));
    }



    public void ponerImagen(int x,int altura,Bitmap img)
    {
        int ancho=img.getWidth();
        int centro=(ancho>=Max_Widh)?0:(Max_Widh-ancho)/2;
        String posicion=centro+","+yAcual;
        Comando comando=new Comando(IMG,posicion);
        comando.setImagen(img);
        pendientes.add(comando);
        yAcual+=altura;
    }


    public ArrayList<Comando> darListaComandos()
    {
        return listaComandos;
    }
}
