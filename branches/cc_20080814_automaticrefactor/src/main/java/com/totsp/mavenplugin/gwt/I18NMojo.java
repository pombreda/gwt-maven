/*
 * I18NMojo.java
 *
 * Created on August 19th, 2008
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

import com.totsp.mavenplugin.gwt.scripting.ProcessWatcher;
import com.totsp.mavenplugin.gwt.scripting.ScriptWriterUnix;
import com.totsp.mavenplugin.gwt.scripting.ScriptWriterWindows;

/**
 * Mojo that performs GWT i18n
 * 
 * @goal i18n
 * @requiresDependencyResolution compile
 * @phase process-resources
 * @description Creates I18N interfaces for constants and messages files.
 * 
 * @author Sascha-Matthias Kulawik <sascha@kulawik.de>
 * @author ccollins
 */
public class I18NMojo extends AbstractGWTMojo {

    /** Creates a new instance of I18NMojo */
    public I18NMojo() {
        super();
    }

    public void execute() throws MojoExecutionException, MojoFailureException {

        if (this.getI18nMessagesNames() == null && this.getI18nConstantsNames() == null) {
            throw new MojoExecutionException(
                    "neither i18nConstantsNames nor i18nMessagesNames present, cannot execute i18n goal");
        }

        if (!this.getI18nOutputDir().exists()) {
            if (getLog().isInfoEnabled())
                getLog().info("I18NModule is creating target directory " + getI18nOutputDir().getAbsolutePath());
            this.getI18nOutputDir().mkdirs();
        }

        if (AbstractGWTMojo.OS_NAME.startsWith(WINDOWS)) {
            ScriptWriterWindows writer = new ScriptWriterWindows();
            try {
                File exec = writer.writeI18nScript(this);
                ProcessWatcher pw = new ProcessWatcher("\"" + exec.getAbsolutePath() + "\"");
                pw.startProcess(System.out, System.err);
                int retVal = pw.waitFor();
                if (retVal != 0) {
                    throw new MojoExecutionException("i18n script exited abnormally with code - " + retVal);
                }
            } catch (Exception e) {
                throw new MojoExecutionException("Exception attempting run.", e);
            }
        } else {
            ScriptWriterUnix writer = new ScriptWriterUnix();
            try {
                File exec = writer.writeI18nScript(this);
                ProcessWatcher pw = new ProcessWatcher(exec.getAbsolutePath().replaceAll(" ", "\\ "));
                pw.startProcess(System.out, System.err);
                int retVal = pw.waitFor();
                if (retVal != 0) {
                    throw new MojoExecutionException("i18n script exited abnormally with code - " + retVal);
                }
            } catch (Exception e) {
                throw new MojoExecutionException("Exception attempting run.", e);
            }
        }

    }

}
