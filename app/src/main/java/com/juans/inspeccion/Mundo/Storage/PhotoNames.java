package com.juans.inspeccion.Mundo.Storage;

import android.content.Context;
import android.text.TextUtils;
import com.juans.inspeccion.Mundo.Album;
import com.juans.inspeccion.Mundo.Inspeccion;
import com.juans.inspeccion.R;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** User-visible photo names retain the released document/container convention. */
public final class PhotoNames {
    private static final Pattern INSPECTION_PHOTO = Pattern.compile(
            "^((?:ENTRADA|SALIDA)_(?:EP|ET)_.*?)(?:_DANIO#[0-9]+)?_[0-9]+\\.jpg$", Pattern.CASE_INSENSITIVE);
    private PhotoNames() { }
    /** General and damage photos share the same readable inspection folder. */
    public static String folderForPhoto(String name) {
        Matcher match = INSPECTION_PHOTO.matcher(name);
        return match.matches() ? match.group(1) : null;
    }
    public static String capture(Context context, Inspeccion inspection, Album album, int damageIndex) {
        String document = inspection.getInformacion().get(context.getString(R.string.NNUMDOC));
        if (TextUtils.isEmpty(document)) document = "temp";
        String container = inspection.getDatosContenedor().get(context.getString(R.string.CCODCNTR));
        if (TextUtils.isEmpty(container)) container = inspection.getInformacion().get(context.getString(R.string.CCODCNTR));
        String prefix = inspection.getTipoInspeccion() + "_" + inspection.getTipoDocumento() + "_" + document + "_" + container;
        return prefix + (damageIndex < 0 ? "" : "_DANIO#" + damageIndex) + "_" + album.getFotos_tomadas() + ".jpg";
    }
}
