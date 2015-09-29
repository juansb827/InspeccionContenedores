package com.juans.inspeccion.Interfaz.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.juans.inspeccion.Mundo.FilaEnConsulta;
import com.juans.inspeccion.Mundo.Formularios;

/**
 * Created by juan__000 on 11/1/2014.
 *
 */

public class ListViewDialog extends DialogFragment {
    static String titulo;
    static String[] lista;
    static int iniciadoPor;
    static Formularios.DataPass dataPass;

    public static ListViewDialog newInstance(String _titulo,String[] _lista,int _iniciadoPor,Formularios.DataPass _dataPass)
    {
        ListViewDialog listViewDialog=new ListViewDialog();
        titulo=_titulo;
        lista=_lista;
        iniciadoPor=_iniciadoPor;
        dataPass=_dataPass;
        return listViewDialog;
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(titulo)
                .setItems(lista, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {


                       dataPass.onDataReceive(lista[which], iniciadoPor);
                    }
                });
        return builder.create();
    }
}
