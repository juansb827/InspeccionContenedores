package com.juans.inspeccion.ui.common;

import com.juans.inspeccion.CustomView.CustomView;
import com.juans.inspeccion.CustomView.GoneView;
import com.juans.inspeccion.CustomView.MyEditText;
import com.juans.inspeccion.ui.inspection.InspeccionActivity;

import android.app.Activity;
import android.view.*;
import android.widget.*;
import com.juans.inspeccion.CustomView.*;
import com.juans.inspeccion.ui.common.Formularios;
import com.juans.inspeccion.R;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import java.util.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=29, application=android.app.Application.class)
public class FormBindingTest {
    private ActivityController<Activity> controller; private Activity activity;
    @Before public void setup(){controller=Robolectric.buildActivity(Activity.class).setup();activity=controller.get();}
    @After public void cleanup(){controller.pause().stop().destroy();InspeccionActivity.soloLectura=false;InspeccionActivity.tipoAccion=null;}
    private View inflate(int layout){return LayoutInflater.from(activity).inflate(layout,null,false);}
    private ArrayList<CustomView> fields(View root){ArrayList<CustomView>f=new ArrayList<>();Formularios.recorrerTableLayout(root.findViewById(R.id.pag_1_inspeccion),f);f.add((CustomView)root.findViewById(R.id.txtNumContenedor));Formularios.recorrerTableLayout(root.findViewById(R.id.pag_1_info_contenedor),f);return f;}
    // ENTRY-001/003 and EXIT-003/004: real XML and business-key metadata, including invisible rows.
    @Test public void allFourFormsIncludeHiddenDateAndDirectionKeys(){for(int layout:new int[]{R.layout.fragment_entrada_pag1,R.layout.fragment_salida_pag1,R.layout.entrada_empresa_transporte,R.layout.salida_empresa_transporte}){View root=inflate(layout);ArrayList<CustomView>f=fields(root);HashSet<String>keys=new HashSet<>();for(CustomView cw:f)keys.add(cw.getNombreCampo());assertTrue(keys.containsAll(Arrays.asList("NANO","NMES","NDIA","CTIPOTURNO","NNUMDOC","CCODCNTR")));CustomView direction=(CustomView)root.findViewById(R.id.txtTipoTurno);assertEquals("CTIPOMOV",direction.getNombreCampoDestino());assertEquals("TBCABMOVPATIO",direction.getVaPara());assertTrue(f.contains(direction));assertEquals(View.GONE,((View)direction).getParent() instanceof View?((View)((View)direction).getParent()).getVisibility():-1);}}
    @Test public void hiddenAndVisibleValuesRoundTripThroughRealBindingHelper(){View root=inflate(R.layout.fragment_entrada_pag1);ArrayList<CustomView>f=fields(root);HashMap<String,String>map=new HashMap<>();map.put("NANO","2026");map.put("NMES","10");map.put("NDIA","13");map.put("CTIPOTURNO","ENTRADA");map.put("CCTELNA","TEST");ActivityHelper helper=new ActivityHelper(activity);helper.actualizarInterfaz(f,map);HashMap<String,String>saved=new HashMap<>();helper.actualizarMapaCampos(f,saved);assertEquals("2026",saved.get("NANO"));assertEquals("ENTRADA",saved.get("CTIPOTURNO"));assertEquals("TEST",saved.get("CCTELNA"));assertFalse(saved.containsKey("CTIPOMOV"));}
    // DRAFT-001/002: persist the actual form maps, including the entry placeholder's blank key.
    @Test public void allFourFormMapsCanBeSavedAndReopenedAsDrafts() {
        com.juans.inspeccion.data.local.Pendientes.invalidate();
        try {
            for(int layout:new int[]{R.layout.fragment_entrada_pag1,R.layout.fragment_salida_pag1,R.layout.entrada_empresa_transporte,R.layout.salida_empresa_transporte}) {
                com.juans.inspeccion.Mundo.Inspeccion inspection=new com.juans.inspeccion.Mundo.Inspeccion();
                boolean entry=layout==R.layout.fragment_entrada_pag1||layout==R.layout.entrada_empresa_transporte;
                inspection.setTipoInspeccion(entry?"ENTRADA":"SALIDA");
                inspection.setTipoDocumento(layout==R.layout.entrada_empresa_transporte||layout==R.layout.salida_empresa_transporte?"ET":"EP");
                inspection.setFechaInspeccion(Formularios.fechaToArray("2026-10-13 08:05:00"));
                for(CustomView field:fields(inflate(layout))) {
                    if("TBCONTENEDORES".equals(field.getVieneDe())) inspection.getDatosContenedor().put(field.getNombreCampo(),field.getTexto());
                    else inspection.getInformacion().put(field.getNombreCampo(),field.getTexto());
                }
                if(layout==R.layout.fragment_entrada_pag1) assertTrue(inspection.getInformacion().containsKey(""));
                com.juans.inspeccion.data.local.Pendientes.upsert(inspection,activity);
                com.juans.inspeccion.data.local.Pendientes.invalidate();
                int index=com.juans.inspeccion.data.local.Pendientes.findIndex(inspection.getPhotoId(),activity);
                com.juans.inspeccion.Mundo.Inspeccion restored=com.juans.inspeccion.data.local.Pendientes.darPendiente(index,activity);
                assertEquals(inspection.getInformacion(),restored.getInformacion());
                assertEquals(inspection.getDatosContenedor(),restored.getDatosContenedor());
                assertArrayEquals(inspection.getFechaInspeccion(),restored.getFechaInspeccion());
            }
        } finally { com.juans.inspeccion.data.local.Pendientes.invalidate(); }
    }
    @Test public void requiredFieldsBlockBlankInputWhileOptionalFieldsRemainEmpty(){ArrayList<CustomView>f=fields(inflate(R.layout.fragment_entrada_pag1));assertFalse(Formularios.comprobarCamposObligatorios(f));int required=0;for(CustomView cw:f)if(cw.esObligatorio()){cw.setTexto("TEST");required++;}assertTrue(required>0);assertTrue(Formularios.comprobarCamposObligatorios(f));}
    @Test public void dateBindingUsesBusinessDateRatherThanLocaleOrder(){MyEditText date=new MyEditText(activity){@Override public String getNombreCampo(){return "DATE";}};date.setMyInputType(CustomView.DIALOGO_CON_DATE_PICKER);ArrayList<CustomView>f=new ArrayList<>();f.add(date);HashMap<String,String>map=new HashMap<>();map.put("DATE","2026-01-03 08:05:00");new ActivityHelper(activity).actualizarInterfaz(f,map);assertEquals("2026/01/03",date.getTexto());assertEquals("20260103",Formularios.dateToDbParser(date.getTexto()));assertEquals("2026/01/03  08:05",Formularios.dateFromDbParser(map.get("DATE"),"/",true));}
    // CONTAINER-003: source-characterized date validation gaps, not approved calendar validation.
    @Test public void monthValidatorAcceptsZeroButRejectsThirteenAndWrongLength(){assertEquals("2026/00/01",Formularios.validarFecha("202600"));assertNull(Formularios.validarFecha("202613"));assertNull(Formularios.validarFecha("20261"));assertEquals("20260103",Formularios.myDateParser("3/1/2026"));}
    @Test public void sameDayComparisonIgnoresTimeAndChecksMonthYear(){String[]one=Formularios.fechaToArray("2026-10-13 08:05:00");assertTrue(Formularios.fechaEsMismoDia(one,Formularios.fechaToArray("2026-10-13 23:59:00")));assertFalse(Formularios.fechaEsMismoDia(one,Formularios.fechaToArray("2026-11-13 08:05:00")));assertFalse(Formularios.fechaEsMismoDia(one,Formularios.fechaToArray("2025-10-13 08:05:00")));}
    @Test public void nonViewHiddenFieldBindsButCurrentlyHasEmptyDestination(){GoneView hidden=new GoneView("TBTURNOS","TBCABMOVPATIO","HIDDEN","");hidden.setTexto("value");ArrayList<CustomView>f=new ArrayList<>();f.add(hidden);HashMap<String,String>map=new HashMap<>();new ActivityHelper(activity).actualizarMapaCampos(f,map);assertEquals("value",map.get("HIDDEN"));assertEquals("",hidden.getNombreCampoDestino());}
    // SEARCH-002/ENTRY-006: run the real menu handler without authenticated onCreate/loaders.
    @Test public void readOnlyAndModificationMenusRestrictSaveDraftAndPrint(){InspeccionActivity screen=Robolectric.buildActivity(InspeccionActivity.class).get();InspeccionActivity.soloLectura=true;Menu readOnly=new PopupMenu(screen,new View(screen)).getMenu();screen.onCreateOptionsMenu(readOnly);assertFalse(readOnly.findItem(R.id.action_guardar_todo).isVisible());assertFalse(readOnly.findItem(R.id.action_terminar_luego).isVisible());assertTrue(readOnly.findItem(R.id.action_imprimir).isVisible());InspeccionActivity.soloLectura=false;InspeccionActivity.tipoAccion="MODIFICAR";Menu modify=new PopupMenu(screen,new View(screen)).getMenu();screen.onCreateOptionsMenu(modify);assertTrue(modify.findItem(R.id.action_guardar_todo).isVisible());assertFalse(modify.findItem(R.id.action_terminar_luego).isVisible());assertFalse(modify.findItem(R.id.action_imprimir).isVisible());}
}
