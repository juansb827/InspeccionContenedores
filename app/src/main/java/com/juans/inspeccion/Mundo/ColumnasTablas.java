package com.juans.inspeccion.Mundo;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import android.util.Xml;
import android.view.View;

import com.juans.inspeccion.ConnectionException;
import com.juans.inspeccion.Interfaz.MainActivity;
import com.juans.inspeccion.R;
import com.juans.inspeccion.Varios;

import org.apache.poi.hssf.record.chart.CategorySeriesAxisRecord;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlSerializer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;


import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.w3c.dom.Document;


/**
 * Created by juan__000 on 8/16/2014.
 */
public class ColumnasTablas {

    private HashMap<String, HashMap<String, String>> tablas;

    /*  Columas de una tabla  <Nombre  de la columna, Tipo de dato>*/
//    private HashMap<String, String> TBCABMOVPATIO;
//    private HashMap<String, String> TBTURNOS;
    private static ColumnasTablas instance;
    private Context context;
    private HashMap<String, String> infoEmpresa;


    private static String FILE_INFO_EMPRESA = "infoEmpresa.hue";
    public static String FILE_LOGO_EMPRESA = "logoEmpresa.jpg";


    public HashMap<String, String> darInfoEmpresa() {
        return infoEmpresa;
    }

    public boolean isHayInfoEmpresa() {
        return infoEmpresa != null && infoEmpresa.size() >= 5 && infoEmpresa.containsKey(FILE_LOGO_EMPRESA);
    }

    public boolean cargarInfoEmpresa(Context c) throws Exception {

        boolean descargar=true;
        try {
            File file = c.getFileStreamPath(FILE_INFO_EMPRESA);

            File file2 = c.getFileStreamPath(FILE_LOGO_EMPRESA);

            if (file != null && file.exists() && file2 != null && file2.exists()) {
                FileInputStream fis = c.openFileInput(FILE_INFO_EMPRESA);
                ObjectInputStream is = new ObjectInputStream(fis);
                infoEmpresa = (HashMap<String, String>) is.readObject();
                descargar=false;
                is.close();
            }

        } catch (Exception e) {
            descargar=true;
            e.printStackTrace();
        }

              if(descargar) descargarInfoEmpresa(c);





        return isHayInfoEmpresa();
    }

    public void borrarInfoEmpresa(Context c) {
        try {
            File file = c.getFileStreamPath(FILE_INFO_EMPRESA);

            File file2 = c.getFileStreamPath(FILE_LOGO_EMPRESA);
            infoEmpresa = null;

            if (file.exists()) file.delete();
            if (file2.exists()) file2.delete();
        } catch (Exception e) {
            e.printStackTrace();

        }

    }


    /*Crear un Mapa con la info de la empresa y el directorio donde esta guardado el logo si pudo descargarlon
        * */
    private void descargarInfoEmpresa(Context c) throws Exception {

        File info = c.getFileStreamPath(FILE_INFO_EMPRESA);

        File logo = c.getFileStreamPath(FILE_LOGO_EMPRESA);

        if (info.exists()) info.delete();
        if (logo.exists()) logo.delete();
        HashMap<String, String> infoEmpresa = Consultas.darInfoEmpresa();
        if (infoEmpresa == null) return;

        Log.e("GuardoLista", "Guardo InfoEmpresa");
        String URL = infoEmpresa.get(c.getResources().getString(R.string.CURLLOGO));
        //InputStream is=c.getAssets().open("logo_centralpark.png");
        //BitmapFactory.decodeStream(is);
        Bitmap foto = Varios.downloadBitmap(URL);
        if (foto == null) {
            infoEmpresa=null;
            throw new Exception("Error bajando el Logo");
        }
        Log.e("ColumnasTablas","FotoDownlaoded w"+foto.getWidth()+"h"+foto.getHeight() );
        foto= MyCameraHelper.getResizedBitmap(foto,600,100);
        Log.e("ColumnasTablas","FotoResized w"+foto.getWidth()+"h"+foto.getHeight() );
        FileOutputStream fos_foto = c.openFileOutput(FILE_LOGO_EMPRESA, Context.MODE_APPEND);
        foto.compress(Bitmap.CompressFormat.JPEG, 90, fos_foto);
        infoEmpresa.put(FILE_LOGO_EMPRESA, logo.getPath());
        FileOutputStream fos_info = c.openFileOutput(FILE_INFO_EMPRESA, Context.MODE_APPEND);
        ObjectOutputStream os = new ObjectOutputStream(fos_info);
        os.writeObject(infoEmpresa);
        os.close();
        fos_foto.flush();
        fos_foto.close();


    }


