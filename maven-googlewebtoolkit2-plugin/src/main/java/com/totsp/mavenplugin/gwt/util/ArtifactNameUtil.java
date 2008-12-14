/*
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
 * 
 */
package com.totsp.mavenplugin.gwt.util;

import java.io.File;

import com.totsp.mavenplugin.gwt.AbstractGWTMojo;

public class ArtifactNameUtil {

    /**
     * Util for artifact and platform names stuff.
     * 
     * @author ccollins
     * 
     */
    private ArtifactNameUtil() {
    }

    /**
     * Convenience return platform name.
     * 
     * @return
     */
    public static final String getPlatformName() {
        String result = AbstractGWTMojo.WINDOWS;
        if (AbstractGWTMojo.OS_NAME.startsWith(AbstractGWTMojo.MAC)) {
            result = AbstractGWTMojo.MAC;
        } else if (AbstractGWTMojo.OS_NAME.startsWith(AbstractGWTMojo.LINUX)) {
            result = AbstractGWTMojo.LINUX;
        }
        return result;
    }

    /**
     * Guess dev jar name based on platform.
     * 
     * @return
     */
    public static final String guessDevJarName() {
        if (AbstractGWTMojo.OS_NAME.startsWith(AbstractGWTMojo.WINDOWS)) {
            return "gwt-dev-windows.jar";
        } else if (AbstractGWTMojo.OS_NAME.startsWith(AbstractGWTMojo.MAC)) {
            return "gwt-dev-mac.jar";
        } else {
            return "gwt-dev-linux.jar";
        }
    }
    
    /**
     * Transforms artifact description containing groupId and artifactId in short form i.e. "com.totsp.gwt:maven-googlewebtoolkit2-plugin" to relative filesystem path i.e. "com/totsp/gwt/maven-googlewebtoolkit2-plugin".
     * 
     * @param artifactDescription Description of artifact in form "&lt;groupId&gt;:&lt;artifactId&gt;"
     * @return Relative path with file separator in the end.
     */
    public static final String getMavenPathPartFromArtifactDescription(String artifactDescription) {
    	return artifactDescription.replaceAll("\\.", "\\" + File.separator).replaceAll("\\:", "\\" + File.separator) + File.separator;
    }
}