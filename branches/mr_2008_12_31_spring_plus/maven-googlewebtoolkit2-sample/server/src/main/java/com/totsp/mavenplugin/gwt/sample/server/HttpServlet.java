package com.totsp.mavenplugin.gwt.sample.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is a sample of how to integrate a standard HTTPServlet inside a
 * GWT-Maven project. More specifically, the mergexml functionality will use
 * this HttpServlet & web.xml servlet mapping to integrate them into your
 * GWT-Maven WebApp.
 * 
 * @author andrew
 * 
 */
public class HttpServlet extends javax.servlet.http.HttpServlet {

	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		try {
			// Call a random compliment, built with Java5 enums!
			resp.getWriter().println(RandomCompliment.get());
		} catch (Exception e) {
			// completely unexpected exception!
			e.printStackTrace(resp.getWriter());
		}
	}

}
