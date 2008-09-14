package com.totsp.sample.client.controller;

import java.util.List;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.totsp.sample.client.MyService;
import com.totsp.sample.client.MyServiceAsync;
import com.totsp.sample.client.model.Entry;
import com.totsp.sample.client.model.MyProjectData;

public class MyProjectController {
    
    private MyProjectData data;
    
    public MyProjectController(MyProjectData data) {
        this.data = data;
    }    
   
    public void addEntry(final String s) {
        // invoke the service, getting reference to it from the handy inner Util class
        MyServiceAsync service = MyService.Util.getInstance();
        service.myMethod(s,
            new AsyncCallback<List<Entry>>() {
            
                // what to do if it works                
                public void onSuccess(List<Entry> results) {
                  data.setEntries(results);
                }

                // what to do if it fails
                public void onFailure(final Throwable caught) {
                    Window.alert("failure - \n" + caught.getMessage());
                }
            });
    }    
}