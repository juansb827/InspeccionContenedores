package com.juans.inspeccion.platform.media;

import android.content.Context;
import android.text.TextUtils;
import com.juans.inspeccion.Mundo.Album;
import com.juans.inspeccion.Mundo.Inspeccion;
import com.juans.inspeccion.R;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** User-visible inspection media names retain the released document/container convention. */
public final class PhotoNames {
    private static final Pattern INSPECTION_MEDIA = Pattern.compile(
            "^((?:ENTRADA|SALIDA)_(?:EP|ET)_.*?)(?:_DANIO#[0-9]+)?_[0-9]+\\.(?:jpg|mp4)$", Pattern.CASE_INSENSITIVE);
    private PhotoNames() { }
    /** General and damage media share the same readable inspection folder. */
    public static String folderForMedia(String name) {
        Matcher match = INSPECTION_MEDIA.matcher(name);
        return match.matches() ? match.group(1) : null;
    }
    public static String capture(Context context, Inspeccion inspection, Album album, int damageIndex) {
        return prefix(inspection, context.getString(R.string.NNUMDOC), context.getString(R.string.CCODCNTR))
                + (damageIndex < 0 ? "" : "_DANIO#" + damageIndex) + "_" + album.getFotos_tomadas() + ".jpg";
    }
    /** Uses the journaled inspection/album snapshot, with no extra serialized fields. */
    public static String video(Inspeccion inspection, Album album) {
        int damage = -1;
        if (!inspection.getFotosGenerales().getId().equals(album.getId())) {
            damage = inspection.getDaniosManager().getListaDanios().size();
            for (int i = 0; i < damage; i++) {
                if (inspection.getDaniosManager().getListaDanios().get(i).getAlbum().getId().equals(album.getId())) { damage = i; break; }
            }
        }
        // These are the existing serialized model's business keys, characterized against resources in tests.
        return prefix(inspection, "NNUMDOC", "CCODCNTR") + (damage < 0 ? "" : "_DANIO#" + damage) + "_0.mp4";
    }
    private static String prefix(Inspeccion inspection, String documentKey, String containerKey) {
        String document = inspection.getInformacion().get(documentKey);
        if (TextUtils.isEmpty(document)) document = "temp";
        String container = inspection.getDatosContenedor().get(containerKey);
        if (TextUtils.isEmpty(container)) container = inspection.getInformacion().get(containerKey);
        return inspection.getTipoInspeccion() + "_" + inspection.getTipoDocumento() + "_" + document + "_" + container;
    }
}
