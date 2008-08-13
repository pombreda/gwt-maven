/*
 * ServiceProxyGeneratorBaseTest.java
 * JUnit based test
 *
 * Created on May 29, 2007, 2:29 PM
 */

package com.totsp.mavenplugin.gwt.support.beans;

import junit.framework.*;
import java.io.File;
import test.service.MyService;
import test.service.MyServiceImpl;

/**
 *
 * @author rcooper
 */
public class ServiceProxyGeneratorBaseTest extends TestCase {
    
    public ServiceProxyGeneratorBaseTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    /**
     * Test of writeService method, of class com.totsp.mavenplugin.gwt.support.beans.ServiceProxyGeneratorBase.
     */
    public void testWriteService() throws Exception {
        System.out.println("writeService");
        
        String packageName = "client.service";
        File packageDirectory = new File("./target/servicegentest/client/service");
        packageDirectory.mkdirs();
        boolean getSet = true;
        boolean propSupport = true;
        boolean overwrite = true;
        Class serviceInterface = MyService.class;
        Class implementation = MyServiceImpl.class;
        
        ServiceProxyGeneratorBase.writeService(packageName, packageDirectory, getSet, propSupport, overwrite, serviceInterface, implementation);
        
        
    }
    
}
