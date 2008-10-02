package com.totsp.mavenplugin.gwt.support;

import junit.framework.TestCase;

import java.io.File;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.net.URLClassLoader;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.ArrayList;

import org.apache.commons.io.filefilter.WildcardFileFilter;

/**
 * Author: willpugh  Apr 16, 2007 - 3:22:57 PM
 */
public class GenerationBaseCase extends TestCase {

  File    classDir;
  String  classpath;


  public GenerationBaseCase(File classDir) {
    super("GenTests");
    this.classDir = classDir;
    classpath = System.getProperty("java.class.path");

    //Not sure why I need to do this.  Maven must do something weird with classpaths.
    classpath = classpath + File.pathSeparatorChar + "./target/test-classes";

  }

  protected boolean doCompile(File[] pathsToCompile) throws IOException {

    StringWriter  diagWriter = new StringWriter();

    classDir.mkdirs();

    ArrayList<String> params = new ArrayList<String>();

    params.add("-d");
    params.add(classDir.getPath());
    params.add("-cp");
    params.add(classpath);

    System.out.println(classpath);

    for (File dir : pathsToCompile) {
      String[] files = dir.list(new WildcardFileFilter("*.java"));
      for (String currFile : files) {
        params.add(dir + "/" + currFile );
      }
    }
    int retVal = com.sun.tools.javac.Main.compile(params.toArray(new String[0]), new PrintWriter(diagWriter));
    assertEquals(diagWriter.getBuffer().toString(), retVal, 0);

    return retVal==0;
  }

  protected File getPackagePath(String packageName) {
    String packagePath = packageName.replace( '.', File.separatorChar );
    File packageDirectory = new File( "./target/test/gen", packagePath );
    packageDirectory.mkdirs();
    return packageDirectory;
  }

  protected URLClassLoader getClassLoader() throws MalformedURLException {
    return URLClassLoader.newInstance(new URL[] {classDir.toURL()}, this.getClass().getClassLoader());
  }


}
