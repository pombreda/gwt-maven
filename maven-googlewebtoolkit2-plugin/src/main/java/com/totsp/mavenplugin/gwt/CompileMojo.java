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
 * 
 */
package com.totsp.mavenplugin.gwt;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.totsp.mavenplugin.gwt.scripting.ScriptUtil;
import com.totsp.mavenplugin.gwt.scripting.ScriptWriter;
import com.totsp.mavenplugin.gwt.scripting.ScriptWriterFactory;



/**
 * Invokes the GWTCompiler for the project source.
 * 
 * @goal compile
 * @phase process-classes
 * @execute phase="compile"
 * @execute goal="mergewebxml"
 * @requiresDependencyResolution compile
 * @description Invokes the GWTCompiler for the project source.
 * 
 * @author cooper
 * @author ccollins
 * @author Marek Romanowski
 */
public class CompileMojo extends AbstractGWTMojo {



  public void execute() throws MojoExecutionException, MojoFailureException {
    if (!this.getOutput().exists()) {
      this.getOutput().mkdirs();
    }

    Long lastChangeTimeMarker = sourcesChanged();
    if ((lastChangeTimeMarker != null) || isForceCompile()) {
      // build it for the correct platform
      ScriptWriter writer = ScriptWriterFactory.getInstance(getJavaCommand());
      File exec = writer.writeCompileScript(this);

      // run it
      if (ScriptUtil.runScript(exec, false) != 0) {
        throw new MojoExecutionException("GWT compilation failed - " +
        		"see GWTCompiler logs for more information");
      } else {
        saveCompilationTimeMarker(lastChangeTimeMarker);
      }
      
    } else {
      getLog().info("No changes found - does not compile");
    }
  }
}


