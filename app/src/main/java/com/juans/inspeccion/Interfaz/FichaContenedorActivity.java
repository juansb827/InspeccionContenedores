package com.juans.inspeccion.Interfaz;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.HttpAuthHandler;
import android.widget.EditText;
import android.widget.Toast;

import com.juans.inspeccion.ConnectionException;

import com.juans.inspeccion.DiccionariosException;
import com.juans.inspeccion.Interfaz.Dialogs.CompletarCampoDialog;
import com.juans.inspeccion.Interfaz.Dialogs.EditTextDialog;
import com.juans.inspeccion.CustomView.CustomView;
import com.juans.inspeccion.Interfaz.Dialogs.MyProgressDialog;
import com.juans.inspeccion.Mundo.Consultas;
import com.juans.inspeccion.Mundo.Data.DataLoader;
import com.juans.inspeccion.Mundo.Data.InspeccionDataSource;
import com.juans.inspeccion.Mundo.FilaEnConsulta;
import com.juans.inspeccion.Mundo.Formularios;
import com.juans.inspeccion.CustomView.MyEditText;
import com.juans.inspeccion.Mundo.Inspeccion;
import com.juans.inspeccion.Mundo.Listas;
import com.juans.inspeccion.R;
import com.juans.inspeccion.Varios;

import java.util.ArrayList;
import java.util.HashMap;

public class FichaContenedorActivity extends AppCompatActivity implements Formularios.DataPass, View.OnFocusChangeListener, InspeccionFragment, LoaderManager.LoaderCallbacks<Object> {

    public final static String TYPE = "TYPE";
    public final static String CREATE = "CREATE";
    public final static String UPDATE = "UPDATE";
    public final static String INSPECCION = "INSPECCION";
    // usar mientras arregla startActivtyForresult
    public static String ultimoCntr;

    private  HashMap<String,String> codigoOriginal;


    ActivityHelper activityHelper;
    ArrayList<CustomView> listaCampos;

