package com.totsp.mavenplugin.gwt;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProjectBuilder;
import org.codehaus.plexus.util.FileUtils;

import java.util.Iterator;
import java.util.Enumeration;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;

/**
 * @phase generate-sources
 * @goal  extractGwt
 * @requiresDependencyResolution compile
 * @Author willpugh  Apr 23, 2007 - 8:38:47 AM
 */
public class GWTExtractor extends AbstractGWTMojo{

  /**
   * Location of the local repository.
   *
   * @parameter expression="${localRepository}"
   * @readonly
   * @required
   */
  protected org.apache.maven.artifact.repository.ArtifactRepository local;

  /**
   * List of Remote Repositories used by the resolver
   *
   * @parameter expression="${project.remoteArtifactRepositories}"
   * @readonly
   * @required
   */
  protected java.util.List remoteRepos;
  /**
   * Artifact factory, needed to download source jars.
   *
   * @component role="org.apache.maven.project.MavenProjectBuilder"
   * @required
   * @readonly
   */
  protected MavenProjectBuilder mavenProjectBuilder;

  /**
   * Used to look up Artifacts in the remote repository.
   *
   * @parameter expression="${component.org.apache.maven.artifact.factory.ArtifactFactory}"
   * @required
   * @readonly
   */
  protected org.apache.maven.artifact.factory.ArtifactFactory factory;

  /**
   * Used to look up Artifacts in the remote repository.
   *
   * @parameter expression="${component.org.apache.maven.artifact.resolver.ArtifactResolver}"
   * @required
   * @readonly
   */
  protected org.apache.maven.artifact.resolver.ArtifactResolver resolver;



  public void execute() throws MojoExecutionException, MojoFailureException {

    Iterator<Artifact>  iter = getProject().getDependencyArtifacts().iterator();

    boolean fFoundDependency = false;
    while (iter.hasNext()) {
      Artifact a = iter.next();

      if (a.getGroupId().equals(getGroupId()) &&
          a.getArtifactId().equals(this.guessArtifactId())) {
        try {

          fFoundDependency = true;

          /*
          It is possible that we were not able to add dependencies before some other component causes the dependencies to
          get resolved.  In this case we need to resolve the file by hand.
          */
          File  artifactFile = a.getFile();

          if (artifactFile == null) {

            Artifact gwtDevArtifact = this.factory.createArtifact(getGroupId(), this.guessArtifactId(), getGwtVersion(), "", "zip");

            try {
              resolver.resolve(gwtDevArtifact, this.remoteRepos, this.local);
            } catch(Exception e) {
              String  error = "Error:  Could not resolve GWT artifact.  If you set \"setup\" goal for this plugin, this may be a bug." +
                "  Check that you do not have any dependencies that conflict with " + this.getGroupId() + ":" + this.guessArtifactId() + "\n" +
                "If not, please report this error to the gwt-maven project.";
              throw new MojoExecutionException(error, e);
            }
            artifactFile = gwtDevArtifact.getFile();
          }
          ZipFile   zipFile = new ZipFile(artifactFile);
          File      extractionDir = getGwtBinDirectory();

          File  timestampFile = new File(extractionDir, ".timestamp-" + a.getFile().lastModified());
          if (!timestampFile.exists()) {
            if (extractionDir.exists()) {
              FileUtils.deleteDirectory(extractionDir);
            }

            System.out.println("Unzipping GWT " + getGwtVersion() + " install to : " + extractionDir.toString());

            extractionDir.mkdirs();

            //If this timestamp file does not exist, then either the GWT zip got updated, or
            //we haven't unpacked it fully yet.
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
              ZipEntry  entry = entries.nextElement();
              File      entryFile = new File(extractionDir, entry.getName());
              if (entry.isDirectory()) {
                entryFile.mkdirs();
              } else {
                FileOutputStream  fis = new FileOutputStream(entryFile);
                copyStream(zipFile.getInputStream(entry), fis);
                fis.flush();
                fis.close();
              }              
            }
            timestampFile.createNewFile();
          }

        } catch (IOException e) {
          String  error = "Error:  Could not load GWT artifact.  If you set \"setup\" goal for this plugin, this may be a bug." +
              "  Check that you do not have any dependencies that conflict with " + this.getGroupId() + ":" + this.guessArtifactId() + "\n" +
              "If not, please report this error to the gwt-maven project.";
          throw new MojoExecutionException(error, e);
        }

        break;
      }


    }

    if (!fFoundDependency) {
      String  error = "Error:  Could not load GWT artifact.  Make sure you have the setup goal enabled for this plugin" +
          " or that you have setup a dependency on " + this.getGroupId() + ":" + this.guessArtifactId() + "\n";
      throw new MojoExecutionException(error);
    }

  }
  
}
