package com.totsp.mavenplugin.gwt;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.totsp.mavenplugin.gwt.support.util.FileIOUtils;



/**
 * @goal copy-webapp-resources
 * @phase process-classes
 * @execute goal="compile"
 * @requiresDependencyResolution compile
 * @requiresDependencyResolution runtime
 * 
 * @author Marek Romanowski
 * @since 2009-01-02
 */
public class ProcessGWTResourcesMojo extends AbstractGWTMojo {

  public void execute() throws MojoExecutionException, MojoFailureException {
    try {
    // copy files for WEB-INF (except for web.xml - not to change behaviour)
    File webappWebInf = new File(getTomcat(), "webapps/ROOT/WEB-INF");
    webappWebInf.mkdirs();
    for (Object o : getProject().getResources()) {
      File rootFile = new File(((Resource) o).getDirectory(), "WEB-INF");
      if (rootFile.exists()) {
        FileIOUtils.copyRecursive(rootFile, webappWebInf, new FileFilter() {
          public boolean accept(File pathname) {
            return !pathname.getName().equals("web.xml");
          }
        });
      }
    };
    
    // copy files from project output directory to webapp
      FileIOUtils.copyRecursive(getOutput(), webappWebInf.getParentFile(), null);
    } catch (IOException e) {
      throw new MojoExecutionException("File copy error", e);
    }
  }
}


