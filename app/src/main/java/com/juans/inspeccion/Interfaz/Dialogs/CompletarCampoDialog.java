package com.juans.inspeccion.Interfaz.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.juans.inspeccion.Mundo.FilaEnConsulta;
import com.juans.inspeccion.Mundo.Formularios;
import com.juans.inspeccion.R;
import com.juans.inspeccion.Varios;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by juan__000 on 9/10/2014.
 */
public class CompletarCampoDialog extends DialogFragment {


    static int iniciadoPor;
    static Formularios.DataPass dataPass;
    ListView list;
    ArrayList<HashMap<String, String>> mylist, mylist_title;
    ListAdapter  adapter;
    HashMap<String, String>  map2;

    private  View view;
    private static ArrayList<FilaEnConsulta> listaFilas;
    private static boolean closeParentOnDismiss;


    @Override
    public void onSaveInstanceState(Bundle outState) {
    };

    public  void  setCloseParentOnDismiss(boolean close)
    {
        closeParentOnDismiss=close;
    }

    public static CompletarCampoDialog newInstance(ArrayList<FilaEnConsulta> lista,String titulo, Formularios.DataPass dp, int _iniciadoPor) {
        CompletarCampoDialog f = new CompletarCampoDialog();

        // Supply num input as an argument.
        Bundle args = new Bundle();


        args.putString("titulo", titulo);

        dataPass=dp;
        iniciadoPor=_iniciadoPor;
        listaFilas=lista;

        f.setArguments(args);

        return f;


    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if(closeParentOnDismiss && getActivity()!=null)       {
            closeParentOnDismiss=false;
            getActivity().finish();
        }

    }

    public Dialog onCreateDialog(Bundle savedInstanceBundle)
    {


        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        String titulo= getArguments().getString("titulo");
        builder.setTitle(titulo);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        view=inflater.inflate(R.layout.doublecolumn_listview_dialog, null );
        builder.setView(view);

        Dialog dialog=builder.create();

        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnShowListener(new ShowListener());

        return  dialog;
    }





    private void configurar()
    {

            if (listaFilas==null) {
                Toast.makeText(view.getContext(), "Cargando  espere", Toast.LENGTH_SHORT).show();

                this.dismiss();
                return;
            }


            list = (ListView) view.findViewById(R.id.listView1);

//        list_head = (ListView) view.findViewById(R.id.listView1);

        mylist = new ArrayList<HashMap<String, String>>();
        mylist_title = new ArrayList<HashMap<String, String>>();


        /**********Display the headings POR SI ACASO************/


//        map1 = new HashMap<String, String>();


//        map1.put("one", textoDato);
//        map1.put("two",textoDetalle);
//        mylist_title.add(map1);
//
//
//
//        try {
//
//            adapter_title = new SimpleAdapter(view.getContext(), mylist_title, R.layout.double_column_listview_content,
//                    new String[] { "one", "two" }, new int[] {
//                    R.id.columna1, R.id.columna2});
//            list_head.setAdapter(adapter_title);
//
//
//
//        } catch (Exception e) {
//

//        }



        /**********Display the contents************/

        for (int i = 0; i < listaFilas.size(); i++) {
            map2 = new HashMap<String, String>();

            FilaEnConsulta fila= listaFilas.get(i);

            map2.put("one", fila.getDato(0));
            map2.put("two", fila.getDato(1));
            mylist.add(map2);
        }


        try {

            adapter = new SimpleAdapter(view.getContext(), mylist, R.layout.double_column_listview_content,
                    new String[] { "one", "two" }, new int[] {
                     R.id.columna1, R.id.columna2 });
            list.setAdapter(adapter);
            list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        } catch (Exception e) {


        }


        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                 dataPass.onDataReceive(listaFilas.get(i), iniciadoPor);



                CompletarCampoDialog.this.dismiss();



            }
        });




        /********************************************************/

    }






    class ShowListener implements DialogInterface.OnShowListener
    {


        public void onShow(DialogInterface dialogInterface) {

            configurar();

        }


    }


}
