package com.juans.inspeccion.Interfaz.Inspeccion_tabs;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.juans.inspeccion.CustomView.CustomView;
import com.juans.inspeccion.CustomView.MyEditText;
import com.juans.inspeccion.Interfaz.Dialogs.BusquedaDialog;
import com.juans.inspeccion.Interfaz.Dialogs.CompletarCampoDialog;
import com.juans.inspeccion.Interfaz.Dialogs.EditTextDialog;
import com.juans.inspeccion.Interfaz.Dialogs.ListViewDialog;
import com.juans.inspeccion.Interfaz.Dialogs.PhotoGridDialog;
import com.juans.inspeccion.Interfaz.Dialogs.SimpleDialog;
import com.juans.inspeccion.Interfaz.Dialogs.YesNoDialog;
import com.juans.inspeccion.Interfaz.InspeccionActivity;
import com.juans.inspeccion.Interfaz.InspeccionFragment;
import com.juans.inspeccion.Interfaz.MainActivity;
import com.juans.inspeccion.Mundo.Album;
import com.juans.inspeccion.Mundo.Consultas;
import com.juans.inspeccion.Mundo.Danio;
import com.juans.inspeccion.Mundo.DaniosManager;
import com.juans.inspeccion.Mundo.Data.DataLoader;
import com.juans.inspeccion.Mundo.FilaEnConsulta;
import com.juans.inspeccion.Mundo.Formularios;
import com.juans.inspeccion.Mundo.Inspeccion;
import com.juans.inspeccion.Mundo.Listas;
import com.juans.inspeccion.Mundo.MyCameraHelper;
import com.juans.inspeccion.R;
import com.juans.inspeccion.Varios;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Juan on 26/03/2015.
 */
public class p2 extends Fragment implements View.OnFocusChangeListener, InspeccionFragment, Formularios.DataPass {


    private LoaderManager.LoaderCallbacks consultasCallBacks;
    private final static int LOADER = 456;
    private final static String DANIO_CARGADO = "DC";
    private final static String ALBUM_CARGADO = "AC";
    private final static String INDEX = "INDEX";
    InspeccionActivity inspeccion;
    HashMap<String,String> infoInspeccion;
    Danio danioCargado;
    Album albumCargado;
    int itemSeleccionado = -1;
    ListView listaDaniosView;
    SimpleAdapter listaDaniosAdapter;
    ArrayList<CustomView> camposFormularioDanios;
    ListView danioList;
    Button btnAgregarDanio;
    Button btnEliminarDanio;
    Button btnActualizarDanio;
    Button btnLimpiarFormulario;
    Button btnTomarFoto;
    Button btnVerAlbum;

    MyEditText txtSecuenciaCorto;
    MyEditText txtSecuenciaLargo;
    MyEditText txtDescripcionReparacion;
    MyEditText txtTipoCalculo;
    MyEditText txtUsaTamano;
    MyEditText txtConCargoA;

