package com.juans.inspeccion.Mundo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.juans.inspeccion.ConnectionException;
import com.juans.inspeccion.CustomView.CustomView;
import com.juans.inspeccion.DataBaseException;
import com.juans.inspeccion.DiccionariosException;
import com.juans.inspeccion.R;
import com.juans.inspeccion.Varios;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import jxl.Workbook;
import jxl.write.*;

import org.apache.poi.hssf.record.chart.CategorySeriesAxisRecord;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;

/**
 * Created by juan__000 on 8/13/2014.
 */



public class DAO  {
    public final static String SERVER_ADDRESS="SERVER";
    public final static String DATA_BASE="DATA_BASE";
    public final static String USER="USER";
    public final static String PASSWORD="PASSWORD";
    public final static String SERVER_INSTANCE="SERVER_INSTANCE";


    public final static int UPDATE_SUCCESFUL=1;
    public final static int DUPLICATED_KEY=2627;


    private final static String MSG_TIME_OUT="";
    private final static String ERROR_CONEXION="Error: Se perdio la conexion intente de nuevo";
    private final static String ERROR_DE_DATOS="Error de base de datos, intente de nuevo";
    /*
    tipos de columna
     */
    private final static String[] numeros= {"I","N","S", "Y"};
    private final static String[] cadenas= {"C","D","T", "M" , "B","V"};

    private final static String logico="L";


    public final static String FUNCION_ULTIMA_ENTRADA_CONTENEDOR= "SELECT DFECENTCNTR,DFECSALCNTR FROM FC26_ULTIMAENTRADACONTENEDOR";    // (CCTELNA, CODIGOCOTENEDOR)
    public final static String FUNCION_STOCK_CONTENEDORES_DETALLADO= "Declare @tablevar table(CCTELNA, CCIDCNTR, CTIPOCNTR, CTAMCNTR, EST_FINAL ,EST_INICIAL ) " +
            "INSERT INTO @tablevar EXECUTE SP_M26STOCKCONTENEDORES";    //      PARAMETRS EJEMPLO  'HAL','20141016'
    public final static String FUNCION_STOCK_CONTENEDORES_RESUMIDO= "EXECUTE SP_M26STOCKRESUMIDOTOTAL";    //      PARAMETROS EJEMPLO  '20141016'
    public final static String FUNCION_TURNOS_DEL_DIA= "SELECT * FROM TBTURNOS WHERE NATENDIDO=1 AND";
    public final static String LINEA_USA_UBICACION="SELECT NOBLUBICA FROM TBCLIENTESLNA WHERE  CCTELNA=";

    /**Consulta que trae INT.DEP.MTY */
    public final static String ACTIVIDAD_ENTRADA_VACIO="SELECT CVALOR FROM SYSAMBIENTE WHERE APLICACION='PATIOS' AND CODIGO='GEN' AND CNOMPARA='XP_CDEVLG04'";
    public final static String ACTIVIDAD_ENTRADA_FULL="SELECT CVALOR FROM SYSAMBIENTE WHERE APLICACION='PATIOS' AND CODIGO='GEN' AND CNOMPARA='XP_CDEVLG06'";
    public final static String ACTIVIDAD_SALIDA_VACIO="SELECT CVALOR FROM SYSAMBIENTE WHERE APLICACION='PATIOS' AND CODIGO='GEN' AND CNOMPARA='XP_CDEVLG05'";
    public final static String ACTIVIDAD_SALIDA_FULL="SELECT CVALOR FROM SYSAMBIENTE WHERE APLICACION='PATIOS' AND CODIGO='GEN' AND CNOMPARA='XP_CDEVLG07'";

    public final static String NOMBRE_TERCERO_CON_NIT= "SELECT TER_RAZONS FROM TERCEROS where TERCERO=";
    public final static String NOMBRE_LINEA_CON_ID="SELECT CDESCTELNA FROM TBCLIENTESLNA WHERE CCTELNA=";


    private final static String INSPECTOR_CON_CEDULA= "SELECT * FROM TBINSPECTORES WHERE TERCERO=";
    public final static String TARIFA_DE_LINEA="SELECT NTARHORHOM FROM TBCLIENTESLNA WHERE CCTELNA=";
    public final static String GRUPO_REP_DE_METODO="SELECT CGRUPOREP FROM  TBMETODOREPAR WHERE CCODMET=";


    private final static String FECHA_DATABASE ="SELECT DAY(GETDATE()),MONTH(GETDATE()),YEAR(GETDATE()),  DATEPART(HOUR,GETDATE()) , DATEPART(MINUTE,GETDATE()), GETDATE();";

