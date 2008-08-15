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
 */

package com.totsp.mavenplugin.gwt;

import java.io.File;
import java.util.Iterator;

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.totsp.mavenplugin.gwt.support.ExitException;
import com.totsp.mavenplugin.gwt.support.GwtWebInfProcessor;
import com.totsp.mavenplugin.gwt.util.FileIOUtils;

/**
 * @goal mergewebxml
 * @requiresDependencyResolution compile
 * @phase compile
 * 
 * @author cooper
 */
public class MergeWebXmlMojo extends AbstractGWTMojo {

   /** Creates a new instance of MergeWebXmlMojo */
   public MergeWebXmlMojo() {
      super();
   }

   public void execute() throws MojoExecutionException, MojoFailureException {

      // TODO
      //Setup the GWT Home using our auto-setup one, if one is not set
      /*
      if (getProject().getProperties().getProperty(GOOGLE_WEBTOOLKIT_HOME) == null) {

         File targetDir = null;
         try {
            targetDir = new File(getGwtBinOutputDirectory(), ArtifactNameUtil.guessArtifactId(this.getGwtVersion()) + "-" + getGwtVersion())
                     .getCanonicalFile();
            getProject().getProperties().setProperty(GOOGLE_WEBTOOLKIT_HOME, targetDir.getCanonicalPath());
            GWT_PATH = targetDir.getCanonicalPath();
         }
         catch (IOException e) {
            throw new MojoExecutionException(e.getMessage());
         }
      }
      */      
      
      try {
         File destination = new File(this.getBuildDir(), "web.xml");

         FileIOUtils.copyFile(this.getWebXml(), destination);
         
         for (int i = 0; i < this.getCompileTarget().length; i++) {
            File moduleFile = null;
            for (Iterator it = this.getProject().getCompileSourceRoots().iterator(); it.hasNext() && moduleFile == null;) {
               File check = new File(it.next().toString() + "/" + this.getCompileTarget()[i].replace('.', '/')
                        + ".gwt.xml");
               System.out.println("Looking for file: " + check.getAbsolutePath());
               if (check.exists()) {
                  moduleFile = check;
               }
            }
            for (Iterator it = this.getProject().getResources().iterator(); it.hasNext();) {
               Resource r = (Resource) it.next();
               File check = new File(r.getDirectory() + "/" + this.getCompileTarget()[i].replace('.', '/') + ".gwt.xml");
               System.out.println("Looking for file: " + check.getAbsolutePath());
               if (check.exists()) {
                  moduleFile = check;
               }
            }

            this.fixThreadClasspath();

            GwtWebInfProcessor processor = null;            
            try {
               if (moduleFile != null) {
                  System.out.println("Module file: " + moduleFile.getAbsolutePath());
                  processor = new GwtWebInfProcessor(this.getCompileTarget()[i], moduleFile, destination
                           .getAbsolutePath(), destination.getAbsolutePath());
               }
               else {
                  throw new MojoExecutionException("module file null");
               }
            }
            catch (ExitException e) {
               this.getLog().warn(e.getMessage());
               return;
            }
            processor.process();
         }
      }
      catch (Exception e) {
         throw new MojoExecutionException("Unable to merge web.xml", e);
      }
   }   
   
}
