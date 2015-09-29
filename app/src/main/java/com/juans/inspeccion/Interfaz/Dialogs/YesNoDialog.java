package com.juans.inspeccion.Interfaz.Dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.provider.ContactsContract;

import com.juans.inspeccion.Mundo.FilaEnConsulta;
import com.juans.inspeccion.Mundo.Formularios;

/**
 * Created by Juan on 10/03/2015.
 */
public class YesNoDialog extends DialogFragment {

        static String titulo;
        static String mensaje;
        static String positivo;
        static String negativo;
        static int iniciadoPor;
        static Formularios.DataPass dataPass;
        //Use this one when calling the dialog from a fragment otherwise onDataReceive from fragment will not work ,it will call the one from the activity
    public static YesNoDialog newInstance(String _titulo,String _mensaje,String _positivo,String _negativo, Formularios.DataPass _dataPass,int _iniciadoPor)
    {

        YesNoDialog dig= newInstance(_titulo, _mensaje, _positivo, _negativo, _iniciadoPor);
        dataPass=_dataPass;

        return dig;
    }

        public static YesNoDialog newInstance(String _titulo,String _mensaje,String _positivo,String _negativo, int _iniciadoPor)
        {
            titulo=_titulo;
            mensaje=_mensaje;
            positivo=_positivo;
            negativo=_negativo;
            iniciadoPor=_iniciadoPor;
            dataPass=null;

            return new  YesNoDialog();
        }



        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);

        }



        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            if(dataPass==null) dataPass= (Formularios.DataPass) getActivity();

            Dialog dialog= new AlertDialog.Builder(getActivity())
                    .setTitle(titulo)
                    .setMessage(mensaje)
                    .setPositiveButton(positivo, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            dataPass.onDataReceive(true, iniciadoPor);
                        }
                    })
                    .setNegativeButton(negativo, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dataPass.onDataReceive(false, iniciadoPor);
                        }
                    })

                    .create();

            dialog.setCanceledOnTouchOutside(false);
            return dialog;
        }

}
