package com.juans.inspeccion.Mundo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Juan on 18/03/2015.
 * This class represents the data that we want to store and retrieve from the database.
 */
public class Inspeccion implements Serializable{

    String tipoInspeccion;
    String tipoDocumento;
    HashMap<String,String> informacion;
    HashMap<String,String> datosContenedor;
    String[] fechaInspeccion;
    boolean perteneceEnBooking;

    DaniosManager daniosManager;
    Album fotosGenerales;
    ArrayList<Album> fotosInspeccion;
    String parentFolder;
    ArrayList<HashMap<String,String>> infoBooking;
    ArrayList<HashMap<String,String>> turnosPendientes;
    boolean estaEnPatio;
    boolean haySaldoBooking;
    boolean usaTurno;

    /*
   /Usada solo para fotos
    */
    public Inspeccion(String parentFolder)
    {
        this.parentFolder=parentFolder;
        fotosInspeccion=new ArrayList<>();
    }

    public Inspeccion() {
        informacion=new HashMap<>();
        datosContenedor=new HashMap<>();
        fotosGenerales=new Album();
        daniosManager=new DaniosManager();
    }





    public void setFotosInspeccion(ArrayList<Album> fotosInspeccion) {
        this.fotosInspeccion = fotosInspeccion;
    }

    public Album getFotosGenerales() {
        return fotosGenerales;
    }

    public void setFotosGenerales(Album fotosGenerales) {
        this.fotosGenerales = fotosGenerales;
    }


    public boolean isPerteneceEnBooking() {
        return perteneceEnBooking;
    }

    public void setPerteneceEnBooking(boolean perteneceEnBooking) {
        this.perteneceEnBooking = perteneceEnBooking;
    }



    public boolean isHaySaldoBooking() {
        return haySaldoBooking;
    }

    public void setHaySaldoBooking(boolean haySaldoBooking) {
        this.haySaldoBooking = haySaldoBooking;
    }


    public void agregarAlbum(Album album)
    {
        fotosInspeccion.add(album);
    }

    public ArrayList<Album> getFotosInspeccion() {
        return fotosInspeccion;
    }

    public String getParentFolder() {
        return parentFolder;
    }




    public HashMap<String, String> getInformacion() {
        return informacion;
    }

    public void setInformacion(HashMap<String, String> informacion) {
        this.informacion = informacion;
    }




    public HashMap<String, String> getDatosContenedor() {
        return datosContenedor;
    }

    public void setDatosContenedor(HashMap<String, String> datosContenedor) {
        this.datosContenedor = datosContenedor;
    }

    public boolean estaEnPatio() {
        return estaEnPatio;
    }

    public void setEstaEnPatio(boolean contenedorEstaEnPatio) {
        this.estaEnPatio = contenedorEstaEnPatio;
    }

    public ArrayList<HashMap<String, String>> getInfoBooking() {
        return infoBooking;
    }

    public void setInfoBooking(ArrayList<HashMap<String, String>> infoBooking) {
        this.infoBooking = infoBooking;
    }

    public String getTipoInspeccion()
    {
        return tipoInspeccion;
    }

    public void setTipoInspeccion(String tipoInspeccion)
    {
        this.tipoInspeccion=tipoInspeccion;
    }

    public String getTipoDocumento()
    {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento)
    {
        this.tipoDocumento=tipoDocumento;
    }

    public DaniosManager getDaniosManager()
    {
        return daniosManager;
    }

    public void setDaniosManager(DaniosManager daniosManager)
    {
        this.daniosManager=daniosManager;
    }


    public void setFechaInspeccion(String[] fechaInspeccion) {
        this.fechaInspeccion = fechaInspeccion;
    }
    public String[] getFechaInspeccion() {
        return fechaInspeccion;
    }

    public boolean isUsaTurno() {
        return usaTurno;
    }

    public void setUsaTurno(boolean usaTurno) {
        this.usaTurno = usaTurno;
    }

    public ArrayList<HashMap<String,String>> getTurnosPendientes()
    {
        return turnosPendientes;
    }
    public void  setTurnosPendientes(ArrayList<HashMap<String,String>> turnosPendientes)
    {
        this.turnosPendientes=turnosPendientes;
    }


    public void limpiar()
    {
        informacion.clear();
        datosContenedor.clear();
        daniosManager.borrarDanios();
        if(infoBooking!=null) infoBooking.clear();
        if(turnosPendientes!=null) turnosPendientes.clear();
        fotosGenerales=new Album();

    }

}


