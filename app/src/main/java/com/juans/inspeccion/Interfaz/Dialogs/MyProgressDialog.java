package com.juans.inspeccion.Interfaz.Dialogs;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;

import com.juans.inspeccion.Mundo.Formularios;

/**
 * Created by Juan on 12/03/2015.
 */
public class MyProgressDialog extends ProgressDialog {
    public final static int PROGRESS_DIALOG =420;
    static boolean closeParent;
    static String titulo;
    static String mensaje;


    public static MyProgressDialog newInstance(Context c,String _titulo,String _mensaje,boolean _closeParent){
        closeParent=_closeParent;
        titulo=_titulo;
        mensaje=_mensaje;
        MyProgressDialog pDialog = new MyProgressDialog(c,closeParent);
        pDialog.setTitle(titulo);
        pDialog.setMessage(mensaje);
        pDialog.setIndeterminate(false);
        pDialog.setCanceledOnTouchOutside(false);
        return pDialog;
    }

    public static MyProgressDialog lastInstance(Context c)
    {
        MyProgressDialog pDialog = new MyProgressDialog(c,closeParent);
        pDialog.setTitle(titulo);
        pDialog.setMessage(mensaje);
        pDialog.setIndeterminate(false);
        pDialog.setCanceledOnTouchOutside(false);
        return pDialog;
    }
    public MyProgressDialog(Context context, boolean closeParent) {
        super(context);
        setOwnerActivity((Activity) context);
        this.closeParent=closeParent;
    }



    @Override
    public void onBackPressed() {
        if(closeParent) {
            Formularios.DataPass dp = (Formularios.DataPass) getOwnerActivity();
            dp.onDataReceive(false, PROGRESS_DIALOG);
        }
        else
        {
            super.onBackPressed();
        }
    }

}
