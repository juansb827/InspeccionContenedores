package com.juans.inspeccion.Mundo.Util;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Juan on 04/05/2015.
 */
public class FTPUpload {
    String server;
    int port;
    String user;
    String pass;
    FTPClient ftpClient;
        public FTPUpload(String server,int port,String user,String pass)
        {
            this.server=server;
            this.port=port;
            this.user=user;
            this.pass=pass;


        }

        public void conect () throws IOException {

            FTPClient ftpClient = new FTPClient();



            ftpClient.connect(server, port);
            ftpClient.login(user, pass);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

        }

        public void disconect() throws IOException {
            ftpClient.logout();
            ftpClient.disconnect();

        }

        public boolean uploadSingleFile(File localFile,String  folderName)  {
            boolean uploaded=false;
            try {
                boolean created = ftpClient.makeDirectory(folderName);
                InputStream inputStream = new FileInputStream(localFile);

                uploaded=ftpClient.storeFile("/"+folderName+"/"+localFile.getName(), inputStream);

            } catch (IOException e) {
                e.printStackTrace();
            }

                  return uploaded;


        }

        public  void  uploadFolder(File folder,String remoteFolderName) {

            FTPClient ftpClient = new FTPClient();

            try {

                System.out.println("Connected");

                String remoteDirPath = "/"+remoteFolderName;
                String localDirPath = folder.getAbsolutePath();

                FTPUtil.uploadDirectory(ftpClient, remoteDirPath, localDirPath, "");

                // log out and disconnect from the server
                ftpClient.logout();
                ftpClient.disconnect();

                System.out.println("Disconnected");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

}
