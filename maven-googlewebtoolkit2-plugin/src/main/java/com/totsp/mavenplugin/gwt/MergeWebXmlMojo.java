/*
 * MergeWebXmlMojo.java
 *
 * Created on January 13, 2007, 7:45 PM
 *
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.totsp.mavenplugin.gwt.support.ExitException;
import com.totsp.mavenplugin.gwt.support.GwtWebInfProcessor;
import com.totsp.mavenplugin.gwt.support.util.FileIOUtils;

/**
 * Merges GWT servlet elements into deployment descriptor (and non GWT servlets into shell).
 * Things done:
 * <ol>
 *   <li>Copy web.xml file from <code>${webXml}</code> to <code>buildDir</code>.</li>
 *   <li>For each <code>moduleName</code> from <code>${compileTargets}</code> do:
 *   <p>
 *   <ol>
 *     <li>Search for GWT module descriptor (<code>moduleName.gwt.xml</code>).
 *     Searching in all compile source roots and resources dirs of project.</li>
 *   </ol>
 * </ol>
 * 
 * @goal mergewebxml
 * @phase process-resources
 * @requiresDependencyResolution compile
 * @description Merges GWT servlet elements into deployment descriptor 
 * (and non GWT servlets into shell).
 * @author cooper
 */
public class MergeWebXmlMojo extends AbstractGWTMojo {

  
  
  /** Creates a new instance of MergeWebXmlMojo */
  public MergeWebXmlMojo() {
      super();
  }

  
  
  public void execute() throws MojoExecutionException, MojoFailureException {
    try {
      this.getLog().info("copy source web.xml - " + this.getWebXml()
                  + " to build dir (source web.xml required if mergewebxml execution is enabled)");
      File destination = new File(this.getBuildDir(), "web.xml");
      if (!destination.exists()) {
        destination.getParentFile().mkdirs();
        destination.createNewFile();
      }

      FileIOUtils.copyFile(this.getWebXml(), destination);

      for (String compileTarget : getCompileTarget()) {
        final String moduleFileName = "/" + compileTarget.replace('.', '/') + ".gwt.xml";
        final List<File> moduleFiles = new ArrayList<File>();
        
        traverseeAllCompileSourceRootsAndResourcesDirectories(new IFileExecution() {
          public void executeForFile(File file) {
            File check = new File(file, moduleFileName);
            getLog().debug("Looking for file: " + check.getAbsolutePath());
            if (check.exists()) {
              moduleFiles.add(check);
            }
          }
        });

        // (o) change thread classloader
        fixThreadClasspath();

        final File moduleFile = moduleFiles.size() > 0 ? moduleFiles.get(0) : null;
        GwtWebInfProcessor processor = null;
        try {
          if (moduleFile != null) {
            getLog().info("Module file: " + moduleFile.getAbsolutePath());
            processor = new GwtWebInfProcessor(compileTarget, moduleFile,
                destination.getAbsolutePath(), destination.getAbsolutePath(),
                this.isWebXmlServletPathAsIs());
            processor.process();
          } else {
            throw new MojoExecutionException("module file null");
          }
        } catch (ExitException e) {
          this.getLog().info(e.getMessage());
          // return;
        }

      }
    } catch (Exception e) {
      throw new MojoExecutionException("Unable to merge web.xml", e);
    }
  }
}


