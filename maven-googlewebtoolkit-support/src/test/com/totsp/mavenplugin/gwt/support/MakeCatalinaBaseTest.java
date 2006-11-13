/*
 * MakeCatalinaBaseTest.java
 * JUnit based test
 *
 * Created on November 12, 2006, 10:29 PM
 */

package com.totsp.mavenplugin.gwt.support;

import junit.framework.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author cooper
 */
public class MakeCatalinaBaseTest extends TestCase {
    
    public MakeCatalinaBaseTest(String testName) {
        super(testName);
    }

    public void testMain() throws Exception {
        System.out.println("main");
        String source = GwtWebInfProcessorTest.getTestFile( "src/test/testWeb2.xml");
        String tomcat = GwtWebInfProcessorTest.getTestFile( "target/tomcat");
        String[] args = { tomcat, source };
        MakeCatalinaBase.main( args );
    }
    
}
