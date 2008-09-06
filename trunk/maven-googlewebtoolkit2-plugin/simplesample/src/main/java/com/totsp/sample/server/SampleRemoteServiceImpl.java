package com.totsp.sample.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.totsp.sample.client.SampleRemoteService;

public class SampleRemoteServiceImpl extends RemoteServiceServlet implements
		SampleRemoteService {

	public String doComplimentMe() {
		return "this is a compliment from the server side - you don't look TOO bad today";
	}	
}