    public void inicializarTablas(Context c) throws Exception {


        Resources res = c.getResources();
        String tablasR[] = res.getStringArray(R.array.Tablas);
        tablas = new HashMap<>();
        for (int i = 0; i < tablasR.length; i++) {
            String nombreTabla = tablasR[i];
            HashMap<String, String> tabla = cargarColumnasTabla(nombreTabla, c);
            tablas.put(nombreTabla, tabla);
        }

    }

    public boolean estanTablas(Context c)
    {
        return tablas !=null && tablas.size()==c.getResources().getStringArray(R.array.Tablas).length;

    }

    public static ColumnasTablas getInstance() {
        if (instance == null) {
            instance = new ColumnasTablas();
        }
        return instance;
    }


    public HashMap<String, String> darTabla(String nombre) {


        HashMap<String, String> tabla = tablas.get(nombre);


        if (tabla == null || tabla.size() == 0) {
            HashMap<String, String> nueva= null;
            try {
                nueva = cargarColumnasTabla(nombre, context);
            } catch (Exception e) {
                e.printStackTrace();
            }
            tablas.put(nombre, nueva);

        }

        return tabla;


    }


    public void setContext(Context c) {
        context = c;
    }


    public HashMap<String, String> cargarColumnasTabla(String nombreTabla, Context c) throws Exception {

        HashMap<String, String> colsTabla = null;
        String fileName = nombreTabla + ".xml";
        File file = c.getFileStreamPath(fileName);
        try {
            if (file.exists()) {
                FileInputStream fis = new FileInputStream(file);
                ObjectInputStream is = new ObjectInputStream(fis);
                colsTabla = (HashMap<String, String>) is.readObject();
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                file.delete();
            } catch (Exception ef) {
                e.printStackTrace();
            }
        }

        if(colsTabla==null||colsTabla.size()==0)

        {   colsTabla = DAO.getInstance().cargarHashColummas(nombreTabla);
            guardarColumnasTabla(nombreTabla, colsTabla, c);
        }

        if(colsTabla==null||colsTabla.size()==0)
            throw new Exception();
        return colsTabla ;

//                colsTabla=new HashMap<>();
//
//                DocumentBuilderFactory dbf;
//                DocumentBuilder db;
//                NodeList items = null;
//                Document dom;
//
//                dbf = DocumentBuilderFactory.newInstance();
//                db = dbf.newDocumentBuilder();
//                dom = db.parse(file);
//                Element docele = dom.getDocumentElement();
//
//
//                items = docele.getElementsByTagName("Column");
//
//
//                for (int i = 0; i < items.getLength(); i++) {
//                    //get the employee element
//                    Element el = (Element) items.item(i);
//
//                    String name = getTextValue(el, "Name");
//                    String type=getTextValue(el, "Type");
//
//                    colsTabla.put(name,type);
//                }




}


    private String getTextValue(Element ele, String tagName) {
        String textVal = null;
        NodeList nl = ele.getElementsByTagName(tagName);
        if (nl != null && nl.getLength() > 0) {
            Element el = (Element) nl.item(0);
            textVal = el.getFirstChild().getNodeValue();
        }

        return textVal;
    }


    public void guardarColumnasTabla(String nombreTabla, HashMap<String, String> columnas, Context c) {


        try {


            String fileName = nombreTabla + ".xml";
            File file = c.getFileStreamPath(fileName);
            if (file.exists()) {
                file.delete();
            }

            FileOutputStream fos;
            fos = c.openFileOutput(fileName, Context.MODE_APPEND);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(columnas);
            os.close();
//            XmlSerializer xs = Xml.newSerializer();
//            xs.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
//            xs.setOutput(fos, "UTF-8");
//            xs.startDocument(null, null);
//            xs.startTag("", "Table");
//            xs.attribute("", "Name", nombreTabla);
//
//            Iterator<String> iterador = columnas.keySet().iterator();
//
//            while (iterador.hasNext()) {
//                //  Log.e("Agrengado", ""+xs.hashCode());
//                String nombreColumna = iterador.next();
//                String tipo = columnas.get(nombreColumna);
//                xs.startTag("", "Column");
//                xs.startTag("", "Name");
//                xs.text(nombreColumna);
//                xs.endTag("", "Name");
//
//                xs.startTag("", "Type");
//                xs.text(tipo);
//                xs.endTag("", "Type");
//
//                xs.endTag("", "Column");
//
//
//            }
//
//            xs.endTag("", "Table");
//            xs.endDocument();
//            fos.close();


        } catch (Exception e) {
            e.printStackTrace();

        }
    }


    public void borrarArchivosXML() {


        Resources res = context.getResources();
        String tablasR[] = res.getStringArray(R.array.Tablas);

        for (int i = 0; i < tablasR.length; i++) {
            String nombreTabla = tablasR[i];

            String fileName = nombreTabla + ".xml";
            File file = context.getFileStreamPath(fileName);
            if (file.exists()) {
                file.delete();
            }

        }
        tablas=null;
    }
}
