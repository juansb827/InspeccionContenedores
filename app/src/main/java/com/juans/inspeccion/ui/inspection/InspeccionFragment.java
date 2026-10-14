package com.juans.inspeccion.ui.inspection;

/**
 * Created by juan__000 on 10/11/2014.
 */
public interface InspeccionFragment {


    public Object guardar();
    public Object actualizarMapa();

    public void limpiar();
    public void recibirInfo(Object... object);

}
