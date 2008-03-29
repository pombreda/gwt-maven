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
 */

package com.totsp.mavenplugin.gwt;

import java.io.File;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import java.util.Locale;

/**
 * @goal compile
 * @phase compile
 * @requiresDependencyResolution compile
 * @author cooper
 */
public class CompileMojo extends AbstractGWTMojo{
    
    /** Creates a new instance of CompileMojo */
    public CompileMojo() {
        super();
    }
    
    public void execute() throws MojoExecutionException, MojoFailureException {
        if( !this.getOutput().exists() ){
            this.getOutput().mkdirs();
        }
        System.out.println("Override: "+System.getProperty("gwt.os", ""));
        if( System.getProperty("gwt.os", "").startsWith("windows") ||
                System.getProperty("os.name").toLowerCase(Locale.US).startsWith("windows") ){
            ScriptWriterWindows writer = new ScriptWriterWindows();
            try{
                File exec = writer.writeCompileScript(this);
                ProcessWatcher    pw = new ProcessWatcher("\""+exec.getAbsolutePath()+"\"");
                pw.startProcess(System.out, System.err);
                int retVal = pw.waitFor();
                if( retVal != 0 ){
                    throw new MojoFailureException("Compilation failed.");
                }
            } catch(Exception e){
                throw new MojoExecutionException("Exception attempting compile.", e);
            }
        } else {
            ScriptWriterUnix writer = new ScriptWriterUnix();
            try{
                File exec = writer.writeCompileScript(this);
                ProcessWatcher pw = new ProcessWatcher(exec.getAbsolutePath().replaceAll(" ", "\\ "));
                pw.startProcess(System.out, System.err);
                int retVal = pw.waitFor();
                if( retVal != 0 ){
                    throw new MojoFailureException("Compilation failed.");
                }
            } catch(Exception e){
                throw new MojoExecutionException("Exception attempting compile.", e);
            }
        }
    }
    /**
     *
     * @param alloutput output to pick out error lines to be printed from
     */
    private void logErrorLines(String alloutput) {
        final String errorTag = "[ERROR]";
        String[] lines = alloutput.split("\n");
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].contains(errorTag)) {
                getLog().error(lines[i].replace(errorTag, ""));
            }
        }
    }
    
    
}
