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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.commons.lang.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.io.IOException;

/**
 *
 * @author cooper
 * @goal gwt
 * @requiresDependencyResolution runtime
 * @description Runs the the project in the GWT Development Shell.
 * @execute phase=package
 */
public class GWTMojo extends AbstractGWTMojo {
    
    String[] baseArgs = new String[0];
    
    /** Creates a new instance of GWTMojo */
    public GWTMojo() {
        super();
    }
    
    public void execute() throws MojoExecutionException, MojoFailureException {
        String classpath = null;
        try{
            classpath = this.buildClasspath(true);
        } catch(Exception e){
            throw new MojoExecutionException( "Unable to build classpath", e );
        }
        try{
            this.makeCatalinaBase();
        } catch(Exception e){
            throw new MojoExecutionException( "Unable to build catalina.base", e);
        }
        if( !this.getOutput().exists() ){
            this.getOutput().mkdirs();
        }
        System.out.println( "Using classpath: "+ classpath );

        ArrayList<String> argList = new ArrayList<String>();
        argList.add(JAVA_COMMAND);

        argList.addAll(Arrays.asList(baseArgs));

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

      if (System.getProperty( "os.name" ).toLowerCase( Locale.US ).startsWith("mac")) {
        argList.add("-XstartOnFirstThread");
      }



        argList.add("-cp");
        argList.add(classpath);
        argList.add("-Dcatalina.base="+this.getTomcat().getAbsolutePath());
        argList.add("com.google.gwt.dev.GWTShell");
        argList.add("-gen");
        argList.add(".generated");
        argList.add("-logLevel");
        argList.add(this.getLogLevel());
        argList.add("-style");
        argList.add(this.getStyle());
        argList.add("-out");
        argList.add(this.getOutput().getAbsolutePath());
        argList.add("-port");
        argList.add(Integer.toString( this.getPort() ));

        if( this.isNoServer() ){
          argList.add("-noserver");
        }

        argList.add(this.getRunTarget());

        this.getLog().info(StringUtils.join(argList, ' '));
        
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
