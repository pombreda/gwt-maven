package com.totsp.mavenplugin.gwt.support.beans;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.*;

/**
 * This is a class that generates a "Translator".  This will
 * translate from the class that a bean is generated from , to
 * the generated bean and vice versa.  This can be used on the
 * Java side to tranlate between Business Objects and the IsSerialiable
 * objects that go down to the client.
 *
 * UNDONE(willpugh) -- Find a way to merge this better with BeanGeneratorBase to
 * reuse more code.
 *
 * Author: willpugh  Apr 14, 2007 - 12:30:44 AM
 */
public class TranslatorGenerator implements Generator{

    protected static HashMap<Class, CollectionInfo> collectionInfo = new HashMap<Class, CollectionInfo>();

    static {
      //UNDONE(willpugh) -- This is pretty broken.  The Bean Gen should choose collection based on class +
      //interfaces implemented.  There should probably be some "backoff" heuristic where we try to allocate
      // the more specific class, and then back off to more general ones if there is a problem
      collectionInfo.put(java.util.Map.class, new CollectionInfo("new java.util.HashMap()", AddType.map));
      collectionInfo.put(java.util.HashMap.class, new CollectionInfo("new java.util.HashMap()", AddType.map));
      collectionInfo.put(java.util.Collection.class, new CollectionInfo("new java.util.ArrayList()", AddType.collection));
      collectionInfo.put(java.util.List.class, new CollectionInfo("new java.util.ArrayList()", AddType.collection));
      collectionInfo.put(java.util.ArrayList.class, new CollectionInfo("new java.util.ArrayList()", AddType.collection));
      collectionInfo.put(java.util.Set.class, new CollectionInfo("new java.util.HashSet()", AddType.collection));
      collectionInfo.put(java.util.HashSet.class, new CollectionInfo("new java.util.HashSet()", AddType.collection));
      collectionInfo.put(java.util.Vector.class, new CollectionInfo("new java.util.Vector()", AddType.collection));
      collectionInfo.put(java.util.Stack.class, new CollectionInfo("new java.util.Stack()", AddType.stack));
      collectionInfo.put(java.util.Stack.class, new CollectionInfo("null", AddType.iterator));

    }

    enum AddType {
      collection,
      map,
      stack,
      iterator
    }
    public static class CollectionInfo {
      final public String   allocString;
      final public AddType  addType;


      public CollectionInfo(String allocString, AddType addType) {
        this.allocString = allocString;
        this.addType = addType;
      }
    }

    File    packageDir;
    String  className = "Translater";
    String  packageName;
    String  beanPackageName;
    File    fileName;
    boolean twoWay;
    HashMap<Class, String> translatorMap = new HashMap<Class, String>();
    HashMap<String, String> importMap = new HashMap<String, String>();
    ArrayList<String> methods = new ArrayList<String>();

  public TranslatorGenerator(String packageName, File packageDir, String beanPackageName, boolean twoWay) {
      this.packageName = packageName;
      this.packageDir = packageDir;
      this.beanPackageName = beanPackageName;
      this.twoWay = twoWay;
    }

  protected void writeHeader(PrintWriter pw, String parentName) throws IOException {
    pw.print("package ");
    pw.print(packageName);
    pw.println(";\n");

    String[]  imports = BeanGeneratorContext.getImportList(packageName, importMap);
    for (String str : imports) {
      pw.print("import ");
      pw.print(str);
      pw.println(";");
    }
   
    pw.print("\n\n");

    pw.print("public class ");
    pw.print(className);

    if (parentName != null) {
      pw.print(" extends ");
      pw.print(parentName);
    }

    pw.println(" {");
  }

  public void fun() {

}

  protected String getTabDepth(int depth) {
    StringBuffer buf = new StringBuffer(depth+1);

    for (int i=0; i<=depth+1; i++)
      buf.append("\t\t");

    return buf.toString();
  }

