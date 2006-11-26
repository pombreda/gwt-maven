package com.totsp.sample.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import com.totsp.sample.client.MyService;

import java.util.Date;


public class MyServiceImpl extends RemoteServiceServlet implements MyService {
    public String myMethod(String s) {
        return "Hello from the server " + s + ", the time is now: " +
        new Date() + ".";
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
}
