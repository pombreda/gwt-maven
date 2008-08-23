/*
 * CompileMojo.java
 *
 * Created on January 13, 2007, 11:42 AM
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
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 * 
 */
package com.totsp.mavenplugin.gwt;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.totsp.mavenplugin.gwt.scripting.ProcessWatcher;
import com.totsp.mavenplugin.gwt.scripting.ScriptWriterUnix;
import com.totsp.mavenplugin.gwt.scripting.ScriptWriterWindows;

/**
 * @goal compile
 * @phase compile
 * @requiresDependencyResolution compile
 * @description Invokes the GWTCompiler for the project source.
 * 
 * @author cooper
 * @author ccollins
 */
public class CompileMojo extends AbstractGWTMojo {

    /** Creates a new instance of CompileMojo */
    public CompileMojo() {
        super();
    }

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!this.getOutput().exists()) {
            this.getOutput().mkdirs();
        }

        if (System.getProperty("gwt.os", "").startsWith(WINDOWS) || AbstractGWTMojo.OS_NAME.startsWith(WINDOWS)) {
            ScriptWriterWindows writer = new ScriptWriterWindows();
            try {
                File exec = writer.writeCompileScript(this);
                ProcessWatcher pw = new ProcessWatcher("\"" + exec.getAbsolutePath() + "\"");
                pw.startProcess(System.out, System.err);
                int retVal = pw.waitFor();
                if (retVal != 0) {
                    throw new MojoFailureException("compile script exited abormally with code - " + retVal);
                }
            } catch (Exception e) {
                throw new MojoExecutionException("Exception attempting compile.", e);
            }
        } else {
            ScriptWriterUnix writer = new ScriptWriterUnix();
            try {
                File exec = writer.writeCompileScript(this);
                ProcessWatcher pw = new ProcessWatcher(exec.getAbsolutePath().replaceAll(" ", "\\ "));
                pw.startProcess(System.out, System.err);
                int retVal = pw.waitFor();
                if (retVal != 0) {
                    throw new MojoFailureException("compile script exited abormally with code - " + retVal);
                }
            } catch (Exception e) {
                throw new MojoExecutionException("Exception attempting compile.", e);
            }
        }
    }
}
