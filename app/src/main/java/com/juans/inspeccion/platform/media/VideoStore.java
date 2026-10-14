package com.juans.inspeccion.platform.media;

import java.io.*;

public interface VideoStore {
    long MAX_DURATION_MILLIS = 300_000;
    long MAX_BYTES = 200_000_000;
    File createCapture(String id) throws IOException;
    long validate(File file) throws IOException;
    String publish(File file, String inspection, String name, String id) throws IOException;
    void delete(String reference) throws IOException;
}
