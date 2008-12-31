/*
 * MakeCatalinaBase.java
 * 
 * Created on November 12, 2006, 10:19 PM
 * 
 * To change this template, choose Tools | Template Manager and open the
 * template in the editor.
 */
package com.totsp.mavenplugin.gwt.support;

import java.io.File;
import java.io.FileOutputStream;

import com.totsp.mavenplugin.gwt.support.util.FileIOUtils;



/**
 * Create webapp directory structure and copy web.xml.
 * <pre>
 *  catalinaBase
 *     conf
 *        gwt
 *           localhost
 *     webapps
 *        ROOT
 *           WEB-INF
 *     work
 * </pre>
 * @author cooper
 * @author Marek Romanowski
 */
public class MakeCatalinaBase {

  public static void main(String[] args) throws Exception {
    String baseDir = args[0];
    String sourceWebXml = args[1];
    String shellServletMappingURL = args[2];

    File catalinaBase = new File(baseDir);
    catalinaBase.mkdirs();
    File conf = new File(catalinaBase, "conf");
    conf.mkdirs();
    File gwt = new File(conf, "gwt/localhost");
    gwt.mkdirs();
    File webinf = new File(catalinaBase, "webapps/ROOT/WEB-INF");
    webinf.mkdirs();
    new File(catalinaBase, "work").mkdirs();

    // copy default container web.xml file
    FileOutputStream fos = new FileOutputStream(new File(conf, "web.xml"));
    FileIOUtils.copyStream(
        MakeCatalinaBase.class.getResourceAsStream("baseWeb.xml"), fos);
    
    // build application web.xml file
    File mergeWeb = new File(webinf, "web.xml");
    File sourceWeb = new File(sourceWebXml);
    if (sourceWeb.exists()) {
      GwtShellWebProcessor p = new GwtShellWebProcessor(
          mergeWeb.getAbsolutePath(), sourceWebXml, shellServletMappingURL);
      p.process();
    } else {
      fos = new FileOutputStream(mergeWeb);
      FileIOUtils.copyStream(
          MakeCatalinaBase.class.getResourceAsStream("emptyWeb.xml"), fos);
    }

  }
}


