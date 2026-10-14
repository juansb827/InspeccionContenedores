package com.juans.inspeccion.data.remote.sqlserver;

/**
 * Created by juan__000 on 11/23/2014.
 */
public class Contenedores {

    public final static String ESTA_EN_PATIO="ESTA_EN_PATIO";

    public static boolean contenedorEstaEnPatio(String linea,String  codcontenedor)
    {
        Object ob=DAO.getInstance().contenedorEstaEnPatio
                ( linea, codcontenedor)[0];
        return (Boolean)ob;
    }
}
