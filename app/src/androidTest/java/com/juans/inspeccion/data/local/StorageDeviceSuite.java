package com.juans.inspeccion.data.local;

import com.juans.inspeccion.data.local.FotosPendientes;
import com.juans.inspeccion.Mundo.Inspeccion;
import com.juans.inspeccion.data.local.Pendientes;
import com.juans.inspeccion.data.local.StorageFormatProbe;
import com.juans.inspeccion.data.local.StoredInspections;

import android.app.*;
import android.content.*;
import android.os.*;
import com.juans.inspeccion.Mundo.*;
import com.juans.inspeccion.Mundo.Storage.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/** Dedicated local fixture device only; no connections/settings, no business submission. */
public final class StorageDeviceSuite {
    private StorageDeviceSuite() {}
    public static void run(Instrumentation runner) {
        Bundle report=new Bundle(); Context app=runner.getTargetContext();
        try {
            Pendientes.invalidate();
            // install -r package-move run first reopens data produced by the storage-prerequisite APK.
            if(app.getFileStreamPath(StoredInspections.DRAFTS).exists()) {
                ArrayList<Inspeccion> previous=StoredInspections.read(app,StoredInspections.DRAFTS);
                for(Inspeccion prior:previous) check(prior.getPhotoId()!=null&&prior.getFotosGenerales()!=null,"pre-move current drafts reopen");
            }
            for(String name:new String[]{"photo-publications","video-publications","fixture-photo-capture"}) {
                File file=app.getFileStreamPath(name);
                if(file.exists()) AtomicObjects.read(file); // Actual pre-move nested publication/snapshot descriptors.
            }
            for(String name:new String[]{StoredInspections.DRAFTS,StorageCutover.STATE}) for(String suffix:new String[]{"",".bak",".new"}) Files.deleteIfExists(app.getFileStreamPath(name+suffix).toPath());
            File archive=new File(app.getFilesDir(),StorageCutover.ARCHIVE); if(archive.exists()) for(File f:Objects.requireNonNull(archive.listFiles())) Files.delete(f.toPath());
            byte[] retired={(byte)0xac,(byte)0xed,0,5,1,2,3};
            for(String name:new String[]{"listaPendientes.bak","fotosPendientes.new","pending-photo-capture","pending-video-capture","pending-media-deletion"}) Files.write(app.getFileStreamPath(name).toPath(),retired);
            StorageCutover.Status status=StorageCutover.prepare(app); check(status.isolated.size()==5&&status.noticePending,"legacy isolation");
            StorageCutover.acknowledge(app); check(!StorageCutover.prepare(app).noticePending,"durable acknowledgement");
            Inspeccion i=new Inspeccion(); i.setTipoDocumento("ET"); i.setTipoInspeccion("SALIDA"); i.getInformacion().put("fixture","ñ"); i.getFotosGenerales().agregarFoto("content://fixture/photo/1");
            Pendientes.upsert(i,app); Pendientes.invalidate(); check(Pendientes.darListaTurnos(app).get(0).getPhotoId().equals(i.getPhotoId()),"real save/reopen");
            byte[] before=Files.readAllBytes(app.getFileStreamPath(StoredInspections.DRAFTS).toPath());
            try { StoredInspections.write(app,StoredInspections.DRAFTS,new ArrayList<>(),()->{throw new IOException("fixture failure");}); throw new AssertionError("failed write accepted"); } catch(StoredInspections.StorageException expected) { check(expected.outcome==StorageFormatProbe.Outcome.IO_ERROR,"write classification"); }
            check(Arrays.equals(before,Files.readAllBytes(app.getFileStreamPath(StoredInspections.DRAFTS).toPath())),"failed write preservation");
            File draft=app.getFileStreamPath(StoredInspections.DRAFTS), backup=new File(draft+".bak");
            Files.write(backup.toPath(),before); Files.write(draft.toPath(),"{\"format\":\"inspection-drafts\",\"schemaVersion\":2}".getBytes(java.nio.charset.StandardCharsets.UTF_8));
            try { StoredInspections.read(app,StoredInspections.DRAFTS); throw new AssertionError("newer primary replaced by backup"); }
            catch(StoredInspections.StorageException expected) { check(expected.outcome==StorageFormatProbe.Outcome.UNSUPPORTED_NEWER,"newer candidate preservation"); }
            Files.delete(backup.toPath()); Files.write(draft.toPath(),before);
            check(StorageCutover.currentJournal(app,"pending-photo-capture").getName().endsWith("-v1"),"isolated operation namespace");
            check(!new FotosPendientes(app).hayPendientes(),"queue cannot replay");
            String[] components={"com.juans.inspeccion.ui.access.LoginActivity","com.juans.inspeccion.ui.inspection.InspeccionActivity","com.juans.inspeccion.ui.pending.PendientesActivity","com.juans.inspeccion.ui.settings.SettingsActivity"};
            for(String name:components) app.getPackageManager().getActivityInfo(new ComponentName(app,name),0);
            Activity login=runner.startActivitySync(new Intent().setComponent(new ComponentName(app,components[0])).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            runner.runOnMainSync(()->{
                android.view.ContextThemeWrapper themed=new android.view.ContextThemeWrapper(login,com.juans.inspeccion.R.style.PhotoGalleryTheme);
                for(int layout:new int[]{com.juans.inspeccion.R.layout.activity_inspeccion,com.juans.inspeccion.R.layout.fragment_entrada_pag1,com.juans.inspeccion.R.layout.fragment_salida_pag1,com.juans.inspeccion.R.layout.entrada_empresa_transporte,com.juans.inspeccion.R.layout.salida_empresa_transporte,com.juans.inspeccion.R.layout.nuevo_danio})
                    android.view.LayoutInflater.from(themed).inflate(layout,null);
                androidx.fragment.app.FragmentFactory factory=new androidx.fragment.app.FragmentFactory();
                check(factory.instantiate(login.getClassLoader(),"com.juans.inspeccion.Interfaz.Inspeccion_tabs.p1") instanceof com.juans.inspeccion.ui.inspection.InspectionInformationFragment,"original information fragment name");
                check(factory.instantiate(login.getClassLoader(),"com.juans.inspeccion.Interfaz.Inspeccion_tabs.p2") instanceof com.juans.inspeccion.ui.inspection.InspectionDamageFragment,"original damage fragment name");
                check(login.findViewById(com.juans.inspeccion.R.id.btnLogearse)!=null,"stable launcher XML");
                login.finish();
            });
            runner.waitForIdleSync();
            report.putString("stream","PASS: storage cutover, acknowledgement, draft commit/recovery, queue retirement, old fragment names, registered components and launcher/operator-form XML on API "+Build.VERSION.SDK_INT+"\n");
            runner.finish(Activity.RESULT_OK,report);
        } catch(Throwable e) { report.putString("stream","FAIL: "+android.util.Log.getStackTraceString(e)); runner.finish(Activity.RESULT_CANCELED,report); }
    }
    private static void check(boolean value,String message) { if(!value) throw new AssertionError(message); }
}
