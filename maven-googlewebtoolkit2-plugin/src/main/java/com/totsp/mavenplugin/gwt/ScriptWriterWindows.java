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
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;

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
            //break the line at 4000 characters to about max size.
            if (cpString.length() > 4000) {
                writer.println(cpString);
                cpString = new StringBuffer();
                writer.print("set CLASSPATH=%CLASSPATH%;");
            }
        }
        writer.println(cpString);

        writer.println();

        String extra = (mojo.getExtraJvmArgs() != null) ? mojo.getExtraJvmArgs() : "";
        writer.print("\"" + AbstractGWTMojo.JAVA_COMMAND + "\" " + extra + " -cp %CLASSPATH% ");

        if (mojo instanceof DebugMojo) {
            writer.print(" -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,address=");
            writer.print(mojo.getDebugPort());
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

    public File writeRunScriptNew(AbstractGWTMojo mojo) throws IOException, DependencyResolutionRequiredException {

        writeClassPathFile(mojo);
        writeClasspathInvoker(mojo);

        String filename = (mojo instanceof DebugMojo) ? "debug.cmd" : "run.cmd";
        File file = new File(mojo.getBuildDir(), filename);
        PrintWriter writer = new PrintWriter(new FileWriter(file));

        String targetDir = mojo.getBuildDir().getAbsolutePath();

        String extra = (mojo.getExtraJvmArgs() != null) ? mojo.getExtraJvmArgs() : "";

        writer.print("\"" + AbstractGWTMojo.JAVA_COMMAND + "\" " + extra + " -cp \"" + targetDir + "\" ");

        if (mojo instanceof DebugMojo) {
            writer.print(" -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,address=");
            writer.print(mojo.getDebugPort());
            writer.print(",suspend=y ");
        }

        writer.print("-Dcatalina.base=\"" + mojo.getTomcat().getAbsolutePath() + "\" ");
        writer.print(" ClasspathInvoker \"" + targetDir + "/classpath\" ");
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

        writeClasspathInvoker(mojo);
        writeClassPathFile(mojo);

        File file = new File(mojo.getBuildDir(), "compile.cmd");
        PrintWriter writer = new PrintWriter(new FileWriter(file));

        String targetDir = mojo.getBuildDir().getAbsolutePath();

        for (String target : mojo.getCompileTarget()) {
            String extra = (mojo.getExtraJvmArgs() != null) ? mojo.getExtraJvmArgs() : "";

            writer.print("\"" + AbstractGWTMojo.JAVA_COMMAND + "\" " + extra + " -cp \"" + targetDir + "\" ");
            writer.print(" ClasspathInvoker \"" + targetDir + "/classpath\" ");
            writer.print(" com.google.gwt.dev.GWTCompiler ");
            writer.print(" -gen \"");
            writer.print(mojo.getGen().getAbsolutePath());
            writer.print("\" -logLevel ");
            writer.print(mojo.getLogLevel());
            writer.print(" -style ");
            writer.print(" ");
            writer.print(mojo.getStyle());

            writer.print(" -out ");
            writer.print(mojo.getOutput().getAbsolutePath());
            writer.print(" ");

            if (mojo.isEnableAssertions()) {
                writer.print(" -ea ");
            }

            writer.print(" " + target);
            writer.println();

            // if gwtVersion is NOT 1.3 or 1.4, then assume it is 1.5 or higher, and move "std" and "xs" linker directories to target
            if(!(mojo.getGwtVersion().startsWith("1.3.") || mojo.getGwtVersion().startsWith("1.4."))){

                String std = mojo.getOutput().getAbsolutePath() + "\\" + target + "\\std";
                writer.println("move " + std + "\\* " + mojo.getOutput().getAbsolutePath() + "\\" + target);
                writer.println("del " + std + " /S /Q");

                String xs = mojo.getOutput().getAbsolutePath() + "\\" + target + "\\xs";
                writer.println("move " + xs + "\\* " + mojo.getOutput().getAbsolutePath() + "\\" + target);
                writer.println("del " + xs + " /S /Q");
            }
        }

        writer.flush();
        writer.close();

        return file;
    }

    protected void writeClasspathInvoker(AbstractGWTMojo mojo) throws IOException {

        File file = new File(mojo.getBuildDir(), "ClasspathInvoker.class");
        URL url = getClass().getClassLoader().getResource("/ClasspathInvoker.class");
        InputStream in = url.openStream();

        copyResource(in, file);
    }

    protected void copyResource(InputStream in, File outFile) throws IOException {

        OutputStream out = new FileOutputStream(outFile);
        try {
            try {
                byte[] buffer = new byte[20480];
                int r = in.read(buffer);
                while (r > 0) {
                    out.write(buffer, 0, r);
                    r = in.read(buffer);
                }
            } finally {
                in.close();
            }
        } finally {
            out.close();
        }
    }

    protected void writeClassPathFile(AbstractGWTMojo mojo)
        throws IOException, DependencyResolutionRequiredException {

        File file = new File(mojo.getBuildDir(), "classpath");
        PrintWriter writer = new PrintWriter(new FileWriter(file));
        try {
            Collection<File> classpath = mojo.buildClasspathList(false);

            for (File f : classpath) {
                writer.println(f.getAbsolutePath());
            }
        } finally {
            writer.close();
        }
    }
}
