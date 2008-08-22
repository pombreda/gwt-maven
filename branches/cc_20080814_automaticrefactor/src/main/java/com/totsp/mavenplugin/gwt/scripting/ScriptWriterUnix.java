/*
 * ScriptWriterUnix.java
 *
 * Created on November 23, 2007, 12:18 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.totsp.mavenplugin.gwt.scripting;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.MojoExecutionException;

import com.totsp.mavenplugin.gwt.AbstractGWTMojo;
import com.totsp.mavenplugin.gwt.DebugMojo;
import com.totsp.mavenplugin.gwt.util.BuildClasspathUtil;

/**
 * Handler for writing shell scripts for the mac and linux platforms.
 *
 * @author rcooper
 */
public class ScriptWriterUnix {
   /** Creates a new instance of ScriptWriterUnix */
   public ScriptWriterUnix() {
   }

   public File writeRunScript(AbstractGWTMojo mojo) throws IOException, DependencyResolutionRequiredException, MojoExecutionException {
      String filename = (mojo instanceof DebugMojo) ? "debug.sh" : "run.sh";
      File file = new File(mojo.getBuildDir(), filename);
      PrintWriter writer = new PrintWriter(new FileWriter(file));
      Collection<File> classpath = BuildClasspathUtil.buildRuntimeClasspathList(mojo);
      File sh = new File("/bin/bash");

      if (!sh.exists()) {
         sh = new File("/usr/bin/bash");
      }

      if (!sh.exists()) {
         sh = new File("/bin/sh");
      }

      writer.println("#!" + sh.getAbsolutePath());
      writer.print("export CLASSPATH=");

      for (File f : classpath) {
         writer.print("\"" + f.getAbsolutePath() + "\":");
      }

      writer.println();

      String extra = (mojo.getExtraJvmArgs() != null) ? mojo.getExtraJvmArgs() : "";

      if (AbstractGWTMojo.OS_NAME.startsWith("mac")
               && (extra.indexOf("-XstartOnFirstThread") == -1)) {
         extra = "-XstartOnFirstThread " + extra;
      }

      writer.print("\"" + AbstractGWTMojo.JAVA_COMMAND + "\" " + extra + " -cp $CLASSPATH ");

      if (mojo instanceof DebugMojo) {
         writer.print(" -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,address=");
         writer.print(mojo.getDebugPort());
         writer.print(mojo.isDebugSuspend() ? ",suspend=y " : ",suspend=n ");
      }

      writer.print("-Dcatalina.base=\"" + mojo.getTomcat().getAbsolutePath() + "\" ");
      writer.print(" com.google.gwt.dev.GWTShell");
      writer.print(" -gen \"");
      writer.print(mojo.getGen().getAbsolutePath());
      writer.print("\" -logLevel ");
      writer.print(mojo.getLogLevel());
      writer.print(" -style ");
      writer.print(mojo.getStyle());
      writer.print(" -out ");
      writer.print("\"" + mojo.getOutput().getAbsolutePath() + "\"");
      writer.print(" -port ");
      writer.print(Integer.toString(mojo.getPort()));

      if (mojo.isNoServer()) {
         writer.print(" -noserver ");
      }

      writer.print(" " + mojo.getRunTarget());
      writer.println();

      writer.flush();
      writer.close();

      try {
         ProcessWatcher pw = new ProcessWatcher("chmod +x " + file.getAbsolutePath());
         pw.startProcess(System.out, System.err);
         pw.waitFor();
      }
      catch (Exception e) {
         throw new RuntimeException(e);
      }

      return file;
   }

   public File writeCompileScript(AbstractGWTMojo mojo) throws IOException, DependencyResolutionRequiredException, MojoExecutionException {
      File file = new File(mojo.getBuildDir(), "compile.sh");
      PrintWriter writer = new PrintWriter(new FileWriter(file));
      File sh = new File("/bin/bash");

      if (!sh.exists()) {
         sh = new File("/usr/bin/bash");
      }

      if (!sh.exists()) {
         sh = new File("/bin/sh");
      }

      writer.println("#!" + sh.getAbsolutePath());

      Collection<File> classpath = BuildClasspathUtil.buildClasspathList(mojo, false);
      writer.print("export CLASSPATH=");

      for (File f : classpath) {
         writer.print("\"" + f.getAbsolutePath() + "\":");
      }

      writer.println();

      for (String target : mojo.getCompileTarget()) {
         String extra = (mojo.getExtraJvmArgs() != null) ? mojo.getExtraJvmArgs() : "";

         if (AbstractGWTMojo.OS_NAME.startsWith("mac")
                  && (extra.indexOf("-XstartOnFirstThread") == -1)) {
            extra = "-XstartOnFirstThread " + extra;
         }

         writer.print("\"" + AbstractGWTMojo.JAVA_COMMAND + "\" " + extra + " -cp $CLASSPATH ");
         writer.print(" com.google.gwt.dev.GWTCompiler ");
         writer.print(" -gen ");
         writer.print(mojo.getGen().getAbsolutePath());
         writer.print(" -logLevel ");
         writer.print(mojo.getLogLevel());
         writer.print(" -style ");
         writer.print(mojo.getStyle());
         writer.print(" -out ");

         if (mojo.isEnableAssertions()) {
            writer.print(" -ea ");
         }

         writer.print(mojo.getOutput().getAbsolutePath());
         writer.print(" ");
         writer.print(target);
         writer.println();

         // if gwtVersion is NOT 1.3 or 1.4, then assume it is 1.5 or higher, and move "std" and "xs" linker directories to target
         if (!(mojo.getGwtVersion().startsWith("1.3.") || mojo.getGwtVersion().startsWith("1.4."))) {
            //TODO change this to inspect linker output
            String std = mojo.getOutput().getAbsolutePath() + "/" + target + "/std";
            writer.println("mv " + std + "/* " + mojo.getOutput().getAbsolutePath() + "/" + target);
            writer.println("rm -rf " + std);

            String xs = mojo.getOutput().getAbsolutePath() + "/" + target + "/xs";
            writer.println("mv " + xs + "/* " + mojo.getOutput().getAbsolutePath() + "/" + target);
            writer.println("rm -rf " + xs);
         }
      }

      writer.flush();
      writer.close();

      try {
         ProcessWatcher pw = new ProcessWatcher("chmod +x " + file.getAbsolutePath());
         pw.startProcess(System.out, System.err);
         pw.waitFor();
      }
      catch (Exception e) {
         throw new RuntimeException(e);
      }

      return file;
   }
}
