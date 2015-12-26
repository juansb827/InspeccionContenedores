package com.juans.inspeccion.Interfaz;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v4.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.godex.Godex;
import com.juans.inspeccion.Interfaz.Dialogs.EditTextDialog;
import com.juans.inspeccion.Interfaz.Dialogs.ListViewDialog;
import com.juans.inspeccion.Interfaz.Dialogs.MyProgressDialog;
import com.juans.inspeccion.Interfaz.Dialogs.YesNoDialog;
import com.juans.inspeccion.Mundo.DAO;
import com.juans.inspeccion.Mundo.ColumnasTablas;
import com.juans.inspeccion.Mundo.Data.DataLoader;
import com.juans.inspeccion.Mundo.FilaEnConsulta;
import com.juans.inspeccion.Mundo.Formularios;
import com.juans.inspeccion.Mundo.Listas;
import com.juans.inspeccion.Mundo.MyCameraHelper;
import com.juans.inspeccion.Mundo.Recibos.Impresora;
import com.juans.inspeccion.Mundo.Recibos.RecibosTest;
import com.juans.inspeccion.Other.BroadcastObserver;
import com.juans.inspeccion.R;


import java.io.InputStream;
import java.util.Objects;
import java.util.Observable;
import java.util.Observer;


public class MainActivity extends ActionBarActivity implements Formularios.DataPass, LoaderManager.LoaderCallbacks<Object>, Observer {


    public static Context context;
    private static int LOADER = 1;
    final static int LIST_MODIFICAR_DOCUMENTO = 89;
    public static boolean loading = false;
    public static boolean retrying = false;

    final static int SALIR_DIALOG = 567;
    Handler handler;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        new ActivityHelper(this);
        if(savedInstanceState==null) loading=false;
        DAO.getInstance().cargarDatosConexion(this);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();
        handler=new Handler();

