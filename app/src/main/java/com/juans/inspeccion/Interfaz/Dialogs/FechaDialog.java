package com.juans.inspeccion.Interfaz.Dialogs;

import android.content.Context;
import android.widget.TimePicker;

import com.juans.inspeccion.Mundo.FilaEnConsulta;
import com.juans.inspeccion.Mundo.Formularios;

import java.util.Calendar;

/**
 * Created by juan__000 on 11/2/2014.
 */
public class FechaDialog {


    public static android.app.DatePickerDialog mostrarDatePicker(final int iniciadoPor, Formularios.DataPass _dp,Context cont) {
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);
        final Formularios.DataPass dp = _dp;

        // Launch Date Picker Dialog
        android.app.DatePickerDialog dpd = new android.app.DatePickerDialog(cont,
                new android.app.DatePickerDialog.OnDateSetListener() {


                    public void onDateSet(android.widget.DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
                        String fecha = (dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                        FilaEnConsulta fila = new FilaEnConsulta(1);
                        fila.setDato(0, fecha);
                        dp.onDataReceive(fila, iniciadoPor);

                    }
                }, mYear, mMonth, mDay
        );

         dpd.getDatePicker().setDescendantFocusability(TimePicker.FOCUS_BLOCK_DESCENDANTS);

        return dpd;
    }
}
