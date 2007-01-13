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
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.maven.model.Resource;


/**
 *
 * @author cooper
 */
public abstract class AbstractGWTMojo extends AbstractMojo {
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
     * @readonly
     */
    private File defaultWebXml;
    
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
     * @parameter expression="${maven.war.webxml}"
     *
     */
    private File webXml;
    
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
    private String compileTarget;
    
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
    
    /** Creates a new instance of AbstractGWTMojo */
    public AbstractGWTMojo() {
    }
    
    public void setBuildDir(File buildDir) {
        this.buildDir = buildDir;
    }
    
    public File getBuildDir() {
        return buildDir;
    }
    
    public void setCompileTarget(String compileTarget) {
        this.compileTarget = compileTarget;
    }
    
    public String getCompileTarget() {
        return compileTarget;
    }
    
    public void setContextXml(File contextXml) {
        this.contextXml = contextXml;
    }
    
    public File getContextXml() {
        return contextXml;
    }
    
    public void setDefaultWebXml(File defaultWebXml) {
        this.defaultWebXml = defaultWebXml;
    }
    
    public File getDefaultWebXml() {
        return defaultWebXml;
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
    
    public String buildClasspath() throws DependencyResolutionRequiredException {
        StringBuffer sb = new StringBuffer();
        sb.append(
                StringUtils.join(
                getProject().getRuntimeClasspathElements().iterator(),
                File.pathSeparator));
        sb.append(File.pathSeparator);
        sb.append(new File(getGwtHome(), "gwt-dev-linux.jar"));
        sb.append(File.pathSeparator);
        sb.append(new File(getGwtHome(), "gwt-dev-mac.jar"));
        sb.append(File.pathSeparator);
        sb.append(new File(getGwtHome(), "gwt-dev-windows.jar"));
        sb.append(File.pathSeparator);
        sb.append(new File(getGwtHome(), "gwt-user.jar"));
        sb.append(File.pathSeparator);
        ArrayList resources = new ArrayList();
        for(Iterator it = project.getResources().iterator(); it.hasNext();  ){
            Resource r = (Resource) it.next();
            resources.add( r.getDirectory() );
        }
        sb.append(
                StringUtils.join(
                resources.iterator(), File.pathSeparator));
        
        return sb.toString();
    }
    
    public void makeCatalinaBase() throws Exception {
        File webXml = ( this.getWebXml() != null ) ? this.getWebXml() : this.getDefaultWebXml();
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
}
