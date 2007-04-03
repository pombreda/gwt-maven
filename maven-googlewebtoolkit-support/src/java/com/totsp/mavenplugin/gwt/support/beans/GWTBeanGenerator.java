/*
 * GWTBeanGenerator.java
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
package com.totsp.mavenplugin.gwt.support.beans;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;


/**
 *
 * @author cooper
 */
public class GWTBeanGenerator extends BeanGeneratorBase{
    private static final HelpFormatter formatter = new HelpFormatter();
    private static final Option helpOpt = new Option(
            "help", "print this message"
            );
    private static final Option startBean = OptionBuilder
            .withArgName("startBean")
            .hasArg()
            .withDescription("bean to begin mapping from")
            .create("startBean");
    private static final Option destinationPackage = OptionBuilder
            .withArgName("destinationPackage")
            .hasArg()
            .withDescription("package to put generated beans into")
            .create("destinationPackage");
    private static final Option destinationDirectory = OptionBuilder
            .withArgName("destinationDirectory")
            .hasArg()
            .withDescription("directory to put generated java files into")
            .create("destinationDirectory");
    private static final Option withGetSet = new Option("withGetSet",
            "create getters and setters for GWT classes");
    private static final Option withPropertyChangeSupport = new Option( "withPropertyChangeSupport",
            "create change events for beans (implies withGetSet)");
    
    private final static Options options = new Options();
    
    static {
        options.addOption(helpOpt);
        options.addOption(startBean);
        options.addOption(destinationPackage);
        options.addOption(destinationDirectory);
        options.addOption(withPropertyChangeSupport);
        options.addOption(withGetSet);
    }
    
    
    /** Creates a new instance of GWTBeanGenerator */
    public GWTBeanGenerator() {
        super();
    }
    
    public static void main(String args[]) throws Exception{
        CommandLineParser parser = new GnuParser();
        
        // parse the command line arguments
        CommandLine line = parser.parse(options, args);
        
        // help
        if(
                (line == null) || (line.getOptions() == null) ||
                (line.getOptions().length == 0) ||
                (line.hasOption("help"))
                ) {
            GWTBeanGenerator.formatter.printHelp("GWTBeanGenerator",
                    options);
        }
        
        Class startBean = Class.forName( line.getOptionValue("startBean") );
        File directory = new File( line.getOptionValue("destinationDirectory"));
        directory.mkdirs();
        Bean root = new Bean( startBean );
        String packageName = line.getOptionValue( "destinationPackage");
        String packagePath = packageName.replace( '.', File.separatorChar );
        File packageDirectory = new File( directory, packagePath );
        
        packageDirectory.mkdirs();
        boolean getSet = line.hasOption("withGetSet");
        boolean propertyChangeSupport = line.hasOption("withPropertyChangeSupport");
        writeBean( packageName, packageDirectory, getSet, propertyChangeSupport, root );
        
    }
    
    
}
