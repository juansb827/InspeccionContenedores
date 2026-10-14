package com.juans.inspeccion.platform.media;

import android.graphics.Bitmap;
import android.net.Uri;
import java.io.*;

public interface PhotoStore {
    File createCapture(String captureId) throws IOException;
    Uri captureUri(File file);
    String publish(File source, String inspectionId, String displayName, String publicationId) throws IOException;
    String importPhoto(InputStream source, String inspectionId, String displayName, String publicationId) throws IOException;
    InputStream open(String reference) throws IOException;
    String displayName(String reference) throws IOException;
    Bitmap thumbnail(String reference, int width, int height) throws IOException;
    void rename(String reference, String displayName) throws IOException;
    void delete(String reference) throws IOException;
}
