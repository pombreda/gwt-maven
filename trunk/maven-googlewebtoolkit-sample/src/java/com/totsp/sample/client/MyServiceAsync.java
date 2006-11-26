package com.totsp.sample.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

interface MyServiceAsync {
  public void myMethod(String s, AsyncCallback callback);
}