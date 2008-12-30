package com.totsp.sample.client;

import com.google.gwt.user.client.rpc.AsyncCallback;


/**
 * Simple GWT CLIENT side ASYNC service interface
 * (same prefix NAME as non async interface, no return type, AsyncCallback param added, no explicit throws).
 *
 * @author ccollins
 *
 */
interface MyServiceAsync {
    public void myMethod(String s, AsyncCallback callback);
}
