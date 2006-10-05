package com.totsp.mavenplugin.gwt;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class GwtWebInfProcessor
{

    // TODO get web.xml DTD and make sure to get things in correct order
    
    
    // command line opts
    private static Option helpOpt = new Option("help", "print this message");
    private static Option moduleFilePathOpt = OptionBuilder.withArgName("moduleFilePath").hasArg().withDescription(
            "specify GWT module to inspect for service servlet definitions").create("moduleFilePath");
    private static Option webXmlFilePathOpt = OptionBuilder.withArgName("webXmlFilePath").hasArg().withDescription(
            "src web.xml file (maven.war.src/WEB-INF/web.xml)").create("webXmlFilePath");
    private static Options options = new Options();
    static
    {
        options.addOption(helpOpt);
        options.addOption(moduleFilePathOpt);
        options.addOption(webXmlFilePathOpt);
    }
    private static HelpFormatter formatter = new HelpFormatter();

    /**
     * Process supplied values for GWT module file and web.xml file
     * and create relative servlet mappings in web.xml for respective GWT service servlet entries.
     * (Peek at the GWT module file and extract all service servlet entries, then make sure 
     * those entries are in the web.xml, if they are not there then create them.)
     * 
     * @param moduleFilePath path to GWT module file
     * @param webXmlFilePath path to Maven maven.war.src
     * @return
     */
    private static String process(final String moduleFilePath, final String webXmlFilePath)
    {
        String returnValue = null;
        File webXml = null;
        File gwtMod = null;

        // obtain web-inf
        webXml = new File(webXmlFilePath);
        if ((webXml.exists()) && (webXml.canRead()) && (webXml.canWrite()))
        {
            System.out.println("found web.xml and its viable");
        }
        else
        {
            System.out.println("web.xml problem");
            returnValue = "supplied webXmlFilePath is not valid (not present or invalid permissions) - " + webXmlFilePath;
        }

        // obtain gwt module
        gwtMod = new File(moduleFilePath);
        if ((gwtMod.exists()) && (gwtMod.canRead()) && (gwtMod.canWrite()))
        {
            System.out.println("found gwtMod and its viable");
        }
        else
        {
            System.out.println("gwtMod problem");
            returnValue = "supplied moduleFilePath is invalid (not present or invalid permissions) - " + moduleFilePath;
        }

        // read service servlet defs from mod (all the "servlet" entries")
        try
        {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document gwtDoc = db.parse(gwtMod);
            Document webXmlDoc = db.parse(webXml);
            
            // parse the "servlet" elements from the gwt module
            NodeList gwtServlets = gwtDoc.getElementsByTagName("servlet");
            for (int i = 0; i < gwtServlets.getLength(); i++)
            {
                Element gwtServlet = (Element) gwtServlets.item(i);
                String path = gwtServlet.getAttribute("path");
                String className = gwtServlet.getAttribute("class");
                
                System.out.println("found gwtServlet DOM node - class = " + className);
                
                // for each servlet element in gwt doc, check existence of and or create 
                // "servlet" and "servlet-mapping" in web.xml
                NodeList webServlets = webXmlDoc.getElementsByTagName("servlet");
                
                
                
                
                
            }
            
            
        }
        catch (IOException e)
        {
            System.err.println(e.getMessage());
        }
        catch (ParserConfigurationException e)
        {
            System.err.println(e.getMessage());
        }
        catch (SAXException e)
        {
            System.err.println(e.getMessage());
        }
        
        return returnValue;
    }

    /**
     * Main runner.
     * 
     * 
     * @param args
     */
    public static void main(String[] args)
    {

        CommandLineParser parser = new GnuParser();
        try
        {
            // parse the command line arguments
            CommandLine line = parser.parse(options, args);

            // help
            if ((line == null) || (line.getOptions() == null) || (line.getOptions().length == 0)
                    || (line.hasOption("help")))
            {
                GwtWebInfProcessor.formatter.printHelp("GwtWebInfProcessor", options);
            }
            else
            {
                // process
                if ((line.hasOption("moduleFilePath")) && (line.hasOption("webXmlFilePath")))
                {
                    System.out.println(GwtWebInfProcessor.process(line.getOptionValue("moduleFilePath"), line
                            .getOptionValue("webXmlFilePath")));
                }
                else
                {
                    GwtWebInfProcessor.formatter.printHelp("GwtWebInfProcessor", options);
                }
            }

        }
        catch (ParseException exp)
        {
            // oops, something went wrong
            System.err.println("Parsing failed.  Reason: " + exp.getMessage());
        }

    }

}