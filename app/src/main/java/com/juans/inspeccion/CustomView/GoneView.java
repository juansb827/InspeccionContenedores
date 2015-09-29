package com.juans.inspeccion.CustomView;

import java.io.Serializable;

/**
 * Created by juan__000 on 9/20/2014.
 */
public class GoneView implements CustomView, Serializable {
    String vieneDe;
    String vaPara;
    String nombreCampo;
    String llaveEn;
    String texto;

    public GoneView(String _vieneDe,String _vaPara, String _nombreCampo,String _llaveEn)
    {
        vieneDe=_vieneDe;
        vaPara=_vaPara;
        nombreCampo=_nombreCampo;
        llaveEn=_llaveEn;
        texto="";


    }
    @Override
    public void setTexto(String _texto)
    {
        texto=_texto;
    }

    @Override
    public boolean esObligatorio() {
        return false;
    }

    @Override
    public String getNombreLista() {
        return null;
    }

    @Override
    public void setNombreLista(String lista) {

    }

    @Override
    public String getTexto()
    {
        return texto;
    }



    @Override
    public String getVieneDe() {
        return vieneDe;
    }

    @Override
    public String getVaPara() {
        return vaPara;
    }

    @Override
    public String getNombreCampo() {
        return nombreCampo;
    }

    @Override
    public boolean esLlave(String nombreTabla) {
        boolean respuesta=false;
//        if(llaveEn!=null) {
//            respuesta=llaveEn.contains(nombreTabla);
//        }
        return respuesta;
    }

    @Override
    public String getNombreCampoDestino() {
        return "";
    }

    @Override
    public void limpiar()
    {
        texto="";
    }

    @Override
    public int getMyInputType() {
        return 0;
    }

    @Override
    public int getSecondaryInputType() {
        return 0;
    }


}
