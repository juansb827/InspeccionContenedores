package com.juans.inspeccion.CustomView;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.util.AttributeSet;
import android.widget.EditText;


import com.juans.inspeccion.R;

import java.io.Serializable;

/**
 * Created by juan__000 on 8/24/2014.
 */
public class MyEditText extends EditText implements CustomView,Serializable {

    private String vieneDe;
    private String nombreCampo;
    private String llaveEn="";
    private String vaPara;
    private String nombreCampoDestino;
    private int myInputType;
    private int secondaryType;
    private boolean obligatorio;
    private int estado=0;


    public MyEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
             TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.MyEditText,
                0, 0);

        try {
             vieneDe = a.getString( R.styleable.MyEditText_vieneDeTabla);
            nombreCampo = a.getString(R.styleable.MyEditText_nombreCampo);
            llaveEn= a.getString(R.styleable.MyEditText_llaveEn);
            vaPara=a.getString(R.styleable.MyEditText_vaParaTabla);
            nombreCampoDestino=a.getString(R.styleable.MyEditText_nombreCampo_Destino);
            myInputType =a.getInteger(R.styleable.MyEditText_myInputType, 0);
            secondaryType=a.getInteger(R.styleable.MyCheckBox_secondaryInputType, 0);
            obligatorio=a.getBoolean(R.styleable.MyEditText_obligatorio, false);


        } finally {
            a.recycle();
        }

    }
    public MyEditText(Context context) {
        super(context);

    }

    public MyEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

    }

    @Override
    public String getVieneDe()
    {
        return vieneDe==null?"":vieneDe;
    }

    @Override
    public String getVaPara() {
        return vaPara==null?"":vaPara;
    }
    @Override
    public String getNombreCampo()
    {
        return nombreCampo==null?"":nombreCampo ;
    }

    @Override
    public boolean esLlave(String nombreTabla)
    {
        boolean respuesta=false;
        if(llaveEn!=null) {
             respuesta=llaveEn.contains(nombreTabla);
         }
        return respuesta;
    }

    @Override
    public String getNombreCampoDestino() {
        return nombreCampoDestino==null?getNombreCampo():nombreCampoDestino;
    }


    @Override
    public String getTexto() {
        Editable respuesta=super.getText();
        return respuesta==null? "":respuesta.toString();
    }

    @Override
    public void limpiar()
    {
        super.setText("");
    }

    @Override
    public int getMyInputType() {
        return myInputType;
    }

    public void setMyInputType(int myInputType) {this.myInputType=myInputType;}

    @Override
    public int getSecondaryInputType() {
        return secondaryType;
    }

    @Override
    public void setTexto(String texto) {
        super.setText(texto);
    }

    @Override
    public boolean esObligatorio() {
        return obligatorio;
    }

    @Override
    public void setObligatorio(boolean _obligatorio) {
    obligatorio=_obligatorio;
    }

    @Override
    public String getNombreLista() {
        return null;
    }

    @Override
    public void setNombreLista(String lista) {

    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }
}
