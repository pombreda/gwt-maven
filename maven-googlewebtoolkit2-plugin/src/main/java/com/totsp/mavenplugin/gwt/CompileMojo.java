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
import org.codehaus.plexus.util.cli.Commandline;

/**
 * @goal compile
 * @phase compile
 * @requiresDependencyResolution runtime
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
                classpath = this.buildClasspath();
            } catch(Exception e){
                throw new MojoExecutionException( "Unable to build classpath", e );
            }
            try{
                this.makeCatalinaBase();
            } catch(Exception e){
                throw new MojoExecutionException( "Unable to build catalina.base", e);
            }
            System.out.println( "Using classpath: "+ classpath );
            Commandline cl = new Commandline();
            cl.setExecutable( JAVA_COMMAND );
            if( this.getExtraJvmArgs() != null ){
                String[] extraJvmArgs = { this.getExtraJvmArgs() };
                cl.addArguments( extraJvmArgs );
            }
            String[] args = {
                "-classpath", classpath,
                "com.google.gwt.dev.GWTCompiler",
                "-logLevel", this.getLogLevel(),
                "-style", this.getStyle(),
                "-out", this.getOutput().getAbsolutePath(),
                compileTarget
            };
            cl.addArguments( args );
            cl.setWorkingDirectory( this.getBuildDir().getAbsolutePath() );
            try{
                this.getLog().info( "Running GWTCompile with command: "+cl.toString());
                CommandLineUtils.StringStreamConsumer stdout = new CommandLineUtils.StringStreamConsumer();
                CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();
                int code = CommandLineUtils.executeCommandLine( cl, stdout, stderr );
                
                System.out.println( stdout.getOutput() );
                System.err.println( stderr.getOutput() );
                if( code != 0 ){
                    throw new MojoExecutionException( stderr.getOutput() );
                }
            } catch(Exception pe){
                pe.printStackTrace();
            }
        }
    }
    
}
