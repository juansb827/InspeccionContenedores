package com.juans.inspeccion.ui.settings;

import android.app.Activity;
import androidx.appcompat.app.AlertDialog;
import com.juans.inspeccion.data.local.StorageCutover;

/** Notice acknowledgement is durable only after a successful marker write. */
public final class StorageNotice {
    public static final int MENU_ID=0x7e010301;
    private StorageNotice() {}
    public static void show(Activity activity,boolean always) {
        try {
            StorageCutover.Status status=StorageCutover.prepare(activity);
            if(!always&&!status.noticePending) return;
            new AlertDialog.Builder(activity).setTitle("Estado de almacenamiento").setMessage(status.description())
                .setPositiveButton("Entendido",(dialog,which)->{
                    try { StorageCutover.acknowledge(activity); }
                    catch(Exception e) { error(activity,"No se pudo confirmar el aviso. Se volverá a mostrar; los archivos se conservan."); }
                }).setNegativeButton("Cerrar",null).show();
        } catch(Exception e) { error(activity,"No se pudo revisar el almacenamiento local. Los archivos se conservan para recuperación. "+e.getMessage()); }
    }
    private static void error(Activity activity,String message) { new AlertDialog.Builder(activity).setTitle("Estado de almacenamiento").setMessage(message).setPositiveButton("Aceptar",null).show(); }
}
