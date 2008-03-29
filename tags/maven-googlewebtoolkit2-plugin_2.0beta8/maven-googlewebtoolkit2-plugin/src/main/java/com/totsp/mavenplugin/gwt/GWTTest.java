package com.totsp.mavenplugin.gwt;

import java.io.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import org.codehaus.plexus.util.StringUtils;


/**
 * @phase test
 * @goal testGwt
 * @requiresDependencyResolution test
 */
public class GWTTest extends AbstractGWTMojo {
    /**
     *  @parameter default-value=""
     */
    private String extraTestArgs;

    /**
     * @parameter expression="${maven.test.skip}"
     */
    private boolean skip;

    /**
     *
     * @throws org.apache.maven.plugin.MojoExecutionException
     * @throws org.apache.maven.plugin.MojoFailureException
     */
    public void execute() throws MojoExecutionException, MojoFailureException {
        if(isSkip()) {
            return;
        }

        try {
            ArrayList<String> classPathList = new ArrayList<String>(
                    this.getProject().getTestClasspathElements());

            try {
                //First see if there is already an artifact that defines the dev jar
                Thread.currentThread().getContextClassLoader()
                      .loadClass("com.google.gwt.dev.GWTCompiler");
            } catch(ClassNotFoundException cnf) {
                String doNotFindGwtDev = this.getProject().getProperties()
                                             .getProperty(
                        "google.webtoolkit.doNotFileGwtDev");

                if(!Boolean.valueOf(doNotFindGwtDev)) {
                    String toolkitHomeStr = this.getProject().getProperties()
                                                .getProperty(
                            "google.webtoolkit.home");

                    //Setup the GWT Home using our auto-setup one, if one is not set
                    if(
                        getProject().getProperties()
                                .getProperty("google.webtoolkit.home") == null) {
                        File targetDir = null;

                        try {
                            targetDir = new File(
                                    getGwtBinDirectory(),
                                    this.guessArtifactId() + "-"
                                    + getGwtVersion()).getCanonicalFile();
                            getProject().getProperties()
                                .setProperty(
                                "google.webtoolkit.home",
                                targetDir.getCanonicalPath());
                            GWT_PATH = targetDir.getCanonicalPath();
                            toolkitHomeStr = GWT_PATH;
                        } catch(IOException e) {
                            throw new MojoExecutionException(e.getMessage());
                        }
                    }

                    if(toolkitHomeStr == null) {
                        try {
                            if(getGwtHome() != null) {
                                toolkitHomeStr = getGwtHome().getCanonicalPath();
                            }
                        } catch(IOException e) {
                            throw new MojoFailureException(
                                "Error:  google.webtoolkit.home is not set.  Please set this property an active profile in your POM or user settings.");
                        }

                        if(toolkitHomeStr == null) {
                            throw new MojoFailureException(
                                "Error:  google.webtoolkit.home is not set.  Please set this property an active profile in your POM or user settings.");
                        }
                    }

                    File toolkitHome = new File(toolkitHomeStr);
                    File devJar = new File(
                            toolkitHome, GWTSetup.guessDevJarName());

                    if(!devJar.exists()) {
                        //If there is not a file that seems to correspond with what we expect,
                        //try something else.
                        String[] devJars = toolkitHome.list(
                                new WildcardFileFilter("gwt-dev-*.jar"));

                        if(devJars.length == 1) {
                            devJar = new File(toolkitHome, devJars[0]);
                        } else if(devJars.length == 1) {
                            String Error = "Could not find a gwt-dev jar.  Looked in "
                                + toolkitHome + ".  If you think you "
                                + "already gwt-dev-*.jar in your path, you can set the property google.webtoolkit.doNotFileGwtDev to "
                                + "\"true\" in your POM.";
                            throw new MojoFailureException(Error);
                        } else {
                            String Error = "Could not find a gwt-dev jar.  \n"
                                + "Looked in " + toolkitHome
                                + ", but found more than one jar that fullfilled gwt-dev-*.jar"
                                + "If you think you already gwt-dev-*.jar in your path, you can set "
                                + "the property google.webtoolkit.doNotFileGwtDev to \"true\" in your POM.";
                            throw new MojoFailureException(Error);
                        }
                    }

                    try {
                        classPathList.add(devJar.getCanonicalPath());
                    } catch(IOException e) {
                        classPathList.add(devJar.getAbsolutePath());
                    }
                }
            }

            classPathList.add(getProject().getBuild().getSourceDirectory());
            classPathList.add(getProject().getBuild().getTestSourceDirectory());

            String exe = System.getProperty("java.home") + File.separator
                + "bin" + File.separator + "java";
            String classpath = StringUtils.join(
                    classPathList.iterator(), File.pathSeparator);
            ArrayList<String> arguments = new ArrayList<String>();

            StringBuffer cmd = new StringBuffer();

            cmd.append(exe).append(' ');

            String extra = (this.getExtraJvmArgs() != null)
                ? this.getExtraJvmArgs() : "";

            if(
                System.getProperty("os.name").toLowerCase(Locale.US)
                          .startsWith("mac")
                    && (extra.indexOf("-XstartOnFirstThread") == -1)) {
                extra = "-XstartOnFirstThread " + extra;
            }

            cmd.append(extra)
               .append(" ")
               .append((this.extraTestArgs == null) ? "" : this.extraTestArgs)
               .append(" ");

            //cmd.append("-Dgwt.args=\"-out www-test\" ");
            cmd.append("-cp ");
            cmd.append(classpath)
               .append(' ');
            cmd.append("junit.textui.TestRunner ");

            File outputDir = new File(this.getBuildDir(), "gwtTest");
            outputDir.mkdirs();
            outputDir.mkdir();

            List testCompileRoots = getProject().getTestCompileSourceRoots();

            int run = 0;
            int fail = 0;
            int error = 0;

            for(String currRoot : (List<String>) testCompileRoots) {
                //UNDONE(willpugh) -- Need to be able to change the File filter here.
                Collection<File> coll = FileUtils.listFiles(
                        new File(currRoot),
                        new WildcardFileFilter(this.getTestFilter()),
                        HiddenFileFilter.VISIBLE);

                for(File currFile : coll) {
                    List<String> specificArgs = new ArrayList<String>(
                            arguments);

                    String packageName = currFile.toString();

                    //Pull off the extension
                    if(
                        packageName.lastIndexOf('.') > packageName.lastIndexOf(
                                File.separatorChar)) {
                        packageName = packageName.substring(
                                0, packageName.lastIndexOf('.'));
                    }

                    if(packageName.startsWith(currRoot)) {
                        packageName = packageName.substring(currRoot.length());
                    }

                    if(packageName.startsWith(File.separator)) {
                        packageName = packageName.substring(1);
                    }

                    packageName = StringUtils.replace(
                            packageName, File.separatorChar, '.');
                    specificArgs.add(packageName);

                    try {
                        String fullCmd = cmd.toString() + packageName;

                        System.out.println(fullCmd);

                        ProcessWatcher pw = new ProcessWatcher(
                                fullCmd, null, this.getBuildDir());

                        StringBuffer out = new StringBuffer();
                        StringBuffer err = new StringBuffer();
                        pw.startProcess(out, err);
                        pw.waitFor();

                        if(err.length() > 0) {
                            System.err.println(err);
                        }

                        FileWriter writer = new FileWriter(
                                new File(
                                    outputDir, "TEST-" + packageName + ".txt"));
                        writer.write(err.toString());
                        writer.write("\n" + out.toString());
                        writer.flush();
                        writer.close();

                        Pattern p = Pattern.compile(
                                "\\s*Tests run:\\s*(\\d+)\\s*,\\s*Failures:\\s*(\\d+),\\s*Errors:\\s*(\\d+)\\s*");
                        Pattern p2 = Pattern.compile(
                                "OK\\s\\((\\d+) tests\\)\\s*");
                        Matcher m = null;
                        Matcher m2 = null;

                        String[] lines = out.toString().split("\n");

                        for(String curr : lines) {
                            m = p.matcher(curr);

                            if(m.matches()) {
                                break;
                            }

                            m2 = p2.matcher(curr);

                            if(m2.matches()) {
                                break;
                            }
                        }

                        if(m.matches()) {
                            run += Integer.parseInt(m.group(1));
                            fail += Integer.parseInt(m.group(2));
                            error += Integer.parseInt(m.group(3));

                            System.out.println("Testing " + packageName);
                            System.out.println(m.group(0));
                        } else if(err.length() == 0) {
                            if(m2.matches()) {
                                run += Integer.parseInt(m2.group(1));
                            } else {
                                System.out.println(out.toString());
                            }
                        } else {
                            error++;
                            System.out.println("Unit tests failed.");
                        }
                    } catch(InterruptedException e) {
                        e.printStackTrace();
                    } catch(IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            if((error + fail) > 0) {
                throw new MojoExecutionException(
                    "GWT Unit Tests failed.\nTests run: " + run
                    + ", Failures: " + fail + ", Errors: " + error
                    + ", Skipped: 0");
            }

            System.out.println("OK (" + run + " tests)");
        } catch(DependencyResolutionRequiredException e) {
            e.printStackTrace();
        }
    }

    public String getExtraTestArgs() {
        return extraTestArgs;
    }

    public boolean isSkip() {
        return skip;
    }

    public void setExtraTestArgs(String extraTestArgs) {
        this.extraTestArgs = extraTestArgs;
    }

    public void setSkip(boolean skip) {
        this.skip = skip;
    }

    static class StreamSucker extends Thread {
        InputStream steam;
        OutputStream os;
        volatile boolean timeToClose = false;
        int byteswritten = 0;

        public StreamSucker(InputStream steam, OutputStream os) {
            this.steam = steam;
            this.os = os;
        }

        public void run() {
            byte[] buf = new byte[4096];

            try {
                while(true) {
                    synchronized(this) {
                        this.wait(80);
                    }

                    siphonAvailableBytes(buf);
                }
            } catch(InterruptedException e) {
                //We got interupted, time to go. . .
            } catch(IOException e) {;
            }
        }

        public void siphonAvailableBytes(byte[] buf) throws IOException {
            boolean close = timeToClose;

            int available = steam.available();

            while(available > 0) {
                available = steam.read(buf);
                os.write(buf, 0, available);
                byteswritten += available;
                available = steam.available();
            }

            if(close) {
                throw new IOException("Done");
            }
        }
    }
}
