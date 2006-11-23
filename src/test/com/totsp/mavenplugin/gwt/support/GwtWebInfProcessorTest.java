/*
 * GwtWebInfProcessorTest.java
 * JUnit based test
 *
 * Created on October 10, 2006, 3:47 PM
 */

package com.totsp.mavenplugin.gwt.support;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import junit.framework.*;

/**
 *
 * @author cooper
 */
public class GwtWebInfProcessorTest extends TestCase {
    
    /** 
     * Basedir for all file I/O. Important when running tests from
     * the reactor.
     */
    public static String basedir = System.getProperty("basedir");
    
    public GwtWebInfProcessorTest(String testName) {
        super(testName);
    }

    public void testProcess() throws Exception {
        System.out.println("process");
        
         GwtWebInfProcessor instance = new GwtWebInfProcessor( "testModule.Test",
                this.getTestFile("target/web.xml"),
                this.getTestFile("src/test/testWeb.xml"));
         instance.process();
         instance = new GwtWebInfProcessor( "testModule.Test",
                this.getTestFile("target/web2.xml"),
                this.getTestFile("src/test/testWeb2.xml"));
         instance.process();
        
        // TODO review the generated test code and remove the default call to fail.
        
    }

     public static void copyFile(File in, File out) throws IOException {
        FileInputStream fis = new FileInputStream(in);
        FileOutputStream fos = new FileOutputStream(out);
        byte[] buf = new byte[1024];
        int i = 0;
        while ((i = fis.read(buf)) != -1) {
            fos.write(buf, 0, i);
        }
        fis.close();
        fos.close();
    }
    
     /**
     * Get test input file.
     *
     * @param path Path to test input file.
     */
    public static String getTestFile(String path)
    {
        return new File(basedir,path).getAbsolutePath();
    }
}
