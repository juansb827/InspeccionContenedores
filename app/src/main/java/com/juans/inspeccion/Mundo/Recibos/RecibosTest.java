package com.juans.inspeccion.Mundo.Recibos;

import android.content.res.Resources;

/**
 * Created by Juan on 08/09/2015.
 */
public class RecibosTest {
    public static void Test(Resources resources)
    {
        String testString="HABIA UNA VEZ UNA RANA QUE TOMABA    CAFE.Nos gustaría conocer tu opinión. Responde unas preguntas sobre tu sección de noticias.";
        ComandosImpresora comandos=new ComandosImpresora();

        int[] cordPrrafo=comandos.escribirParrafo(testString,ComandosImpresora.S3,40,10,false,200);
        comandos.ponerRectanguloCordenadas(cordPrrafo[0],cordPrrafo[1]);
        comandos.escribir("inspector juan",ComandosImpresora.S3,10,true);
        comandos.finalizar();
        Impresora.getInstance().enviarComandosImpresora(comandos.darListaComandos(), resources);

    }
}
