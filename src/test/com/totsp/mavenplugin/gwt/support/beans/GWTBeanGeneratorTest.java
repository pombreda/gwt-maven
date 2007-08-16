/*
 * GWTBeanGeneratorTest.java
 * JUnit based test
 *
 * Created on November 26, 2006, 11:22 AM
 */

package com.totsp.mavenplugin.gwt.support.beans;

import junit.framework.*;

/**
 *
 * @author cooper
 */
public class GWTBeanGeneratorTest extends TestCase {
    
    public GWTBeanGeneratorTest(String testName) {
        super(testName);
    }

    public void testMain() throws Exception {
        System.out.println("main");
        
        String[] args = {  "-withPropertyChangeSupport", "-destinationPackage", "test.client", 
                         "-destinationDirectory", System.getProperty("basedir")+"/target/test/gen",
                         "-startBean", "test.server.ABean",};
        
        
        GWTBeanGenerator.main(args);
        
        
    }
    
}
