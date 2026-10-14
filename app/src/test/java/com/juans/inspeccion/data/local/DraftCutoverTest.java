package com.juans.inspeccion.data.local;

import com.juans.inspeccion.Mundo.Album;
import com.juans.inspeccion.Mundo.Danio;
import com.juans.inspeccion.Mundo.DaniosManager;
import com.juans.inspeccion.Mundo.FotosPendientes;
import com.juans.inspeccion.Mundo.Inspeccion;
import com.juans.inspeccion.Mundo.Pendientes;
import com.juans.inspeccion.Mundo.Storage.CaptureCoordinator;
import com.juans.inspeccion.Mundo.Storage.LocalMediaIndex;
import com.juans.inspeccion.Mundo.Storage.MediaPhotoStore;
import com.juans.inspeccion.Mundo.Storage.StorageFormatProbe;
import com.juans.inspeccion.Mundo.Storage.StoredInspections;

import android.app.Application;
import android.content.Context;
import com.juans.inspeccion.Mundo.*;
import com.juans.inspeccion.Mundo.Storage.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import org.json.*;
import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import static org.junit.Assert.*;
import static com.juans.inspeccion.data.local.StorageFormatProbe.Outcome.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk={29,34},application=Application.class)
public class DraftCutoverTest {
    private Context app;
    @Before public void setup() { app=RuntimeEnvironment.getApplication(); Pendientes.invalidate(); }
    @After public void clear() { Pendientes.invalidate(); }
    private File current() { return app.getFileStreamPath(StoredInspections.DRAFTS); }
    private byte[] content(File f) throws IOException { return Files.readAllBytes(f.toPath()); }
    private void write(File f,String s) throws IOException { Files.write(f.toPath(),s.getBytes(StandardCharsets.UTF_8)); }
    private ArrayList<Inspeccion> one(Inspeccion i) { return new ArrayList<>(Collections.singletonList(i)); }
    private void error(StorageFormatProbe.Outcome outcome) {
        byte[] before; try { before=content(current()); } catch(IOException e) { before=null; }
        try { StoredInspections.write(app,StoredInspections.DRAFTS,one(new Inspeccion())); fail("must preserve evidence"); }
        catch(StoredInspections.StorageException e) { assertEquals(outcome,e.outcome); }
        if(before!=null) try { assertArrayEquals(before,content(current())); } catch(IOException e) { throw new AssertionError(e); }
    }
    private byte[] fixture(String name) throws IOException {
        try(InputStream in=getClass().getResourceAsStream("/released-d9c52d3/"+name)) { assertNotNull(in); return StrictJson.bytes(in); }
    }
    // DRAFT-001/002/006 and MEDIA-005: real record graph, not an envelope-only fake.
    @Test public void completeRoundTripPreservesAllStateAndSharedAlbumIdentityWithoutRecalculation() throws Exception {
        Inspeccion i=new Inspeccion(); i.setTipoInspeccion("SALIDA"); i.setTipoDocumento("ET");
        i.setFechaInspeccion(new String[]{"13","10","2026","14","31","2026-10-13 14:31","13/10/2026"});
        i.setPerteneceEnBooking(true); i.setEstaEnPatio(true); i.setHaySaldoBooking(true); i.setUsaTurno(true);
        i.getInformacion().put("NANO","2026"); i.getInformacion().put("empty",""); i.getInformacion().put("optional",null); i.getDatosContenedor().put("CCODCNTR","FIXTURE");
        ArrayList<HashMap<String,String>> rows=new ArrayList<>(); HashMap<String,String> row=new HashMap<>(); row.put("NTURNO","7"); rows.add(row); i.setInfoBooking(rows); i.setTurnosPendientes(rows);
        Album shared=i.getFotosGenerales(); shared.agregarFoto("content://fixture/photo/1"); shared.setFotos_tomadas(9); shared.setRemoteName("content://fixture/photo/1","fixture.jpg");
        Danio d=new Danio(); d.getDetalles().put("hidden","valor español"); d.getDetalles().put(DaniosManager.COSTO_TOTAL_DANIO,"4"); d.setAlbum(shared); i.getDaniosManager().getListaDanios().add(d); i.getDaniosManager().recalcularValorTotal(); i.getDaniosManager().recalcularValorTotal();
        Album extra=new Album(); i.setFotosInspeccion(new ArrayList<>(Arrays.asList(extra,shared)));
        LocalMediaIndex index=new LocalMediaIndex(app); index.addVideo(i.getPhotoId(),shared,"content://fixture/video/1",1200);
        Pendientes.upsert(i,app); Pendientes.invalidate(); Inspeccion r=Pendientes.darListaTurnos(app).get(0);
        assertEquals(i.getPhotoId(),r.getPhotoId()); assertEquals("SALIDA",r.getTipoInspeccion()); assertEquals("ET",r.getTipoDocumento()); assertArrayEquals(i.getFechaInspeccion(),r.getFechaInspeccion());
        assertTrue(r.isPerteneceEnBooking()&&r.estaEnPatio()&&r.isHaySaldoBooking()&&r.isUsaTurno()); assertEquals(i.getInformacion(),r.getInformacion()); assertEquals(i.getDatosContenedor(),r.getDatosContenedor()); assertEquals(rows,r.getInfoBooking()); assertEquals(rows,r.getTurnosPendientes());
        assertEquals(8,r.getDaniosManager().darValorTotal(),0); assertEquals(d.getDetalles(),r.getDaniosManager().getListaDanios().get(0).getDetalles());
        assertSame(r.getFotosGenerales(),r.getDaniosManager().getListaDanios().get(0).getAlbum()); assertSame(r.getFotosGenerales(),r.getFotosInspeccion().get(1)); assertEquals(extra.getId(),r.getFotosInspeccion().get(0).getId());
        assertEquals(9,r.getFotosGenerales().getFotos_tomadas()); assertEquals("fixture.jpg",r.getFotosGenerales().recordedRemoteName("content://fixture/photo/1"));
        assertEquals(2,index.items(r.getPhotoId(),r.getFotosGenerales()).size()); assertFalse(new String(content(current()),StandardCharsets.UTF_8).contains("com.juans"));
    }
    @Test public void failedDraftCommitKeepsLastCompleteDraftAndCanRetry() throws Exception {
        Inspeccion first=new Inspeccion(); Pendientes.upsert(first,app); byte[] before=content(current());
        try { StoredInspections.write(app,StoredInspections.DRAFTS,one(new Inspeccion()),()->{throw new IOException("sanitized injected disk failure");}); fail(); }
        catch(StoredInspections.StorageException e) { assertEquals(IO_ERROR,e.outcome); }
        assertArrayEquals(before,content(current())); Pendientes.invalidate(); assertEquals(first.getPhotoId(),Pendientes.darListaTurnos(app).get(0).getPhotoId());
        Pendientes.upsert(new Inspeccion(),app); assertEquals(2,StoredInspections.read(app,StoredInspections.DRAFTS).size());
    }
    @Test public void backupWithoutPrimaryIsRecoveredNotTreatedAsEmpty() throws Exception { Pendientes.upsert(new Inspeccion(),app); byte[] before=content(current()); Files.move(current().toPath(),new File(current()+".bak").toPath()); Pendientes.invalidate(); assertEquals(1,Pendientes.darListaTurnos(app).size()); assertArrayEquals(before,content(current())); }
    @Test public void stagingWithoutPrimaryCannotBeSilentlyReset() throws Exception { write(new File(current()+".new"),"{partial"); try { Pendientes.upsert(new Inspeccion(),app); fail(); } catch(StoredInspections.StorageException e) { assertEquals(CORRUPT,e.outcome); } assertFalse(current().exists()); assertEquals("{partial",new String(content(new File(current()+".new")),StandardCharsets.UTF_8)); }
    @Test public void invalidBackupCannotEraseValidPrimary() throws Exception { Pendientes.upsert(new Inspeccion(),app); byte[] before=content(current()); write(new File(current()+".bak"),"{broken"); error(CORRUPT); assertArrayEquals(before,content(current())); }
    @Test public void staleStagingDoesNotReplaceCommittedPrimary() throws Exception { Inspeccion i=new Inspeccion(); Pendientes.upsert(i,app); write(new File(current()+".new"),"{partial"); Pendientes.invalidate(); assertEquals(i.getPhotoId(),Pendientes.darListaTurnos(app).get(0).getPhotoId()); }
    @Test public void newerPrimaryAndStagingCannotBeDiscardedByBackupRecoveryOrSave() throws Exception {
        Pendientes.upsert(new Inspeccion(),app); byte[] good=content(current());
        String newer="{\"format\":\"inspection-drafts\",\"schemaVersion\":2}";
        Files.write(new File(current()+".bak").toPath(),good); write(current(),newer); error(UNSUPPORTED_NEWER);
        assertArrayEquals(good,content(new File(current()+".bak")));
        Files.delete(new File(current()+".bak").toPath()); Files.write(current().toPath(),good); write(new File(current()+".new"),newer);
        error(UNSUPPORTED_NEWER); assertEquals(newer,new String(content(new File(current()+".new")),StandardCharsets.UTF_8));
    }
    @Test public void unsupportedNewerIsPreserved() throws Exception { write(current(),"{\"format\":\"inspection-drafts\",\"schemaVersion\":2}"); error(UNSUPPORTED_NEWER); }
    @Test public void retiredJsonSchemaIsIsolatedAndAllowsCurrentDrafts() throws Exception { write(current(),"{\"format\":\"inspection-drafts\",\"schemaVersion\":0}"); Pendientes.upsert(new Inspeccion(),app); assertEquals(1,StoredInspections.read(app,StoredInspections.DRAFTS).size()); assertTrue(StorageCutover.prepare(app).noticePending); assertEquals(1,StorageCutover.prepare(app).isolated.size()); }
    @Test public void unknownFileIsPreserved() throws Exception { write(current(),"unrecognized bytes"); error(UNKNOWN); }
    @Test public void invalidRecordIsNotAValidEnvelope() throws Exception { write(current(),"{\"format\":\"inspection-drafts\",\"schemaVersion\":1,\"records\":[{}]}"); error(CORRUPT); }
    @Test public void invalidUtf8IsCorrupt() throws Exception { Files.write(current().toPath(),new byte[]{'{','"',(byte)0xc3,0x28,'"','}'}); error(CORRUPT); }
    @Test public void lenientSyntaxAndDuplicateKeysAreRejected() throws Exception { for(String s:new String[]{"{'format':'inspection-drafts'}","{format:1}","{\"format\":\"inspection-drafts\",\"schemaVersion\":1,\"records\":[],\"records\":[]}","{\"format\":\"inspection-drafts\",\"schemaVersion\":01,\"records\":[]}"}) { write(current(),s); error(CORRUPT); } }
    @Test public void invalidRelationsDuplicateIdsAndNonBooleanAreRejected() throws Exception {
        JSONObject good=StrictJson.object(DraftCodec.encode(one(new Inspeccion()))); JSONObject record=good.getJSONArray("records").getJSONObject(0);
        record.put("generalAlbum",UUID.randomUUID().toString()); write(current(),good.toString()); error(CORRUPT);
        good=StrictJson.object(DraftCodec.encode(one(new Inspeccion()))); good.getJSONArray("records").put(good.getJSONArray("records").getJSONObject(0)); write(current(),good.toString()); error(CORRUPT);
        good=StrictJson.object(DraftCodec.encode(one(new Inspeccion()))); good.getJSONArray("records").getJSONObject(0).put("usesTurn","true"); write(current(),good.toString()); error(CORRUPT);
    }
    @Test public void writerResourceLimitsAndInvalidModelPreserveCurrentDraft() throws Exception {
        Inspeccion i=new Inspeccion(); Pendientes.upsert(i,app); byte[] before=content(current());
        char[] big=new char[16385]; Arrays.fill(big,'x'); i.getInformacion().put("oversize",new String(big));
        try { Pendientes.upsert(i,app); fail(); } catch(StoredInspections.StorageException e) { assertEquals(CORRUPT,e.outcome); } assertArrayEquals(before,content(current()));
    }
    @Test public void oversizeInputIsBoundedAndCorrupt() throws Exception { byte[] bytes=new byte[StrictJson.MAX_BYTES+1]; bytes[0]='{'; Files.write(current().toPath(),bytes); error(CORRUPT); }
    @Test public void ioFailureIsDistinctAndNeverEmpty() throws Exception { assertTrue(current().mkdir()); error(IO_ERROR); assertTrue(current().isDirectory()); }
    @Test public void allLegacyCandidatesAreIsolatedAndMediaAndBookkeepingRemainByteExact() throws Exception {
        for(String base:new String[]{"listaPendientes","fotosPendientes","pending-photo-capture","pending-video-capture","pending-media-deletion"}) for(String suffix:new String[]{"",".bak",".new"}) Files.write(app.getFileStreamPath(base+suffix).toPath(),fixture("listaPendientes"));
        Map<File,byte[]> preserved=new HashMap<>(); for(String name:new String[]{"photo-publications","video-publications","legacy-photo-imports","local-media-index.json"}) { File file=app.getFileStreamPath(name); byte[] b={4,5,6}; Files.write(file.toPath(),b); preserved.put(file,b); }
        File media=new File(app.getExternalFilesDir(null),"fixture-photo.jpg"); Files.write(media.toPath(),new byte[]{7,8,9}); preserved.put(media,content(media));
        StorageCutover.Status status=StorageCutover.prepare(app); assertEquals(15,status.isolated.size()); assertTrue(status.noticePending); assertTrue(status.unresolved.isEmpty());
        for(String name:status.isolated) assertArrayEquals(fixture("listaPendientes"),content(new File(new File(app.getFilesDir(),StorageCutover.ARCHIVE),name)));
        for(Map.Entry<File,byte[]> e:preserved.entrySet()) assertArrayEquals(e.getValue(),content(e.getKey()));
        Pendientes.upsert(new Inspeccion(),app); assertEquals(1,StoredInspections.read(app,StoredInspections.DRAFTS).size());
    }
    @Test public void interruptedIsolationIsRecoveredBeforeMarkerPublicationWithoutLosingNewData() throws Exception {
        Inspeccion i=new Inspeccion(); Pendientes.upsert(i,app); byte[] before=content(current());
        Files.write(app.getFileStreamPath("listaPendientes.bak").toPath(),fixture("listaPendientes"));
        try { StorageCutover.prepare(app,source->{throw new IOException("crash boundary");}); fail(); } catch(IOException expected) {}
        StorageCutover.Status status=StorageCutover.prepare(app); assertTrue(status.noticePending); assertEquals(1,status.isolated.size()); assertArrayEquals(before,content(current()));
        StorageCutover.acknowledge(app); assertFalse(StorageCutover.prepare(app).noticePending); assertFalse(StorageCutover.prepare(app).noticePending);
    }
    @Test public void failedMarkerWriteDoesNotLoseIsolatedFilesAndRetryRestoresNotice() throws Exception {
        Files.write(app.getFileStreamPath("listaPendientes").toPath(),fixture("listaPendientes")); File state=app.getFileStreamPath(StorageCutover.STATE); assertTrue(state.mkdir());
        try { StorageCutover.prepare(app); fail(); } catch(IOException expected) {}
        assertTrue(app.getFileStreamPath("listaPendientes").exists()); assertTrue(state.delete()); assertTrue(StorageCutover.prepare(app).noticePending);
    }
    @Test public void unrecognizedOldDraftBlocksEmptyReplacementAndKeepsBytes() throws Exception { File old=app.getFileStreamPath("listaPendientes.bak"); write(old,"unknown"); try { Pendientes.upsert(new Inspeccion(),app); fail(); } catch(StoredInspections.StorageException e) { assertEquals(UNKNOWN,e.outcome); } assertEquals("unknown",new String(content(old),StandardCharsets.UTF_8)); assertFalse(current().exists()); }
    @Test public void unrecognizedQueueAndOldJournalNeverBlockDraftsOrReplay() throws Exception { write(app.getFileStreamPath("fotosPendientes"),"unknown"); write(app.getFileStreamPath("pending-photo-capture"),"unknown"); Pendientes.upsert(new Inspeccion(),app); assertFalse(StorageCutover.currentJournal(app,"pending-photo-capture").exists()); assertEquals(2,StorageCutover.prepare(app).unresolved.size()); assertFalse(new FotosPendientes(app).hayPendientes()); }
    @Test public void corruptAcknowledgementIsNotSilentlyReset() throws Exception { StorageCutover.prepare(app); write(app.getFileStreamPath(StorageCutover.STATE),"{invalid"); try { Pendientes.upsert(new Inspeccion(),app); fail(); } catch(StoredInspections.StorageException e) { assertEquals(CORRUPT,e.outcome); } assertFalse(current().exists()); }
    @Test public void actualOldCaptureSnapshotIsIsolatedWithoutPublishingOrDiscardingPrivateBytes() throws Exception {
        MediaPhotoStore photos=new MediaPhotoStore(app);
        CaptureCoordinator old=new CaptureCoordinator(photos,app.getFileStreamPath("pending-photo-capture"));
        Inspeccion i=new Inspeccion(); old.begin(i,i.getFotosGenerales(),"fixture.jpg","fixture.jpg");
        File original=new File(old.session().path); Files.write(original.toPath(),new byte[]{7,8,9});
        old.cameraResult(true); byte[] journal=content(app.getFileStreamPath("pending-photo-capture"));
        CaptureCoordinator current=new CaptureCoordinator(photos,StorageCutover.currentJournal(app,"pending-photo-capture"));
        assertNull(current.session()); assertArrayEquals(new byte[]{7,8,9},content(original));
        String archive=StorageCutover.prepare(app).isolated.iterator().next(); assertArrayEquals(journal,content(new File(new File(app.getFilesDir(),StorageCutover.ARCHIVE),archive)));
    }
    @Test public void markerCommitFailureAfterIsolationRetainsEvidenceAndRecoversNotice() throws Exception {
        StorageCutover.prepare(app); Files.write(app.getFileStreamPath("listaPendientes").toPath(),fixture("listaPendientes"));
        try { StorageCutover.prepare(app,source->{},()->{throw new IOException("marker commit failure");}); fail(); } catch(IOException expected) {}
        assertFalse(app.getFileStreamPath("listaPendientes").exists()); assertEquals(1,new File(app.getFilesDir(),StorageCutover.ARCHIVE).listFiles().length);
        assertTrue(StorageCutover.prepare(app).noticePending); StorageCutover.acknowledge(app); assertFalse(StorageCutover.prepare(app).noticePending);
    }
    @Test public void supportedCompleteRecordsIgnoreWriterAppVersion() throws Exception {
        Inspeccion i=new Inspeccion(); JSONObject root=StrictJson.object(DraftCodec.encode(one(i))); root.put("writerAppVersion","999.unknown"); write(current(),root.toString());
        assertEquals(i.getPhotoId(),StoredInspections.read(app,StoredInspections.DRAFTS).get(0).getPhotoId());
    }
    @Test public void failedQueueIsolationDoesNotBlockHealthyNewDraftsOrReplayTransfers() throws Exception {
        Files.write(app.getFileStreamPath("fotosPendientes").toPath(),fixture("fotosPendientes")); write(new File(app.getFilesDir(),StorageCutover.ARCHIVE),"obstacle");
        Pendientes.upsert(new Inspeccion(),app); assertEquals(1,StoredInspections.read(app,StoredInspections.DRAFTS).size());
        assertEquals(IO_ERROR,StorageCutover.prepare(app).unresolved.get("fotosPendientes")); assertArrayEquals(fixture("fotosPendientes"),content(app.getFileStreamPath("fotosPendientes"))); assertFalse(new FotosPendientes(app).hayPendientes());
    }
    @Test public void staleRecognizedLegacyFilesCannotReplaceCurrentDrafts() throws Exception { Inspeccion i=new Inspeccion(); Pendientes.upsert(i,app); byte[] before=content(current()); Files.write(app.getFileStreamPath("listaPendientes").toPath(),fixture("listaPendientes")); Pendientes.invalidate(); assertEquals(i.getPhotoId(),Pendientes.darListaTurnos(app).get(0).getPhotoId()); assertArrayEquals(before,content(current())); }
}
