package com.juans.inspeccion.Mundo;

import android.content.res.Resources;

import com.juans.inspeccion.ConnectionException;
import com.juans.inspeccion.DataBaseException;
import com.juans.inspeccion.Interfaz.MainActivity;
import com.juans.inspeccion.R;
import com.juans.inspeccion.Varios;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by juan__000 on 10/21/2014.
 */


public class DaniosManager implements Serializable {

    ArrayList<Danio> listaDanios;
    public final static String NO_APLICA="NOAPLICA";
    public final static String COSTO_TOTAL_DANIO="COSTO_TOTAL_DANIO";
    private float valorTotal;


    public DaniosManager()
    {

        listaDanios=new ArrayList<Danio>();
        valorTotal=0;
    }

    public ArrayList<Danio> getListaDanios() {
        if(listaDanios==null)
        {
            listaDanios=new ArrayList<Danio>();
        }
        return listaDanios;
    }

    public static  ArrayList<HashMap<String,String>> darListaSoloDetalles(ArrayList<Danio> listaDanios)
    {
        ArrayList<HashMap<String,String>> danios=new ArrayList<>();
        for(Danio danio:listaDanios)
        {
            danios.add(danio.getDetalles());
        }
        return danios;
    }

    public void setListaDanios(ArrayList<Danio> danios)
    {
        listaDanios=danios;
        recalcularValorTotal();

    }

    public float darValorTotal()
    {
        return valorTotal;
    }