  protected char getVarNameForDepth(int depth) {
    return (char)('a' + depth);
  }

  protected String getPreviousArrayString(String attr, int depth, boolean isGetSet) {
    if (depth == 0) {
      return isGetSet ? "get" + Bean.getCamelCase(attr) + "()" : attr;
    } else {
      return getPreviousArrayString(attr, depth-1, isGetSet) + "[" + getVarNameForDepth(depth-1) + "]";
    }
  }

  protected String getArrayString(String attr, int depth, boolean isGetSet) {
    return getPreviousArrayString(attr, depth, isGetSet) + "[" + getVarNameForDepth(depth) + "]";
  }

  protected String getArrayType(String baseType, String sizeToAllocate, int totalDepth, int currDepth) {
    StringBuffer  buff = new StringBuffer(baseType);
    for (int i=currDepth; i<totalDepth; i++) {
      if (i==currDepth)
        buff.append("[").append(sizeToAllocate).append("]");
      else
        buff.append("[]");
    }
    return buff.toString();
  }

  protected static abstract class ParamGen {
    BeanGeneratorContext beanContext;
    boolean              isGetSet;

    protected ParamGen(BeanGeneratorContext beanContext, boolean isGetSet) {
      this.beanContext = beanContext;
      this.isGetSet = isGetSet;
    }

    boolean isGetSet() {
      return isGetSet;
    }

    public   String  getUsableClassName(String type) {
      return beanContext.getUsableClassName(type);
    }

    abstract String  resolveType(Bean attType) throws IOException;
    abstract String  getGet(String object, String attr);
    abstract String  getSet(String object, String attr, String value);
  }

  protected static class OriginalParamGen extends ParamGen{

    protected OriginalParamGen(BeanGeneratorContext beanContext) {
      super(beanContext, true);
    }

    public String resolveType(Bean attType) throws IOException {
      return beanContext.resolveOriginalType(attType);
    }

    public String getGet(String object, String attr) {
      return beanContext.getOriginalGet(object, attr);
    }

    public String getSet(String object, String attr, String value) {
      return beanContext.getOriginalSet(object, attr, value);
    }
  }

  protected static class TranslatedParamGen extends ParamGen{

    protected TranslatedParamGen(BeanGeneratorContext beanContext) {
      super(beanContext, beanContext.getGlobalContext().isGetSet());
    }

    public String resolveType(Bean attType) throws IOException {
      return beanContext.resolveTranslatedType(attType);
    }

    public String getGet(String object, String attr) {
      return beanContext.getGeneratedGet(object, attr);
    }

    public String getSet(String object, String attr, String value) {
      return beanContext.getGeneratedSet(object, attr, value);
    }
  }

