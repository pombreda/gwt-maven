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

import com.totsp.mavenplugin.gwt.support.MakeCatalinaBase;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.project.MavenProject;

import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.maven.model.Resource;
import org.codehaus.classworlds.ClassRealm;
import org.codehaus.classworlds.ClassWorld;


/**
 *
 * @author cooper
 */
public abstract class AbstractGWTMojo extends AbstractMojo {
    
    /**
     * @parameter
     */
    private String generatorRootClasses;
    /**
     * @parameter
     */
    private String generatorDestinationPackage;
    
    /**
     * @parameter
     */
    private boolean generateGettersAndSetters;
    
    /**
     * @parameter
     */
    private boolean generatePropertyChangeSupport;
    
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
     * @parameter expression="${project.build.directory}/${project.build.finalName}"
     */
    private File gen;
    
    /**
     * @parameter expression="${google.webtoolkit.home}"
     * @required
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
     * @parameter
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
     * @parameter default-value="false
     */
    private boolean noServer;
    
    /**
     * @parameter default-value="8888"
     */
    private int port;
    /**
     * @parameter default-value="8888"
     */
    private int debugPort;
    
    
    protected static final String JAVA_COMMAND = System.getProperty( "java.home") != null ?
        "\""+System.getProperty( "java.home") + File.separator + "bin\"" + File.separator + "java" :
        "java";
    
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
    
    public List buildClasspathList() throws DependencyResolutionRequiredException {
        List items = new ArrayList();
        for( Iterator it = getProject().getRuntimeClasspathElements().iterator(); it.hasNext() ;){
            items.add( new File( it.next().toString() ) );
        }
        items.add( new File(getGwtHome(), "gwt-dev-linux.jar") );
        items.add( new File(getGwtHome(), "gwt-dev-mac.jar") );
        items.add( new File(getGwtHome(), "gwt-dev-windows.jar") );
        items.add( new File(getGwtHome(), "gwt-user.jar"));
        for(Iterator it = project.getResources().iterator(); it.hasNext();  ){
            Resource r = (Resource) it.next();
            items.add( new File( r.getDirectory()) );
        }
        for( Iterator it =  getProject().getCompileSourceRoots().iterator(); it.hasNext() ;){
            items.add( new File( it.next().toString() ) );
        }
        return items;
    }
    
    public ClassLoader fixThreadClasspath(){
        try{
        ClassWorld world = new ClassWorld();

        //use the existing ContextClassLoader in a realm of the classloading space
        ClassRealm root = world.newRealm("gwt-plugin", Thread.currentThread().getContextClassLoader());
        ClassRealm realm = root.createChildRealm( "gwt-project");
        for( Iterator it = buildClasspathList().iterator(); it.hasNext(); ){
            realm.addConstituent( ((File)it.next()).toURL() );
        }
        Thread.currentThread().setContextClassLoader(realm.getClassLoader());
        return realm.getClassLoader();
        } catch(Exception e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    
    public String buildClasspath() throws DependencyResolutionRequiredException {
        StringBuffer sb = new StringBuffer();
        sb.append(
                StringUtils.join(
                buildClasspathList().iterator(),
                File.pathSeparator));
        return sb.toString();
    }
    
    public void makeCatalinaBase() throws Exception {
        File webXml = this.getWebXml();
        String[] args = {
            this.getTomcat().getAbsolutePath(),
            webXml.getAbsolutePath()
        };
        MakeCatalinaBase.main(args);
        if( this.getContextXml() != null && this.getContextXml().exists()){
            FileUtils.copyFile(
                    this.getContextXml(),
                    new File(this.getTomcat(), "conf/gwt/localhost/ROOT.xml"));
        }
    }

    public String getGeneratorRootClasses() {
        return generatorRootClasses;
    }

    public void setGeneratorRootClasses(String generatorRootClasses) {
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

    public int getDebugPort() {
        return debugPort;
    }

    public void setDebugPort(int debugPort) {
        this.debugPort = debugPort;
    }
    
    protected void copyFile(File source, File destination) throws IOException {
        FileOutputStream fos = new FileOutputStream( destination );
        FileInputStream fis = new FileInputStream( source );
        copyStream( fis, fos );
        fos.flush();
        fos.close();
        fis.close();
    }
    
     /**
     * default read size for stream copy
     */
    public static final int DEFAULT_BUFFER_SIZE = 1024;

    /** Copies the data from an InputStream object to an OutputStream object.
     * @param sourceStream The input stream to be read.
     * @param destinationStream The output stream to be written to.
     * @return int value of the number of bytes copied.
     * @exception IOException from java.io calls.
     */
    protected static int copyStream(InputStream sourceStream,OutputStream destinationStream) throws IOException {
        int bytesRead = 0;
        int totalBytes = 0;
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];

        while(bytesRead >= 0) {
            bytesRead = sourceStream.read(buffer,0,buffer.length);

            if(bytesRead > 0) {
                destinationStream.write(buffer,0,bytesRead);
            }

            totalBytes += bytesRead;
        }

        return totalBytes;
    }

}
