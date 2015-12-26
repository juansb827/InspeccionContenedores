package com.juans.inspeccion.Mundo.Recibos;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import com.juans.inspeccion.Interfaz.InspeccionActivity;
import com.juans.inspeccion.Mundo.ColumnasTablas;
import com.juans.inspeccion.Mundo.Consultas;
import com.juans.inspeccion.Mundo.DaniosManager;
import com.juans.inspeccion.Mundo.Formularios;
import com.juans.inspeccion.Mundo.Inspeccion;
import com.juans.inspeccion.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Juan on 05/03/2015.
 */
public class CreadorRecibos {


    public CreadorRecibos(){

    };

    public  static void generarFactura(Inspeccion inspeccion,Context c) {



        Resources resources=c.getResources();
        ComandosImpresora comandos=new ComandosImpresora();
        HashMap<String,String> cabecera=inspeccion.getInformacion();
        Iterator it = cabecera.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            String campo = (String) pair.getValue();
            if (campo == null) {
                cabecera.put((String) pair.getKey(), "--");
            } else if (campo.isEmpty()) {
                cabecera.put((String) pair.getKey(), "-");
            }
        }


        agregarImagen(comandos);
        agregarContenido(comandos, inspeccion, resources);
        agregarDanios(comandos,inspeccion,resources);
        agregarFinal(comandos,inspeccion,resources);
        comandos.finalizar();



