package com.totsp.mavenplugin.gwt.util;

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
}