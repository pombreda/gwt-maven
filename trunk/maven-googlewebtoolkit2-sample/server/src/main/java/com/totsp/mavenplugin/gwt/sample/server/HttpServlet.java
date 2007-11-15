package com.totsp.mavenplugin.gwt.sample.server;

import java.io.IOException;

import javax.ejb.EJB;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.totsp.mavenplugin.gwt.sample.ejb.RandomCompliment;

public class HttpServlet extends javax.servlet.http.HttpServlet {

	@EJB
	private static RandomCompliment bean;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		try {
			InitialContext ic = new InitialContext();
			bean = (RandomCompliment) ic.lookup(RandomCompliment.class.getName());
			resp.getWriter().println(bean.getCompliment());
		} catch (Exception e) {
			e.printStackTrace(resp.getWriter());
		}
	}
}