  public void writeSnippet(PrintWriter pw, String src, String dest, String attName,
                           Bean attType, int depth, ParamGen srcGen, ParamGen destGen) throws IOException {
    //First create the Translation from one bean to the next

    //  If Simple Type, just a transfer
    //  If Array, go over each element and call appropriate transfer
    //  If Colleciton, go over each element and call appropriate transfer
    //  If User Type, call TranslateX.translate on it.

    String camelName = Bean.getCamelCase(attName);
    String tab = getTabDepth(depth);

    char  variableName = getVarNameForDepth(depth);

    String  SrcAttType = srcGen.resolveType(attType);
    String  DestAttType = destGen.resolveType(attType);


    //First, see if we are an array
    if (attType.getArrayDepth() > depth) {

      String  srcArray = src + "." + getPreviousArrayString(attName, depth, srcGen.isGetSet());
      String  destArray = dest + "." + getPreviousArrayString(attName, depth, destGen.isGetSet());

      if (attType.getArrayDepth() > depth) {
        pw.println(tab + "if (" + srcArray + " != null) {");
        if (depth == 0)
          pw.println(tab + "\t" + destGen.getSet(dest, attName, " new " + getArrayType(DestAttType, srcArray + ".length", attType.getArrayDepth(), depth)) + ";");
        else
          pw.println(tab + "\t" + destArray +" = new " + getArrayType(DestAttType, srcArray + ".length", attType.getArrayDepth(), depth) + ";");
        pw.println(tab + "\tfor(int " + variableName + "=0; " + variableName + "<" + srcArray + ".length; " + variableName + "++) {");

        //If this is the last iteration, we need to pass in the calculated arrays for Src + Dest, beccause
        //This will be the last recursion using the array handling bit.
        if (depth == attType.getArrayDepth()-1) {
          String  nextSrcArray = src + "." + getArrayString(attName, depth, srcGen.isGetSet());
          String  nextDestArray = dest + "." + getArrayString(attName, depth, destGen.isGetSet());

          writeSnippet(pw, nextSrcArray, nextDestArray, attName, attType, depth+1, srcGen, destGen);
        } else {
          writeSnippet(pw, src, dest, attName, attType, depth+1, srcGen, destGen);
        }
        pw.println(tab + "\t}");
        pw.println(tab + "}");
      } 
      
    } else {
      if (attType.isBaseType()) {

        if (attName != null && depth==0)
          pw.println(tab + destGen.getSet(dest, attName, srcGen.getGet(src, attName)) + ";");
        else
          pw.println(tab + dest + "=" + src + ";");

      } else if (attType.isCollectionType()) {
        CollectionInfo  info = collectionInfo.get(attType.getType());

        //If depth is 0, we are dealing with a property off this bean, if it is greater, we are dealing
        //with a reference into an array that came from a property off this bean.
        if (depth == 0) {
          pw.println(tab + "if (" +  srcGen.getGet(src, attName) +  " != null) {");
          pw.println(tab + "\t" + destGen.getSet(dest, attName, info.allocString) + ";");
        } else {
          pw.println(tab + "if (" + src + " != null) {");
          pw.println(tab + "\t" + dest + "=" + info.allocString + ";");
        }


        String  iteratorName = "iter_" + variableName;
        pw.print(tab + "\t"  + srcGen.getUsableClassName("java.util.Iterator") + " " + iteratorName + "=");

        String  localSrc = (depth == 0 ? srcGen.getGet(src, attName) : src);

        switch(info.addType) {
          case collection:
          case stack:
            pw.println(localSrc + ".iterator();");
            break;

          case map:
            pw.println(localSrc + ".keySet().iterator();");
            break;

          case iterator:
            break;
        }

        pw.println(tab + "\twhile (" + iteratorName + ".hasNext()) {");
        pw.println("");

        String    varSrc1 = "varSrc_" + variableName;
        String    varDest1 = "varDest_" + variableName;
        String    varSrc2 = "varSrc2_" + variableName;
        String    varDest2 = "varDest2_" + variableName;
        switch(info.addType) {
          case collection:
          case stack:
          {
            Bean param1 = attType.getParameterTypes().get(0);
            String param1DestType = destGen.resolveType(param1);
            String param1SrcType = srcGen.resolveType(param1);

            pw.println(tab + "\t" + param1DestType + " " + varDest1 + ";");
            pw.println(tab + "\t" + param1SrcType + " " + varSrc1 + "=(" + param1SrcType+ ")" + iteratorName +".next();");

            writeSnippet(pw, varSrc1, varDest1, attName, param1, depth+1, srcGen, destGen);

            //This is a gross cast, but we need to cast generics away, since if someone uses ? extends blahhh
            //we can't really deteremine a reasonable intermediate variable to use, and not using an intermediate variable
            //is a bit trickier.  I the end, that is probably the right fix, since there can be generics that are not just
            //collections and we could imagine running into a problem with those.
            String grossCast = "((" + srcGen.getUsableClassName("java.util.Collection") +")";

            if (depth == 0) {
              pw.println(tab + "\t" + grossCast + destGen.getGet(dest, attName) + ").add(" + varDest1 + ");");
            } else {
              pw.println(tab + "\t" + grossCast + dest + ").add(" + varDest1 + ");");
            }
            break;
          }

          case map:
          {
            Bean param1 = attType.getParameterTypes().get(0);
            String param1DestType1 = destGen.resolveType(param1);
            String param1SrcType1 = srcGen.resolveType(param1);

            Bean param2 = attType.getParameterTypes().get(1);
            String param1DestType2 = destGen.resolveType(param2);
            String param1SrcType2 = srcGen.resolveType(param2);

            
            pw.println(tab + "\t" + param1DestType1 + " " + varDest1 + ";");
            pw.println(tab + "\t" + param1SrcType1 + " " + varSrc1 + "=(" + param1SrcType1+ ")" + iteratorName +".next();");


            pw.println(tab + "\t" + param1DestType2 + " " + varDest2 + ";");
            pw.println(tab + "\t" + param1SrcType2 + " " + varSrc2 + "=(" + param1SrcType2+ ")" + localSrc +".get( " + varSrc1 + ");");

            writeSnippet(pw, varSrc1, varDest1, attName, param1, depth+1, srcGen, destGen);
            writeSnippet(pw, varSrc2, varDest2, attName, param2, depth+1, srcGen, destGen);

            //This is a gross cast, but we need to cast generics away, since if someone uses ? extends blahhh
            //we can't really deteremine a reasonable intermediate variable to use, and not using an intermediate variable
            //is a bit trickier.  I the end, that is probably the right fix, since there can be generics that are not just
            //collections and we could imagine running into a problem with those.
            String grossCast = "((" + srcGen.getUsableClassName("java.util.Map") +")";

            if (depth == 0) {
              pw.println(tab + "\t" + grossCast + destGen.getGet(dest, attName) + ").put("+ varDest1 + ", " + varDest2 + ");");
            } else {
              pw.println(tab + "\t" + grossCast + dest + ").put("+ varDest1 + ", " + varDest2 + ");");
            }
            break;
          }
          case iterator:
            break;
        }

        pw.println(tab + "\t}");
        pw.println(tab + "}");
      } else {
        //This should be a custom type
        //Create a new one, and then call the translator.
        String    varDest1 = "varDest_" + variableName;

        //attName will be null, if we are writing this snippet for translating a parent type.
        if (attName == null) {
          varDest1 = dest;
          pw.println(tab + "\t\ttranslate(" + src + ", " + varDest1 + ");");
        } else {

          if (depth == 0) {
            pw.println(tab + "\tif (" + srcGen.getGet(src, attName) + " != null) {");
            pw.println(tab + "\t\t" + DestAttType + " " + varDest1  + "= new " + DestAttType +"();");
            pw.print(tab + "\t\t" + destGen.getSet(dest, attName, varDest1) + ";");
            pw.println(tab + "\t\ttranslate(" + srcGen.getGet(src, attName) + ", " + varDest1 + ");");
            pw.println(tab + "\t} else {");
            pw.print(tab + "\t\t" + destGen.getSet(dest, attName, "null") + ";");
          } else {
            pw.println(tab + "\tif (" + src + " != null) {");
            pw.println(tab + "\t\t" + DestAttType + " " + varDest1  + "= new " + DestAttType +"();");
            pw.println(tab + "\t\t" + dest + "= " + varDest1 + ";");
            pw.println(tab + "\t\ttranslate(" + src + ", " + varDest1 + ");");
            pw.println(tab + "\t} else {");
            pw.println(tab + "\t\t" + dest + "= " + null + ";");
          }

          pw.println(tab + "\t}");

        }
      }
    }

  }

  
  public String getTranlatorName(Bean attribute, BeanGeneratorContext beanContext) throws IOException {
    if (!translatorMap.containsKey(attribute.getType())) {

      //This case should never get here, because resolveTranslatedType should have already done this.
      assert(false);
      Generate(new BeanGeneratorContext(beanContext.getGlobalContext(), attribute, this, getImportMap()));      
    }
    return translatorMap.get(attribute.getType());
  }

