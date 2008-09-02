/*
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
package com.totsp.mavenplugin.gwt.util;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import com.totsp.mavenplugin.gwt.AbstractGWTMojo;
import com.totsp.mavenplugin.gwt.util.ArtifactNameUtil;

public class BuildClasspathUtil {    

    /**
     * Build classpath list using either gwtHome (if present) or using *project*
     * dependencies. Note that this is ONLY used for the script/cmd writers (so
     * the scopes are not for the compiler, or war plugins, etc).
     * 
     * This is required so that the script writers can get the dependencies they need
     * regardless of the Maven scopes (still want to use the Maven scopes for everything else
     * Maven, but for GWT-Maven we need to access deps differently - directly at times).
     * 
     * @author ccollins
     * 
     * @param mojo
     * @param scope
     * @return file collection for classpath
     * @throws DependencyResolutionRequiredException
     */
    public static Collection<File> buildClasspathList(final AbstractGWTMojo mojo, final DependencyScope scope)
            throws DependencyResolutionRequiredException, MojoExecutionException {

        mojo.getLog().info("establishing classpath list (buildClaspathList - scope = " + scope + ")");

        File gwtHome = mojo.getGwtHome();
        MavenProject project = mojo.getProject();

        Set<File> items = new LinkedHashSet<File>();

        // inject GWT jars and relative native libs for all scopes
        // (gwt-user and gwt-dev should be scoped provided to keep them out of
        // other maven stuff - not end up in war, etc - this util is only used for GWT-Maven scripts)
        // TODO filter the rest of the stuff so we don't double add these
        if (gwtHome != null) {
            mojo.getLog().info(
                    "google.webtoolkit.home (gwtHome) set, using it for GWT dependencies - "
                            + gwtHome.getAbsolutePath());
            items.addAll(BuildClasspathUtil.injectGwtDepsFromGwtHome(gwtHome, mojo));
        } else {
            mojo.getLog().info("google.webtoolkit.home (gwtHome) *not* set, using project POM for GWT dependencies");
            items.addAll(BuildClasspathUtil.injectGwtDepsFromRepo(mojo));
        }

        // add sources and resources
        if (mojo.getSourcesOnPath()) {
            for (Iterator it = project.getCompileSourceRoots().iterator(); it.hasNext();) {
                items.add(new File(it.next().toString()));
            }
            for (Iterator it = project.getResources().iterator(); it.hasNext();) {
                Resource r = (Resource) it.next();
                items.add(new File(r.getDirectory()));
            }
        }

        // add classes dir
        items.add(new File(project.getBasedir(), "classes"));

        // if runtime add runtime
        if (scope == DependencyScope.RUNTIME) {
            for (Iterator it = project.getRuntimeClasspathElements().iterator(); it.hasNext();) {
                items.add(new File(it.next().toString()));
            }            
        }
        
        // if test add test
        if (scope == DependencyScope.TEST) {
            for (Iterator it = project.getTestClasspathElements().iterator(); it.hasNext();) {
                items.add(new File(it.next().toString()));
            }
            
            // add test sources and resources
            for (Iterator it = project.getTestCompileSourceRoots().iterator(); it.hasNext();) {
                items.add(new File(it.next().toString()));
            }
            for (Iterator it = project.getTestResources().iterator(); it.hasNext();) {
                Resource r = (Resource) it.next();
                items.add(new File(r.getDirectory()));
            }
        } 
        
        // add compile (even when scope is other than)
        for (Iterator it = project.getCompileClasspathElements().iterator(); it.hasNext();) {
            items.add(new File(it.next().toString()));
        }

        // add system 
        for (Iterator it = project.getSystemClasspathElements().iterator(); it.hasNext();) {
            items.add(new File(it.next().toString()));
        }        

        mojo.getLog().debug("SCRIPT INJECTION CLASSPATH LIST");
        for (File f : items) {
            mojo.getLog().debug("   " + f.getAbsolutePath());
        }

        return items;
    }

    /**
     * Helper to inject gwt-user and gwt-dev into classpath from gwtHome, ONLY
     * for compile and run scripts.
     * 
     * @param mojo
     * @return
     */
    public static Collection<File> injectGwtDepsFromGwtHome(final File gwtHome, final AbstractGWTMojo mojo) {
        mojo
                .getLog()
                .debug(
                        "injecting gwt-user and gwt-dev for script classpath from google.webtoolkit.home (and expecting relative native libs)");
        Collection<File> items = new LinkedHashSet<File>();
        File userJar = new File(gwtHome, "gwt-user.jar");
        File devJar = new File(gwtHome, ArtifactNameUtil.guessDevJarName());
        items.add(userJar);
        items.add(devJar);
        return items;
    }

    /**
     * Helper to inject gwt-user and gwt-dev into classpath from repo, ONLY for
     * compile and run scripts.
     * 
     * @param mojo
     * @return
     */
    public static Collection<File> injectGwtDepsFromRepo(final AbstractGWTMojo mojo) throws MojoExecutionException {
        mojo
                .getLog()
                .debug(
                        "injecting gwt-user and gwt-dev for script classpath from local repository (and expecting relative native libs)");
        Collection<File> items = new LinkedHashSet<File>();

        Artifact gwtUser = mojo.getArtifactFactory().createArtifactWithClassifier("com.google.gwt", "gwt-user",
                mojo.getGwtVersion(), "jar", null);
        Artifact gwtDev = mojo.getArtifactFactory().createArtifactWithClassifier("com.google.gwt", "gwt-dev",
                mojo.getGwtVersion(), "jar", ArtifactNameUtil.getPlatformName());

        try {
            mojo.getResolver().resolve(gwtUser, null, mojo.getLocalRepository());
            mojo.getResolver().resolve(gwtDev, null, mojo.getLocalRepository());
            items.add(gwtUser.getFile());
            items.add(gwtDev.getFile());
        } catch (ArtifactNotFoundException e) {
            throw new MojoExecutionException("artifact not found - " + e.getMessage(), e);
        } catch (ArtifactResolutionException e) {
            throw new MojoExecutionException("artifact resolver problem - " + e.getMessage(), e);
        }

        return items;
    }
}