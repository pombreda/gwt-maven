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
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
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
            classpath = this.buildClasspath();
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
        Commandline cl = new Commandline();
        cl.setExecutable( JAVA_COMMAND );
        if( this.getExtraJvmArgs() != null ){
            String[] extraJvmArgs = { this.getExtraJvmArgs() };
            cl.addArguments( extraJvmArgs );
        }
        String[] args = {
            "-Dcatalina.base="+this.getTomcat().getAbsolutePath(),
            "-classpath", classpath,
            "com.google.gwt.dev.GWTShell", 
            "-logLevel", this.getLogLevel(),
            "-style", this.getStyle(),
            "-port", Integer.toString( this.getPort() ),
            "-out", this.getOutput().getAbsolutePath()
        };
        cl.addArguments( baseArgs );
        cl.addArguments( args );
        if( this.isNoServer() ){
            String[] ns = { "-noserver" };
            cl.addArguments(ns);
        }
        String[] runTarget = { this.getRunTarget() };
        cl.addArguments( runTarget );
        cl.setWorkingDirectory( this.getBuildDir().getAbsolutePath() );
        try{
            this.getLog().info( "Running GWT with command: "+cl.toString());
            CommandLineUtils.StringStreamConsumer stdout = new CommandLineUtils.StringStreamConsumer();
            CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();
                this.getLog().info( "Exited with code "+CommandLineUtils.executeCommandLine( cl, stdout, stderr ) );
        } catch(Exception pe){
            pe.printStackTrace();
        }
        
    }
    
    
}