  public void writeOneArgTranslate(PrintWriter pw, String srcType, String destType) {
    pw.println("\tpublic static " + destType +  " translate(" + srcType + " src) throws Exception {");
    pw.println("\t\t" + destType +  " var = new " + destType + "();");
    pw.println("\t\ttranslate(src, var);");
    pw.println("\t\treturn var;");
    pw.println("\t}");
  }

  public void writeTwoArgTranslate(PrintWriter pw, Bean bean, String srcType, String destType, ParamGen srcGen, ParamGen destGen) throws IOException {
    pw.println("\tpublic static void translate(" + srcType + " src, " + destType +" dest) throws Exception {");
    if (bean.parent != null) {
      pw.println("\t\t{ //Translate Parent Class");

      writeSnippet(pw, "(" + srcGen.resolveType(bean.parent) + ")src", "(" + destGen.resolveType(bean.parent) + ")dest", null, bean.parent, 0, srcGen,  destGen);
      pw.println("\t\t}\n");
    }

    Iterator<String> propertyNames = bean.properties.keySet().iterator();
    while (propertyNames.hasNext()) {
      String  propertyName = propertyNames.next();
      Bean  propertyType = bean.properties.get(propertyName);


      pw.println("\t\t{ //Translate " + propertyName);
      writeSnippet(pw, "src", "dest", propertyName, propertyType, 0, srcGen,  destGen);
      pw.println("\t\t}\n");
    }
    pw.println("\t}\n");

  }


