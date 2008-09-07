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
 * 
 */
package com.totsp.mavenplugin.gwt;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.project.MavenProject;
import org.codehaus.classworlds.ClassRealm;
import org.codehaus.classworlds.ClassWorld;
import org.codehaus.plexus.util.FileUtils;

import com.totsp.mavenplugin.gwt.util.BuildClasspathUtil;
import com.totsp.mavenplugin.gwt.util.DependencyScope;

/**
 * Abstract Mojo for GWT-Maven.
 * 
 * @author ccollins
 * @author cooper
 * @author willpugh
 */
public abstract class AbstractGWTMojo extends AbstractMojo {

    public static final String OS_NAME = System.getProperty("os.name").toLowerCase(Locale.US);
    public static final String GWT_GROUP_ID = "com.google.gwt";
    public static final String WINDOWS = "windows";
    public static final String LINUX = "linux";
    public static final String MAC = "mac";
    public static final String LEOPARD = "leopard";
    public static final String GOOGLE_WEBTOOLKIT_HOME = "google.webtoolkit.home";

    public static final String JAVA_COMMAND = (System.getProperty("java.home") != null) ? FileUtils.normalize(System
            .getProperty("java.home")
            + File.separator + "bin" + File.separator + "java") : "java";

    // Maven properties

    /**
     * Project instance, used to add new source directory to the build.
     * 
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;
    /**
     * <i>Maven Internal</i>: List of artifacts for the plugin.
     * 
     * @parameter expression="${plugin.artifacts}"
     * @required
     * @readonly
     */
    private List pluginClasspathList;
    /**
     * @component
     */
    private org.apache.maven.artifact.factory.ArtifactFactory artifactFactory;
    /**
     * @component
     */
    private org.apache.maven.artifact.resolver.ArtifactResolver resolver;
    /**
     * @parameter expression="${localRepository}"
     */
    private org.apache.maven.artifact.repository.ArtifactRepository localRepository;
    /**
     * @parameter expression="${project.remoteArtifactRepositories}"
     */
    private java.util.List remoteRepositories;

    // GWT-Maven properties

    /**
     * Location on filesystem where project should be built.
     * 
     * @parameter expression="${project.build.directory}"
     */
    private File buildDir;
    /**
     * Set the GWT version number - used to build dependency paths, should match
     * the "version" in the Maven repo.
     * 
     * @parameter default-value="1.5.1"
     */
    private String gwtVersion;
    /**
     * Location on filesystem where GWT is installed - for manual mode (existing
     * GWT on machine - not needed for automatic mode).
     * 
     * @parameter expression="${google.webtoolkit.home}"
     */
    private File gwtHome;
    /**
     * Location on filesystem where GWT will write output files (-out option to
     * GWTCompiler).
     * 
     * @parameter 
     *            expression="${project.build.directory}/${project.build.finalName}"
     */
    private File output;
    /**
     * Location on filesystem where GWT will write generated content for review
     * (-gen option to GWTCompiler).
     * 
     * @parameter expression="${project.build.directory}/.generated"
     */
    private File gen;
    /**
     * List of GWT module names that should be compiled with the GWT compiler.
     * 
     * @parameter property="compileTargets"
     * @required
     */
    private String[] compileTarget;
    /**
     * URL that should be automatically opened by default in the GWT shell.
     * 
     * @parameter
     * @required
     */
    private String runTarget;
    /**
     * GWT logging level (-logLevel ERROR, WARN, INFO, TRACE, DEBUG, SPAM, or
     * ALL).
     * 
     * @parameter default-value="INFO"
     */
    private String logLevel;
    /**
     * GWT JavaScript compiler output style (-style OBF[USCATED], PRETTY, or
     * DETAILED).
     * 
     * @parameter default-value="OBF"
     */
    private String style;
    /**
     * Prevents the embedded GWT Tomcat server from running (even if a port is
     * specified).
     * 
     * @parameter default-value="false"
     */
    private boolean noServer;
    /**
     * Runs the embedded GWT Tomcat server on the specified port.
     * 
     * @parameter default-value="8888"
     */
    private int port;
    /**
     * Specify the location on the filesystem for the generated embedded Tomcat
     * directory.
     * 
     * @parameter expression="${project.build.directory}/tomcat"
     */
    private File tomcat;
    /**
     * Port to listen for debugger connection on.
     * 
     * @parameter default-value="8000"
     */
    private int debugPort;
    /**
     * Source Tomcat context.xml for GWT shell - copied to
     * /gwt/localhost/ROOT.xml (used as the context.xml for the SHELL - requires
     * Tomcat 5.0.x format).
     * 
     * @parameter
     */
    private File contextXml;
    /**
     * Source web.xml deployment descriptor that is used for GWT shell and for
     * deployment WAR to "merge" servlet entries.
     * 
     * @parameter expression="${basedir}/src/main/webapp/WEB-INF/web.xml"
     */
    private File webXml;
    /**
     * Whether or not to suspend execution until a debugger connects.
     * 
     * @parameter default-value="true"
     */
    private boolean debugSuspend;
    /**
     * Extra JVM arguments that are passed to the GWT-Maven generated scripts
     * (for compiler, shell, etc - typically use -Xmx512m here, or
     * -XstartOnFirstThread, etc).
     * 
     * @parameter expression="${google.webtoolkit.extrajvmargs}"
     */
    private String extraJvmArgs;
    /**
     * Simple string filter for classes that should be treated as GWTTestCase
     * type (and therefore invoked during gwtTest goal).
     * 
     * @parameter default-value="GwtTest*"
     */
    private String testFilter;
    /**
     * Extra JVM arguments that are passed only to the GWT-Maven generated test
     * scripts (in addition to std extraJvmArgs).
     * 
     * @parameter default-value=""
     */
    private String extraTestArgs;
    /**
     * Whether or not to skip testing (including gwt:test testing).
     * 
     * @parameter expression="${maven.test.skip}"
     */
    private boolean skip;
    /**
     * Whether or not to add resources and compile source root to classpath.
     * 
     * @parameter default-value="true"
     */
    private boolean sourcesOnPath;
    /**
     * Whether or not to enable assertions in generated scripts (-ea).
     * 
     * @parameter default-value="false"
     */
    private boolean enableAssertions;
    /**
     * Location on filesystem to output generated i18n Constants and Messages
     * interfaces.
     * 
     * @parameter expression="${basedir}/src/main/java/"
     */
    private File i18nOutputDir;
    /**
     * List of names of properties files that should be used to generate i18n
     * Messages interfaces.
     * 
     * @parameter
     */
    private String[] i18nMessagesNames;
    /**
     * List of names of properties files that should be used to generate i18n
     * Constants interfaces.
     * 
     * @parameter
     */
    private String[] i18nConstantsNames;
    /**
     * Top level (root) of classes to begin generation from.
     * 
     * @parameter property="generatorRootClasses"
     */
    private String[] generatorRootClasses;
    /**
     * Destination package for generated classes.
     * 
     * @parameter
     */
    private String generatorDestinationPackage;
    /**
     * Whether or not to generate getter/setter methods for generated classes.
     * 
     * @parameter
     */
    private boolean generateGettersAndSetters;
    /**
     * Whether or not to generate PropertyChangeSupport handling for generated
     * classes.
     * 
     * @parameter
     */
    private boolean generatePropertyChangeSupport;
    /**
     * Whether or not to overwrite generated classes if they exist.
     * 
     * @parameter
     */
    private boolean overwriteGeneratedClasses;