    public void recalcularValorTotal()
    {
        String nvalor=COSTO_TOTAL_DANIO;

        for(int i=0;i<listaDanios.size();i++)
        {
            try
            {
                float valorNum;
                String valorStr=listaDanios.get(i).getDetalles().get(nvalor);
                if(valorStr.trim().isEmpty())
                {
                    valorNum=0;

                }
                else {
                    valorNum=Float.parseFloat(valorStr);
                }
                valorTotal+=valorNum;

            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

   public void actualizarDanio(int i, Danio danio)
    {

        listaDanios.set(i, danio);

    }
    public void agregarDanio(Danio danio)
    {
        if(listaDanios==null) listaDanios=new ArrayList();
        //Danio nuevoDanio=copiarDanio(danio);
      // if(copiarFotos) nuevoDanio.setAlbum(danio.getAlbum());
        listaDanios.add(danio);
        try {
            String cvalor = danio.getDetalles().get(COSTO_TOTAL_DANIO);

            float nvalor =Varios.parseFloat(cvalor);
            valorTotal += nvalor;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        renumerarDanios();

    }

    /*
    Genera un danio con los mismos detalles, las fotos no se copian
     */
    public  Danio copiarDanio( Danio original)
    {

        HashMap<String,String> copia_detalles=new HashMap<String, String>();
        Iterator ite=original.getDetalles().entrySet().iterator();

        while (ite.hasNext()) {
            Map.Entry pair = (Map.Entry) ite.next();
            String key = (String) pair.getKey();
            String value = (String) pair.getValue();
            copia_detalles.put(key, value);

        }
        Danio nuevoDanio=new Danio();
        nuevoDanio.setDetalles(copia_detalles);

        return nuevoDanio;

    }



    public Danio darDanio(int i)
    {
        return listaDanios.get(i);
    }

    public void removerDanio(int i,Resources res)
    {
        try {
            String cvalor = listaDanios.get(i).getDetalles().get(COSTO_TOTAL_DANIO);
            float nvalor ;
            if (cvalor.trim().isEmpty())
            {
                nvalor=0;
            }
            else
            {
                nvalor = Float.parseFloat(cvalor);
            }

            valorTotal -= nvalor;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        Danio danio=listaDanios.remove(i);
        try {
            danio.getAlbum().borrarAlbum();
        } catch (IOException e) {
            e.printStackTrace();
        }

        renumerarDanios();
    }

    public void renumerarDanios()
    {
        for(int i=0;i<listaDanios.size();i++)
        {
            Danio danio = listaDanios.get(i);
            danio.getDetalles().put(MainActivity.context.getResources().getString(R.string.NUM_ITEM), (i + 1) + "");


        }
    }

    public void borrarDanios()
    {
        if(listaDanios!=null)listaDanios.clear();
        valorTotal=0;
    }


    public static String borrarDaniosSentencia(Resources r, String numDoc) throws ConnectionException, DataBaseException {
        String nomtabla=r.getString(R.string.TBDETMOVPATIO);
        String[] columnas=new String[]{r.getString(R.string.NNUMDOC)};
        String[] values=new String[]{numDoc};
        String sentencia=DAO.getInstance().crearSentenciaDelete(nomtabla,columnas,values);
        return sentencia;



    }

    public static ArrayList<Danio> crearListaDanios(ArrayList<HashMap<String,String>> danios)
    {

        ArrayList<Danio> lista_danios=new ArrayList<>();
        for(HashMap<String,String> detalles:danios)
        {
            Danio danio=new Danio();
            danio.setDetalles(detalles);
            lista_danios.add(danio);
        }
        return lista_danios;
    }






    public static String validarDanio(Resources r, HashMap<String, String> danio, String usaTamano, String tamanoCntr) {
        String mensaje = null;
        String ubicacion = danio.get(r.getString(R.string.CCODUBI));
        String codigoElemento = danio.get(r.getString(R.string.CCODELE));
        String codigoDanio = danio.get(r.getString(R.string.CCODDAN));
        String codigoMetodo = danio.get(r.getString(R.string.CCODMET));
        String tipoCalculo = danio.get(r.getString(R.string.CTIPCALCULO));
        String largo = danio.get(r.getString(R.string.NLARGO));
        String ancho = danio.get(r.getString(R.string.NANCHO));

        String unidades = danio.get(r.getString(R.string.NUNIDADES));


        if (ubicacion.trim().isEmpty()) {
            mensaje = "Ingrese la ubicacion ";
        } else if (codigoElemento.trim().isEmpty()) {
            mensaje = "Ingrese el componente dañado ";
        } else if (codigoDanio.trim().isEmpty()) {
            mensaje = "Ingrese el tipo de daño ";
        } else if (codigoMetodo.trim().isEmpty()) {
            mensaje = "Ingrese el metodo de reparacion";
        } else if (unidades.trim().isEmpty()) {
            mensaje = "Ingrese la cantidad";
        } else if (tipoCalculo.equals("D")) {
            if (ancho.trim().isEmpty() || largo.trim().isEmpty()) {
                mensaje = "Ingrese las dimensiones de daño";
            }
        } else if (usaTamano.equals("1")) {
            if (tamanoCntr.trim().isEmpty()) {
                mensaje = "No se encontro el tamaño del contendor";
            } else {
                danio.put(r.getString(R.string.CMATCNTR), tamanoCntr);
            }

        }
        return mensaje;
    }


    public static boolean BuscarInfoDanio(Resources res,HashMap<String,String> danio,HashMap<String,String> infoDeCabecera,HashMap<String,String> infoContenedor,boolean no_aplica  )
    {
        boolean encontroTarifa=false;

        String codigoMetodo = danio.get(res.getString(R.string.CCODMET));
        String linea = infoDeCabecera.get(res.getString(R.string.CCTELNA));
        String usaUbicacion="";
        HashMap<String, String> busqueda=null;
        String tarifaHorasHomDeLinea;
        String grupoReparacion;


        try {

            if(!no_aplica) {
                usaUbicacion=DAO.getInstance().consulta_1_Dato(DAO.LINEA_USA_UBICACION+ "'" + linea + "'");
                danio.put(res.getString(R.string.NOBLUBICA), usaUbicacion);
                String ctam=res.getString(R.string.CTAMCNTR);
                String tamanioCntr=infoContenedor.get(ctam);

                infoDeCabecera.put(ctam,tamanioCntr);
                busqueda = Consultas.darCostosDanios(res, danio, infoDeCabecera);

                tarifaHorasHomDeLinea = DAO.getInstance().consulta_1_Dato(DAO.TARIFA_DE_LINEA + "'" + linea + "'");
                grupoReparacion = DAO.getInstance().consulta_1_Dato(DAO.GRUPO_REP_DE_METODO + "'" + codigoMetodo + "'");
                if (busqueda == null  || busqueda.size() == 0)
                {
                    encontroTarifa=false;
                }
                else
                {
                    encontroTarifa=true;
                    float costo = Float.valueOf(busqueda.get(res.getString(R.string.NCOSTO)));
                    float cantidad = Varios.parseFloat(danio.get(res.getString(R.string.NUNIDADES)));
                    float valor = costo * cantidad;

                    danio.put(res.getString(R.string.NVALOR), Float.toString(valor));
                    String numHorasHombre = busqueda.get(res.getString(R.string.NHORASHOM));
                    float numHorasHom = Varios.parseFloat(numHorasHombre);
                    danio.put(res.getString(R.string.NHORASHOM), Float.toString(numHorasHom));


                    float costoHoras = numHorasHom * Varios.parseFloat(tarifaHorasHomDeLinea) * cantidad;
                    danio.put(res.getString(R.string.NCOSHORHOM), Float.toString(costoHoras));
                    danio.put(res.getString(R.string.NTARIVA), busqueda.get(res.getString(R.string.NTARIVA)));
                    danio.put(res.getString(R.string.NCOSTO), busqueda.get(res.getString(R.string.NCOSTO)));
                    danio.put(res.getString(R.string.NTARHORHOM), tarifaHorasHomDeLinea);
                    danio.put(res.getString(R.string.CGRUPOREP), grupoReparacion);
                    danio.put(res.getString(R.string.CMONEDA), busqueda.get(res.getString(R.string.CMONEDA)));
                    danio.put(res.getString(R.string.CCODREPCNTR), busqueda.get(res.getString(R.string.CCPTOLINEA)));
                    String ivaStr=busqueda.get(res.getString(R.string.NTARIVA));
                    float iva=Varios.parseFloat(ivaStr);
                    float total_danio=valor + costoHoras;
                    float iva_danio=(total_danio)*(iva/100);
                    total_danio=total_danio+iva_danio;
                   // total_danio=Varios.floatCon2Decimales(total_danio);
                    String total_danioStr=total_danio+"";
                    danio.put(COSTO_TOTAL_DANIO, total_danioStr);



                }
            }
            if(no_aplica || busqueda==null || busqueda.size()==0)
            {



                danio.put(res.getString(R.string.NVALOR), "0");

                danio.put(res.getString(R.string.NHORASHOM), "0");



                danio.put(res.getString(R.string.NCOSHORHOM), "0");
                danio.put(res.getString(R.string.NTARIVA), "0");
                danio.put(res.getString(R.string.NCOSTO), "0");
                danio.put(res.getString(R.string.NTARHORHOM), "0");
                danio.put(res.getString(R.string.CGRUPOREP), "");
                danio.put(res.getString(R.string.CMONEDA), "");
                danio.put(res.getString(R.string.CCODREPCNTR), "");

                danio.put(COSTO_TOTAL_DANIO, "0");

            }

        }
        catch (Exception e) {
            e.printStackTrace();
            danio.put(res.getString(R.string.NVALOR), "0");

            danio.put(res.getString(R.string.NHORASHOM), "0");



            danio.put(res.getString(R.string.NCOSHORHOM), "0");
            danio.put(res.getString(R.string.NTARIVA), "0");
            danio.put(res.getString(R.string.NCOSTO), "0");
            danio.put(res.getString(R.string.NTARHORHOM), "0");
            danio.put(res.getString(R.string.CGRUPOREP), "");
            danio.put(res.getString(R.string.CMONEDA), "");
            danio.put(res.getString(R.string.CCODREPCNTR), "");

            danio.put(COSTO_TOTAL_DANIO, "0");

        }






        return encontroTarifa;
    }




}