  public void writeMethods(PrintWriter pw, BeanGeneratorContext beanContext) throws IOException {

    Bean  bean = beanContext.getBean();
    if (translatorMap.containsKey(bean.getType()))
      return;

    translatorMap.put(bean.getType(), "translate");

    String  OriginalTypeName = beanContext.resolveOriginalType(bean);
    String  GeneratedTypeName = beanContext.resolveTranslatedType(bean);


    writeOneArgTranslate(pw, OriginalTypeName, GeneratedTypeName);

    if (twoWay) {
      writeOneArgTranslate(pw, GeneratedTypeName, OriginalTypeName);                
    }

    ParamGen  origGen = new OriginalParamGen(beanContext);
    ParamGen  translatedGen = new TranslatedParamGen(beanContext);
    
    writeTwoArgTranslate(pw, bean, OriginalTypeName, GeneratedTypeName, origGen, translatedGen);

    if (twoWay) {
      writeTwoArgTranslate(pw, bean, GeneratedTypeName, OriginalTypeName, translatedGen, origGen);
    }

  }


  public void Init(GlobalGeneratorContext globalContext) throws Exception {
    fileName = globalContext.CreateFile(packageDir.toString(), className);
  }

  public void Generate(BeanGeneratorContext beanContext) throws IOException{
    System.out.println(beanContext.getBean().getArrayDepth() + " " + beanContext.getBean().clazz);
    if (beanContext.markAsGenerated())
      return;

    //UNDONE(willpugh) -- There is a conflict case that is not taken care of here, and will be fixed when BeanGeneration
    //uses the same contexts.
    beanContext.getGlobalContext().putTranslatedName(beanContext.getBean().clazz.getName(), beanPackageName + "." + beanContext.getBean().clazz.getSimpleName());

    StringWriter body = new StringWriter();

    writeMethods(new PrintWriter(body), beanContext);
    methods.add(body.getBuffer().toString());
  }

  public void Finish(GlobalGeneratorContext globalContext) throws Exception {

    PrintWriter pw = new PrintWriter(fileName);

    writeHeader(pw, null);

    for (String str : methods) {
      pw.println(str);
    }

    pw.println("}");
    pw.flush();
    pw.close();
  }

  public HashMap<String, String>  getImportMap() {
    return importMap;
  }

}
