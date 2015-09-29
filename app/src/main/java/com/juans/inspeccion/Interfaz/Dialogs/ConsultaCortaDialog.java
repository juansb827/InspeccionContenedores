package com.juans.inspeccion.Interfaz.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.juans.inspeccion.Mundo.Formularios;
import com.juans.inspeccion.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Juan on 11/03/2015.
 */
public class ConsultaCortaDialog extends DialogFragment {

    static ArrayList<HashMap<String,String>> cursores;
    static String titulo;
    static int iniciadoPor;
    static int viewId;
    static String[] columns;
    static int adapterLayoutId;
    static int[] adapterColumns_id;
    static Formularios.DataPass dataPass;

    View view;
    public static ConsultaCortaDialog newInstance(String _titulo, ArrayList<HashMap<String,String>> _cursores, int _viewId,int _adaptaterLayourId,String []_columns,int[] adapterColumnsId)
    {
        titulo=_titulo;
        cursores=_cursores;
        iniciadoPor=-1;
        dataPass=null;
        viewId=_viewId;
        columns=_columns;
        adapterLayoutId=_adaptaterLayourId;
        adapterColumns_id=adapterColumnsId;



        return new ConsultaCortaDialog();
    }

    public static void setIniciadoPor(Formularios.DataPass _dataPass,int autor)
    {
        iniciadoPor=autor;
        dataPass=_dataPass;
    }



    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        LayoutInflater inflater=getActivity().getLayoutInflater();
        view=inflater.inflate(viewId,null);
        configurar();
        builder.setView(view);
        builder.setTitle(titulo);
        return builder.create();

    }

    public void configurar()
    {
        ListView list = (ListView) view.findViewById(R.id.listView1);
        ArrayList<HashMap<String, String>> myList=new ArrayList<>();
        //itera sobre las columnas
        for(int i=0;i<cursores.size();i++)
        {
            Iterator ite=cursores.get(i).entrySet().iterator();

            HashMap<String,String> map=new HashMap<String,String>();
            //itera sobre las filas
            while(ite.hasNext())
            {
                Map.Entry entry= (Map.Entry) ite.next();
                map.put((String) entry.getKey(), (String) entry.getValue());

            }
            myList.add(map);
        }

        try {
            Resources res=getActivity().getResources();


           SimpleAdapter adapter = new SimpleAdapter(getActivity().getBaseContext(), myList, adapterLayoutId,
                    columns
                    , adapterColumns_id);
            list.setAdapter(adapter);
            list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (dataPass!=null)
                    {
                        HashMap<String,String> fila= (HashMap<String, String>) parent.getAdapter().getItem(position);
                        dataPass.onDataReceive(fila,iniciadoPor);
                    }
                    dismiss();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();

        }
    }




}
