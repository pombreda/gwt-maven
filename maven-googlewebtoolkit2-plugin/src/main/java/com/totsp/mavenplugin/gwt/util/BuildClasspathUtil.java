package com.totsp.mavenplugin.gwt.util;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Resource;
import org.apache.maven.project.MavenProject;

import com.totsp.mavenplugin.gwt.AbstractGWTMojo;

public class BuildClasspathUtil {

   /**
    * Build classpath list using either gwtHome (if present) or using *project* dependencies.
    * Note that this is ONLY used for the script/cmd writers (so the scopes are not for the compiler, or war plugins, etc).
    * 
    * @param fRuntime
    * @return
    * @throws DependencyResolutionRequiredException
    */
   public static Collection<File> buildClasspathList(final AbstractGWTMojo mojo, final boolean runtime)
            throws DependencyResolutionRequiredException {

      System.out.println("establishing classpath list (buildClaspathList - runtime = " + runtime + ")");

      File gwtHome = mojo.getGwtHome();
      MavenProject project = mojo.getProject();

      Set<File> items = new LinkedHashSet<File>();

      // add GWT jars and relative native libs
      // (note that gwt-user should be scoped provided so it does not end up in WAR, etc  . . .
      // however, we do need it for the shell, so it's a special scope and we inject it here even for runtime scope)
      if (gwtHome != null) {
         System.out.println("google.webtoolkit.home set, using directory for GWT dependencies, rather than POM - "
                  + gwtHome.getAbsolutePath());
         items.add(gwtHome);
         items.add(new File(gwtHome, "gwt-user.jar"));
         items.add(new File(gwtHome, ArtifactNameUtil.guessDevJarName()));
      }
      else {
         System.out.println("google.webtoolkit.home *not* set, using project POM for GWT dependencies");
         System.out.println("(injecting gwt-user and expecting native libs to be relative)");
         // we ALSO need to get gwt-user
         // we need it when building scripts - but it wont be present in any getClasspathElements call (scoped provided)
         // (adding it here does NOT mean it will be added in to other plugins, like the war plugin, for example)
         // (we get it from dependency list because it's scope [provided] excludes it from everything else at this point)          

         ArtifactRepository repo = mojo.getLocalRepository();
         Artifact gwtUser = mojo.getArtifactFactory().createArtifactWithClassifier("com.google.gwt", "gwt-user",
                  mojo.getGwtVersion(), "jar", null);
         try {
            mojo.getResolver().resolve(gwtUser, null, mojo.getLocalRepository());
            items.add(gwtUser.getFile());
         }
         catch (ArtifactNotFoundException e) {
            // TODO
            e.printStackTrace();
         }
         catch (ArtifactResolutionException e) {
            // TODO
            e.printStackTrace();
         }
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

      System.out.println("DEBUG CLASSPATH LIST");
      for (File f : items) {
         System.out.println("   " + f.getAbsolutePath());
      }

      return items;
   }

   /**
    * Convenience to build runtime classpath passing true to <code>buildClasspathList</code>.
    * 
    * @return
    * @throws DependencyResolutionRequiredException
    */
   public static Collection<File> buildRuntimeClasspathList(final AbstractGWTMojo mojo)
            throws DependencyResolutionRequiredException {
      Collection<File> classpathItems = BuildClasspathUtil.buildClasspathList(mojo, true);
      Collection<File> items = new LinkedHashSet<File>();

      /* buildClasspathList already does this?
      for (Iterator it = project.getResources().iterator(); this.getSourcesOnPath() && it.hasNext();) {
         Resource r = (Resource) it.next();
         items.add(new File(r.getDirectory()));
      }
      */

      items.addAll(classpathItems);
      return items;
   }

}