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
import java.io.FileInputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.artifact.ActiveProjectArtifact;

import com.totsp.mavenplugin.gwt.AbstractGWTMojo;

/**
 * Util to consolidate classpath manipulation stuff in one place.
 * 
 * @author ccollins
 */
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
	List<String> transformedArtifacts = new LinkedList<String>();

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
        	addSourcesAndResourcesWithActiveProjects(project, items, DependencyScope.COMPILE);
        }

        // add classes dir
        items.add(new File(project.getBasedir(), "classes"));

        // if runtime add runtime
        if (scope == DependencyScope.RUNTIME) {
            for (Iterator it = project.getRuntimeClasspathElements().iterator(); it.hasNext();) {
            	transformClassPathUsingOptionalDevPaths(mojo, items, transformedArtifacts, it.next().toString());
            }            
        }
        
        // if test add test
        if (scope == DependencyScope.TEST) {
            for (Iterator it = project.getTestClasspathElements().iterator(); it.hasNext();) {
            	transformClassPathUsingOptionalDevPaths(mojo, items, transformedArtifacts, it.next().toString());
            }
            
            // add test sources and resources
            addSourcesAndResourcesWithActiveProjects(project, items, scope);
        } 
        
        // add compile (even when scope is other than)
        for (Iterator it = project.getCompileClasspathElements().iterator(); it.hasNext();) {
        	transformClassPathUsingOptionalDevPaths(mojo, items, transformedArtifacts, it.next().toString());
        }

        // add system 
        for (Iterator it = project.getSystemClasspathElements().iterator(); it.hasNext();) {
        	transformClassPathUsingOptionalDevPaths(mojo, items, transformedArtifacts, it.next().toString());
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

      List remoteRepositories = mojo.getRemoteRepositories();

      try {
         mojo.getResolver().resolve(gwtUser, remoteRepositories, mojo.getLocalRepository());
         mojo.getResolver().resolve(gwtDev, remoteRepositories, mojo.getLocalRepository());
         items.add(gwtUser.getFile());
         items.add(gwtDev.getFile());
      }
      catch (ArtifactNotFoundException e) {
         throw new MojoExecutionException("artifact not found - " + e.getMessage(), e);
      }
      catch (ArtifactResolutionException e) {
         throw new MojoExecutionException("artifact resolver problem - " + e.getMessage(), e);
      }

      return items;
   }
    
    /**
     * Add all sources and resources also with active (maven reactor active) referenced project sources and resources.
     * Addresses issue no. 147.
     * 
     * @param project
     * @param items
     * @param scope
     */
    private static void addSourcesAndResourcesWithActiveProjects(final MavenProject project,
			final Set<File> items, final DependencyScope scope) {
    	final List<Artifact> scopeArtifacts = getScopeArtifacts(project, scope);
    	
    	addSourcesAndResources(items, getSourceRoots(project, scope), getResources(project, scope));
    	
    	for(Artifact artifact : scopeArtifacts) {
    		if(artifact instanceof ActiveProjectArtifact) {
    			MavenProject refProject = (MavenProject)project.getProjectReferences().get(getProjectReferenceId(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion()));
    			if(refProject != null) {
    				addSourcesAndResources(items, getSourceRoots(refProject, scope), getResources(refProject, scope));
    			}
    		}
    	}
    }
    
    /**
     * Get artifacts for specific scope.
     * @param project
     * @param scope
     * @return
     */
    @SuppressWarnings("unchecked")
	private static List<Artifact> getScopeArtifacts(final MavenProject project, final DependencyScope scope) {
    	if(DependencyScope.COMPILE.equals(scope)) {
    		return project.getCompileArtifacts();
    	} else if(DependencyScope.TEST.equals(scope)) {
    		return project.getTestArtifacts();
    	} else {
    		throw new RuntimeException("Not allowed scope " + scope);
    	}
    }
    
    /**
     * Get source roots for specific scope.
     * @param project
     * @param scope
     * @return
     */
    @SuppressWarnings("unchecked")
	private static List getSourceRoots(final MavenProject project, final DependencyScope scope) {
    	if(DependencyScope.COMPILE.equals(scope)) {
    		return project.getCompileSourceRoots();
    	} else if(DependencyScope.TEST.equals(scope)) {
    		return project.getTestCompileSourceRoots();
    	} else {
    		throw new RuntimeException("Not allowed scope " + scope);
    	}
    }
    
    /**
     * Get resources for specific scope.
     * @param project
     * @param scope
     * @return
     */
    @SuppressWarnings("unchecked")
	private static List<Artifact> getResources(final MavenProject project, final DependencyScope scope) {
    	if(DependencyScope.COMPILE.equals(scope)) {
    		return project.getResources();
    	} else if(DependencyScope.TEST.equals(scope)) {
    		return project.getTestResources();
    	} else {
    		throw new RuntimeException("Not allowed scope " + scope);
    	}
    }
    
    /**
     * Add source path and resource paths of the project to the list of classpath items.
     * @param items Classpath items.
     * @param sourceRoots
     * @param resources
     */
	@SuppressWarnings("unchecked")
	private static void addSourcesAndResources(final Set<File> items, final List sourceRoots, final List resources) {
		for (Iterator it = sourceRoots.iterator(); it
				.hasNext();) {
			items.add(new File(it.next().toString()));
		}
		for (Iterator it = resources.iterator(); it.hasNext();) {
			Resource r = (Resource) it.next();
			items.add(new File(r.getDirectory()));
		}
	}

	/**
	 * Cut from MavenProject.java
	 * 
	 * @param groupId
	 * @param artifactId
	 * @param version
	 * @return
	 */
	private static String getProjectReferenceId(final String groupId,
			final String artifactId, final String version) {
		return groupId + ":" + artifactId + ":" + version;
	}
	
    /**
	 * Transforms artifact's direct JAR classpath using its project location
	 * from artifact development projects file definition. It directly modifies
	 * built classpath and uses optimization for already transformed artifacts.
	 * 
	 * @param mojo
	 * @param items
	 *            Classpath items.
	 * @param transformedArtifacts
	 *            Already transformed artifacts so we don't transform twice or
	 *            more.
	 * @param artifactClassPath
	 *            Current proposed classpath for artifact. If no transformation
	 *            rule, this will be used directly.
	 */
	private static void transformClassPathUsingOptionalDevPaths(
			final AbstractGWTMojo mojo, final Set<File> items,
			final List<String> transformedArtifacts,
			final String artifactClassPath) {
		if (mojo.getArtifactDevelProjects() == null
				|| !mojo.getArtifactDevelProjects().exists()) {
			// we don't transform anything because nobody wants to
			items.add(new File(artifactClassPath));
			return;
		}

		File projectDescriptor;
		try {
			projectDescriptor = getArtifactProjectPath(mojo, artifactClassPath,
					transformedArtifacts);
		} catch (ArtifactProjectException e1) {
			// we transformed this artifact before so there is no need to
			// duplicate it.
			return;
		}
		if (projectDescriptor == null) {
			// there is no entry in project descriptor file
			items.add(new File(artifactClassPath));
			return;
		}

		MavenProject project;
		try {
			project = mojo.getProjectBuilder().build(projectDescriptor,
					mojo.getLocalRepository(), null);
		} catch (ProjectBuildingException e) {
			throw new RuntimeException("Cannot build project for pom file "
					+ projectDescriptor, e);
		}

		for (Iterator it = project.getCompileSourceRoots().iterator(); it
				.hasNext();) {
			items.add(new File(it.next().toString()));
		}
		for (Iterator it = project.getResources().iterator(); it.hasNext();) {
			Resource r = (Resource) it.next();
			items.add(new File(r.getDirectory()));
		}
		items.add(new File(project.getBuild().getOutputDirectory()));
	}

	/**
	 * Transforms artifact's classpath using its project path.
	 * 
	 * @param mojo
	 * @param artifactClassPath
	 * @param transformedArtifacts
	 * @return Null if there is no match in artifact project file definition,
	 *         else pom.xml location of found project definition.
	 * @throws ArtifactProjectException
	 *             Indicates that artifact was already transformed.
	 */
	private static File getArtifactProjectPath(final AbstractGWTMojo mojo,
			final String artifactClassPath,
			final List<String> transformedArtifacts)
			throws ArtifactProjectException {
		Properties props = new Properties();
		try {
			props.loadFromXML(new FileInputStream(mojo
					.getArtifactDevelProjects()));
		} catch (Exception e) {
			throw new RuntimeException("Cannot read artifact projects file "
					+ mojo.getArtifactDevelProjects(), e);
		}
		for (Object key : props.keySet()) {
			String strKey = (String) key;
			if (artifactClassPath.contains(ArtifactNameUtil
					.getMavenPathPartFromArtifactDescription(strKey))) {
				if (transformedArtifacts.contains(strKey)) {
					throw new ArtifactProjectException(
							"Artifact was transformed.");
				}
				transformedArtifacts.add(strKey);
				return new File((String) props.get(key));
			}
		}
		return null;
	}

	private static class ArtifactProjectException extends Exception {
		private static final long serialVersionUID = -1651064607009117705L;

		public ArtifactProjectException(String arg0) {
			super(arg0);
		}
	}	
}