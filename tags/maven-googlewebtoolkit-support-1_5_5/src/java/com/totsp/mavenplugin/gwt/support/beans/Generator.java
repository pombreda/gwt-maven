package com.totsp.mavenplugin.gwt.support.beans;

import java.io.IOException;
import java.util.HashMap;

/**
 * Author: willpugh  Apr 15, 2007 - 4:09:17 PM
 */
public interface Generator {
  public void Init(GlobalGeneratorContext globalContext) throws Exception;
  public void Generate(BeanGeneratorContext context) throws IOException;
  public void Finish(GlobalGeneratorContext globalContext) throws Exception;

  public HashMap<String, String>  getImportMap();
}
