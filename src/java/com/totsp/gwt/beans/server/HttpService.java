/*
 * HttpService.java
 *
 * Created on May 29, 2007, 12:29 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.totsp.gwt.beans.server;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
/**
 *
 * @author rcooper
 */
public interface HttpService {
    
    public void setThreadLocal( HttpServletRequest request, HttpServletResponse response );
    
    public void unsetThreadLocal();
}
