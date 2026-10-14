package com.juans.inspeccion.ui.settings;

import android.app.Activity;
import androidx.appcompat.app.AlertDialog;
import com.juans.inspeccion.data.local.StorageCutover;

/** Storage diagnostics shown only on an explicit Settings request. */
public final class StorageNotice {
    public static final int MENU_ID=0x7e010301;
    private StorageNotice() {}
    public static void show(Activity activity) {
        try {
            StorageCutover.Status status=StorageCutover.prepare(activity);
            new AlertDialog.Builder(activity).setTitle("Estado de almacenamiento").setMessage(status.description())
                .setPositiveButton("Cerrar",null).show();
        } catch(Exception e) { error(activity,"No se pudo revisar el almacenamiento local. Los archivos se conservan para recuperación. "+e.getMessage()); }
    }
    private static void error(Activity activity,String message) { new AlertDialog.Builder(activity).setTitle("Estado de almacenamiento").setMessage(message).setPositiveButton("Aceptar",null).show(); }
}
