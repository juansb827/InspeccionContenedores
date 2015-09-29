package com.juans.inspeccion.Interfaz.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Service;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
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
public class BusquedaDialog extends DialogFragment {


    static int iniciadoPor;
    static Formularios.DataPass dataPass;
    ListView list;
    ArrayList<HashMap<String, Object>> mylist;
    SimpleAdapter  adapter;
    HashMap<String, Object>  map2;

    private  View view;
    private static ArrayList<FilaEnConsulta> listaFilas;
    private static String columna1;
    private static String columna2;



    public static void  setHeader(String _columna1,String _columna2)
    {
        columna1=_columna1;
        columna2=_columna2;


    }
    public static BusquedaDialog newInstance(ArrayList<FilaEnConsulta> lista,String titulo, Formularios.DataPass dp, int _iniciadoPor) {
        BusquedaDialog f = new BusquedaDialog();

        // Supply num input as an argument.
        Bundle args = new Bundle();


        args.putString("titulo", titulo);

        dataPass=dp;
        iniciadoPor=_iniciadoPor;
        listaFilas=lista;
        columna1=null;
        columna2=null;

        f.setArguments(args);

        return f;


    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);


    }

    public Dialog onCreateDialog(Bundle savedInstanceBundle)
    {


        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        String titulo= getArguments().getString("titulo");
        String columna1= getArguments().getString("columna1");
        String columna2= getArguments().getString("columna2");

        builder.setTitle(titulo);



        LayoutInflater inflater = getActivity().getLayoutInflater();
        view=inflater.inflate(R.layout.busqueda_listview_dialog, null );


        builder.setView(view);

        Dialog dialog=builder.create();


        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnShowListener(new ShowListener());

        return  dialog;
    }





    private void configurar()
    {


        if(columna1!=null)
        {
            TextView c1= (TextView) view.findViewById(R.id.txtItem);
            c1.setText(columna1);
            TextView c2= (TextView) view.findViewById(R.id.txtDescripcion);
            c2.setText(columna2);
        }
        if (listaFilas==null) {
            Toast.makeText(view.getContext(), "Cargando  espere", Toast.LENGTH_SHORT).show();

            this.dismiss();
            return;
        }


        list = (ListView) view.findViewById(R.id.listView1);

//        list_head = (ListView) view.findViewById(R.id.listView1);

        mylist = new ArrayList<HashMap<String, Object>>();



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
            map2 = new HashMap<String, Object>();

            FilaEnConsulta fila= listaFilas.get(i);
            map2.put("zero", fila);
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
                HashMap<String,Object> item= (HashMap<String, Object>) adapter.getItem(i);

                dataPass.onDataReceive((FilaEnConsulta) item.get("zero"), iniciadoPor);





                BusquedaDialog.this.dismiss();



            }
        });


        final EditText txtBusqueda= (EditText) view.findViewById(R.id.txtBusqueda);

        txtBusqueda.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {

            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {

                InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Service.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(txtBusqueda.getWindowToken(), 0);


                return false;
            }
        });
        txtBusqueda.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {




            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                if(charSequence.equals(""))
                {
                    adapter.notifyDataSetChanged();
                }
                else
                {

                    BusquedaDialog.this.adapter.getFilter().filter(charSequence);

                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

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
