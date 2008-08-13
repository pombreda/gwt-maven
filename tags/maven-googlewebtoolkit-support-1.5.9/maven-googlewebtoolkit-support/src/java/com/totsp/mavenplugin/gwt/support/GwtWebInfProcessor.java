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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.jdom.Comment;
import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;


public class GwtWebInfProcessor {
    
    private Document webXml;
    protected File destination;
    protected List servletDescriptors;
    private String moduleName;
    private File moduleFile;
    protected String webXmlPath;
    private HashSet checkedModules = new HashSet();
    
    protected GwtWebInfProcessor(){
        super();
    }
    
    public GwtWebInfProcessor(
            String moduleName, String targetWebXml, String sourceWebXml
            ) throws Exception {
        
        this.moduleName = moduleName;
        this.webXmlPath = sourceWebXml;        
        File webXmlFile = new File(sourceWebXml);
        
        if(!webXmlFile.exists() || !webXmlFile.canRead()) {
            throw new Exception("Unable to locate source web.xml");
        }
        
        this.destination = new File(targetWebXml);        
        if(
                GwtWebInfProcessor.class.getResource("/"+moduleName.replace('.', '/') + ".gwt.xml") == null
                
                ) {
            
            throw new Exception("Unable to locate module definition file: "+moduleName.replace('.', '/') + ".gwt.xml");
        }
        
        this.servletDescriptors = this.getGwtServletDescriptors(moduleName);
        
        if(this.servletDescriptors.size() == 0) {
            throw new ExitException("No servlets found.");
        }
    }    
    
    public GwtWebInfProcessor(String moduleName,
            File moduleDefinition, String targetWebXml, String sourceWebXml
            ) throws Exception {
        
        this.moduleName = moduleName;
        this.webXmlPath = sourceWebXml;
        this.moduleFile = moduleDefinition;
        File webXmlFile = new File(sourceWebXml);
        
        if(!webXmlFile.exists() || !webXmlFile.canRead()) {
            throw new Exception("Unable to locate source web.xml");
        }
        
        this.destination = new File(targetWebXml);
        
        this.servletDescriptors = this.getGwtServletDescriptors(null);
        
        if(this.servletDescriptors.size() == 0) {
            throw new ExitException("No servlets found.");
        }
    }    
   
    
    /**
     * Return List of ServletDescriptor from gwt module file.
     *
     * @param gwtModFile
     * @return
     */
    protected List getGwtServletDescriptors(String module) throws IOException, JDOMException {
        
        System.out.println("GwtWebInfProcessor getGwtServletDescriptors (module - " + module + ")");
        
        ArrayList servletElements = new ArrayList();
        checkedModules.add( module );
        Document document = null;
        SAXBuilder builder = null;
        
        try{
            builder = new SAXBuilder(false);
            builder.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            if(module == null && this.moduleFile != null){
                document = builder.build(this.moduleFile);
            } else {
                
                ///System.out.println("GwtWebInfProcessor - classloader from CLASS = " + GwtWebInfProcessor.class.getClassLoader().toString());
                ///System.out.println("GwtWebInfProcessor - classloader from THREAD = " + Thread.currentThread().getContextClassLoader().toString());
                
                document = builder.build(
                        GwtWebInfProcessor.class.getResourceAsStream(
                        "/"+module.replace('.', '/') + ".gwt.xml"
                        )
                        );
            }
        } catch(Exception e) {
            ///if( !module.startsWith("com.google.gwt.dev.") && !module.startsWith("com.google.gwt.user."))
                
                // try one more time, parse module using THREAD classpath 
                // (AbstractMojo fixThreadClasspath path differs from ClassLoader on CLASS)
                try {
                    System.out.println("   Unable to parse module using ClassLoader on CLASS, trying THREAD (which is specifically \"fixed\" by AbstractGwtMojo).");
                    document = builder.build(
                        Thread.currentThread().getContextClassLoader().getResourceAsStream(
                        "/"+module.replace('.', '/') + ".gwt.xml"
                        )
                        );
                    System.out.println("   Parsing module using thread fixed classpath succeeded.");
                } catch (Exception ee) {
                    System.err.println("   Unable to parse module");
                    ee.printStackTrace();
                    return servletElements;
                }                            
        } 
        
        Element element = document.getRootElement();
        List inherits = element.getChildren("inherits");
        
        for(int i = 0; (inherits != null) && (i < inherits.size()); i++) {
            Element inherit = (Element) inherits.get(i);
            if( !checkedModules.contains(inherit.getAttributeValue("name")) )
                servletElements.addAll(
                        this.getGwtServletDescriptors(
                        inherit.getAttributeValue("name")));
        }
        
        List servlets = element.getChildren("servlet");
        if (servlets != null && servlets.size() > 0) {
            ///System.out.println("   servlets found in module - " + servlets.size());        
            for(int i = 0; i < servlets.size(); i++) {                         
                Element servlet = (Element) servlets.get(i);            
                ///System.out.println("   processing servlet element - " + servlet.getAttributeValue("class"));            
                String servletPath = "/"+this.moduleName+servlet.getAttributeValue("path");
                String servletClass = servlet.getAttributeValue("class");
                ServletDescriptor servletDesc = new ServletDescriptor(
                    servletPath, servletClass
                    );
                servletElements.add(servletDesc);
            }
        } 
        
        return servletElements;
    }
    
