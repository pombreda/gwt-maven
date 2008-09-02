package com.totsp.mavenplugin.gwt;

import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.totsp.mavenplugin.gwt.scripting.ScriptUtil;
import com.totsp.mavenplugin.gwt.scripting.ScriptWriter;
import com.totsp.mavenplugin.gwt.scripting.ScriptWriterFactory;

/**
 * Runs special (non surefire) test phase for GWTTestCase derived tests.
 * 
 * This is a giant hack because Surefire has some issues with GWTTestCase.  
 * Surefire states that it offers multiple ways to load the classpath
 * (http://maven.apache.org/plugins/maven-surefire-plugin/examples/class-loading.html),
 * but it doesn't seem to work as advertised. Manifest class path works, and isolated
 * classpath also works, but just getting to a normal java class path doesn't seem to work
 * (surefire still refers to /tmp/surefireX). Without a normal java class path GWTTestCase
 * won't work - because the GWT JUnitShell inspects the classpath and sets itself up, it 
 * doesn't like surefire magic. Also, presuming surefire did work and just set a normal 
 * classpath, it still would be susceptible to the line too long on Windows crap. 
 * I get the same problem others do, no matter how I configure surefire (and yes, I did
 * RTFM and try the useSystemClassLoader and useManifestOnlyJar settings, various ways)
 * : http://www.mail-archive.com/users@maven.apache.org/msg87660.html - and 
 * http://jira.codehaus.org/browse/SUREFIRE-508.
 * 
 * Hopefully we can kill this someday, it sucks, but for now, this is the ONLY way we know of
 * to run GWTTestCase based tests. 
 * 
 * @goal test
 * @phase test
 * @requiresDependencyResolution test
 * @description Runs special (non surefire) test phase for GWTTestCase derived tests.
 * 
 * @author ccollins
 */
public class GWTTestMojo extends AbstractGWTMojo {

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (isSkip()) {
            return;
        }
        
        this.getLog().info("running gwt:test GWTTestCase tests (using test name filter -  " + this.getTestFilter());

        FileWriter testResultsWriter = null;

        // build scripts for each test case for the correct platform
        ScriptWriter writer = ScriptWriterFactory.getInstance();
        writer.writeTestScripts(this);

        // run the scripts
        boolean testFailure = false;
        File testDir = new File(this.getBuildDir(), "gwtTest");
        FileFilter fileFilter = new WildcardFileFilter("gwtTest-*");
        File[] files = testDir.listFiles(fileFilter);
        for (int i = 0; i < files.length; i++) {
            File test = files[i];
            this.getLog().info("running test - " + test.getName());

            // create results writer
            try {
                testResultsWriter = new FileWriter(new File(testDir, "TEST-" + test.getName() + ".txt"));

                // run test script
                try {
                    ScriptUtil.runScript(test);
                    // write SUCCESS results to result file?
                    testResultsWriter.write("OK");
                } catch (MojoExecutionException e) {
                    // TODO need to get the actual GWT output here, rather than the script runner failure
                    testFailure = true;
                    testResultsWriter.write("FAILURE\n");
                    e.printStackTrace(new PrintWriter(testResultsWriter));
                }
                
                testResultsWriter.flush();
                testResultsWriter.close();
            } catch (IOException e) {
                throw new MojoExecutionException("unable to create test results output file", e);
            }
        }
        if (testFailure) {
            throw new MojoExecutionException("There were test failures - see test reports (target/gwtTest)");
        }        
    }
}
