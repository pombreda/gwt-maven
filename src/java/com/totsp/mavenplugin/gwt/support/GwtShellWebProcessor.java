/*
 * GwtShellWebProcessor.java
 *
 * Created on November 12, 2006, 9:36 PM
 */

package com.totsp.mavenplugin.gwt.support;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.jdom.JDOMException;

/**
 *
 * @author cooper
 */
public class GwtShellWebProcessor extends GwtWebInfProcessor {
    
    /** Creates a new instance of GwtShellWebProcessor */
    public GwtShellWebProcessor(String targetWebXml, String sourceWebXml) throws Exception {
        // obtain web.xml
        this.webXmlPath = sourceWebXml;
        File webXmlFile = new File(sourceWebXml);
        
        if(!webXmlFile.exists() || !webXmlFile.canRead()) {
            throw new Exception("Unable to locate source web.xml");
        }
        
        this.destination = new File(targetWebXml);
        this.servletDescriptors = new ArrayList();
        ServletDescriptor d = new ServletDescriptor(
                "/*",
                "com.google.gwt.dev.shell.GWTShellServlet");
        d.setName("shell");
        this.servletDescriptors.add( d );
    }

    
    
    
}
