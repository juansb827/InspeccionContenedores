package com.juans.inspeccion.Mundo.Data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import android.content.res.Resources;
import android.text.TextUtils;
import android.util.Log;

import com.juans.inspeccion.ConnectionException;
import com.juans.inspeccion.CustomView.CustomView;
import com.juans.inspeccion.DataBaseException;
import com.juans.inspeccion.DiccionariosException;
import com.juans.inspeccion.Interfaz.InspeccionActivity;
import com.juans.inspeccion.InvalidOperationException;
import com.juans.inspeccion.Mundo.ColumnasTablas;
import com.juans.inspeccion.Mundo.Consultas;
import com.juans.inspeccion.Mundo.Contenedores;
import com.juans.inspeccion.Mundo.DAO;
import com.juans.inspeccion.Mundo.Danio;
import com.juans.inspeccion.Mundo.DaniosManager;
import com.juans.inspeccion.Mundo.FilaEnConsulta;
import com.juans.inspeccion.Mundo.Formularios;
import com.juans.inspeccion.Mundo.Inspeccion;
import com.juans.inspeccion.Mundo.Usuario;
import com.juans.inspeccion.R;

import org.w3c.dom.Text;

public class InspeccionDataSource  {

    public static final String TABLE_NAME = "test";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    // Database creation sql statement
    public static final String CREATE_COMMAND = "create table " + TABLE_NAME
            + "(" + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_NAME + " text not null);";
    public InspeccionDataSource() {


        // TODO Auto-generated constructor stub
    }

    public void re_insert(ArrayList<CustomView> listaCampos, Inspeccion inspeccion, Resources res) throws DiccionariosException, ConnectionException, DataBaseException
    {
        HashMap<String,String> info=inspeccion.getInformacion();
        ArrayList<Danio> danios=inspeccion.getDaniosManager().getListaDanios();

        String destino=res.getString(R.string.TBCABMOVPATIO);

        for(int i=0;i<listaCampos.size();i++)
        {

            CustomView cw=listaCampos.get(i);
            info.put(cw.getNombreCampoDestino(), cw.getTexto());
        }

        HashMap<String, String> columnas = ColumnasTablas.getInstance().darTabla(destino);

        String tbDanios=res.getString(R.string.TBDETMOVPATIO);


        String numdoc=inspeccion.getInformacion().get( res.getString(R.string.NNUMDOC));
                ArrayList<String> sentencias=new ArrayList<>();
        HashMap<String,String> condiciones=new HashMap<>();
        condiciones.put(res.getString(R.string.NNUMDOC),numdoc);
        String borrarCabecera=DAO.crearSentenciaDELETE(destino, condiciones   );
        String sentCabecera=DAO.getInstance().crearSentenciaINSERT(destino, info, columnas);
        sentencias.add(borrarCabecera);
        sentencias.add(sentCabecera);

        if(inspeccion.getTipoInspeccion().equals(InspeccionActivity.ENTRADA)) {
            String borrarDanios=DAO.crearSentenciaDELETE(res.getString(R.string.TBDETMOVPATIO),condiciones);
            sentencias.add(borrarDanios);
            sentencias.addAll(DAO.getInstance().generarINSERTSlista(tbDanios, DaniosManager.darListaSoloDetalles(danios)));
        }
        DAO.getInstance().ejecutarComoTransaccion(sentencias);

    }




