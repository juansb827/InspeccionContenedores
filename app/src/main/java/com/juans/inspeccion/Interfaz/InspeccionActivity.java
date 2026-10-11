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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.tabs.TabLayout;
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



import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class InspeccionActivity extends AppCompatActivity implements Formularios.DataPass, LoaderManager.LoaderCallbacks<Object>, com.juans.inspeccion.Interfaz.Gallery.GalleryHost {


    private com.juans.inspeccion.Mundo.Storage.CaptureCoordinator captureCoordinator;
    private com.juans.inspeccion.Mundo.Storage.PhotoStore photoStore;
    private Album editingAlbum;
    private final PrinterPermissionGate printerGate = new PrinterPermissionGate(this);
    private boolean printing;
    private boolean processingPhoto;
    private boolean photoRecoveryShown;
    private String photoStorageError;
    private static final java.util.concurrent.ExecutorService PHOTO_WORKER = java.util.concurrent.Executors.newSingleThreadExecutor();
    private final androidx.activity.result.ActivityResultLauncher<android.net.Uri> cameraLauncher = registerForActivityResult(
            new com.juans.inspeccion.Mundo.Storage.PrivateTakePicture(), this::cameraReturned);
    private final androidx.activity.result.ActivityResultLauncher<String[]> photoPermissionLauncher = registerForActivityResult(
            new androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions(), grants -> {
                boolean granted = !com.juans.inspeccion.Mundo.Storage.MediaPhotoStore.needsStoragePermission(this);
                if (granted && captureCoordinator != null && captureCoordinator.isBusy()) {
                    if (captureCoordinator.session().cameraReturned) processPhoto(); else launchPendingCamera();
                } else if (captureCoordinator != null && captureCoordinator.isBusy()) {
                    if (captureCoordinator.session().cameraReturned) showPhotoRecovery("Permiso de almacenamiento denegado; la foto se conserva");
                    else cancelCapture();
                }
            });

    public Album getEditingAlbum() { return editingAlbum; }
    public void setEditingAlbum(Album album) { editingAlbum = album; }
    public Album findPhotoAlbum(String id) {
        Album album = inspeccionObject.findAlbum(id);
        return album != null ? album : editingAlbum != null && editingAlbum.getId().equals(id) ? editingAlbum : null;
    }
    public void showPhotoAlbum(Album album) {
        if (album == null) return;
        if (inspeccionObject.findAlbum(album.getId()) == null) setEditingAlbum(album);
        if (getSupportFragmentManager().isStateSaved() || getSupportFragmentManager().findFragmentByTag("photos") != null) return;
        com.juans.inspeccion.Interfaz.Gallery.PhotoGalleryFragment.newInstance(inspeccionObject.getPhotoId(), album.getId())
                .show(getSupportFragmentManager(), "photos");
    }
    @Override public Album resolveGalleryAlbum(String inspectionId, String albumId) {
        return com.juans.inspeccion.Interfaz.Gallery.GalleryAlbums.resolve(inspeccionObject, editingAlbum, inspectionId, albumId);
    }
    @Override public String galleryAlbumLabel(String albumId) {
        return com.juans.inspeccion.Interfaz.Gallery.GalleryAlbums.label(inspeccionObject, albumId);
    }
    @Override public boolean canDeleteGalleryPhoto(String inspectionId, String albumId) {
        return com.juans.inspeccion.Interfaz.Gallery.GalleryAlbums.canDelete(inspeccionObject, editingAlbum,
                inspectionId, albumId, soloLectura, tipoAccion, photoBusy() || printing);
    }
    @Override public void galleryPhotoDeleted(String inspectionId, String albumId, String reference) {
        Album current = resolveGalleryAlbum(inspectionId, albumId);
        if (current != null && current.removePhotoReference(reference)) persistPhotoChanges();
    }
    public void persistPhotoChanges() {
        try {
            if (isCargarPendiente) Pendientes.upsert(inspeccionObject, this);
            if (captureCoordinator != null) captureCoordinator.updateCompletedSnapshot(inspeccionObject, editingAlbum);
        } catch (Exception e) { Toast.makeText(this, "Los cambios requieren recuperación: " + e.getMessage(), Toast.LENGTH_LONG).show(); }
    }
    public boolean photoBusy() { return processingPhoto || (captureCoordinator != null && captureCoordinator.isBusy()); }

    public void takePhoto(Album album, int damageIndex) {
        if (photoStorageError != null) { Toast.makeText(this, photoStorageError, Toast.LENGTH_LONG).show(); return; }
        if (captureCoordinator == null || photoBusy() || soloLectura || tipoAccion != null) return;
        try {
            // Persist the form values before leaving this activity for the camera.
            Fragment first = (Fragment) viewPager.getAdapter().instantiateItem(viewPager, 0);
            if (first instanceof p1) ((p1) first).actualizarMapa();
            String remoteName = com.juans.inspeccion.Mundo.Storage.PhotoNames.capture(this, inspeccionObject, album, damageIndex);
            captureCoordinator.begin(inspeccionObject, album, remoteName, remoteName);
            lockPhotoUi(true);
            if (com.juans.inspeccion.Mundo.Storage.MediaPhotoStore.needsStoragePermission(this))
                photoPermissionLauncher.launch(com.juans.inspeccion.Mundo.Storage.MediaPhotoStore.storagePermissions());
            else launchPendingCamera();
        } catch (Exception e) { showPhotoRecovery(e.getMessage()); }
    }
    private void launchPendingCamera() {
        if (captureCoordinator == null || !captureCoordinator.isBusy()) return;
        try {
            captureCoordinator.launch(cameraLauncher::launch);
        } catch (android.content.ActivityNotFoundException e) {
            cancelCapture();
            Toast.makeText(this, "No hay una aplicación de cámara disponible", Toast.LENGTH_LONG).show();
        } catch (RuntimeException | IOException e) { showPhotoRecovery("No se pudo abrir la cámara: " + e.getMessage()); }
    }
    private void cameraReturned(boolean success) {
        if (captureCoordinator == null || !captureCoordinator.isBusy()) return;
        try {
            captureCoordinator.cameraResult(success);
            if (success) processPhoto(); else lockPhotoUi(false);
        } catch (java.io.IOException e) { showPhotoRecovery(e.getMessage()); }
    }
    private void processPhoto() {
        if (processingPhoto || captureCoordinator == null || !captureCoordinator.isBusy()) return;
        com.juans.inspeccion.Mundo.Storage.CaptureCoordinator.Session session = captureCoordinator.session();
        if (!session.cameraReturned) return;
        Album target = findPhotoAlbum(session.albumId);
        if (target == null) { showPhotoRecovery("No se encontró el álbum de la captura; la foto se conserva"); return; }
        processingPhoto = true;
        lockPhotoUi(true);
        final Inspeccion inspection = inspeccionObject;
        final com.juans.inspeccion.Mundo.Storage.CaptureCoordinator coordinator = captureCoordinator;
        PHOTO_WORKER.execute(() -> {
            String error = null;
            try { coordinator.complete(inspection, target); }
            catch (Exception e) { error = e.getMessage(); }
            final String result = error;
            runOnUiThread(() -> {
                processingPhoto = false;
                if (isDestroyed() || isFinishing()) return;
                if (result == null) {
                    lockPhotoUi(false);
                    Toast.makeText(this, "Foto guardada en la galería", Toast.LENGTH_SHORT).show();
                } else showPhotoRecovery(result);
            });
        });
    }
    private void lockPhotoUi(boolean lock) {
        if (lock) getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        else getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }
    private void cancelCapture() {
        try { if (captureCoordinator != null) captureCoordinator.discard(); }
        catch (java.io.IOException e) { Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show(); }
        lockPhotoUi(false);
    }
    private void showPhotoRecovery(String message) {
        lockPhotoUi(false);
        if (isDestroyed() || isFinishing() || photoRecoveryShown) return;
        if (captureCoordinator == null || !captureCoordinator.isBusy()) {
            Toast.makeText(this, message == null ? "No se pudo preparar la foto" : message, Toast.LENGTH_LONG).show(); return;
        }
        photoRecoveryShown = true;
        new android.app.AlertDialog.Builder(this).setTitle("Recuperar foto")
                .setMessage((message == null ? "Hay una captura interrumpida" : message) + ". Puede reintentar el guardado o descartar la captura privada.")
                .setCancelable(false)
                .setPositiveButton("Reintentar guardado", (dialog, which) -> {
                    photoRecoveryShown = false;
                    try {
                        // Recovery processes existing bytes; it never automatically reopens the camera.
                        captureCoordinator.cameraResult(true);
                        if (com.juans.inspeccion.Mundo.Storage.MediaPhotoStore.needsStoragePermission(this))
                            photoPermissionLauncher.launch(com.juans.inspeccion.Mundo.Storage.MediaPhotoStore.storagePermissions());
                        else processPhoto();
                    } catch (java.io.IOException e) { showPhotoRecovery(e.getMessage()); }
                })
                .setNegativeButton("Descartar captura", (dialog, which) -> { photoRecoveryShown = false; cancelCapture(); })
                .show();
    }

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
        outState.putSerializable("editingPhotoAlbum", editingAlbum);
        try { if (captureCoordinator != null) captureCoordinator.acknowledgeSavedState(); }
        catch (IOException e) { android.util.Log.e("Photos", "El registro de recuperación se conserva", e); }

    }

    @Override
    protected void onPause() {
        super.onPause();
        cerrarProgressDialog();
    }

    private void requestLeave() {
        if (printing) { Toast.makeText(this, "Espere a que termine la impresión", Toast.LENGTH_SHORT).show(); return; }
        if (photoBusy()) { showPhotoRecovery("Termine la captura antes de salir"); return; }
        preguntarSiCerrarInspeccion();
    }
    MyPagerAdapter myAdapter;
    protected void onCreate(Bundle savedInstanceState) {
        Log.e("Activty","OnCreate");
        new ActivityHelper(this);
        Intent intent = getIntent();
        boolean recoveredPhotoCapture = false;

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
        photoStore = new com.juans.inspeccion.Mundo.Storage.MediaPhotoStore(this);
        if (savedInstanceState != null) editingAlbum = (Album) savedInstanceState.getSerializable("editingPhotoAlbum");
        try {
            captureCoordinator = new com.juans.inspeccion.Mundo.Storage.CaptureCoordinator(photoStore, getFileStreamPath("pending-photo-capture"));
            com.juans.inspeccion.Mundo.Storage.CaptureCoordinator.Session session = captureCoordinator.session();
            if (session != null && (captureCoordinator.isBusy() || savedInstanceState == null)) {
                recoveredPhotoCapture = !session.inspectionId.equals(inspeccionObject.getPhotoId());
                inspeccionObject = session.inspection;
                indiceTurno = Pendientes.findIndex(session.inspectionId, this);
                isCargarPendiente = indiceTurno >= 0;
                if (recoveredPhotoCapture) { soloLectura = false; tipoAccion = null; }

                if (inspeccionObject.findAlbum(session.albumId) == null) editingAlbum = session.album;
                usaTurno = inspeccionObject.isUsaTurno();
            } else if (session != null && session.inspectionId.equals(inspeccionObject.getPhotoId())) {
                Album target = findPhotoAlbum(session.albumId);
                if (target == null) editingAlbum = session.album;
                else if (session.publishedUri != null) {
                    target.agregarFoto(session.publishedUri);
                    target.setRemoteName(session.publishedUri, session.remoteName);
                }
            }
        } catch (IOException | com.juans.inspeccion.Mundo.Storage.StoredInspections.StorageException e) { photoStorageError = e.getMessage(); }
        inspeccionObject.initializePhotoIds();
        //This super.Oncreate  restores the fragments so we gotta load things from saved instance to have inspeccionObject!=null
        super.onCreate(savedInstanceState);
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() { requestLeave(); }
        });

    setContentView(R.layout.activity_inspeccion);

    viewPager = (ViewPager) findViewById(R.id.pager);
    TabLayout tabLayout = findViewById(R.id.tab_layout);

    cambiarTitulo();
    inspeccionObject.setUsaTurno(usaTurno);
    myAdapter=new MyPagerAdapter(getSupportFragmentManager(),viewPager);
    addTabs(null);
    viewPager.setAdapter(myAdapter);
    tabLayout.setupWithViewPager(viewPager);
    viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                // actionBar.setSelectedNavigationItem(position); // Removed: not needed with ViewPager only

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        if (recoveredPhotoCapture) Toast.makeText(this, "Se recuperó la inspección de la captura interrumpida", Toast.LENGTH_LONG).show();
        boolean visualizar = !recoveredPhotoCapture && intent.getBooleanExtra(VISUALIZAR, false);

        if (!recoveredPhotoCapture && (visualizar || tipoAccion!=null)) {

            getSupportLoaderManager().initLoader(LOADER_BUSCAR_DOCUMENTO, intent.getExtras(), this);
            if (visualizar) { soloLectura = true; }
        }
        if (getSupportLoaderManager().getLoader(LOADER_BUSCAR_DOCUMENTO) != null)

            getSupportLoaderManager().initLoader(LOADER_BUSCAR_DOCUMENTO, null, this);



    }

    private void inicializar() {


        isCargarPendiente = false;
        indiceTurno = -1;
        inspeccionObject.limpiar();


    }

    private void addTabs(Object unused) {
        myAdapter.addTab(new p1(),"INFORMACION");

                if (darTipoEmpresa().equals(EP)) {
            myAdapter.addTab(new p2(),"DAÑOS");

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
        if (tipoTurno.equals(InspeccionActivity.ENTRADA)) {
            fl.addView(View.inflate(this, R.layout.entrada_empresa_transporte, null));
        } else {
            fl.addView(View.inflate(this, R.layout.salida_empresa_transporte, null));
        }
        p1 fragment = (p1) viewPager.getAdapter().instantiateItem(viewPager, 0);
        fragment.inicializarFormulario();



    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (photoBusy()) { showPhotoRecovery("Hay una foto pendiente"); return true; }



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

                if (frag2 == null) guardar(frag1,null);
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

                try {
                    Pendientes.upsert(inspeccionObject, this);
                    if (captureCoordinator != null) captureCoordinator.acknowledgeSavedState();
                } catch (Exception e) {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                    return true;
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
        if (photoBusy()) return;
        try { if (captureCoordinator != null) captureCoordinator.acknowledgeSavedState(); }
        catch (IOException e) { Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show(); return; }
        editingAlbum = null;
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


    public void generarRecibo() { imprimir(); }

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
                if (hacer) {
                    try { if (captureCoordinator != null) captureCoordinator.acknowledgeSavedState(); }
                    catch (IOException e) { Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show(); return; }
                    finish();
                }
                break;

        }


    }

    private void imprimir() {
        if (printing || photoBusy()) return;
        printerGate.request(() -> {
            if (!Impresora.getInstance().cargarDatosImpresora(this)) {
                Toast.makeText(this, "No ha configurado ninguna impresora", Toast.LENGTH_LONG).show(); return;
            }
            printing = true;
            lockPhotoUi(true);
            Impresora.IO.execute(() -> {
                String error = null;
                try {
                    if (Impresora.getInstance().conectar(getApplicationContext()) != Impresora.OK)
                        throw new IOException("No se pudo conectar a la impresora; no se imprimió");
                    CreadorRecibos.generarFactura(inspeccionObject, getApplicationContext());
                } catch (Exception e) { error = e.getMessage(); }
                finally { Impresora.getInstance().desconectar(); }
                final String result = error;
                runOnUiThread(() -> {
                    printing = false;
                    lockPhotoUi(false);
                    if (isFinishing() || isDestroyed()) return;
                    if (result != null) Toast.makeText(this, result, Toast.LENGTH_LONG).show();
                    else new YesNoDialog().show(getFragmentManager(), "yesno");
                });
            });
        });
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
            String photoRenameError;
            @Override
            protected void onPreExecute() {

                Toast.makeText(MainActivity.context, "Guardando", Toast.LENGTH_SHORT).show();
            }

            @Override
            protected String doInBackground(Void... params) {
                InspeccionDataSource dataSource = new InspeccionDataSource();
                try {
                    if (InspeccionActivity.tipoAccion == null || !InspeccionActivity.tipoAccion.equals(InspeccionActivity.MODIFICAR_DOCUMENTO)) {
                        if (!InspeccionActivity.usaTurno) {
                            InspeccionActivity.this.cambiarNumeroDocumento(dataSource.generarConsecutivoSinTurno(InspeccionActivity.this.inspeccionObject, InspeccionActivity.this.getResources()));
                            InspeccionActivity.this.inspeccionObject.getInformacion().put(InspeccionActivity.this.getString(R.string.TER_RAZONS), dataSource.darAgenteLinea(InspeccionActivity.this.inspeccionObject.getInformacion(), InspeccionActivity.this.getResources()));
                        }
                        dataSource.insert(InspeccionActivity.this.listaCampos, InspeccionActivity.this.inspeccionObject, InspeccionActivity.this.getResources());
                    } else {
                        dataSource.re_insert(InspeccionActivity.this.listaCampos, InspeccionActivity.this.inspeccionObject, InspeccionActivity.this.getResources());
                    }
                    try { renombrarFotos(getInfoInspeccion().get(getString(R.string.NNUMDOC)),
                            inspeccionObject.getDatosContenedor().get(getString(R.string.CCODCNTR))); }
                    catch (IOException e) { photoRenameError = e.getMessage(); }
                    return null;
                } catch (Exception e) {
                    e.printStackTrace();
                    return e.getMessage();
                }
            }

            @Override
            protected void onPostExecute(String error) {
                if(Varios.isActityEnding(InspeccionActivity.this)) return;
                String mensaje = "GUARDADO CORRECTO\"";
                if (error != null) mensaje = error;
                Toast.makeText(getBaseContext(), mensaje, Toast.LENGTH_LONG).show();
                if (error == null) {
                    try {
                        if (photoRenameError != null) throw new IOException(photoRenameError);
                        if (isCargarPendiente) Pendientes.remove(inspeccionObject.getPhotoId(), getBaseContext());
                        if (captureCoordinator != null) captureCoordinator.acknowledgeSavedState();
                    } catch (Exception e) {
                        Toast.makeText(InspeccionActivity.this, "El documento se guardó; las fotos/borrador requieren recuperación: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        return;
                    }
                    imprimirDespuesDeGuardar();
                    MenuItem menuItem=menu.findItem(R.id.action_imprimir_y_borrar);
                    menuItem.setVisible(true);

                }
            }
        };
        guardarTodo.execute();

    }
    /**
     * Usado cuando no hay turno
     * El nombre de cada foto es de acuerdo al NUMDOC
       En guias sin turno ell NUMDOC se sabe hasta despues de grabar en la base de datos
       entonces es necesario renombrar las fotos
     **/
    public void renombrarFotos(String numDoc, String container) throws IOException {
        String prefix = darSubCarpetaFotos(numDoc, container);
        Album general = inspeccionObject.getFotosGenerales();
        for (int i = 0; i < general.getFotos().size(); i++) updatePhotoName(general, i, prefix + "_" + i + ".jpg");
        ArrayList<Danio> damages = inspeccionObject.getDaniosManager().getListaDanios();
        for (int i = 0; i < damages.size(); i++) {
            Album album = damages.get(i).getAlbum();
            for (int j = 0; j < album.getFotos().size(); j++) updatePhotoName(album, j, prefix + "_DANIO#" + i + "_" + j + ".jpg");
        }
    }
    private void updatePhotoName(Album album, int index, String remoteName) throws IOException {
        String reference = album.getFotos().get(index);
        if (!reference.startsWith("content://")) { album.setRemoteName(reference, remoteName); return; }
        // Keep the URI stable and the local filename unique; remote naming remains the released convention.
        String localName = remoteName.replaceFirst("(?i)\\.jpg$", "") + "-" + album.getId() + "-" + index + ".jpg";
        photoStore.rename(reference, localName);
        album.setRemoteName(reference, remoteName);
    }
    public void enviarFotos() {
        String carpetaDeLaInspeccion = "";
        String numdoc = getInfoInspeccion().get(getResources().getString(R.string.NNUMDOC));
        String ccodcntr = inspeccionObject.getDatosContenedor().get(getResources().getString(R.string.CCODCNTR));
        String subFolder = darSubCarpetaFotos(numdoc, ccodcntr);
        carpetaDeLaInspeccion = subFolder;
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
        if (photoBusy() && !processingPhoto) {
            if (captureCoordinator.session().cameraReturned) processPhoto();
            else showPhotoRecovery("Hay una captura interrumpida");
        }
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
