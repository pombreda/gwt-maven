/*
 *
 *  Copyright (C) 2006 Charlie Collins <charlie@screaming-penguin.com>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.totsp.mavenplugin.gwt.support;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jdom.Comment;
import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;



/**
 * Creates web.xml for GWT purposes.
 * @author Charlie Collins
 * @author Marek Romanowski
 */
public class GwtWebInfProcessor {

  private Document webXml;
  protected File destination;
  protected List<ServletDescriptor> servletDescriptors;
  private String moduleName;
  private File moduleFile;
  protected String webXmlPath;
  private Set<String> checkedModules = new HashSet<String>();
  private final SAXBuilder builder;
  
  
  
  public GwtWebInfProcessor(String moduleName, String targetWebXml,
      String sourceWebXml, boolean webXmlServletPath) throws Exception {
    this(moduleName, null, targetWebXml, sourceWebXml);

    // servlet descriptors
    servletDescriptors = getGwtServletDescriptors(moduleName, webXmlServletPath);
    if (servletDescriptors.size() == 0) {
      throw new ExitException("No servlets found.");
    }
  }



    public GwtWebInfProcessor(String moduleName, File moduleDefinition,
        String targetWebXml, String sourceWebXml, boolean webXmlServletPath)
        throws Exception {
    this(moduleName, moduleDefinition, targetWebXml, sourceWebXml);

    // servlet descriptors
    servletDescriptors = getGwtServletDescriptors(null, webXmlServletPath);
    if (servletDescriptors.size() == 0) {
      throw new ExitException("No servlets found.");
    }
  }



  protected GwtWebInfProcessor(String moduleName, File moduleDefinition,
      String targetWebXml, String sourceWebXml)
      throws Exception {
    // all purpose jDOM parser
    builder = new SAXBuilder(false);
    builder.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

    this.moduleName = moduleName;
    this.webXmlPath = sourceWebXml;
    this.moduleFile = moduleDefinition;
    File webXmlFile = new File(sourceWebXml);
    // parse source web.xml to jDOM
    webXml = builder.build(webXmlFile);

    if (!webXmlFile.exists() || !webXmlFile.canRead()) {
      throw new Exception("Unable to locate source web.xml");
    }

    this.destination = new File(targetWebXml);
  }



  protected URL getResource(String path) {
    return this.getClass().getResource(path);
  }
  
  
  
  protected String getModuleDescriptorPath(String someModuleName) {
    return "/" + someModuleName.replace('.', '/') + ".gwt.xml";
  }

   
   
  /**
   * Return List of ServletDescriptor from gwt module file.
   */
  protected List<ServletDescriptor> getGwtServletDescriptors(String module,
      boolean webXmlServletPath) throws IOException, JDOMException {

    ArrayList<ServletDescriptor> servletElements = new ArrayList<ServletDescriptor>();
    checkedModules.add(module);
    Document document = null;

    if (module == null && this.moduleFile != null) {
      document = builder.build(this.moduleFile);
    } else {
      document = builder.build(Thread.currentThread().getContextClassLoader()
          .getResourceAsStream(getModuleDescriptorPath(module)));
    }

    Element element = document.getRootElement();
    List<Element> inherits = genericList(element.getChildren("inherits"));

    for (Element inherit : inherits) {
      if (!checkedModules.contains(inherit.getAttributeValue("name"))) {
        servletElements.addAll(getGwtServletDescriptors(
            inherit.getAttributeValue("name"), webXmlServletPath));
      }
    }

    List<Element> servlets = genericList(element.getChildren("servlet"));
    if (servlets != null && servlets.size() > 0) {
      for (int i = 0; i < servlets.size(); i++) {
        Element servlet = (Element) servlets.get(i);
        String servletPath = null;
        if (webXmlServletPath) {
          servletPath = servlet.getAttributeValue("path");
        } else {
          servletPath = "/" + this.moduleName
              + servlet.getAttributeValue("path");
        }
        String servletClass = servlet.getAttributeValue("class");
        ServletDescriptor servletDesc = new ServletDescriptor(servletPath,
            servletClass);
        servletElements.add(servletDesc);
      }
    }

    return servletElements;
  }
   
   

