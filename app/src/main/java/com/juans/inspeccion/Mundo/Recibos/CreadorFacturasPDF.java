package com.juans.inspeccion.Mundo.Recibos;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.juans.inspeccion.Mundo.Consultas;
import com.juans.inspeccion.Mundo.DaniosManager;

import com.juans.inspeccion.Mundo.Inspeccion;
import com.juans.inspeccion.R;


public class CreadorFacturasPDF {
    private static String FILE = "FirstPdf.pdf";
    private static Font catFont = new Font(Font.FontFamily.TIMES_ROMAN, 18,
            Font.BOLD);
    private static Font redFont = new Font(Font.FontFamily.TIMES_ROMAN, 12,
            Font.NORMAL, BaseColor.RED);
    private static Font subFont = new Font(Font.FontFamily.TIMES_ROMAN, 16,
            Font.BOLD);
    private static Font smallBold = new Font(Font.FontFamily.TIMES_ROMAN, 12,
            Font.BOLD);
    private static Font smallCourier = new Font(Font.FontFamily.COURIER, 12,
            Font.NORMAL);
    private static Font smallBoldCourier = new Font(Font.FontFamily.COURIER, 12,
            Font.BOLD);

    public static File generarFactura(Inspeccion inspeccion,String[] fechaAlGenerar,Resources resources, File file) {




        try {
            FileOutputStream fos;

            fos = new FileOutputStream(file);
//                float Width=800;
//                float Height=1600;
//                Rectangle pageSize=new Rectangle(Width,Height);

            Document document = new Document();
            document.setMargins(125, 125, 36, 36);
            PdfWriter writer = PdfWriter.getInstance(document, fos);
            document.open();


            addMetaData(document);
            //agregarContenido(document,turno,fechaAlGenerar,resources);


            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    // iText allows to add metadata to the PDF which can be viewed in your Adobe
    // Reader
    // under File -> Properties
    private static void addMetaData(Document document) {
        document.addTitle("Factura");
        document.addSubject("Using iText");
        document.addKeywords("Java, PDF, iText");
        document.addAuthor("Focus");
        document.addCreator("Focus");
    }

    private static void agregarContenido(Document document,Inspeccion turno,String[] fechaGen,Resources res)
            throws DocumentException {

        Paragraph preface = new Paragraph();
        preface.setAlignment(Element.ALIGN_CENTER);
        preface.setLeading(0, 2f);

        Image img = buscarLogo();

        if (img != null) {
            img.setAlignment(Element.ALIGN_CENTER);
            preface.add(img);
        }


        Paragraph nombreEmpresa = new Paragraph("INVERSIONES CENTRAL PARK LTDA", smallCourier);
        preface.add(nombreEmpresa);

        preface.add(new Paragraph("EQUIPMENT INTERCHANGE RECEIPT", smallCourier));

        String[] fecha=null;
        int hora1= Integer.parseInt(fecha[Consultas.FECHA_HORA]);
        int minuto1=Integer.parseInt(fecha[Consultas.FECHA_MINUTOS]);
        String fecha1=fecha[Consultas.FECHA_DIA]+"-"+fecha[Consultas.FECHA_MES]+"-"+fecha[Consultas.FECHA_ANIO]+"     "           //fecha al completar el turno
                     +String.format("%02d", hora1)+":"+String.format("%02d", minuto1);
        int hora2= Integer.parseInt(fechaGen[Consultas.FECHA_HORA]);
        int minuto2=Integer.parseInt(fechaGen[Consultas.FECHA_MINUTOS]);

        String fecha2=fechaGen[Consultas.FECHA_DIA]+"-"+fechaGen[Consultas.FECHA_MES]+"-"+fechaGen[Consultas.FECHA_ANIO]+"     " //fecha al generar el pdf
                     +String.format("%02d", hora2)+":"+String.format("%02d", minuto2);


        preface.add(new Paragraph( fecha1, smallCourier));
        HashMap<String,String> cabecera=null;
        String tipoTurno=cabecera.get(res.getString(R.string.CTIPOTURNO));
        String tipo="-";
        if(tipoTurno.equals("ebrae"))
        {
            tipo="GATE IN";
        }
        else if(tipoTurno.equals("sañoda"))
        {
            tipo="GATE OUT";
        }


        preface.add(new Paragraph(tipo, smallBoldCourier));

        document.add(preface);

        Paragraph contenido = new Paragraph();
        contenido.setLeading(0, 2f);

        Chunk NroEIR1 = new Chunk("Nro de EIR     :", smallCourier);
        String tipoDoc=cabecera.get(res.getString(R.string.CTIPDOC));
        String numDoc=cabecera.get(res.getString(R.string.NNUMDOC));
        String docMostrar=tipoDoc+"-"+numDoc;
        Chunk NroEIR2 = new Chunk(docMostrar, smallBoldCourier);
        Phrase NroEIR = new Phrase(NroEIR1);
        NroEIR.add(NroEIR2);
        contenido.add(NroEIR);


        String numBooking=cabecera.get(res.getString(R.string.CBOOKING));
        if(numBooking==null)
        {
            numBooking="-";
        }
        Chunk booking1 = new Chunk("\nBooking       :", smallCourier);
        Chunk booking2 = new Chunk(numBooking, smallBoldCourier);
        Phrase booking = new Phrase(booking1);
        booking.add(booking2);
        contenido.add(booking);

        String codcntr=cabecera.get(res.getString(R.string.CCODCNTR));
        if(codcntr==null)   codcntr="-";
        String tamcntr=cabecera.get(res.getString(R.string.CTAMCNTR));
        if(tamcntr==null)   tamcntr="-";
        String tipocntr=cabecera.get(res.getString(R.string.CTIPOCNTR));
        if(tipocntr==null)   tipocntr="-";
        String codiso=cabecera.get(res.getString(R.string.CCODISOCNTR));
        if(codiso==null)   codiso="-";


        Chunk numcntr = new Chunk("\n"+codcntr+"     ", smallBoldCourier);
        Chunk detalles = new Chunk(tamcntr+"  "+tipocntr+"  "+codiso, smallCourier);
        Phrase cntr = new Phrase(numcntr);
        cntr.add(detalles);
        contenido.add(cntr);

        String linea=cabecera.get(res.getString(R.string.CCTELNA));
        String lineaDes=cabecera.get(res.getString(R.string.CDESCTELNA));
        String motonave=cabecera.get(res.getString(R.string.CMOTONAVE));
        String viaje=cabecera.get(res.getString(R.string.CVIAJE));
        String tipoMov=cabecera.get(res.getString(R.string.CUSOLOGICO));
        contenido.add(new Paragraph("\nLinea    : "+linea,
                smallCourier));

        contenido.add(new Paragraph("Buque    : "+motonave,
                smallCourier));

        contenido.add(new Paragraph("Viaje    : "+viaje,
                smallCourier));

        contenido.add(new Paragraph("Mov      : "+tipoMov,
                smallCourier));

        contenido.add(new Paragraph("Des      : "+lineaDes,
                smallCourier));

        addEmptyLine(contenido, 1);

        document.add(contenido);

        PdfPTable tablaDanios= crearTablaDaños(DaniosManager.darListaSoloDetalles(null), res);
        document.add(tablaDanios);


        Paragraph contenido2 = new Paragraph();
        contenido2.setLeading(0, 2f);

        String estado=cabecera.get(res.getString(R.string.CESTADOCNTR));
        String uso=cabecera.get(res.getString(R.string.CESTFINCNTR));
        String transportador=cabecera.get(res.getString(R.string.CDESTRANSPOR));
        String conductor=cabecera.get(res.getString(R.string.CDESCONDUCTOR));
        String cedula=cabecera.get(res.getString(R.string.CDESCEDULA));
        String celular=cabecera.get(res.getString(R.string.CCELULAR));
        String cplaca=cabecera.get(res.getString(R.string.CPLACA));
        String cobserva=cabecera.get(res.getString(R.string.COBSERVACION));
        String nomInspector=cabecera.get(res.getString(R.string.CNOMINSPECTOR));




        contenido2.add(new Paragraph("Estado   : "+estado, smallCourier));
        contenido2.add(new Paragraph("Uso      : "+uso, smallCourier));
        contenido2.add(new Paragraph("Transp   : "+transportador, smallCourier));
        contenido2.add(new Paragraph("Cond     : "+conductor, smallCourier));
        contenido2.add(new Paragraph("Cedula   : "+cedula,  smallCourier));
        contenido2.add(new Paragraph("Celular  : "+celular, smallCourier));


        Chunk placa1 = new Chunk("Placa    :", smallCourier);
        Chunk placa2 = new Chunk(cplaca, smallBoldCourier);
        Phrase placa = new Phrase(placa1);
        placa.add(placa2);
        contenido2.add(placa);
        addEmptyLine(contenido2, 2);

        document.add(contenido2);

        PdfPTable observaciones=crearTablaObservaciones(cobserva);
        document.add(observaciones);

        Paragraph inspector = new Paragraph(nomInspector,
                smallCourier);
        inspector.add(new Paragraph("\nInspector"));
        inspector.setAlignment(Element.ALIGN_CENTER);
        addEmptyLine(inspector, 3);
        document.add(inspector);


        LineSeparator ls = new LineSeparator();
        document.add(new Chunk(ls));

        Paragraph firma = new Paragraph("Nombre y Firma del Conductor", smallCourier);
        firma.setAlignment(Element.ALIGN_CENTER);
        addEmptyLine(firma, 2);
        document.add(firma);

        Paragraph parrafo = new Paragraph(res.getString(R.string.leyenda_factura),
                smallCourier);
        parrafo.setAlignment(Element.ALIGN_JUSTIFIED);
        addEmptyLine(parrafo, 2);
        document.add(parrafo);

        Paragraph fechaImpresion = new Paragraph(fecha2,
                smallCourier);
        fechaImpresion.setAlignment(Element.ALIGN_CENTER);
        document.add(fechaImpresion);


        // Start a new page


    }

    private static Image buscarLogo() {
        Image img = null;
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(path, "/Contenedores/" + "logocentralpark.jpg");
        try {
            InputStream ims = new FileInputStream(file);
            Bitmap bmp = BitmapFactory.decodeStream(ims);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
            img = Image.getInstance(stream.toByteArray());


        } catch (Exception e) {
            e.printStackTrace();
        }

        return img;
    }


    private static PdfPTable crearTablaObservaciones(String observaciones)
            throws BadElementException {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100f);


        PdfPCell c0 = new PdfPCell(new Phrase(observaciones));
        c0.setMinimumHeight(100f);


        table.addCell(c0);
        return table;

    }

    private static PdfPTable crearTablaDaños(ArrayList<HashMap<String,String>> lista, Resources res)
            throws BadElementException {
        PdfPTable table = new PdfPTable(6);
        try {
            table.setWidths(new float[]{10f, 10f, 10f, 8f, 12f, 10f});
        } catch (DocumentException e) {
            e.printStackTrace();
        }

        table.setWidthPercentage(100);


        // t.setBorderColor(BaseColor.GRAY);
        // t.setPadding(4);
        // t.setSpacing(4);
        // t.setBorderWidth(1);


        PdfPCell c0 = new PdfPCell(new Phrase("DAÑOS"));
        c0.setColspan(6);
        table.addCell(c0);
        PdfPCell c1 = new PdfPCell(new Phrase("Loc"));
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(c1);

        c1 = new PdfPCell(new Phrase("Daño"));
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(c1);

        c1 = new PdfPCell(new Phrase("Comp/ \n Met Rep"));
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(c1);

        c1 = new PdfPCell(new Phrase("Cant"));
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(c1);

        c1 = new PdfPCell(new Phrase("Resp \n Daño"));
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(c1);

        c1 = new PdfPCell(new Phrase("Rep"));
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(c1);


        table.setHeaderRows(1);
        if(lista.size()==0){
            String vacio=" \n-\n ";
            table.addCell(vacio);
            table.addCell(vacio);
            table.addCell(vacio);
            table.addCell(vacio);
            table.addCell(vacio);
            table.addCell(vacio);

        }

        for(int i=0;i<lista.size();i++)
        {
           HashMap<String,String> danio=lista.get(i);
            table.addCell(danio.get( res.getString(R.string.CCODUBI)) );
            table.addCell(danio.get(res.getString(R.string.CCODDAN)));
            table.addCell(danio.get(res.getString(R.string.CCODELE)));
            table.addCell(danio.get(res.getString(R.string.NUNIDADES)));
            table.addCell(danio.get(res.getString(R.string.CCARGOA)) );
            table.addCell(danio.get(res.getString(R.string.CCODMET)));

        }



        return table;
    }

    private static void addEmptyLine(Paragraph paragraph, int number) {
        for (int i = 0; i < number; i++) {
            paragraph.add(new Paragraph(" "));
        }
    }
}