package com.totsp.mavenplugin.gwt;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.util.StringUtils;

import java.io.*;
import java.util.Locale;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * @phase test
 * @goal testGwt
 * @requiresDependencyResolution test
 */
public class GWTTest extends AbstractGWTMojo {


  public void execute() throws MojoExecutionException, MojoFailureException {
    try {

      ArrayList<String> classPathList = new ArrayList<String>(this.getProject().getTestClasspathElements());
      try {
        //First see if there is already an artifact that defines the dev jar
        Thread.currentThread().getContextClassLoader().loadClass("com.google.gwt.dev.GWTCompiler");
        
      } catch (ClassNotFoundException cnf) {

        String doNotFindGwtDev = this.getProject().getProperties().getProperty("google.webtoolkit.doNotFileGwtDev");

        if (!Boolean.valueOf(doNotFindGwtDev)) {
          String toolkitHomeStr = this.getProject().getProperties().getProperty("google.webtoolkit.home");
          
          //Setup the GWT Home using our auto-setup one, if one is not set
          if (getProject().getProperties().getProperty("google.webtoolkit.home") == null) {

            File  targetDir = null;
            try {
              targetDir = new File(getGwtBinDirectory(), GWTSetup.guessArtifactId() + "-" + getGwtVersion()).getCanonicalFile();
              getProject().getProperties().setProperty("google.webtoolkit.home", targetDir.getCanonicalPath());
              GWT_PATH = targetDir.getCanonicalPath();
              toolkitHomeStr = GWT_PATH;
            } catch (IOException e) {
               throw new MojoExecutionException(e.getMessage());
            }
          }

          if (toolkitHomeStr == null) {
            try {
              if (getGwtHome() != null)
                toolkitHomeStr = getGwtHome().getCanonicalPath();
            } catch (IOException e) {
              throw new MojoFailureException("Error:  google.webtoolkit.home is not set.  Please set this property an active profile in your POM or user settings.");
            }

            if (toolkitHomeStr == null)
              throw new MojoFailureException("Error:  google.webtoolkit.home is not set.  Please set this property an active profile in your POM or user settings.");

          }

          File  toolkitHome = new File(toolkitHomeStr);
          File  devJar = new File(toolkitHome, GWTSetup.guessDevJarName());
          if (!devJar.exists()) {
            //If there is not a file that seems to correspond with what we expect,
            //try something else.

            String[] devJars = toolkitHome.list(new WildcardFileFilter("gwt-dev-*.jar"));
            if (devJars.length == 1) {
              devJar = new File(toolkitHome, devJars[0]);
            } else if (devJars.length == 1) {
              String Error = "Could not find a gwt-dev jar.  Looked in " + toolkitHome + ".  If you think you " +
                  "already gwt-dev-*.jar in your path, you can set the property google.webtoolkit.doNotFileGwtDev to " +
                  "\"true\" in your POM.";
              throw new MojoFailureException(Error);
            } else {
              String Error = "Could not find a gwt-dev jar.  \n" +
                  "Looked in " + toolkitHome + ", but found more than one jar that fullfilled gwt-dev-*.jar" +
                  "If you think you already gwt-dev-*.jar in your path, you can set " +
                  "the property google.webtoolkit.doNotFileGwtDev to \"true\" in your POM.";
              throw new MojoFailureException(Error);
            }
          }
          try {
            classPathList.add(devJar.getCanonicalPath());
          } catch (IOException e) {
            classPathList.add(devJar.getAbsolutePath());
          }
        }

      }
      classPathList.add(getProject().getBuild().getSourceDirectory());
      classPathList.add(getProject().getBuild().getTestSourceDirectory());

      String exe = System.getProperty( "java.home" ) + File.separator + "bin" + File.separator + "java";
      String classpath = StringUtils.join(classPathList.iterator(), File.pathSeparator);
      ArrayList<String> arguments = new ArrayList<String>();

      StringBuffer  cmd = new StringBuffer();

      cmd.append(exe).append(' ');

      if (System.getProperty( "os.name" ).toLowerCase( Locale.US ).startsWith("mac")) {
        cmd.append("-XstartOnFirstThread ");
      }


      //cmd.append("-Dgwt.args=\"-out www-test\" ");
      cmd.append("-cp ");
      cmd.append(classpath).append(' ');
      cmd.append("junit.textui.TestRunner ");

      File  OutputDir = new File(new File(getProject().getBasedir(), "target"), "gwtTest");
      OutputDir.mkdirs();

      List testCompileRoots = getProject().getTestCompileSourceRoots();

      int run = 0;
      int fail = 0;
      int error = 0;

      for (String currRoot : (List<String>) testCompileRoots) {

        //UNDONE(willpugh) -- Need to be able to change the File filter here.
        Collection<File> coll = FileUtils.listFiles(new File(currRoot), new WildcardFileFilter("GwtTest*"), HiddenFileFilter.VISIBLE);
        for (File currFile : coll) {
          List<String> specificArgs = new ArrayList<String>(arguments);

          String packageName = currFile.toString();

          //Pull off the extension
          if (packageName.lastIndexOf('.') > packageName.lastIndexOf(File.separatorChar)) {
            packageName = packageName.substring(0, packageName.lastIndexOf('.'));  
          }

          if (packageName.startsWith(currRoot)) {
            packageName = packageName.substring(currRoot.length());
          }

          if (packageName.startsWith("/")) {
            packageName = packageName.substring(1);
          }

          packageName = StringUtils.replace(packageName, '/', '.');
          specificArgs.add(packageName);

          
          try {

            String  fullCmd = cmd.toString() + packageName;

            System.out.println(fullCmd);
            Process process = Runtime.getRuntime().exec(fullCmd);

            StreamSucker suckOut = null;
            StreamSucker suckErr = null;
            try {
            suckOut = new StreamSucker(process.getInputStream(), new FileOutputStream(new File(OutputDir, packageName + ".txt")));
            suckErr = new StreamSucker(process.getErrorStream(), new FileOutputStream(new File(OutputDir, packageName + ".err")));

            suckOut.start();
            suckErr.start();
            } catch(FileNotFoundException fne) {
            }

            process.waitFor();
            suckOut.timeToClose = true;
            suckErr.timeToClose = true;
            suckOut.join();
            suckErr.join();

            FileInputStream   fis = new FileInputStream(new File(OutputDir, packageName + ".txt"));
            String            output = IOUtils.toString(fis);

            String            err = "";

            if (suckErr.byteswritten > 0) {
              FileInputStream   ois = new FileInputStream(new File(OutputDir, packageName + ".err"));
              err = IOUtils.toString(ois).trim();
            }

            if (err.length() > 0) {
              System.err.println(err);
            }


            Pattern p = Pattern.compile("\\s*Tests run:\\s*(\\d+)\\s*,\\s*Failures:\\s*(\\d+),\\s*Errors:\\s*(\\d+)\\s*");
            Pattern p2 = Pattern.compile("OK\\s\\((\\d+) tests\\)\\s*");
            Matcher m = null;
            Matcher m2 = null;

            String[]  lines = output.split("\n");
            for (String curr : lines) {
              m = p.matcher(curr);
              if (m.matches())
                break;

              m2 = p2.matcher(curr);
              if (m2.matches())
                break;

            }

            if (m.matches()) {
              run += Integer.parseInt(m.group(1));
              fail += Integer.parseInt(m.group(2));
              error += Integer.parseInt(m.group(3));

              System.out.println("Testing " + packageName);
              System.out.println(m.group(0));

            } else if (err.length() == 0){
              if (m2.matches()) {
                run += Integer.parseInt(m2.group(1));   
              } else {
                System.out.println("Could not get JUnit results.  Look at " + new File(OutputDir, packageName + ".txt").getAbsolutePath());
              }
            } else {
              error++;
              System.out.println("Unit tests failed.");
            }

          } catch (InterruptedException e) {
            e.printStackTrace();
          } catch (IOException e) {
            e.printStackTrace();
          }


        }

      }

      if (error + fail > 0)
        throw new MojoExecutionException("GWT Unit Tests failed.\nTests run: " + run  + ", Failures: " + fail + ", Errors: " + error + ", Skipped: 0");

      System.out.println("OK (" + run + " tests)");

    } catch (DependencyResolutionRequiredException e) {
      e.printStackTrace();
    }

  }


  static class StreamSucker extends Thread {
    InputStream   steam;
    OutputStream  os;
    int byteswritten = 0;
    volatile boolean timeToClose = false;

    public StreamSucker(InputStream steam, OutputStream os) {
      this.steam = steam;
      this.os = os;
    }

    public void siphonAvailableBytes(byte[] buf) throws IOException {
      boolean close = timeToClose;

      int available = steam.available();
      while (available > 0) {
        available = steam.read(buf);
        os.write(buf, 0, available);
        byteswritten += available;
        available = steam.available();
      }

      if (close)
        throw new IOException("Done");
    }

    public void run() {
      byte[]  buf = new byte[4096];

      try {

        while (true) {
          synchronized (this) {
            this.wait(80);
          }
          siphonAvailableBytes(buf);
        }

      } catch (InterruptedException e) {
        //We got interupted, time to go. . .
      } catch (IOException e) {

      }
    }

  }
}


