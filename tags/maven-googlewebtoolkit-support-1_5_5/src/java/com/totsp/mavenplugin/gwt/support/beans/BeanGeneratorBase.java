/*
 * BeanGeneratorBase.java
 *
 * Created on February 17, 2007, 9:09 PM
 */

package com.totsp.mavenplugin.gwt.support.beans;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.beans.IntrospectionException;

/**
 * @author cooper
 */
public class BeanGeneratorBase {

  /**
   * Creates a new instance of BeanGeneratorBase
   */
  protected BeanGeneratorBase() {
  }

  private static final HashMap TO_BIGS = new HashMap();

  static {
    TO_BIGS.put("int ", "Integer");
    TO_BIGS.put("double ", "Double");
    TO_BIGS.put("float ", "Float");
    TO_BIGS.put("long ", "Long");
    TO_BIGS.put("byte ", "Byte");
    TO_BIGS.put("char ", "Character");
    TO_BIGS.put("boolean ", "Boolean");
  }

  private static HashMap<String, String> translatedClasses = new HashMap<String, String>();
  private static HashSet<File> writtenFiles = new HashSet<File>();


  static protected void writeHeader(PrintWriter pw, String name, String packageName,
                                    String parent, HashMap<String, String> packages) {
    pw.print("package ");
    pw.print(packageName);
    pw.println(";\n");

    String[] importArray = packages.values().toArray(new String[0]);
    Arrays.sort(importArray);

    for (int i = 0; i < importArray.length; i++) {

      //If the class name starts with our package, and the last '.' is
      //right after the package name, than this class is in our package.
      boolean inSamePackage = importArray[i].startsWith(packageName)
          && (importArray[i].lastIndexOf('.') == packageName.length());

      if (!importArray[i].startsWith("java.lang") && !inSamePackage) {
        pw.print("import ");
        pw.print(importArray[i]);
        pw.println(";");
      }
    }
    pw.print("\n\n");

    pw.print("public class ");
    pw.print(name);

    if (parent != null) {
      pw.print(" extends ");
      pw.print(parent);
    }

    pw.print(" implements IsSerializable");
    pw.println(" {");
  }

  static protected void writePropertyChangeSupport(PrintWriter pw) {
    String propSupportName = "changes" + System.currentTimeMillis();

    pw.print("\tprivate PropertyChangeSupport ");
    pw.print(propSupportName);
    pw.println("= new PropertyChangeSupport( this );\n");
    pw.println("\tpublic void addPropertyChangeListener( PropertyChangeListener l ){ ");
    pw.print("\t\t");
    pw.print(propSupportName);
    pw.println(".addPropertyChangeListener( l ); ");
    pw.println("}\n");

    pw.println("\tprotected PropertyChangeSupport _getPropertyChangeListener(){ ");
    pw.print("\t\treturn ");
    pw.print(propSupportName);
    pw.println("; ");
    pw.println("}\n");


    pw.println("\tpublic void addPropertyChangeListener( String propertyName, PropertyChangeListener l ){");
    pw.print("\t\t");
    pw.print(propSupportName);
    pw.println(".addPropertyChangeListener( propertyName, l );");
    pw.println("\t}\n");

    pw.println("\tpublic void removePropertyChangeListener( PropertyChangeListener l ){ ");
    pw.print("\t\t");
    pw.print(propSupportName);
    pw.println(".removePropertyChangeListener( l );");
    pw.println("\t}\n");

    pw.println("\tpublic void removePropertyChangeListener( String propertyName, PropertyChangeListener l ){ ");
    pw.print("\t\t");
    pw.print(propSupportName);
    pw.println(".removePropertyChangeListener( propertyName, l );");
    pw.println("\t}\n");

    pw.println("\tpublic PropertyChangeListener[] getPropertyChangeListeners(){ ");
    pw.print("\t\t return ");
    pw.print(propSupportName);
    pw.println(".getPropertyChangeListeners();");
    pw.println("\t}\n");
  }

  static protected void writeParameterizedArgs(PrintWriter pw, String packageName, File packageDirectory, boolean getSet,
                                              boolean propSupport, boolean overwrite, Bean propertyType,
                                              HashMap<String, String> packageMap, boolean isNewVal) throws IOException, IntrospectionException {
      pw.println("\t/**");
      pw.print("\t * @gwt.typeArgs ");
      if (isNewVal)
        pw.print(" newValue ");

      Class[] classes = propertyType.getParameterizedTypes();
      pw.print("<");
      for( int i=0; i < classes.length; i++ ){
          Bean  b = Bean.loadBean(classes[i]);
          String translatedName = resolveTranslatedType(packageName, packageDirectory, getSet,
                                              propSupport, overwrite,  b, 
                                              packageMap, true);

          pw.print( translatedName );
          if( i + 1 < classes.length ){
              pw.print(", ");
          }
      }
      pw.println( ">");
      pw.println("\t */");
  }

