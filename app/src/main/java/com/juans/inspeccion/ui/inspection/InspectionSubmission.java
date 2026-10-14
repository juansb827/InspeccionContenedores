package com.juans.inspeccion.ui.inspection;

import com.juans.inspeccion.data.remote.sqlserver.InspeccionDataSource;

import android.content.Context;
import android.content.res.Resources;
import com.juans.inspeccion.CustomView.CustomView;
import com.juans.inspeccion.ui.inspection.InspeccionActivity;
import com.juans.inspeccion.Mundo.Inspeccion;
import com.juans.inspeccion.data.local.Pendientes;
import com.juans.inspeccion.Mundo.Storage.CaptureCoordinator;
import com.juans.inspeccion.Mundo.Storage.VideoCaptureCoordinator;
import com.juans.inspeccion.R;
import java.io.IOException;
import java.util.ArrayList;

/** Existing save work and post-save local cleanup, separated from AsyncTask/toasts for testing. */
public final class InspectionSubmission {
    private InspectionSubmission() {}
    public interface NumberAssignment { void assign(String number) throws Exception; }
    public interface PhotoNaming { void rename(String number, String container) throws IOException; }
    public static final class Result {
        public final String remoteError, photoNamingError;
        private Result(String remote, String photo) { remoteError=remote; photoNamingError=photo; }
    }
    public static Result save(InspeccionDataSource source, ArrayList<CustomView> fields,
            Inspeccion inspection, Resources res, String action, boolean usesTurno,
            NumberAssignment numberAssignment, PhotoNaming photoNaming) {
        String photoError=null;
        try {
            if (action == null || !action.equals(InspeccionActivity.MODIFICAR_DOCUMENTO)) {
                if (!usesTurno) {
                    numberAssignment.assign(source.generarConsecutivoSinTurno(inspection,res));
                    inspection.getInformacion().put(res.getString(R.string.TER_RAZONS),
                            source.darAgenteLinea(inspection.getInformacion(),res));
                }
                source.insert(fields,inspection,res);
            } else source.re_insert(fields,inspection,res);
            try { photoNaming.rename(inspection.getInformacion().get(res.getString(R.string.NNUMDOC)),
                    inspection.getDatosContenedor().get(res.getString(R.string.CCODCNTR))); }
            catch (IOException e) { photoError=e.getMessage(); }
            return new Result(null,photoError);
        } catch (Exception e) {
            e.printStackTrace();
            // Preserve the existing null-message behavior; characterize rather than fix it here.
            return new Result(e.getMessage(),photoError);
        }
    }
    public static boolean completeLocal(Result result, Context context, Inspeccion inspection,
            boolean resumedDraft, CaptureCoordinator photo, VideoCaptureCoordinator video) throws IOException {
        if (result.remoteError != null) return false;
        if (result.photoNamingError != null) throw new IOException(result.photoNamingError);
        if (resumedDraft) Pendientes.remove(inspection.getPhotoId(),context);
        if (photo != null) photo.acknowledgeSavedState();
        if (video != null) video.acknowledgeSavedState();
        return true;
    }
}
