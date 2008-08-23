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
     * @author ccollins
     * 
     * @param mojo
     * @param runtime
     * @return file collection for classpath
     * @throws DependencyResolutionRequiredException
     */
    public static Collection<File> buildClasspathList(final AbstractGWTMojo mojo, final boolean runtime)
            throws DependencyResolutionRequiredException, MojoExecutionException {

        mojo.getLog().info("establishing classpath list (buildClaspathList - runtime = " + runtime + ")");

        File gwtHome = mojo.getGwtHome();
        MavenProject project = mojo.getProject();

        Set<File> items = new LinkedHashSet<File>();

        // inject GWT jars and relative native libs
        // (gwt-user and gwt-dev should be scoped provided to keep them out of
        // other maven stuff - not end up in war, etc)
        if (gwtHome != null) {
            mojo.getLog().info(
                    "google.webtoolkit.home (gwtHome) set, using it for GWT dependencies - " + gwtHome.getAbsolutePath());
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
        if (runtime) {
            for (Iterator it = project.getRuntimeClasspathElements().iterator(); it.hasNext();) {
                items.add(new File(it.next().toString()));
            }
        }
        // add compile time (if not runtime)
        else {
            for (Iterator it = project.getCompileClasspathElements().iterator(); it.hasNext();) {
                items.add(new File(it.next().toString()));
            }
        }

        // add system
        for (Iterator it = project.getSystemClasspathElements().iterator(); it.hasNext();) {
            items.add(new File(it.next().toString()));
        }

        System.out.println("DEBUG SCRIPT CLASSPATH LIST");
        for (File f : items) {
            System.out.println("   " + f.getAbsolutePath());
        }

        return items;
    }

    /**
     * Convenience to build runtime classpath passing true to
     * <code>buildClasspathList</code>.
     * 
     * @return
     * @throws DependencyResolutionRequiredException
     */
    public static Collection<File> buildRuntimeClasspathList(final AbstractGWTMojo mojo)
            throws DependencyResolutionRequiredException, MojoExecutionException {
        Collection<File> classpathItems = BuildClasspathUtil.buildClasspathList(mojo, true);
        Collection<File> items = new LinkedHashSet<File>();
        items.addAll(classpathItems);
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
        mojo.getLog().debug("injecting gwt-user and gwt-dev for script classpath from google.webtoolkit.home");
        mojo.getLog().debug("(and expecting native libs to be relative)");
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
        mojo.getLog().debug("injecting gwt-user and gwt-dev for script classpath from local repository");
        mojo.getLog().debug("(and expecting native libs to be relative)");
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