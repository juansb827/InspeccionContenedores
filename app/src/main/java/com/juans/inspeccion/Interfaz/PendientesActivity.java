package com.juans.inspeccion.Interfaz;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
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


    @Override
    protected void onResume() {
        super.onResume();
        listaResumenes= Pendientes.darResumenTurnos(PendientesActivity.this
        );
        setAdapter();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pendientes);
        listaPendientesView= (ListView) findViewById(R.id.listaRegistrosPendientes);
        listaResumenes = Pendientes.darResumenTurnos(PendientesActivity.this);
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
            Pendientes.borrarTodo();
            listaPendientesAdapter.notifyDataSetChanged();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
