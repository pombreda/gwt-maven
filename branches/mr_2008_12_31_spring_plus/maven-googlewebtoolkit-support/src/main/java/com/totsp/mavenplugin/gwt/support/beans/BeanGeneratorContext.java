package com.totsp.mavenplugin.gwt.support.beans;

import java.util.HashMap;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Author: willpugh  Apr 15, 2007 - 2:46:52 PM
 */
public class BeanGeneratorContext {

  private final GlobalGeneratorContext globalContext;
  private final Bean bean;

  private Generator gen;

  HashMap<String, String> importMap = new HashMap<String, String>();

  public BeanGeneratorContext(GlobalGeneratorContext globalContext, Bean bean) {
    this(globalContext, bean, null);
  }

  public BeanGeneratorContext(GlobalGeneratorContext globalContext, Bean bean, Generator gen) {
    this(globalContext, bean, gen, null);
  }

  public BeanGeneratorContext(GlobalGeneratorContext globalContext, Bean bean, Generator gen, HashMap<String, String> importMap) {
    this.globalContext = globalContext;
    this.bean = bean;
    this.setGen(gen);
    this.importMap = importMap;
  }


  public boolean markAsGenerated() {
    // successfully start our scope if the bean has not been processed yet.
    if ( !globalContext.hasBeanBeenProcessed(bean, gen)) {
      globalContext.markBeanAsProcessed(bean, gen);
      return false;
    } 
      return true;
  }

  

  /**
   * This will run through all the imports that were stored up from translating class names,
   * and will return the list of packages that are not in this package and are not in java.lang.
   * @param packageName the package name of the class being generated
   * @return
   */
  static public String[] getImportList(String packageName, HashMap<String, String> importMap) {

    ArrayList<String> keepers = new ArrayList<String>(importMap.size());


    Iterator<String> iter = importMap.values().iterator();
    while (iter.hasNext()) {
      String  curr = iter.next();

      //If the class name starts with our package, and the last '.' is
      //right after the package name, than this class is in our package.
      boolean inSamePackage = curr.startsWith(packageName)
          && (curr.lastIndexOf('.') == packageName.length());

      if (!curr.startsWith("java.lang") && !inSamePackage) {
        keepers.add(curr);
      }
    }
    String[]  importArray = keepers.toArray(new String[0]);

    Arrays.sort(importArray);
    return importArray;
  }


  /**
   * This will actually write the import list out to a printwriter.
   *
   * @param pw the PrintWriter to write to
   * @param packageName the package name of the class being generated
   */
  public void writeImportList(PrintWriter pw, String packageName) {
    String[]  imports = getImportList(packageName, importMap);

    for (String str : imports) {
      pw.print("import ");
      pw.print(str);
      pw.println(";");
    }
  }

  /**
   * This will get the parent class name to write to the file.
   * @return
   */
  public String getParentName() throws IOException {
    if (bean.parent == null)
      return null;

    return resolveTranslatedType(bean.parent);
  }

  protected String resolveTranslatedType(Bean bean) throws IOException {

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
          getGen().Generate(new BeanGeneratorContext(getGlobalContext(), bean, getGen(), getGen().getImportMap()));
          type = getGlobalContext().getTranslatedName(bean.getType().getName());
        } else {
          type = clazz.getName();
        }

        type = getUsableClassName(type);
      }
      return type;
    }

    protected String resolveOriginalType(Bean bean) {

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
          type = getUsableClassName(clazz.getName());
        }
        return type;
      }


    public String getUsableClassName(String type) {
      String nameParts[] = type.split("\\.");
      String simpleName = nameParts[nameParts.length - 1];

      String packageName = type.substring(0, type.length() - simpleName.length()-1);
      String[]  nestedNames = simpleName.split("\\$");

      String  enclosingClassName = nestedNames[0];
      String  enclosingClassNameFQ = packageName + "." + nestedNames[0];

      String  retVal = enclosingClassNameFQ;
      if (!importMap.containsKey(enclosingClassName) || importMap.get(enclosingClassName).equals(enclosingClassNameFQ)) {
        importMap.put(enclosingClassName, enclosingClassNameFQ);
        retVal = enclosingClassName;
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

  public GlobalGeneratorContext getGlobalContext() {
    return globalContext;
  }

  public Bean getBean() {
    return bean;
  }

  public Generator getGen() {
    return gen;
  }

  public void setGen(Generator gen) {
    this.gen = gen;
  }

  public String getGetPrefix() {
    return getBean().getGetPrefix();
  }

  public String getGeneratedGet(String object, String attr) {
    return getBean().getGeneratedGet(object, attr, getGlobalContext().isGetSet());
  }

  public String getGeneratedSet(String object, String attr, String value) {
    return getBean().getGeneratedSet(object, attr, value, getGlobalContext().isGetSet());
  }

  public String getOriginalGet(String object, String attr) {
    return getBean().getOriginalGet(object, attr);
  }

  public String getOriginalSet(String object, String attr, String value) {
    return getBean().getOriginalSet(object, attr, value);
  }



}

