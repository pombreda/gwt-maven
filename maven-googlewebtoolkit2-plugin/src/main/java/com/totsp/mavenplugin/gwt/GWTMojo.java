/*
 * GWTMojo.java
 *
 * Created on January 11, 2007, 6:42 PM
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */
package com.totsp.mavenplugin.gwt;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.FileUtils;

import com.totsp.mavenplugin.gwt.scripting.ScriptUtil;
import com.totsp.mavenplugin.gwt.scripting.ScriptWriterFactory;
import com.totsp.mavenplugin.gwt.support.MakeCatalinaBase;

/**
 * Runs the the project in the GWTShell for development.
 * 
 * Note that this goal is intended to be explicitly run from the command line 
 * (execute phase=), whereas other GWT-Maven goals are not (others happen as 
 * part of the standard Maven life-cycle phases: "compile" "test" "install").
 * 
 * @goal gwt
 * @execute goal="copy-webapp-resources"
 * @requiresDependencyResolution compile
 * @description Runs the the project in the GWTShell for development.
 * 
 * @author ccollins
 * @author cooper
 */
public class GWTMojo extends AbstractGWTMojo {

    /** Creates a new instance of GWTMojo */
    public GWTMojo() {
        super();
    }

    public void execute() throws MojoExecutionException, MojoFailureException {

        try {
            this.makeCatalinaBase();
        } catch (Exception e) {
            throw new MojoExecutionException("Unable to build catalina.base", e);
        }
        if (!this.getOutput().exists()) {
            this.getOutput().mkdirs();
        }

        // build it for the correct platform
        File exec = writeScript();        
        
        // run it
        ScriptUtil.runScript(exec);
    }
    
    
    
    protected File writeScript() throws MojoExecutionException {
      return ScriptWriterFactory.getInstance(getJavaCommand()).writeRunScript(this);
    }
    
    

    /**
     * Create embedded GWT tomcat base dir based on properties.
     * 
     * @throws Exception
     */
    public void makeCatalinaBase() throws Exception {
        getLog().debug("make catalina base for embedded Tomcat");        
        
        if (this.getWebXml() != null && this.getWebXml().exists()) {
            this.getLog().info("source web.xml present - " + this.getWebXml() + " - using it with embedded Tomcat");
        } else {
            this.getLog().info("source web.xml NOT present, using default empty web.xml for shell");
        }

        // note that MakeCatalinaBase (support jar) will use emptyWeb.xml if webXml does not exist 
        String[] args = { this.getTomcat().getAbsolutePath(), this.getWebXml().getAbsolutePath(), this.getShellServletMappingURL() };
        MakeCatalinaBase.main(args);

        if ((this.getContextXml() != null) && this.getContextXml().exists()) {
            this.getLog().info("contextXml parameter present - " + this.getContextXml() + " - using it for embedded Tomcat ROOT.xml");
            FileUtils.copyFile(this.getContextXml(), new File(this.getTomcat(), "conf/gwt/localhost/ROOT.xml"));
        }
    }
}