  static protected void writeProperty(PrintWriter pw, String packageName, File packageDirectory, String propertyName, Bean propertyType,
                                      String access, String typeString, boolean getSet, boolean propSupport, boolean overwrite, HashMap<String, String> packageMap) throws IOException, IntrospectionException {

    for (int i = 0; i < propertyType.getArrayDepth(); i++) {
      typeString += "[]";
    }
    typeString += " ";
    if (propertyType.getTypeArgs() != null) {
      writeParameterizedArgs(pw, packageName, packageDirectory, getSet,
          propSupport, overwrite, propertyType, packageMap, false);
    }

    pw.print("  ");
    pw.print(access);
    pw.print(typeString);
    pw.print(propertyName);
    pw.println(";");

    if (getSet || propSupport) {
      String methodName = propertyName.substring(0, 1).toUpperCase();
      if (propertyName.length() > 1) {
        methodName += propertyName.substring(1, propertyName.length());
      }
      if (propertyType.getTypeArgs() != null) {
        writeParameterizedArgs(pw, packageName, packageDirectory, getSet,
          propSupport, overwrite, propertyType, packageMap, false);
      }
      pw.print("\tpublic ");
      pw.print(typeString);
      pw.print(propertyType.getGetPrefix());
      pw.print(methodName);
      pw.println("(){");
      pw.print("\t\t return this.");
      pw.print(propertyName);
      pw.print(";");
      pw.println("\t}");

      if (propertyType.getTypeArgs() != null) {
        writeParameterizedArgs(pw, packageName, packageDirectory, getSet,
          propSupport, overwrite, propertyType, packageMap, true);
      }
      pw.print("\tpublic void set");
      pw.print(methodName);
      pw.print("( ");
      pw.print(typeString);
      pw.println(" newValue ){");
      if (propSupport) {
        pw.print("\t\t");
        pw.print(typeString);
        pw.print("oldValue = this.");
        pw.print(propertyName);
        pw.println(";");
      }
      pw.print("\t\tthis.");
      pw.print(propertyName);
      pw.println(" = newValue;");

      if (propSupport) {
        pw.print("\t\tthis._getPropertyChangeListener().firePropertyChange(\"");
        pw.print(propertyName);
        //System.out.println( typeString +" "+ TO_BIGS.containsKey( typeString));
        if (TO_BIGS.containsKey(typeString)) {
          pw.println("\", new " + TO_BIGS.get(typeString) + "(oldValue), new " + TO_BIGS.get(typeString) + "(newValue));");
        } else {
          pw.println("\", oldValue, newValue );");
        }

      }
      pw.println("\t}");
    }
  }

  public static void writeBean(String packageName, File packageDirectory, boolean getSet,
                               boolean propSupport, Bean bean) throws IOException, IntrospectionException {
    writeBean(packageName, packageDirectory, getSet, propSupport, false, bean);
  }


