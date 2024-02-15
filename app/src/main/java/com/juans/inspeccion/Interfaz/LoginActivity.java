package com.juans.inspeccion.Interfaz;

import android.content.Intent;
import android.hardware.usb.UsbInterface;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.juans.inspeccion.ConnectionException;
import com.juans.inspeccion.DataBaseException;
import com.juans.inspeccion.Mundo.DAO;
import com.juans.inspeccion.Mundo.Data.UsuarioDataSource;
import com.juans.inspeccion.Mundo.Usuario;
import com.juans.inspeccion.R;
import com.newrelic.agent.android.NewRelic;
import java.lang.reflect.Field;

public class LoginActivity extends AppCompatActivity {



    private static boolean logeado;
    private static Usuario inspector;

    TextView txtError;
    ImageView imgError;
    EditText txtNumeroCedula;
    Button btnLogear;


    public static String darCodigoInspector()
    {
        String idIn = String.format("%02d", inspector.getId());
        return idIn;
    }

    public static Usuario getInspector()
    {
        return inspector;
    }

    public static String darNombreInspector() {
        return inspector.getNombre();
    }
    public static boolean estaLogeado()    { return logeado;   }




    @Override
    protected void onCreate(Bundle savedInstanceState) { new ActivityHelper(this);
        /**
        NewRelic.withApplicationToken(
                "AAb11447a860836440baa49dff086d5f254b1b4426"
        ).start(this.getApplication());
         */

        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if(menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        cargarCampos();




    }

    @Override
    protected void onResume() {
        super.onResume();
        showErrorViews(DAO.getInstance().cargarDatosConexion(this)==null);
    }

    private void cargarCampos()
    {
        txtError= (TextView) findViewById(R.id.labelErrorLogin);
        imgError= (ImageView) findViewById(R.id.imgErrorLogin);
        txtNumeroCedula= (EditText) findViewById(R.id.txtCedula);
        btnLogear= (Button) findViewById(R.id.btnLogearse);

        txtNumeroCedula.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {

                if(keyEvent!=null && keyEvent.getAction()!=keyEvent.getAction()) return false;
                InputMethodManager imm = (InputMethodManager) getSystemService(
                        INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                logear(null);
                return true;
            }
        });




    }

    public void logear(View v)
    {

        InputMethodManager imm = (InputMethodManager) getSystemService(
                INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

        String numeroCedula=txtNumeroCedula.getText().toString();
        if(numeroCedula.matches("[0-9]+"))
        {
            Logear loger=new Logear();
            loger.execute(numeroCedula);
        }
        else
        {
            Toast.makeText(this,"Ingrese un codigo valido", Toast.LENGTH_SHORT).show();
        }






    }

    private void showErrorViews(boolean mostrar) {
        int visibilidad= View.INVISIBLE;
        boolean habilitado=true;
        if (mostrar)
        {
            visibilidad=View.VISIBLE;
            habilitado=false;
        }

        txtError.setVisibility(visibilidad);
        imgError.setVisibility(visibilidad);
        btnLogear.setEnabled(habilitado);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent= new  Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_help)
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }




    private class Logear extends AsyncTask<String,Void,Boolean>
    {



        Usuario inspector;
        @Override
        protected void onPreExecute() {
            btnLogear.setEnabled(false);
        }

        @Override
        protected Boolean doInBackground(String ... codigo) {
            int cod= Integer.parseInt(codigo[0]);
            try {
                inspector= UsuarioDataSource.findUserByid( cod,getResources());
            } catch (ConnectionException e) {

                e.printStackTrace();
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean aVoid) {
            btnLogear.setEnabled(true);
            if(aVoid==false)
            {
                Toast.makeText(getApplicationContext(), "Error en conexion con base de datos",Toast.LENGTH_SHORT).show();

                return;
            }
            if (inspector==null)
            {
                Toast.makeText(getApplicationContext(), "Inspector no encontrado",Toast.LENGTH_SHORT).show();
            }
            else if( !inspector.isHabilitado())
            {
                Toast.makeText(getApplicationContext(), "El inspector se encuentra deshabilitado",Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(getApplicationContext(),"Bienvenido "+inspector.getNombre(), Toast.LENGTH_LONG).show();

                LoginActivity.inspector=inspector;
                logeado=true;
                Intent intent= new Intent(LoginActivity.this,MainActivity.class);
                startActivity(intent);



            }
        }
    }
}
