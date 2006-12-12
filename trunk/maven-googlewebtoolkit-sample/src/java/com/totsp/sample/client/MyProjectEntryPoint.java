package com.totsp.sample.client;

import java.util.List;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.totsp.sample.client.model.Entry;


/**
 * EntryPoint example of a GWT RPC call,
 * in order to demonstrate using the TotSP Maven GWT plugin:
 * http://code.google.com/p/gwt-maven.
 * 
 * In the real world do not put all your crap in an EntryPoint like this 
 * (this is an example which is meant to be quick and dirty).
 *
 * @author ccollins
 *
 */
public class MyProjectEntryPoint implements EntryPoint {
    // get data service
    MyServiceAsync service = (MyServiceAsync) GWT.create(MyService.class);
    ServiceDefTarget endpoint = (ServiceDefTarget) service;

    /**
     * EntryPoint onModuleLoad.
     *
     */
    public void onModuleLoad() {
        // direct the endpoint to the service name as defined in the MyProject.gwt.xml module file
        endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "MyService");

        // add some basic layout and input elements
        final VerticalPanel panel = new VerticalPanel();
        final Label display = new Label();
        final Label name = new Label("Enter Name: ");
        final Button button = new Button("Go");
        final TextBox input = new TextBox();

        // handle the button being clicked with a ClickListener
        button.addClickListener(new ClickListener() {
                public void onClick(Widget sender) {
                    // make the RPC call to the server
                    callMyService(input.getText(), display);
                }
            });

        // add the widgets to the panel, and the panel to the RootPanel
        panel.add(name);
        panel.add(input);
        panel.add(button);
        panel.add(display);
        RootPanel.get().add(panel);
    }

    /**
     * Super simple method to wrap GWT RPC call.
     *
     * @param s
     * @param l
     */
    public void callMyService(final String s, final Label l) {
        // invoke the service
        service.myMethod(s,
            new AsyncCallback() {
                // what to do if it works
                public void onSuccess(Object result) {                    
                    List entries = (List) result;
                    StringBuffer resultBuffer = new StringBuffer();
                    if (entries != null)
                    {
                        for (int i=0; i < entries.size(); i++)
                        {
                            Entry entry = (Entry) entries.get(i);
                            resultBuffer.append("name - " + entry.name + "  | time - " + entry.time + "\n");
                        }
                    }
                    
                    l.setText("The current DataSource store result set is: \n" + resultBuffer.toString());
                }

                // what to do if it fails
                public void onFailure(final Throwable caught) {
                    Window.alert("failure - \n" + caught.getMessage());
                }
            });
    }
}
