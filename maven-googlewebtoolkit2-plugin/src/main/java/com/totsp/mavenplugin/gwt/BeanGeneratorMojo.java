/*
 * BeanGeneratorMojo.java
 *
 * Created on February 17, 2007, 8:53 PM
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

import com.totsp.mavenplugin.gwt.support.beans.Bean;
import com.totsp.mavenplugin.gwt.support.beans.BeanGeneratorBase;
import java.io.File;
import java.util.StringTokenizer;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * @requiresDependencyResolution compile
 * @goal generateClientBeans
 * @author cooper
 */
public class BeanGeneratorMojo extends AbstractGWTMojo {
    
    /** Creates a new instance of BeanGeneratorMojo */
    public BeanGeneratorMojo() {
    }
    
    public void execute() throws MojoExecutionException, MojoFailureException {
        if( this.getGeneratorRootClasses() == null ){
            throw new MojoExecutionException( "No root classes specified : make sure your POM has a generatorRootClasses set.");
        }

        if (this.getGeneratorDestinationPackage() == null) {
            throw new MojoExecutionException( "No destination package specified :\n    <generatorRootClasses> was specified in the POM without a <generatorDestinationPackage> being specified as well.");
        }

        ClassLoader loader = this.fixThreadClasspath();
        try{
            String[]    rootClasses = this.getGeneratorRootClasses();
            String src = this.getProject().getCompileSourceRoots().get(0).toString();
            for (int i=0; i<rootClasses.length; i++) {
                
                String packagePath = this.getGeneratorDestinationPackage().replace( '.', File.separatorChar );
                File packageDirectory = new File( src, packagePath );
        
                packageDirectory.mkdirs();
                Bean root = new Bean( loader.loadClass(rootClasses[i] ) );
                BeanGeneratorBase.writeBean(
                        this.getGeneratorDestinationPackage(),
                        packageDirectory, this.isGenerateGettersAndSetters() ,
                        this.isGeneratePropertyChangeSupport(), root );
            }
        } catch(Exception e){
            throw new MojoExecutionException( "Exception running Generator", e );
        }
    }
    
}
