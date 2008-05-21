package com.totsp.mavenplugin.gwt.sample.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.totsp.mavenplugin.gwt.sample.client.SampleRemoteService;
import com.totsp.mavenplugin.gwt.sample.client.SampleRemoteServiceAsync;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Application implements EntryPoint {

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		 
		//example of image bundle
		final SampleImageBundle sampleImageBundle = (SampleImageBundle) GWT.create(SampleImageBundle.class);
		final Image gwtLogoImage = sampleImageBundle.getGWTLogo().createImage();
		gwtLogoImage.setTitle("This image was delivered via automatic resource inclusion and the ImageBundle feature");
		
			
		final Button button = new Button("Run GWT Async RPC!");
		//example of css inclusion and use
		button.setStyleName("gwtMavenButton");
		
		final Label label = new Label();

		button.setTitle("This will execute the RPC call to the Java v1.5 GWT server");
		
		button.addClickListener(new ClickListener() {
			public void onClick(Widget sender) {

				// (1) Create the client proxy. Note that although you are
				// creating the
				// service interface proper, you cast the result to the
				// asynchronous
				// version of
				// the interface. The cast is always safe because the generated
				// proxy
				// implements the asynchronous interface automatically.
				SampleRemoteServiceAsync sampleRemoteService = (SampleRemoteServiceAsync) GWT
						.create(SampleRemoteService.class);

				// (2) Specify the URL at which our service implementation is
				// running.
				// Note that the target URL must reside on the same domain and
				// port from
				// which the host page was served.
				ServiceDefTarget endpoint = (ServiceDefTarget) sampleRemoteService;

				String moduleRelativeURL = GWT.getModuleBaseURL()
						+ "sampleRemoteService";
				endpoint.setServiceEntryPoint(moduleRelativeURL);

				// (3) Create an asynchronous callback to handle the result.
				AsyncCallback callback = new AsyncCallback() {
					public void onSuccess(Object result) {
						// do some UI stuff to show success
						label.setText((String) result);
					}

					public void onFailure(Throwable caught) {
						// do some UI stuff to show failure
						label.setText("DAMMIT! This didnt work.");
					}
				};

				// (4) Make the call. Control flow will continue immediately and
				// later
				// 'callback' will be invoked when the RPC completes.
				sampleRemoteService.doComplimentMe(callback);

			}
		});

		// Assume that the host HTML has elements defined whose
		// IDs are "slot1", "slot2". In a real app, you probably would not want
		// 4532
		// to hard-code IDs. Instead, you could, for example, search for all
		// elements with a particular CSS class and replace them with widgets.
		//
		RootPanel.get("gwtLogo").add(gwtLogoImage);
		RootPanel.get("rpcButton").add(button);
		RootPanel.get("rpcResponse").add(label);
	}
}
