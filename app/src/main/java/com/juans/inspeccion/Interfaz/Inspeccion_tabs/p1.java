package com.juans.inspeccion.Interfaz.Inspeccion_tabs;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.juans.inspeccion.CustomView.CustomView;
import com.juans.inspeccion.CustomView.MyEditText;
import com.juans.inspeccion.Interfaz.Dialogs.BusquedaDialog;
import com.juans.inspeccion.Interfaz.Dialogs.CompletarCampoDialog;
import com.juans.inspeccion.Interfaz.Dialogs.ConsultaCortaDialog;
import com.juans.inspeccion.Interfaz.Dialogs.EditTextDialog;
import com.juans.inspeccion.Interfaz.Dialogs.PhotoGridDialog;
import com.juans.inspeccion.Interfaz.Dialogs.YesNoDialog;
import com.juans.inspeccion.Interfaz.FichaContenedorActivity;
import com.juans.inspeccion.Interfaz.InspeccionActivity;
import com.juans.inspeccion.Interfaz.InspeccionFragment;
import com.juans.inspeccion.Interfaz.LoginActivity;
import com.juans.inspeccion.Interfaz.MainActivity;
import com.juans.inspeccion.Interfaz.UIEffects;
import com.juans.inspeccion.Mundo.Consultas;
import com.juans.inspeccion.Mundo.Data.DataLoader;
import com.juans.inspeccion.Mundo.Data.InspeccionDataSource;
import com.juans.inspeccion.Mundo.FilaEnConsulta;
import com.juans.inspeccion.Mundo.Formularios;
import com.juans.inspeccion.Mundo.Inspeccion;
import com.juans.inspeccion.Mundo.Listas;
import com.juans.inspeccion.Mundo.MyCameraHelper;
import com.juans.inspeccion.Mundo.Util.FTPUpload;
import com.juans.inspeccion.R;
import com.juans.inspeccion.Varios;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Juan on 26/03/2015.
 */
public class p1 extends Fragment implements View.OnFocusChangeListener, InspeccionFragment, Formularios.DataPass {
    InspeccionActivity inspeccion;
    TextView lbNumContenedor;
    TableLayout tablaInfoContenedor;
    MyEditText txtTurno;
    MyEditText txtCodContenedor;
    static String tipoBusqueda;
    private LoaderManager.LoaderCallbacks consultasCallBacks;
    Handler handler;


    private final static String BUSCAR_CABECERA = "BT";
    private final static String BUSCAR_TURNOS_PENDIENTES = "BTP";
    private final static String BUSCAR_INFO_CONTENEDORES = "BIC";
    private final static String BUSCAR_DANIOS_DE_CONTENEDOR = "BD";
    private final static String BUSCAR_NOMBRE_CLIENTE = "BNC";
    private final static String BUSCAR_TIPO_ACTIVIDAD = "BTAC";
    private final static int LOADER = 777;
    private final static int LOADER_EXTRA = 999;
    private final static int DIALOG_MODIFICAR_CONTENEDOR = 854;

    private final static int DIALOG_TURNOS_PENDIENTES = 498;


    private HashMap<String, String> infoInspeccion;

    private final static int DIALOG_CREAR_CONTENEDOR=5473;
    private static String ultimoCodigoContenedor;

    public final static int CREAR_CONTENEDOR = 1;
    public final String[] booking_cols = new String[]{"CTIPOCNTR", "CTAMCNTR", "CESTFINCNTR", "NSOLICITADOS"};
    public final String[] info_turno_cols = new String[]{"NTURNO", "CCODCNTR", "CPLACA", "CCTELNA"};
    public final int[] dialog_columns = new int[]{R.id.columna1, R.id.columna2, R.id.columna3, R.id.columna4};






    @Override
    public void onResume() {
        super.onResume();


    }

    @Override
    /*/
    /Determina que formulario cargar dependiendo si es entrada o salida
     */
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.e("Fragment1", "OnCreateView");
        View view = null;

        String tipoInspeccion = inspeccion.darTipoInspeccion();
        if (tipoInspeccion.equals(InspeccionActivity.ENTRADA)) {
            if (inspeccion.darTipoEmpresa().equals(InspeccionActivity.ET)) {
                view = inflater.inflate(R.layout.entrada_empresa_transporte, container, false);
            } else {
                view = inflater.inflate(R.layout.fragment_entrada_pag1, container, false);
            }
        } else if (tipoInspeccion.equals(InspeccionActivity.SALIDA)) {
            if (inspeccion.darTipoEmpresa().equals(InspeccionActivity.ET)) {
                view = inflater.inflate(R.layout.salida_empresa_transporte, container, false);
            } else {
                view = inflater.inflate(R.layout.fragment_salida_pag1, container, false);
            }

        }


