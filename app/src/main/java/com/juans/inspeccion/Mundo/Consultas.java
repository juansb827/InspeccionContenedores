package com.juans.inspeccion.Mundo;

import android.content.res.Resources;
import android.util.Log;
import android.widget.SimpleAdapter;

import com.juans.inspeccion.ConnectionException;
import com.juans.inspeccion.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by juan__000 on 11/3/2014.
 */
public class Consultas {

    public final static int  FECHA_ENTERA=5;
    public final static int  FECHA_HORA=3;
    public final static int  FECHA_MINUTOS=4;
    public final static int  FECHA_MOSTRAR=6;

    public final static int  FECHA_DIA=0;
    public final static int  FECHA_MES=1;
    public final static int  FECHA_ANIO=2;
    public final static int ERROR_REGISTRO_DUPLICADO=2627;


    public static String[] darFecha()
    {
        return DAO.getInstance().darFecha();
    }

    public static String darNombreAgente(String linea) throws ConnectionException {
        String sentencia="SELECT TER_RAZONS FROM TERCEROS WHERE TERCERO=(select TOP  1 cnitagente from TBCLIENTESLNA where cctelna='"+linea+"')";
        return DAO.getInstance().consulta_1_Dato(sentencia);}

    public  static Object[] contenedorEstaEnPatio(String linea,String codigoContenedor) throws ConnectionException {
        boolean esta=false;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");


        String sentencia="SELECT * FROM FC26_ULTIMAENTRADACONTENEDOR ("+"'"+linea+"'"+","+"'"+codigoContenedor+"'"+")";

        HashMap<String,String> consulta= DAO.getInstance().generarHashConConsulta(sentencia);
        if (consulta==null) return null;
        try {
            String ColEntrada="DFECENTCNTR";
            String ColSalida="DFECSALCNTR";
            String entrada=consulta.get(ColEntrada);
            String salida=consulta.get(ColSalida);


            java.util.Date fechaEntrada = formatter.parse(entrada);
            java.util.Date fechaSalida = formatter.parse(salida);
            if( fechaEntrada.compareTo(fechaSalida) > 0   )
            {
                esta=true;
            }
            else
            {
                esta=false;
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        return new Object[]{esta, consulta};

    }

    public static ArrayList<HashMap<String, String>> darInfoBooking(String linea, String numBooking) throws ConnectionException {

            String sentencia = "SELECT CTIPOCNTR,CTAMCNTR,CESTFINCNTR, NSOLICITADOS " +
                "FROM TBDETBOOKCNTR" +
                " WHERE CCTELNA='" + linea + "'   AND CNUMBOOKING='" + numBooking + "'";

        return DAO.getInstance().generarHashConsultaLista(sentencia);

    }

    public static String consultaStockDetallada(String linea)
    {
       String sentencia= " EXECUTE sp_m26stockcontenedorestablets "+"'"+linea+"'";
                      return sentencia;
    }


    public static String esEstadoApto(String estado)
    {
        return "";
    }

    public static String consultaInfoDeCabecera(String numDoc)
    {
        String sentencia=" SELECT a.*,his.cestadocntr as cestadoactual, l.Cdesctelna,t.ter_razons as ter_razons, ter.ter_razons as ccliente, ins.Cnominspector"+
        " FROM TBCABMOVPATIO A left join tbhistocntr  his on a.cctelna = his.cctelna"+
        " and a.ccodcntr = his.ccodcntr and a.nnumdocori=his.nnumdocori"+
        " left join TBCLIENTESLNA l ON l.Cctelna=a.cctelna"+
        " left join terceros t on t.tercero=l.Cnitagente"+
        " left join terceros ter on ter.tercero=a.cnitcliente"+
        " left join TBINSPECTORES ins on ins.Ccodinspector=a.Ccodinspector"+
        " where  a.nnumdoc="+numDoc;

        return sentencia;
    }

    public static String consultaNumDocDeContenedor(String codCntr)
    {
        String sentencia="SELECT Nnumdocori,nnumdocsal FROM TBHISTOCNTR A WHERE CCODCNTR='"+codCntr+"'  AND"+
            " DFECENTCNTR=(SELECT MAX(DFECENTCNTR) FROM TBHISTOCNTR WHERE CCODCNTR='"+codCntr+"')";
        return sentencia;
    }

    public static String consultaDaniosDeDocumento(String numDoc)
    {
        return "SELECT *  FROM TBDETMOVPATIO WHERE NNUMDOC ="+numDoc;
    }

    public static HashMap<String,String> buscar(String sentencia) throws ConnectionException {
        return DAO.getInstance().generarHashConConsulta(sentencia);
    }
    public static String consultaDatosContenedor(String codCntr)
    {

        return   "select  * from TBCONTENEDORES where ccodcntr='"+codCntr+"'";

    }

    public static String  darNombreTerceroConNit(String clienteNit) throws ConnectionException
    {
        return  DAO.getInstance().consulta_1_Dato(DAO.NOMBRE_TERCERO_CON_NIT + "'" + clienteNit + "'");


    }

    public static String consutlaHistoriCntr(String linea,String codigoContenedor)
    {
        return "SELECT CTIPDOCORI,NNUMDOCORI,CESTADOCNTR,CCTELNA FROM FC26_ULTIMAENTRADACONTENEDOR ("+"'"+linea+"'"+","+"'"+codigoContenedor+"'"+")";
    }


    public static  HashMap<String,String>  darInfoEmpresa() throws ConnectionException {
        return DAO.getInstance().generarHashConConsulta("SELECT * FROM TB99CONFIGTABLES");
    }

    public static boolean haySaldoBooking(String linea,String booking) throws ConnectionException
    {
        boolean haySaldo=false;
        String saldoStr="";
        try {
            saldoStr = DAO.getInstance().consulta_1_Dato("EXEC sp_m26saldobooking " + "'" + linea + "','" + booking + "'");

            int saldo = Integer.parseInt(saldoStr);
            haySaldo= saldo>0;
        }catch (Exception e)
        {

            e.printStackTrace();
            if(saldoStr==null) throw new ConnectionException();
        }
        return haySaldo;

    }

    public static boolean contenedorPerteneceBooking(String linea,String booking,String codcntr) throws ConnectionException
    {
        String sentencia="exec sp_m26saldobookingxtipocntr '"+linea+"', '"+booking+"','"+codcntr+"'";
        String pertenenceStr="";
        boolean pertenece=false;
        try{
            pertenenceStr=DAO.getInstance().consulta_1_Dato(sentencia);
            pertenece=Integer.parseInt(pertenenceStr)!=0;



        }catch (Exception e){
            e.printStackTrace();
            if(pertenenceStr==null) throw new ConnectionException();
        }
        return pertenece;
    }

    public static String darSiglaIso(String tipo,String tamcntr) throws ConnectionException {
        String sentencia="SELECT CSIGLAISO  FROM TB26TIPOISOCNTR  WHERE CTIPOCNTR='"+tipo+"'  AND CTAMCNTR='"+tamcntr+"'";
        return DAO.getInstance().consulta_1_Dato(sentencia);
    }

    public static String cambiarContenedorTurno(String codigoCntr, HashMap<String,String> llaves)
    {
        String sentencia="UPDATE TBTURNOS SET CCODCNTR='"+codigoCntr+"' WHERE "+DAO.agregarCondiciones(llaves);
        return sentencia;
    }

    public static String darTurnosRestantes(HashMap<String,String> llaves,String tipoTurno)
    {
        String sentencia="SELECT NTURNO, CCODCNTR,CPLACA,CCTELNA FROM TBTURNOS WHERE "+DAO.agregarCondiciones(llaves)+
                " AND NATENDIDO='0' AND CTIPOTURNO='"+tipoTurno+"' ORDER BY DFECHALOG";

        return sentencia;
    }

    //**
    // Danios
    public static HashMap<String,String> darCostosDanios(Resources r,HashMap<String,String> mapaDanios,HashMap<String,String> infoCabecera) throws ConnectionException {

        String cctelna=infoCabecera.get(r.getString(R.string.CCTELNA));
        String ccodubi=mapaDanios.get(r.getString(R.string.CCODUBI));
        String ccodele=mapaDanios.get(r.getString(R.string.CCODELE));
        String ccoddan=mapaDanios.get(r.getString(R.string.CCODDAN));
        String ccodmet=mapaDanios.get(r.getString(R.string.CCODMET));
        String tipoCalculo=mapaDanios.get(r.getString(R.string.CTIPCALCULO));
        String largo=mapaDanios.get(r.getString(R.string.NLARGO));
        String ancho=mapaDanios.get(r.getString(R.string.NANCHO));
        String metodoUsaTamano=mapaDanios.get(r.getString(R.string.NTARTAMANO));
        String metodoUsaUbicacion=mapaDanios.get(r.getString(R.string.NOBLUBICA));
        String tamanoCont=infoCabecera.get(r.getString(R.string.CTAMCNTR));
        boolean usaDimensiones=tipoCalculo.equals("D");
        boolean usaTamano=metodoUsaTamano.equals("1");
        boolean usaUbicacion=metodoUsaUbicacion.trim().equals("1");
        int calculo=0;
        int tamano=0;
        if(usaDimensiones) calculo = Integer.parseInt(largo) * Integer.parseInt(ancho);
        if(usaTamano)    tamano = Integer.parseInt(tamanoCont);





        String sentencia="SELECT TOP 1 *  FROM TBCOSREPXLINEA WHERE CCTELNA='"+cctelna+ "' AND CCODELE='"+ccodele+"' AND CCODMET='"+ccodmet+"'";
        if(usaUbicacion)
        {

            sentencia+=" AND CCODUBI='"+ccodubi+"' AND CCODDAN='"+ ccoddan+"'";
        }
        if(usaDimensiones)
        {
            sentencia+=" AND "+calculo+">=NRANGOINI  and "+calculo+"<=NRANGOFIN  ";
        }
        if(usaTamano)
        {
            sentencia+=" AND CTAMCNTR='"+tamano+"'";
        }

        sentencia+=" ORDER BY NRANGOFIN";
        Log.e("ConsultaPrecios", sentencia);
        return DAO.getInstance().generarHashConConsulta(sentencia);


    }

    public static String countDocsDelDia(HashMap<String,String> info,Resources res)
    {
        String dia=info.get(res.getString(R.string.NDIA));
        String mes=info.get(res.getString(R.string.NMES));
        String ano=info.get(res.getString(R.string.NANO));
        String sentencia="SELECT count(*) FROM TBCABMOVPATIO WHERE DATEDIFF(dd, DFECMVTO, '"+ano+mes+dia+"') = 0";
        return sentencia;
    }





}

