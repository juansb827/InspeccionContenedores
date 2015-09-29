package com.juans.inspeccion.Interfaz;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.juans.inspeccion.Mundo.Consultas;
import com.juans.inspeccion.Mundo.DAO;
import com.juans.inspeccion.Mundo.FilaEnConsulta;
import com.juans.inspeccion.Mundo.Formularios;
import com.juans.inspeccion.Mundo.Listas;
import com.juans.inspeccion.R;
import com.juans.inspeccion.Varios;
import com.juans.inspeccion.Interfaz.Dialogs.CompletarCampoDialog;

import java.io.File;
import java.util.ArrayList;

public class ConsultasActivity extends ActionBarActivity implements Formularios.DataPass{

    public final static String HACER_CONSULTA_STOCK_DETALLADO="STOCK_DETALLADO";
    public final static String HACER_CONSULTA_STOCK_RESUMIDO="STOCK_RESUMIDO";
    public final static String HACER_CONSULTA_TURNOS_REGISTRADOS="TURNOS_REGISTRADOS";
    public final static String TIPO_CONSULTA="TIPO";
    private String tipoConsulta;
    private String seleccion;
    private String[] fechaDB;
    private String paramFecha;
    private ArrayList<FilaEnConsulta> lineas;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consultas);
         tipoConsulta=getIntent().getStringExtra(TIPO_CONSULTA);
        cargarConsulta();


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.consultas, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void cargarConsulta()
    {



           Cargar cargar=new Cargar();
            cargar.execute();



    }


    @Override
    public void onDataReceive(Object lista, int autor) {
        seleccion= ((FilaEnConsulta)lista).getDato(0);
        String consulta= Consultas.consultaStockDetallada(seleccion.trim());
        ConsultaExcel consultaExcel=new ConsultaExcel();
        consultaExcel.execute(consulta);


    }



    @Override
    public void onBackPressed() {
        this.finish();
    }

    private class Cargar extends AsyncTask<String,Void,Void>
    {
        @Override
        protected void onPreExecute() {
            if (ConsultasActivity.this!=null)
            {
                Varios.lockRotation(getResources(), ConsultasActivity.this);
            }
        }

        @Override
        protected Void doInBackground(String... strings) {
             fechaDB=DAO.getInstance().darFecha();

             paramFecha=fechaDB[Consultas.FECHA_ANIO]+ fechaDB[Consultas.FECHA_MES]+fechaDB[Consultas.FECHA_DIA];
            if(tipoConsulta.equals(ConsultasActivity.HACER_CONSULTA_STOCK_DETALLADO)) {

                lineas = (ArrayList<FilaEnConsulta>) Listas.darLista(Listas.LISTA_LINEAS);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            if(tipoConsulta.equals(HACER_CONSULTA_STOCK_DETALLADO)) {
                if(!ConsultasActivity.this.isFinishing()  ) {
                    FragmentManager fm=getSupportFragmentManager();
                    android.support.v4.app.FragmentTransaction ft =fm.beginTransaction();


                    CompletarCampoDialog dg=CompletarCampoDialog.newInstance(lineas, "LINEAS", ConsultasActivity.this, R.id.txtEstadoFinal);
//                    dg.setCloseParentOnDismiss(true);
//                    ft.add(dg, "fsafaf");
//                    ft.commitAllowingStateLoss();
                      dg.show(getSupportFragmentManager(), "lineas");
                }

            }
            else if (tipoConsulta.equals(HACER_CONSULTA_STOCK_RESUMIDO))
            {
                String consulta=DAO.FUNCION_STOCK_CONTENEDORES_RESUMIDO+"'"+paramFecha+"'";
                ConsultaExcel consultaExcel=new ConsultaExcel();
                consultaExcel.execute(consulta);
            }
            else
            {
                String NANO=" NANO="+fechaDB[Consultas.FECHA_ANIO];
                String NMES=" AND NMES="+fechaDB[Consultas.FECHA_MES];
                String NDIA=" AND NDIA="+fechaDB[Consultas.FECHA_DIA];
                String parametros=NANO+NMES+NDIA;

                String consulta=DAO.FUNCION_TURNOS_DEL_DIA+parametros+" ORDER BY NTURNO";
                ConsultaExcel consultaExcel=new ConsultaExcel();
                consultaExcel.execute(consulta);
            }




        }
    }

    private class ConsultaExcel extends AsyncTask<String,Void,File>
    {

        @Override
        protected void onPreExecute() {
            Toast.makeText(ConsultasActivity.this,"Un momento por favor...", Toast.LENGTH_LONG).show();
        }

        @Override
        protected File doInBackground(String... strings) {


                if(!ConsultasActivity.this.isFinishing())
                {
                    ConsultasActivity.this.finish();
                }

              String consulta=strings[0];
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File folder = new File(path, "Contenedores");
            folder.mkdir();

            File archivoExcel=new File(path, "/Contenedores/"+tipoConsulta+".xls");
            //String[] fecha=DAO.getInstance().darFecha();
            //String marcaArchivo=fecha[FECHA_DIA]+"-"+fecha[FECHA_MES]+"-"+fecha[FECHA_ANIO]+"   "+fecha[FECHA_HORA]+"_"+fecha[FECHA_MINUTOS];
            DAO.getInstance().generarExcelConsulta(consulta, archivoExcel, 1000);
              return  archivoExcel;
        }

        @Override
        protected void onPostExecute(File file) {
            try {
                Varios.abrirExcel(ConsultasActivity.this, file);
            }catch (ActivityNotFoundException e)
            {
                Toast.makeText(getBaseContext(),"NECESITA INSTALAR UN VISUALIZADOR DE ARCHIVOS EXCEL",Toast.LENGTH_LONG).show();
            }

            if(ConsultasActivity.this!=null || !ConsultasActivity.this.isFinishing())
            {
                ConsultasActivity.this.finish();

            }


        }
    }


}
