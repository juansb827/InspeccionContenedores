package com.juans.inspeccion.ui.printing;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;
import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import com.juans.inspeccion.platform.printing.PrinterAccess;

/** One operation may wait for permission/enablement. Recreation safely requires the user to retry. */
public final class PrinterPermissionGate {
    private final ComponentActivity activity;
    private final ActivityResultLauncher<String[]> permissions;
    private final ActivityResultLauncher<Intent> enable;
    private Runnable pending;

    public PrinterPermissionGate(ComponentActivity activity) { this(activity, activity.getActivityResultRegistry()); }
    public PrinterPermissionGate(ComponentActivity activity, androidx.activity.result.ActivityResultRegistry registry) {
        this.activity = activity;
        permissions = activity.registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), registry, result -> {
            if (pending == null) return;
            if (PrinterAccess.granted(activity)) continueOperation();
            else fail("Permiso de Bluetooth denegado; no se imprimió");
        });
        enable = activity.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), registry, result -> {
            if (pending == null) return;
            if (PrinterAccess.isReady(activity)) runPending();
            else fail("Bluetooth está desactivado o no tiene permiso; no se imprimió");
        });
    }
    public void request(Runnable action) {
        if (pending != null) return;
        if (BluetoothAdapter.getDefaultAdapter() == null) { fail("Este dispositivo no admite Bluetooth"); return; }
        pending = action;
        if (!PrinterAccess.granted(activity)) permissions.launch(PrinterAccess.permissions(Build.VERSION.SDK_INT));
        else continueOperation();
    }
    private void continueOperation() {
        if (PrinterAccess.isReady(activity)) { runPending(); return; }
        if (!PrinterAccess.granted(activity)) { fail("Se revocó el permiso de Bluetooth"); return; }
        try { enable.launch(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)); }
        catch (RuntimeException e) { fail("No se pudo activar Bluetooth"); }
    }
    private void runPending() {
        Runnable action = pending; pending = null;
        try { if (action != null && PrinterAccess.isReady(activity)) action.run(); }
        catch (RuntimeException e) { fail("No se pudo usar la impresora: " + e.getMessage()); }
    }
    private void fail(String message) { pending = null; Toast.makeText(activity, message, Toast.LENGTH_LONG).show(); }
}