    // ctor

    /** Creates a new instance of AbstractGWTMojo */
    public AbstractGWTMojo() {
    }

    // methods

    /**
     * Helper hack for classpath problems, used as a fallback.
     * 
     * @return
     */
    protected ClassLoader fixThreadClasspath() {

        try {
            ClassWorld world = new ClassWorld();

            //use the existing ContextClassLoader in a realm of the classloading space
            ClassRealm root = world.newRealm("gwt-plugin", Thread.currentThread().getContextClassLoader());
            ClassRealm realm = root.createChildRealm("gwt-project");

            for (Iterator it = BuildClasspathUtil.buildClasspathList(this, DependencyScope.COMPILE).iterator(); it.hasNext();) {
                realm.addConstituent(((File) it.next()).toURI().toURL());
            }

            Thread.currentThread().setContextClassLoader(realm.getClassLoader());
            ///System.out.println("AbstractGwtMojo realm classloader = " + realm.getClassLoader().toString());

            return realm.getClassLoader();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }    

    //
    // accessors/mutators
    //

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

    public String[] getGeneratorRootClasses() {
        return generatorRootClasses;
    }

    public void setGeneratorRootClasses(String[] generatorRootClasses) {
        this.generatorRootClasses = generatorRootClasses;
    }

    public String getGeneratorDestinationPackage() {
        return generatorDestinationPackage;
    }

    public void setGeneratorDestinationPackage(String generatorDestinationPackage) {
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

    public void setGeneratePropertyChangeSupport(boolean generatePropertyChangeSupport) {
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

    public String getGwtVersion() {
        return gwtVersion;
    }

    public void setGwtVersion(String gwtVersion) {
        this.gwtVersion = gwtVersion;
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

    public List getPluginClasspathList() {
        return this.pluginClasspathList;
    }

    public void setPluginClasspathList(List pluginClasspathList) {
        this.pluginClasspathList = pluginClasspathList;
    }

    public org.apache.maven.artifact.factory.ArtifactFactory getArtifactFactory() {
        return this.artifactFactory;
    }

    public void setArtifactFactory(org.apache.maven.artifact.factory.ArtifactFactory artifactFactory) {
        this.artifactFactory = artifactFactory;
    }

    public org.apache.maven.artifact.resolver.ArtifactResolver getResolver() {
        return this.resolver;
    }

    public void setResolver(org.apache.maven.artifact.resolver.ArtifactResolver resolver) {
        this.resolver = resolver;
    }

    public org.apache.maven.artifact.repository.ArtifactRepository getLocalRepository() {
        return this.localRepository;
    }

    public void setLocalRepository(org.apache.maven.artifact.repository.ArtifactRepository localRepository) {
        this.localRepository = localRepository;
    }

    public java.util.List getRemoteRepositories() {
        return this.remoteRepositories;
    }

    public void setRemoteRepositories(java.util.List remoteRepositories) {
        this.remoteRepositories = remoteRepositories;
    }

    public File getI18nOutputDir() {
        return this.i18nOutputDir;
    }

    public void setI18nOutputDir(File outputDir) {
        this.i18nOutputDir = outputDir;
    }

    public String[] getI18nMessagesNames() {
        return this.i18nMessagesNames;
    }

    public void setI18nMessagesNames(String[] messagesNames) {
        this.i18nMessagesNames = messagesNames;
    }

    public String[] getI18nConstantsNames() {
        return this.i18nConstantsNames;
    }

    public void setI18nConstantsNames(String[] constantsNames) {
        this.i18nConstantsNames = constantsNames;
    }

    public String getExtraTestArgs() {
        return extraTestArgs;
    }

    public void setExtraTestArgs(String extraTestArgs) {
        this.extraTestArgs = extraTestArgs;
    }

    public boolean isSkip() {
        return skip;
    }

    public void setSkip(boolean skip) {
        this.skip = skip;
    }

}