    public void insert(ArrayList<CustomView> listaCampos, Inspeccion inspeccion, Resources res) throws DiccionariosException, ConnectionException, DataBaseException {

        HashMap<String,String> info=inspeccion.getInformacion();
        ArrayList<Danio> danios=inspeccion.getDaniosManager().getListaDanios();
        String numdoc=info.get(res.getString(R.string.NNUMDOC));
        String destino=res.getString(R.string.TBCABMOVPATIO);

        for(int i=0;i<listaCampos.size();i++)
        {

            CustomView cw=listaCampos.get(i);
            String texto=cw.getTexto();
            if(cw.getMyInputType()==CustomView.DIALOGO_CON_DATE_PICKER)
            {
                texto= Formularios.dateToDbParser(texto);
            }
            info.put(cw.getNombreCampoDestino(), texto);

        }
        //Al nousar turno, nnumdoc es =0 en la interfaz ,se perderia si no se hace esto
        if(!inspeccion.isUsaTurno())
        {
            info.put(res.getString(R.string.NNUMDOC),numdoc);
        }

        HashMap<String, String> columnas = ColumnasTablas.getInstance().darTabla(destino);

        String tbDanios=res.getString(R.string.TBDETMOVPATIO);
        String sentCabecera=DAO.getInstance().crearSentenciaINSERT(destino, info, columnas);

        ArrayList<String> sentencias=new ArrayList<>();
        sentencias.add(sentCabecera);

        if(inspeccion.getTipoInspeccion().equals(InspeccionActivity.ENTRADA)) {
            sentencias.addAll(DAO.getInstance().generarINSERTSlista(tbDanios, DaniosManager.darListaSoloDetalles(danios)));
        }
         DAO.getInstance().ejecutarComoTransaccion(sentencias);


    }

    public void  insertContenedor(ArrayList<CustomView> listaCampos,Inspeccion inspeccion,HashMap<String,String> codigoOriginal,Resources res) throws DiccionariosException, ConnectionException, DataBaseException {
        HashMap<String,String> datosContenedor=inspeccion.getDatosContenedor();

        String destino=res.getString(R.string.TBCONTENEDORES);
        for(int i=0;i<listaCampos.size();i++)
        {

            CustomView cw=listaCampos.get(i);
            String texto= cw.getTexto();
            if(cw.getMyInputType()==CustomView.DIALOGO_CON_DATE_PICKER)
            {
                texto= Formularios.dateToDbParser(texto);
            }
            datosContenedor.put(cw.getNombreCampoDestino(),texto);
        }
        HashMap<String, String> columnas = ColumnasTablas.getInstance().darTabla(destino);

        String cntrOriginal=codigoOriginal.remove(res.getString(R.string.CCODCNTR));
        String cntrFinal=datosContenedor.get(res.getString(R.string.CCODCNTR));
        //Si el usuario cambio el codigo del contenedor hay que actualizarlo en la tabla turnos tambien
        String sentenciaTurnos=null;

        if(inspeccion.isUsaTurno() && !cntrOriginal.equals(cntrFinal))
        {
            sentenciaTurnos=Consultas.cambiarContenedorTurno(cntrFinal, generarCondicionTurnos( inspeccion.getInformacion(),res ) );
        }
        String sentenciaContenedores=DAO.crearSentenciaINSERT(destino,datosContenedor,columnas);
        if(sentenciaTurnos!=null)
        {
            ArrayList<String> sentencias=new ArrayList();
            sentencias.add(sentenciaTurnos);
            sentencias.add(sentenciaContenedores);
            DAO.getInstance().ejecutarComoTransaccion(sentencias);
        }
        else
        {
            DAO.getInstance().ejecutarSQL(sentenciaContenedores);
        }





    }

