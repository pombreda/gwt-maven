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

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.MojoExecutionException;

import com.totsp.mavenplugin.gwt.AbstractGWTMojo;
import com.totsp.mavenplugin.gwt.support.util.DependencyScope;
import com.totsp.mavenplugin.gwt.util.BuildClasspathUtil;



/**
 * Handler for writing shell scripts for the mac and linux platforms.
 * 
 * @author ccollins
 * @author rcooper
 * @author Marek Romanowski
 */
public class ScriptWriterUnix extends AbstractScriptWriter {

  
  
  /**
   * @param javaHome
   */
  public ScriptWriterUnix(String javaHome) {
    super(javaHome);
  }



  /**
   * Util to get a PrintWriter with Unix preamble and classpath.
   */
  protected PrintWriter getPrintWriterWithClasspath(final AbstractGWTMojo mojo,
      File file, final DependencyScope scope) throws MojoExecutionException {

    // create writer (and file too!)
    PrintWriter writer = null;
    try {
      writer = new PrintWriter(new FileWriter(file));
    } catch (IOException e) {
      throw new MojoExecutionException("Error creating script - " + file, e);
    }

    // shell selection
    File sh = new File("/bin/bash");
    if (!sh.exists()) {
      sh = new File("/usr/bin/bash");
    }
    if (!sh.exists()) {
      sh = new File("/bin/sh");
    }

    // write shell info to file
    writer.println("#!" + sh.getAbsolutePath());
    writer.println();

    StringBuilder sb = new StringBuilder();
    try {
      sb.append("export CLASSPATH=");
      for (File f : BuildClasspathUtil.buildClasspathList(mojo, scope)) {
        sb.append("\"" + f.getAbsolutePath() + "\":");
      }
      sb.deleteCharAt(sb.length()-1);
    } catch (DependencyResolutionRequiredException e) {
      throw new MojoExecutionException("Error creating script - " + file, e);
    }
    writer.print(sb.toString());
    writer.println();
    writer.println();
    return writer;
  }



  /**
   * Util to chmod Unix file.
   */
  @Override
  protected void systemSpecificFileModifications(File file) {
    try {
      ProcessWatcher pw = new ProcessWatcher(
          "chmod +x " + file.getAbsolutePath());
      pw.startProcess(System.out, System.err);
      pw.waitFor();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }



  @Override
  protected String getFileName(String base) {
    return base + ".sh";
  }
}


