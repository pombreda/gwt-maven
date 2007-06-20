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
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
//import com.google.gwt.dev.GWTCompiler;

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
            CommandLineUtils.StringStreamConsumer stdout = new CommandLineUtils.StringStreamConsumer();
            CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

            try{
                this.getLog().info( "Running GWTCompile with command: "+cl.toString());
                int code = CommandLineUtils.executeCommandLine( cl, stdout, stderr );

                System.out.println( stdout.getOutput() );
                System.err.println( stderr.getOutput() );
                if( code != 0 ){
                    throw new MojoExecutionException( stderr.getOutput() );
                }
            }  catch (CommandLineException cle) {
                logErrorLines(stdout.getOutput());
                throw new MojoExecutionException("Error running GWT compiler", cle);
            } finally {
                getLog().debug(stdout.getOutput());
                getLog().debug(stderr.getOutput());
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