    private int getInsertPosition(String[] startAfter, String[] stopBefore
            ) throws JDOMException, IOException {
        Element webapp = this.getWebXml().getRootElement();
        List children = webapp.getContent();
        Content insertAfter = new Comment(
                "inserted by gwt-maven"
                );
        
        ArrayList namesBefore = new ArrayList();
        ArrayList namesAfter = new ArrayList();
        
        for(int i = 0; i < startAfter.length; i++) {
            namesBefore.add(startAfter[i]);
        }
        
        for(int i = 0; i < stopBefore.length; i++) {
            namesAfter.add(stopBefore[i]);
        }
        
        if((children == null) || (children.size() == 0)) {
            webapp.addContent(insertAfter);
        } else {
            boolean foundPoint = false;;
            for(int i = 0; !foundPoint && i < children.size(); i++) {
                Object o = children.get(i);
                if(!(o instanceof Element)) {
                    continue;
                }
                
                Element child = (Element) o;
                
                if(namesAfter.contains(child.getName())) {
                    webapp.addContent(i, insertAfter);
                    foundPoint = true;
                    break;
                }
                
                if(!namesBefore.contains(child.getName())) {
                    webapp.addContent(i + 1, insertAfter);
                    foundPoint = true;
                    break;                    
                }                
            }
            if( !foundPoint ){
                webapp.addContent( insertAfter );
            }
        }
        
        return webapp.indexOf(insertAfter);
    }
    
    private Document getWebXml() throws JDOMException, IOException {        
        if (this.webXml == null) {
            SAXBuilder builder = new SAXBuilder(false);
            builder.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            return this.webXml = builder.build(this.webXmlPath); 
        }
        else {
            return this.webXml;
        }        
    }
    
    private void insertServlets() throws JDOMException, IOException {
        /*
         <!ELEMENT web-app (icon?, display-name?, description?, distributable?,
            context-param*, filter*, filter-mapping*, listener*, servlet*,
            servlet-mapping*, session-config?, mime-mapping*, welcome-file-list?,
            error-page*, taglib*, resource-env-ref*, resource-ref*, security-constraint*,
            login-config?, security-role*, env-entry*, ejb-ref*,  ejb-local-ref*)>
         */
        Element webapp = this.getWebXml().getRootElement();
        String[] beforeServlets = {
            "icon", "display-name", "description", "distributable",
            "context-param", "filter", "filter-mapping", "listener", "servlet"
        };
        String[] afterServlets = {
            "servlet-mapping", "session-config", "mime-mapping",
            "welcome-file-list", "error-page", "taglib", "resource-env-ref",
            "resource-ref", "security-constraint", "login-config",
            "security-role", "env-entry", "ejb-ref", "ejb-local-ref"
        };
        
        String[] beforeMappings = {
            "icon", "display-name", "description", "distributable",
            "context-param", "filter", "filter-mapping", "listener",
            "servlet", "servlet-mapping"
        };
        String[] afterMappings = {
             "session-config", "mime-mapping", "welcome-file-list",
            "error-page", "taglib", "resource-env-ref", "resource-ref",
            "security-constraint", "login-config", "security-role",
            "env-entry", "ejb-ref", "ejb-local-ref"
        };
        
        int insertAfter = this.getInsertPosition(beforeServlets, afterServlets
                );
        
        for(int i = 0; i < this.servletDescriptors.size(); i++) {
            insertAfter++;
            
            ServletDescriptor d = (ServletDescriptor) this.servletDescriptors.get(i);
            Element servlet = new Element("servlet" , webapp.getNamespace());
            Element servletName = new Element("servlet-name", webapp.getNamespace());
            servletName.setText(d.getName() == null ? d.getClassName() + d.getPath() : d.getName());
            servlet.addContent(servletName);
            
            Element servletClass = new Element("servlet-class", webapp.getNamespace());
            servletClass.setText(d.getClassName());
            servlet.addContent(servletClass);
            webapp.addContent(insertAfter, servlet);
        }
        
        insertAfter = this.getInsertPosition(
                beforeMappings, afterMappings
                );
        
        for(int i = 0; i < this.servletDescriptors.size(); i++) {
            insertAfter++;
            
            ServletDescriptor d = (ServletDescriptor) this.servletDescriptors.get(i);
            Element servletMapping = new Element("servlet-mapping", webapp.getNamespace());
            Element servletName = new Element("servlet-name", webapp.getNamespace());
            servletName.setText(d.getName() == null ? d.getClassName() + d.getPath() : d.getName());
            servletMapping.addContent(servletName);
            
            Element urlPattern = new Element("url-pattern", webapp.getNamespace());
            
            urlPattern.setText( d.getPath() );
            servletMapping.addContent(urlPattern);
            webapp.addContent(insertAfter, servletMapping);
        }
    }
    
    
    
    public void process() throws Exception {
        this.insertServlets();
        
        XMLOutputter out = new XMLOutputter( Format.getPrettyFormat() );
        FileWriter writer = new FileWriter(this.destination);
        out.output(this.webXml, new FileWriter(this.destination));
        writer.flush();
        writer.close();
        
    }
}
