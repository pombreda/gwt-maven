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
import java.io.PrintWriter;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.StringUtils;

import com.totsp.mavenplugin.gwt.AbstractGWTMojo;
import com.totsp.mavenplugin.gwt.support.util.DependencyScope;



/**
 * Abstract class for ScriptWriters - encapsulates code that till now was
 * written redundantly.
 * @author Marek Romanowski
 * @since 2008-12-27
 */
public abstract class AbstractScriptWriter implements ScriptWriter {
  
  
  
  protected String javaHome;
  
  
  
  public AbstractScriptWriter(String javaHome) {
    this.javaHome = javaHome;
  }
  
  
  
  /**
   * From base like "debug" makes runnable file name like "debug.sh" (unix)
   * or "debug.cmd" (windows).
   */
  protected abstract String getFileName(String base);
  
  
  
  /**
   * Returns extra arguments for JVM.
   */
  protected String getExtraArgs(AbstractGWTMojo mojo) {
    String extra = (mojo.getExtraJvmArgs() != null) ? mojo.getExtraJvmArgs() : "";
    if (AbstractGWTMojo.isMac && (extra.indexOf("-XstartOnFirstThread") == -1)) {
       extra = "-XstartOnFirstThread " + extra;
    }
    return extra;
  }
  
  
  
  /**
   * Returns string representing java command with extra JVM args and classpath
   * definition.
   */
  protected String getCommandWithExtraJvmArgs(AbstractGWTMojo mojo) {
    return "\"" + javaHome + "\" " + getExtraArgs(mojo) + " -cp $CLASSPATH ";
  }
  
  
  
  /**
   * This method returns string with common parameters passed to GWT scripts.
   */
  protected String getCommonParameters(AbstractGWTMojo mojo) {
    StringBuilder sb = new StringBuilder();
    
    sb.append(" -gen \"" + mojo.getGen().getAbsolutePath() + "\"");
    sb.append(" -logLevel " + mojo.getLogLevel());
    sb.append(" -style "+ mojo.getStyle());
    sb.append(" -out " + "\"" + mojo.getOutput().getAbsolutePath() + "\" ");
    
    return sb.toString();
  }
  
  
  
  /**
   * Write script using <code>scriptWriterPart</code> for generating main
   * part of the script.
   */
  protected File writeScript(AbstractGWTMojo mojo, File baseDir, String fileNameBase,
      DependencyScope dependencyScope, ScriptWriterPart scriptWriterPart
      ) throws MojoExecutionException {
    File file = new File(baseDir, getFileName(fileNameBase));
    PrintWriter writer = getPrintWriterWithClasspath(mojo, file, dependencyScope);

    scriptWriterPart.write(writer);
    
    writer.flush();
    writer.close();
    systemSpecificFileModifications(file);
    return file;
  }
  
  
  
  /**
   * Write script using buildDirectory as base for generated script.
   */
  protected File writeScript(AbstractGWTMojo mojo, String fileNameBase,
      DependencyScope dependencyScope, ScriptWriterPart scriptWriterPart
      ) throws MojoExecutionException {
    return writeScript(mojo, mojo.getBuildDir(), fileNameBase, dependencyScope, scriptWriterPart);
  }
  
  
  
  /**
   * Creates script for runnig shell.
   * @param debug <code>true</code> if you want to debug running app.
   */
  protected File writeRunnableScript(final AbstractGWTMojo mojo, 
      String fileNameBase, final boolean debug) throws MojoExecutionException {
    return writeScript(mojo, fileNameBase, DependencyScope.RUNTIME, 
        new ScriptWriterPart() {
          public void write(PrintWriter writer) {
            writer.print(getCommandWithExtraJvmArgs(mojo));
            
            if (debug) {
              writer.print(" -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,address=");
              writer.print(mojo.getDebugPort());
              writer.print(mojo.isDebugSuspend() ? ",suspend=y " : ",suspend=n ");
            }
            
            writer.print("-Dcatalina.base=\"" + mojo.getTomcat().getAbsolutePath() + "\" ");
            writer.print(" com.google.gwt.dev.GWTShell");
            writer.print(getCommonParameters(mojo));
            writer.print(" -port " + Integer.toString(mojo.getPort()));
            
            if (mojo.isNoServer()) {
              writer.print(" -noserver ");
            }
            
            writer.print(" " + mojo.getRunTarget());
            writer.println();
          }
    });
  }
  
  
  
  /**
   * Creates run script. See gwt docs.
   */
  public final File writeRunScript(AbstractGWTMojo mojo) throws MojoExecutionException {
    return writeRunnableScript(mojo, "run", false);
  }
  
  
  
  /**
   * Creates debug script. See gwt docs.
   */
  public final File writeDebugScript(AbstractGWTMojo mojo) throws MojoExecutionException {
    return writeRunnableScript(mojo, "debug", true);
  }
  
  
  
