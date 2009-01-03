/*
 * BeanGeneratorBase.java
 * 
 * Created on February 17, 2007, 9:09 PM
 */

package com.totsp.mavenplugin.gwt.support.beans;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.transform.JDOMSource;

import de.hunsicker.jalopy.Jalopy;



/**
 * @author cooper
 * @author Marek Romanowski
 */
public class BeanGeneratorBase {
  
  

  private static HashSet<File> writtenFiles = new HashSet<File>();
  private static HashMap<String, Element> baseClasses = new HashMap<String, Element>();
  private static HashMap<String, Element> allClasses = new HashMap<String, Element>();



  /**
   * Creates a new instance of BeanGeneratorBase
   */
  protected BeanGeneratorBase() {
  }



  public static void buildBeanElements(String packageName, Bean bean) {
    String cannonical = bean.clazz.getCanonicalName();
    if (allClasses.containsKey(cannonical)) {
      return;
    }
    Element clazz = new Element("class");
    clazz
        .addContent(new Element("package").setText(getPackageName(bean.clazz)))
        .addContent(new Element("shortName").setText(bean.clazz.getSimpleName()))
        .addContent(new Element("extends").addContent(
            new Element("package").setText(
                getPackageName(bean.clazz.getSuperclass()))).addContent(
                    new Element("shortName").setText(
                        bean.clazz.getSuperclass().getSimpleName())));

    for (Entry<String, Bean> entry : bean.properties.entrySet()) {
      Element prop = new Element("property").addContent(
          new Element("name").setText(entry.getKey())).addContent(
          new Element("package").setText(getPackageName(entry.getValue().clazz))).addContent(
          new Element("shortName").setText(entry.getValue().clazz.getSimpleName()));
      if (entry.getValue().getArrayDepth() > 0) {
        prop.addContent(new Element("arrayDepth").setText(
            "" + entry.getValue().getArrayDepth()));
      }
      for (Bean p : entry.getValue().getParameterTypes()) {
        prop.addContent(new Element("parameterType").addContent(
            new Element("package").setText(getPackageName(p.clazz)))
            .addContent(
                new Element("shortName").setText(p.clazz.getSimpleName())));
      }
      clazz.addContent(prop);
    } // end propertyLoop
    boolean found = false;

    for (String outterClassName : allClasses.keySet()) {
      if (cannonical.substring(0, cannonical.lastIndexOf(".")).equals(
          outterClassName)) {
        allClasses.get(outterClassName).addContent(clazz);
        found = true;
        break;
      }
    }
    if (!found) {
      baseClasses.put(cannonical, clazz);
    }
    allClasses.put(cannonical, clazz);

    for (Bean prop : bean.properties.values()) {
      if (prop.isCustom()) {
        buildBeanElements(packageName, prop);
      }
      for (Bean param : prop.getParameterTypes()) {
        if (param.isCustom()) {
          buildBeanElements(packageName, param);
        }
      }
    }
    for (Bean param : bean.getParameterTypes()) {
      if (param.isCustom()) {
        buildBeanElements(packageName, param);
      }
    }
    if (bean.parent != null && bean.parent.isCustom()) {
      buildBeanElements(packageName, bean.parent);
    }

  }



  public static void writeBean(String packageName, File packageDirectory,
      boolean getSet, boolean propSupport, Bean bean) throws IOException {
    writeBean(packageName, packageDirectory, getSet, propSupport, false, bean);
  }



  public static void writeBean(String packageName, File packageDirectory,
      boolean getSet, boolean propSupport, boolean overwrite, Bean bean)
      throws IOException {

    buildBeanElements(packageName, bean);
    for (Entry<String, Element> entry : baseClasses.entrySet()) {

      String beanName = entry.getKey().substring(
          entry.getKey().lastIndexOf(".") + 1);
      File javaFile = new File(packageDirectory, beanName + ".java");

      // If we're not overwriting anything, we simply make sure our new
      // classes avoid any files. If we are overwriting, we need to make sure
      // we avoid any of the classes we have written in the generation.
      if (!overwrite) {
        for (int i = 0; javaFile.exists(); i++) {
          beanName += i;
          javaFile = new File(packageDirectory, beanName + ".java");
        }
      }
      writtenFiles.add(javaFile);
      System.out.print("Generating: " + bean.clazz.getCanonicalName());
      System.out.println(" to file: " + javaFile.getAbsolutePath());
      TransformerFactory xformFactory = TransformerFactory.newInstance();
      try {
        // Get an XSL Transformer object
        Transformer transformer = xformFactory.newTransformer(new StreamSource(
            BeanGeneratorBase.class.getResourceAsStream("./DTO.xsl")));
        transformer.setParameter("propertyChangeSupport", propSupport ? "yes"
            : "no");
        transformer.setParameter("gettersAndSetters", getSet ? "yes" : "no");
        transformer.setParameter("destinationPackage", packageName);

        Document doc = new Document();
        doc.setRootElement(entry.getValue());
        transformer.transform(new JDOMSource(doc), new StreamResult(
            new FileOutputStream(javaFile)));
        doc.detachRootElement();
        Jalopy jalopy = new Jalopy();

        // specify input and output target
        jalopy.setInput(javaFile);
        jalopy.setOutput(javaFile);

        // format and overwrite the given input file
        jalopy.format();

        if (jalopy.getState() == Jalopy.State.OK)
          System.out.println(javaFile + " successfully formatted");
        else if (jalopy.getState() == Jalopy.State.WARN)
          System.out.println(javaFile + " formatted with warnings");
        else if (jalopy.getState() == Jalopy.State.ERROR)
          System.out.println(javaFile + " could not be formatted");

      } catch (TransformerConfigurationException ex) {
        ex.printStackTrace();
      } catch (TransformerException ex) {
        ex.printStackTrace();
      }
    }
  }



  static String getPackageName(Class clazz) {
    return clazz.getCanonicalName().indexOf(".") == -1 ? "java.lang" : clazz
        .getCanonicalName().substring(0,
            clazz.getCanonicalName().lastIndexOf("."));
  }
}


