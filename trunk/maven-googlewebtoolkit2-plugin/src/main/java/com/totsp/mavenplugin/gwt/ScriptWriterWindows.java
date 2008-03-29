/*
 * ScriptWriterWindows.java
 *
 * Created on November 23, 2007, 10:46 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.totsp.mavenplugin.gwt;

import org.apache.maven.artifact.DependencyResolutionRequiredException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.Collection;


/**
 *
 * @author rcooper
 */
public class ScriptWriterWindows {
    /** Creates a new instance of ScriptWriterWindows */
    public ScriptWriterWindows() {
    }

    public File writeRunScript(AbstractGWTMojo mojo) throws IOException, DependencyResolutionRequiredException {
        String filename = (mojo instanceof DebugMojo) ? "debug.cmd" : "run.cmd";
        File file = new File(mojo.getBuildDir(), filename);
        PrintWriter writer = new PrintWriter(new FileWriter(file));
        Collection<File> classpath = mojo.buildRuntimeClasspathList();
        writer.print("set CLASSPATH=");

        StringBuffer cpString = new StringBuffer();

        for (File f : classpath) {
            cpString.append("\"" + f.getAbsolutePath() + "\";");
            //break the line at 4000 characters to avout max size.
            if (cpString.length() > 4000) {
                writer.println(cpString);
                cpString = new StringBuffer();
                writer.print("set CLASSPATH=%CLASSPATH%;");
            }
        }
        writer.println(cpString);

        writer.println();

        String extra = (mojo.getExtraJvmArgs() != null) ? mojo.getExtraJvmArgs() : "";
        writer.print("\"" + mojo.JAVA_COMMAND + "\" " + extra + " -cp %CLASSPATH% ");

        if (mojo instanceof DebugMojo) {
            writer.print(" -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,address=");
            writer.print(mojo.getPort());
            writer.print(",suspend=y ");
        }

        writer.print("-Dcatalina.base=\"" + mojo.getTomcat().getAbsolutePath() + "\" ");
        writer.print(" com.google.gwt.dev.GWTShell");
        writer.print(" -gen \"");
        writer.print(mojo.getGen().getAbsolutePath());
        writer.print("\" -logLevel ");
        writer.print(mojo.getLogLevel());
        writer.print(" -style ");
        writer.print(mojo.getStyle());
        writer.print(" -out ");
        writer.print("\"" + mojo.getOutput().getAbsolutePath() + "\"");
        writer.print(" -port ");
        writer.print(Integer.toString(mojo.getPort()));

        if (mojo.isNoServer()) {
            writer.print(" -noserver ");
        }

        writer.print(" " + mojo.getRunTarget());
        writer.println();
        writer.flush();
        writer.close();

        return file;
    }

    public File writeCompileScript(AbstractGWTMojo mojo)
        throws IOException, DependencyResolutionRequiredException {
        File file = new File(mojo.getBuildDir(), "compile.cmd");
        PrintWriter writer = new PrintWriter(new FileWriter(file));
        Collection<File> classpath = mojo.buildClasspathList(false);
        writer.print("set CLASSPATH=");

        StringBuffer cpString = new StringBuffer();

        for (File f : classpath) {
            cpString.append("\"" + f.getAbsolutePath() + "\";");

            //break the line at 4000 characters to avout max size.
            if (cpString.length() > 4000) {
                writer.println(cpString);
                cpString = new StringBuffer();
                writer.print("set CLASSPATH=%CLASSPATH%;");
            }
        }
        writer.println(cpString);
        writer.println();

        for (String target : mojo.getCompileTarget()) {
            String extra = (mojo.getExtraJvmArgs() != null) ? mojo.getExtraJvmArgs() : "";
            writer.print("\"" + mojo.JAVA_COMMAND + "\" " + extra + " -cp %CLASSPATH% ");
            writer.print(" com.google.gwt.dev.GWTCompiler ");
            writer.print(" -gen \"");
            writer.print(mojo.getGen().getAbsolutePath());
            writer.print("\" -logLevel ");
            writer.print(mojo.getLogLevel());
            writer.print(" -style ");

            writer.print(" -out ");
            writer.print(mojo.getOutput().getAbsolutePath());
            writer.print(" ");
            writer.print(mojo.getStyle());

            if (mojo.isEnableAssertions()) {
                writer.print(" -ea ");
            }

            writer.print(target);
            writer.println();

            if( !(mojo.getGwtVersion().startsWith("1.3.") || mojo.getGwtVersion().startsWith("1.4."))){
                //TODO change this to inspect linker output
                String std = mojo.getOutput().getAbsolutePath() + "\\" + target + "\\std";
                writer.println("move " + std + "\\* " + mojo.getOutput().getAbsolutePath() + "\\" + target);
                writer.println("deltree " + std);

                String xs = mojo.getOutput().getAbsolutePath() + "\\" + target + "\\xs";
                writer.println("move " + xs + "\\* " + mojo.getOutput().getAbsolutePath() + "\\" + target);
                writer.println("deltree " + xs);
            }
        }

        writer.flush();
        writer.close();

        return file;
    }
}
