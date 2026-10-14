package com.juans.inspeccion.ui.inspection;

import com.juans.inspeccion.data.remote.sqlserver.InspeccionDataSource;

import com.juans.inspeccion.Mundo.Inspeccion;
import com.juans.inspeccion.data.local.Pendientes;

import android.content.Context;
import android.content.res.Resources;
import com.juans.inspeccion.CustomView.CustomView;
import com.juans.inspeccion.Mundo.*;
import com.juans.inspeccion.data.local.StoredInspections;
import com.juans.inspeccion.data.remote.sqlserver.ConnectionException;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=29, application=android.app.Application.class)
public class InspectionSubmissionTest {
    private Context app; private Resources res; private Inspeccion inspection; private Source source;
    private final ArrayList<String> events=new ArrayList<>();
    @Before public void setup(){app=RuntimeEnvironment.getApplication();res=app.getResources();inspection=new Inspeccion();inspection.setTipoInspeccion("ENTRADA");inspection.getInformacion().put("NNUMDOC","original");inspection.getDatosContenedor().put("CCODCNTR","TEST0000010");Pendientes.invalidate();Pendientes.upsert(inspection,app);source=new Source();}
    @After public void cleanup(){Pendientes.invalidate();}
    private class Source extends InspeccionDataSource {
        String failure; boolean nullMessage;
        @Override public String generarConsecutivoSinTurno(Inspeccion i,Resources r){events.add("number");return "assigned";}
        @Override public String darAgenteLinea(HashMap<String,String> info,Resources r){events.add("agent");return "TEST AGENT";}
        private void write(String event) throws ConnectionException {events.add(event);if(failure!=null)throw new ConnectionException(failure);if(nullMessage)throw new ConnectionException();}
        @Override public void insert(ArrayList<CustomView> fields,Inspeccion i,Resources r) throws ConnectionException {write("insert");}
        @Override public void re_insert(ArrayList<CustomView> fields,Inspeccion i,Resources r) throws ConnectionException {write("modify");}
    }
    private InspectionSubmission.Result save(String action,boolean turno,boolean failNaming){return InspectionSubmission.save(source,new ArrayList<>(),inspection,res,action,turno,number->{events.add("assign");inspection.getInformacion().put("NNUMDOC",number);},(number,container)->{events.add("names");assertEquals(inspection.getInformacion().get("NNUMDOC"),number);assertEquals("TEST0000010",container);if(failNaming)throw new IOException("test photo rename failure");});}
    // ENTRY-004/005/006, EXIT-004/005/006 / DRAFT-005. Real orchestration; remote operations are fakes.
    @Test public void noTurnoAssignsNumberAndAgentBeforeInsertThenNaming(){InspectionSubmission.Result result=save(null,false,false);assertNull(result.remoteError);assertEquals(Arrays.asList("number","assign","agent","insert","names"),events);assertEquals("assigned",inspection.getInformacion().get("NNUMDOC"));assertEquals("TEST AGENT",inspection.getInformacion().get("TER_RAZONS"));}
    @Test public void turnoAndModificationDoNotAllocateNewNumbers(){save(null,true,false);assertEquals(Arrays.asList("insert","names"),events);events.clear();save("MODIFICAR",false,false);assertEquals(Arrays.asList("modify","names"),events);assertEquals("original",inspection.getInformacion().get("NNUMDOC"));}
    @Test public void successfulCompletionRemovesOnlyTheResumedDraft() throws Exception {Inspeccion other=new Inspeccion();Pendientes.upsert(other,app);InspectionSubmission.Result result=save(null,true,false);assertTrue(InspectionSubmission.completeLocal(result,app,inspection,true,null,null));Pendientes.invalidate();assertEquals(1,Pendientes.darListaTurnos(app).size());assertEquals(other.getPhotoId(),Pendientes.darListaTurnos(app).get(0).getPhotoId());}
    @Test public void unresumedSaveDoesNotRemoveAnExistingDraft() throws Exception {InspectionSubmission.Result result=save(null,true,false);assertTrue(InspectionSubmission.completeLocal(result,app,inspection,false,null,null));assertEquals(1,StoredInspections.read(app,StoredInspections.DRAFTS).size());}
    @Test public void remoteTimeoutDoesNotNameMediaOrRemoveDraft() throws Exception {source.failure="test timeout; commit uncertain";InspectionSubmission.Result result=save(null,true,false);assertEquals(source.failure,result.remoteError);assertEquals(Collections.singletonList("insert"),events);assertFalse(InspectionSubmission.completeLocal(result,app,inspection,true,null,null));assertEquals(1,StoredInspections.read(app,StoredInspections.DRAFTS).size());}
    @Test public void duplicateDocumentErrorRetainsDraftAndIsNotSuccess() throws Exception {source.failure="ERROR:EL TURNO YA SE HABIA GRABADO";InspectionSubmission.Result result=save(null,true,false);assertEquals(source.failure,result.remoteError);assertFalse(InspectionSubmission.completeLocal(result,app,inspection,true,null,null));assertEquals(1,StoredInspections.read(app,StoredInspections.DRAFTS).size());}
    @Test public void remoteSuccessWithNamingFailureRetainsDraftForLocalRecovery() throws Exception {InspectionSubmission.Result result=save(null,true,true);assertNull(result.remoteError);assertEquals("test photo rename failure",result.photoNamingError);try{InspectionSubmission.completeLocal(result,app,inspection,true,null,null);fail();}catch(IOException expected){assertEquals(result.photoNamingError,expected.getMessage());}assertEquals(1,StoredInspections.read(app,StoredInspections.DRAFTS).size());}
    @Test public void cleanupFailurePreservesDamagedStoreAndDoesNotUndoRemoteSuccess() throws Exception {InspectionSubmission.Result result=save(null,true,false);byte[]damaged={1,2,3};Files.write(app.getFileStreamPath(StoredInspections.DRAFTS).toPath(),damaged);try{InspectionSubmission.completeLocal(result,app,inspection,true,null,null);fail();}catch(StoredInspections.StorageException expected){assertNull(result.remoteError);assertArrayEquals(damaged,Files.readAllBytes(app.getFileStreamPath(StoredInspections.DRAFTS).toPath()));}}
    @Test public void characterizationRepeatedSubmissionCallsWriteTwice(){save(null,true,false);save(null,true,false);assertEquals(Arrays.asList("insert","names","insert","names"),events);}
    @Test public void characterizationExceptionWithoutMessageStillReturnsNullError(){source.nullMessage=true;InspectionSubmission.Result result=save(null,true,false);assertNull(result.remoteError);assertEquals(Collections.singletonList("insert"),events); /* Existing ambiguous error contract; separate future fix. */}
}
