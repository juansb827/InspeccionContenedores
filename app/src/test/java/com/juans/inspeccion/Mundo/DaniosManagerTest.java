package com.juans.inspeccion.Mundo;

import android.content.res.Resources;
import com.juans.inspeccion.Interfaz.MainActivity;
import com.juans.inspeccion.R;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import java.util.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 29, application = android.app.Application.class)
public class DaniosManagerTest {
    private Resources res;
    @Before public void setup() { MainActivity.context=RuntimeEnvironment.getApplication(); res=MainActivity.context.getResources(); }
    @After public void cleanup() { MainActivity.context=null; }
    private HashMap<String,String> valid() {
        HashMap<String,String> d=new HashMap<>();
        int[] keys={R.string.CCODUBI,R.string.CCODELE,R.string.CCODDAN,R.string.CCODMET,R.string.NUNIDADES,R.string.NLARGO,R.string.NANCHO};
        for(int key:keys) d.put(res.getString(key),"1");
        d.put(res.getString(R.string.CTIPCALCULO),"U"); return d;
    }
    // DAMAGE-002: literal messages also protect validation precedence.
    @Test public void requiredFieldsAreRejectedInBusinessOrder() {
        int[] keys={R.string.CCODUBI,R.string.CCODELE,R.string.CCODDAN,R.string.CCODMET,R.string.NUNIDADES};
        String[] messages={"Ingrese la ubicacion ","Ingrese el componente dañado ","Ingrese el tipo de daño ","Ingrese el metodo de reparacion","Ingrese la cantidad"};
        HashMap<String,String> d=valid(); for(int key:keys)d.put(res.getString(key)," ");
        for(int i=0;i<keys.length;i++){ assertEquals(messages[i],DaniosManager.validarDanio(res,d,"0","20")); d.put(res.getString(keys[i]),"1"); }
        assertNull(DaniosManager.validarDanio(res,d,"0","20"));
    }
    @Test public void dimensionalMethodsRequireBothDimensions() {
        HashMap<String,String> d=valid();d.put(res.getString(R.string.CTIPCALCULO),"D");
        for(int key:new int[]{R.string.NLARGO,R.string.NANCHO}){d.put(res.getString(key),"");assertEquals("Ingrese las dimensiones de daño",DaniosManager.validarDanio(res,d,"0","20"));d.put(res.getString(key),"1");}
        assertNull(DaniosManager.validarDanio(res,d,"0","20"));
    }
    @Test public void sizeBasedMethodsRequireAndCopyContainerSize() {
        HashMap<String,String> d=valid();assertEquals("No se encontro el tamaño del contendor",DaniosManager.validarDanio(res,d,"1"," "));
        assertNull(DaniosManager.validarDanio(res,d,"1","40"));assertEquals("40",d.get(res.getString(R.string.CMATCNTR)));
    }
    @Test public void characterizationDimensionalBranchSkipsAlternativeSizeRequirement() {
        HashMap<String,String> d=valid();d.put(res.getString(R.string.CTIPCALCULO),"D");
        assertNull(DaniosManager.validarDanio(res,d,"1",""));assertFalse(d.containsKey(res.getString(R.string.CMATCNTR)));
    }
    // DAMAGE-004: independently worked total: (20 material + 10 labor) with 10% IVA = 33.
    @Test public void valueAndLaborAreTaxedTogether() {
        HashMap<String,String> d=valid();d.put(res.getString(R.string.NVALOR),"20");d.put(res.getString(R.string.NCOSHORHOM),"10");d.put(res.getString(R.string.NTARIVA),"10");
        assertEquals(33f,DaniosManager.calcularValorDanio(d,res),0.0001f);
    }
    @Test public void blankAndAbsentCostComponentsAreZero() {
        HashMap<String,String>d=new HashMap<>();d.put(res.getString(R.string.NVALOR),"");assertEquals(0f,DaniosManager.calcularValorDanio(d,res),0f);
        d.put(res.getString(R.string.NVALOR),"12.5");assertEquals(12.5f,DaniosManager.calcularValorDanio(d,res),0f);
    }
    private Danio damage(float total) { Danio d=new Danio();d.getDetalles().put(DaniosManager.COSTO_TOTAL_DANIO,Float.toString(total));return d; }
    // DAMAGE-005 / MEDIA-008: removal unlinks the album without resetting capture numbering.
    @Test public void addRemoveRenumbersAndSubtractsStoredCost() throws Exception {
        DaniosManager m=new DaniosManager();Danio first=damage(33),second=damage(7);first.getAlbum().agregarFoto("content://fixture/1");
        m.agregarDanio(first);m.agregarDanio(second);assertEquals(40f,m.darValorTotal(),0f);
        m.removerDanio(0,res);assertEquals(7f,m.darValorTotal(),0f);assertEquals("1",second.getDetalles().get(res.getString(R.string.NUM_ITEM)));
        assertTrue(first.getFotos().isEmpty());assertEquals(1,first.getAlbum().getFotos_tomadas());
    }
    @Test public void characterizationRepeatedRecalculationAccumulatesInsteadOfResetting() {
        DaniosManager m=new DaniosManager();m.agregarDanio(damage(10));m.recalcularValorTotal();assertEquals(20f,m.darValorTotal(),0f);
        m.recalcularValorTotal();assertEquals(30f,m.darValorTotal(),0f); // Suspected defect; not an approved sum contract.
    }
    @Test public void characterizationRecomputingLoadedDamageValuesAlsoAccumulatesTheTotal() {
        Danio d=damage(0);d.getDetalles().put(res.getString(R.string.NVALOR),"20");
        d.getDetalles().put(res.getString(R.string.NCOSHORHOM),"10");d.getDetalles().put(res.getString(R.string.NTARIVA),"10");
        DaniosManager m=new DaniosManager();m.getListaDanios().add(d);
        m.recalcularValorDanios(res);assertEquals(33f,m.darValorTotal(),0.0001f);
        m.recalcularValorDanios(res);assertEquals(66f,m.darValorTotal(),0.0001f);
        assertEquals("33.0",d.getDetalles().get(DaniosManager.COSTO_TOTAL_DANIO));
    }
    // DAMAGE-006
    @Test public void copiedDamageHasIndependentDetailsAndNoCopiedPhotos() throws Exception {
        Danio d=damage(10);d.getAlbum().agregarFoto("content://fixture/1");Danio copy=new DaniosManager().copiarDanio(d);
        copy.getDetalles().put(DaniosManager.COSTO_TOTAL_DANIO,"20");assertEquals("10.0",d.getDetalles().get(DaniosManager.COSTO_TOTAL_DANIO));assertTrue(copy.getFotos().isEmpty());assertNotEquals(d.getAlbum().getId(),copy.getAlbum().getId());
    }
}
