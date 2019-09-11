package com.juans.inspeccion.Interfaz;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.godex.Godex;
import com.juans.inspeccion.Interfaz.Dialogs.ListViewDialog;
import com.juans.inspeccion.Mundo.FilaEnConsulta;
import com.juans.inspeccion.Mundo.Formularios;
import com.juans.inspeccion.Mundo.Recibos.Impresora;
import com.juans.inspeccion.Mundo.Recibos.RecibosTest;
import com.juans.inspeccion.R;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

public class ImpresoraActivity extends AppCompatActivity implements Formularios.DataPass{
    int REQUEST_ENABLE_BT=1;

    int pudoConectar;
    Button mostrarDispositivos,probar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        new ActivityHelper(this);
        setContentView(R.layout.activity_impresora);
        mostrarDispositivos= (Button) findViewById(R.id.btnListarDispositivos);
        probar= (Button) findViewById(R.id.btnProbarImpresora);
        mostrarDispositivos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarDispositivos();
            }
        });

        probar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                probarImpresora(null);
            }
        });
        EditText nombre= (EditText) findViewById(R.id.txtNombreImpresora);
        EditText mac= (EditText) findViewById(R.id.txtDireccionImpresora);
        boolean hayConfig=Impresora.getInstance().cargarDatosImpresora(getBaseContext());
        if(hayConfig)
        {   String _nombre=Impresora.getInstance().getNombreImpresora();
            nombre.setText(_nombre);

            String _adress=Impresora.getInstance().getMacAdress();
            mac.setText(_adress);
        }


    }










    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode==RESULT_OK)
        {

            escojerDispositivo();


        }


    }

    public void mostrarDispositivos()
    {
        BluetoothAdapter bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter==null)
        {
            //Device does not support bluetooth
        }
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }
        else{
            escojerDispositivo();
        }
    }

    public void escojerDispositivo()
    {
        final Set<BluetoothDevice> pairedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();

// If there are paired devices
        if (pairedDevices.size() > 0) {

            // Loop through paired devices
            String[] listaDispositivos=new String[pairedDevices.size()];
            int i=0;
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView

                listaDispositivos[i]=device.getName() + "\n" + device.getAddress();
                i++;



            }
            ListViewDialog dialogo=ListViewDialog.newInstance("Seleccione la impresora",listaDispositivos,0, this);
            dialogo.show(getSupportFragmentManager(), "Hue");
        }

    }





    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_impresora, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    @Override
    public void onDataReceive(Object lista, int autor) {

        EditText nombre= (EditText) findViewById(R.id.txtNombreImpresora);
        EditText mac= (EditText) findViewById(R.id.txtDireccionImpresora);
        String[] dispositivo=((String)lista).split("\n");
        nombre.setText(dispositivo[0]);
        mac.setText(dispositivo[1]);
        Impresora.getInstance().setMacAdress(dispositivo[1]);
        Impresora.getInstance().setNombreImpresora(dispositivo[0]);
        Impresora.getInstance().guardarDatosImpresora(getApplicationContext());


    }

    public void conectar()
    {
        pudoConectar= Impresora.getInstance().conectar();
        if(pudoConectar==Impresora.ERROR_AL_CONECTAR)
            Toast.makeText(getApplicationContext(), "Bluetooth Connect fail", Toast.LENGTH_SHORT).show();
        else if(pudoConectar==Impresora.OK)
        {
            Toast.makeText(getApplicationContext(), "Bluetooth Connected", Toast.LENGTH_SHORT).show();

        }
    }


    public boolean probarImpresora(View v)
    {
            String macAdress=Impresora.getInstance().getMacAdress();
        if(macAdress==null)
        {
            Toast.makeText(getApplicationContext(), "Seleccione una impresora primero", Toast.LENGTH_SHORT).show();
            return false;
        }

        else {
            int i=0;
            if(!Impresora.getInstance().conectado()) {
                 i = Impresora.getInstance().conectar();
            }
            //RecibosTest.Test(getResources());


            try {
                final InputStream bitmap = getAssets().open("test.jpg");
                if(i==0) {
                    Thread thead = new Thread() {
                        @Override
                        public void run() {
                            Bitmap myBitmap = BitmapFactory.decodeStream(bitmap);
                            Godex.sendCommand("^Q25,0,0");
                            Godex.sendCommand("^L");
                            Godex.putimage(0, 0, myBitmap);
                            Godex.sendCommand("E");


                        }
                    };
                    thead.start();
                }

                } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }









        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Impresora.getInstance().desconectar();
    }
}
