package com.totsp.mavenplugin.gwt.scripting;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;

import com.totsp.mavenplugin.gwt.AbstractGWTMojo;

public final class ScriptUtil {
    
    private ScriptUtil() {
    }
    
    public static void runScript(final File exec) throws MojoExecutionException {
        ProcessWatcher pw = null;
        if (AbstractGWTMojo.OS_NAME.startsWith(AbstractGWTMojo.WINDOWS)) {            
            pw = new ProcessWatcher("\"" + exec.getAbsolutePath() + "\"");
        } else {
            pw = new ProcessWatcher(exec.getAbsolutePath().replaceAll(" ", "\\ "));
        }
        
        try {                
            pw.startProcess(System.out, System.err);
            int retVal = pw.waitFor();
            if (retVal != 0) {
                throw new MojoExecutionException(exec.getName() + " script exited abnormally with code - " + retVal);
            }
        } catch (Exception e) {
            throw new MojoExecutionException("Exception attempting to run script - " + exec.getName(), e);
        }
    }
       
}  