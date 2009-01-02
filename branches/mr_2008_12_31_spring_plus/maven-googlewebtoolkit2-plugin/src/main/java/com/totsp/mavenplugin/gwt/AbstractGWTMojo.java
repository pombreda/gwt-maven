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
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.Locale;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.classworlds.ClassRealm;
import org.codehaus.classworlds.ClassWorld;
import org.codehaus.plexus.util.FileUtils;

import com.totsp.mavenplugin.gwt.support.util.DependencyScope;
import com.totsp.mavenplugin.gwt.util.BuildClasspathUtil;

/**
 * Abstract Mojo for GWT-Maven.
 * 
 * @author ccollins
 * @author cooper
 * @author willpugh
 * @author Marek Romanowski
 */
public abstract class AbstractGWTMojo extends AbstractMojo {

   private static final String OS_NAME = 
     System.getProperty("os.name").toLowerCase(Locale.US);
   private static final String WINDOWS = "windows";
   private static final String LINUX = "linux";
   private static final String MAC = "mac";
   private static final String LEOPARD = "leopard";
   
   public static final boolean isWindows = OS_NAME.startsWith(WINDOWS);
   public static final boolean isLinux   = OS_NAME.startsWith(LINUX);
   public static final boolean isMac     = OS_NAME.startsWith(MAC);
   public static final boolean isLeopard = OS_NAME.startsWith(LEOPARD);
   
   /**
    * Platform on which mvn runs. Default is Linux (should work for all 
    * unix-like systems).
    */
   public static final String platformName = isWindows ? 
       WINDOWS : ((isMac || isLeopard) ? MAC : LINUX);
   
   public static final String GWT_GROUP_ID = "com.google.gwt";
   public static final String GOOGLE_WEBTOOLKIT_HOME = "google.webtoolkit.home";