  /**
   * Creates compile script. See gwt docs.
   */
  public final File writeCompileScript(
      final AbstractGWTMojo mojo) throws MojoExecutionException {
    return writeScript(mojo, "compile", DependencyScope.COMPILE, new ScriptWriterPart() {
      public void write(PrintWriter writer) {
        for (String target : mojo.getCompileTarget()) {

          writer.print(getCommandWithExtraJvmArgs(mojo));
          writer.print(" com.google.gwt.dev.GWTCompiler ");
          writer.print(getCommonParameters(mojo));

          if (mojo.isEnableAssertions()) {
             writer.print(" -ea ");
          }

          writer.println(mojo.getOutput().getAbsolutePath() + " " + target);
       }
      }
    });
  }

  
  
  /**
   * Creates script for i18n. See gwt docs.
   */
  public final File writeI18nScript(
      final AbstractGWTMojo mojo) throws MojoExecutionException {
    return writeScript(mojo, "i18n", DependencyScope.COMPILE, new ScriptWriterPart() {
      public void write(PrintWriter writer) {
        // constants
        createFiles(writer, mojo.getI18nConstantsNames(), "");
        // constants with lookup
        createFiles(writer, mojo.getI18nConstantsWithLookupNames(), " -createConstantsWithLookup ");
        // messages
        createFiles(writer, mojo.getI18nMessagesNames(), " -createMessages ");
      }
      
      
      
      private void createFiles(PrintWriter writer, String[] names, String param) {
        if (names != null) {
          for (String target : names) {
            writer.print(getI18NSyncCommand(mojo, target, param));
          }
        }
      }
    });
  }
  
  
  
  /**
   * Returns string declaring command i18n.
   */
  protected String getI18NSyncCommand(
      AbstractGWTMojo mojo, String target, String additionalParams) {
    StringBuilder sb = new StringBuilder();
    
    sb.append(getCommandWithExtraJvmArgs(mojo));

    sb.append(" com.google.gwt.i18n.tools.I18NSync")
      .append(additionalParams)
      .append(" -out ").append(mojo.getI18nOutputDir())
      .append(" ").append(target).append("\n");

     return sb.toString();
  }

  
  
  /**
   * Creates script for running all GwtTests.
   */
  @SuppressWarnings("unchecked")
  public final void writeTestScripts(AbstractGWTMojo mojo) throws MojoExecutionException {

    // get extras
    final String extra = getExtraArgs(mojo) + ((mojo.getExtraTestArgs() != null) ? mojo.getExtraTestArgs() : "");

    // make sure output dir is present
    File outputDir = new File(mojo.getBuildDir(), "gwtTest");
    outputDir.mkdirs();

    // for each test compile source root, build a test script
    List<String> testCompileRoots = mojo.getProject().getTestCompileSourceRoots();
    for (String root : testCompileRoots) {

       Collection<File> coll = FileUtils.listFiles(
           new File(root), 
           new WildcardFileFilter(mojo.getTestFilter()), 
           HiddenFileFilter.VISIBLE);

       for (File testFile : coll) {

          String tmpName = testFile.toString();
          mojo.getLog().debug(("gwtTest test match found (after filter applied) - " + tmpName));

          // parse off the extension
          if (tmpName.lastIndexOf('.') > tmpName.lastIndexOf(File.separatorChar)) {
            tmpName = tmpName.substring(0, tmpName.lastIndexOf('.'));
          }
          if (tmpName.startsWith(root+File.separator)) {
            tmpName = tmpName.substring(root.length()+1);
          }
          final String testName = 
            StringUtils.replace(tmpName, File.separatorChar, '.');
          
          mojo.getLog().debug("testName after parsing - " + testName);

          // start script inside gwtTest output dir, and name it with test class name
          writeScript(mojo, outputDir, "gwtTest-" + testName, 
              DependencyScope.TEST, new ScriptWriterPart() {
            public void write(PrintWriter writer) {
              // build Java command
              writer.print("\"" + javaHome + "\" ");
              writer.print(" " + extra + " ");
              writer.print(" -cp $CLASSPATH ");
              writer.print("junit.textui.TestRunner ");
              writer.print(testName);
            }
          });
       }
    }
  }
  
  
  
  /**
   * Do things on resulting file that are specific for operating system.
   * Do nothing default. 
   */
  protected void systemSpecificFileModifications(File file) {
    // defult: do nothing
  }



  /**
   * Get PrintWriter for script file. This method not only creates file
   * and writer, but additionally writes to script system-specific
   * prologue (like "#!/bin/bash" for linux).
   */
  protected abstract PrintWriter getPrintWriterWithClasspath(
      AbstractGWTMojo mojo, File file, DependencyScope runtime
      ) throws MojoExecutionException;
  
  
  
  /**
   * Simple interface for classes that write something to {@link PrintWriter}.
   * Used in many places in this class.
   * @author Marek Romanowski
   * @since 2008-12-28
   */
  static interface ScriptWriterPart {
    void write(PrintWriter writer);
  }
}



