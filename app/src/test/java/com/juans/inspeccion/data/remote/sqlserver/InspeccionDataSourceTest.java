package com.juans.inspeccion.data.remote.sqlserver;

import com.juans.inspeccion.data.remote.sqlserver.ConnectionException;
import com.juans.inspeccion.CustomView.CustomView;
import com.juans.inspeccion.CustomView.GoneView;
import com.juans.inspeccion.CustomView.MyEditText;
import com.juans.inspeccion.data.remote.sqlserver.InvalidOperationException;
import com.juans.inspeccion.data.remote.sqlserver.DAO;
import com.juans.inspeccion.Mundo.Danio;
import com.juans.inspeccion.ui.common.Formularios;
import com.juans.inspeccion.Mundo.Inspeccion;

import android.content.res.Resources;
import com.juans.inspeccion.CustomView.*;
import com.juans.inspeccion.Mundo.*;
import com.juans.inspeccion.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import java.util.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=29, application=android.app.Application.class)
public class InspeccionDataSourceTest {
    private Resources res;private RecordingSql sql;private InspeccionDataSource source;
    private String headerTable,detailTable,containerTable;
    @Before public void setup(){res=RuntimeEnvironment.getApplication().getResources();sql=new RecordingSql();headerTable=res.getString(R.string.TBCABMOVPATIO);detailTable=res.getString(R.string.TBDETMOVPATIO);containerTable=res.getString(R.string.TBCONTENEDORES);sql.schemas.put(headerTable,schema("NNUMDOC","CTIPDOC","DFECMVTO"));sql.schemas.put(detailTable,schema("NNUMDOC","NUM_ITEM"));sql.schemas.put(containerTable,schema("CCODCNTR","CCTELNA"));source=new InspeccionDataSource(){@Override protected DAO dao(){return sql;}@Override protected HashMap<String,String> columns(String table){return sql.schemas.get(table);}@Override protected String[] currentDate(){return Formularios.fechaToArray("2026-10-13 08:30:00");}};}
    private HashMap<String,String> schema(String...keys){HashMap<String,String>s=new HashMap<>();for(String key:keys)s.put(key,"C");return s;}
    private Inspeccion inspection(String direction,String type,boolean turno){Inspeccion i=new Inspeccion();i.setTipoInspeccion(direction);i.setTipoDocumento(type);i.setUsaTurno(turno);i.getInformacion().put("NNUMDOC","2261013005");i.getInformacion().put("CTIPDOC",type);i.getInformacion().put("NANO","2026");i.getInformacion().put("NMES","10");i.getInformacion().put("NDIA","13");i.getInformacion().put("NTURNO","5");i.getDatosContenedor().put("CCODCNTR","TEST0000010");Danio d=new Danio();d.getDetalles().put("NNUMDOC","2261013005");d.getDetalles().put("NUM_ITEM","1");i.getDaniosManager().getListaDanios().add(d);return i;}
    // ENTRY-004 / EXIT-004: all document-type/direction branches with the real SQL builders.
    @Test public void entrySendsHeaderAndDetailsWhileExitSendsOnlyHeaderForBothTypes() throws Exception {for(String type:new String[]{"EP","ET"})for(String direction:new String[]{"ENTRADA","SALIDA"})for(boolean turno:new boolean[]{true,false}){source.insert(new ArrayList<>(),inspection(direction,type,turno),res);ArrayList<String>b=sql.batches.get(sql.batches.size()-1);assertEquals(direction.equals("ENTRADA")?2:1,b.size());assertTrue(b.get(0).startsWith("INSERT INTO "+headerTable));assertTrue(b.get(0).contains("'"+type+"'"));if(b.size()>1)assertTrue(b.get(1).startsWith("INSERT INTO "+detailTable));}}
    private ArrayList<CustomView> numberField(String number){ArrayList<CustomView>f=new ArrayList<>();f.add(new GoneView("",headerTable,"NNUMDOC",""){@Override public String getNombreCampoDestino(){return "NNUMDOC";}});f.get(0).setTexto(number);return f;}
    @Test public void noTurnoSavePreservesAssignedNumberOverFormPlaceholder() throws Exception {Inspeccion i=inspection("ENTRADA","EP",false);source.insert(numberField("0"),i,res);assertEquals("2261013005",i.getInformacion().get("NNUMDOC"));assertTrue(sql.batches.get(0).get(0).contains("'2261013005'"));}
    @Test public void turnoSaveUsesTheFormDocumentNumber() throws Exception {Inspeccion i=inspection("ENTRADA","EP",true);source.insert(numberField("123"),i,res);assertEquals("123",i.getInformacion().get("NNUMDOC"));}
    @Test public void insertConvertsPickerDateButModificationCurrentlyDoesNot() throws Exception {MyEditText date=new MyEditText(RuntimeEnvironment.getApplication()){@Override public String getNombreCampoDestino(){return "DFECMVTO";}};date.setMyInputType(CustomView.DIALOGO_CON_DATE_PICKER);date.setTexto("2026/1/3");ArrayList<CustomView>fields=new ArrayList<>();fields.add(date);Inspeccion i=inspection("ENTRADA","EP",true);source.insert(fields,i,res);assertEquals("20260103",i.getInformacion().get("DFECMVTO"));source.re_insert(fields,i,res);assertEquals("2026/1/3",i.getInformacion().get("DFECMVTO"));}
    // ENTRY-006 / EXIT-006
    @Test public void modificationReplacesHeaderAndOnlyEntryDetails() throws Exception {for(String type:new String[]{"EP","ET"})for(String direction:new String[]{"ENTRADA","SALIDA"}){source.re_insert(new ArrayList<>(),inspection(direction,type,true),res);ArrayList<String>b=sql.batches.get(sql.batches.size()-1);assertEquals(direction.equals("ENTRADA")?4:2,b.size());assertTrue(b.get(0).startsWith("DELETE FROM "+headerTable));assertTrue(b.get(1).startsWith("INSERT INTO "+headerTable));if(b.size()>2){assertTrue(b.get(2).startsWith("DELETE FROM "+detailTable));assertTrue(b.get(3).startsWith("INSERT INTO "+detailTable));}}}
    @Test public void noTurnoNumberUsesNextCountAndExistingDateConvention() throws Exception {Inspeccion i=inspection("SALIDA","EP",false);assertEquals("2261013005",source.generarConsecutivoSinTurno(i,res));assertEquals("5",i.getInformacion().get("NTURNO"));assertEquals(1,sql.reads.size());}
    @Test public void numberingPadsSingleDigitDateAndTurno(){Inspeccion i=inspection("ENTRADA","EP",true);i.getInformacion().put("NMES","1");i.getInformacion().put("NDIA","3");assertEquals("2260103005",source.generarConsecutivo(i.getInformacion(),res));}
    @Test public void writeFailurePropagatesInsteadOfReturningNormally() throws Exception {sql.fail=true;try{source.insert(new ArrayList<>(),inspection("ENTRADA","EP",true),res);fail();}catch(ConnectionException expected){assertEquals("test timeout; commit uncertain",expected.getMessage());}assertTrue(sql.batches.isEmpty());}
    // CONTAINER-004: inspect real linked-write requests; not proof of SQL transaction atomicity.
    private HashMap<String,String> original(){HashMap<String,String>m=new HashMap<>();m.put("CCODCNTR","OLD0000010");m.put("CCTELNA","TEST");return m;}
    @Test public void createWithChangedTurnoCodeRequestsLinkedUpdateAndInsert() throws Exception {source.insertContenedor(new ArrayList<>(),inspection("ENTRADA","EP",true),original(),res);assertEquals(2,sql.batches.get(0).size());assertTrue(sql.batches.get(0).get(0).startsWith("UPDATE "));assertTrue(sql.batches.get(0).get(0).contains("TEST0000010"));assertTrue(sql.batches.get(0).get(0).contains("NTURNO='5'"));assertTrue(sql.batches.get(0).get(1).startsWith("INSERT INTO "+containerTable));}
    @Test public void createWithoutTurnoOrUnchangedCodeUsesSingleInsert() throws Exception {source.insertContenedor(new ArrayList<>(),inspection("ENTRADA","ET",false),original(),res);HashMap<String,String>same=original();same.put("CCODCNTR","TEST0000010");source.insertContenedor(new ArrayList<>(),inspection("ENTRADA","EP",true),same,res);assertEquals(2,sql.singles.size());assertTrue(sql.batches.isEmpty());}
    @Test public void characterizationUpdateLinksChangedCodeEvenWithoutTurno() throws Exception {source.updateContenedor(new ArrayList<>(),inspection("ENTRADA","ET",false),original(),res);assertEquals(2,sql.batches.get(0).size());assertTrue(sql.batches.get(0).get(1).startsWith("UPDATE "+containerTable));}
    @Test public void unchangedContainerUpdateDoesNotRequestTurnoWrite() throws Exception {HashMap<String,String>same=original();same.put("CCODCNTR","TEST0000010");source.updateContenedor(new ArrayList<>(),inspection("ENTRADA","EP",true),same,res);assertEquals(1,sql.singles.size());assertTrue(sql.batches.isEmpty());}
    private void readRows(String day){HashMap<String,String>numbers=new HashMap<>();numbers.put("NNUMDOCORI","111");numbers.put("NNUMDOCSAL","222");sql.rows.add(numbers);HashMap<String,String>header=new HashMap<>();header.put(res.getString(R.string.DFECHALOG),day+" 08:00:00");sql.rows.add(header);sql.rows.add(new HashMap<>());}
    @Test public void modificationRejectsPriorDayBeforeReadingContainerOrDetails() throws Exception {readRows("2026-10-12");try{source.read("TEST0000010","ENTRADA","MODIFICAR",res);fail();}catch(InvalidOperationException expected){assertEquals("Esta guia no es de hoy",expected.getMessage());}assertEquals(2,sql.reads.size());}
    @Test public void sameDayExitLoadsExitHeaderWithEntryDamageDocument() throws Exception {readRows("2026-10-13");assertNotNull(source.read("TEST0000010","SALIDA","MODIFICAR",res));assertTrue(sql.reads.get(1).contains("222"));assertTrue(sql.reads.get(3).contains("111"));}
    @Test public void readOnlyDocumentCanBeFromPriorDay() throws Exception {readRows("2026-10-12");assertNotNull(source.read("TEST0000010","ENTRADA","VISUALIZAR",res));assertEquals(4,sql.reads.size());}
}
