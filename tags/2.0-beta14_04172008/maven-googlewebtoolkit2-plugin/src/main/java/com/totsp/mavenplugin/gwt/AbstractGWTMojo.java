/*
 * AbstractGWTMojo.java
 *
 * Created on January 11, 2007, 6:17 PM
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.project.MavenProject;
import org.codehaus.classworlds.ClassRealm;
import org.codehaus.classworlds.ClassWorld;
import org.codehaus.plexus.util.FileUtils;

import com.totsp.mavenplugin.gwt.support.MakeCatalinaBase;

/**
 *
 * @author cooper
 */
public abstract class AbstractGWTMojo extends AbstractMojo {

    static public final String OS_NAME = System.getProperty("os.name").toLowerCase(Locale.US);
    static public String GWT_PATH = null;
    static public String EXTA_ARG = null;
    public static final String JAVA_COMMAND = (System.getProperty("java.home") != null)
            ? FileUtils.normalize(
            System.getProperty("java.home") + File.separator + "bin" + File.separator + "java") : "java";
    /**
     * default read size for stream copy
     */
    public static final int DEFAULT_BUFFER_SIZE = 1024;
    /**
     * @parameter property="generatorRootClasses"
     */
    private String[] generatorRootClasses;
    /**
     * @parameter
     */
    private String generatorDestinationPackage;
    /**
     * @parameter
     */
    private String translatorDestinationPackage;
    /**
     * @parameter
     */
    private boolean translatorTwoWay;
    /**
     * @parameter
     */
    private boolean generateGettersAndSetters;
    /**
     * @parameter
     */
    private boolean generatePropertyChangeSupport;
    /**
     * @parameter
     */
    private boolean overwriteGeneratedClasses;
    /**
     * @parameter
     */
    private String groupId = "com.google.gwt";
    /**
     * @parameter default-value="1.4.61"
     */
    private String gwtVersion;
    /**
     */
    private String type = "zip";
    /**
     * @parameter expression="${project.build.directory}"
     */
    private File buildDir;
    /**
     * @parameter
     */
    private File contextXml;
    /**
     * @parameter expression="${basedir}/src/main/webapp/WEB-INF/web.xml"
     *
     */
    private File webXml;
    /**
     * @parameter expression="${project.build.directory}/.generated"
     */
    private File gen;
    /**
     * @parameter expression="${google.webtoolkit.home}"
     */
    private File gwtHome;
    /**
     * @parameter expression="${project.build.directory}/${project.build.finalName}"
     */
    private File output;
    /**
     * @parameter expression="${project.build.directory}/tomcat"
     */
    private File tomcat;
    /**
     * Project instance, used to add new source directory to the build.
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;
    /**
     * @parameter property="compileTargets"
     * @required
     */
    private String[] compileTarget;
    /**
     * @parameter expression="${google.webtoolkit.extrajvmargs}"
     *
     */
    private String extraJvmArgs;
    /**
     * @parameter default-value="INFO"
     */
    private String logLevel;
    /**
     * @parameter
     * @required
     */
    private String runTarget;
    /**
     * @parameter default-value="OBF"
     */
    private String style;
    /**
     * @parameter default-value="false"
     */
    private boolean noServer;
    /**
     * @parameter default-value="8888"
     */
    private int port;
    /**
     * @parameter default-value="8000"
     */
    private int debugPort;
    /**
     * @parameter default-value="true"
     */
    private boolean debugSuspend;
    /**
     * @parameter default-value="GwtTest*"
     */
    private String testFilter;
    /**
     * @parameter default-value="true"
     */
    private boolean sourcesOnPath;
    
    
    /**
     * @parameter default-value="false"
     */
    private boolean enableAssertions;
    
    
    

    /** Creates a new instance of AbstractGWTMojo */
    public AbstractGWTMojo() {
    }

    public void setBuildDir(File buildDir) {
        this.buildDir = buildDir;
    }

    public File getBuildDir() {
        return buildDir;
    }

    public void setCompileTarget(String[] compileTarget) {
        this.compileTarget = compileTarget;
    }

    public String[] getCompileTarget() {
        return compileTarget;
    }

    public void setContextXml(File contextXml) {
        this.contextXml = contextXml;
    }

    public File getContextXml() {
        return contextXml;
    }

    public void setExtraJvmArgs(String extraJvmArgs) {
        this.extraJvmArgs = extraJvmArgs;
    }

    public String getExtraJvmArgs() {
        return extraJvmArgs;
    }

    public void setGen(File gen) {
        this.gen = gen;
    }

    public File getGen() {
        return gen;
    }

