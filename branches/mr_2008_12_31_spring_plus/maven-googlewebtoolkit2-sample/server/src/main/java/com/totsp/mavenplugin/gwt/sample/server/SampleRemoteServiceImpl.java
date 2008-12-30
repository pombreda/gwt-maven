package com.totsp.mavenplugin.gwt.sample.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.totsp.mavenplugin.gwt.sample.client.SampleRemoteService;

public class SampleRemoteServiceImpl extends RemoteServiceServlet implements
		SampleRemoteService {

	public String doComplimentMe() {
		return RandomCompliment.get();
	}	
}
