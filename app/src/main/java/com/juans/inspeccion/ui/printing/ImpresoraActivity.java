package com.juans.inspeccion.ui.printing;

import com.juans.inspeccion.ui.common.ActivityHelper;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.juans.inspeccion.ui.common.dialogs.ListViewDialog;
import com.juans.inspeccion.platform.printing.Comando;
import com.juans.inspeccion.ui.common.Formularios;
import com.juans.inspeccion.platform.printing.ComandosImpresora;
import com.juans.inspeccion.platform.printing.Impresora;
import com.juans.inspeccion.platform.printing.PrinterAccess;
import com.juans.inspeccion.R;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Set;

public class ImpresoraActivity extends AppCompatActivity implements Formularios.DataPass {
    private final PrinterPermissionGate printerGate = new PrinterPermissionGate(this);
    private boolean working;
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        new ActivityHelper(this);
        setContentView(R.layout.activity_impresora);
        findViewById(R.id.btnListarDispositivos).setOnClickListener(v -> mostrarDispositivos());
        findViewById(R.id.btnProbarImpresora).setOnClickListener(v -> probarImpresora(v));
        if (Impresora.getInstance().cargarDatosImpresora(this)) {
            ((EditText) findViewById(R.id.txtNombreImpresora)).setText(Impresora.getInstance().getNombreImpresora());
            ((EditText) findViewById(R.id.txtDireccionImpresora)).setText(Impresora.getInstance().getMacAdress());
        }
    }
    public void mostrarDispositivos() { if (!working) printerGate.request(this::pairedDevices); }
    public void escojerDispositivo() { mostrarDispositivos(); }
    private void pairedDevices() {
        if (!PrinterAccess.isReady(this)) return;
        if (Build.VERSION.SDK_INT >= 31 && ContextCompat.checkSelfPermission(this,
                Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) return;
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) return;
        try {
            Set<BluetoothDevice> devices = adapter.getBondedDevices();
            if (devices.isEmpty()) { message("No hay impresoras vinculadas; vincule una en Ajustes de Bluetooth"); return; }
            String[] choices = new String[devices.size()]; int i = 0;
            for (BluetoothDevice device : devices) choices[i++] = device.getName() + "\n" + device.getAddress();
            ListViewDialog.newInstance("Seleccione la impresora", choices, 0, this).show(getSupportFragmentManager(), "printer");
        } catch (SecurityException e) { message("Se revocó el permiso de Bluetooth"); }
    }
    @Override public void onDataReceive(Object value, int source) {
        String device = (String) value; int split = device.lastIndexOf('\n');
        if (split < 0) return;
        String name = device.substring(0, split), address = device.substring(split + 1);
        ((EditText) findViewById(R.id.txtNombreImpresora)).setText(name);
        ((EditText) findViewById(R.id.txtDireccionImpresora)).setText(address);
        Impresora.getInstance().setMacAdress(address);
        Impresora.getInstance().setNombreImpresora(name);
        Impresora.getInstance().guardarDatosImpresora(this);
    }
    public void conectar() { if (!working) printerGate.request(() -> operate(false)); }
    public boolean probarImpresora(View view) { if (!working) printerGate.request(() -> operate(true)); return true; }
    private void operate(boolean printTest) {
        working = true;
        Impresora.IO.execute(() -> {
            String error = null;
            try {
                if (Impresora.getInstance().conectar(getApplicationContext()) != Impresora.OK)
                    throw new java.io.IOException("No se pudo conectar; seleccione una impresora y revise Bluetooth");
                if (printTest) {
                    Bitmap bitmap;
                    try (InputStream source = getAssets().open("test.jpg")) { bitmap = BitmapFactory.decodeStream(source); }
                    if (bitmap == null) throw new java.io.IOException("Imagen de prueba no disponible");
                    try {
                        ArrayList<Comando> commands = new ArrayList<>();
                        commands.add(new Comando(ComandosImpresora.CMD, "^Q25,0,0"));
                        commands.add(new Comando(ComandosImpresora.CMD, "^L"));
                        Comando image = new Comando(ComandosImpresora.IMG, "0,0"); image.setImagen(bitmap); commands.add(image);
                        commands.add(new Comando(ComandosImpresora.CMD, "E"));
                        Impresora.getInstance().enviarComandosImpresora(commands, getApplicationContext());
                    } finally { bitmap.recycle(); }
                }
            } catch (Exception e) { error = e.getMessage(); }
            finally { if (printTest) Impresora.getInstance().desconectar(); }
            String result = error;
            runOnUiThread(() -> { working = false; if (!isFinishing() && !isDestroyed()) message(result == null ? "Operación completada" : result); });
        });
    }
    private void message(String text) { Toast.makeText(this, text, Toast.LENGTH_LONG).show(); }
    @Override public boolean onCreateOptionsMenu(Menu menu) { getMenuInflater().inflate(R.menu.menu_impresora, menu); return true; }
    @Override public boolean onOptionsItemSelected(MenuItem item) {
        return item.getItemId() == R.id.action_settings || super.onOptionsItemSelected(item);
    }
    @Override protected void onDestroy() { super.onDestroy(); Impresora.IO.execute(() -> Impresora.getInstance().desconectar()); }
}
