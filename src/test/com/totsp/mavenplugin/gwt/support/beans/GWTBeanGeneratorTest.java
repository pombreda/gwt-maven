/*
 * GWTBeanGeneratorTest.java
 * JUnit based test
 *
 * Created on November 26, 2006, 11:22 AM
 */

package com.totsp.mavenplugin.gwt.support.beans;

import junit.framework.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

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