   /**
    * Returns path to java command that should be used when executing script.
    */
   public String getJavaCommand() throws MojoExecutionException {
     final File javaHome = getJavaHomeForScriptExecutions();
     // if JAVA_HOME for scripts is set, then use it
     if (javaHome != null) {
       File javaCommandFile = new File(javaHome, "bin/java");
       if (!javaCommandFile.exists()) {
         throw new MojoExecutionException(
             "could not find java in <javaHomeForScriptExecutions>:" 
             + javaHome.getAbsolutePath());
       }
       return javaCommandFile.getAbsolutePath();
     }
     
     // else use standard java
     return (System.getProperty("java.home") != null) ? 
         FileUtils.normalize(System.getProperty("java.home")
             + File.separator + "bin" + File.separator + "java") : "java";
   }

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
   private List<DefaultArtifact> pluginClasspathList;
   /**
    * @component
    */
   private ArtifactFactory artifactFactory;
   /**
    * @component
    */
   private ArtifactResolver resolver;
   /**
    * @parameter expression="${localRepository}"
    */
   private ArtifactRepository localRepository;
   /**
    * @parameter expression="${project.remoteArtifactRepositories}"
    */
   private List<DefaultArtifactRepository> remoteRepositories;

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
    * @parameter default-value="1.5.3"
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
    * @parameter expression="${project.build.directory}/${project.build.finalName}"
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
    * Tomcat 5.0.x format - hence no default).
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
    * Specifies whether or not to add the module name as a prefix to the
    * servlet path when merging web.xml.  If you set this to false the exact
    * path from the GWT module will be used, nothing else will be prepended.
    * 
    * @parameter default-value="false"
    */
   private boolean webXmlServletPathAsIs;
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
    * Whether or not to skip GWT testing.
    * 
    * @parameter 
    */
   private boolean testSkip;
   /**
    * Whether or not to skip GWT testing.
    * 
    * @parameter default-value="${maven.test.skip}"
    */
   private boolean mavenTestSkip;
   /**
    * Whether or not to add compile source root to classpath.
    * 
    * @parameter default-value="true"
    */
   private boolean sourcesOnPath;
   /**
    * Whether or not to add resources root to classpath.
    *
    * @parameter default-value="true"
    */
   private boolean resourcesOnPath;
   /**
    * Whether or not to enable assertions in generated scripts (-ea).
    * 
    * @parameter default-value="false"
    */
   private boolean enableAssertions;
   /**
    * Specifies the mapping URL to be used with the shell servlet.
    * 
    * @parameter default-value="/*"
    */
   private String shellServletMappingURL;
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
    * List of names of properties files that should be used to generate i18n
    * ConstantsWithLookup interfaces.
    * 
    * @parameter
    */
   private String[] i18nConstantsWithLookupNames;
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
   /**
    * If this property is set, scripts will be fired like this was JAVA_HOME
    * directory.
    * @parameter
    */
   private File javaHomeForScriptExecutions;
   /**
    * Compile GWT sources even if compilation time marker is newer then all
    * source files.
    * @parameter default-value="false"
    */
   private boolean forceCompile;

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

      // use the existing ContextClassLoader in a realm of the classloading
      // space
      ClassRealm root = world.newRealm("gwt-plugin", 
          Thread.currentThread().getContextClassLoader());
      ClassRealm realm = root.createChildRealm("gwt-project");

      // add all classpath elements to new realm
      for (File file : BuildClasspathUtil.buildClasspathList(this, DependencyScope.COMPILE)) {
        realm.addConstituent(file.toURI().toURL());
      }

      // set new thread classloader
      Thread.currentThread().setContextClassLoader(realm.getClassLoader());

      return realm.getClassLoader();
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }
   
   
   
  public static final String sourceChangedMarkerFile = ".sourceChangedMarkerFile";



  /**
   * Check did sources changed.
   */
  protected Long sourcesChanged() {
    // younges modification dates
    final SortedSet<Long> modificationDates = new TreeSet<Long>();
    // walker that collects modification dates
    final ModificationDateCollectingDirectoryWalker walker =
      new ModificationDateCollectingDirectoryWalker();
    // check last modification dates of all files
    traverseeAllCompileSourceRoots(new IFileExecution() {
      public void executeForFile(final File file) {
        modificationDates.add(walker.walk(file));
      }
    });
    
    long lastModification = modificationDates.last();
    long markerValue = getMarker();
    // if last changes are newer then last compilation
    if (lastModification > markerValue) {
      return lastModification;
    } else {
      return null;
    }
  }
  
  
  
  protected void saveCompilationTimeMarker(long markerValue) {
    setMarker(markerValue);
  }
  
  
  
  private long getMarker() {
    try {
      File markerFile = new File (
          new File(getProject().getBuild().getOutputDirectory()).getParentFile(), 
          sourceChangedMarkerFile);
      // if marker file does not exist return min value - force compile
      if (!markerFile.exists()) {
        return Long.MIN_VALUE;
      }
      RandomAccessFile marker = new RandomAccessFile(markerFile, "r");
      return marker.readLong();
    } catch (IOException e) {
      // if exception occured log it and return true (compile)
      getLog().warn("Error occured while accessing marker file. " 
          + "Plugin will work like if there were no marker file on disk.");
      // force compilation
      return Long.MIN_VALUE;
    }
  }
  
  
  
  private void setMarker(long markerValue) {
    try {
      File markerFile = new File (
          new File(getProject().getBuild().getOutputDirectory()).getParentFile(), 
          sourceChangedMarkerFile);
      markerFile.delete();
        markerFile.createNewFile();
      RandomAccessFile marker = new RandomAccessFile(markerFile, "rw");
      // update marker
      marker.writeLong(markerValue);
      marker.close();
    } catch (IOException e) {
      // do nothing when problems with marker file occured beside logging
      getLog().warn("Error occured while accessing marker file. " 
          + "Plugin will work like if there were no marker file on disk.", e);
    }
  }
  
  
  
  /**
   * Force compilation next time. Removes source changed marker so compilation
   * will be fired.
   */
  protected void forceCompile() {
    File markerFile = new File (
        new File(getProject().getBuild().getOutputDirectory()).getParentFile(), 
        sourceChangedMarkerFile);
    
    // remove marker file if exists
    if (markerFile.exists()) {
      markerFile.delete();
    }
  }
  
  
  
  /**
   * Execute some job for all compile source roots and resource directories.
   * @param fileExecution
   */
  protected void traverseeAllCompileSourceRootsAndResourcesDirectories(
      IFileExecution fileExecution) {
    traverseeAllCompileSourceRoots(fileExecution);
    traverseeAllResourcesDirectories(fileExecution);
  }
  
  
  
  /**
   * Execute some job for all compile source roots and resource directories.
   * @param fileExecution
   */
  @SuppressWarnings("unchecked")
  protected void traverseeAllCompileSourceRoots(
      IFileExecution fileExecution) {
    final List<String> compileSourceRoots = getProject().getCompileSourceRoots();
    // do it for all compile source roots
    for (String compileSourceRoot : compileSourceRoots) {
      fileExecution.executeForFile(new File(compileSourceRoot));
    }
  }
  
  
  
  /**
   * Execute some job for all compile source roots and resource directories.
   * @param fileExecution
   */
  @SuppressWarnings("unchecked")
  protected void traverseeAllResourcesDirectories(
      IFileExecution fileExecution) {
    // do it for all resource directories
    final List<Resource> resources = getProject().getResources();
    for (Resource resource : resources) {
      fileExecution.executeForFile( new File(resource.getDirectory()));
    }
  }
  
  
  
  // ===========================================================================
  // GETTERS AND SETTERS
  // ===========================================================================

   public void setBuildDir(File buildDir) {
      this.buildDir = buildDir;
   }
   public File getBuildDir() {
      return this.buildDir;
   }
   public void setCompileTarget(String[] compileTarget) {
      this.compileTarget = compileTarget;
   }
   public String[] getCompileTarget() {
      return this.compileTarget;
   }
   public void setContextXml(File contextXml) {
      this.contextXml = contextXml;
   }
   public File getContextXml() {
      return this.contextXml;
   }
   public void setExtraJvmArgs(String extraJvmArgs) {
      this.extraJvmArgs = extraJvmArgs;
   }
   public String getExtraJvmArgs() {
      return this.extraJvmArgs;
   }
   public void setGen(File gen) {
      this.gen = gen;
   }
   public File getGen() {
      return this.gen;
   }
   public void setGwtHome(File gwtHome) {
      this.gwtHome = gwtHome;
   }
   public File getGwtHome() {
      return this.gwtHome;
   }
   public void setLogLevel(String logLevel) {
      this.logLevel = logLevel;
   }
   public String getLogLevel() {
      return this.logLevel;
   }
   public void setNoServer(boolean noServer) {
      this.noServer = noServer;
   }
   public boolean isNoServer() {
      return this.noServer;
   }
   public void setOutput(File output) {
      this.output = output;
   }
   public File getOutput() {
      return this.output;
   }
   public void setPort(int port) {
      this.port = port;
   }
   public int getPort() {
      return this.port;
   }
   public void setProject(MavenProject project) {
      this.project = project;
   }
   public MavenProject getProject() {
      return this.project;
   }
   public void setRunTarget(String runTarget) {
      this.runTarget = runTarget;
   }
   public String getRunTarget() {
      return this.runTarget;
   }
   public void setStyle(String style) {
      this.style = style;
   }
   public String getStyle() {
      return this.style;
   }
   public void setTomcat(File tomcat) {
      this.tomcat = tomcat;
   }
   public File getTomcat() {
      return this.tomcat;
   }
   public void setWebXml(File webXml) {
      this.webXml = webXml;
   }
   public File getWebXml() {
      return this.webXml;
   }
   public boolean isWebXmlServletPathAsIs() {
      return this.webXmlServletPathAsIs;
   }
   public void setWebXmlServletPathAsIs(boolean webXmlServletPathAsIs) {
      this.webXmlServletPathAsIs = webXmlServletPathAsIs;
   }
   public String getShellServletMappingURL() {
      return this.shellServletMappingURL;
   }
   public void setShellServletMappingURL(String shellServletMappingURL) {
      this.shellServletMappingURL = shellServletMappingURL;
   }
   public String[] getGeneratorRootClasses() {
      return this.generatorRootClasses;
   }
   public void setGeneratorRootClasses(String[] generatorRootClasses) {
      this.generatorRootClasses = generatorRootClasses;
   }
   public String getGeneratorDestinationPackage() {
      return this.generatorDestinationPackage;
   }
   public void setGeneratorDestinationPackage(String generatorDestinationPackage) {
      this.generatorDestinationPackage = generatorDestinationPackage;
   }
   public boolean isGenerateGettersAndSetters() {
      return this.generateGettersAndSetters;
   }
   public void setGenerateGettersAndSetters(boolean generateGettersAndSetters) {
      this.generateGettersAndSetters = generateGettersAndSetters;
   }
   public boolean isGeneratePropertyChangeSupport() {
      return this.generatePropertyChangeSupport;
   }
   public void setGeneratePropertyChangeSupport(boolean generatePropertyChangeSupport) {
      this.generatePropertyChangeSupport = generatePropertyChangeSupport;
   }
   public boolean isOverwriteGeneratedClasses() {
      return this.overwriteGeneratedClasses;
   }
   public void setOverwriteGeneratedClasses(boolean overwriteGeneratedClasses) {
      this.overwriteGeneratedClasses = overwriteGeneratedClasses;
   }
   public int getDebugPort() {
      return this.debugPort;
   }
   public void setDebugPort(int debugPort) {
      this.debugPort = debugPort;
   }
   public boolean isDebugSuspend() {
      return this.debugSuspend;
   }
   public void setDebugSuspend(boolean debugSuspend) {
      this.debugSuspend = debugSuspend;
   }
   public String getGwtVersion() {
      return this.gwtVersion;
   }
   public void setGwtVersion(String gwtVersion) {
      this.gwtVersion = gwtVersion;
   }
   public void setCompileTargets(String[] targets) {
      this.compileTarget = targets;
   }
   public String getTestFilter() {
      return this.testFilter;
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
   public boolean getResourcesOnPath() {
     return resourcesOnPath;
   }
  public void setResourcesOnPath(boolean resourcesOnPath) {
    this.resourcesOnPath = resourcesOnPath;
  }
  public boolean isEnableAssertions() {
    return this.enableAssertions;
  }
   public void setEnableAssertions(boolean enableAssertions) {
      this.enableAssertions = enableAssertions;
   }
   public List<DefaultArtifact> getPluginClasspathList() {
      return this.pluginClasspathList;
   }
   public void setPluginClasspathList(List<DefaultArtifact> pluginClasspathList) {
      this.pluginClasspathList = pluginClasspathList;
   }
   public ArtifactFactory getArtifactFactory() {
      return this.artifactFactory;
   }
   public void setArtifactFactory(ArtifactFactory artifactFactory) {
      this.artifactFactory = artifactFactory;
   }
   public ArtifactResolver getResolver() {
      return this.resolver;
   }
   public void setResolver(ArtifactResolver resolver) {
      this.resolver = resolver;
   }
   public ArtifactRepository getLocalRepository() {
      return this.localRepository;
   }
   public void setLocalRepository(ArtifactRepository localRepository) {
      this.localRepository = localRepository;
   }
   public List<DefaultArtifactRepository> getRemoteRepositories() {
      return this.remoteRepositories;
   }
   public void setRemoteRepositories(List<DefaultArtifactRepository> remoteRepositories) {
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
      return this.extraTestArgs;
   }
   public void setExtraTestArgs(String extraTestArgs) {
      this.extraTestArgs = extraTestArgs;
   }
   public boolean isTestSkip() {
      return this.testSkip;
   }
   public void setTestSkip(boolean skip) {
      this.testSkip = skip;
   }
  public String[] getI18nConstantsWithLookupNames() {
    return i18nConstantsWithLookupNames;
  }
  public void setI18nConstantsWithLookupNames(
      String[] i18nConstantsWithLookupNames) {
    this.i18nConstantsWithLookupNames = i18nConstantsWithLookupNames;
  }
  public File getJavaHomeForScriptExecutions() {
    return javaHomeForScriptExecutions;
  }
  public void setJavaHomeForScriptExecutions(File javaHomeForScriptExecutions) {
    this.javaHomeForScriptExecutions = javaHomeForScriptExecutions;
  }
  public boolean isMavenTestSkip() {
    return mavenTestSkip;
  }
  public void setMavenTestSkip(boolean mavenTestSkip) {
    this.mavenTestSkip = mavenTestSkip;
  }
  public boolean isForceCompile() {
    return forceCompile;
  }
  public void setForceCompile(boolean forceCompile) {
    this.forceCompile = forceCompile;
  }
}


