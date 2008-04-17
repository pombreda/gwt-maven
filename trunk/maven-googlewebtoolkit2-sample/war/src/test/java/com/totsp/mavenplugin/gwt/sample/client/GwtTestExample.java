package com.totsp.mavenplugin.gwt.sample.client;

import junit.framework.Assert;

import com.google.gwt.junit.client.GWTTestCase;

public class GwtTestExample extends GWTTestCase
{
    
    public String getModuleName() {
        return "com.totsp.mavenplugin.gwt.sample.Application";
    }

    public void setUp() throws Exception {
        super.setUp();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }     
    
    public void testSomething()
    {
        // Not much to actually test in this sample app
        // Ideally you would test your Controller here (NOT YOUR UI)
        // (Make calls to RPC services, test client side model objects, test client side logic, etc)
        Assert.assertTrue(true);
    }    
}