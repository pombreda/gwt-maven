/*
 * MergeWebXmlMojo.java
 *
 * Created on January 13, 2007, 7:45 PM
 *
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

package com.totsp.mavenplugin.gwt;

import java.io.File;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * @goal mergewebxml
 * @phase package
 * @author cooper
 */
public class MergeWebXmlMojo extends AbstractGWTMojo{
    
    /** Creates a new instance of MergeWebXmlMojo */
    public MergeWebXmlMojo() {
        super();
    }
    
    public void execute() throws MojoExecutionException, MojoFailureException {
        String[] args = {
            "-moduleName",
            this.getCompileTarget(),
            "-webXmlPath",
            this.getWebXml() != null ?
                this.getWebXml().getAbsolutePath() :
                this.getDefaultWebXml().getAbsolutePath(),
            "-targetWebXmlPath",
            new File(this.getOutput(),
                    "WEB-INF/web.xml").getAbsolutePath()
        };
        com.totsp.mavenplugin.gwt.support.Main.main( args );
    }
}