        return view;

    }




    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.e("Fragment1", "OnCreate");
        super.onCreate(savedInstanceState);
        inspeccion = (InspeccionActivity) getActivity();
        infoInspeccion = inspeccion.getInfoInspeccion();
        handler = new Handler();

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Log.e("Fragment1", "OnViewCreated");
        super.onViewCreated(view, savedInstanceState);

        inicializarFormulario();
        createLoaderCallBacks();
        Loader loader1 = inspeccion.getSupportLoaderManager().getLoader(LOADER);
        Loader loader2 = inspeccion.getSupportLoaderManager().getLoader(LOADER_EXTRA);
        if (loader1 != null)
            inspeccion.getSupportLoaderManager().initLoader(LOADER, null, consultasCallBacks);

        else if (loader2 != null)
            inspeccion.getSupportLoaderManager().initLoader(LOADER_EXTRA, null, consultasCallBacks);


        try {
            inicializarNoUsaTurno(savedInstanceState==null);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //Prepara las listas que este formulario va a necesitar
    public void inicializarFormulario() {
        cargarCampos();
        CargarListaCampos listaCampos = new CargarListaCampos();
        listaCampos.execute();

        cargarFecha();



        //
        //Campo donde se busca el turno

        txtTurno.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (keyEvent != null && keyEvent.getAction() != keyEvent.getAction())
                    return false;
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Service.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(txtTurno.getWindowToken(), 0);
                buscarTurno(txtTurno.getTexto());
                return true;


            }
        });



        //
        Button tomarFoto = (Button) getView().findViewById(R.id.btnAgregarFoto);
        //No todas los formularios tienen opcion de foto
        if (tomarFoto != null) {
            tomarFoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String numDoc = infoInspeccion.get(getResources().getString(R.string.NNUMDOC));
                    String codcntr = infoInspeccion.get(getResources().getString(R.string.CCODCNTR));
                    if (!TextUtils.isEmpty(numDoc)) {
                        String subFolder = inspeccion.darSubCarpetaFotos(numDoc, codcntr);

                        String nombreArchivo = subFolder + "_" + inspeccion.darAlbumGeneral().getFotos_tomadas();
                        boolean abrirCamara = MyCameraHelper.openCamera(inspeccion, nombreArchivo, InspeccionActivity.CARPETA_FOTOS, subFolder);
                        if (!abrirCamara)
                            Toast.makeText(getActivity(), "Error abriendo la camara", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        final AsyncTask hueTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                ArrayList<String> fotos = inspeccion.darAlbumGeneral().getFotos();

                try {
                    FTPUpload uploader = new FTPUpload("70.38.10.203", 21, "focus01", "Contabilidad2015");
                    uploader.conect();
                    String numDoc = infoInspeccion.get(getResources().getString(R.string.NNUMDOC));
                    String codcntr = infoInspeccion.get(getResources().getString(R.string.CCODCNTR));
                    String subFolder = inspeccion.darSubCarpetaFotos(numDoc, codcntr);
                    for (String foto : fotos) {
                        // uploader.uploadSingleFile(foto, subFolder);
                    }
                    uploader.disconect();


                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };

        Button verFotos = (Button) getView().findViewById(R.id.btnVerFotos);
        if (verFotos != null) {
            verFotos.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PhotoGridDialog photoGridDialog = PhotoGridDialog.newInstance(inspeccion.darAlbumGeneral());
                    photoGridDialog.show(inspeccion.getSupportFragmentManager(), "sometag");

                }
            });
        }

        //
        lbNumContenedor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle_contents();
            }
        });

        if (inspeccion.isCargarPendiente()) actualizarInterfaz();
        txtCodContenedor = (MyEditText) getView().findViewById(R.id.txtNumContenedor);
        if (crearContenedor()) cambiarColorCampoContenedor(crearContenedor());

        txtCodContenedor.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                if (inspeccion.getInspeccion().getDatosContenedor().size() != 0 && inspeccion.getInspeccion().getTipoInspeccion().equals(InspeccionActivity.ENTRADA)) {
                    if(!InspeccionActivity.soloLectura && InspeccionActivity.tipoAccion==null) {
                        Intent intent = new Intent(getView().getContext(), FichaContenedorActivity.class);


                        intent.putExtra(FichaContenedorActivity.INSPECCION, inspeccion.getInspeccion());
                        intent.putExtra(FichaContenedorActivity.TYPE, FichaContenedorActivity.UPDATE);
                        startActivityForResult(intent, CREAR_CONTENEDOR);
                    }
                }
                return false;
            }
        });


    }

    private boolean crearContenedor() {
        return inspeccion.getInspeccion().getDatosContenedor().size() == 0 && infoInspeccion.size() > 5;
    }

    public void toggle_contents() {
        int mostar = tablaInfoContenedor.isShown() ? View.GONE : View.VISIBLE;
        int icono = tablaInfoContenedor.isShown() ? R.drawable.ic_action_collapse : R.drawable.ic_action_expand;
        lbNumContenedor.setCompoundDrawablesWithIntrinsicBounds(icono, 0, 0, 0);
        if (mostar == View.GONE) {
            UIEffects.slide_up(this.getActivity(), tablaInfoContenedor);
            tablaInfoContenedor.setVisibility(mostar);
        } else {
            tablaInfoContenedor.setVisibility(mostar);
            UIEffects.slide_down(this.getActivity(), tablaInfoContenedor);
        }

    }

    //Carga el formulario con la informacion de la inspeccion
    private void actualizarInterfaz() {
        ArrayList<CustomView> listaCampos = inspeccion.darListaCampos();
        infoInspeccion = inspeccion.getInfoInspeccion();
        HashMap<String, String> datosContenedor = inspeccion.getInspeccion().getDatosContenedor();
        //Si la fecha viene de una guia anterior y no la actual
        if (InspeccionActivity.soloLectura ||InspeccionActivity.tipoAccion!=null) {
            String[] fechaDocumento = Formularios.fechaToArray(infoInspeccion.get(getString(R.string.DFECHALOG)));
            setFechaEnMapa(fechaDocumento);
            inspeccion.getInspeccion().setFechaInspeccion(fechaDocumento);
        }


        if (listaCampos != null) {
            for (int i = 0; i < listaCampos.size(); i++) {
                CustomView cw = listaCampos.get(i);
                String texto;
                if (cw.getVieneDe().equals(getString(R.string.TBCONTENEDORES)) ) {


                    texto = datosContenedor.get(cw.getNombreCampo());


                } else {
                    texto = infoInspeccion.get(cw.getNombreCampo());
                }
                if (cw instanceof EditText)
                {
                   EditText editText= (EditText) cw;
                    if( editText.getInputType()== InputType.TYPE_CLASS_DATETIME && !TextUtils.isEmpty(texto) )
                    {
                        texto= Formularios.dateFromDbParser(texto,"/",false);
                    }

                }

                cw.setTexto(texto);

            }
        }

    }


    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if (!hasFocus) return;
        view.clearFocus();
        switch (view.getId()) {


            case R.id.txtEstado:


                CompletarCampoDialog.newInstance((ArrayList<FilaEnConsulta>) Listas.darLista(Listas.LISTA_ESTADOS), "ESTADO INICIAL", p1.this, R.id.txtEstado).show(getFragmentManager(), "estadoInicial");


                break;


            case R.id.txtEstadoFinal:


                CompletarCampoDialog.newInstance((ArrayList<FilaEnConsulta>) Listas.darLista(Listas.LISTA_ESTADOS), "ESTADO FINAL", p1.this, R.id.txtEstadoFinal).show(getFragmentManager(), "estadoFinal");


                break;


            case R.id.txtSitioOrigen:

                BusquedaDialog.newInstance((ArrayList<FilaEnConsulta>) Listas.darLista(Listas.LISTA_PUERTOS), "PUERTO ORIGEN", p1.this, R.id.txtSitioOrigen).show(getFragmentManager(), "sitioOrigen");
                break;
            case R.id.txtLineaLargo:
            case R.id.txtLineaCorto:
                BusquedaDialog.newInstance((ArrayList<FilaEnConsulta>) Listas.darLista(Listas.LISTA_LINEAS),"LINEAS",p1.this, R.id.txtLineaCorto).show(getFragmentManager(), "listas");
                break;
            case R.id.txtUsoLogico:
                CompletarCampoDialog.newInstance((ArrayList<FilaEnConsulta>) Listas.darLista(Listas.LISTA_USO_LOGICO), "CARGAMENTO", p1.this, R.id.txtUsoLogico).show(getFragmentManager(), "cargamento");
                break;
            case R.id.txtPuerto:

                BusquedaDialog.newInstance((ArrayList<FilaEnConsulta>) Listas.darLista(Listas.LISTA_PUERTOS), "PUERTO DESTINO", p1.this, R.id.txtPuerto).show(getFragmentManager(), "sitioOrigen");
                break;




            case R.id.txtNumeroBooking:
                mostrarInfoBooking();
                break;

            case R.id.txtTurno:
                if (InspeccionActivity.soloLectura) return;
                tipoBusqueda = BUSCAR_TURNOS_PENDIENTES;
                inspeccion.getSupportLoaderManager().initLoader(LOADER, null, consultasCallBacks);
                break;
            case R.id.txtTipoIngreso:

                BusquedaDialog.newInstance((ArrayList<FilaEnConsulta>) Listas.darLista(Listas.LISTA_TIPOS_INGRESO),"TIPO DE INGRESO",p1.this, R.id.txtTipoIngreso).show(getFragmentManager(), "listas");
                break;
            case R.id.txtMotonave:
                MyEditText txtLineaCorto= (MyEditText) getView().findViewById(R.id.txtLineaCorto);
                if (!txtLineaCorto.getTexto().equals("")) {
                    HashMap<String, ArrayList<FilaEnConsulta>> listas=
                            (HashMap<String, ArrayList<FilaEnConsulta>>) Listas.darLista(Listas.LISTAS_MOTONAVES_POR_LINEA);
                    ArrayList<FilaEnConsulta> listaUbicaciones = listas.get(txtLineaCorto.getTexto());
                    // si no hay lista para esa empresa puede escribir lo que sea
                    if(listaUbicaciones==null || listaUbicaciones.size()==0 )    EditTextDialog.newInstance(((MyEditText) view).getInputType(), view.getId(), ((MyEditText) view).getTexto(), this).show(getFragmentManager(), "input");
                    else BusquedaDialog.newInstance(listaUbicaciones, "MOTONAVES", this, R.id.txtMotonave).show(getFragmentManager(), "ubicaciones");
                }
                else
                {
                    Toast.makeText(MainActivity.context,"SELECCIONE UNA LINEA PRIMERO",Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.txtNumContenedor:
                MyEditText txtLineaCort= (MyEditText) getView().findViewById(R.id.txtLineaCorto);
                if (txtLineaCort.getTexto().equals(""))
                {
                    Toast.makeText(MainActivity.context,"SELECCIONE PRIMERO UNA LINEA",Toast.LENGTH_SHORT).show();
                    break;
                }

            default:

                if (view instanceof MyEditText && ((MyEditText) view).getMyInputType() == CustomView.DIALOGO_CON_DATE_PICKER) {


                    String texto = Formularios.editarFecha(((MyEditText) view).getTexto());
                    EditTextDialog.newInstance(InputType.TYPE_CLASS_NUMBER, view.getId(), texto, this).show(inspeccion.getSupportFragmentManager(), "input");


                } else if (view instanceof MyEditText) {


                    EditTextDialog.newInstance(((MyEditText) view).getInputType(), view.getId(), ((MyEditText) view).getTexto(), this).show(getFragmentManager(), "input");

                }

                break;


        }


    }

    //Le asigna a el formulario la fecha de la base de datos amenos que ya tenga una
    private void cargarFecha() {
        String fecha[];
        if (InspeccionActivity.soloLectura || InspeccionActivity.tipoAccion!=null) return;
        else {
            MyEditText txtFecha = (MyEditText) getView().findViewById(R.id.txtFecha);
            txtFecha.setText("Conectando...");
            fecha = inspeccion.getInspeccion().getFechaInspeccion();
        }
        if (fecha != null) {
            setFechaEnMapa(fecha);
            actualizarInterfaz();
        } else {

            AsyncTask<Void, Void, String[]> cargarFecha = new AsyncTask<Void, Void, String[]>() {


                @Override
                protected String[] doInBackground(Void... params) {
                    String[] fecha;
                    fecha = Consultas.darFecha();
                    return fecha;
                }

                @Override
                protected void onPostExecute(String[] fecha) {
                    if (Varios.isActityEnding(inspeccion)) return;
                    if (fecha != null) {

                        setFechaEnMapa(fecha);
                        inspeccion.getInspeccion().setFechaInspeccion(fecha);

                    } else {
                        inspeccion.getInspeccion().setFechaInspeccion(null);
                        infoInspeccion.put(getString(R.string.FECHA_MOSTRAR), "SIN_CONEXION");
                    }
                    actualizarInterfaz();
                }
            };

            cargarFecha.execute();
        }


    }

    private void setFechaEnMapa(String[] fecha) {
        infoInspeccion.put(getString(R.string.NDIA), fecha[Consultas.FECHA_DIA]);
        infoInspeccion.put(getString(R.string.NMES), fecha[Consultas.FECHA_MES]);
        infoInspeccion.put(getString(R.string.NANO), fecha[Consultas.FECHA_ANIO]);
        infoInspeccion.put(getString(R.string.FECHA_MOSTRAR), fecha[Consultas.FECHA_MOSTRAR]);

    }


    private void mostrarInfoBooking() {
        ArrayList<HashMap<String, String>> infoBooking = inspeccion.getInspeccion().getInfoBooking();
        if (infoBooking != null) {
            try {
                ConsultaCortaDialog dialog = ConsultaCortaDialog.newInstance("Datos Booking", infoBooking, R.layout.dialog_info_booking, R.layout.four_column_listview_row, booking_cols, dialog_columns);

                dialog.show(inspeccion.getFragmentManager(), "mng");

            } catch (Exception e) {
                e.printStackTrace();
            }

        }


    }

    private void mostrarTurnosPendientes(ArrayList<HashMap<String, String>> restantes) {


        if (restantes != null) {

            ConsultaCortaDialog dialog = ConsultaCortaDialog.newInstance("TURNOS", restantes, R.layout.dialog_info_turnos, R.layout.info_turnos_adapter, info_turno_cols, dialog_columns);
            dialog.setIniciadoPor(this, R.id.txtTurno);
            dialog.show(inspeccion.getFragmentManager(), "mng");
        }


    }


    private void buscarTurno(String turno) {

        if (turno.matches("[0-9]+")) {


            inspeccion.limpiarTodo();
            tipoBusqueda = BUSCAR_CABECERA;
            inspeccion.getSupportLoaderManager().initLoader(LOADER, null, consultasCallBacks);

        } else txtTurno.limpiar();

    }

    private void buscarNit(String nit)
    {
        if(TextUtils.isEmpty(nit))
        {
            Toast.makeText(MainActivity.context,"NIT VACIO",Toast.LENGTH_SHORT);
        }
        else {
            tipoBusqueda = BUSCAR_NOMBRE_CLIENTE;
            inspeccion.getSupportLoaderManager().initLoader(LOADER, null, consultasCallBacks);
        }

    }

    private void buscarTipoActividad(String usoLogico)
    {
        if(TextUtils.isEmpty(usoLogico))
        {
            Toast.makeText(MainActivity.context,"CAMPO VACIO",Toast.LENGTH_SHORT);
        }
        else {
            tipoBusqueda = BUSCAR_TIPO_ACTIVIDAD;
            inspeccion.getSupportLoaderManager().initLoader(LOADER, null, consultasCallBacks);
        }
    }



    private void cambiarColorCampoContenedor(boolean crearContenedor) {


        if (isDetached()) return;
        if (crearContenedor) {
            if (inspeccion.darTipoInspeccion().equals(InspeccionActivity.ENTRADA)) {
                txtCodContenedor.setBackgroundColor(getResources().getColor(R.color.RojoError));
                txtCodContenedor.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        preguntarSiCrearContenedor();
                    }
                });
            }

        } else {
            if (inspeccion.darTipoInspeccion().equals(InspeccionActivity.ENTRADA)) {
                txtCodContenedor.setBackgroundColor(getResources().getColor(R.color.Blanco));
                txtCodContenedor.setOnClickListener(null);
            } else if (inspeccion.darTipoInspeccion().equals(InspeccionActivity.SALIDA)) {
                txtCodContenedor.setBackgroundColor(getResources().getColor(R.color.VerdeSuave));
            }

            // redondearCamposContenedor();
        }
    }

    private void preguntarSiCambiarContenedor() {
        YesNoDialog dialog = YesNoDialog.newInstance("Modificar Contenedor", "¿Desea modificar el contenedor?", "MODIFICAR", "CANCELAR", this, DIALOG_MODIFICAR_CONTENEDOR);
        dialog.show(inspeccion.getFragmentManager(), "" + DIALOG_MODIFICAR_CONTENEDOR);
    }

    private void preguntarSiCrearContenedor() {
        YesNoDialog yesNoDialog = YesNoDialog.newInstance("No encontrado", getString(R.string.NO_ENCONTRO_CONTENEDOR), "CREAR CONTENEDOR",
                "CANCELAR", this, R.id.txtNumContenedor);
        yesNoDialog.show(inspeccion.getFragmentManager(), "");


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e("Fragment1", "OnCreate");
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CREAR_CONTENEDOR) {
            if (resultCode == Activity.RESULT_OK) {


                tipoBusqueda = BUSCAR_INFO_CONTENEDORES;
                txtCodContenedor.setTexto(FichaContenedorActivity.ultimoCntr);

                inspeccion.getSupportLoaderManager().initLoader(LOADER, null, consultasCallBacks);
            }
        }

    }

    private boolean validarFormulario(boolean comprobarCampos) {
        String mensaje=null;
        boolean camposLlenos=Formularios.comprobarCamposObligatorios(inspeccion.darListaCampos());
        if(!camposLlenos) mensaje = "Faltan campos obligatorios";

        String lineaCntr=inspeccion.getInspeccion().getDatosContenedor().get(getString(R.string.CCTELNA));
        String lineaTurno=infoInspeccion.get(getString(R.string.CCTELNA));
        if (lineaCntr != null) {
            boolean pertenece = TextUtils.equals(lineaCntr,lineaTurno);
            if(!pertenece)  mensaje = "El contenedor no peternece a la linea";
        }

        boolean estaEnpatio = inspeccion.getInspeccion().estaEnPatio();
        if (!estaEnpatio && inspeccion.darTipoInspeccion().equals(InspeccionActivity.SALIDA))
            mensaje = "El contenedor NO se encuentra en el patio";
        else if (estaEnpatio && inspeccion.darTipoInspeccion().equals(InspeccionActivity.ENTRADA))
            mensaje = "El contenedor YA se encuentra en el patio";


        if( InspeccionActivity.tipoAccion!=null ||
                TextUtils.equals(infoInspeccion.get(getString(R.string.CTIPDOC)), InspeccionActivity.ET)){
            if(mensaje!=null) Toast.makeText(inspeccion, mensaje, Toast.LENGTH_LONG).show();
            return mensaje==null;
        }

        if (inspeccion.darTipoInspeccion().equals(InspeccionActivity.SALIDA)) {


            if (! inspeccion.getInspeccion().isPerteneceEnBooking())
                mensaje = "El contenedor no pertenece al Booking";

            if (!inspeccion.getInspeccion().isHaySaldoBooking())
                mensaje = "El booking ya fue atendido totalmente";
        }


        if (mensaje!=null) {
            Toast.makeText(MainActivity.context, mensaje, Toast.LENGTH_LONG).show();
        }
        return mensaje==null;
    }

    @Override
    public Object guardar() {
        String fecha_al_cargar_turno[] = inspeccion.getInspeccion().getFechaInspeccion();
        if (fecha_al_cargar_turno == null) {

            return false;
        }

        if (!validarFormulario(true)) {
            return false;
        }


        String fechaEntera = Varios.fechaDAOtoString(fecha_al_cargar_turno);
        String horaConMinutos = Varios.fechaDAOtoHora(fecha_al_cargar_turno);
        String fechaInsercion = getResources().getString(R.string.DFECMVTO);
        infoInspeccion.put(fechaInsercion, fechaEntera);

        String fechaLog = getResources().getString(R.string.DFECHALOG);
        infoInspeccion.put(fechaLog, fechaEntera);
        String horaInsecion = getResources().getString(R.string.CHORA);
        infoInspeccion.put(horaInsecion, horaConMinutos);

        MyEditText txtSitio = (MyEditText) getView().findViewById(R.id.txtSitioOrigen);
        String sitioOrigenCopia = getResources().getString(R.string.CCODSITCNTR);
        infoInspeccion.put(sitioOrigenCopia, txtSitio.getTexto());


        String tipoDocumentoOri = getResources().getString(R.string.CTIPDOCORI);
        String numeroDocumentoOri = getResources().getString(R.string.NNUMDOCORI);


        MyEditText txtTipoDocumento = (MyEditText) getView().findViewById(R.id.txtTipoDocumento);
        MyEditText txtNumDocumento = (MyEditText) getView().findViewById(R.id.txtNumeroDocumento);
        if (inspeccion.darTipoInspeccion().equals(InspeccionActivity.ENTRADA)) {
            infoInspeccion.put(tipoDocumentoOri, txtTipoDocumento.getTexto());
            infoInspeccion.put(numeroDocumentoOri, txtNumDocumento.getTexto());
        } else {
            infoInspeccion.put(tipoDocumentoOri, inspeccion.getInspeccion().getDatosContenedor().get(tipoDocumentoOri));
            infoInspeccion.put(numeroDocumentoOri, inspeccion.getInspeccion().getDatosContenedor().get(numeroDocumentoOri));
        }
        actualizarMapa();

        return true;
    }

    @Override
    public Object actualizarMapa() {

        infoInspeccion = inspeccion.getInfoInspeccion();
        HashMap<String,String> infoCntr=inspeccion.getInspeccion().getDatosContenedor();
        String tbContenedores=getString(R.string.TBCONTENEDORES);

        if (true) {
            for (int i = 0; i < inspeccion.darListaCampos().size(); i++) {
                CustomView cw = inspeccion.darListaCampos().get(i);
                if(cw.getVieneDe().equals(tbContenedores))
                {
                    infoCntr.put(cw.getNombreCampo(), cw.getTexto());
                }
                else {
                    infoInspeccion.put(cw.getNombreCampo(), cw.getTexto());
                }
            }

        } else {
            Toast.makeText(MainActivity.context, "No ha ingresado un turno", Toast.LENGTH_SHORT).show();
        }
        return null;

    }

    @Override
    public void limpiar() {
        ArrayList<CustomView> arrayList = inspeccion.darListaCampos();
        MyEditText turno = (MyEditText) getView().findViewById(R.id.txtTurno);
        String elTurno = turno.getTexto();
        for (int i = 0; i < arrayList.size(); i++) {
            arrayList.get(i).limpiar();
        }
        cargarFecha();
        turno.setTexto(elTurno);
        actualizarMapa();

    }


    private void inicializarNoUsaTurno(boolean firstRun) throws Exception
    {
          //usoLogico

        String tipoTurno=(inspeccion.getInspeccion().getTipoInspeccion());
        EditText tx= (EditText) getView().findViewById(R.id.txtTipoTurno);
        tx.setText(tipoTurno);
        //tipoIngreso
        EditText tipoIngreso= (EditText) getView().findViewById(R.id.txtTipoIngreso);
//        tipoIngreso.setOnFocusChangeListener(this);

        EditText usoLogico= (EditText) getView().findViewById(R.id.txtUsoLogico);
        usoLogico.setText("EMPTY");
  //      usoLogico.setOnFocusChangeListener(this);

        TableRow row1= (TableRow) getView().findViewById(R.id.rowlblUsoLogico);
        row1.setVisibility(View.VISIBLE);
        TableRow row2= (TableRow) getView().findViewById(R.id.rowtxtUsoLogico);
        row2.setVisibility(View.VISIBLE);
          //listas
            EditText lineaCorto= (EditText) getView().findViewById(R.id.txtLineaCorto);
    //    lineaCorto.setOnFocusChangeListener(this);
        EditText lineaLargo= (EditText) getView().findViewById(R.id.txtLineaLargo);
      //  lineaLargo.setOnFocusChangeListener(this);

        //nit
        MyEditText nitCliente= (MyEditText) getView().findViewById(R.id.txtClienteNit);
        nitCliente.setInputType(InputType.TYPE_CLASS_NUMBER);
        //nitCliente.setOnFocusChangeListener(this);
        //sitio origen
        EditText sitioOrigen= (EditText) getView().findViewById(R.id.txtSitioOrigen);
       // sitioOrigen.setOnFocusChangeListener(this);
        //MOTOnave
        EditText motonave= (EditText) getView().findViewById(R.id.txtMotonave);
        motonave.setInputType(InputType.TYPE_CLASS_TEXT);
//        motonave.setOnFocusChangeListener(this);




        ArrayList<CustomView>  listaCampos=inspeccion.darListaCampos();
        infoInspeccion=inspeccion.getInfoInspeccion();
        infoInspeccion.put(   getResources().getString(R.string.NTURNO),""+0);

        String tbTurno=getResources().getString(R.string.TBTURNOS);
        for(int i=0;i<listaCampos.size();i++){
            CustomView cw=listaCampos.get(i);
            if(   cw.getVieneDe().equals( tbTurno) )
            if(   cw.getVieneDe().equals( tbTurno) )
            {
                if(cw instanceof EditText)
                {
                      EditText ed=(EditText)cw;
                      ed.setInputType(InputType.TYPE_CLASS_TEXT);
                    ed.setOnFocusChangeListener(this);
                }
            }

        }
        EditText ed= (EditText) getView().findViewById(R.id.txtTurno);
        ed.setEnabled(false);
        if(firstRun) {
            buscarTipoActividad("EMPTY");
        }
    }


    @Override
    public void recibirInfo(Object... object) {
        actualizarInterfaz();

    }

    @Override
    public void onDataReceive(Object _data, int iniciadoPor) {
        if (Varios.isActityEnding(getActivity())) return;
        EditText temp = (EditText) getView().findViewById(iniciadoPor);
        FilaEnConsulta data = null;
        HashMap<String, String> dataH = null;
        if (_data instanceof FilaEnConsulta) {
            data = (FilaEnConsulta) _data;
        } else if (_data instanceof HashMap) {
            dataH = (HashMap<String, String>) _data;
        }
        HashMap<String,String> mapaCampos=inspeccion.getInfoInspeccion();

        if (_data != null) {

            switch (iniciadoPor) {
                case R.id.txtEstado:
                    EditText txtAdaptable = (MyEditText) getView().findViewById(R.id.txtEstadoFinal);
                    temp.setText(data.getDato(0));
                    if (data.getDato(2).equals("1")) txtAdaptable.setText(data.getDato(0));
                    break;
                case DIALOG_CREAR_CONTENEDOR:

                    if ((boolean) _data == true) {
                        Intent intent = new Intent(getView().getContext(), FichaContenedorActivity.class);
                        intent.putExtra(FichaContenedorActivity.INSPECCION, inspeccion.getInspeccion());
                        intent.putExtra(FichaContenedorActivity.TYPE, FichaContenedorActivity.CREATE);
                        startActivityForResult(intent, CREAR_CONTENEDOR);
                    }

                    break;
                case R.id.txtNumContenedor:

                    if(!inspeccion.usaTurno)
                    {

                        tipoBusqueda = BUSCAR_INFO_CONTENEDORES;
                        temp.setText(data.getDato(0).trim().toUpperCase());
                        ultimoCodigoContenedor=data.getDato(0).trim().toUpperCase();
                        inspeccion.getSupportLoaderManager().initLoader(LOADER, null, consultasCallBacks);
                    }
                    else {
                        if (inspeccion.darTipoInspeccion().equals(InspeccionActivity.SALIDA)) {
                            tipoBusqueda = BUSCAR_INFO_CONTENEDORES;
                            temp.setText(data.getDato(0).trim().toUpperCase());
                            inspeccion.getSupportLoaderManager().initLoader(LOADER, null, consultasCallBacks);
                        } else if ((boolean) _data == true) {

                            Intent intent = new Intent(getView().getContext(), FichaContenedorActivity.class);
                            intent.putExtra(FichaContenedorActivity.INSPECCION, inspeccion.getInspeccion());
                            intent.putExtra(FichaContenedorActivity.TYPE, FichaContenedorActivity.CREATE);
                            startActivityForResult(intent, CREAR_CONTENEDOR);

                        }
                    }
                    break;
                case R.id.txtFechaArribo:
                    String texto = Formularios.validarFecha(data.getDato(0));
                    if (texto == null)
                        Toast.makeText(inspeccion, "El formato es AAAAMM", Toast.LENGTH_SHORT).show();
                    else temp.setText(texto);
                    break;
                case R.id.txtTurno:
                    String turno = dataH.get(getString(R.string.NTURNO));
                    temp.setText(turno);
                    buscarTurno(turno);
                    break;
                case R.id.txtLineaCorto:
                    EditText linea1= (EditText) getView().findViewById(R.id.txtLineaCorto);
                    EditText linea2= (EditText) getView().findViewById(R.id.txtLineaLargo);
                    linea1.setText(data.getDato(0));
                    linea2.setText(data.getDato(1));
                    break;
                case R.id.txtClienteNit:
                    String nit=data.getDato(0).toUpperCase().trim();
                    temp.setText(nit);
                    buscarNit(nit);
                    break;
                case R.id.txtUsoLogico:
                    String usoLogico=data.getDato(0).toUpperCase().trim();
                    temp.setText(usoLogico);
                    buscarTipoActividad(usoLogico);
                        break;



                default:
                    if (data != null)
                        temp.setText(data.getDato(0).toUpperCase().trim());

            }


        }
    }


    public void cargarCampos() {
        txtTurno = (MyEditText) getView().findViewById(R.id.txtTurno);
        tablaInfoContenedor = (TableLayout) getView().findViewById(R.id.pag_1_info_contenedor);
        lbNumContenedor = (TextView) getView().findViewById(R.id.lblNumContenedor);
        txtTurno = (MyEditText) getView().findViewById(R.id.txtTurno);

    }


    //Carga la lista de campos con todos los campos del formulario
    class CargarListaCampos extends AsyncTask<Void, Void, Void> {


        @Override
        protected Void doInBackground(Void... params) {
            if (getView() != null) {


                inspeccion.inicializarListaCampos();
                ArrayList<CustomView> listaCampos = inspeccion.darListaCampos();


                ViewGroup tabla = (ViewGroup) getView().findViewById(R.id.pag_1_inspeccion);
                Formularios.recorrerTableLayout(tabla, listaCampos);

                MyEditText txtCodContenedor = (MyEditText) getView().findViewById(R.id.txtNumContenedor);
                listaCampos.add(txtCodContenedor);

                 ViewGroup tabla2 = (ViewGroup) getView().findViewById(R.id.pag_1_info_contenedor);
                Formularios.recorrerTableLayout(tabla2, listaCampos);

                Formularios.asignarInputDialog(listaCampos, p1.this);




                }


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (isAdded() && inspeccion.isCargarPendiente()) {
                actualizarInterfaz();
            }
        }
    }


    public void createLoaderCallBacks() {


        System.out.println(consultasCallBacks = new LoaderManager.LoaderCallbacks<Object>() {

            @Override
            public Loader<Object> onCreateLoader(int id, Bundle args) {
                boolean busqueda = false;
                actualizarMapa();


                //no es true si alguna busqueda no necesita progresDialog
                busqueda=true;

                if (busqueda) {
                    inspeccion.mostrarProgressDialog("Buscando ", "Un momento por favor...", true);
                    InspeccionActivity.loading=true;
                }
                Bundle bundle = new Bundle();
                bundle.putSerializable(InspeccionActivity.LISTA_CAMPOS, inspeccion.darListaCampos());
                bundle.putSerializable(InspeccionActivity.MAPA_CAMPOS, infoInspeccion);
                bundle.putSerializable(InspeccionActivity.TIPO, inspeccion.darTipoInspeccion());
                return new BuscarTask(inspeccion, bundle, tipoBusqueda);

            }


            @Override
            public void onLoadFinished(Loader<Object> loader, Object resultado) {
                Inspeccion result=null;
                if(resultado instanceof Inspeccion)
                {
                    result= (Inspeccion) resultado;
                }
                else if(resultado instanceof Exception)
                {
                    inspeccion.cerrarProgressDialog();
                    Exception ex= (Exception) resultado;
                    Toast.makeText(MainActivity.context,"Error:"+ex.getMessage(),Toast.LENGTH_LONG).show();
                    inspeccion.getSupportLoaderManager().destroyLoader(LOADER);
                    return;
                }
                InspeccionActivity.loading=false;
                if (resultado == null) {
                    inspeccion.cerrarProgressDialog();
                    inspeccion.sePerdioLaConexion();
                    inspeccion.getSupportLoaderManager().destroyLoader(LOADER);
                    return;
                }
                if (tipoBusqueda.equals(BUSCAR_CABECERA)) {
                    inspeccion.cerrarProgressDialog();

                    String mensaje = validarTurno(result.getInformacion());
                    if (mensaje != null) {
                        Toast.makeText(MainActivity.context, mensaje, Toast.LENGTH_SHORT).show();
                    } else {
                        String codigoInspector = LoginActivity.darCodigoInspector().trim();
                        String nombreInspector = LoginActivity.darNombreInspector().trim();

                        infoInspeccion.put(getResources().getString(R.string.CCODINSPECTOR), codigoInspector);
                        infoInspeccion.put(getResources().getString(R.string.CNOMINSPECTOR), nombreInspector);
                        infoInspeccion.putAll(result.getInformacion());

                        HashMap<String, String> datosContenedor = result.getDatosContenedor();

                        infoInspeccion.put(getString(R.string.CTIPDOC), inspeccion.darTipoEmpresa());
                        inspeccion.getInspeccion().setDatosContenedor(datosContenedor);

                        actualizarInterfaz();
                        cargarFecha();

                        cambiarColorCampoContenedor(crearContenedor());
                        inspeccion.getInspeccion().setEstaEnPatio(result.estaEnPatio());
                        inspeccion.getInspeccion().setHaySaldoBooking(result.isHaySaldoBooking());


                        if (inspeccion.darTipoInspeccion().equals(InspeccionActivity.SALIDA)) {

                            inspeccion.getInspeccion().setInfoBooking(result.getInfoBooking());
                        }


                    }
                } else if (tipoBusqueda.equals(BUSCAR_INFO_CONTENEDORES))

                {
                    HashMap<String, String> datosCntr = result.getDatosContenedor();


                    inspeccion.getInspeccion().setDatosContenedor(datosCntr);

                    inspeccion.cerrarProgressDialog();
                    actualizarInterfaz();
                    String error = null;
                    boolean datosVacios = result.getDatosContenedor().size() == 0;
                    cambiarColorCampoContenedor(datosVacios);


                    inspeccion.getInspeccion().setPerteneceEnBooking(result.isPerteneceEnBooking());
                    inspeccion.getInspeccion().setEstaEnPatio(result.estaEnPatio());
                    validarFormulario(false);
                    //No usa turno
                    if(!inspeccion.usaTurno && inspeccion.darTipoInspeccion().equals(InspeccionActivity.ENTRADA))
                    {
                        boolean hayQueCrear=crearContenedor();
                        cambiarColorCampoContenedor(hayQueCrear);
                        inspeccion.getInspeccion().setEstaEnPatio(result.estaEnPatio());
                        inspeccion.getInspeccion().setHaySaldoBooking(result.isHaySaldoBooking());
                        if(hayQueCrear) {
                            YesNoDialog yesNoDialog = YesNoDialog.newInstance("Contendor no encontrado", "No se ha encontrado el contenedor: " + ultimoCodigoContenedor +
                                    " \n Desea crearlo?", "CREAR", "CANCELAR", p1.this, DIALOG_CREAR_CONTENEDOR);
                            yesNoDialog.show(getActivity().getFragmentManager(), "");
                        }



                    }
                    if (result.estaEnPatio() && inspeccion.darTipoInspeccion().equals(InspeccionActivity.SALIDA)) {


                        tipoBusqueda = BUSCAR_DANIOS_DE_CONTENEDOR;
                        inspeccion.getSupportLoaderManager().destroyLoader(LOADER);
                        inspeccion.getSupportLoaderManager().initLoader(LOADER_EXTRA, null, consultasCallBacks);
                    }
                    if (error != null)
                        Toast.makeText(MainActivity.context, error, Toast.LENGTH_SHORT).show();

                } else if (tipoBusqueda.equals(BUSCAR_DANIOS_DE_CONTENEDOR)) {
                    inspeccion.getInspeccion().getDaniosManager().setListaDanios(result.getDaniosManager().getListaDanios());
                    //actualiza la interfaz de la pag de danios
                    inspeccion.enviarInfo(1, null);
                    inspeccion.getSupportLoaderManager().destroyLoader(LOADER_EXTRA);
                } else if (tipoBusqueda.equals(BUSCAR_TURNOS_PENDIENTES)) {

                    final ArrayList<HashMap<String, String>> leResult = result.getTurnosPendientes();
                    inspeccion.getSupportLoaderManager().destroyLoader(LOADER);
                    inspeccion.cerrarProgressDialog();

                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                            mostrarTurnosPendientes(leResult);
                        }
                    });


                }else if(tipoBusqueda.equals(BUSCAR_NOMBRE_CLIENTE))
                {
                   inspeccion.cerrarProgressDialog();
                    EditText nombreCliente= (EditText) getView().findViewById(R.id.txtClienteNombre);
                    String nombre=(String) resultado;
                    String nit=infoInspeccion.get(getString(R.string.CNITCLIENTE));
                    if (nombre.isEmpty())
                    {
                    Toast.makeText(MainActivity.context,"No se encontro el NIT : "+nit,Toast.LENGTH_LONG).show();
                    }
                    else
                    {
                        nombreCliente.setText(nombre);
                    }


                }else if(tipoBusqueda.equals(BUSCAR_TIPO_ACTIVIDAD)) {
                    inspeccion.cerrarProgressDialog();
                    EditText tipoActividad= (EditText) getView().findViewById(R.id.txtTipoActividad);
                    tipoActividad.setText((String)resultado);

                }
                inspeccion.getSupportLoaderManager().destroyLoader(LOADER);


            }

            @Override
            public void onLoaderReset(Loader<Object> loader) {

            }
        });


    }

    private String validarTurno(HashMap<String, String> datos) {

        String mensaje = null;
        if (datos.size() == 0) {
            mensaje = "No se ha encontrado el turno";

        } else {
            String tipoTurno = datos.get(getResources().getString(R.string.CTIPOTURNO));
            String turnoAtendido = datos.get(getResources().getString(R.string.NATENDIDO));
            String tipo = inspeccion.darTipoInspeccion();
            if (turnoAtendido.equals("1")) {
                mensaje = "Este turno ya fue atendido";

            } else if (tipoTurno != null && !tipoTurno.trim().equals(tipo)) {
                mensaje = "El tipo de turno no es de :" + tipo;

            }

        }
        return mensaje;
    }

    private static class BuscarTask extends DataLoader<Object> {

        Bundle params;
        String tipoBusqueda;

        public BuscarTask(Context context, Bundle _params, String _tipoBusqueda) {
            super(context);
            params = _params;
            tipoBusqueda = _tipoBusqueda;


        }

        @Override
        public Object loadInBackground() {
            Resources res = MainActivity.context.getResources();
            InspeccionDataSource dataSource = new InspeccionDataSource();
            Object result = null;
            HashMap<String, String> mapaCampos = (HashMap<String, String>) params.get(InspeccionActivity.MAPA_CAMPOS);
            try {
                switch (tipoBusqueda)
                {
                    case BUSCAR_CABECERA:
                        ArrayList<CustomView> listaCampos = (ArrayList<CustomView>) params.get(InspeccionActivity.LISTA_CAMPOS);
                        result = dataSource.read(listaCampos, res);
                        break;
                    case BUSCAR_INFO_CONTENEDORES:
                        result = dataSource.readInfoCntr(mapaCampos, res);
                        break;
                    case BUSCAR_DANIOS_DE_CONTENEDOR:
                        result = dataSource.readDaniosCntr(mapaCampos, res);
                        break;
                    case BUSCAR_TURNOS_PENDIENTES:
                        result = dataSource.readTurnosPendientes(mapaCampos, (String) params.get(InspeccionActivity.TIPO), res);
                        break;
                    case BUSCAR_NOMBRE_CLIENTE:
                        String nit=mapaCampos.get(res.getString(R.string.CNITCLIENTE) );
                        result=dataSource.darNombreCliente(nit);
                        break;
                    case BUSCAR_TIPO_ACTIVIDAD:
                        result=dataSource.darTipoActividad(mapaCampos,res);
                        break;
                }




            } catch (Exception e) {
                return e;
            }
            return result;


        }


    }
}