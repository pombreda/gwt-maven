package com.totsp.mavenplugin.gwt;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.model.Dependency;

import java.io.File;
import java.io.IOException;

/**
 *
 * This Mojo sets up the initial dependency on the
 *   
 *
 * @phase validate
 * @goal  setup
 * @Author willpugh  Apr 23, 2007 - 12:40:26 AM
 */
public class GWTSetup extends AbstractGWTMojo {

  public void execute() throws MojoExecutionException, MojoFailureException {

    if (getGwtVersion() == null)
      throw new MojoExecutionException("You must set the \"gwtVersion\" property on the GWT plugin in order to use the setup goal.");

    Dependency    gwtDependency = new Dependency();
    gwtDependency.setGroupId(getGroupId());
    gwtDependency.setType(getType());
    gwtDependency.setArtifactId(guessArtifactId());
    gwtDependency.setVersion(getGwtVersion());
    getProject().getModel().addDependency(gwtDependency);

    File  targetDir = null;
    try {
      targetDir = new File(getGwtBinDirectory(), guessArtifactId() + "-" + getGwtVersion()).getCanonicalFile();
    } catch (IOException e) {
       throw new MojoExecutionException(e.getMessage());
    }
    try {
      //getProject().getModel().addProperty("google.webtoolkit.home", targetDir.getCanonicalPath());
      //Dunno why, but maven seems to lose this property for the gwt:gwt task
      getProject().getProperties().setProperty("google.webtoolkit.home", targetDir.getCanonicalPath());
      GWT_PATH = targetDir.getCanonicalPath();
    } catch (IOException e) {
      throw new MojoExecutionException(e.getMessage()); 
    }
    if (OS_NAME.startsWith("mac") && this.getExtraJvmArgs() == null) {
      //Dunno why, but maven seems to lose this property for the gwt:gwt task
      EXTA_ARG = "-XstartOnFirstThread";
    }


    try {
      //Add all the appropriate System Dependencies (even if they don't exist yet)
      addGwtSystemJarDependency(guessDevJarName(), new File(targetDir, guessDevJarName()));
      addGwtSystemJarDependency("gwt-servlet", new File(targetDir, "gwt-servlet.jar"));
      addGwtSystemJarDependency("gwt-user", new File(targetDir, "gwt-user.jar"));
    } catch (IOException ioe) {
      throw new MojoExecutionException(ioe.getMessage());
    }

  }


  public Dependency addGwtSystemJarDependency(String artifactId, File path) throws IOException {
    Dependency    dependency = new Dependency();
    dependency.setGroupId(getGroupId());
    dependency.setType("jar");
    dependency.setArtifactId(artifactId);
    dependency.setVersion(getGwtVersion());
    dependency.setScope("system");
    dependency.setOptional(true);
    dependency.setSystemPath(path.getAbsolutePath());
    getProject().getModel().addDependency(dependency);

    if (!path.exists()) {
      path.getParentFile().mkdirs();
      path.createNewFile();
    }

    return dependency;
  }


   

  static public String  guessDevJarName() {
    if (OS_NAME.startsWith("windows")) {
      return "gwt-dev-windows.jar";
    } else if (OS_NAME.startsWith("mac")) {
      return "gwt-dev-mac.jar";
    } else {
      return "gwt-dev-linux.jar";
    }
  }
}
