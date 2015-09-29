package com.juans.inspeccion.Interfaz;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.text.TextUtils;
import android.widget.Toast;

import com.juans.inspeccion.CustomView.CustomView;
import com.juans.inspeccion.Interfaz.Dialogs.MyProgressDialog;
import com.juans.inspeccion.Interfaz.Dialogs.SimpleDialog;
import com.juans.inspeccion.Mundo.Formularios;
import com.juans.inspeccion.Varios;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Juan on 13/05/2015.
 */
public class ActivityHelper {
    Activity activity;
    public ActivityHelper(Activity activity) {
        this.activity=activity;
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
    }

    public void sePerdioLaConexion()
    {
        SimpleDialog dialog=SimpleDialog.newInstance("Sin Conexion","Se perdio la conexion con la base de datos, intente nuevamenete");
        dialog.show(activity.getFragmentManager(), "noCon");
    }

    public void actualizarInterfaz(ArrayList<CustomView> listaCampos, HashMap<String,String> mapaCampos) {

        if (listaCampos != null) {
            for (int i = 0; i < listaCampos.size(); i++) {
                CustomView cw = listaCampos.get(i);
                String texto=mapaCampos.get(cw.getNombreCampo());
                if(cw.getMyInputType()==CustomView.DIALOGO_CON_DATE_PICKER && !TextUtils.isEmpty(texto) )
                {
                    texto= Formularios.dateFromDbParser(texto,"/",false);
                }
                cw.setTexto(texto);
            }
        }

    }

    public void actualizarMapaCampos(ArrayList<CustomView> listaCampos,HashMap<String, String> mapaCampos)
    {
        for (int i = 0; i < listaCampos.size(); i++) {
            CustomView cw = listaCampos.get(i);
            mapaCampos.put(cw.getNombreCampo(), cw.getTexto());
        }
    }

    ProgressDialog pDialog;
    public ProgressDialog mostrarProgressDialog()
    {
         pDialog= new MyProgressDialog(activity, false);
        pDialog.setTitle("");
        pDialog.setMessage("Un momento por favor");
        pDialog.setIndeterminate(false);
        pDialog.setCanceledOnTouchOutside(false);
        pDialog.show();

        return pDialog;
    }

    public void cerrarProgressDialog()
    {
            if(pDialog!=null && pDialog.isShowing())
                pDialog.dismiss();

    }

}