  public static void writeBean(String packageName, File packageDirectory, boolean getSet,
                               boolean propSupport, boolean overwrite, Bean bean) throws IOException, IntrospectionException {
    System.out.println(bean.getArrayDepth() + " " + bean.clazz);
    if (translatedClasses.containsKey(bean.clazz.getName())) {
      return;
    }


    HashMap<String, String> importMap = new HashMap<String, String>();

    String beanName = bean.clazz.getSimpleName();
    File javaFile = new File(packageDirectory, beanName + ".java");

    //If we're not overwriting anything, we simply make sure our new
    //classes avoid any files.  If we are overwriting, we need to make sure
    //we avoid any of the classes we have written in the generation.
    if (!overwrite) {
      for (int i = 0; javaFile.exists(); i++) {
        javaFile = new File(packageDirectory, beanName + i + ".java");
      }
    } else {
      for (int i = 0; writtenFiles.contains(javaFile); i++) {
        javaFile = new File(packageDirectory, beanName + i + ".java");
      }
    }
    writtenFiles.add(javaFile);

    //Pull out the name and calculate the new name.
    String name = javaFile.getName();
    String className = name.substring(0, name.indexOf("."));
    String parentName = null;
    String newName = packageName + "." + className;
    translatedClasses.put(bean.clazz.getName(), newName);

    //If this bean inherits from another class else, lets make sure that class is imported, and let's get it's name.
    if (bean.parent!=null) {
        parentName = resolveTranslatedType(packageName, packageDirectory, getSet, propSupport, overwrite, bean.parent, importMap);
    }

    //PrintWriter pw = new PrintWriter( javaFile );
    StringWriter header = new StringWriter();
    StringWriter body = new StringWriter();

    importMap.put("IsSerializable", "com.google.gwt.user.client.rpc.IsSerializable");
    if (propSupport) {
      importMap.put("PropertyChangeSupport", "java.beans.PropertyChangeSupport");
      importMap.put("PropertyChangeListener", "java.beans.PropertyChangeListener");
    }

    String access = getSet || propSupport ? "private " : "public ";
    PrintWriter pwBody = new PrintWriter(body);
    if (propSupport && bean.parent == null) {
      writePropertyChangeSupport(pwBody);
    }

    for (Iterator<String> attributes = bean.properties.keySet().iterator();
         attributes.hasNext();) {
      String att = attributes.next();
      Bean attType = bean.properties.get(att);
      String typeString = resolveTranslatedType(packageName, packageDirectory, getSet, propSupport, overwrite, attType, importMap);

      //Make sure we create beans for all the parameterize type beans
      for (Bean parameterBean : attType.getParameterTypes()) {
        if (parameterBean.isCustom() && !translatedClasses.containsKey(attType)) {
          writeBean(packageName, packageDirectory, getSet, propSupport, overwrite, parameterBean);
        }
      }

      writeProperty(pwBody, packageName, packageDirectory, att, attType, access, typeString, getSet, propSupport, overwrite, importMap);


    }// end atts loop

    //Now that all the imports have been listed, we can write out the header
    PrintWriter pwHeader = new PrintWriter(header);
    writeHeader(pwHeader, className, packageName, parentName, importMap);

    //Now let's write the file
    PrintWriter pw = new PrintWriter(javaFile);
    pw.print(header.getBuffer().toString());
    pw.print(body.getBuffer().toString());
    pw.println("}");
    pw.flush();
    pw.close();
  }

  private static String resolveTranslatedType(String packageName, File packageDirectory, boolean getSet,
                                                boolean propSupport, boolean overwrite, Bean bean,
                                                HashMap<String, String> packageMap) throws IOException, IntrospectionException {
    return resolveTranslatedType(packageName, packageDirectory, getSet, propSupport, overwrite, bean,
                                              packageMap, false);
  }


  private static String resolveTranslatedType(String packageName, File packageDirectory, boolean getSet,
                                              boolean propSupport, boolean overwrite, Bean bean,
                                              HashMap<String, String> packageMap, boolean dontSimplify) throws IOException, IntrospectionException {

    Class clazz = bean.getType();

    String type = null;
    if (clazz == Integer.TYPE) {
      type = "int";
    } else if (clazz == Long.TYPE) {
      type = "long";
    } else if (clazz == Float.TYPE) {
      type = "float";
    } else if (clazz == Double.TYPE) {
      type = "double";
    } else if (clazz == Boolean.TYPE) {
      type = "boolean";
    } else if (clazz == Character.TYPE) {
      type = "char";
    } else if (clazz == Byte.TYPE) {
      type = "byte";
    } else {

      if (bean.isCustom()) {
        writeBean(packageName, packageDirectory, getSet, propSupport, overwrite, bean);
        type = translatedClasses.get(bean.getType().getName());
      } else {
        type = clazz.getName();
      }

      if (!dontSimplify) {
        String nameParts[] = type.split("\\.");
        String simpleName = nameParts[nameParts.length - 1];
        if (!packageMap.containsKey(simpleName) || packageMap.get(simpleName).equals(type)) {
          packageMap.put(simpleName, type);
          type = simpleName;
        }
      }
    }
    return type;
  }

  public String getUsableClassName(String type, HashMap<String, String> packageMap, boolean dontSimplify) {
      String nameParts[] = type.split("\\.");
      String simpleName = nameParts[nameParts.length - 1];

      String packageName = type.substring(0, type.length() - simpleName.length()-1);
      String[]  nestedNames = simpleName.split("\\$");

      String  enclosingClassName = nestedNames[0];
      String  enclosingClassNameFQ = packageName + "." + nestedNames[0];

      String  retVal = enclosingClassNameFQ;
      if (!dontSimplify) {
        if (!packageMap.containsKey(enclosingClassName) || packageMap.get(enclosingClassName).equals(enclosingClassNameFQ)) {
          packageMap.put(enclosingClassName, enclosingClassNameFQ);
          retVal = enclosingClassName;
        }
      }
    
      if (nestedNames.length > 1) {
        StringBuffer  buff = new StringBuffer(retVal);

        for (int i=1; i<nestedNames.length; i++) {
          buff.append(".").append(nestedNames[i]);
        }
        retVal = buff.toString();
      }

      return retVal;
    }
}
