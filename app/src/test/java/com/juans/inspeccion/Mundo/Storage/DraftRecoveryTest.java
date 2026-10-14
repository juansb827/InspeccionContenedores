package com.juans.inspeccion.Mundo.Storage;

import android.content.Context;
import com.juans.inspeccion.Mundo.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import java.io.*;
import java.nio.file.Files;
import java.util.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=29, application=android.app.Application.class)
public class DraftRecoveryTest {
    private Context app;
    @Before public void setup(){app=RuntimeEnvironment.getApplication();Pendientes.invalidate();}
    @After public void cleanup(){Pendientes.invalidate();}
    // DRAFT-001/002/004, current serialized format only; writers have not switched.
    @Test public void absenceIsAnEmptyListWithoutCreatingAFile(){assertTrue(Pendientes.darListaTurnos(app).isEmpty());assertFalse(app.getFileStreamPath(StoredInspections.DRAFTS).exists());}
    @Test public void saveInvalidateAndReopenPreservesModeHiddenFieldsDamageAndAlbum() throws Exception {Inspeccion i=new Inspeccion();i.setTipoInspeccion("SALIDA");i.setTipoDocumento("ET");i.setUsaTurno(true);i.getInformacion().put("NANO","2026");i.getInformacion().put("NTURNO","5");i.getFotosGenerales().agregarFoto("content://fixture/1");Danio damage=new Danio();damage.getDetalles().put(DaniosManager.COSTO_TOTAL_DANIO,"33");i.getDaniosManager().getListaDanios().add(damage);Pendientes.upsert(i,app);Pendientes.invalidate();Inspeccion reopened=Pendientes.darListaTurnos(app).get(0);assertEquals("SALIDA",reopened.getTipoInspeccion());assertEquals("ET",reopened.getTipoDocumento());assertTrue(reopened.isUsaTurno());assertEquals("2026",reopened.getInformacion().get("NANO"));assertEquals(i.getPhotoId(),reopened.getPhotoId());assertEquals(i.getFotosGenerales().getId(),reopened.getFotosGenerales().getId());assertEquals("content://fixture/1",reopened.getFotosGenerales().getFotos().get(0));assertEquals("33",reopened.getDaniosManager().getListaDanios().get(0).getDetalles().get(DaniosManager.COSTO_TOTAL_DANIO));}
    @Test public void atomicBackupRecoversWhenBaseFileIsDamaged() throws Exception {Inspeccion i=new Inspeccion();Pendientes.upsert(i,app);File base=app.getFileStreamPath(StoredInspections.DRAFTS);byte[] good=Files.readAllBytes(base.toPath());Files.write(new File(base+".bak").toPath(),good);Files.write(base.toPath(),new byte[]{1,2,3});Pendientes.invalidate();assertEquals(i.getPhotoId(),Pendientes.darListaTurnos(app).get(0).getPhotoId());assertArrayEquals(good,Files.readAllBytes(base.toPath()));}
    @Test public void corruptStoreCannotBeClearedAndOriginalBytesRemain() throws Exception {File base=app.getFileStreamPath(StoredInspections.DRAFTS);byte[] bytes={1,2,3};Files.write(base.toPath(),bytes);try{Pendientes.borrarTodo(app);fail();}catch(StoredInspections.StorageException expected){assertArrayEquals(bytes,Files.readAllBytes(base.toPath()));}}
    @Test public void wrongCollectionTypeIsPreservedInsteadOfConvertedToEmpty() throws Exception {File base=app.getFileStreamPath(StoredInspections.DRAFTS);AtomicObjects.write(base,"not an inspection collection");byte[]bytes=Files.readAllBytes(base.toPath());try{Pendientes.upsert(new Inspeccion(),app);fail();}catch(StoredInspections.StorageException expected){assertArrayEquals(bytes,Files.readAllBytes(base.toPath()));}}
    @Test public void unreadableDraftPathIsAnErrorRatherThanAnEmptyStore(){File path=app.getFileStreamPath(StoredInspections.DRAFTS);assertTrue(path.mkdir());try{Pendientes.darListaTurnos(app);fail();}catch(StoredInspections.StorageException expected){assertTrue(path.isDirectory());assertTrue(expected.getCause() instanceof IOException);}}
    @Test public void damagedUnusedQueueDoesNotAffectDirectDraftPersistence() throws Exception {File queue=app.getFileStreamPath(StoredInspections.UPLOADS);byte[] bytes={1,2,3};Files.write(queue.toPath(),bytes);Inspeccion i=new Inspeccion();Pendientes.upsert(i,app);Pendientes.invalidate();assertEquals(i.getPhotoId(),Pendientes.darListaTurnos(app).get(0).getPhotoId());assertArrayEquals(bytes,Files.readAllBytes(queue.toPath()));}
    // DRAFT-003: actual public-media deletion is separately covered by the media suites/device gate.
    @Test public void clearDraftsDoesNotClearIndependentMediaIndex() throws Exception {File index=app.getFileStreamPath("local-media-index.json");byte[]bytes="{\"version\":1,\"albums\":{}}".getBytes(java.nio.charset.StandardCharsets.UTF_8);Files.write(index.toPath(),bytes);Pendientes.upsert(new Inspeccion(),app);Pendientes.borrarTodo(app);Pendientes.invalidate();assertTrue(Pendientes.darListaTurnos(app).isEmpty());assertArrayEquals(bytes,Files.readAllBytes(index.toPath()));}
}
