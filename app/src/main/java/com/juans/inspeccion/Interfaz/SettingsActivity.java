package com.juans.inspeccion.Interfaz;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.juans.inspeccion.CustomView.CustomView;
import com.juans.inspeccion.CustomView.MyEditText;
import com.juans.inspeccion.Interfaz.Dialogs.EditTextDialog;
import com.juans.inspeccion.Mundo.DAO;
import com.juans.inspeccion.Mundo.ColumnasTablas;
import com.juans.inspeccion.Mundo.FilaEnConsulta;
import com.juans.inspeccion.Mundo.Formularios;
import com.juans.inspeccion.Mundo.Listas;
import com.juans.inspeccion.R;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity implements View.OnFocusChangeListener, Formularios.DataPass {


    private boolean connected=false;
    ArrayList<CustomView> listaCampos;




    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new ActivityHelper(this);
        setContentView(R.layout.activity_settings);
         setTitle("Configuracion");

        cargarDatosConexion();
        CargarListaCampos cargarListaCampos=new CargarListaCampos();
        cargarListaCampos.execute();

        final Button btnSaveConnection= (Button) findViewById(R.id.btnSaveConnection);
        Button btnTestConnection=(Button) findViewById(R.id.btnTestConnection);

        btnSaveConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });


        btnTestConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                 HashMap<String,String> campos=new HashMap<String, String>();

                EditText txtServer=(EditText) findViewById(R.id.txtServerAdress);
                campos.put( DAO.SERVER_ADDRESS, txtServer.getText().toString() );

                EditText txtDb= (EditText)findViewById(R.id.txtDataBase);
                campos.put( DAO.DATA_BASE, txtDb.getText().toString() );

                EditText txtUser= (EditText)findViewById(R.id.txtUser);
                campos.put(DAO.USER, txtUser.getText().toString());

                EditText txtPassword=(EditText) findViewById(R.id.txtPassword);
                campos.put(DAO.PASSWORD, txtPassword.getText().toString());


                if ( !comprobarSiFormularioVacio(campos) )
                {
                    Toast.makeText( getApplicationContext(), R.string.msg_complete_form , Toast.LENGTH_SHORT ).show();
                }
                else
                {
                    EditText txtInstance=(EditText)findViewById(R.id.txtServerInstance);
                    campos.put(DAO.SERVER_INSTANCE, txtInstance.getText().toString());

                    probarConexion(campos);
                    btnSaveConnection.setEnabled(true);
                }


            }
        });


        btnSaveConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                OnClickguardarConexion();
            }
        });




    }


    public void cargarDatosConexion()
    {

        DAO dao= DAO.getInstance();
        HashMap<String,String> parametros=dao.cargarDatosConexion(getApplicationContext());
        if ( parametros!=null)
        {
            EditText serverAdress = (EditText) findViewById(R.id.txtServerAdress);
            serverAdress.setText(parametros.get(DAO.SERVER_ADDRESS));

            EditText db_name = (EditText) findViewById(R.id.txtDataBase);
            db_name.setText(parametros.get(DAO.DATA_BASE));

            EditText user = (EditText) findViewById(R.id.txtUser);
            user.setText(parametros.get(DAO.USER));

            EditText password = (EditText) findViewById(R.id.txtPassword);
            password.setText(parametros.get(DAO.PASSWORD));

            EditText serverInstance= (EditText) findViewById(R.id.txtServerInstance);
            serverInstance.setText(parametros.get(DAO.SERVER_INSTANCE));
        }


    }

    public void OnClickguardarConexion()
    {

        if(!connected)
        {
            AlertDialog.Builder builder=new AlertDialog.Builder(SettingsActivity.this);
            builder.setMessage(R.string.save_connection_anyway).setTitle(R.string.save);

            builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    guardarConexion();


                }
            });

            builder.setNegativeButton(R.string.no,new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });

            AlertDialog saveDialog=builder.create();
            saveDialog.show();


        }
        else
        {
            guardarConexion();
        }


    }


    public void guardarConexion()
    {
        DAO.getInstance().guardarDatosConexion(getApplicationContext());
        Toast.makeText(getApplicationContext(),"Configuracion guardada", Toast.LENGTH_SHORT).show();
    }
    public void probarConexion(HashMap<String,String> params)
    {
        DAO.getInstance().inicializarDatos( params);
        AsyncTask probar=new AsyncTask() {


            @Override
            protected Object doInBackground(Object[] objects) {



                try {


                    DAO.getInstance().probarConexion();









                } catch (Exception e) {
                    e.printStackTrace();
                    return e.getMessage();

                }
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                final String msg= (String) o;
                if(o==null)
                {
                    Toast.makeText(getApplicationContext(), "Conexión Exitosa", Toast.LENGTH_SHORT).show();
                    connected=true;
                }
                else {
                    // cuadro de dialogo de error
                    AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                    builder.setMessage(R.string.msg_connection_error).setTitle(R.string.error);

                    //Opcion Mostrar error

                    builder.setPositiveButton(R.string.show_error, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            // cuadro de dialogo del  mensaje
                            AlertDialog.Builder builder2 = new AlertDialog.Builder(SettingsActivity.this);
                            builder2.setMessage((String)msg).setTitle(R.string.error);
                            builder2.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            });

                            AlertDialog sql_msg = builder2.create();
                            sql_msg.show();


                        }
                    });

                    //cancelar

                    builder.setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });

                    AlertDialog alert=builder.create();
                    alert.show();
                }

            }

        };
        try {
            probar.execute();

        }  catch (Exception e) {
            Toast.makeText(this,"Tiempo de espera excedido", Toast.LENGTH_LONG );
        }


    }
    //Devuelve Falso si algun campo esta vacio
    public boolean comprobarSiFormularioVacio(HashMap<String,String> campos)
    {

        Iterator ite=campos.entrySet().iterator();
        while(ite.hasNext())
        {
            Map.Entry pair= (Map.Entry) ite.next();
            if(TextUtils.isEmpty((String) pair.getValue()))
            {
            return false;

        }

        }




        return true;
    }

    public void borrarInfoEmpresa(View v)
    {
        ColumnasTablas.getInstance().borrarInfoEmpresa(getApplicationContext());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.settings, menu);

        return true;
    }


    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        Log.e("Config","onSavedinstance");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void borrarTablasXML(View v)

    {
        ColumnasTablas.getInstance().setContext(this);
        ColumnasTablas.getInstance().borrarArchivosXML();
    }

    public void configurarImpresora(View v)
    {
        Intent intent= new  Intent(this, ImpresoraActivity.class);
        startActivity(intent);

    }
    public void actualizarListas(View v)

    {
        Listas.borrarListas(SettingsActivity.this);

    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if(hasFocus) {
            v.clearFocus();
            switch (v.getId())
            {
                default:

                     if (v instanceof MyEditText) {


                        EditTextDialog.newInstance(((MyEditText) v).getInputType(), v.getId(), ((MyEditText) v).getTexto(), this).show(getSupportFragmentManager(), "input");

                    }

                    break;

            }
    }






}

    @Override
    public void onDataReceive(Object dato, int autor) {

        EditText temp=(EditText ) findViewById(autor);
        if(dato!=null )
        {temp.setText(((FilaEnConsulta)dato).getDato(0));
        }
    }






    private class CargarListaCampos extends AsyncTask<Void,Void,Void>
    {


        @Override
        protected void onPreExecute() {
            listaCampos=new ArrayList<CustomView>();




        }



        @Override
        protected Void doInBackground(Void... voids) {
            ViewGroup linearLayour= (ViewGroup) findViewById(R.id.linear_layour_settings);
            Formularios.recorrerLinearLayour(linearLayour, listaCampos);
            Formularios.asignarInputDialog(listaCampos, SettingsActivity.this);
            return null;
        }
    }
}