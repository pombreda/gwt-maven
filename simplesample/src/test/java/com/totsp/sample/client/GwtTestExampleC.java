package com.totsp.sample.client;

import junit.framework.Assert;

import com.google.gwt.junit.client.GWTTestCase;

public class GwtTestExampleC extends GWTTestCase
{
    
    public String getModuleName() {
        return "com.totsp.sample.Application";
    }
    
    public void testSomething()
    {
        // check that we can fail one
        Assert.assertTrue(false);
    }    
}