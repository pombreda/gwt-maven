/*
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
 * Handler for writing cmd scripts for the windows platform.
 * 
 * TODO - make an abstract script writer so we can remove dupe code and handle calling for diff platforms better
 *
 * @author ccollins
 * @author rcooper
 */
public class ScriptWriterWindows {

   public ScriptWriterWindows() {
   }

   public File writeRunScript(AbstractGWTMojo mojo) throws IOException, DependencyResolutionRequiredException,
            MojoExecutionException {
      String filename = (mojo instanceof DebugMojo) ? "debug.cmd" : "run.cmd";
      File file = new File(mojo.getBuildDir(), filename);
      PrintWriter writer = new PrintWriter(new FileWriter(file));
      Collection<File> classpath = BuildClasspathUtil.buildRuntimeClasspathList(mojo);
      writer.print("set CLASSPATH=");

      StringBuffer cpString = new StringBuffer();

      for (File f : classpath) {
         cpString.append("\"" + f.getAbsolutePath() + "\";");
         //break the line at 4000 characters to avoid max size.
         if (cpString.length() > 4000) {
            writer.println(cpString);
            cpString = new StringBuffer();
            writer.print("set CLASSPATH=%CLASSPATH%;");
         }
      }
      writer.println(cpString);

      writer.println();

      String extra = (mojo.getExtraJvmArgs() != null) ? mojo.getExtraJvmArgs() : "";
      writer.print("\"" + AbstractGWTMojo.JAVA_COMMAND + "\" " + extra + " -cp %CLASSPATH% ");

      if (mojo instanceof DebugMojo) {
         writer.print(" -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,address=");
         writer.print(mojo.getDebugPort());
         writer.print(",suspend=y ");
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

      return file;
   }

   public File writeCompileScript(AbstractGWTMojo mojo) throws IOException, DependencyResolutionRequiredException,
            MojoExecutionException {
      File file = new File(mojo.getBuildDir(), "compile.cmd");
      PrintWriter writer = new PrintWriter(new FileWriter(file));
      Collection<File> classpath = BuildClasspathUtil.buildClasspathList(mojo, false);
      writer.print("set CLASSPATH=");

      StringBuffer cpString = new StringBuffer();

      for (File f : classpath) {
         cpString.append("\"" + f.getAbsolutePath() + "\";");

         //break the line at 4000 characters to avout max size.
         if (cpString.length() > 4000) {
            writer.println(cpString);
            cpString = new StringBuffer();
            writer.print("set CLASSPATH=%CLASSPATH%;");
         }
      }
      writer.println(cpString);
      writer.println();

      for (String target : mojo.getCompileTarget()) {
         String extra = (mojo.getExtraJvmArgs() != null) ? mojo.getExtraJvmArgs() : "";
         writer.print("\"" + AbstractGWTMojo.JAVA_COMMAND + "\" " + extra + " -cp %CLASSPATH% ");
         writer.print(" com.google.gwt.dev.GWTCompiler ");
         writer.print(" -gen \"");
         writer.print(mojo.getGen().getAbsolutePath());
         writer.print("\" -logLevel ");
         writer.print(mojo.getLogLevel());
         writer.print(" -style ");
         writer.print(mojo.getStyle());

         writer.print(" -out ");
         writer.print(mojo.getOutput().getAbsolutePath());
         writer.print(" ");

         if (mojo.isEnableAssertions()) {
            writer.print(" -ea ");
         }

         writer.print(target);
         writer.println();        
      }

      writer.flush();
      writer.close();

      return file;
   }

   public File writeI18nScript(AbstractGWTMojo mojo) throws IOException, DependencyResolutionRequiredException,
            MojoExecutionException {
      File file = new File(mojo.getBuildDir(), "i18n.cmd");
      PrintWriter writer = new PrintWriter(new FileWriter(file));
      Collection<File> classpath = BuildClasspathUtil.buildClasspathList(mojo, false);
      writer.print("set CLASSPATH=");

      StringBuffer cpString = new StringBuffer();

      for (File f : classpath) {
         cpString.append("\"" + f.getAbsolutePath() + "\";");

         //break the line at 4000 characters to avout max size.
         if (cpString.length() > 4000) {
            writer.println(cpString);
            cpString = new StringBuffer();
            writer.print("set CLASSPATH=%CLASSPATH%;");
         }
      }
      writer.println(cpString);
      writer.println();

      // constants
      if (mojo.getI18nConstantsNames() != null) {
         for (String target : mojo.getI18nConstantsNames()) {
            String extra = (mojo.getExtraJvmArgs() != null) ? mojo.getExtraJvmArgs() : "";

            writer.print("\"" + AbstractGWTMojo.JAVA_COMMAND + "\" " + extra + " -cp %CLASSPATH%");
            writer.print(" com.google.gwt.i18n.tools.I18NSync");
            writer.print(" -out ");
            writer.print(mojo.getI18nOutputDir());
            writer.print(" ");
            writer.print(target);
            writer.println();
         }
      }

      // messages
      if (mojo.getI18nMessagesNames() != null) {
         for (String target : mojo.getI18nMessagesNames()) {
            String extra = (mojo.getExtraJvmArgs() != null) ? mojo.getExtraJvmArgs() : "";

            writer.print("\"" + AbstractGWTMojo.JAVA_COMMAND + "\" " + extra + " -cp %CLASSPATH%");
            writer.print(" com.google.gwt.i18n.tools.I18NSync");
            writer.print(" -createMessages ");
            writer.print(" -out ");
            writer.print(mojo.getI18nOutputDir());
            writer.print(" ");
            writer.print(target);
            writer.println();
         }
      }

      writer.flush();
      writer.close();

      return file;
   }
}