   private int getInsertPosition(List<String> startAfter, List<String> stopBefore
       ) throws JDOMException, IOException {
     
      Element webapp = webXml.getRootElement();
      List<Content> children = genericList(webapp.getContent());
      Comment insertAfter = new Comment("inserted by gwt-maven");

      if ((children == null) || (children.size() == 0)) {
         webapp.addContent(insertAfter);
      } else {
         boolean foundPoint = false;
         
         for (int i = 0; !foundPoint && i < children.size(); i++) {
           Content o = children.get(i);
            if (!(o instanceof Element)) {
               continue;
            }

            Element child = (Element) o;

            // if current element is in startAfter, then inserts should 
            // be before it
            if (stopBefore.contains(child.getName())) {
               webapp.addContent(i, insertAfter);
               foundPoint = true;
               break;
            }

            // if current element is not in stopBefore, then inserts should
            // be after it
            if (!startAfter.contains(child.getName())) {
               webapp.addContent(i + 1, insertAfter);
               foundPoint = true;
               break;
            }
         }
         if (!foundPoint) {
            webapp.addContent(insertAfter);
         }
      }

      return webapp.indexOf(insertAfter);
   }
   
   
   
  /**
   * Used not to make long method annotated with SuppressWarnings.
   */
  @SuppressWarnings("unchecked")
  private <E> List<E> genericList(List list) {
    return list;
  }
   
   

   private void insertServlets() throws JDOMException, IOException {
      /*
       <!ELEMENT web-app (icon?, display-name?, description?, distributable?,
          context-param*, filter*, filter-mapping*, listener*, servlet*,
          servlet-mapping*, session-config?, mime-mapping*, welcome-file-list?,
          error-page*, taglib*, resource-env-ref*, resource-ref*, security-constraint*,
          login-config?, security-role*, env-entry*, ejb-ref*,  ejb-local-ref*)>
       */
      Element webapp = webXml.getRootElement();
      List<String> beforeServlets = Collections.unmodifiableList(Arrays.asList(
          new String[] { "icon", "display-name", "description", "distributable", "context-param", "filter",
               "filter-mapping", "listener", "servlet" }));
      List<String> afterServlets = Collections.unmodifiableList(Arrays.asList(
          new String[] { "servlet-mapping", "session-config", "mime-mapping", "welcome-file-list",
               "error-page", "taglib", "resource-env-ref", "resource-ref", "security-constraint", "login-config",
               "security-role", "env-entry", "ejb-ref", "ejb-local-ref" }));

      List<String> beforeMappings = Collections.unmodifiableList(Arrays.asList(
          new String[] { "icon", "display-name", "description", "distributable", "context-param", "filter",
               "filter-mapping", "listener", "servlet", "servlet-mapping" }));
      List<String> afterMappings = Collections.unmodifiableList(Arrays.asList(
          new String[] { "session-config", "mime-mapping", "welcome-file-list", "error-page", "taglib",
               "resource-env-ref", "resource-ref", "security-constraint", "login-config", "security-role", "env-entry",
               "ejb-ref", "ejb-local-ref" }));

      // search last position before servlets
      int insertAfter = getInsertPosition(beforeServlets, afterServlets);
      // insert all servlet descriptors into document
      for (ServletDescriptor d : servletDescriptors) {
         insertAfter++;
         
         // servlet element
         Element servlet = new Element("servlet", webapp.getNamespace());
         // servlet-name subelement
         Element servletName = new Element("servlet-name", webapp.getNamespace());
         // servlet-name value
         servletName.setText(d.getName() == null ? d.getClassName() + d.getPath() : d.getName());
         // add servlet-name element to servlet element
         servlet.addContent(servletName);

         // servlet-class subelement
         Element servletClass = new Element("servlet-class", webapp.getNamespace());
         // servlet-class value
         servletClass.setText(d.getClassName());
         
         // add servlet-class element to servlet element
         servlet.addContent(servletClass);
         // add servlet element to webapp element
         webapp.addContent(insertAfter, servlet);
      }

      // search last position before servlet mappings
      insertAfter = getInsertPosition(beforeMappings, afterMappings);
      // insert all servlet mappings into document
      for (ServletDescriptor d : servletDescriptors) {
         insertAfter++;

         // servlet-mapping element
         Element servletMapping = new Element("servlet-mapping", webapp.getNamespace());
         // servlet-name subelement
         Element servletName = new Element("servlet-name", webapp.getNamespace());
         // servlet-name value
         servletName.setText(d.getName() == null ? d.getClassName() + d.getPath() : d.getName());
         // add servlet-name element to servlet-mapping element
         servletMapping.addContent(servletName);

         // url-pattern element
         Element urlPattern = new Element("url-pattern", webapp.getNamespace());
         // url-pattern value
         urlPattern.setText(d.getPath());
         
         // add url-pattern element to servlet-mapping element
         servletMapping.addContent(urlPattern);
         // add servlet-mapping element to webapp element
         webapp.addContent(insertAfter, servletMapping);
      }
   }
   
   
   
   public void process() throws Exception {
      insertServlets();

      XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
      FileWriter writer = new FileWriter(destination);
      out.output(webXml, new FileWriter(destination));
      writer.flush();
      writer.close();
   }
}