    public void updateContenedor(ArrayList<CustomView> listaCampos,Inspeccion ins,HashMap<String,String> codigoOriginal,Resources res) throws DiccionariosException, ConnectionException, DataBaseException {

        HashMap<String,String> datosContenedor=ins.getDatosContenedor();
        String destino=res.getString(R.string.TBCONTENEDORES);
        for(int i=0;i<listaCampos.size();i++)
        {
            CustomView cw=listaCampos.get(i);
            String texto= cw.getTexto();
            if(cw.getMyInputType()==CustomView.DIALOGO_CON_DATE_PICKER)
            {
                texto= Formularios.dateToDbParser(texto);
            }
            datosContenedor.put(cw.getNombreCampoDestino(), texto);
        }
        HashMap<String, String> columnas = ColumnasTablas.getInstance().darTabla(destino);
        String cntrOriginal=codigoOriginal.remove(res.getString(R.string.CCODCNTR));
        String cntrFinal=datosContenedor.get(res.getString(R.string.CCODCNTR));
        //Si el usuario cambio el codigo del contenedor hay que actualizarlo en la tabla turnos tambien
        String sentenciaTurnos=null;
        if(!cntrOriginal.equals(cntrFinal))
        {
            sentenciaTurnos=Consultas.cambiarContenedorTurno(cntrFinal, generarCondicionTurnos( ins.getInformacion(),res ) );
        }
        String sentenciaContenedores=DAO.crearSentenciaUPDATE(destino,listaCampos,datosContenedor,codigoOriginal,columnas);
        if(sentenciaTurnos!=null)
        {
            ArrayList<String> sentencias=new ArrayList();
            sentencias.add(sentenciaTurnos);
            sentencias.add(sentenciaContenedores);
            DAO.getInstance().ejecutarComoTransaccion(sentencias);
        }
        else
        {
            DAO.getInstance().ejecutarSQL(sentenciaContenedores);
        }



    }
    //TODO:  Usar el metadata y las primary key de las tablas en vez hacer esto
    private  HashMap<String,String> generarCondicionTurnos(HashMap<String,String> mapa,Resources res)
    {
       HashMap<String,String> condiciones=new HashMap<>();
       String nano=res.getString(R.string.NANO);
       condiciones.put(nano, mapa.get(nano));
        String nmes=res.getString(R.string.NMES);
        condiciones.put(nmes, mapa.get(nmes));
        String ndia=res.getString(R.string.NDIA);
        condiciones.put(ndia, mapa.get(ndia));
        String nturno=res.getString(R.string.NTURNO);
        condiciones.put(nturno, mapa.get(nturno));
        return  condiciones;

    }

    public Inspeccion read(String codCntr, String tipoInspeccion,String tipoOperacion,Resources res) throws ConnectionException, InvalidOperationException, DiccionariosException, DataBaseException {


        HashMap<String, String> documentos = DAO.getInstance().generarHashConConsulta(Consultas.consultaNumDocDeContenedor(codCntr));
        String numDoc=documentos.get("NNUMDOCORI");
        String numDocSalida=documentos.get("NNUMDOCSAL");

        String numDocCabecera= tipoInspeccion.equals("ENTRADA") ? numDoc: numDocSalida;
        HashMap<String,String>  informacion = DAO.getInstance().generarHashConConsulta(Consultas.consultaInfoDeCabecera(numDocCabecera));
        if (informacion.size()==0) throw new DataBaseException("No se ha encontrado el documento para \n "+codCntr);
        if(TextUtils.equals(tipoOperacion,InspeccionActivity.MODIFICAR_DOCUMENTO))
        {

             String fechaLog=informacion.get(res.getString(R.string.DFECHALOG));
             String[] fechaGuia=Formularios.fechaToArray(fechaLog);
             String[] fechaActual=Consultas.darFecha();
            if(!Formularios.fechaEsMismoDia(fechaActual,fechaGuia))
            {
                throw new InvalidOperationException("Esta guia no es de hoy");

            };

        }

        HashMap<String,String> datosContenedor=DAO.getInstance().generarHashConConsulta(Consultas.consultaDatosContenedor(codCntr));
        String cestadocntr=res.getString(R.string.CESTADOCNTR);
        datosContenedor.put(cestadocntr,informacion.get(cestadocntr));

        HashMap<String,String> tbdanios=ColumnasTablas.getInstance().darTabla(res.getString(R.string.TBDETMOVPATIO));
        ArrayList<HashMap<String,String>> listaDanios= DAO.getInstance().generarHashConsultaListaConFechas(tbdanios, Consultas.consultaDaniosDeDocumento(numDoc));
        if(informacion==null || listaDanios==null) return null;
        Inspeccion inspeccion=new Inspeccion();

        if(informacion.size()!=0 && !TextUtils.isEmpty(numDocCabecera) && !numDocCabecera.equals("0") ) {
            inspeccion.setInformacion(informacion);
            inspeccion.setDatosContenedor(datosContenedor);
            DaniosManager daniosManager=new DaniosManager();
            daniosManager.setListaDanios(DaniosManager.crearListaDanios(listaDanios));
            inspeccion.setDaniosManager(daniosManager);
        }



        return inspeccion;
    }



