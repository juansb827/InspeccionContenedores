package com.juans.inspeccion.Interfaz.Dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Created by Juan on 10/03/2015.
 */
public class SimpleDialog extends DialogFragment {

    static String text;
    static String titulo;

    public static SimpleDialog newInstance(String _titulo,String _text)
    {
        text=_text;
        titulo=_titulo;
        return new SimpleDialog();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle(titulo)
                .setMessage(text)
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {


                    }
                })

                .create();
    }

}
