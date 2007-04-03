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
        String[] baseArgs = { "-Xdebug",
                          "-Xnoagent",
                          "-Djava.compiler=NONE",
                          "-Xrunjdwp:transport=dt_socket,server=y,address="+
                            this.getPort() +",suspend=y" };
        this.baseArgs = baseArgs;
        super.execute();
    }
    
    
}
