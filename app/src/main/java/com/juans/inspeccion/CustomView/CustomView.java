package com.juans.inspeccion.CustomView;

/**
 * Created by juan__000 on 9/15/2014.
 */
public interface CustomView {
    public static int NORMAL=0;
    public static int DIALOGO_CON_LISTA=1;
    public static int DIALOGO_CON_EDIT_TEXT=2;
    public static int DIALOGO_CON_DATE_PICKER=3;


    public static int ESTADO_NORMAL=0;
    public static int ESTADO_ERROR=0;
    public static int ESTADO_WARNING=0;
    public static int ESTADO_BIEN=0;



    public String getVieneDe();
    public String getVaPara();
    public String getNombreCampo();
    public boolean esLlave(String nombreTabla);
    public String getNombreCampoDestino();
    public String getTexto();
    public void limpiar();
    public int getMyInputType();
    public int getSecondaryInputType();
    public void setTexto(String texto);
    public boolean esObligatorio();
    public void setObligatorio(boolean obligatario);
    public String getNombreLista();
    public void setNombreLista(String lista);




}
