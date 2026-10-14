package com.juans.inspeccion.domain.rules;

import com.juans.inspeccion.Mundo.Inspeccion;

import com.juans.inspeccion.ui.inspection.InspectionInformationFragment;

import android.content.res.Resources;
import android.text.TextUtils;
import com.juans.inspeccion.ui.inspection.InspeccionActivity;
import com.juans.inspeccion.R;
import java.util.ArrayList;
import java.util.HashMap;

/** Source-characterized rules extracted from InspectionInformationFragment; precedence and bypasses are intentional copies. */
public final class InspectionRules {
    private InspectionRules() {}
    public static String validationMessage(boolean fieldsComplete, Inspeccion inspection,
            HashMap<String,String> header, String action, Resources res) {
        String message = fieldsComplete ? null : "Faltan campos obligatorios";
        String containerLine = inspection.getDatosContenedor().get(res.getString(R.string.CCTELNA));
        String line = header.get(res.getString(R.string.CCTELNA));
        if (containerLine != null && !TextUtils.equals(containerLine, line))
            message = "El contenedor no peternece a la linea";
        boolean inYard = inspection.estaEnPatio();
        if (!inYard && inspection.getTipoInspeccion().equals(InspeccionActivity.SALIDA) && action == null)
            message = "El contenedor NO se encuentra en el patio";
        else if (inYard && inspection.getTipoInspeccion().equals(InspeccionActivity.ENTRADA) && action == null)
            message = "El contenedor YA se encuentra en el patio";
        if (action != null || TextUtils.equals(header.get(res.getString(R.string.CTIPDOC)), InspeccionActivity.ET))
            return message;
        if (inspection.getTipoInspeccion().equals(InspeccionActivity.SALIDA)) {
            ArrayList<HashMap<String,String>> booking = inspection.getInfoBooking();
            if (booking != null && !booking.isEmpty()) {
                if (!inspection.isPerteneceEnBooking()) message = "El contenedor no pertenece al Booking";
                if (!inspection.isHaySaldoBooking()) message = "El booking ya fue atendido totalmente";
            }
        }
        return message;
    }
    public static String turnoMessage(HashMap<String,String> data, String direction, Resources res) {
        if (data.size() == 0) return "No se ha encontrado el turno";
        String type = data.get(res.getString(R.string.CTIPOTURNO));
        String attended = data.get(res.getString(R.string.NATENDIDO));
        if (attended.equals("1")) return "Este turno ya fue atendido";
        if (type != null && !type.trim().equals(direction)) return "El tipo de turno no es de :" + direction;
        return null;
    }
}