    /*/
    BUSCAR CABECERA
     */
    //Generar una inspeccion usando parametros que vienen del formulario para la consulta
    public Inspeccion read(ArrayList<CustomView> listaCampos,Resources res) throws ConnectionException {
        Inspeccion inspeccion=new Inspeccion();
        String tbTurnos=res.getString(R.string.TBTURNOS);
        ArrayList<String> adicionales=new ArrayList<>();
        //Se agregaron a la interfaz, no es necesario hacer esto ahora
//        adicionales.add(res.getString(R.string.CDESCEDULA));
//        adicionales.add(res.getString(R.string.CCELULAR));
        //Campos que no estanen la interfaz pero se necesitan traer
        adicionales.add(res.getString(R.string.CEN_COSTO));
        adicionales.add(res.getString(R.string.NATENDIDO));
        adicionales.add(res.getString(R.string.NATENDIDO));

        adicionales.add(res.getString(R.string.CNUMSELLOS));
        adicionales.add(res.getString(R.string.CDETALLECARGA));
        //CPTODESTINO se maneja desde la interfaz porque tiene difernetes nombres en las tablas
        adicionales.add(res.getString(R.string.CPTODESTINO));
        adicionales.add(res.getString(R.string.NPESCARPUERTO));
        adicionales.add(res.getString(R.string.NPESCARBASCULA));





        HashMap<String,String> esquema=ColumnasTablas.getInstance().darTabla(tbTurnos);
        String sentenciaInfo=DAO.getInstance().buscarCamposFormulario(tbTurnos,listaCampos,adicionales,esquema);
        HashMap<String,String> informacion= DAO.getInstance().generarHashConConsulta(sentenciaInfo);



        if(informacion==null) return null;
        //Si informacion.size==0 no encontro el turno
        if(informacion.size()==0) return inspeccion;




        inspeccion.setInformacion(informacion);
        String natendido=informacion.get(res.getString(R.string.NATENDIDO));

        boolean disponible=!TextUtils.isEmpty(natendido) && natendido.equals("0");
        if(! disponible) return inspeccion;
        //Completa los detalles (cosas de la inspeccion que no vienen del turno)
            //agregar el  tipo de actividad ()

        String puertoDestino=informacion.get(res.getString(R.string.CPTODESTINO));
        informacion.put(res.getString(R.string.CPTOORIGEN), puertoDestino);

        String tipoActividad=darTipoActividad(informacion,res);
        Log.e("TIPOACTIVIDAD",tipoActividad);
            informacion.put(res.getString(R.string.CCODEVELOG) , tipoActividad);
            //busca el cliente con el nit
        String clienteNit = informacion.get(res.getString(R.string.CNITCLIENTE));
        String nombreTercero = Consultas.darNombreTerceroConNit(clienteNit);
        informacion.put(res.getString(R.string.CCLIENTE),nombreTercero );
            //busca el nombre de la linea con su id
        String nombreLineaCorto = informacion.get(res.getString(R.string.CCTELNA));
        String nombreLineaLargo = DAO.getInstance().consulta_1_Dato(DAO.NOMBRE_LINEA_CON_ID + "'" + nombreLineaCorto + "'");
        informacion.put(res.getString(R.string.CDESCTELNA), nombreLineaLargo);
            //pone el numero de documento para el formulario
        String consecutivo= generarConsecutivo(informacion,res);
        informacion.put(res.getString(R.string.NNUMDOC), consecutivo);
                //busca el nombre el agente para la linea
        String agenteLinea= Consultas.darNombreAgente(nombreLineaCorto);
        informacion.put(res.getString(R.string.TER_RAZONS),agenteLinea);
                //


        String codCntr=informacion.get(res.getString(R.string.CCODCNTR));

        HashMap<String,String> datosContenedor=null;

        //Solo las
        //entradas tienen ccodcntr!=empty
        if (codCntr==null || codCntr.isEmpty())  datosContenedor=new HashMap<>();
        else {
            Inspeccion datosCntr=readInfoCntr(informacion,res);
            if(datosCntr==null) return null;
            datosContenedor=datosCntr.getDatosContenedor();
           inspeccion.setEstaEnPatio(datosCntr.estaEnPatio());
        }


        if (datosContenedor==null) return null;

        String numBooking=informacion.get(res.getString(R.string.CBOOKING));
        boolean haySaldoBooking=false;
        //Las entrasda no tienen booking
        if(!TextUtils.isEmpty(numBooking))
        {
            haySaldoBooking=Consultas.haySaldoBooking(nombreLineaCorto,numBooking);
            String tipoTurno=informacion.get(res.getString(R.string.CTIPOTURNO));
            if(!TextUtils.isEmpty(tipoTurno) && tipoTurno.equals(InspeccionActivity.SALIDA))
            {

                inspeccion.setInfoBooking(readInfoBooking(informacion, res));

            }


        }

        inspeccion.setHaySaldoBooking(haySaldoBooking);
        inspeccion.setDatosContenedor(datosContenedor);
        return inspeccion;

    }

