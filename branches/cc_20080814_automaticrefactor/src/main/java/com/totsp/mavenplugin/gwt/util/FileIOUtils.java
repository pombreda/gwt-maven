package com.totsp.mavenplugin.gwt.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 
 * @author ccollins
 *
 */
public class FileIOUtils {

   public static final int DEFAULT_BUFFER_SIZE = 1024;

   private FileIOUtils() {
   }

   /** 
    * Copies the data from an InputStream object to an OutputStream object.
    * 
    * @param sourceStream The input stream to be read.
    * @param destinationStream The output stream to be written to.
    * @return int value of the number of bytes copied.
    * @exception IOException from java.io calls.
    */
   public static int copyStream(InputStream sourceStream, OutputStream destinationStream) throws IOException {
      int bytesRead = 0;
      int totalBytes = 0;
      byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];

      while (bytesRead >= 0) {
         bytesRead = sourceStream.read(buffer, 0, buffer.length);

         if (bytesRead > 0) {
            destinationStream.write(buffer, 0, bytesRead);
         }

         totalBytes += bytesRead;
      }
      destinationStream.flush();
      destinationStream.close();
      return totalBytes;
   }

   /**
    * Copy a file from source to destination.
    * 
    * @param source
    * @param destination
    * @throws IOException
    */
   public static void copyFile(File source, File destination) throws IOException {
      FileOutputStream fos = new FileOutputStream(destination);
      FileInputStream fis = new FileInputStream(source);
      copyStream(fis, fos);
      fos.flush();
      fos.close();
      fis.close();
   }

}