    TextView lblValorTotal;
    TableLayout tablaDanios;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode== MyCameraHelper.REQUEST_TAKE_PHOTO)
        {
            if(resultCode== Activity.RESULT_OK)
            {
                String rutaFoto=MyCameraHelper.getFilePath();
                try {


                    albumCargado.agregarFoto(rutaFoto);
                    Intent intent =
                            new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    intent.setData(Uri.fromFile(new File(rutaFoto)));
                    getActivity().sendBroadcast(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // String tipoInspeccion = inspeccion.darTipoInspeccion();


        View view = inflater.inflate(R.layout.fragment_entrada_pag2, container, false);


        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        inspeccion = (InspeccionActivity) getActivity();
        infoInspeccion=inspeccion.getInfoInspeccion();
        createLoaderCallBacks();
        if (inspeccion.getSupportLoaderManager().getLoader(LOADER) != null) {

            inspeccion.getSupportLoaderManager().initLoader(LOADER, null, consultasCallBacks);

        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(DANIO_CARGADO, danioCargado);
        outState.putSerializable(ALBUM_CARGADO, albumCargado);
        outState.putInt(INDEX, itemSeleccionado);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        inspeccion = (InspeccionActivity) getActivity();
        if (savedInstanceState != null) {

            itemSeleccionado= savedInstanceState.getInt(INDEX);
            if(itemSeleccionado!=-1)
            {
                danioCargado=inspeccion.darListaDanios().get(itemSeleccionado);
                albumCargado=danioCargado.getAlbum();
            }
            else
            {
                danioCargado = (Danio) savedInstanceState.getSerializable(DANIO_CARGADO);
                albumCargado = (Album) savedInstanceState.getSerializable(ALBUM_CARGADO);
            }


        }

        listaDaniosView = (ListView) getView().findViewById(R.id.danioListView);
        ScrollView daniosSV = (ScrollView) getView().findViewById(R.id.danioScrollView);
        LayoutInflater l = getLayoutInflater(savedInstanceState);
        View mv = l.inflate(R.layout.nuevo_danio, null);

        daniosSV.addView(mv);

        inicializarFormulario();
        actualizarValorTotal();
        if (savedInstanceState == null) {
            btnActualizarDanio.setEnabled(false);
            btnEliminarDanio.setEnabled(false);
        }

        setAdaptador(view);
    }

    private void inicializarFormulario() {
        cargarCampos();
        CargarListaCampos cargarListaCampos = new CargarListaCampos();
        cargarListaCampos.execute();
        if(InspeccionActivity.soloLectura ||
                inspeccion.darTipoInspeccion().equals(InspeccionActivity.SALIDA))
        {

            btnTomarFoto.setEnabled(false);
            btnVerAlbum.setEnabled(false);
        }
        btnAgregarDanio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                agregarDanio();
            }
        });
        btnEliminarDanio = (Button) getView().findViewById(R.id.btnRemoverDanio);
        if (inspeccion.darListaDanios().size() == 0) {
            btnEliminarDanio.setEnabled(false);
            btnActualizarDanio.setEnabled(false);
        }

        btnLimpiarFormulario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                limpiarFormulario();
            }
        });
        btnEliminarDanio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                eliminarDanio();
            }
        });

        btnActualizarDanio.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // actualizarDanio(itemSeleccionado);
            }
        });
        if(inspeccion.darTipoInspeccion().equals(InspeccionActivity.SALIDA))
        {

        }
        btnTomarFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String numDoc = infoInspeccion.get(getResources().getString(R.string.NNUMDOC));
                String codcntr = infoInspeccion.get(getResources().getString(R.string.CCODCNTR));
                if (!TextUtils.isEmpty(numDoc)) {
                    String subFolder = inspeccion.darSubCarpetaFotos(numDoc, codcntr);
                        int numeroDanio=itemSeleccionado==-1?inspeccion.darListaDanios().size():itemSeleccionado;
                    //EJ  DANIO3_3
                    if(albumCargado==null ) albumCargado=new Album();
                    String nombreFoto="DANIO#"+numeroDanio+"_"+albumCargado.getFotos_tomadas();
                    String nombreArchivo = subFolder + "_" + nombreFoto;

                    boolean abrirCamara= MyCameraHelper.openCamera(p2.this, nombreArchivo, InspeccionActivity.CARPETA_FOTOS, subFolder);
                    if(!abrirCamara) Toast.makeText(getActivity(),"Error abriendo la camara",Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnVerAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Album album=albumCargado;
                if(album!=null && album.getFotos().size()!=0) {
                    PhotoGridDialog photoGridDialog = PhotoGridDialog.newInstance(albumCargado);
                    photoGridDialog.show(inspeccion.getSupportFragmentManager(), "LOLLOL");
                }
            }
        });


        listaDaniosView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                if(danioCargado==null && albumCargado!=null && albumCargado.getFotos().size()!=0) {
                    YesNoDialog dialog =
                            YesNoDialog.newInstance("Borrar Fotos?", "No ha agregado este daño a la lista, se perderan las fotos, \n ¿Esta Seguro?", "SI", "NO", p2.this,R.id.danioListView);

                    dialog.show(getActivity().getFragmentManager(), "LOL");



                }
                else {
                    itemSeleccionado = i;
                    mostrarDanioExistente(i);
                }

            }
        });
    }


    public void createLoaderCallBacks() {

        consultasCallBacks = new LoaderManager.LoaderCallbacks<Object[]>() {
            @Override
            public Loader onCreateLoader(int id, Bundle args) {

                btnAgregarDanio.setEnabled(false);
                btnEliminarDanio.setEnabled(false);
                btnActualizarDanio.setEnabled(false);
                btnLimpiarFormulario.setEnabled(false);
                danioList.setEnabled(false);
                boolean no_aplica = true;

                String ccargoa =getString(R.string.CCARGOA);
                danioCargado.getDetalles().put(ccargoa, txtConCargoA.getTexto());

                if (!txtConCargoA.getTexto().trim().equals(DaniosManager.NO_APLICA)) {
                    inspeccion.mostrarProgressDialog("Buscandos Costos", "Buscando los costos del daño...", false);
                    InspeccionActivity.loading=true;
                    no_aplica = false;

                }
                Bundle params = new Bundle();

                params.putSerializable(InspeccionActivity.MAPA_CAMPOS, infoInspeccion);
                params.putSerializable(InspeccionActivity.INFO_CONTENEDOR, inspeccion.getInspeccion().getDatosContenedor());
                params.putSerializable("DMG", danioCargado);
                return new BuscarTask(inspeccion, params, no_aplica);
            }

            @Override
            public void onLoadFinished(Loader<Object[]> loader, Object[] data) {
                inspeccion.cerrarProgressDialog();
                InspeccionActivity.loading=false;
                //en COSTO_TOTAL_DANIO esta el valor del daño
                Danio danio = (Danio) data[0];
                boolean busqueda = (boolean) data[1];
                boolean no_aplica = (boolean) data[2];

                if (!busqueda && !no_aplica) {
                    try {
                        SimpleDialog simpleDialog = SimpleDialog.newInstance("Tarifas no encontradas", "No se han encontrado las tarifas para este daño");
                        simpleDialog.show(inspeccion.getFragmentManager(), "asfa");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                }
                danioCargado.setDetalles(danio.getDetalles());
                mostrarDanio(danioCargado);
                btnAgregarDanio.setEnabled(true);
                btnEliminarDanio.setEnabled(true);
                btnActualizarDanio.setEnabled(true);
                btnLimpiarFormulario.setEnabled(true);
                danioList.setEnabled(true);

                inspeccion.getSupportLoaderManager().destroyLoader(LOADER);


            }

            @Override
            public void onLoaderReset(Loader loader) {

            }
        };



    }


    private void setAdaptador(View v) {
        try {


            listaDaniosAdapter = new SimpleAdapter(v.getContext(), DaniosManager.darListaSoloDetalles(inspeccion.darListaDanios()), R.layout.danio_listview_row,
                    new String[]{"NUM_ITEM", "CCODUBI", "CCODELE", "CCODDAN", "CCODMET", "NLARGO", "NANCHO", "NUNIDADES"}, new int[]{
                    R.id.lblNumItem, R.id.lblUbicacion, R.id.lblComponente, R.id.lblTipo, R.id.lblMetodo, R.id.lblLargo, R.id.lblAncho, R.id.lblCantidad}
            );
            listaDaniosView.setAdapter(listaDaniosAdapter);
            listaDaniosView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        } catch (Exception e) {


        }
    }


    private void actualizarValorTotal() {
        lblValorTotal.setText("COSTO TOTAL : $" + inspeccion.darDaniosManager().darValorTotal());
    }

    private void agregarDanio() {
        if (    InspeccionActivity.soloLectura ||
                inspeccion.darTipoInspeccion().equals(InspeccionActivity.SALIDA)) return;
        danioCargado = crearDanio();

        if (danioCargado != null )
        {
           String cargoA=danioCargado.getDetalles().get(getActivity().getResources().getString(R.string.CCARGOA));
           if(TextUtils.isEmpty(cargoA))
           {
               Toast.makeText(MainActivity.context, "DEBE INDICAR A CARGO DE QUIEN SE HACE LA REPARACION", Toast.LENGTH_SHORT).show();
           }
          else {
              // boolean copiarFotos=itemSeleccionado==-1;
               //if(copiarFotos && albumCargado!=null) danioCargado.setAlbum(albumCargado);
               inspeccion.darDaniosManager().agregarDanio(danioCargado);
               limpiarFormulario();
               setAdaptador(getView());
               actualizarValorTotal();
           }

        }

    }

    private Danio crearDanio() {
        Danio danio = new Danio();
        if (danioCargado != null) danio = inspeccion.darDaniosManager().copiarDanio(danioCargado);
        for (int i = 0; i < camposFormularioDanios.size(); i++) {
            CustomView cw = camposFormularioDanios.get(i);
            danio.getDetalles().put(cw.getNombreCampo(), cw.getTexto());

        }
        if(itemSeleccionado==-1 && albumCargado!=null)
        {
            danio.setAlbum(albumCargado);
        }

        return danio;
    }

    private void eliminarDanio() {
        if (InspeccionActivity.soloLectura ||
                inspeccion.darTipoInspeccion().equals(InspeccionActivity.SALIDA)) return;


        inspeccion.darDaniosManager().removerDanio(itemSeleccionado, inspeccion.getResources());
        setAdaptador(getView());
        limpiarFormulario();

        actualizarValorTotal();
    }

    /*Muestra un daño que aun no esta e la lista
        * */
    private void mostrarDanio(Danio danio) {
        Log.e("MostrarDanio",""+danio.getAlbum().getFotos_tomadas());
        for (int i = 0; i < camposFormularioDanios.size(); i++) {
            CustomView cw = camposFormularioDanios.get(i);
            cw.setTexto(danio.getDetalles().get(cw.getNombreCampo()));
        }
        danioCargado = danio;
        btnAgregarDanio.setEnabled(true);
    }

    /*Muestra un daño de la lista
    * */
    private void mostrarDanioExistente(int item) {
        Danio danio = inspeccion.darDaniosManager().darDanio(item);
        Log.e("MostrarDanioExistente",item+"#"+danio.getAlbum().getFotos_tomadas());
        for (int i = 0; i < camposFormularioDanios.size(); i++) {
            CustomView cw = camposFormularioDanios.get(i);
            cw.setTexto(danio.getDetalles().get(cw.getNombreCampo()));
        }
        danioCargado = danio;
        albumCargado=danio.getAlbum();
        btnAgregarDanio.setEnabled(true);
        btnEliminarDanio.setEnabled(true);
        btnActualizarDanio.setEnabled(true);
    }


    @Override
    public Object guardar() {
        return true;
    }

    @Override
    public Object actualizarMapa() {

        return null;
    }

    @Override
    public void limpiar() {
        limpiarFormulario();
        actualizarValorTotal();
        btnActualizarDanio.setEnabled(false);
        btnAgregarDanio.setEnabled(false);
        btnEliminarDanio.setEnabled(false);
        setAdaptador(getView());



    }

    @Override
    public void recibirInfo(Object... object) {

        inspeccion.darDaniosManager().recalcularValorDanios(getResources());
        actualizarValorTotal();

        setAdaptador(getView());
        infoInspeccion=inspeccion.getInfoInspeccion();


    }

    public void limpiarFormulario() {
        for (int i = 0; i < camposFormularioDanios.size(); i++) {
            camposFormularioDanios.get(i).limpiar();
        }
        danioCargado = null;
        albumCargado=null;
        itemSeleccionado=-1;
        btnActualizarDanio.setEnabled(false);
        btnEliminarDanio.setEnabled(false);

        ScrollView daniosSV = (ScrollView) getView().findViewById(R.id.danioScrollView);
        Log.e("Scroll", ""+daniosSV.getMaxScrollAmount());
        daniosSV.scrollTo(0,10);


    }

    @Override
    public void onDataReceive(Object data, int iniciadoPor) {
        if (data==null) {
            Log.e("Data", "null");
            return;
        }

        MyEditText editText = (MyEditText) getView().findViewById(iniciadoPor);
        FilaEnConsulta lista=null;
         if (data instanceof FilaEnConsulta){
         lista   =(FilaEnConsulta) data;
        }

        switch (iniciadoPor)
        {
            case R.id.danioListView:
                if((boolean)data)
                {
                    try {
                        albumCargado.borrarAlbum();
                        Toast.makeText(getActivity(),"Se eliminaron",Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            break;
            case R.id.txtSecuenciaCorto:
                txtSecuenciaCorto.setText(lista.getDato(0).trim());
            txtSecuenciaLargo.setText(lista.getDato(1).trim());
            break;
            case R.id.txtUbicacion:
            case R.id.txtComponente:
            case R.id.txtTipo:
            case R.id.txtMetodoReparacion:

                txtDescripcionReparacion.setTexto(lista.getDato(1));
                if (iniciadoPor == R.id.txtMetodoReparacion) {
                    txtTipoCalculo.setText(lista.getDato(2));
                    txtUsaTamano.setText(lista.getDato(3));
                    if (true) {
                        MyEditText largo = (MyEditText) getView().findViewById(R.id.txtLargo);
                        largo.setTexto("1");
                        MyEditText ancho = (MyEditText) getView().findViewById(R.id.txtAncho);
                        ancho.setTexto("1");
                        MyEditText cantidad = (MyEditText) getView().findViewById(R.id.txtCantidad);
                        cantidad.setTexto("1");
                    }

                }
            break;
            case R.id.txtConCargoA:
            editText.setTexto((String) data);
            inspeccion.getSupportLoaderManager().initLoader(LOADER, null, consultasCallBacks);
            break;

        }

        if ( lista!=null && !lista.getDato(0).trim().equals(editText.getTexto().trim())) {
            txtConCargoA.limpiar();

        }

        if(lista!=null)
        {
            editText.setTexto(lista.getDato(0));
        }






    }



    @Override
    public void onFocusChange(View view, boolean hasFocus) {

        if (!hasFocus) return;
        view.clearFocus();
        String nombreLista=null;
        String titulo=null;
        switch (view.getId()) {
            case (R.id.txtSecuenciaCorto):
                btnAgregarDanio.setEnabled(true);
                nombreLista=Listas.LISTA_SECUENCIAS;
                titulo="SECUENCIAS";

                break;

            case (R.id.txtSecuenciaLargo):
                nombreLista=Listas.LISTA_SECUENCIAS;
                titulo="SECUENCIAS";

                break;

            case (R.id.txtUbicacion):
                if (!txtSecuenciaCorto.getTexto().equals("")) {
                    HashMap<String, ArrayList<FilaEnConsulta>> listas=
                            (HashMap<String, ArrayList<FilaEnConsulta>>) Listas.darLista(Listas.LISTAS_DE_UBICACIONES);
                    ArrayList<FilaEnConsulta> listaUbicaciones = listas.get(txtSecuenciaCorto.getTexto());
                    BusquedaDialog.newInstance(listaUbicaciones, "UBICACIONES", this, R.id.txtUbicacion).show(getFragmentManager(), "ubicaciones");
                }
                break;

            case (R.id.txtComponente):
                nombreLista= Listas.LISTA_COMPONENTES;
                titulo="COMPONENTES";
                break;

            case (R.id.txtTipo):
                nombreLista=Listas.LISTA_TIPOS_DE_DANIO;
                titulo="TIPOS DE DAÑO";
                break;

            case (R.id.txtMetodoReparacion):
                nombreLista=Listas.LISTA_METODOS_DE_REPARACION;
                titulo= "METODOS DE REPARACION";
                break;

            case (R.id.txtConCargoA):
                if (validarDanio()) {
                    ListViewDialog.newInstance("Con cargo A?", MainActivity.context.getResources().getStringArray(R.array.lista_cargo_a), R.id.txtConCargoA, this).show(getFragmentManager(), "cargo");
                }
                break;

            default:
                if (view instanceof MyEditText && ((MyEditText) view).getMyInputType() == CustomView.DIALOGO_CON_EDIT_TEXT) {


                    EditTextDialog.newInstance(((MyEditText) view).getInputType(), view.getId(), ((MyEditText) view).getTexto(), this).show(getFragmentManager(), "input");

                }
                break;


        }
        if(nombreLista!=null)
        BusquedaDialog.newInstance((ArrayList<FilaEnConsulta>) Listas.darLista(nombreLista), titulo, this, view.getId()).show(getFragmentManager(), "somelist");

    }

    private boolean validarDanio() {
        boolean esValido = true;

        if (infoInspeccion == null) {
            Toast.makeText(inspeccion, "Ingrese un turno con contenedor primero", Toast.LENGTH_SHORT).show();
            esValido = false;
        } else {
            HashMap<String, String> nuevoDanio = new HashMap<String, String>();

            for (int i = 0; i < camposFormularioDanios.size(); i++) {
                CustomView cw = camposFormularioDanios.get(i);
                nuevoDanio.put(cw.getNombreCampo(), cw.getTexto());

            }

            HashMap<String,String> datosContenedor=inspeccion.getInspeccion().getDatosContenedor();
            String usaTamano = txtUsaTamano.getTexto();
            final Resources r =getResources();
            String linea = infoInspeccion.get(r.getString(R.string.CCTELNA));
            String tamano_contenedor = datosContenedor.get(r.getString(R.string.CTAMCNTR));

            String mensaje = null;

            if (linea == null || linea.equals(""))
                mensaje = "El campo de linea se encuentra vacio";
            else if (tamano_contenedor == null || tamano_contenedor.equals(""))
                mensaje = "el campo tamaño del contenedor se encuentra vacio";

            String tamano = datosContenedor.get(r.getString(R.string.CTAMCNTR));
            if (mensaje == null)
                mensaje = DaniosManager.validarDanio(r, nuevoDanio, usaTamano, tamano);

            if (mensaje != null) {
                Toast.makeText(getActivity(), mensaje, Toast.LENGTH_SHORT).show();
                esValido = false;

            } else {
                String fechaEntera[] = inspeccion.getInspeccion().getFechaInspeccion();
                String fecha = Varios.fechaDAOtoString(fechaEntera);
                String ctip = r.getString(R.string.CTIPDOC);
                String numDoc = r.getString(R.string.NNUMDOC);
                String fecha1 = r.getString(R.string.DFECMVTO);
                String anio = r.getString(R.string.NANO);
                String cctelna = r.getString(R.string.CCTELNA);

                String numItem = r.getString(R.string.NUM_ITEM);

                nuevoDanio.put(ctip, infoInspeccion.get(ctip));
                nuevoDanio.put(numDoc, infoInspeccion.get(numDoc));
                nuevoDanio.put(fecha1, fecha);

                nuevoDanio.put(anio, fechaEntera[Consultas.FECHA_ANIO]);
                nuevoDanio.put(cctelna, linea);
                nuevoDanio.put(numItem, "");
                danioCargado = new Danio();
                danioCargado.setDetalles(nuevoDanio);


            }

        }

        return esValido;

    }

    private void cargarCampos() {
        camposFormularioDanios = new ArrayList<CustomView>();
        tablaDanios = (TableLayout) getView().findViewById(R.id.tabla_nuevo_danio);
        btnAgregarDanio = (Button) getView().findViewById(R.id.btnAgregarDanio);
        btnActualizarDanio = (Button) getView().findViewById(R.id.btnActualizarDanio);
        btnLimpiarFormulario = (Button) getView().findViewById(R.id.btnLimpiarFormulario);
        btnTomarFoto = (Button) getView().findViewById(R.id.btnTomarFotoDanio);
        btnVerAlbum = (Button) getView().findViewById(R.id.btnVerFotosDanio);
        txtSecuenciaCorto = (MyEditText) getView().findViewById(R.id.txtSecuenciaCorto);
        txtSecuenciaLargo = (MyEditText) getView().findViewById(R.id.txtSecuenciaLargo);

        txtDescripcionReparacion = (MyEditText) getView().findViewById(R.id.txtDescripcionReparacion);
        txtTipoCalculo = (MyEditText) getView().findViewById(R.id.txtTipoCalculo);
        txtUsaTamano = (MyEditText) getView().findViewById(R.id.txtUsaTamano);
        txtConCargoA = (MyEditText) getView().findViewById(R.id.txtConCargoA);
        lblValorTotal = (TextView) getView().findViewById(R.id.lblValorTotal);
        danioList = (ListView) getView().findViewById(R.id.danioListView);


    }

    private class CargarListaCampos extends AsyncTask<Void, Void, Void> {


        @Override
        protected void onPostExecute(Void aVoid) {
            if (isAdded() && inspeccion.isCargarPendiente()) {
                listaDaniosAdapter.notifyDataSetChanged();
                inspeccion.setCargarPendiente(false);
                actualizarValorTotal();
            }
            actualizarValorTotal();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            Formularios.recorrerTableLayout(tablaDanios, camposFormularioDanios);
            Formularios.asignarInputDialog(camposFormularioDanios, p2.this);
            return null;

        }

        @Override
        protected void onPreExecute() {


        }


    }

    @Override
    public void onResume() {
        super.onResume();
        //Si hay un loader se recoencta a el
        if (albumCargado == null) Log.e("onResume", "album==nul");
        else
            Log.e("onResume", "" + albumCargado.getFotos_tomadas());
    }

    private static class BuscarTask extends DataLoader<Object[]> {
        Bundle params;
        boolean no_aplica;

        public BuscarTask(Context context, Bundle _params, boolean na) {            super(context);
            params = _params;
            no_aplica = na;

        }

        @Override
        public Object[] loadInBackground() {

            Danio danio = (Danio) params.getSerializable("DMG");

            HashMap<String, String> mapaCampos = (HashMap<String, String>) params.get(InspeccionActivity.MAPA_CAMPOS);
            HashMap<String, String> infoContenedor = (HashMap<String, String>) params.get(InspeccionActivity.INFO_CONTENEDOR);

            boolean busqueda = DaniosManager.BuscarInfoDanio(MainActivity.context.getResources(), danio.getDetalles(), mapaCampos, infoContenedor,no_aplica);

            return new Object[]{danio, busqueda, no_aplica};

        }
    }

}
