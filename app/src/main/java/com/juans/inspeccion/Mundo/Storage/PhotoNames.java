package com.juans.inspeccion.Mundo.Storage;

import android.content.Context;
import android.text.TextUtils;
import com.juans.inspeccion.Mundo.Album;
import com.juans.inspeccion.Mundo.Inspeccion;
import com.juans.inspeccion.R;

/** Remote names retain the released convention; local destinations add unique capture IDs. */
public final class PhotoNames {
    private PhotoNames() { }
    public static String capture(Context context, Inspeccion inspection, Album album, int damageIndex) {
        String document = inspection.getInformacion().get(context.getString(R.string.NNUMDOC));
        if (TextUtils.isEmpty(document)) document = inspection.getPhotoId();
        String container = inspection.getDatosContenedor().get(context.getString(R.string.CCODCNTR));
        if (TextUtils.isEmpty(container)) container = inspection.getInformacion().get(context.getString(R.string.CCODCNTR));
        String prefix = inspection.getTipoInspeccion() + "_" + inspection.getTipoDocumento() + "_" + document + "_" + container;
        return prefix + (damageIndex < 0 ? "" : "_DANIO#" + damageIndex) + "_" + album.getFotos_tomadas() + ".jpg";
    }
}