    public final static String LISTA_ESTADOS_HABILITADOS_INSPECCION="SELECT CESTADOCNTR,CDESCONDCNTR,NAPTOCNTR FROM TBESTADOSCNTR WHERE NHABILITADO=1;";
    public final static String LISTA_TIPOS_CONTENEDOR="SELECT CTIPCNTR, CTIPDES FROM TBTIPOSCNTR ORDER BY CTIPDES";
    public final static String LISTA_MATERIALES_CONTENEDOR="SELECT CMATCNTR, CMATDES FROM TBMATERIALCNTR";
    public final static String LISTA_TAMANOS_CONTENEDOR="SELECT CTAMCNTR, CTAMDES  FROM TBTAMANOSCNTR";
    public final static String LISTA_LINEAS="SELECT CCTELNA,CDESCTELNA FROM TBCLIENTESLNA ORDER BY CCTELNA";
    public final static String LISTA_SECUENCIAS="SELECT CIDSECUENCIA, CDESCRIPCION from TB26SECUENCIAS ORDER BY NORDEN";
    public final static String LISTA_UBICACIONES_CON_SECUENCIA="SELECT * FROM TBUBICACION WHERE CIDSECUENCIA=";
    public final static String LISTA_COMPONENTES="SELECT CCODELE, CDES1ELE FROM TBELEMENTOS ORDER BY CCODELE";
    public final static String LISTA_TIPOS_DANIOS="SELECT CCODDAN, CDES1DAN FROM TBDANOS ORDER BY CCODDAN";
    public final static String LISTA_METODOS_REPARACION="SELECT CCODMET, CDES1MET, CTIPCALCULO, NTARTAMANO  FROM TBMETODOREPAR ORDER BY CCODMET";
    public final static String LISTA_DANIOS_DE_TURNO="SELECT * FROM TBDETMOVPATIO WHERE NNUMDOC=";
    public final static String LISTA_PUERTOS="SELECT * FROM TBSITCNTR ORDER BY CCODSITCNTR";









    public final static int QUERY_TIME_OUT_SECONDS=10;
    public final static String NO_ENCONTRADO="NO_ENCONTRADO";
    public final static String NO_HABILITADO="NO_HABILITADO"; //TODO








    /**
    * Instancia del DAO (Data Access Object )
    */
    private static DAO instance;

    /**
     * conexion con la base de datos
     */
    private Connection connection;
    /**
     * Paramtros de conexion
     */
     private HashMap<String,String> parametrosConexion;

    private boolean succcesful_connection;

    private final static String FILE_NAME="connectionSettings";
    /**
     *  Determina si la conexion a la base fue exitosa o no
     */
    private ColumnasTablas columnas;




    private DAO()
    {
        columnas=ColumnasTablas.getInstance();
    }

    public synchronized static DAO getInstance()
    {

        if(instance==null)
        {
            instance=new DAO();
        }
        return instance;
    }

    public void inicializarDatos(HashMap<String,String> params)
    {
        this.parametrosConexion=params;


    }

    public boolean guardarDatosConexion(Context c)
    {
        try {

            File file = c.getFileStreamPath(FILE_NAME);
            if (file.exists()) {
                file.delete();
            }

            FileOutputStream fos;
            fos = c.openFileOutput(FILE_NAME, Context.MODE_APPEND);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(parametrosConexion);
            os.close();
        } catch (Exception e){
            e.printStackTrace();
        }





        return true;
    }


    public HashMap<String,String> cargarDatosConexion(Context c)
    {
        if( parametrosConexion==null)
        {

            FileInputStream fis = null;
            try {
                File file=c.getFileStreamPath(FILE_NAME);
                if(!file.exists()) return null;
                fis = c.openFileInput(FILE_NAME);

                ObjectInputStream is = new ObjectInputStream(fis);
                parametrosConexion = (HashMap<String, String>) is.readObject();
            } catch (Exception e) {
                e.printStackTrace();

                return null;
            }


        }
        return parametrosConexion;
    }








    /*  Crea una conexion en caso de que no exista o este cerrada, USAR PARA TODAS LAS CONSULTAS*/
    public Connection darConexion() throws  SQLException
    {

            if(connection==null || connection.isClosed()) {



                connection=crearConexion();

            }

            return connection;





    }
    /* Prueba si la ultima conexion cargada funciona */

    public void probarConexion() throws Exception
    {
        succcesful_connection=false;
        connection = darConexion();
        ResultSet rs;
        Statement statement=connection.createStatement();
        String command="SELECT COUNT(*) AS NUMTABLAS FROM sys.TABLES";
        rs= statement.executeQuery(command);
        int numeroTablas=-1;
        rs.next();
        numeroTablas=rs.getInt("NUMTABLAS");
        darConexion().close();



    }










