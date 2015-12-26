package com.juans.inspeccion.Interfaz;


import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.juans.inspeccion.ConnectionException;
import com.juans.inspeccion.CustomView.CustomView;
import com.juans.inspeccion.Interfaz.CustomAdapters.MyPagerAdapter;
import com.juans.inspeccion.Interfaz.Dialogs.MyProgressDialog;
import com.juans.inspeccion.Interfaz.Dialogs.SimpleDialog;
import com.juans.inspeccion.Interfaz.Dialogs.YesNoDialog;
import com.juans.inspeccion.Interfaz.Inspeccion_tabs.p1;
import com.juans.inspeccion.Interfaz.Inspeccion_tabs.p2;
import com.juans.inspeccion.Mundo.Album;
import com.juans.inspeccion.Mundo.ColumnasTablas;
import com.juans.inspeccion.Mundo.Consultas;
import com.juans.inspeccion.Mundo.Danio;
import com.juans.inspeccion.Mundo.Recibos.CreadorRecibos;
import com.juans.inspeccion.Mundo.DAO;
import com.juans.inspeccion.Mundo.DaniosManager;
import com.juans.inspeccion.Mundo.Data.DataLoader;
import com.juans.inspeccion.Mundo.Data.InspeccionDataSource;
import com.juans.inspeccion.Mundo.FilaEnConsulta;
import com.juans.inspeccion.Mundo.Formularios;
import com.juans.inspeccion.Mundo.Recibos.Impresora;
import com.juans.inspeccion.Mundo.Inspeccion;
import com.juans.inspeccion.Mundo.MyCameraHelper;
import com.juans.inspeccion.Mundo.Pendientes;
import com.juans.inspeccion.Other.SimpleService;
import com.juans.inspeccion.R;
import com.juans.inspeccion.Varios;


