/*
 * Main.java
 *
 * Created on October 10, 2006, 3:56 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.totsp.mavenplugin.gwt.support;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 *
 * @author cooper
 */
public class Main {
    private static HelpFormatter formatter = new HelpFormatter();
    
    
    // command line opts
    private static Option helpOpt = new Option("help", "print this message");
    private static Option moduleNameOpt = OptionBuilder.withArgName(
            "moduleName"
        ).hasArg()
                                                       .withDescription(
            "specify GWT module name (complete, com.mystuff.module.Module)"
        ).create("moduleName");
    private static Option targetWebXmlPath = OptionBuilder.withArgName(
            "targetWebXmlPath"
        ).hasArg()
                                                          .withDescription(
            "specify the output file to write to (${maven.war.webapp.dir}/WEB-INF/web.xml)"
        ).create("targetWebXmlPath");
    private static Option webXmlFilePathOpt = OptionBuilder.withArgName(
            "webXmlPath"
        ).hasArg()
                                                           .withDescription(
            "src web.xml file (maven.war.src/WEB-INF/web.xml)"
        ).create("webXmlPath");
   
    private static Options options = new Options();

    static {
        options.addOption(helpOpt);
        options.addOption(moduleNameOpt);
        options.addOption(targetWebXmlPath);
        options.addOption(webXmlFilePathOpt);
    }
    
    
    /** Creates a new instance of Main */
    private Main() {
        super();
    }
    /**
     * Main runner.
     *
     *
     * @param args
     */
    public static void main(String[] args) {
        CommandLineParser parser = new GnuParser();

        try {
            // parse the command line arguments
            CommandLine line = parser.parse(options, args);

            // help
            if(
                (line == null) || (line.getOptions() == null) ||
                    (line.getOptions().length == 0) ||
                    (line.hasOption("help"))
            ) {
                Main.formatter.printHelp(
                    "GwtWebInfProcessor", options
                );
            } else {
                // process
                if(
                    (line.hasOption("moduleName")) &&
                        (line.hasOption("targetWebXmlPath")) &&
                        (line.hasOption("webXmlPath"))
                ) {
                    try{
                        GwtWebInfProcessor processor = new GwtWebInfProcessor(
                            line.getOptionValue("moduleName"),
                            line.getOptionValue("targetWebXmlPath"),
                            line.getOptionValue("webXmlPath"),
                            false);
                        processor.process();
                    } catch(ExitException ee ){
                        ee.printStackTrace();
                        System.exit(0);
                    } catch(Exception e){
                        e.printStackTrace();
                        System.exit(1);
                    }
                    
                } else {
                    Main.formatter.printHelp(
                        "GwtWebInfProcessor", options
                    );
                }
            }
        } catch(ParseException exp) {
            exp.printStackTrace();
            // oops, something went wrong
            System.err.println("Parsing failed.  Reason: " + exp.getMessage());
        }
    }
    
}
