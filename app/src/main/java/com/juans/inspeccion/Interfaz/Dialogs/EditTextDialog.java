package com.juans.inspeccion.Interfaz.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.juans.inspeccion.Mundo.FilaEnConsulta;
import com.juans.inspeccion.Mundo.Formularios;
import com.juans.inspeccion.R;

import java.lang.reflect.Field;


/**
 * Created by juan__000 on 10/16/2014.
 */
public class EditTextDialog extends DialogFragment {
    private static  String titulo;
    private static String mensaje;
    public final static int TEXTO=0;
    public final static int NUMERO=1;


   private static int iniciadoPor;
    private static String textoInicial;
    private  static Formularios.DataPass dataPass;
    private static int inputType;
    private static String placeHolder;


    public static EditTextDialog newInstance(int _inputType,int _iniciadoPor,String _textoInicial, Formularios.DataPass _dp) {
        mensaje=null;
        titulo=null;

        iniciadoPor=_iniciadoPor;
        EditTextDialog dialog=new EditTextDialog();
        textoInicial=_textoInicial;
        dataPass=_dp;
        inputType=_inputType;
        return dialog;

    }

    public static EditTextDialog newInstance(int _inputType, Formularios.DataPass _dp,String _titulo,String _mensaje, int _iniciadoPor) {
        iniciadoPor=_iniciadoPor;
        EditTextDialog dialog=new EditTextDialog();
        textoInicial="";
        dataPass=_dp;
        inputType=_inputType;
        mensaje=_mensaje;
        titulo=_titulo;


        return dialog;

    }



    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }



    public static void setPlaceHolder(String _placeHolder)
    {
     placeHolder=_placeHolder;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {


        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        builder.setTitle(titulo);
        builder.setMessage(mensaje);
        final FilaEnConsulta filaEnConsulta=new FilaEnConsulta(2);


        final EditText input = new EditText(getActivity().getBaseContext());

        input.setInputType(inputType);
        input.setText(textoInicial);
        input.setTextColor(getResources().getColor(R.color.Negro));
        input.setSelection(input.getText().length());
        input.setBackgroundColor(getResources().getColor(R.color.Blanco));
        input.setHint(placeHolder);
        placeHolder=null;
        try {
            Field f=TextView.class.getDeclaredField("mCursorDrawableRes");
            f.setAccessible(true);
            f.set(input,R.drawable.cursor);

        } catch (Exception e) {
            e.printStackTrace();
        }

        input.setCursorVisible(true);






        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                filaEnConsulta.setDato(0,input.getText().toString());


                dismiss();
                return false;
            }
        });


        builder.setView(input);
        filaEnConsulta.setDato(0,input.getText().toString());
        input.setImeActionLabel("OK", EditorInfo.IME_ACTION_SEND);

        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent keyEvent) {
                if(keyEvent!=null && keyEvent.getAction()!=keyEvent.getAction()) return false;
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                        getActivity().INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
                filaEnConsulta.setDato(0,input.getText().toString());
                dataPass.onDataReceive(filaEnConsulta, iniciadoPor);
                EditTextDialog.this.dismiss();
                return true;

            }
        });


        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                filaEnConsulta.setDato(0,input.getText().toString());

                dataPass.onDataReceive(filaEnConsulta, iniciadoPor);


            }

                }).setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Do nothing.

            }
        });


        Dialog dialog=builder.create();

        dialog.setCanceledOnTouchOutside(false);


        return dialog;
    }
}