        if(true)
        {
            try {
                Impresora.getInstance().enviarComandosImpresora(comandos.darListaComandos(), resources);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }

        }



    }

    private static void agregarFinal(ComandosImpresora comandos, Inspeccion ins,Resources res) {
        HashMap<String,String> cabecera=ins.getInformacion();

        String estado=cabecera.get(res.getString(R.string.CESTADOCNTR));
        String tipoTurno=cabecera.get(res.getString(R.string.CTIPOTURNO));
        if (tipoTurno==null ) tipoTurno=cabecera.get(res.getString(R.string.CTIPOMOV)).trim();


        if ( estado!=null && tipoTurno.equals(InspeccionActivity.ENTRADA))
        {
            comandos.escribir("Estado", ComandosImpresora.S3, 0, false);
            comandos.escribir(":" + estado, ComandosImpresora.S3, 200, false);
            comandos.saltoLinea();

            String estFinal = cabecera.get(res.getString(R.string.CESTFINCNTR));
            comandos.escribir("Adaptable", ComandosImpresora.S3, 0, false);
            comandos.escribir(":" + estFinal, ComandosImpresora.S3, 200, false);
            comandos.saltoLinea();
        }
        else if(estado!=null && tipoTurno.equals(InspeccionActivity.SALIDA))
        {

            comandos.escribir("Estado", ComandosImpresora.S3, 0, false);
            comandos.escribir(":" + estado, ComandosImpresora.S3, 200, false);
            comandos.saltoLinea();
        }

        String transportador=cabecera.get(res.getString(R.string.CDESTRANSPOR));
        comandos.escribir("Transp", ComandosImpresora.S3,0,false);
        comandos.escribir(":"+transportador, ComandosImpresora.S3,200,false);
        comandos.saltoLinea();

        String conductor=cabecera.get(res.getString(R.string.CDESCONDUCTOR));
        comandos.escribir("Cond", ComandosImpresora.S3,0,false);
        comandos.escribir(":"+conductor, ComandosImpresora.S3,200,false);
        comandos.saltoLinea();

        String cedula=cabecera.get(res.getString(R.string.CDESCEDULA));
        comandos.escribir("Cedula", ComandosImpresora.S3,0,false);
        comandos.escribir(":"+cedula, ComandosImpresora.S3,200,false);
        comandos.saltoLinea();

        String celular=cabecera.get(res.getString(R.string.CCELULAR));
        comandos.escribir("Celular", ComandosImpresora.S3,0,false);
        comandos.escribir(":"+celular, ComandosImpresora.S3,200,false);
        comandos.saltoLinea();

        String cplaca=cabecera.get(res.getString(R.string.CPLACA));
        comandos.escribir("Placa", ComandosImpresora.S3,0,false);
        comandos.escribir(":"+cplaca, ComandosImpresora.S3,200,true);
        comandos.saltoLinea();

        String cobserva=cabecera.get(res.getString(R.string.COBSERVACION)).trim();


        int[] cordPrrafo=comandos.escribirParrafo(cobserva,ComandosImpresora.S3,40,10,false,200);
        comandos.ponerRectanguloCordenadas(cordPrrafo[0],cordPrrafo[1]);

        String nomInspector=cabecera.get(res.getString(R.string.CNOMINSPECTOR));

         comandos.escribirCentrado(nomInspector, ComandosImpresora.S3,false);
        comandos.saltoLinea();
        comandos.escribirCentrado("Inspector", ComandosImpresora.S3, false);
        comandos.saltoLinea(150);
        comandos.ponerLinea();
        comandos.saltoLinea(10);
        comandos.escribir("Firma del conductor", ComandosImpresora.S3, 100, false);
        comandos.saltoLinea(50);
        comandos.escribir("El consignatario o el embarcador son responsables", ComandosImpresora.S2, 0, false);
        comandos.saltoLinea(20);
        comandos.escribir("de la devolución del contenedor en   buen estado.", ComandosImpresora.S2, 0, false);
        comandos.saltoLinea(20);
        comandos.escribir("Daños  encontrados al momento de  la   devolución", ComandosImpresora.S2,0,false);
        comandos.saltoLinea(20  );
        comandos.escribir("deben ser recobrados. Este documento reemplaza el", ComandosImpresora.S2,0,false);
        comandos.saltoLinea(20  );
        comandos.escribir("contrato   de  comodato  bajo   los  términos del", ComandosImpresora.S2,0,false);
        comandos.saltoLinea(20  );
        comandos.escribir("contrato de transporte marítimo.", ComandosImpresora.S2,0,false);

        comandos.saltoLinea(50);









    }

    public static void agregarImagen(ComandosImpresora comandos)
    {

        try {
            HashMap<String,String> info=ColumnasTablas.getInstance().darInfoEmpresa();
            Bitmap myBitmap= BitmapFactory.decodeFile(info.get(ColumnasTablas.FILE_LOGO_EMPRESA));
            //FileInputStream fis= context.openFileInput(ColumnasTablas.FILE_LOGO_EMPRESA);
            //Bitmap bitmap= BitmapFactory.decodeStream(fis);

            comandos.ponerImagen(0,myBitmap.getHeight(), myBitmap);





        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }


    public static void agregarContenido(ComandosImpresora comandos, Inspeccion ins,Resources res)
    {

        HashMap<String,String> cabecera=ins.getInformacion();
        HashMap<String,String> datosCntr=ins.getDatosContenedor();

        String fechaEntera= Formularios.dateFromDbParser(ins.getFechaInspeccion()[Consultas.FECHA_ENTERA],"-",true);

        HashMap<String, String> infoEmpresa = ColumnasTablas.getInstance().darInfoEmpresa();
        String nombreEmpresa=infoEmpresa.get(res.getString(R.string.CNOMBRE));
        String nit=infoEmpresa.get(res.getString(R.string.CNIT));
        comandos.escribirCentrado(nombreEmpresa,ComandosImpresora.S3,false);
        comandos.saltoLinea();
        comandos.escribirCentrado("NIT:"+nit,ComandosImpresora.S3,false);
        comandos.saltoLinea();

        comandos.escribirCentrado("EQUIPEMENT INTERCHANGE RECEIPT",ComandosImpresora.S3,false);
        comandos.saltoLinea();
        comandos.escribirCentrado(fechaEntera, ComandosImpresora.S3, false);
        comandos.saltoLinea();


        String tipoTurno=cabecera.get(res.getString(R.string.CTIPOTURNO));
        if (tipoTurno==null || tipoTurno.startsWith("-") ){
            tipoTurno=cabecera.get(res.getString(R.string.CTIPOMOV));

        }

        String tipo="-";
        if (tipoTurno!=null){ tipoTurno=tipoTurno.trim();
        if(tipoTurno.equals(InspeccionActivity.ENTRADA))
        {
            tipo="GATE IN (ENTRADA)";
        }
        else if(tipoTurno.equals(InspeccionActivity.SALIDA))
        {
            tipo="GATE OUT (SALIDA)";
        }}
        comandos.escribirCentrado(tipo, ComandosImpresora.S3, true);
        comandos.saltoLinea();

        comandos.escribir("Nro de EIR", ComandosImpresora.S3, 0, false);
        String tipoDoc=cabecera.get(res.getString(R.string.CTIPDOC));
        String numDoc=cabecera.get(res.getString(R.string.NNUMDOC));
        comandos.escribir(": "+tipoDoc+"-"+numDoc, ComandosImpresora.S3, 200, true);
        comandos.saltoLinea();

        String numBooking=cabecera.get(res.getString(R.string.CBOOKING));
        if(numBooking!=null) {
            comandos.escribir("Booking", ComandosImpresora.S3, 0, false);
            comandos.escribir(":" + numBooking, ComandosImpresora.S3, 200, false);
            comandos.saltoLinea();
        }

        String codcntr=datosCntr.get(res.getString(R.string.CCODCNTR));

        String tamcntr=datosCntr.get(res.getString(R.string.CTAMCNTR));

        String tipocntr=datosCntr.get(res.getString(R.string.CTIPOCNTR));

        String codiso=datosCntr.get(res.getString(R.string.CCODISOCNTR));


        comandos.escribir(codcntr,ComandosImpresora.S5,0,true);
        comandos.saltoLinea(50);

        String codcompleto=tamcntr+"    "+tipocntr+"   "+codiso;
        comandos.escribir(codcompleto,ComandosImpresora.S3,0,true);
        comandos.saltoLinea();

        String dfechafab=datosCntr.get(res.getString(R.string.DFECHAFAB));
        if(dfechafab!=null && dfechafab.length()>10)
        {
            dfechafab=dfechafab.substring(0,10);
        }

        comandos.escribir("Manuf Date", ComandosImpresora.S3,0, false);
        comandos.escribir(":"+dfechafab, ComandosImpresora.S3,180, false);
        comandos.saltoLinea();

        String maxTam=datosCntr.get(res.getString(R.string.NPESMAXCNTR));
        comandos.escribir("Max Gross", ComandosImpresora.S3,0, false);
        comandos.escribir(":"+maxTam, ComandosImpresora.S3,180, false);
        comandos.saltoLinea();

        String tara=datosCntr.get(res.getString(R.string.NTARACNTR));
        comandos.escribir("Tare", ComandosImpresora.S3,0, false);
        comandos.escribir(":"+tara, ComandosImpresora.S3,180, false);
        comandos.saltoLinea();

        String acep=datosCntr.get(res.getString(R.string.CCODACEP));
        comandos.escribir("Acep", ComandosImpresora.S3,0, false);
        comandos.escribir(":"+acep, ComandosImpresora.S3,180, false);
        comandos.saltoLinea();

        String lineaDes=cabecera.get(res.getString(R.string.CDESCTELNA));
        comandos.escribir("Linea", ComandosImpresora.S3,0, false);
        comandos.escribirMultiLinea(180,":"+lineaDes,ComandosImpresora.S3,false);



        String cliente=cabecera.get(res.getString(R.string.CCLIENTE));
        comandos.escribir("Cliente", ComandosImpresora.S3,0, false);
        comandos.escribirMultiLinea(180,":"+cliente,ComandosImpresora.S3,false);


        String agente=cabecera.get(res.getString(R.string.TER_RAZONS));
        comandos.escribir("Agente", ComandosImpresora.S3,0, false);
        comandos.escribirMultiLinea(180,":"+agente,ComandosImpresora.S3,false);



        if(TextUtils.equals(InspeccionActivity.ET,tipoDoc))
        {
            String detalle=cabecera.get(res.getString(R.string.CDETALLECARGA));
            comandos.escribir("Detalle Carga", ComandosImpresora.S3,0, false);
            comandos.escribirMultiLinea(180,":"+detalle,ComandosImpresora.S3,false);



            String numsellos=cabecera.get(res.getString(R.string.CNUMSELLOS));
            comandos.escribir("Sellos", ComandosImpresora.S3,0, false);
            comandos.escribirMultiLinea(180,":"+numsellos,ComandosImpresora.S3,false);
            comandos.saltoLinea();

            String numBl=cabecera.get(res.getString(R.string.CNUMEROBL));
            comandos.escribir("Importacion", ComandosImpresora.S3,0, false);
            comandos.escribir(":"+numBl, ComandosImpresora.S3,180, true);
            comandos.saltoLinea();

        }
        else{
            String motonave=cabecera.get(res.getString(R.string.CMOTONAVE));
            comandos.escribir("Buque", ComandosImpresora.S3,0, false);
            comandos.escribir(":"+motonave, ComandosImpresora.S3,180, false);
            comandos.saltoLinea();

            String viaje=cabecera.get(res.getString(R.string.CVIAJE));
            comandos.escribir("Viaje", ComandosImpresora.S3,0, false);
            comandos.escribir(":"+viaje, ComandosImpresora.S3,180, false);
            comandos.saltoLinea();

        }

        String tipoMov=cabecera.get(res.getString(R.string.CUSOLOGICO));
        String tipoIngreso=cabecera.get(res.getString(R.string.CGRUPOMOV));
        String mov=tipoMov+" - "+tipoIngreso;
        comandos.escribir("Mov", ComandosImpresora.S3,0, false);
        comandos.escribir(":"+mov, ComandosImpresora.S3,180, false);
        comandos.saltoLinea();




    }

    public static  void agregarDanios(ComandosImpresora comandos,Inspeccion inspeccion,Resources res)
    {


        ArrayList<HashMap<String, String>> listaDanios = DaniosManager.darListaSoloDetalles(inspeccion.getDaniosManager().getListaDanios());
        HashMap<String, String> mapa = inspeccion.getInformacion();
        String tipoTurno=mapa.get(res.getString(R.string.CTIPOTURNO));
        if(tipoTurno==null) {tipoTurno=mapa.get(res.getString(R.string.CTIPOMOV));}
        String estadoActual=mapa.get(res.getString(R.string.CESTADOCNTR));
        if (listaDanios==null || listaDanios.size()==0) return;
        if (tipoTurno.trim().equals("SALIDA") && estadoActual.startsWith("AP"))
        {
            return;
        }

        comandos.saltoLinea();
        comandos.escribir("DAÑOS",ComandosImpresora.S3,200,true);
        comandos.saltoLinea();

        //Agregar primera fila
        comandos.escribir("Loc", ComandosImpresora.S2,0,true);
        comandos.escribir("Daño", ComandosImpresora.S2,80,true);
        comandos.escribir("Comp", ComandosImpresora.S2,180,true);
        comandos.escribir("Cant", ComandosImpresora.S2,280,true);
        comandos.escribir("Met", ComandosImpresora.S2,380,true);
        comandos.escribir("Resp", ComandosImpresora.S2,480,true);

        //agregaContenido

        for(int i=0;i<listaDanios.size();i++)
        {
            HashMap<String,String> danio=listaDanios.get(i);
            comandos.saltoLinea();
            comandos.escribir(danio.get( res.getString(R.string.CCODUBI)), ComandosImpresora.S2,0,false);
            comandos.escribir(danio.get(res.getString(R.string.CCODDAN)), ComandosImpresora.S2,80,false);
            comandos.escribir(danio.get(res.getString(R.string.CCODELE)), ComandosImpresora.S2,180,false);
            comandos.escribir(danio.get(res.getString(R.string.NUNIDADES)), ComandosImpresora.S2,280,false);
            comandos.escribir(danio.get(res.getString(R.string.CCODMET)), ComandosImpresora.S2,380,false);
            String cargoA=danio.get(res.getString(R.string.CCARGOA));
            if (cargoA.equals(DaniosManager.NO_APLICA))
            {
                cargoA="N/A";
            }
            comandos.escribir(cargoA, ComandosImpresora.S2, 480, false);



        }
        comandos.saltoLinea();


    }

}

