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

import java.util.ArrayList;



/**
 * Special processor which adds to web.xml only one servlet descriptor -
 * with name <code>shell</code> and class 
 * <code>com.google.gwt.dev.shell.GWTShellServlet</code>.
 * @author cooper
 * @author Marek Romanowski
 */
public class GwtShellWebProcessor extends GwtWebInfProcessor {
    
  
  
    /** 
     * Creates a new instance of GwtShellWebProcessor. 
     */
    public GwtShellWebProcessor(String targetWebXml, String sourceWebXml, 
        String shellServletMappingURL) throws Exception {
      super(null, null, targetWebXml, sourceWebXml);

      servletDescriptors = new ArrayList<ServletDescriptor>();
        ServletDescriptor servletDescriptor = new ServletDescriptor(
            shellServletMappingURL, "com.google.gwt.dev.shell.GWTShellServlet");
        servletDescriptor.setName("shell");
        this.servletDescriptors.add(servletDescriptor);
    }
}


