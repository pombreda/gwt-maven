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

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.totsp.mavenplugin.gwt.support.beans.Bean;
import com.totsp.mavenplugin.gwt.support.beans.BeanGeneratorBase;
import com.totsp.mavenplugin.gwt.support.beans.BeanGeneratorContext;
import com.totsp.mavenplugin.gwt.support.beans.Generator;
import com.totsp.mavenplugin.gwt.support.beans.GlobalGeneratorContext;
import com.totsp.mavenplugin.gwt.support.beans.TranslatorGenerator;

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

        //Setup the GWT Home using our auto-setup one, if one is not set
        if (getProject().getProperties().getProperty("google.webtoolkit.home") == null) {

          File  targetDir = null;
          try {
            targetDir = new File(getGwtBinDirectory(), this.guessArtifactId() + "-" + getGwtVersion()).getCanonicalFile();
            getProject().getProperties().setProperty("google.webtoolkit.home", targetDir.getCanonicalPath());
            GWT_PATH = targetDir.getCanonicalPath();
          } catch (IOException e) {
             throw new MojoExecutionException(e.getMessage());
          }
        }

        Generator translationGenerator = null;
        GlobalGeneratorContext  global = null;
        ClassLoader loader = this.fixThreadClasspath();
        try{
            String[]    rootClasses = this.getGeneratorRootClasses();
            String src = this.getProject().getCompileSourceRoots().get(0).toString();
            for (int i=0; i<rootClasses.length; i++) {
                
                String packagePath = this.getGeneratorDestinationPackage().replace( '.', File.separatorChar );
                File packageDirectory = new File( src, packagePath );
        
                packageDirectory.mkdirs();
                Bean root = Bean.loadBean( loader.loadClass(rootClasses[i]) );
                BeanGeneratorBase.writeBean(
                        this.getGeneratorDestinationPackage(),
                        packageDirectory, this.isGenerateGettersAndSetters() ,
                        this.isGeneratePropertyChangeSupport(), this.isOverwriteGeneratedClasses(), root );

                if (getTranslatorDestinationPackage() != null) {
                  if (translationGenerator == null) {
                    System.out.println("Generating Translator");
                    global = new GlobalGeneratorContext(isGenerateGettersAndSetters(), isGeneratePropertyChangeSupport(), isOverwriteGeneratedClasses());

                    String translatorPackagePath = getTranslatorDestinationPackage().replace( '.', File.separatorChar );
                    File   tranlatorPath = new File( src, translatorPackagePath );
                    tranlatorPath.mkdirs();
                    translationGenerator = new TranslatorGenerator(getTranslatorDestinationPackage(), tranlatorPath, getGeneratorDestinationPackage(), isTranslatorTwoWay());
                    translationGenerator.Init(global);
                  }
                  BeanGeneratorContext  beanContext = new BeanGeneratorContext(global, root, translationGenerator, translationGenerator.getImportMap());
                  translationGenerator.Generate(beanContext);
                }
            }

          if (translationGenerator != null) {
            translationGenerator.Finish(global);
          }

        } catch(Exception e){
            throw new MojoExecutionException( "Exception running Generator", e );
        }
    }
    
}
