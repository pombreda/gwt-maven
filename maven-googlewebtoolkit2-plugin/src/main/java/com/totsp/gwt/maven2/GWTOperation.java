/*
 * GWTOperation.java
 *
 * Created on October 10, 2006, 7:36 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.totsp.gwt.maven2;

import java.io.File;
import org.apache.maven.plugin.AbstractMojo;

/**
 *
 * @author cooper
 */
public abstract class GWTOperation extends AbstractMojo {
    
    /**
     * GWT Installation Directory.
     * @parameter expression="${google.webtoolkit.home}"
     * @required
     */
    protected File googleWebToolkitHome;
    
    /**
     * GWT Output Folder
     * @parameter 
     *   expression="${google.webtoolkit.output}" 
     *   default-value="${project.build.directory}/${project.build.finalName}"
     */
    protected File googleWebToolkitOutputDirectory;
    
    /**
     * GWT Output Folder
     * @parameter 
     *   expression="${google.webtoolkit.style}" 
     *   default-value="OBF"
     */
    protected String googleWebToolkitStyle;
    
   
    /** 
     * GWT Module to compile
     * @parameter
     *   expression="${google.webtoolkit.compiletarget}"
     * @required
     */
    protected String googleWebToolkitCompileTarget;
    
    /**
     * GWT Logging level
     * @parameter
     *   expression="${google.webtoolkit.logLevel}"
     *   default-value="WARN";
     */
    protected String googleWebToolkitLogLevel;
    
    
    
    
    
    /** Creates a new instance of GWTOperation */
    public GWTOperation() {
        super();
    }
    
}