        BroadcastObserver.instance().addObserver(this);


    }

    ProgressDialog progressDialog;

    public void mostrarProgress(String titulo, String mensaje) {

       if(progressDialog!=null && progressDialog.isShowing()) progressDialog.dismiss();
        progressDialog = new MyProgressDialog(MainActivity.this, true);

        progressDialog.setTitle(titulo);
        progressDialog.setMessage(mensaje);
        progressDialog.setIndeterminate(false);
        progressDialog.setCanceledOnTouchOutside(false);

        progressDialog.show();

    }

    public void cerrarProgres() {
        if (progressDialog != null && progressDialog.isShowing()) progressDialog.dismiss();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cerrarProgres();
    }

    public void ConsultaStockResumido(View v) {
        Intent intent = new Intent(this, ConsultasActivity.class);
        intent.putExtra(ConsultasActivity.TIPO_CONSULTA, ConsultasActivity.HACER_CONSULTA_STOCK_RESUMIDO);
        startActivity(intent);
    }

    public void ConsultaStockDetallado(View v) {
        Intent intent = new Intent(this, ConsultasActivity.class);
        intent.putExtra(ConsultasActivity.TIPO_CONSULTA, ConsultasActivity.HACER_CONSULTA_STOCK_DETALLADO);
        startActivity(intent);
    }


    public void ConsultaTurnosRegistrados(View v) {
        Intent intent = new Intent(this, ConsultasActivity.class);
        intent.putExtra(ConsultasActivity.TIPO_CONSULTA, ConsultasActivity.HACER_CONSULTA_TURNOS_REGISTRADOS);
        startActivity(intent);
    }

    public void nuevaEntradaEP(View v) {
        InspeccionActivity.setTipoAccion(null);
        Intent intent = new Intent(this, InspeccionActivity.class);

        intent.putExtra(InspeccionActivity.TIPO, InspeccionActivity.ENTRADA);
        intent.putExtra(InspeccionActivity.TIPO_DOC, InspeccionActivity.EP);
        startActivity(intent);
    }

    public void nuevaEntradaET(View v) {
        InspeccionActivity.setTipoAccion(null);
        Intent intent = new Intent(this, InspeccionActivity.class);

        intent.putExtra(InspeccionActivity.TIPO, InspeccionActivity.ENTRADA);
        intent.putExtra(InspeccionActivity.TIPO_DOC, InspeccionActivity.ET);
        startActivity(intent);
    }

    public void nuevaSalidaEP(View v) {
        InspeccionActivity.setTipoAccion(null);
        Intent intent = new Intent(this, InspeccionActivity.class);

        intent.putExtra(InspeccionActivity.TIPO, InspeccionActivity.SALIDA);
        intent.putExtra(InspeccionActivity.TIPO_DOC, InspeccionActivity.EP);
        startActivity(intent);
    }

    public void nuevaSalidaET(View v) {
        InspeccionActivity.setTipoAccion(null);
        Intent intent = new Intent(this, InspeccionActivity.class);

        intent.putExtra(InspeccionActivity.TIPO, InspeccionActivity.SALIDA);
        intent.putExtra(InspeccionActivity.TIPO_DOC, InspeccionActivity.ET);
        startActivity(intent);
    }

    public void turnosPendientes(View v) {

        Intent intent = new Intent(getApplicationContext(), PendientesActivity.class);
        startActivity(intent);

    }

    public void buscarDocumento(View v) {
        EditTextDialog dialog = EditTextDialog.newInstance(InputType.TYPE_CLASS_TEXT, this, "Buscar EIR", "Ingrese el codigo del contenedor", R.id.btnBuscarDocumento);
        dialog.show(getSupportFragmentManager(), "eir");
    }

    public void modificarDocumento(View v) {
        EditTextDialog dialog = EditTextDialog.newInstance(InputType.TYPE_CLASS_TEXT, this, "Modifica Guia", "Ingrese el codigo del contenedor", R.id.btnModificarDoc);
        dialog.show(getSupportFragmentManager(), "edit");
    }


    public void probarFicha(View v)

    {

        //Drawable d = Drawable.createFromPath("/data/data/com.juans.inspeccion/files/logoEmpresa.jpg");
        //Button probar = (Button) findViewById(R.id.button);
        //probar.setBackground(d);
        //String file=Environment.getExternalStorageDirectory()+"/lafotosocio.jgp";
//      1111

//        Intent i=new Intent(MainActivity.this,SimpleService.class);
//        startService(i);

//        Intent intent= new  Intent(getApplicationContext(), FichaContenedorActivity.class);
//        startActivity(intent);
//        File file= CreadorFacturasPDF.generarFactura(this);
//        Varios.abrirPDF(this, file);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_help) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void sePerdioLaConexion() {
        Toast.makeText(this, "Error al sincronizar, reintenado", Toast.LENGTH_SHORT).show();
        //SimpleDialog dialog=SimpleDialog.newInstance("Error","Se perdio la conexion con la base de datos, intente nuevamenete");
        //dialog.show(getFragmentManager(), "noCon");
    }

    private static String numDocBusqueda = "";

    @Override
    protected void onDestroy() {
        super.onDestroy();


    }

    @Override
    public void onDataReceive(Object data, int autor) {

        switch (autor) {
            case R.id.btnBuscarDocumento:
                numDocBusqueda = ((FilaEnConsulta) data).getDato(0);
                if (numDocBusqueda != null && numDocBusqueda.length() != 11) {
                    Toast.makeText(this, "Codigo invalido", Toast.LENGTH_LONG).show();
                } else {
                    ListViewDialog list = ListViewDialog.newInstance("Tipo de EIR", new String[]{"ENTRADA", "SALIDA"}, 1, this);
                    list.show(getSupportFragmentManager(), "tag");
                }
                break;
            case R.id.btnModificarDoc:
                numDocBusqueda=((FilaEnConsulta) data).getDato(0);
                if (numDocBusqueda != null && numDocBusqueda.length() != 11) {
                    Toast.makeText(this, "Codigo invalido", Toast.LENGTH_LONG).show();
                } else {
                    ListViewDialog list = ListViewDialog.newInstance("Tipo de EIR", new String[]{"ENTRADA", "SALIDA"}, LIST_MODIFICAR_DOCUMENTO, this);
                    list.show(getSupportFragmentManager(), "tag");
                }
                break;

            case 1: {
                Intent intent = new Intent(this, InspeccionActivity.class);
                intent.putExtra(InspeccionActivity.TIPO, (String) data);
                intent.putExtra(InspeccionActivity.TIPO_DOC, InspeccionActivity.EP);
                intent.putExtra(InspeccionActivity.VISUALIZAR, true);
                intent.putExtra(InspeccionActivity.TIPO_ACCION, InspeccionActivity.VISUALIZAR);
                InspeccionActivity.setTipoAccion(InspeccionActivity.VISUALIZAR);
                intent.putExtra(InspeccionActivity.NUM_DOC, numDocBusqueda);
                startActivity(intent);
            }
                break;
            case LIST_MODIFICAR_DOCUMENTO: {
                Intent intent = new Intent(this, InspeccionActivity.class);
                intent.putExtra(InspeccionActivity.TIPO, (String) data);
                intent.putExtra(InspeccionActivity.TIPO_DOC, InspeccionActivity.EP);
                intent.putExtra(InspeccionActivity.TIPO_ACCION, InspeccionActivity.MODIFICAR_DOCUMENTO);
                InspeccionActivity.setTipoAccion(InspeccionActivity.MODIFICAR_DOCUMENTO);
                intent.putExtra(InspeccionActivity.NUM_DOC, numDocBusqueda);
                startActivity(intent);
            }

                break;



            case MyProgressDialog.PROGRESS_DIALOG:
                YesNoDialog salir = YesNoDialog.newInstance("Salir", "Desea salir de la aplicacion", "Salir", "No, Gracias", SALIR_DIALOG);
                salir.show(getFragmentManager(), "salir");
                break;
            case SALIR_DIALOG:
                if ((boolean) data) finish();
                break;


        }


    }


    @Override
    protected void onResume() {
        super.onResume();
        if (loading && progressDialog == null)
            mostrarProgress("Sincronizando", "Un momento porfavor...");
        else if (retrying && progressDialog == null)
            mostrarProgress("Error de Conexion", "Reintentado \n Un momento por favor...");

        if (getSupportLoaderManager().getLoader(LOADER) != null)
            getSupportLoaderManager().initLoader(LOADER, null, this);

        if (!ColumnasTablas.getInstance().estanTablas(context) || !ColumnasTablas.getInstance().isHayInfoEmpresa() || !Listas.noInspeccionEstanCargadas || !Listas.importantesEstanCargadas) {
            getSupportLoaderManager().initLoader(LOADER, null, this);
        }
    }


    @Override
    public android.support.v4.content.Loader<Object> onCreateLoader(int id, Bundle args) {
        if (!retrying) loading = true;

        if (loading)
            mostrarProgress("Sincronizando", "Un momento porfavor...");
        else if (retrying)
            mostrarProgress("Error de Conexion", "Reintentado \n Un momento por favor...");

        return new InfoEmpresa(context);

    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Object> loader, Object data) {

        //setSupportProgressBarIndeterminateVisibility(false);
        //progressBar.setVisibility(View.GONE);
        //dialog.hide();
        cerrarProgres();
        if ( data instanceof Exception ) {
            Toast.makeText(this,"Error:"+((Exception) data).getMessage(),Toast.LENGTH_LONG).show();
            loading = false;
            retrying = true;

            getSupportLoaderManager().destroyLoader(LOADER);
            getSupportLoaderManager().initLoader(LOADER, null, MainActivity.this);


        } else {
            getSupportLoaderManager().destroyLoader(LOADER);
            retrying = false;
            loading = false;
        }


    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Object> loader) {
        Log.e("Loader", "onLoaderReset");

    }

    @Override
    public void update(Observable observable, final Object internet) {

        Log.e("Observable", "" + internet);

    }


    private static class InfoEmpresa extends DataLoader<Object> {

        public InfoEmpresa(Context context) {
            super(context);
        }

        @Override

        public Object loadInBackground() {
            Log.e("Start","start");
            try {
                if (retrying) Thread.sleep(10 * 1000);
                if (!ColumnasTablas.getInstance().estanTablas(context)) {
                    long tiempo1 = System.currentTimeMillis();
                    ColumnasTablas.getInstance().setContext(context);
                    ColumnasTablas.getInstance().inicializarTablas(context);
                    long tiempo2 = System.currentTimeMillis();

                    long t = (tiempo2 - tiempo1);

                    Log.e("tiempos", String.valueOf(t));

                }

                if (!ColumnasTablas.getInstance().isHayInfoEmpresa()) {

                    long tiempo1 = System.currentTimeMillis();
                    ColumnasTablas.getInstance().cargarInfoEmpresa(context);
                    long lefinal = System.currentTimeMillis() - tiempo1;
                    Log.e("tiemposFotos", "" + lefinal);

                }

                if (!Listas.importantesEstanCargadas) {
                    long tiempo1 = System.currentTimeMillis();
                    Listas.cargarListasInspecion(context);
                    long lefinal = System.currentTimeMillis() - tiempo1;
                    Log.e("tiempoListasEstaticas", "" + lefinal);
                }
                if (!Listas.noInspeccionEstanCargadas) {
                    long tiempo1 = System.currentTimeMillis();
                    Listas.cargarListasNoInspeccion(context);
                    long lefinal = System.currentTimeMillis() - tiempo1;
                    Log.e("tiempoOtrasListas", "" + lefinal);
                }


            } catch (Exception e) {
                e.printStackTrace();
                Log.e("ErrorCargando",e.getMessage());
                MainActivity.retrying=true;
                return e;

            }
            return true;
        }
    }


}
