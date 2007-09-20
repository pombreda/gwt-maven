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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.CommandLineException;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.ArrayList;
import java.io.IOException;

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
        for( int i=0; i < this.getCompileTarget().length; i++){
            String compileTarget = this.getCompileTarget()[i];
            String classpath = null;
            try{
                classpath = this.buildClasspath(false);
            } catch(Exception e){
                throw new MojoExecutionException( "Unable to build classpath", e );
            }
            try{
                this.makeCatalinaBase();
            } catch(Exception e){
                throw new MojoExecutionException( "Unable to build catalina.base", e);
            }


          if (!isFork()) {
            String[] args = {
                "-logLevel", this.getLogLevel(),
                "-style", this.getStyle(),
                "-out", this.getOutput().getAbsolutePath(),
                compileTarget
            };

            this.fixThreadClasspath();

            try {
              Class compilerClass = Thread.currentThread().getContextClassLoader().loadClass("com.google.gwt.dev.GWTCompiler");
              Method m = compilerClass.getMethod("main", args.getClass());
              m.invoke(null, new Object[] {args});
            } catch (ClassNotFoundException e) {
              System.out.println("ERROR:  Could not find GWTCompiler.  Make sure your classpath includes the the GWT dev jars");
            } catch (NoSuchMethodException e) {
              System.out.println("ERROR:  Could not find the main method on GWTCompiler.  You must be using a newer version of GWT than the plugin expects.  Send this error to the gwt-maven group, and get is fixed.");
            } catch (IllegalAccessException e) {
              System.out.println("ERROR:  Problem calling the main method on GWTCompiler.  Send this error to the gwt-maven group, and get is fixed.");
            } catch (InvocationTargetException e) {
              System.out.println("ERROR:  Problem calling the main method on GWTCompiler.  Send this error to the gwt-maven group, and get is fixed.");
            }
            //GWTCompiler.main(args);
          } else {

            ArrayList<String> argList = new ArrayList<String>();
            argList.add(JAVA_COMMAND);
            if( this.getExtraJvmArgs() == null ){
                this.setExtraJvmArgs( EXTA_ARG );
            }
            if( this.getExtraJvmArgs() != null ){
              try {
                String[] extraJvmArgs = CommandLineUtils.translateCommandline(this.getExtraJvmArgs());
                argList.addAll(Arrays.asList(extraJvmArgs));
              } catch (Exception e) {
                e.printStackTrace();
              }
            }

            argList.add("-cp");
            argList.add(classpath);

            argList.add("com.google.gwt.dev.GWTCompiler");
            argList.add("-gen");
            argList.add(".generated");
            argList.add("-logLevel");
            argList.add(this.getLogLevel());
            argList.add("-style");
            argList.add(this.getStyle());
            argList.add("-out");
            argList.add(this.getOutput().getAbsolutePath());
            argList.add(compileTarget);


            try {
              ProcessWatcher    pw = new ProcessWatcher(argList.toArray(new String[0]), null, this.getBuildDir().getCanonicalFile());
              pw.startProcess(System.out, System.err);
              int retVal = pw.waitFor();
            } catch (IOException ioe) {
              ioe.printStackTrace();
            } catch (InterruptedException ie) {
              Thread.currentThread().interrupt();
            }            
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
