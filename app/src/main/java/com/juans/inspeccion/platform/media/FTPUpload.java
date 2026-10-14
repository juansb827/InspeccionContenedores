package com.juans.inspeccion.platform.media;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import java.io.*;

/** Stream-based uploads do not assume a filesystem path for MediaStore photos. */
public class FTPUpload implements AutoCloseable {
    private final String server, user, pass;
    private final int port;
    private final FTPClient ftpClient;
    public FTPUpload(String server, int port, String user, String pass) {
        this(server, port, user, pass, new FTPClient());
    }
    public FTPUpload(String server, int port, String user, String pass, FTPClient client) {
        this.server = server; this.port = port; this.user = user; this.pass = pass; ftpClient = client;
    }
    public void conect() throws IOException {
        try {
            ftpClient.setConnectTimeout(10000);
            ftpClient.connect(server, port);
            if (!ftpClient.login(user, pass)) throw new IOException("El servidor FTP rechazó la conexión");
            ftpClient.enterLocalPassiveMode();
            if (!ftpClient.setFileType(FTP.BINARY_FILE_TYPE)) throw new IOException("No se pudo configurar la transferencia FTP");
        } catch (IOException | RuntimeException e) {
            try { close(); } catch (IOException cleanup) { e.addSuppressed(cleanup); }
            throw e;
        }
    }
    public boolean uploadSingleFile(InputStream input, String folder, String filename) throws IOException {
        try (InputStream source = input) {
            if (!ftpClient.isConnected()) throw new IOException("FTP no conectado");
            if (!component(folder) || !component(filename)) throw new IOException("Nombre remoto inválido");
            ftpClient.makeDirectory(folder); // An existing directory may return false.
            return ftpClient.storeFile("/" + folder + "/" + filename, source);
        }
    }
    private static boolean component(String value) {
        return value != null && !value.isEmpty() && !value.equals(".") && !value.equals("..")
                && !value.contains("/") && !value.contains("\\") && !value.contains("\r") && !value.contains("\n");
    }
    public void disconect() throws IOException { close(); }
    @Override public void close() throws IOException {
        if (ftpClient.isConnected()) {
            try { ftpClient.logout(); }
            finally { ftpClient.disconnect(); }
        }
    }
}