import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class InspeccionActivity extends ActionBarActivity implements ActionBar.TabListener, Formularios.DataPass, LoaderManager.LoaderCallbacks<Object> {


    public final static String CARPETA_FOTOS = "Inspecciones";

    public final static String VISUALIZAR = "VISUALIZAR";
    public final static String TIPO_ACCION="TIPO_ACCION";
    public final static String MODIFICAR_DOCUMENTO="MODIFICAR";

    public final static int LOADER_BUSCAR_DOCUMENTO = 0;
    public final static String NUM_DOC = "NUM_DOC";
    public final static String ENTRADA = "ENTRADA";
    public final static String SALIDA = "SALIDA";
    public final static String TIPO = "TIPO";
    public final static String TIPO_DOC = "TIPO_DOC";
    public final static String EP = "EP";
    public final static String ET = "ET";
    public final static String CARGAR_TURNO_PENDIENTE = "CTP";
    public final static String INDICE_TURNO = "INDICE";
    public final static String LISTA_CAMPOS = "LC";
    public final static String MAPA_CAMPOS = "MC";
    public final static String INFO_CONTENEDOR = "INFC";
    private static final int SALIR_DIALOG = 12;
    private static final String INSPECCION = "INSPECCION";
    public final int IMPRIMIR = 4324;
    public final int IMPRIMIR_GUARDADO = 914;

    public static boolean usaTurno=false;
    public static boolean soloLectura;
    public static boolean loading=false;
    public static String tipoAccion;
    public static void setTipoAccion(String ntipo)
    {
        tipoAccion=ntipo;
    }

    ViewPager viewPager = null;
    private ArrayList<CustomView> listaCampos;
    private boolean isCargarPendiente;
    private Inspeccion inspeccionObject;
    private int indiceTurno;
    ProgressDialog pDialog;
    private String[] fecha;

    private void cambiarTitulo() {
        String titulo = "";
        String tipo = inspeccionObject.getTipoInspeccion();
        if (inspeccionObject.getTipoInspeccion() == null) return;

        if (tipo.equals(ENTRADA)) {
            titulo = getResources().getString(R.string.ENTRADA_INSPECCION);

        }
        if (tipo.equals(SALIDA)) {
            titulo = getResources().getString(R.string.SALIDA_INSPECCION);
        }
        if (soloLectura) titulo = "BUSQUEDA DE IER";

        getSupportActionBar().setTitle(titulo);
    }


    @Override
    protected void onDestroy() {

        super.onDestroy();

    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(CARGAR_TURNO_PENDIENTE, isCargarPendiente);
        outState.putInt(INDICE_TURNO, indiceTurno);
        outState.putSerializable(INSPECCION, inspeccionObject);

    }

    @Override
    protected void onPause() {
        super.onPause();
        cerrarProgressDialog();
    }

    @Override
    public void onBackPressed() {
        preguntarSiCerrarInspeccion();
    }
    MyPagerAdapter myAdapter;
    protected void onCreate(Bundle savedInstanceState) {
        Log.e("Activty","OnCreate");
        new ActivityHelper(this);
        Intent intent = getIntent();


        if (savedInstanceState != null) {


            isCargarPendiente = savedInstanceState.getBoolean(CARGAR_TURNO_PENDIENTE);
            indiceTurno = savedInstanceState.getInt(INDICE_TURNO);
            inspeccionObject = (Inspeccion) savedInstanceState.getSerializable(INSPECCION);

        } else {
            String manejaTurno=ColumnasTablas.getInstance().darInfoEmpresa().get(getString(R.string.NMANEGATURNO));
            usaTurno=TextUtils.equals(manejaTurno,"1");
            loading=false;
            Inspeccion pendiente = (Inspeccion) intent.getSerializableExtra(Pendientes.PENDIENTE);
            if (pendiente != null) {

                listaCampos = new ArrayList<>();
                isCargarPendiente = true;
                indiceTurno = intent.getIntExtra(INDICE_TURNO, -1);
                inspeccionObject = pendiente;


            } else {
                listaCampos = new ArrayList<CustomView>();
                inspeccionObject = new Inspeccion();
                inicializar();
                inspeccionObject.setTipoInspeccion(intent.getStringExtra(TIPO));
                inspeccionObject.setTipoDocumento(intent.getStringExtra(TIPO_DOC));
                soloLectura = false;


            }


        }
        //This super.Oncreate  restores the fragments so we gotta load things from saved instance to have inspeccionObject!=null
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_inspeccion);

        viewPager = (ViewPager) findViewById(R.id.pager);


        cambiarTitulo();
        inspeccionObject.setUsaTurno(usaTurno);
        final android.support.v7.app.ActionBar actionBar = getSupportActionBar();


        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
       // addTabs(actionBar);
      //  MyAdapter myAdapter = new MyAdapter(getSupportFragmentManager());
         myAdapter=new MyPagerAdapter(getSupportFragmentManager(),viewPager,getSupportActionBar());
        addTabs(null);
        viewPager.setAdapter(myAdapter);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        boolean visualizar = intent.getBooleanExtra(VISUALIZAR, false);

        if (visualizar || tipoAccion!=null) {

            getSupportLoaderManager().initLoader(LOADER_BUSCAR_DOCUMENTO, intent.getExtras(), this);
            if (visualizar)
            soloLectura = true;
        }
        if (getSupportLoaderManager().getLoader(LOADER_BUSCAR_DOCUMENTO) != null)

            getSupportLoaderManager().initLoader(LOADER_BUSCAR_DOCUMENTO, null, this);



    }

    private void inicializar() {


        isCargarPendiente = false;
        indiceTurno = -1;
        inspeccionObject.limpiar();


    }

    private void addTabs(ActionBar actionBar) {
        myAdapter.addTab(new p1(),"INFORMACION");

                if (darTipoEmpresa().equals(EP)) {
            myAdapter.addTab(new p2(),"DAÑOS");

        }


//        ActionBar.Tab tab1 = actionBar.newTab();
//        tab1.setText(R.string.informacion);
//        tab1.setTabListener(this);
//
//        ActionBar.Tab tab2 = actionBar.newTab();
//        tab2.setText(R.string.danio);
//        tab2.setTabListener(this);
//
//
//        actionBar.addTab(tab1);
//
//
//        if (darTipoEmpresa().equals(EP)) {
//            actionBar.addTab(tab2);
//
//        }





    }





    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MyCameraHelper.REQUEST_TAKE_PHOTO) {
            if (resultCode == Activity.RESULT_OK) {
                String rutaFoto = MyCameraHelper.getFilePath();
                try {
                    inspeccionObject.getFotosGenerales().agregarFoto(rutaFoto);
                    Intent intent =
                            new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    intent.setData(Uri.fromFile(new File(rutaFoto)));
                    sendBroadcast(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        }
    }


    Menu menu;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.entrada, menu);
        getMenuInflater().inflate(R.menu.inspeccion_contenedor, menu);
        MenuItem terminar_luego = menu.findItem(R.id.action_terminar_luego);
        MenuItem guardar = menu.findItem(R.id.action_guardar_todo);
        MenuItem imprimir=menu.findItem(R.id.action_imprimir);
        MenuItem test1=menu.findItem(R.id.action_test_1);
        test1.setVisible(false);

        if (soloLectura) {

            terminar_luego.setVisible(false);
            guardar.setVisible(false);
        }else if(tipoAccion!=null) {
            terminar_luego.setVisible(false);
            imprimir.setVisible(false);
        }else{
            imprimir.setVisible(false);
        }
        this.menu=menu;
        return super.onCreateOptionsMenu(menu);
    }


    public void cambiarPag1(String tipoTurno)
    {
        FrameLayout fl= (FrameLayout) findViewById(R.id.frame_layout);
        fl.removeAllViews();
        if(tipoTurno.equals(InspeccionActivity.ENTRADA))
        fl.addView(View.inflate(this, R.layout.entrada_empresa_transporte, null));
        else
        fl.addView(View.inflate(this, R.layout.salida_empresa_transporte, null));
        p1 fragment = (p1) viewPager.getAdapter().instantiateItem(viewPager, 0);
        fragment.inicializarFormulario();



    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        int pagina = viewPager.getCurrentItem();


        Fragment fragment1 = (Fragment) viewPager.getAdapter().instantiateItem(viewPager, 0);
        Fragment fragment2=null;
        if(myAdapter.getCount()>1) {
             fragment2 = (Fragment) viewPager.getAdapter().instantiateItem(viewPager, 1);
        }
        final InspeccionFragment frag1 = (InspeccionFragment) fragment1;
        final InspeccionFragment frag2 = (InspeccionFragment) fragment2;


        if (id == R.id.action_guardar_todo) {
            //Ambas paginas validan sus campos para saber si se puede guardar
            if ((boolean) frag1.guardar()) {

                if (frag2 == null) guardar(frag1,frag2);
                else if ((boolean) frag2.guardar()) {
                    guardar(frag1,frag2);
                }
            }

        } else if (id == R.id.action_terminar_luego) {


            if (inspeccionObject.getFechaInspeccion() == null || inspeccionObject.getInformacion().size() < 5) {
                Toast.makeText(getApplicationContext(), "No ha cargado ningun turno o contenedor", Toast.LENGTH_SHORT).show();
            } else if (darTipoInspeccion() != null && darTipoEmpresa() != null) {

                frag1.actualizarMapa();
                if (darTipoEmpresa().equals(EP)) {
                    frag2.actualizarMapa();
                }

                if (indiceTurno == -1) {

                    Pendientes.agregarInspeccion(inspeccionObject, InspeccionActivity.this);
                } else {
                    Pendientes.remplazarTurno(indiceTurno, inspeccionObject, InspeccionActivity.this);
                }

                Toast.makeText(getApplicationContext(), "Se ha agregado el turno lista de pendientes", Toast.LENGTH_LONG).show();
                this.finish();
            }

        }
        if (id == R.id.action_imprimir) {
            if (inspeccionObject != null && inspeccionObject.getInformacion().size() > 5) {
                if (!soloLectura) {   //Si es de solo lectura asi el usuario cambie el formulario no se imprimiran los cambios
                    frag1.guardar();
                }
                preguntarSiImprimir();
            }

        }
        else if(id==R.id.action_imprimir_y_borrar)
        {
            imprimirDespuesDeGuardar();
        }
        else if(id==R.id.action_configurar_impresora)
        {
            Intent intent=new Intent(this,ImpresoraActivity.class);
            startActivity(intent);
        }

        if(id==R.id.action_test_1)
        {
            Log.e("Hue","CambioAhue");
            try {
                renombrarFotos("21412", "heuhue32");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        return super.onOptionsItemSelected(item);
    }



    public void enviarInfo(int pag, Object... objects) {
        if(pag>=myAdapter.getCount()) return;
        Fragment fragment = (Fragment) viewPager.getAdapter().instantiateItem(viewPager, pag);
        if (fragment != null && fragment.isAdded()) {
            ((InspeccionFragment) fragment).recibirInfo(objects);
        }
    }


    public void limpiarTodo() {
        MenuItem menuItem=menu.findItem(R.id.action_imprimir_y_borrar);
        menuItem.setVisible(false);
        inicializar();
        darListaDanios().clear();
        viewPager.setCurrentItem(0);
        Fragment fragment = (Fragment) viewPager.getAdapter().instantiateItem(viewPager, 0);
        ((InspeccionFragment) fragment).limpiar();
        if(myAdapter.getCount()>1) {

            Fragment fragment2 = (Fragment) viewPager.getAdapter().instantiateItem(viewPager, 1);
            if (darTipoEmpresa().equals(EP)) {
                ((InspeccionFragment) fragment2).limpiar();
            }
        }


    }

    public Inspeccion getInspeccion() {
        return inspeccionObject;
    }

    public Album darAlbumGeneral() {
        return inspeccionObject.getFotosGenerales();
    }

    public String darTipoInspeccion() {
        return inspeccionObject.getTipoInspeccion();
    }

    public String darTipoEmpresa() {
        return inspeccionObject.getTipoDocumento();
    }

    /*ejem: ENTRADA_EP_201216703_CCL887676
    * */
    public String darSubCarpetaFotos(String numDoc, String codcntr) {

        return darTipoInspeccion() + "_" + darTipoEmpresa() + "_" + numDoc + "_" + codcntr;
    }

    public HashMap<String, String> getInfoInspeccion() {
        return inspeccionObject.getInformacion();
    }


    public void inicializarListaCampos() {
        listaCampos = new ArrayList<>();
    }

    public ArrayList<CustomView> darListaCampos() {
        return listaCampos;
    }

    public ArrayList<Danio> darListaDanios() {
        return inspeccionObject.getDaniosManager().getListaDanios();
    }

    public boolean isCargarPendiente() {
        return isCargarPendiente;
    }

    public DaniosManager darDaniosManager() {
        return inspeccionObject.getDaniosManager();
    }


    public void setCargarPendiente(boolean cargar) {
        isCargarPendiente = cargar;
    }


    public void generarRecibo() {





        synchronized (this) {
            try {

                CreadorRecibos.generarFactura(inspeccionObject, getBaseContext());
                Thread.sleep(1000);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        new YesNoDialog().show(getFragmentManager(), "yesno");


    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction fragmentTransaction) {
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onDataReceive(Object data, int iniadoPor) {
        boolean hacer = (boolean) data;
        switch (iniadoPor) {

            case IMPRIMIR:
                try {
                    if (hacer) imprimir();
                    else Impresora.getInstance().desconectar();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case IMPRIMIR_GUARDADO:
                try {
                    if (hacer) imprimir();
                    else {
                        Impresora.getInstance().desconectar();
                        inspeccionObject.setFechaInspeccion(null);
                        limpiarTodo();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;


            case MyProgressDialog.PROGRESS_DIALOG:
                preguntarSiCerrarInspeccion();

                break;
            case SALIR_DIALOG:
                if (hacer) finish();
                break;

        }


    }

    private void imprimir() {
        boolean hayConfig=Impresora.getInstance().cargarDatosImpresora(getApplicationContext());
        if (!hayConfig)Toast.makeText(this,"No ha configurado ninguna impresora",Toast.LENGTH_LONG).show();
        else {
            if (!Impresora.getInstance().conectado()) {
                Impresora.getInstance().conectar();
            }
            generarRecibo();
        }
    }

    public void imprimirDespuesDeGuardar() {
        Impresora.getInstance().cargarDatosImpresora(getBaseContext());
        YesNoDialog imprimir = YesNoDialog.newInstance("Guardó correctamente", "Desea imprimir?", "Imprimir", "No, Gracias", IMPRIMIR_GUARDADO);


        imprimir.show(getFragmentManager(), "yesno");
    }


    public void preguntarSiImprimir() {
        Impresora.getInstance().cargarDatosImpresora(getBaseContext());
        YesNoDialog imprimir = YesNoDialog.newInstance("Imprimir", "Desea imprimir?", "Imprimir", "No, Gracias", IMPRIMIR);
        imprimir.show(getFragmentManager(), "yesno");
    }

    public void preguntarSiCerrarInspeccion() {
        YesNoDialog salir = YesNoDialog.newInstance("Salir", "Desea salir esta Inspeccion?", "Salir", "No, Gracias", SALIR_DIALOG);
        salir.show(getFragmentManager(), "salir");
    }





    public void mostrarProgressDialog(String titulo, String mensaje, boolean finishActivityIfClosed) {
        try {

            pDialog = MyProgressDialog.newInstance(this,titulo,mensaje,finishActivityIfClosed);
            pDialog.show();


        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public void editarProgressDialog(String titulo, String mensaje) {
        try {
            pDialog.setTitle(titulo);
            pDialog.setMessage(mensaje);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public void cerrarProgressDialog() {

        try {
            if (pDialog != null )
                pDialog.dismiss();
            pDialog=null;


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sePerdioLaConexion() {
        SimpleDialog dialog = SimpleDialog.newInstance("Sin Conexion", "Se perdio la conexion con la base de datos, intente nuevamenete");
        dialog.show(getFragmentManager(), "noCon");
    }



    @Override
    public Loader<Object> onCreateLoader(int id, Bundle args) {


        loading=true;
        mostrarProgressDialog("Buscando ", "Un momento por favor...", true);
        String numDoc = args.getString(NUM_DOC);
        return new BuscarDocumentoTask(InspeccionActivity.this, numDoc, inspeccionObject.getTipoInspeccion());

    }

    @Override
    public void onLoadFinished(Loader<Object> loader, Object le_result) {



        loading=false;
        cerrarProgressDialog();
        if(le_result instanceof String)
        {
            Toast.makeText(this,(String)le_result,Toast.LENGTH_LONG).show();
            return;

        }
        Inspeccion result= (Inspeccion) le_result;
        if (result == null) {
            sePerdioLaConexion();
            return;
        }
        HashMap<String, String> info = result.getInformacion();
        inspeccionObject.setInformacion(info);
        inspeccionObject.setDatosContenedor(result.getDatosContenedor());
        inspeccionObject.getDaniosManager().setListaDanios(result.getDaniosManager().getListaDanios());



        if (result.getInformacion().size() == 0) {
            String codCntr = getIntent().getStringExtra(NUM_DOC);
            SimpleDialog dialog = SimpleDialog.newInstance("EIR no encontrado", "El contenedor " + codCntr + "no tiene registrada una " + darTipoInspeccion());
            dialog.show(getFragmentManager(), "asf");

        } else {
            info.put(getString(R.string.CTIPOTURNO),info.get(getString(R.string.CTIPOMOV)));
            String tipoDoc=info.get(getString(R.string.CTIPDOC));
            inspeccionObject.setTipoDocumento(tipoDoc);
            if(TextUtils.equals(tipoDoc,ET))
            {
                cambiarPag1(darTipoInspeccion());
                myAdapter.removePage(viewPager,myAdapter,1);
            }
            enviarInfo(0, null); //Actualiza la interfaz de ambos fragmentos
            enviarInfo(1, null);
        }
        getSupportLoaderManager().destroyLoader(LOADER_BUSCAR_DOCUMENTO);



    }

    @Override
    public void onLoaderReset(Loader<Object> loader) {

    }

    public String[] getFechaActual() {

        return fecha;
    }


    class MyAdapter extends FragmentPagerAdapter {


        public MyAdapter(FragmentManager fm)

        {
            super(fm);
        }



        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            if (position == 0) {
                fragment = new p1();

            } else if (position == 1) {
                fragment = new p2();
            }
            return fragment;
        }

        @Override
        //Numero de pestañas
        public int getCount() {
            int count = 2;
            if (darTipoEmpresa().equals(ET)) {
                count = 1;
            }

            return count;
        }

    }
    //debe ser llamado antes de guardar en P1
    private void cambiarNumeroDocumento(String nuevoNumDoc)
    {
        Fragment fragment1 = (Fragment) viewPager.getAdapter().instantiateItem(viewPager, 0);
        p1 frag1 = (p1) fragment1;
        frag1.cambiarEIR(nuevoNumDoc);

        inspeccionObject.getDaniosManager().cambiarEIR(nuevoNumDoc,getResources());





    }




    private void guardar(final InspeccionFragment pag1,InspeccionFragment pag2) {


        CustomView txtTurno = (CustomView) findViewById(R.id.txtTurno);
        final String tablaCabecera = txtTurno.getVaPara();
        final String tablaDanios = getResources().getString(R.string.TBDETMOVPATIO);

        AsyncTask<Void, Void, String> guardarTodo = new AsyncTask<Void, Void, String>() {
            String nuevoNum=null;
            @Override
            protected void onPreExecute() {

                Toast.makeText(MainActivity.context, "Guardando", Toast.LENGTH_SHORT).show();
            }

            @Override
            protected String doInBackground(Void... params) {

                InspeccionDataSource dataSource = new InspeccionDataSource();

                try {
                    String fecha[] =DAO.getInstance().darFecha();
                    p1 tab1=(p1) pag1;

                    inspeccionObject.getDaniosManager().actualizarFechas(fecha, getResources());

                    if(tipoAccion!=null && tipoAccion.equals(MODIFICAR_DOCUMENTO))
                    {
                        String fechaEntera = Varios.fechaDAOtoString(fecha);
                        String fechaLog = getResources().getString(R.string.DFECHALOG);
                        inspeccionObject.getInformacion().put(fechaLog, fechaEntera);
                        dataSource.re_insert(listaCampos,inspeccionObject,getResources());

                    }
                    else {
                        tab1.actualizarFecha(fecha);
                        if(!usaTurno)
                        {
                            nuevoNum=dataSource.generarConsecutivoSinTurno(inspeccionObject,getResources());
                            cambiarNumeroDocumento(nuevoNum);
                            String agenteLinea=dataSource.darAgenteLinea( inspeccionObject.getInformacion() , getResources());
                            inspeccionObject.getInformacion().put(getString(R.string.TER_RAZONS),agenteLinea);
                        }
                        dataSource.insert(listaCampos, inspeccionObject, getResources());
                        if(!usaTurno)
                        {
                            renombrarFotos(nuevoNum,inspeccionObject.getInformacion().get(getString(R.string.CCODCNTR)));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return e.getMessage();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String error) {
                if(Varios.isActityEnding(InspeccionActivity.this)) return;
                String mensaje = "GUARDADO CORRECTO\"";
                if (error != null) mensaje = error;
                Toast.makeText(getBaseContext(), mensaje, Toast.LENGTH_LONG).show();
                if (error == null) {
                    if (indiceTurno != -1) Pendientes.borrarTurno(indiceTurno, getBaseContext());
                    imprimirDespuesDeGuardar();
                    MenuItem menuItem=menu.findItem(R.id.action_imprimir_y_borrar);
                    menuItem.setVisible(true);

                }
            }
        };
        guardarTodo.execute();

    }
    public void renombrarFotos(String numDoc,String codCntr) throws IOException {

        String subCarpeta=darSubCarpetaFotos(numDoc, codCntr);



        Album general=inspeccionObject.getFotosGenerales();
        File oldFolder=null;
        //Cambia las generales
        for(int i=0;i<general.getFotos().size();i++)
        {

            File oldname=new File(general.getFotos().get(i));
            if(i==0) oldFolder=oldname.getParentFile();
            String nuevoNombre =subCarpeta+"_"+i;
            File newname= MyCameraHelper.createImageFile(nuevoNombre,CARPETA_FOTOS,subCarpeta);
            oldname.renameTo(newname);
            Intent intent =
                    new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intent.setData(Uri.fromFile(oldname));
            sendBroadcast(intent);
            Intent intent2 =
                    new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intent2.setData(Uri.fromFile(newname));
            sendBroadcast(intent2);
            general.getFotos().set(i,newname.getPath());

        }
        ArrayList<Danio> danios=inspeccionObject.getDaniosManager().getListaDanios();
        //Cambia las de los daños
        for(int i=0;i<danios.size();i++)
        {
            Album album=danios.get(i).getAlbum();
            for(int j=0;j<album.getFotos().size();j++)
            {
                String rutaFoto=album.getFotos().get(j);
                File oldName=new File(rutaFoto);
                String nombreFoto=subCarpeta+"_DANIO#"+i+"_"+j;
                File newName= MyCameraHelper.createImageFile(nombreFoto,CARPETA_FOTOS,subCarpeta);
                oldName.renameTo(newName);

                Intent intent =
                        new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                intent.setData(Uri.fromFile(oldName));
                sendBroadcast(intent);
                Intent intent2 =
                        new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                intent2.setData(Uri.fromFile(newName));
                sendBroadcast(intent2);
                album.getFotos().set(j,newName.getPath());

            }
        }

        FileUtils.deleteDirectory(oldFolder);

    }
    public void enviarFotos() {
        String carpetaDeLaInspeccion = Environment.getExternalStorageDirectory() + "/" + CARPETA_FOTOS;
        String numdoc = getInfoInspeccion().get(getResources().getString(R.string.NNUMDOC));
        String ccodcntr = inspeccionObject.getDatosContenedor().get(getResources().getString(R.string.CCODCNTR));
        String subFolder = darSubCarpetaFotos(numdoc, ccodcntr);
        carpetaDeLaInspeccion += "/" + subFolder;
        Inspeccion fotosInsp = new Inspeccion(carpetaDeLaInspeccion);
        ArrayList<Danio> danios = darDaniosManager().getListaDanios();
        for (int i = 0; i < danios.size(); i++) {
            Album album = danios.get(i).getAlbum();
            if (album.getFotos().size() != 0)
                fotosInsp.agregarAlbum(album);
        }
        if (inspeccionObject.getFotosGenerales().getFotos().size() != 0)
            fotosInsp.agregarAlbum(inspeccionObject.getFotosGenerales());


        Intent intent = new Intent(this, SimpleService.class);
        intent.putExtra(SimpleService.INSPECCION, fotosInsp);
        intent.putExtra(SimpleService.PARAMS, ColumnasTablas.getInstance().darInfoEmpresa());

    }


    @Override
    protected void onResume() {
        super.onResume();
        if(loading && pDialog==null) {
            pDialog= MyProgressDialog.lastInstance(this);
            pDialog.show();
        }

    }

    private static class BuscarDocumentoTask extends DataLoader<Object> {
        String numDoc;
        String tipoInspeccion;
        String tipoLoader;

        public BuscarDocumentoTask(Context context, String numDoc, String tipoInspeccion) {
            super(context);
            this.numDoc = numDoc;
            this.tipoInspeccion = tipoInspeccion;

        }

        @Override
        public Object loadInBackground() {
            InspeccionDataSource dataSource = new InspeccionDataSource();
            Inspeccion inspeccion = null;
            try {
                inspeccion = dataSource.read(numDoc, tipoInspeccion, tipoAccion,getContext().getResources());
            } catch (Exception e) {
                e.printStackTrace();
                return e.getMessage();
            }
            return inspeccion;


        }
    }

}