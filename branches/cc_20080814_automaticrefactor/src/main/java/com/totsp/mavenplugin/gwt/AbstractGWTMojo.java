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
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.project.MavenProject;
import org.codehaus.classworlds.ClassRealm;
import org.codehaus.classworlds.ClassWorld;
import org.codehaus.plexus.util.FileUtils;

import com.totsp.mavenplugin.gwt.util.BuildClasspathUtil;

// TODO replace all sys out with logging - this.getLog().warn(e.getMessage());

/**
 * Abstract Mojo for GWT-Maven.
 *
 * @author ccollins
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
   /** @component 
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
    * @parameter property="compileTargets"
    * @required
    */
   private String[] compileTarget;
   /**
    * @parameter expression="${google.webtoolkit.extrajvmargs}"
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

         for (Iterator it = BuildClasspathUtil.buildClasspathList(this, false).iterator(); it.hasNext();) {
            realm.addConstituent(((File) it.next()).toURI().toURL());
         }

         Thread.currentThread().setContextClassLoader(realm.getClassLoader());
         ///System.out.println("AbstractGwtMojo realm classloader = " + realm.getClassLoader().toString());

         return realm.getClassLoader();
      }
      catch (Exception e) {
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

   public String getTranslatorDestinationPackage() {
      return translatorDestinationPackage;
   }

   public void setTranslatorDestinationPackage(String translatorDestinationPackage) {
      this.translatorDestinationPackage = translatorDestinationPackage;
   }

   public boolean isTranslatorTwoWay() {
      return translatorTwoWay;
   }

   public void setTranslatorTwoWay(boolean translatorTwoWay) {
      this.translatorTwoWay = translatorTwoWay;
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
}
