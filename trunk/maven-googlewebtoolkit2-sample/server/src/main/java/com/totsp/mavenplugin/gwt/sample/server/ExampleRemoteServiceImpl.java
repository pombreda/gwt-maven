package com.totsp.mavenplugin.gwt.sample.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.totsp.mavenplugin.gwt.sample.client.ExampleRemoteService;
import com.totsp.mavenplugin.gwt.sample.ejb.RandomComplimentBean;

public class ExampleRemoteServiceImpl extends RemoteServiceServlet implements
		ExampleRemoteService {

	//@EJB
	//private static RandomCompliment bean;

	public String doComplimentMe() {
		String response = "ERROR: NEVER EXECUTED!!!!";
		try {
			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			RandomComplimentBean rcb = (RandomComplimentBean) java.beans.Beans.instantiate(cl, RandomComplimentBean.class.getName());
			//bean = (RandomCompliment) ic.lookup(RandomCompliment.class.getName());
			//response = bean.getCompliment();
			response = rcb.getCompliment();
		} catch (Exception e) {
			response = e.toString();
		}
		return response;
	}	
}
