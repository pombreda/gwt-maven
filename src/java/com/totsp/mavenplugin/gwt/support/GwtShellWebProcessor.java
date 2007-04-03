/*
 * GwtShellWebProcessor.java
 *
 *  Copyright (C) 2006  Robert "kebernet" Cooper <cooper@screaming-penguin.com>
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