    public void setGwtHome(File gwtHome) {
        this.gwtHome = gwtHome;
    }

    public File getGwtHome() {
        return gwtHome;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public void setNoServer(boolean noServer) {
        this.noServer = noServer;
    }

    public boolean isNoServer() {
        return noServer;
    }

    public void setOutput(File output) {
        this.output = output;
    }

    public File getOutput() {
        return output;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public void setProject(MavenProject project) {
        this.project = project;
    }

    public MavenProject getProject() {
        return project;
    }

    public void setRunTarget(String runTarget) {
        this.runTarget = runTarget;
    }

    public String getRunTarget() {
        return runTarget;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public String getStyle() {
        return style;
    }

    public void setTomcat(File tomcat) {
        this.tomcat = tomcat;
    }

    public File getTomcat() {
        return tomcat;
    }

    public void setWebXml(File webXml) {
        this.webXml = webXml;
    }

    public File getWebXml() {
        return webXml;
    }

    public Collection<File> buildClasspathList(boolean fRuntime)   
            throws DependencyResolutionRequiredException {
        
        Set<File> items = new LinkedHashSet<File>();
        //Because Maven loses our properties for some odd reason, we need to double check
        File GWTHome = getGwtHome();

        if (GWTHome == null) {
            GWTHome = new File(GWT_PATH);
        }

        items.add(GWTHome);
        items.add(new File(GWTHome, "gwt-user.jar"));
        items.add(new File(GWTHome, GWTSetup.guessDevJarName()));
        
        if (this.getSourcesOnPath()) {
            for (Iterator it = getProject().getCompileSourceRoots().iterator();
                    it.hasNext();) {
                items.add(new File(it.next().toString()));
            }

            for (Iterator it = project.getResources().iterator(); it.hasNext();) {
                Resource r = (Resource) it.next();
                items.add(new File(r.getDirectory()));
            }
        } 
        
        items.add(new File(getProject().getBasedir(), "classes"));

        if (fRuntime) {
            for (Iterator it = getProject().getRuntimeClasspathElements().iterator(); it.hasNext();) {
                items.add(new File(it.next().toString()));
            }
        } else {
            for (Iterator it = getProject().getCompileClasspathElements().iterator(); it.hasNext();) {
                items.add(new File(it.next().toString()));
            }
        }

        for (Iterator it = getProject().getSystemClasspathElements().iterator();
                it.hasNext();) {
            items.add(new File(it.next().toString()));
        }

        /*
        System.out.println("DEBUG CLASSPATH LIST");
        for (File f : items) {
            System.out.println("   " + f.getAbsolutePath());
        } 
        */       

        return items;
    }

    public String guessArtifactId() {

    if (OS_NAME.startsWith("windows")) {
      return "gwt-windows";
    } else if (OS_NAME.startsWith("mac")) {
        if( this.getGwtVersion().startsWith("1.4.") || this.getGwtVersion().startsWith("1.3.") ){
            return "gwt-mac";
        } else if(System.getProperty("os.version").startsWith("10.5.") ){
            return "gwt-mac_10.5";
        } else {
            return "gwt-mac_10.4";
        }
    } else {
      return "gwt-linux";
    }
  }
    
    public Collection<File> buildRuntimeClasspathList()
            throws DependencyResolutionRequiredException {
        Collection<File> classpathItems = buildClasspathList(true);
        Collection<File> items = new LinkedHashSet<File>();

        for (Iterator it = project.getResources().iterator(); this.getSourcesOnPath() && it.hasNext();) {
            Resource r = (Resource) it.next();
            items.add(new File(r.getDirectory()));
        }

        items.addAll(classpathItems);

        return items;
    }

    public ClassLoader fixThreadClasspath() {
        
        try {
            ClassWorld world = new ClassWorld();

            //use the existing ContextClassLoader in a realm of the classloading space
            ClassRealm root = world.newRealm("gwt-plugin", Thread.currentThread().getContextClassLoader());
            ClassRealm realm = root.createChildRealm("gwt-project");

            for (Iterator it = buildClasspathList(false).iterator();
                    it.hasNext();) {
                realm.addConstituent(((File) it.next()).toURI().toURL());
            }

            Thread.currentThread().setContextClassLoader(
                    realm.getClassLoader());
            ///System.out.println("AbstractGwtMojo realm classloader = " + realm.getClassLoader().toString());

            return realm.getClassLoader();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public String buildClasspath(boolean fRuntime)
            throws DependencyResolutionRequiredException {
        StringBuffer sb = new StringBuffer();
        Iterator<File> iter = buildClasspathList(fRuntime).iterator();

        while (iter.hasNext()) {
            File path = iter.next();

            if (sb.length() > 0) {
                sb.append(File.pathSeparatorChar);
            }

            try {
                sb.append(path.getCanonicalPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return sb.toString();
    }

    public void makeCatalinaBase() throws Exception {
        File webXml = this.getWebXml();
        String[] args = {
            this.getTomcat().getAbsolutePath(), webXml.getAbsolutePath()
        };
        MakeCatalinaBase.main(args);

        if ((this.getContextXml() != null) && this.getContextXml().exists()) {
            FileUtils.copyFile(
                    this.getContextXml(),
                    new File(this.getTomcat(), "conf/gwt/localhost/ROOT.xml"));
        }
    }

    public String[] getGeneratorRootClasses() {
        return generatorRootClasses;
    }

    public void setGeneratorRootClasses(String[] generatorRootClasses) {
        this.generatorRootClasses = generatorRootClasses;
    }

    public String getGeneratorDestinationPackage() {
        return generatorDestinationPackage;
    }

    public void setGeneratorDestinationPackage(
            String generatorDestinationPackage) {
        this.generatorDestinationPackage = generatorDestinationPackage;
    }

    public boolean isGenerateGettersAndSetters() {
        return generateGettersAndSetters;
    }

    public void setGenerateGettersAndSetters(boolean generateGettersAndSetters) {
        this.generateGettersAndSetters = generateGettersAndSetters;
    }

    public boolean isGeneratePropertyChangeSupport() {
        return generatePropertyChangeSupport;
    }

    public void setGeneratePropertyChangeSupport(
            boolean generatePropertyChangeSupport) {
        this.generatePropertyChangeSupport = generatePropertyChangeSupport;
    }

    public boolean isOverwriteGeneratedClasses() {
        return overwriteGeneratedClasses;
    }

    public void setOverwriteGeneratedClasses(boolean overwriteGeneratedClasses) {
        this.overwriteGeneratedClasses = overwriteGeneratedClasses;
    }

    public int getDebugPort() {
        return debugPort;
    }

    public void setDebugPort(int debugPort) {
        this.debugPort = debugPort;
    }

    public boolean isDebugSuspend() {
        return debugSuspend;
    }

    public void setDebugSuspend(boolean debugSuspend) {
        this.debugSuspend = debugSuspend;
    }

    protected void copyFile(File source, File destination)
            throws IOException {
        FileOutputStream fos = new FileOutputStream(destination);
        FileInputStream fis = new FileInputStream(source);
        copyStream(fis, fos);
        fos.flush();
        fos.close();
        fis.close();
    }

    /** Copies the data from an InputStream object to an OutputStream object.
     * @param sourceStream The input stream to be read.
     * @param destinationStream The output stream to be written to.
     * @return int value of the number of bytes copied.
     * @exception IOException from java.io calls.
     */
    protected static int copyStream(
            InputStream sourceStream, OutputStream destinationStream)
            throws IOException {
        int bytesRead = 0;
        int totalBytes = 0;
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];

        while (bytesRead >= 0) {
            bytesRead = sourceStream.read(buffer, 0, buffer.length);

            if (bytesRead > 0) {
                destinationStream.write(buffer, 0, bytesRead);
            }

            totalBytes += bytesRead;
        }

        return totalBytes;
    }

    protected File getGwtBinDirectory() throws IOException {
        return new File(
                getProject().getBuild().getOutputDirectory(), "../gwtBin").getCanonicalFile();
    }

    public String getTranslatorDestinationPackage() {
        return translatorDestinationPackage;
    }

    public void setTranslatorDestinationPackage(
            String translatorDestinationPackage) {
        this.translatorDestinationPackage = translatorDestinationPackage;
    }

    public boolean isTranslatorTwoWay() {
        return translatorTwoWay;
    }

    public void setTranslatorTwoWay(boolean translatorTwoWay) {
        this.translatorTwoWay = translatorTwoWay;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGwtVersion() {
        return gwtVersion;
    }

    public void setGwtVersion(String gwtVersion) {
        this.gwtVersion = gwtVersion;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setCompileTargets(String[] targets) {
        compileTarget = targets;
    }

    public String getTestFilter() {
        return testFilter;
    }

    public void setTestFilter(String testFilter) {
        this.testFilter = testFilter;
    }

    public boolean getSourcesOnPath() {
        return this.sourcesOnPath;
    }

    public void setSourcesOnPath(boolean value) {
        this.sourcesOnPath = value;
    }


    public boolean isEnableAssertions() {
        return enableAssertions;
    }

    public void setEnableAssertions(boolean enableAssertions) {
        this.enableAssertions = enableAssertions;
    }
}
