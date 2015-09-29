package com.juans.inspeccion.Interfaz.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.ViewGroup.LayoutParams;
import android.widget.ProgressBar;

import com.juans.inspeccion.R;


/**
 * Created by Juan on 02/05/2015.
 */
public class BigProgressDialog extends Dialog {

    public static BigProgressDialog show(Context context, CharSequence title,
                                        CharSequence message) {
        return show(context, title, message, false);
    }

    public static BigProgressDialog show(Context context, CharSequence title,
                                        CharSequence message, boolean indeterminate) {
        return show(context, title, message, indeterminate, false, null);
    }

    public static BigProgressDialog show(Context context, CharSequence title,
                                        CharSequence message, boolean indeterminate, boolean cancelable) {
        return show(context, title, message, indeterminate, cancelable, null);
    }

    public static BigProgressDialog show(Context context, CharSequence title,
                                                 CharSequence message, boolean indeterminate,
                                                 boolean cancelable, OnCancelListener cancelListener) {
        BigProgressDialog dialog = new BigProgressDialog(context);
        dialog.setTitle(title);
        dialog.setCancelable(cancelable);
       // dialog.setOnCancelListener(cancelListener);
        dialog.setCanceledOnTouchOutside(false);
        /* The next line will add the ProgressBar to the dialog. */

        dialog.addContentView(new ProgressBar(context), new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        dialog.show();
        return dialog;
    }







    public BigProgressDialog(Context context) {
        super(context, R.style.NewDialog);
    }
}
