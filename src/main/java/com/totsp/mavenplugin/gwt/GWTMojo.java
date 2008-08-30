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

import com.totsp.mavenplugin.gwt.scripting.ProcessWatcher;
import com.totsp.mavenplugin.gwt.scripting.ScriptWriterUnix;
import com.totsp.mavenplugin.gwt.scripting.ScriptWriterWindows;
import com.totsp.mavenplugin.gwt.support.MakeCatalinaBase;

/**
 * Runs the the project in the GWTShell for development.
 * 
 * @goal gwt
 * @execute phase=compile
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

        if (AbstractGWTMojo.OS_NAME.startsWith(WINDOWS)) {
            ScriptWriterWindows writer = new ScriptWriterWindows();
            try {
                File exec = writer.writeRunScript(this);
                ProcessWatcher pw = new ProcessWatcher("\"" + exec.getAbsolutePath() + "\"");
                pw.startProcess(System.out, System.err);
                int retVal = pw.waitFor();
                if (retVal != 0) {
                    throw new MojoExecutionException("run script exited abnormally with code - " + retVal);
                }
            } catch (Exception e) {
                throw new MojoExecutionException("Exception attempting run.", e);
            }
        } else {
            ScriptWriterUnix writer = new ScriptWriterUnix();
            try {
                File exec = writer.writeRunScript(this);
                ProcessWatcher pw = new ProcessWatcher(exec.getAbsolutePath().replaceAll(" ", "\\ "));
                pw.startProcess(System.out, System.err);
                int retVal = pw.waitFor();
                if (retVal != 0) {
                    throw new MojoExecutionException("run script exited abnormally with code - " + retVal);
                }
            } catch (Exception e) {
                throw new MojoExecutionException("Exception attempting run.", e);
            }
        }
    }

    /**
     * Create embedded GWT tomcat base dir based on properties.
     * 
     * @throws Exception
     */
    public void makeCatalinaBase() throws Exception {
        getLog().debug("make catalina base for embedded Tomcat");
        String[] args = { this.getTomcat().getAbsolutePath(), this.getWebXml().getAbsolutePath() };
        MakeCatalinaBase.main(args);

        if ((this.getContextXml() != null) && this.getContextXml().exists()) {
            FileUtils.copyFile(this.getContextXml(), new File(this.getTomcat(), "conf/gwt/localhost/ROOT.xml"));
        }
    }
}