    public String darTipoActividad(HashMap<String,String> informacion,Resources res) throws ConnectionException {



        String usoLogico=informacion.get(res.getString(R.string.CUSOLOGICO));
        String tipoTurno=informacion.get(res.getString(R.string.CTIPOTURNO));
        String tipoActividad="";
        boolean uso_empty=TextUtils.equals(usoLogico,"EMPTY");


        if (!TextUtils.isEmpty(usoLogico)) {

            if(TextUtils.equals(tipoTurno,InspeccionActivity.ENTRADA))
            {
                if (uso_empty)   tipoActividad = DAO.getInstance().consulta_1_Dato(DAO.ACTIVIDAD_ENTRADA_VACIO);
                else  tipoActividad = DAO.getInstance().consulta_1_Dato(DAO.ACTIVIDAD_ENTRADA_FULL);
            }
            else if(TextUtils.equals(tipoTurno,InspeccionActivity.SALIDA))
            {
                if(uso_empty)   tipoActividad = DAO.getInstance().consulta_1_Dato(DAO.ACTIVIDAD_SALIDA_VACIO);
                else  tipoActividad = DAO.getInstance().consulta_1_Dato(DAO.ACTIVIDAD_SALIDA_FULL);
            }


        }


        return tipoActividad;
    }
    public String generarConsecutivoSinTurno(Inspeccion inspeccion,Resources res) throws Exception {
        HashMap<String, String> info = inspeccion.getInformacion();
        String consulta=Consultas.countDocsDelDia(info,res);
        String countDocs=DAO.getInstance().consulta_1_Dato(consulta);
        int numDocs=Integer.parseInt(countDocs);
        info.put(res.getString(R.string.NTURNO), ""+(numDocs+1) );
        return generarConsecutivo(info,res);

    }
    public   String generarConsecutivo(HashMap<String,String> informacion,Resources res)
    {

        String consecutivo = "";
        try {
            char[] array = informacion.get(res.getString(R.string.NANO)).toCharArray();

            String mes = informacion.get(res.getString(R.string.NMES));
            String dia = informacion.get(res.getString(R.string.NDIA));
            String turno =informacion.get(res.getString(R.string.NTURNO));
            String anio = array[0] + "" + array[2] + "" + array[3];
            mes = String.format("%02d", Integer.parseInt(mes));
            dia = String.format("%02d", Integer.parseInt(dia));
            turno = String.format("%03d", Integer.parseInt(turno));
            consecutivo = anio + mes + dia + turno;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return consecutivo;


    }

    public Inspeccion readInfoCntr(HashMap<String,String> informacion,Resources res) throws ConnectionException {
        String codCntr=informacion.get(res.getString(R.string.CCODCNTR));
        String tipoTurno=informacion.get(res.getString(R.string.CTIPOTURNO));

        HashMap<String,String> datosContenedor=null;
        Inspeccion inspeccion=new Inspeccion();
        String nombreLineaCorto = informacion.get(res.getString(R.string.CCTELNA));

        if (codCntr==null || codCntr.isEmpty())  datosContenedor=new HashMap<>();
        else {
            datosContenedor=DAO.getInstance().generarHashConConsulta(Consultas.consultaDatosContenedor(codCntr));
            inspeccion.setEstaEnPatio(Contenedores.contenedorEstaEnPatio(nombreLineaCorto, codCntr));
            inspeccion.setDatosContenedor(datosContenedor);
        }

        if(tipoTurno!=null && tipoTurno.trim().equals("SALIDA"))
        {
            datosContenedor.put(res.getString(R.string.NNUMDOC), generarConsecutivo(informacion,res));
            HashMap<String,String> historicoCntr= DAO.getInstance().generarHashConConsulta( Consultas.consutlaHistoriCntr(nombreLineaCorto,codCntr));
            datosContenedor.putAll(historicoCntr);
            String booking=informacion.get(res.getString(R.string.CBOOKING));
            inspeccion.setPerteneceEnBooking(Consultas.contenedorPerteneceBooking(nombreLineaCorto,booking,codCntr));
        }
        return inspeccion ;

    }

    public Inspeccion readDaniosCntr(HashMap<String,String> informacion,Resources res) throws ConnectionException {
        Inspeccion inspeccion=new Inspeccion();
        String codCntr=informacion.get(res.getString(R.string.CCODCNTR));
        if(codCntr==null || codCntr.isEmpty()) return inspeccion;
        HashMap<String, String> entrada = DAO.getInstance().generarHashConConsulta(Consultas.consultaNumDocDeContenedor(codCntr));
        if (entrada==null) return null;
        String numDocEntrada=entrada.get(res.getString(R.string.NNUMDOCORI));
        ArrayList<HashMap<String, String>> danios = DAO.getInstance().generarHashConsultaLista(Consultas.consultaDaniosDeDocumento(numDocEntrada));
        if (danios==null) return null;
        DaniosManager daniosManager=new DaniosManager();
        daniosManager.setListaDanios(DaniosManager.crearListaDanios(danios));

        inspeccion.setDaniosManager(daniosManager);
        return inspeccion;

    }

    public ArrayList<HashMap<String, String>>  readInfoBooking(HashMap<String,String> informacion,Resources res) throws ConnectionException {

        String booking=informacion.get(res.getString(R.string.CBOOKING));
        String linea=informacion.get(res.getString(R.string.CCTELNA));
        ArrayList<HashMap<String, String>> infoBooking = Consultas.darInfoBooking(linea, booking);

        return infoBooking;
    }

    public Inspeccion readTurnosPendientes(HashMap<String,String> informacion,String tipoTurno,Usuario usuario,Resources res) throws ConnectionException, DataBaseException {
        Inspeccion inspeccion=new Inspeccion();
        HashMap<String,String> condiciones=generarCondicionTurnos(informacion,res);
        condiciones.remove(res.getString(R.string.NTURNO));
        String sentencia=Consultas.darTurnosRestantes(condiciones  , tipoTurno,usuario.getPatio());
        ArrayList<HashMap<String,String>> lista = DAO.getInstance().generarHashConsultaLista(sentencia);
        inspeccion.setTurnosPendientes(lista);
        return inspeccion;


    }

    //
    public String darNombreCliente(String nit) throws ConnectionException {
        return Consultas.darNombreTerceroConNit(nit);
    }

    public String darAgenteLinea(HashMap<String,String> info,Resources res) throws ConnectionException {
        return Consultas.darNombreAgente( info.get(res.getString(R.string.CCTELNA) ));

    }

}