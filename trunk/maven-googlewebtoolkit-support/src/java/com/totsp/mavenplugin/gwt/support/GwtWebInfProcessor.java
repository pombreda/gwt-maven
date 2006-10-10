package com.totsp.mavenplugin.gwt.support;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
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
    private File destination;
    private List servletDescriptors;
    private String moduleName;
    private String webXmlPath;
    
    public GwtWebInfProcessor(
            String moduleName, String targetWebXml, String sourceWebXml
            ) throws Exception {
        this.moduleName = moduleName;
        // obtain web.xml
        this.webXmlPath = sourceWebXml;
        
        File webXmlFile = new File(sourceWebXml);
        
        if(!webXmlFile.exists() || !webXmlFile.canRead()) {
            throw new Exception("Unable to locate source web.xml");
        }
        
        this.destination = new File(targetWebXml);
        
        if(
                GwtWebInfProcessor.class.getResource("/"+moduleName.replace('.', '/') + ".gwt.xml") == null
                
                ) {
            
            throw new Exception("Unable to locate module definition file.");
        }
        
        this.servletDescriptors = this.getGwtServletDescriptors(moduleName);
        
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
    private List getGwtServletDescriptors(String module) throws IOException, JDOMException {
        ArrayList servletElements = new ArrayList();
        
        
        Document document = null;
        try{
            document = new SAXBuilder().build(
        
                GwtWebInfProcessor.class.getResourceAsStream(
                "/"+module.replace('.', '/') + ".gwt.xml"
                )
                );
        } catch(Exception e){
            System.err.println("Unable to parse module: "+ moduleName );
            return servletElements;
        }
        Element element = document.getRootElement();
        List inherits = element.getChildren("inherits");
        
        for(int i = 0; (inherits != null) && (i < inherits.size()); i++) {
            Element inherit = (Element) inherits.get(i);
            servletElements.addAll(
                    this.getGwtServletDescriptors(
                    inherit.getAttributeValue("name")
                    )
                    );
        }
        
        List servlets = element.getChildren("servlet");
        
        for(int i = 0; i < servlets.size(); i++) {
            Element servlet = (Element) servlets.get(i);
            String servletPath = servlet.getAttributeValue("path");
            String servletClass = servlet.getAttributeValue("class");
            ServletDescriptor servletDesc = new ServletDescriptor(
                    servletPath, servletClass
                    );
            servletElements.add(servletDesc);
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
                }
                
                if(!namesBefore.contains(child.getName())) {
                    webapp.addContent(i + 1, insertAfter);
                    foundPoint = true;
                   
                }
               
            }
            if( !foundPoint ){
                webapp.addContent( insertAfter );
            }
        }
        
        return webapp.indexOf(insertAfter);
    }
    
    private Document getWebXml() throws JDOMException, IOException {
        return this.webXml = (this.webXml == null) ? new SAXBuilder().build(this.webXmlPath)
        : this.webXml;
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
            "context-param", "filter", "filter-mapping", "listener"
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
            "servlet"
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
            Element servlet = new Element("servlet");
            Element servletName = new Element("servlet-name");
            servletName.setText(d.getClassName() + d.getPath());
            servlet.addContent(servletName);
            
            Element servletClass = new Element("servlet-class");
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
            Element servletMapping = new Element("servlet-mapping");
            Element servletName = new Element("servlet-name");
            servletName.setText(d.getClassName() + d.getPath());
            servletMapping.addContent(servletName);
            
            Element urlPattern = new Element("url-pattern");
            urlPattern.setText("/" + this.moduleName + d.getPath());
            servletMapping.addContent(urlPattern);
            webapp.addContent(insertAfter, servletMapping);
        }
    }
    
    
    
    public void process() throws Exception {
        this.insertServlets();
        
        XMLOutputter out = new XMLOutputter( Format.getPrettyFormat() );
        out.output(this.webXml, new FileWriter(this.destination));
    }
}
