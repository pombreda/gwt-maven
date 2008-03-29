/*
 * DebugMojo.java
 *
 * Created on January 12, 2007, 9:45 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.totsp.mavenplugin.gwt;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * @goal debug
 * @author cooper
 */
public class DebugMojo extends GWTMojo {
    public DebugMojo() {
        super();
    }

    public void execute() throws MojoExecutionException, MojoFailureException {
    	if (isDebugSuspend())
    		System.out.println( "Starting debugger on port " + getDebugPort() + " in suspend mode." );
    	else
    		System.out.println( "Starting debugger on port " + getDebugPort() );
        super.execute();
    }
}