    ////    MyEditText txtCodigoContenedor;
////    MyEditText txtSigla;
////    MyEditText txtSerie;
////    MyEditText txtDigitoVerificacion;
////    MyEditText txtLinea;
////
////    MyEditText txtTipo;
////    MyEditText txtTamanio;
////    MyEditText txtMaterial;
//    private GoneView txtFechaInsercion;
    private HashMap<String, String> mapaCampos;
    private Inspeccion inspeccion;
    public final static String MAPA_CAMPOS = "MAPA_CAMPOS";
    private final static int LOADER = 11;
    private static boolean loading = false;
    String tipoOperacion;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("1",inspeccion);
        outState.putSerializable(TYPE, tipoOperacion);
        outState.putSerializable("ORI", codigoOriginal);


    }
    Bundle savedInstance=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        activityHelper = new ActivityHelper(this);
        setContentView(R.layout.activity_ficha_contenedor);
        savedInstance=savedInstanceState;
        if (savedInstanceState != null) {


            tipoOperacion = savedInstanceState.getString(TYPE);
            inspeccion= (Inspeccion) savedInstanceState.get("1");

            codigoOriginal= (HashMap<String, String>) savedInstanceState.get("ORI");
            mapaCampos=inspeccion.getDatosContenedor();

        } else {
            loading=false;
            tipoOperacion = getIntent().getStringExtra(TYPE);
            codigoOriginal=new HashMap<>();
            inspeccion= (Inspeccion) getIntent().getSerializableExtra(INSPECCION);
            mapaCampos=inspeccion.getDatosContenedor();

            cargarCodigoContenedor();
        }


        getSupportActionBar().setTitle("FICHA DEL CONTENDOR");
        setCampos();
        if (loading) activityHelper.mostrarProgressDialog();

    }


    public void setCampos()

    {
        listaCampos = new ArrayList<CustomView>();
        CargarListaCampos cargarListaCampos = new CargarListaCampos();
        cargarListaCampos.execute();


    }

    public void cargarCodigoContenedor() {
        String codigo=inspeccion.getInformacion().get(getString(R.string.CCODCNTR));
        String linea=getString(R.string.CCTELNA);
        mapaCampos.put(linea,inspeccion.getInformacion().get(linea));

        int longitud = codigo.length();
        if (codigo != "" && longitud > 4) {
            String sigla = codigo.substring(0, 4);
            String serie = codigo.substring(4, longitud - 1);
            String digitoVer = codigo.substring(longitud - 1);
            mapaCampos.put(getString(R.string.CCODCNTR),codigo);
            mapaCampos.put(getString(R.string.CSIGLACNTR), sigla);
            mapaCampos.put(getString(R.string.CSERIECNTR), serie);
            mapaCampos.put(getString(R.string.CDIGVERCNTR), digitoVer);
            mapaCampos.put(getString(R.string.CMATCNTR), "AC");

            codigoOriginal.put(getString(R.string.CSIGLACNTR), sigla);
            codigoOriginal.put(getString(R.string.CSERIECNTR), serie);
            codigoOriginal.put(getString(R.string.CDIGVERCNTR), digitoVer);
            codigoOriginal.put(getString(R.string.CCODCNTR),codigo);

        }
    }

    public void armarCodigoContenedor() {
        String codigo = "";
        codigo += mapaCampos.get(getString(R.string.CSIGLACNTR));
        codigo += mapaCampos.get(getString(R.string.CSERIECNTR));
        codigo += mapaCampos.get(getString(R.string.CDIGVERCNTR));
        mapaCampos.put(getString(R.string.CCODCNTR), codigo);


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.ficha_contenedor, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case (R.id.action_guardar_contenedor):
                guardar();
                break;


        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onDataReceive(Object lista, int iniciadoPor) {

        EditText temp = (EditText) findViewById(iniciadoPor);
        String mensaje = null;
        String texto = null;
        boolean codigo=false;
        if (lista instanceof FilaEnConsulta) {
            texto = ((FilaEnConsulta) lista).getDato(0);
            if (texto == null) texto = "";
        }

        switch (iniciadoPor) {
            case R.id.txtSigla:
                if (texto.length() != 4) mensaje = "La SIGLA debe  tener 4 letras";
                else codigo=true;
                break;
            case R.id.txtSerie:
                if (texto.length() != 6) mensaje = "La SERIE debe tener 6 numeros";
                codigo=true;
                break;
            case R.id.txtDigitoVerificacion:
                if (texto.length() != 1) mensaje = "El DIGITO debe tener 1 numero";
                else codigo=true;
                break;
            case R.id.txtFechaSiguienteInspeccion:
            case R.id.txtFechaFabricacion:
                texto = Formularios.validarFecha(texto);
                if (texto == null) mensaje = "El formato es AAAAMM";
                break;
            case R.id.txtTipoContenedor:
            case R.id.txtTamanoContenedor:
                temp.setText(texto.toUpperCase());
                texto=null;
                MyEditText txtTipo = (MyEditText) findViewById(R.id.txtTipoContenedor);
                MyEditText txtTamanio = (MyEditText) findViewById(R.id.txtTamanoContenedor);
                String tipo = txtTipo.getTexto();
                String tamano = txtTamanio.getTexto();
                if (!TextUtils.isEmpty(tipo) && !TextUtils.isEmpty(tamano)) {

                    getSupportLoaderManager().initLoader(LOADER, null, this);
                }
                break;
        }


        if(mensaje!=null) Toast.makeText(this,mensaje,Toast.LENGTH_SHORT).show();
        else if (texto != null) {
            temp.setText(texto.toUpperCase());
            if(codigo) actualizarCodigo();
        }


    }


    private void actualizarCodigo() {
        activityHelper.actualizarMapaCampos(listaCampos, mapaCampos);
        armarCodigoContenedor();
        activityHelper.actualizarInterfaz(listaCampos, mapaCampos);
    }



    @Override
    public void onFocusChange(View view, boolean b) {
        if (b) {
            view.clearFocus();
            switch (view.getId()) {

                case (R.id.txtTipoContenedor):

                    CompletarCampoDialog.newInstance((ArrayList<FilaEnConsulta>) Listas.darLista(Listas.LISTA_TIPOS_CONTENEDOR), "TIPO DEL CONTENEDOR", this, R.id.txtTipoContenedor).show(getSupportFragmentManager(), "tipoContenedor");
                    break;

                case (R.id.txtTamanoContenedor):

                    CompletarCampoDialog.newInstance((ArrayList<FilaEnConsulta>) Listas.darLista(Listas.LISTA_TAMANOS_CONTENEDOR), "TAMAÑO DEL CONTENEDOR", this, R.id.txtTamanoContenedor).show(getSupportFragmentManager(), "tamanoContenedor");
                    break;

                case (R.id.txtMaterialContenedor):

                    CompletarCampoDialog.newInstance((ArrayList<FilaEnConsulta>) Listas.darLista(Listas.LISTA_MATERIALES_CONTENEDOR), "MATERIAL DEL CONTENEDOR ", this, R.id.txtMaterialContenedor).show(getSupportFragmentManager(), "materialContenedor");
                    break;

                default:
                    if (view instanceof MyEditText && ((MyEditText) view).getMyInputType() == CustomView.DIALOGO_CON_EDIT_TEXT) {


                        EditTextDialog.newInstance(((MyEditText) view).getInputType(), view.getId(), ((MyEditText) view).getTexto(), this).show(getSupportFragmentManager(), "input");

                    } else if (view instanceof MyEditText && ((MyEditText) view).getMyInputType() == CustomView.DIALOGO_CON_DATE_PICKER) {
                        String texto=Formularios.editarFecha(((MyEditText) view).getTexto());
                        EditTextDialog edtd=EditTextDialog.newInstance(InputType.TYPE_CLASS_NUMBER, view.getId(), texto, this);
                        edtd.setPlaceHolder("AAAAMM");
                        edtd.show(getSupportFragmentManager(), "input");


                    }
                    break;

            }


        }
    }


    @Override
    public void onBackPressed() {
        Varios.onBackPressed(this, "Atencion:", "No ha guardado el contenedor, Esta seguro?");
        return;
    }

    public boolean comprobarCamposObligatorios() {
        return Formularios.comprobarCamposObligatorios(listaCampos);

    }


    @Override
    public HashMap<String, String> guardar() {

        if (!comprobarCamposObligatorios()) {
            Toast.makeText(getBaseContext(), "FALTAN CAMPOS OBLIGATORIOS", Toast.LENGTH_SHORT).show();
            return null;
        }
        activityHelper.actualizarMapaCampos(listaCampos, mapaCampos);

        AsyncTask<Void, Void, String> laTask = new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {
                String mensaje = null;
                String fecha[] = Consultas.darFecha();
                String fechaEntera = Varios.fechaDAOtoString(fecha);
                String horaConMinutos = Varios.fechaDAOtoHora(fecha);
                mapaCampos.put(getString(R.string.DFECHALOG), fechaEntera);

                InspeccionDataSource inspeccionDataSource = new InspeccionDataSource();
                try {
                    if (tipoOperacion.equals(CREATE))
                        inspeccionDataSource.insertContenedor(listaCampos, inspeccion,codigoOriginal, getResources());
                    else if (tipoOperacion.equals(UPDATE))
                        inspeccionDataSource.updateContenedor(listaCampos, inspeccion,codigoOriginal, getResources());

                } catch (Exception e) {
                    mensaje = e.getMessage();
                }
                return mensaje;
            }

            @Override
            protected void onPostExecute(String o) {
                if (isFinishing()) return;
                String mensaje = o;
                if (o == null) {
                    mensaje = "SE GUARDO EL CONTENEDOR";
                    Intent returnIntent = new Intent();
                    ultimoCntr = mapaCampos.get(getString(R.string.CCODCNTR));
                    setResult(RESULT_OK, returnIntent);
                }
                Toast.makeText(FichaContenedorActivity.this, mensaje, Toast.LENGTH_LONG).show();
                if (o == null) finish();


            }
        };
        laTask.execute();
        return null;

    }

    @Override
    public Object actualizarMapa() {
        return null;
    }


    @Override
    public void limpiar() {

    }

    @Override
    public void recibirInfo(Object... object) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public Loader<Object> onCreateLoader(int id, Bundle args) {
        loading = true;
        activityHelper.mostrarProgressDialog();
        activityHelper.actualizarMapaCampos(listaCampos, mapaCampos);
        Bundle params = new Bundle();

        params.putSerializable(MAPA_CAMPOS, mapaCampos);
        return new LoadTask(this, params);
    }

    @Override
    public void onLoadFinished(Loader<Object> loader, Object data) {
        loading = false;
        activityHelper.cerrarProgressDialog();
        if (data == null) {
            activityHelper.sePerdioLaConexion();
            return;
        }
        mapaCampos.put(getString(R.string.CCODISOCNTR), (String) data);
        activityHelper.actualizarInterfaz(listaCampos, mapaCampos);
        getSupportLoaderManager().destroyLoader(LOADER);
    }

    @Override
    public void onLoaderReset(Loader<Object> loader) {

    }


    @Override
    protected void onPause() {
        super.onPause();
        activityHelper.cerrarProgressDialog();
    }

    private class CargarListaCampos extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            if (Varios.isActityEnding(FichaContenedorActivity.this)) return null;
            ViewGroup tabla = (ViewGroup) findViewById(R.id.tabla_ficha_contenedor);
            Formularios.recorrerTableLayout(tabla, listaCampos);
            Formularios.asignarInputDialog(listaCampos, FichaContenedorActivity.this);
            //Si es la primera vez que se crea hay que cargar el intent

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (Varios.isActityEnding(FichaContenedorActivity.this)) return;
            super.onPostExecute(aVoid);
            activityHelper.actualizarInterfaz(listaCampos, mapaCampos);
        }
    }

    private static class LoadTask extends DataLoader<Object> {

        Bundle params;
        Context c;

        public LoadTask(Context context, Bundle _params) {
            super(context);
            params = _params;
            c = context;
        }

        @Override
        public Object loadInBackground() {
            HashMap<String, String> mapa = (HashMap<String, String>) params.get(MAPA_CAMPOS);
            String sigla = "";
            try {
                sigla = Consultas.darSiglaIso(mapa.get(c.getString(R.string.CTIPOCNTR)), mapa.get(c.getString(R.string.CTAMCNTR)));
            } catch (ConnectionException e) {
                e.printStackTrace();
                return null;
            }
            return sigla;
        }
    }


}
