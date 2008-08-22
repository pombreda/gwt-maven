/*
 * I18NMojo.java
 *
 * Created on August 19th, 2008
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
import java.util.Collection;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.totsp.mavenplugin.gwt.scripting.ProcessWatcher;
import com.totsp.mavenplugin.gwt.util.BuildClasspathUtil;

/**
 * @goal i18n
 * @requiresDependencyResolution compile
 * @phase compile
 * @author Sascha-Matthias Kulawik <sascha@kulawik.de>
 */
public class I18NMojo extends AbstractGWTMojo {
   /**
    * @parameter expression="${basedir}/src/main/java/"
    */
   private File i18nOutputDir;
   /**
    * @parameter 
    */
   private String[] i18nConstantsNames;

   public File getI18nOutputDir() {
      return i18nOutputDir;
   }

   public void setI18nOutputDir(File outputDir) {
      i18nOutputDir = outputDir;
   }

   public String[] getI18nConstantsNames() {
      return i18nConstantsNames;
   }

   public void setI18nConstantsNames(String[] constantsName) {
      i18nConstantsNames = constantsName;
   }

   /** Creates a new instance of I18NMojo */
   public I18NMojo() {
      super();
   }

   public void execute() throws MojoExecutionException, MojoFailureException {
      if (!this.getI18nOutputDir().exists()) {
         if (getLog().isInfoEnabled())
            getLog().info("I18NModule is creating target directory " + getI18nOutputDir().getAbsolutePath());
         this.getI18nOutputDir().mkdirs();
      }

      String command = AbstractGWTMojo.JAVA_COMMAND + " -cp \"";
      Collection<File> classpath;
      try {
         classpath = BuildClasspathUtil.buildClasspathList(this, false);
      }
      catch (DependencyResolutionRequiredException e1) {
         e1.printStackTrace();
         throw new MojoFailureException("Dependency Resolution failed");
      }
      boolean first = true;
      for (File f : classpath) {
         if (!first) {
            command += File.pathSeparator;
         }
         first = false;
         command += f.getAbsolutePath();
      }
      command += "\" com.google.gwt.i18n.tools.I18NSync -createMessages -out " + getI18nOutputDir().getAbsolutePath()
               + " ";
      for (String constantsName : getI18nConstantsNames()) {
         String localCommand = command + constantsName;

         if (getLog().isInfoEnabled())
            getLog().info("I18N command to execute: " + localCommand);
         try {
            ProcessWatcher pw = new ProcessWatcher(localCommand);
            pw.startProcess(System.out, System.err);
            int retVal = pw.waitFor();
            if (retVal != 0) {
               throw new MojoFailureException("Compilation failed.");
            }
         }
         catch (Exception e) {
            throw new MojoExecutionException("Exception attempting compile.", e);
         }
      }
   }
}
