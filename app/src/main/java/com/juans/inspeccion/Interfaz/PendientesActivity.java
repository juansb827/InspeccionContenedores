package com.juans.inspeccion.Interfaz;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.juans.inspeccion.Mundo.Inspeccion;
import com.juans.inspeccion.Mundo.Pendientes;

import com.juans.inspeccion.R;

import java.util.ArrayList;
import java.util.HashMap;

public class PendientesActivity extends AppCompatActivity {
    ListView listaPendientesView;
    SimpleAdapter listaPendientesAdapter;
    ArrayList<HashMap<String, String>> listaResumenes;
    private boolean draftReadError;
    private void loadDrafts() {
        try {
            Pendientes.cargarListaTurnos(this);
            listaResumenes = Pendientes.darResumenTurnos(this);
            draftReadError = false;
        } catch (Exception e) {
            draftReadError = true;
            listaResumenes = new ArrayList<>();
            new android.app.AlertDialog.Builder(this).setTitle("Borradores no disponibles")
                    .setMessage(e.getMessage()).setPositiveButton("Aceptar", null).show();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        loadDrafts();
        setAdapter();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pendientes);
        com.juans.inspeccion.ui.settings.StorageNotice.show(this,false);
        listaPendientesView= (ListView) findViewById(R.id.listaRegistrosPendientes);
        loadDrafts();
        setAdapter();
    }
     private void setAdapter()
     {

         try {


             listaPendientesAdapter = new SimpleAdapter( getApplicationContext(), listaResumenes , R.layout.pendiente_listview_row,
                     new String[]{"NTURNO", "CTIPOTURNO", "FECHA_MOSTRAR"}, new int[]{
                     R.id.lblNumTurno, R.id.lblTipoTurno, R.id.lblFechaTurno}

             );
             listaPendientesView.setAdapter(listaPendientesAdapter);
             listaPendientesView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
         } catch (Exception e) {
             e.printStackTrace();


         }

         listaPendientesView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                 Intent intent= new  Intent(PendientesActivity.this, InspeccionActivity.class);
                 Inspeccion inspeccion=Pendientes.darListaTurnos(PendientesActivity.this).get(i);
                 String tipoDoc=inspeccion.getInformacion().get(getResources().getString(R.string.CTIPDOC));
                 intent.putExtra(Pendientes.PENDIENTE, inspeccion );
                 intent.putExtra(InspeccionActivity.INDICE_TURNO, i );
                 intent.putExtra(InspeccionActivity.TIPO_DOC, tipoDoc );

                 startActivity(intent);
             }
         });
     }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.pendientes, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_borrar_pendientes) {
            if (draftReadError) return true;
            try { Pendientes.borrarTodo(this); loadDrafts(); setAdapter(); }
            catch (Exception e) { android.widget.Toast.makeText(this, e.getMessage(), android.widget.Toast.LENGTH_LONG).show(); }

            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
