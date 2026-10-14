package com.juans.inspeccion.platform.printing;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.content.ContextCompat;

public final class PrinterAccess {
    public interface Gateway { boolean ready(Context context); }
    private PrinterAccess() { }
    public static String[] permissions(int sdk) {
        return sdk >= 31 ? new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN}
                : new String[0];
    }
    public static boolean granted(Context context) {
        for (String permission : permissions(Build.VERSION.SDK_INT))
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) return false;
        return true;
    }
    public static boolean isReady(Context context) {
        if (Build.VERSION.SDK_INT >= 31 &&
                (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                 ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED)) return false;
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        try { return adapter != null && adapter.isEnabled(); }
        catch (SecurityException e) { return false; }
    }
}
