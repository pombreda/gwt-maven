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
import com.totsp.mavenplugin.gwt.support.util.DependencyScope;
import com.totsp.mavenplugin.gwt.util.BuildClasspathUtil;



/**
 * Handler for writing cmd scripts for the windows platform.
 * 
 * @author ccollins
 * @author rcooper
 * @author Marek Romanowski
 */
public class ScriptWriterWindows extends AbstractScriptWriter {

  
  
  /**
   * @param javaHome
   */
  public ScriptWriterWindows(String javaHome) {
    super(javaHome);
  }

  
  
  /**
   * Util to get a PrintWriter with Windows preamble.
   */
  protected PrintWriter getPrintWriterWithClasspath(final AbstractGWTMojo mojo,
      File file, final DependencyScope scope) throws MojoExecutionException {

    PrintWriter writer = null;
    try {
      writer = new PrintWriter(new FileWriter(file));
      writer.println("@echo off");
    } catch (IOException e) {
      throw new MojoExecutionException("Error creating script - " + file, e);
    }

    try {
      Collection<File> classpath = 
        BuildClasspathUtil.buildClasspathList(mojo, scope);
      writer.print("set CLASSPATH=");

      StringBuilder sb = new StringBuilder();

      for (File f : classpath) {
        sb.append("\"" + f.getAbsolutePath() + "\";");
        // break the line at 4000 characters to try to avoid max size
        if (sb.length() > 4000) {
          writer.println(sb);
          sb = new StringBuilder();
          writer.print("set CLASSPATH=%CLASSPATH%;");
        }
      }
      writer.println(sb);
      writer.println();
    } catch (DependencyResolutionRequiredException e) {
      throw new MojoExecutionException("Error creating script - " + file, e);
    }

    writer.println();
    return writer;
  }



  @Override
  protected String getFileName(String base) {
    return base + ".cmd";
  }
}


