/*
 * Shell.java
 *
 * Created on October 10, 2006, 8:13 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.totsp.gwt.maven2;

import org.apache.maven.plugin.MojoExecutionException;

/**
 *
 * @author cooper
 */
public class Shell extends Compile {
    
    private static final String SHELL_CLASS_NAME = "com.google.gwt.dev.GWTShell";
    
    /** Creates a new instance of Shell */
    public Shell() {
        super();
    }

    public void execute() throws MojoExecutionException {
        this.runMainClass( Shell.SHELL_CLASS_NAME );
    }
    
    
     
}
