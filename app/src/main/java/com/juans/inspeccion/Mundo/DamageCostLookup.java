package com.juans.inspeccion.Mundo;

import android.content.res.Resources;
import com.juans.inspeccion.ConnectionException;
import com.juans.inspeccion.R;
import com.juans.inspeccion.Varios;
import java.util.HashMap;
import static com.juans.inspeccion.Mundo.DaniosManager.COSTO_TOTAL_DANIO;

/** Existing tariff calculation with only its remote reads supplied by a caller. */
public final class DamageCostLookup {
    private DamageCostLookup() {}
    public interface Source {
        String single(String query) throws ConnectionException;
        HashMap<String,String> costs(Resources res, HashMap<String,String> damage,
                                    HashMap<String,String> header) throws ConnectionException;
    }
    public static boolean calculate(Resources res, HashMap<String,String> danio,
            HashMap<String,String> infoDeCabecera, HashMap<String,String> infoContenedor, boolean no_aplica) {
        return calculate(res, danio, infoDeCabecera, infoContenedor, no_aplica, new Source() {
            @Override public String single(String query) throws ConnectionException {
                return DAO.getInstance().consulta_1_Dato(query);
            }
            @Override public HashMap<String,String> costs(Resources r, HashMap<String,String> d,
                    HashMap<String,String> h) throws ConnectionException {
                return Consultas.darCostosDanios(r, d, h);
            }
        });
    }
    public static boolean calculate(Resources res, HashMap<String,String> danio,
            HashMap<String,String> infoDeCabecera, HashMap<String,String> infoContenedor,
            boolean no_aplica, Source source)
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
                usaUbicacion=source.single(DAO.LINEA_USA_UBICACION+ "'" + linea + "'");
                danio.put(res.getString(R.string.NOBLUBICA), usaUbicacion);
                String ctam=res.getString(R.string.CTAMCNTR);
                String tamanioCntr=infoContenedor.get(ctam);

                infoDeCabecera.put(ctam,tamanioCntr);
                busqueda = source.costs(res, danio, infoDeCabecera);

                tarifaHorasHomDeLinea = source.single(DAO.TARIFA_DE_LINEA + "'" + linea + "'");
                grupoReparacion = source.single(DAO.GRUPO_REP_DE_METODO + "'" + codigoMetodo + "'");
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
