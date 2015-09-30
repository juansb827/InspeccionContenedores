package com.juans.inspeccion.CustomView;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.CheckBox;

import com.juans.inspeccion.R;

/**
 * Created by juan__000 on 9/25/2014.
 */
public class MyCheckBox extends CheckBox implements CustomView {


    private String vieneDe;
    private String nombreCampo;
    private String llaveEn="";
    private String vaPara;
    private String nombreCampoDestino;

    public MyCheckBox (Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.MyCheckBox,
                0, 0);

        try {
            vieneDe = a.getString( R.styleable.MyCheckBox_vieneDeTabla);
            nombreCampo = a.getString(R.styleable.MyCheckBox_nombreCampo);
            llaveEn= a.getString(R.styleable.MyCheckBox_llaveEn);
            vaPara=a.getString(R.styleable.MyCheckBox_vaParaTabla);
            nombreCampoDestino=a.getString(R.styleable.MyEditText_nombreCampo_Destino);

        } finally {
            a.recycle();
        }

    }
    public MyCheckBox (Context context) {
        super(context);

    }

    public MyCheckBox (Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

    }

    @Override
    public String getVieneDe()
    {
        return vieneDe;
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
        boolean checked=super.isChecked();
        String valor="0";
        if (checked)
        {
            valor="1";
        }
        return valor;

    }

    @Override
    public void limpiar()
    {
        super.setChecked(false);
    }

    @Override
    public int getMyInputType() {
        return 0;
    }

    @Override
    public int getSecondaryInputType() {
        return 0;
    }

    @Override
    public void setTexto(String texto) {
        if (TextUtils.isEmpty(texto)) return;
        if (texto.equals("0"))
        {
         super.setChecked(false);
        }
        else if(texto.equals("1") )
        {
         super.setChecked(true);
        }
    }

    @Override
    public boolean esObligatorio() {
        return false;
    }

    @Override
    public void setObligatorio(boolean obligatario) {
        return ;
    }

    @Override
    public String getNombreLista() {
        return null;
    }

    @Override
    public void setNombreLista(String lista) {

    }


}
