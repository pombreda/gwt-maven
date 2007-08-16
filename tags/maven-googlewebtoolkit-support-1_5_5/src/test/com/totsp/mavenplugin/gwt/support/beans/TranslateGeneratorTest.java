package com.totsp.mavenplugin.gwt.support.beans;

import com.totsp.mavenplugin.gwt.support.GenerationBaseCase;

import java.io.File;
import java.lang.reflect.Method;

/**
 * Author: willpugh  Apr 16, 2007 - 3:19:44 PM
 */
public class TranslateGeneratorTest extends GenerationBaseCase {


  public TranslateGeneratorTest() {
    super(new File("./target/test-bin"));
  }

  protected String getPackageName(boolean getSet, boolean propListener) {
    return new String("test.translationTest1." + (getSet ? "get." : "noget.") + (propListener ? "prop" : "noprop"));
  }

  public void testBasicCase() throws Exception {

    boolean getSet = true;
    boolean propListener = true;

    String  PackageName = getPackageName(getSet, propListener);

    String beanPkg = PackageName + ".beans";
    String translatorPkg = PackageName + ".translator";

    File  beansPath = getPackagePath(beanPkg);
    File  translatorPath = getPackagePath(translatorPkg);


    GlobalGeneratorContext  global = new GlobalGeneratorContext(true, true, true);
    Class classes[] = new Class[] { Class.forName("test.server.ABean"), Class.forName("test.server.TestInheritance") };
    Generator translationGenerator = new TranslatorGenerator(translatorPkg, translatorPath, beanPkg, true);
    translationGenerator.Init(global);


    for (Class curr : classes) {
      BeanGeneratorContext  beanContext = new BeanGeneratorContext(global, Bean.loadBean(curr), translationGenerator, translationGenerator.getImportMap());

      //Currently, haven't moved the regular bean generation over to the new model.
      BeanGeneratorBase.writeBean( PackageName + ".beans", beansPath, getSet, propListener, true, Bean.loadBean(curr));
      translationGenerator.Generate(beanContext);

    }
    translationGenerator.Finish(global);

    doCompile(new File[] {beansPath, translatorPath});

    ClassLoader cl = getClassLoader();
    for (Class curr : classes) {
      Object  currObj = curr.newInstance();

      //Translate to generated bean
      Method translate = cl.loadClass(translatorPkg + ".Translater").getMethod("translate", curr);
      Object o = translate.invoke(null, currObj);

      //Translate back
      translate = cl.loadClass(translatorPkg + ".Translater").getMethod("translate", o.getClass());
      o = translate.invoke(null, o);

      assertEquals(o, currObj);
    }


  }
}
