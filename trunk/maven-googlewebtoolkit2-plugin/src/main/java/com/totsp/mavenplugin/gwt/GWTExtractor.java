package com.totsp.mavenplugin.gwt;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.artifact.Artifact;
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

  public void execute() throws MojoExecutionException, MojoFailureException {

    Iterator<Artifact>  iter = getProject().getDependencyArtifacts().iterator();

    boolean fFoundDependency = false;
    while (iter.hasNext()) {
      Artifact a = iter.next();

      if (a.getGroupId().equals(getGroupId()) &&
          a.getArtifactId().equals(GWTSetup.guessArtifactId())) {
        try {

          fFoundDependency = true;

          ZipFile   zipFile = new ZipFile(a.getFile());
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
              "  Check that you do not have any dependencies that conflict with " + this.getGroupId() + ":" + GWTSetup.guessArtifactId() + "\n" +
              "If not, please report this error to the gwt-maven project.";
          throw new MojoExecutionException(error, e);
        }

        break;
      }


    }

    if (!fFoundDependency) {
      String  error = "Error:  Could not load GWT artifact.  Make sure you have the setup goal enabled for this plugin" +
          " or that you have setup a dependency on " + this.getGroupId() + ":" + GWTSetup.guessArtifactId() + "\n";
      throw new MojoExecutionException(error);
    }

  }
  
}