    @SuppressWarnings("unused")
    @SuppressLint("NewApi")
    private Connection crearConexion() throws SQLException
    {
        StrictMode.ThreadPolicy policy= new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Connection conn=null;
        String connURl=null;
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");

            String inst=parametrosConexion.get(SERVER_INSTANCE);
            String instancia= inst.trim().isEmpty()?  "": ";instance=" +inst;
            connURl="jdbc:jtds:sqlserver://" +parametrosConexion.get(SERVER_ADDRESS) + instancia+ ";databaseName=" + parametrosConexion.get(DATA_BASE)
                    + ";user=" +parametrosConexion.get(USER) + ";password=" +parametrosConexion.get(PASSWORD)+";connectTimeout=5";
            Log.e("URL", connURl);





            conn= DriverManager.getConnection(connURl);
        }
        catch(SQLException e)
        {
            throw new SQLException(e.getMessage());
        }
        catch (ClassNotFoundException e) {
            Log.e("Error", e.getMessage() );
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }


        return conn;
    }


    public void close(ResultSet rs,Statement ps)
    {
        if(rs!=null)
        {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if(ps!=null)
        {
            try {
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }




    /**
     COnsultas
     **/
    public HashMap<String, String>  cargarHashColummas(String nombreTabla) throws ConnectionException,DataBaseException {
        HashMap<String, String> columnas=null;
        ResultSet rs=null;
        Statement statement=null;
        try {


             statement = darConexion().createStatement();
            columnas=new HashMap<>();
            String command = "SELECT * from sysdcncampos where cnomtabla='"+nombreTabla+"' order by norden";

            rs = statement.executeQuery(command);

            while(rs.next())
            {
                String nombreColumna=rs.getString("cam_nombre").trim().toUpperCase();
                String tipo=rs.getString("cam_tipo");

                columnas.put( nombreColumna, tipo);

            }



        }
        catch(Exception e)
        {
            Log.e("ErrorDarTablasSQL", e.getMessage());
            e.printStackTrace();
            if(statement==null) throw new ConnectionException();
            throw new DataBaseException(e.getMessage());
        }finally {
           close(rs,statement);
        }
        return columnas;
    }



    public String buscarCamposFormularioTodos (String _nombreTabla, ArrayList<CustomView> _campos)
    {

        String nombreTabla= _nombreTabla;
        ArrayList<CustomView> campos=_campos;
        String sentencia="";

        String CONDICIONES="";

        int numCondiciones=0;
        for(int i=0;i<campos.size();i++)
        {

            CustomView textField= campos.get(i);
            if (textField.esLlave(nombreTabla))
            {

                if( numCondiciones>0) CONDICIONES+=" AND ";



                CONDICIONES += " " + textField.getNombreCampo() + "=" + "'" + textField.getTexto().toString() + "'";


                numCondiciones++;


            }

        }
        sentencia="SELECT * FROM "+nombreTabla+" WHERE "+CONDICIONES;
        Log.e("crear select", sentencia);
        return sentencia;

    }


    public String buscarCamposFormulario(String _nombreTabla, ArrayList<CustomView> _campos,ArrayList<String> adicionales,HashMap<String,String> esquema)
    {


        HashMap<String,String> datos=new HashMap<String, String>();
        String nombreTabla= _nombreTabla;
        ArrayList<CustomView> campos=_campos;

        String CAMPOS=" ";
        String CONDICIONES="";
        int numCampos=0;
        int numCondiciones=0;
        for(int i=0;i<campos.size();i++)
        {

            CustomView textField= campos.get(i);
            if (textField.esLlave(nombreTabla))
            {

                if( numCondiciones>0) CONDICIONES+=" AND ";



                    CONDICIONES += " " + textField.getNombreCampo() + "=" + "'" + textField.getTexto().toString() + "'";


                numCondiciones++;
                if (numCampos > 0) CAMPOS += ",";
                CAMPOS += textField.getNombreCampo();
                numCampos++;


            }
            else
            {   if(esquema.containsKey(textField.getNombreCampo())) {
                if (numCampos > 0) CAMPOS += ",";
                CAMPOS += textField.getNombreCampo();
                numCampos++;
            }
            }
        }
        if(adicionales!=null)
        for(int i=0;i<adicionales.size();i++)
        {
            if( numCampos>0) CAMPOS+=",";
            CAMPOS+=adicionales.get(i);
        }
        String sentencia="SELECT"+CAMPOS+" FROM "+nombreTabla+" WHERE "+CONDICIONES;
        Log.e("crear select", sentencia);
        return sentencia;
    }




    public  Object[] contenedorEstaEnPatio(String linea,String codigoContenedor)
    {
        boolean esta=false;



            String sentencia=FUNCION_ULTIMA_ENTRADA_CONTENEDOR;
            String parametros="("+"'"+linea+"'"+","+"'"+codigoContenedor+"'"+")";

            sentencia+=parametros;
        HashMap<String,String> consulta= null;
        try {
            consulta = generarHashConConsulta(sentencia);
        } catch (ConnectionException e) {
            e.printStackTrace();
        }
        try {
            if(consulta.size()==0) esta=false;
            else {

                String ColEntrada = "DFECENTCNTR";
                String ColSalida = "DFECSALCNTR";
                String entrada = consulta.get(ColEntrada);
                String salida = consulta.get(ColSalida);

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

                java.util.Date fechaEntrada = formatter.parse(entrada);
                java.util.Date fechaSalida = formatter.parse(salida);
                int compare=fechaEntrada.compareTo(fechaSalida);
                if ( compare> 0) {
                    esta = true;
                } else {
                    esta = false;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new Object[]{esta, consulta};

    }







    public String insertarCursor(String nombreTabla,HashMap<String,String> cursor, HashMap<String, String> totalColumnas)
    {
        String pudoInsertar=null;
        String sentencia="INSERT INTO "+nombreTabla+" (";
        String values=" VALUES (";
        Iterator<String> iteradorTotalColumnas= totalColumnas.keySet().iterator();
        int numColumnas=0;




        while ( iteradorTotalColumnas.hasNext())
        {
            String columna=iteradorTotalColumnas.next();

            if (numColumnas!=0)
            {
                sentencia+=",";
                values+=",";

            }
            sentencia+=columna;
            String tipoColumna=totalColumnas.get(columna);
            boolean esNumero= estaEn( tipoColumna, numeros);

            boolean esCadena= estaEn( tipoColumna, cadenas);
            boolean esLogico= tipoColumna.equals(logico);

            String comillas= esCadena ? "'" : "";
            String valor="";

            if (cursor.containsKey(columna))
            {
              valor=cursor.get(columna).trim();
                if(valor.trim().isEmpty() && !esCadena)
                    valor="0";


            }
            else
            {
                if (esCadena )   valor=" ";  //Si es numero pone un 0 si no, un vacio
                else valor="0";

            }
            values +=comillas+valor+comillas;
            numColumnas++;

        }
        sentencia+=")";
        values+=")";
        sentencia+=values;

        Statement statement=null;

        try {
             statement = darConexion().createStatement();
            statement.setQueryTimeout(QUERY_TIME_OUT_SECONDS);

            statement.executeUpdate(sentencia);
        }
        catch (Exception e) {
                    pudoInsertar=e.getMessage();
        }finally {
            close(null,statement);
        }


        return pudoInsertar;



    }

    private static boolean estaEn( String entrada, String[] arregloComparacion)
    {
        boolean igual=false;
     for(int i=0;i<arregloComparacion.length && !igual ;i++ )
     {
        if (entrada.equals(arregloComparacion[i]))
         {
             igual=true;
         }


     }
        return igual;
    }




    public static String crearSentenciaINSERT(String nombreTabla,HashMap<String,String> cursor, HashMap<String, String> esquema) throws DiccionariosException {

        if(esquema==null) throw new DiccionariosException("ERROR:Diccionarios no inicializados");
        String sentencia="INSERT INTO "+nombreTabla+" (";
        String values=" VALUES (";
        Iterator<String> iteradorTotalColumnas= esquema.keySet().iterator();
        int numColumnas=0;



        while ( iteradorTotalColumnas.hasNext())
        {
            String columna=iteradorTotalColumnas.next();

            if (numColumnas!=0)
            {
                sentencia+=",";
                values+=",";

            }
            sentencia+=columna;
            String tipoColumna=esquema.get(columna);
            boolean esNumero= estaEn( tipoColumna, numeros);

            boolean esCadena= estaEn( tipoColumna, cadenas);
            boolean esLogico= tipoColumna.equals(logico);

            String comillas= esCadena ? "'" : "";
            String valor="";

            if (cursor.containsKey(columna))
            {
                valor=cursor.get(columna);

                if(TextUtils.isEmpty(valor) && !esCadena)
                    valor="0";

            }
            else
            {
                if (esCadena )   valor=" ";  //Si es numero pone un 0 si no, un vacio
                else valor="0";

            }
            values +=comillas+valor+comillas;
            numColumnas++;

        }
        sentencia+=")";
        values+=")";
        sentencia+=values;



        Log.e("DAO.crearSentencia=",sentencia);
        return sentencia;

    }

    public static String crearSentenciaDELETE(String nombreTabla,HashMap<String,String> condiciones)
    {
        String sentencia="DELETE FROM "+nombreTabla+" WHERE ";
        sentencia+=agregarCondiciones(condiciones);
        return sentencia;
    }

    public static String crearSentenciaUPDATE(String nombreTabla,ArrayList<CustomView> listaCampos,HashMap<String,String> cursor,HashMap<String,String> llave, HashMap<String, String> esquema) throws DiccionariosException {
        if (esquema == null) throw new DiccionariosException("ERROR:Diccionarios no inicializados");
        String sentencia = "UPDATE " + nombreTabla;
        String values = " SET ";
        Iterator<String> iteradorTotalColumnas = esquema.keySet().iterator();
        int numColumnas = 0;


        while (iteradorTotalColumnas.hasNext()) {
            String columna = iteradorTotalColumnas.next();
            if (numColumnas != 0) values += ",";
            String tipoColumna = esquema.get(columna);
            boolean esCadena = estaEn(tipoColumna, cadenas);
            String comillas = esCadena ? "'" : "";
            String valor = "";
            if (cursor.containsKey(columna)) {
                valor = cursor.get(columna).trim();
                if (valor.trim().isEmpty() && !esCadena)
                    valor = "0";
            } else {
                if (esCadena) valor = " ";  //Si es numero pone un 0 si no, un vacio
                else valor = "0";

            }
            values += columna + "=" + comillas + valor + comillas;
            numColumnas++;

        }

        String condiciones = " WHERE ";
        int numCondiciones = 0;

        //Sometimes PK is changed in the form
        if(llave!=null)
        {
            condiciones+=agregarCondiciones(llave);
        }
         else {
            for (int i = 0; i < listaCampos.size(); i++) {

                CustomView textField = listaCampos.get(i);
                if (textField.esLlave(nombreTabla)) {
                    if (numCondiciones > 0) condiciones += " AND ";
                    condiciones += " " + textField.getNombreCampo() + "=" + "'" + textField.getTexto() + "'";
                    numCondiciones++;
                }
            }
        }
        sentencia+=values+condiciones;




        Log.e("DAO.SentenciaUPDATE=",sentencia);
        return sentencia;


    }

    public static String agregarCondiciones(HashMap<String,String> llave)
    {

        int numCondiciones=0;
        String condiciones="";
        Iterator ite=llave.entrySet().iterator();
            while(ite.hasNext())
            {
                Map.Entry pair= (Map.Entry) ite.next();
                if (numCondiciones > 0) condiciones += " AND ";
                condiciones += " " + pair.getKey()+ "=" + "'" + pair.getValue() + "'";
                numCondiciones++;

            }

        return condiciones;
    }



    public ArrayList<FilaEnConsulta> cargarLista( String consulta, int num_columnas) throws DataBaseException, ConnectionException {

        ArrayList<FilaEnConsulta> arrayList=null;
        boolean conecto=false;

        Statement statement=null;
        ResultSet rs=null;
        try {
             statement = darConexion().createStatement();
            Log.e("CargarLista",consulta);
            arrayList=new ArrayList<FilaEnConsulta>();
            conecto=true;
            statement.setQueryTimeout(QUERY_TIME_OUT_SECONDS);

             rs = statement.executeQuery(consulta);
            ResultSetMetaData rsmd = rs.getMetaData();

            num_columnas=rsmd.getColumnCount();


            while(rs.next())
            {
                FilaEnConsulta fila=new FilaEnConsulta(num_columnas);

                for (int i = 0; i < num_columnas; i++) {
                    String string=rs.getString(i+1);
                    if (string==null) string="";
                    fila.setDato(i, string.trim());

                }
                arrayList.add(fila);

            }

        } catch (Exception e) {
            e.printStackTrace();
            if(conecto) throw new DataBaseException("Error base de datos");
            else throw new ConnectionException("Error de conexion");

        }finally {
            close(rs,statement);
        }
        return arrayList;




    }

    public String consulta_1_Dato(String sentencia) throws ConnectionException {

        String respuesta=null;
        Statement statement=null;
        ResultSet rs=null;

        try {
              statement = darConexion().createStatement();
            respuesta="";
            statement.setQueryTimeout(QUERY_TIME_OUT_SECONDS);
             rs=statement.executeQuery(sentencia);
            if (rs.next() ) {

                respuesta = rs.getString(1).trim().toUpperCase();
            }



        } catch (Exception e) {
            Log.e("COnsulta1Dato",sentencia);

            e.printStackTrace();
            if (statement==null) {
                Log.e("Statement","null");
                throw new ConnectionException();

            }

        }finally {
            close(rs,statement);
        }
        return respuesta;
    }


    public String[]   loginUsuario(String cedula)
    {
        String nombre="";
        String codigo="";
        String habilitado="";
        String sentencia="SELECT * FROM TBINSPECTORES WHERE CCODINSPECTOR="+cedula;
        Statement statement=null;
        ResultSet rs=null;
        try {
             statement=darConexion().createStatement();
            statement.setQueryTimeout(QUERY_TIME_OUT_SECONDS);

             rs=statement.executeQuery(sentencia);
            if(rs.next())
            {
                nombre=rs.getString("CNOMINSPECTOR");
                codigo=rs.getString("CCODINSPECTOR");
                habilitado=rs.getString("NHABILITADO");



            }


        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Logear",sentencia);
            return null;
        }finally {
            close(rs,statement);
        }


        String[] respuesta=new String[]{nombre,codigo,habilitado};

        return respuesta;




    }

    public String[] darFecha()
    {
        String[] respuesta=null;
        Statement statement=null;
        ResultSet rs=null;
        try {
             statement = darConexion().createStatement();

            statement.setQueryTimeout(QUERY_TIME_OUT_SECONDS);

             rs= statement.executeQuery(FECHA_DATABASE);
            if(rs.next()) {
                respuesta=new String[7];
                String dia = rs.getString(1);
                String mes = rs.getString(2);
                String anio = rs.getString(3);
                String hora = rs.getString(4);
                String minutos = rs.getString(5);
                String fechaEntera = rs.getString(6);

                respuesta[Consultas.FECHA_DIA] = Varios.agregarCeros(dia, 2);
                respuesta[Consultas.FECHA_MES] = Varios.agregarCeros(mes, 2);
                respuesta[Consultas.FECHA_ANIO] = anio;
                respuesta[Consultas.FECHA_HORA] = Varios.agregarCeros(hora, 2);
                respuesta[Consultas.FECHA_MINUTOS] = Varios.agregarCeros(minutos, 2);
                respuesta[Consultas.FECHA_ENTERA] = fechaEntera;
                respuesta[Consultas.FECHA_MOSTRAR]= respuesta[Consultas.FECHA_ANIO]+"/"+
                        respuesta[Consultas.FECHA_MES]+"/"+respuesta[Consultas.FECHA_DIA];


            }

        }
        catch(Exception e)
        {
          respuesta=null;

        }finally {
            close(rs,statement);
        }


        return respuesta;

    }


    public ArrayList<FilaEnConsulta> hacerTablaConConsulta(String consulta, int limiteResultado)
    {
        ArrayList<FilaEnConsulta> tabla=new ArrayList<FilaEnConsulta>();
        Statement statement=null;
        ResultSet rs=null;
        try {
            Statement st=darConexion().createStatement();
            st.setQueryTimeout(QUERY_TIME_OUT_SECONDS+10);
             rs=st.executeQuery(consulta);
            ResultSetMetaData rsmd=rs.getMetaData();
            int numColumnas=rsmd.getColumnCount();
            FilaEnConsulta cabecera=new FilaEnConsulta(numColumnas);

            for(int i=0;i<numColumnas;i++)
            {
                cabecera.setDato(i,rsmd.getColumnName(i+1) ) ;


            }
            tabla.add(cabecera);


            int filasAgregadas=0;
            while(rs.next() && filasAgregadas<limiteResultado)
            {
                FilaEnConsulta fila=new FilaEnConsulta(numColumnas);
                for(int i=0;i<numColumnas;i++)
                {
                    fila.setDato(i, rs.getString(i+1));
                }
                tabla.add(fila);
                filasAgregadas++;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            close(rs,statement);
        }
        return tabla;

    }

    public File tablaFilasAExcel(ArrayList<FilaEnConsulta> tabla,String nombreArchivo)
    {
        File tablaExcel=null;
        try {
            //http://www.andykhan.com/jexcelapi/tutorial.html

            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File folder = new File(path, "Contenedores");
            folder.mkdir();
             tablaExcel=new File(path, "/Contenedores/"+nombreArchivo+".xls");





            WritableWorkbook workbook = Workbook.createWorkbook(tablaExcel);



            WritableSheet sheet = workbook.createSheet("First Sheet", 0);


            // Create a cell format for Arial 10 point font
            WritableFont arial12font = new WritableFont(WritableFont.ARIAL, 12, WritableFont.BOLD, false);
            WritableCellFormat arial12Boldformat = new WritableCellFormat (arial12font);

            WritableFont arial10font = new WritableFont(WritableFont.ARIAL, 10);
            WritableCellFormat arial10format = new WritableCellFormat (arial10font);
            int numColumnas=0;
            if(tabla.size()>=1)
            {
                //Escribe encabezado
                FilaEnConsulta filaEnConsulta=tabla.get(0);
                numColumnas=filaEnConsulta.getNumColumnas();

                for(int i=0;i<numColumnas;i++)
                {
                    Label label = new Label(i,0, filaEnConsulta.getDato(i), arial12Boldformat);
                    sheet.setColumnView(i,15);

                    sheet.addCell(label);
                }

            }
            if(tabla.size()>1)
            {
                //Escribe la informacion
                for(int i=1;i<tabla.size();i++)
                {
                    FilaEnConsulta filaEnConsulta=tabla.get(i);
                    for(int j=0;j<numColumnas;j++)
                    {
                        Label label = new Label(j,i, filaEnConsulta.getDato(j), arial10format);
                        sheet.addCell(label);
                    }

                }
            }





            workbook.write();
            workbook.close();



        } catch (Exception e) {
            e.printStackTrace();
        }
        return tablaExcel;
    }


    public void generarExcelConsulta(String consulta, File archivoExcel, int limiteResultados)
    {
        ArrayList<FilaEnConsulta> tabla=hacerTablaConConsulta(consulta,limiteResultados);

        //String[] fecha=DAO.getInstance().darFecha();
        //String marcaArchivo=fecha[FECHA_DIA]+"-"+fecha[FECHA_MES]+"-"+fecha[FECHA_ANIO]+"   "+fecha[FECHA_HORA]+"_"+fecha[FECHA_MINUTOS];
        excelTest(tabla, archivoExcel);



    }

    private void excelTest(   ArrayList<FilaEnConsulta> tabla,File file) {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("Consulta");



        int numColumnas=0;
        if(tabla.size()>0)
        {
            //Escribe encabezado
            HSSFFont font = workbook.createFont();
            font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);

            HSSFCellStyle style = workbook.createCellStyle();
            style.setFont(font);
            FilaEnConsulta filaEnConsulta=tabla.get(0);
            numColumnas=filaEnConsulta.getNumColumnas();
            Row row = sheet.createRow(0);
            for(int i=0;i<numColumnas;i++)
            {
                Cell cell=row.createCell(i);
                cell.setCellValue(filaEnConsulta.getDato(i));
                cell.setCellStyle(style);
                sheet.setColumnWidth(i, 256*15);




            }
            sheet.setAutoFilter(CellRangeAddress.valueOf("A1:"+ (Character.toString((char)( 65+numColumnas-1)))+"1"));



        }


        if(tabla.size()>1)
        {
            //Escribe la informacion
            for(int i=1;i<tabla.size();i++)
            {
                Row row = sheet.createRow(i);
                FilaEnConsulta filaEnConsulta=tabla.get(i);
                for(int j=0;j<numColumnas;j++)
                {
                    Cell cell=row.createCell(j);
                    cell.setCellValue(filaEnConsulta.getDato(j));
                }

            }
        }






        try {
            FileOutputStream out = new FileOutputStream(file);
            workbook.write(out);
            out.close();
            System.out.println("Excel written successfully..");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();



        }


    }

    public void transacTest()
    {
        try{
            Statement st=darConexion().createStatement();
            String tran="BEGIN TRAN INSERT INTO TBINSPECTORES VALUES (09,'fafafad',13424324,231) IF @@ERROR <> 0 BEGIN ROLLBACK TRAN return END  INSERT INTO TBINSPECTORES VALUES (19,'GGFFGSGS',13424324,231) IF @@ERROR <> 0 BEGIN ROLLBACK TRAN return  END   COMMIT TRAN";
            st.execute(tran);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }





    public ArrayList<HashMap<String,String>> generarHashConsultaLista(String sentencia) throws ConnectionException {
        ArrayList<HashMap<String,String>> cursores=new ArrayList<HashMap<String, String>>();
        Statement statement=null;
        ResultSet rs=null;
        try
        {

             statement=darConexion().createStatement();
            statement.setQueryTimeout(QUERY_TIME_OUT_SECONDS+5);
             rs=statement.executeQuery(sentencia);
            ResultSetMetaData rsmd=rs.getMetaData();
            int numCols=rsmd.getColumnCount();
            while(rs.next())
            { HashMap<String,String> cursor=new HashMap<String, String>();

                for(int i=1;i<=numCols;i++)
                {

                    cursor.put(rsmd.getColumnName(i).toUpperCase() , rs.getString(i).trim() );
                }
                cursores.add(cursor);
            }




        }
        catch (Exception e)
        {
            e.printStackTrace();
            if(statement==null) throw new ConnectionException("Se perdio la conexion");
        }finally {
            close(rs,statement);
        }


        return cursores;
    }
    public ArrayList<HashMap<String,String>> generarHashConsultaListaConFechas(HashMap<String,String> esquemaTabla,String sentencia) throws ConnectionException, DiccionariosException {
        if (esquemaTabla==null) throw  new DiccionariosException("");
        ArrayList<HashMap<String,String>> cursores=new ArrayList<HashMap<String, String>>();
        Statement statement=null;
        ResultSet rs=null;
        try
        {

            statement=darConexion().createStatement();
            statement.setQueryTimeout(QUERY_TIME_OUT_SECONDS+5);
            rs=statement.executeQuery(sentencia);
            ResultSetMetaData rsmd=rs.getMetaData();
            int numCols=rsmd.getColumnCount();
            while(rs.next())
            { HashMap<String,String> cursor=new HashMap<String, String>();

                for(int i=1;i<=numCols;i++)
                {
                    String columna=rsmd.getColumnName(i).toUpperCase();
                    String valor=rs.getString(i).trim();
                    switch (esquemaTabla.get(columna))
                    {
                        case "D":valor = Formularios.dateToDbParser(valor); break;
                        case "N":
                            valor=  ""+Varios.eliminarDecimales(valor);
                            break;
                    }

                    cursor.put(columna, valor);
                }
                cursores.add(cursor);
            }




        }
        catch (Exception e)
        {
            e.printStackTrace();
            if(statement==null) throw new ConnectionException("Se perdio la conexion");
        }finally {
            close(rs,statement);
        }


        return cursores;
    }


    public  HashMap<String,String> generarHashConConsulta(String sentencia) throws ConnectionException {
        Log.e("GenerarHashCOnsulta",sentencia);
        HashMap<String,String> cursor=null;
        Statement statement=null;
        ResultSet rs=null;
        try
        {   statement=darConexion().createStatement();
            cursor=new HashMap();
            statement.setQueryTimeout(QUERY_TIME_OUT_SECONDS);
             rs=statement.executeQuery(sentencia);
            ResultSetMetaData rsmd=rs.getMetaData();
            int numCols=rsmd.getColumnCount();
            if(rs.next())
            {
                for(int i=1;i<=numCols;i++)
                {
                    String dato=rs.getString(i);
                    dato = dato==null ? "":dato.trim();
                    cursor.put(rsmd.getColumnName(i).toUpperCase() ,dato );
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            if(statement==null) throw new ConnectionException("Se perdio la conexion");
         }finally {
            close(rs,statement);
        }
        return cursor;
    }

    public ArrayList<String> generarINSERTSlista(String destino, ArrayList<HashMap<String, String>> cursores) throws DiccionariosException {
        ArrayList<String> sentencias=new ArrayList<>();
        HashMap<String, String> columnas = ColumnasTablas.getInstance().darTabla(destino);
        for(int i=0;i<cursores.size();i++)
        {
            sentencias.add(DAO.getInstance().crearSentenciaINSERT(destino, cursores.get(i), columnas));
        }
        return sentencias;
    }

    public void ejecutarComoTransaccion(ArrayList<String> sentencias) throws ConnectionException, DataBaseException {
        int pudoInsertar=UPDATE_SUCCESFUL;
        String sentencia="BEGIN TRAN";
        String error="IF @@ERROR <> 0 BEGIN ROLLBACK TRAN return END";
        for(int i=0;i<sentencias.size();i++)
        {
            sentencia+="\n"+sentencias.get(i)+"\n"+error;
        }
        String fin="COMMIT TRAN";
        sentencia+="\n"+fin;
        Statement statement=null;
        ResultSet rs=null;

        try
        {
            statement=darConexion().createStatement();



            statement.setQueryTimeout(QUERY_TIME_OUT_SECONDS + 5);
            statement.execute(sentencia);
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e("Insert",sentencia);
            pudoInsertar=e.getErrorCode();
            if(pudoInsertar==DUPLICATED_KEY)
                throw new DataBaseException("ERROR:EL TURNO YA SE HABIA GRABADO");
            else if (statement==null) throw new DataBaseException("ERROR: SE PERDIO LA CONEXION \n INTENTE NUEVAMENTE");
            else throw new DataBaseException(e.getMessage());

            //todo-Hacer que todos los metodos sepan que exepcion botar


//            Log.e("SQLSTATE",e.getSQLState());
//            Log.e("ERRORCODE",e.getErrorCode()+"");
//            Log.e("ERROR MESSAGE",e.getMessage());
        }finally {
            close(rs,statement);
        }

    }


    public String ejecutarComoTransaccion(String[] sentencias)
    {
       String pudoInsertar=null;
        String sentencia="BEGIN TRAN";
        String error="IF @@ERROR <> 0 BEGIN ROLLBACK TRAN return END";
        for(int i=0;i<sentencias.length;i++)
        {
            sentencia+="\n"+sentencias[i]+"\n"+error;
        }
       String fin="COMMIT TRAN";
        sentencia+="\n"+fin;

        try
        {
            Statement st=darConexion().createStatement();
            st.setQueryTimeout(QUERY_TIME_OUT_SECONDS+5);
            st.execute(sentencia);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Insert",sentencia);
            pudoInsertar=e.getMessage();

//            Log.e("SQLSTATE",e.getSQLState());
//            Log.e("ERRORCODE",e.getErrorCode()+"");
//            Log.e("ERROR MESSAGE",e.getMessage());
        }
        return pudoInsertar;
    }



    public String crearSentenciaDelete(String tabla,String[] columnas,String[] values)
    {
       String sentencia="DELETE FROM "+tabla+" WHERE";
        for(int i=0;i<columnas.length;i++)
        {
            if(i>0)
            {
                sentencia+=" AND";
            }
            sentencia+=" "+columnas[i]+"="+"'"+values[i]+"'";
        }

        return sentencia;

    }

    public void ejecutarSQL(String sentencia) throws ConnectionException, DataBaseException {
        Statement statement=null;
        ResultSet rs=null;
        try
        {
             statement=darConexion().createStatement();
            statement.setQueryTimeout(QUERY_TIME_OUT_SECONDS);
            statement.execute(sentencia);
        } catch (Exception e) {

                e.printStackTrace();
            if(statement==null) throw new ConnectionException("ERROR:Se perdio la conexio, intente de nuevo");
            else throw new DataBaseException(e.getMessage());

        }finally {
            close(rs,statement);
        }
        
    }

}


