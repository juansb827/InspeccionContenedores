package com.juans.inspeccion.Interfaz.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.juans.inspeccion.Interfaz.CustomAdapters.GridViewAdapter;
import com.juans.inspeccion.Interfaz.CustomAdapters.ImageItem;
import com.juans.inspeccion.Mundo.Album;
import com.juans.inspeccion.Mundo.MyCameraHelper;
import com.juans.inspeccion.R;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Juan on 04/05/2015.
 */
public class PhotoGridDialog extends DialogFragment {
    private GridView gridView;
    private GridViewAdapter gridAdapter;
    private static Album album;
    private int fotoSeleccionada=-1;
    Button btnExpandir;
    Button btnBorrar;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("img", fotoSeleccionada);
    }



    View view;
    public static PhotoGridDialog newInstance(Album _fotos)
    {
        album =_fotos;
        return new PhotoGridDialog();
    }




    public Dialog onCreateDialog(Bundle savedInstanceBundle)
    {
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
         view=inflater.inflate(R.layout.gallery, null );
        builder.setView(view);
        if(savedInstanceBundle!=null)
        {
            fotoSeleccionada=savedInstanceBundle.getInt("img");
        }

        configurar();

        Dialog dialog=builder.create();
        return  dialog;
    }
    private void mostrarBotones(){

        btnBorrar.setVisibility(View.VISIBLE);
        btnExpandir.setVisibility(View.VISIBLE);
    };
    private void esconderBotones(){
        btnBorrar.setVisibility(View.GONE);
        btnExpandir.setVisibility(View.GONE);

    };

    private void configurar()
    {
        btnExpandir= (Button) view.findViewById(R.id.btnExpandir);
        btnBorrar= (Button) view.findViewById(R.id.btnBorrar);
        if(fotoSeleccionada==-1) esconderBotones();
        final ImageView imgV= (ImageView) view.findViewById(R.id.fullScreenPhoto);
        imgV.setVisibility(View.GONE);

        gridView = (GridView)view.findViewById(R.id.gridView);

        try {
            gridAdapter = new GridViewAdapter(getActivity(), R.layout.grid_item_layout, getData());
        } catch (Exception e) {
            e.printStackTrace();
        }
        gridView.setAdapter(gridAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                fotoSeleccionada= position;
                gridAdapter.setSelectedIndex(position);
                mostrarBotones();
            }
        });


        btnExpandir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                gridView.setVisibility(View.GONE);
                imgV.setVisibility(View.VISIBLE);
               // Drawable d = Drawable.createFromPath(album.getFotos().get(fotoSeleccionada));
                //imgV.setImageDrawable(d);

                try {
                    String pathFoto=album.getFotos().get(fotoSeleccionada);
                    Bitmap bitmap = BitmapFactory.decodeFile(pathFoto);
                    imgV.setImageBitmap(bitmap);
                } catch (Exception e) {

                    e.printStackTrace();
                }



            }
        });

        btnBorrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    album.eliminarFoto(fotoSeleccionada);
                    Toast.makeText(getActivity(),"Foto borrada",Toast.LENGTH_SHORT).show();

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(),e.getMessage(),Toast.LENGTH_SHORT).show();
                }
                dismiss();
            }
        });




    }





    private ArrayList<ImageItem> getData() throws Exception {
        final ArrayList<ImageItem> imageItems = new ArrayList<>();

        for (int i = 0; i < album.getFotos().size(); i++) {



            Bitmap bitmap = MyCameraHelper.darMiniatura(album.getFotos().get(i),150,150,false);
            imageItems.add(new ImageItem(bitmap, "Image#" + i, album.getFotos().get(i)));
        }
        return imageItems;
    }


}
