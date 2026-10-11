package com.juans.inspeccion.Mundo.Storage;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.activity.result.contract.ActivityResultContracts;

/** External camera apps receive access only to this request's private destination. */
public final class PrivateTakePicture extends ActivityResultContracts.TakePicture {
    @Override public Intent createIntent(Context context, Uri destination) {
        Intent intent = super.createIntent(context, destination);
        intent.setClipData(ClipData.newRawUri("foto", destination));
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        return intent;
    }
}
