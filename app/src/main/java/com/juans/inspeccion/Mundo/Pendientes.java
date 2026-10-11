package com.juans.inspeccion.Mundo;

import android.content.Context;
import com.juans.inspeccion.Interfaz.MainActivity;
import com.juans.inspeccion.Mundo.Storage.StoredInspections;
import com.juans.inspeccion.R;
import java.util.ArrayList;
import java.util.HashMap;

public class Pendientes {
    private static ArrayList<Inspeccion> listaPendientes;
    private static ArrayList<HashMap<String,String>> resumenTurnos;
    public static final String PENDIENTE = "PENDIENTE";

    public static synchronized void guardarListaTurnos(Context context) {
        if (listaPendientes != null) StoredInspections.write(context, StoredInspections.DRAFTS, listaPendientes);
    }
    public static synchronized void cargarListaTurnos(Context context) {
        listaPendientes = StoredInspections.read(context, StoredInspections.DRAFTS);
        resumenTurnos = null;
    }
    public static synchronized void invalidate() { listaPendientes = null; resumenTurnos = null; }
    public static synchronized Inspeccion darPendiente(int index, Context context) {
        ArrayList<Inspeccion> list = darListaTurnos(context);
        return index >= 0 && index < list.size() ? list.get(index) : null;
    }
    public static synchronized ArrayList<Inspeccion> darListaTurnos(Context context) {
        if (listaPendientes == null) cargarListaTurnos(context);
        return listaPendientes;
    }
    public static synchronized ArrayList<HashMap<String,String>> darResumenTurnos(Context context) {
        if (listaPendientes == null) cargarListaTurnos(context);
        if (resumenTurnos == null) {
            resumenTurnos = new ArrayList<>();
            String turno = context.getString(R.string.NTURNO);
            String fecha = context.getString(R.string.FECHA_MOSTRAR);
            String tipo = context.getString(R.string.CTIPOTURNO);
            for (Inspeccion inspection : listaPendientes) {
                HashMap<String,String> summary = new HashMap<>();
                summary.put(turno, inspection.getInformacion().get(turno));
                summary.put(fecha, inspection.getInformacion().get(fecha));
                summary.put(tipo, inspection.getInformacion().get(tipo));
                resumenTurnos.add(summary);
            }
        }
        return resumenTurnos;
    }
    public static synchronized void agregarInspeccion(Inspeccion inspection, Context context) {
        ArrayList<Inspeccion> next = new ArrayList<>(darListaTurnos(context));
        next.add(inspection);
        commit(context, next);
    }
    public static synchronized void remplazarTurno(int index, Inspeccion inspection, Context context) {
        ArrayList<Inspeccion> next = new ArrayList<>(darListaTurnos(context));
        next.set(index, inspection);
        commit(context, next);
    }
    public static synchronized void borrarTurno(int index, Context context) {
        ArrayList<Inspeccion> next = new ArrayList<>(darListaTurnos(context));
        if (index >= 0 && index < next.size()) { next.remove(index); commit(context, next); }
    }
    public static synchronized void borrarTodo() { borrarTodo(MainActivity.context); }
    public static synchronized void borrarTodo(Context context) {
        darListaTurnos(context); // Refuse to overwrite a damaged store.
        commit(context, new ArrayList<>());
    }
    public static synchronized int findIndex(String id, Context context) {
        ArrayList<Inspeccion> values = StoredInspections.read(context, StoredInspections.DRAFTS);
        for (int i = 0; i < values.size(); i++) if (values.get(i).getPhotoId().equals(id)) return i;
        return -1;
    }
    public static synchronized void upsert(Inspeccion inspection, Context context) {
        ArrayList<Inspeccion> next = StoredInspections.read(context, StoredInspections.DRAFTS);
        int index = -1;
        for (int i = 0; i < next.size(); i++) if (next.get(i).getPhotoId().equals(inspection.getPhotoId())) { index = i; break; }
        if (index == -1) next.add(inspection); else next.set(index, inspection);
        commit(context, next);
    }
    public static synchronized void remove(String id, Context context) {
        ArrayList<Inspeccion> next = StoredInspections.read(context, StoredInspections.DRAFTS);
        for (int i = next.size() - 1; i >= 0; i--) if (next.get(i).getPhotoId().equals(id)) next.remove(i);
        commit(context, next);
    }
    private static void commit(Context context, ArrayList<Inspeccion> next) {
        StoredInspections.write(context, StoredInspections.DRAFTS, next);
        listaPendientes = next;
        resumenTurnos = null;
    }
}
