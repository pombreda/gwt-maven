package com.totsp.mavenplugin.gwt.support.beans;

import java.util.HashMap;
import java.util.HashSet;
import java.io.File;

/**
 * This provides global context for a generator session.  It will encapsulate
 * things such as classes needed to generate, classes that have been generted,
 * mapping between oringal classes and current classes, etc.
 *
 * Author: willpugh  Apr 15, 2007 - 2:46:40 PM
 */
public class GlobalGeneratorContext {

  private HashMap<String, String> processedBeans = new HashMap<String, String>();
  private HashMap<String, String> translatedClasses = new HashMap<String, String>();
  private HashSet<File> writtenFiles = new HashSet<File>();

  private boolean getSet;
  private boolean propSupport;
  private boolean overwriteGeneratedClasses;

  private HashMap properties = new HashMap();

  public GlobalGeneratorContext(boolean getSet, boolean propSupport, boolean overwriteGeneratedClasses) {
    this.getSet = getSet;
    this.propSupport = propSupport;
    this.overwriteGeneratedClasses = overwriteGeneratedClasses;
  }


  public File CreateFile(String packageDir, String baseName) {
    return CreateFile(packageDir, baseName, ".java");
  }

  public File CreateFile(String packageDir, String baseName, String extension) {

    File javaFile = new File(packageDir, baseName + extension);

    //If we're not overwriting anything, we simply make sure our new
    //classes avoid any files.  If we are overwriting, we need to make sure
    //we avoid any of the classes we have written in the generation.
    if (!isOverwriteGeneratedClasses()) {
      for (int i = 0; javaFile.exists(); i++) {
        javaFile = new File(packageDir, baseName + i + extension);
      }
    } else {
      for (int i = 0; writtenFiles.contains(javaFile); i++) {
        javaFile = new File(packageDir, baseName + i + extension);
      }
    }
    writtenFiles.add(javaFile);
    return javaFile;
  }


  public String getTranslatedName(String classname) {
    return translatedClasses.get(classname);
  }

  public void putTranslatedName(String classname, String translatedName) {
      translatedClasses.put(classname, translatedName);
    }


  public boolean hasBeanBeenProcessed(Bean bean, Generator gen) {
    String key = gen.getClass().getName() + "->" + bean.clazz.getName();
    return processedBeans.containsKey(key);
  }

  public void markBeanAsProcessed(Bean bean, Generator gen) {
    String key = gen.getClass().getName() + "->" + bean.clazz.getName();
    processedBeans.put(key, key);    
  }

  public HashMap getProperties() {
    return properties;
  }

  public void setProperties(HashMap properties) {
    this.properties = properties;
  }

  public boolean isGetSet() {
    return getSet | propSupport;
  }

  public void setGetSet(boolean getSet) {
    this.getSet = getSet;
  }

  public boolean isPropSupport() {
    return propSupport;
  }

  public void setPropSupport(boolean propSupport) {
    this.propSupport = propSupport;
  }

  public boolean isOverwriteGeneratedClasses() {
    return overwriteGeneratedClasses;
  }

  public void setOverwriteGeneratedClasses(boolean overwriteGeneratedClasses) {
    this.overwriteGeneratedClasses = overwriteGeneratedClasses;
  }
}
