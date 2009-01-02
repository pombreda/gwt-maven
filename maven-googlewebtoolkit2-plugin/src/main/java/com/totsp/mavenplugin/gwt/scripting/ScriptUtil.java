package com.totsp.mavenplugin.gwt.scripting;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;

import com.totsp.mavenplugin.gwt.AbstractGWTMojo;
import com.totsp.mavenplugin.gwt.scripting.TestResult.TestCode;



public final class ScriptUtil {

  
  
   private ScriptUtil() {
   }
   
   
   
   public static int runScript(final File exec, final boolean exceptionOnFailure) throws MojoExecutionException {
     ProcessWatcher pw = null;
     if (AbstractGWTMojo.isWindows) {
        pw = new ProcessWatcher("\"" + exec.getAbsolutePath() + "\"");
     } else {
        pw = new ProcessWatcher(exec.getAbsolutePath().replaceAll(" ", "\\ "));
     }

      try {
        pw.startProcess(System.out, System.err);
        int retVal = pw.waitFor();
        
        if (!exceptionOnFailure) {
          return retVal;
        }
        
        if (retVal != 0) {
           throw new MojoExecutionException(exec.getName() + " script exited abnormally with code - " + retVal);
        } else {
          return 0;
        }
      } catch (IOException e) {
        throw new MojoExecutionException(
            "Exception attempting to run script - " + exec.getName(), e);
      } catch (InterruptedException e) {
        throw new MojoExecutionException(
            "Exception attempting to run script - " + exec.getName(), e);
      }
   }

   
   
   public static void runScript(final File exec) throws MojoExecutionException {
      runScript(exec, true);
   }

   
   
   public static TestResult runTestScript(final File exec) throws MojoExecutionException {
      TestResult testResult = new TestResult();
      StringBuilder out = new StringBuilder();
      StringBuilder err = new StringBuilder();
      ProcessWatcher pw = null;
      if (AbstractGWTMojo.isWindows) {
         pw = new ProcessWatcher("\"" + exec.getAbsolutePath() + "\"");
      }
      else {
         pw = new ProcessWatcher(exec.getAbsolutePath().replaceAll(" ", "\\ "));
      }

      try {
         pw.startProcess(out, err);
         pw.waitFor();

         // if err has anything it's an exception - excepting special Leopard stuff
         if (err.length() > 0) {
            boolean validError = true;

            // the Mac VM will log CocoaComponent messages to stderr, falsely triggering the exception
            if (AbstractGWTMojo.isMac) {
               validError = false;
               final String[] errLines = err.toString().split("\n");
               for (int i = 0; i < errLines.length; ++i) {
                  final String currentErrLine = errLines[i].trim();
                  if (!currentErrLine.endsWith("[JavaCocoaComponent compatibility mode]: Enabled")
                           && !currentErrLine
                                    .endsWith("[JavaCocoaComponent compatibility mode]: Setting timeout for SWT to 0.100000")
                           && currentErrLine.length() != 0) {
                     validError = true;
                     break;
                  }
               }
            }

            if (validError) {
               throw new MojoExecutionException("error attempting to run test - " + exec.getName() + " - "
                        + err.toString());
            }
         }

         // otherwise populate and return the TestResult
         //
         // SUCCESS ends up in system.out "OK"
         // FAILURE ends up in system.out "FAILURES!!!"
         // ERROR ends up in system.out "FAILURES!!!"
         //
         // example gwt output below
         //
         // FAILURES!!!
         // Tests run: 1,  Failures: 0,  Errors: 1
         // 
         // OK (1 test)

         String[] lines = null;
         if (AbstractGWTMojo.isWindows) {
            lines = out.toString().split("\r\n");
         }
         else {
            lines = out.toString().split("\n");
         }
         String lastLine = lines[lines.length - 1];
         testResult.lastLine = lastLine;
         if (lastLine.indexOf("Tests run") != -1) {
            // TODO add parsing to differentiate FAILURE and ERROR, or BOTH?
            testResult.code = TestCode.FAILURE;
         }
         else if (lastLine.indexOf("OK") != -1) {
            testResult.code = TestCode.SUCCESS;
         }
         testResult.message = out.toString();
      }
      catch (Exception e) {
         throw new MojoExecutionException("error attempting to run test - " + exec.getName() + " - " + e.getMessage(),
                  e);
      }
      return testResult;
   }
}


