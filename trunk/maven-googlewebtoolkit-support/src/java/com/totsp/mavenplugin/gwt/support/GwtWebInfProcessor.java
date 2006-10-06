package com.totsp.mavenplugin.gwt.support;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

public class GwtWebInfProcessor
{

    // TODO get web.xml DTD and make sure to get things in correct order

    // command line opts
    private static Option helpOpt = new Option("help", "print this message");
    private static Option moduleNameOpt = OptionBuilder.withArgName("moduleName").hasArg().withDescription(
            "specify GWT module name (complete, com.mystuff.module.Module)").create("moduleName");
    private static Option moduleFilePathOpt = OptionBuilder.withArgName("modulePath").hasArg().withDescription(
            "specify GWT module to inspect for service servlet definitions").create("modulePath");
    private static Option webXmlFilePathOpt = OptionBuilder.withArgName("webXmlPath").hasArg().withDescription(
            "src web.xml file (maven.war.src/WEB-INF/web.xml)").create("webXmlPath");
    private static Options options = new Options();
    static
    {
        options.addOption(helpOpt);
        options.addOption(moduleNameOpt);
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
     * @param gwtModName name of gwt module (complete name com.myproject.module.Module)
     * @param gwtModFilePath path to GWT module file
     * @param webXmlFilePath path to Maven maven.war.src
     * @return
     */
    private static String process(final String gwtModName, final String gwtModFilePath, final String webXmlFilePath)
    {
        String returnValue = null;
        File webXml = null;
        File gwtMod = null;

        // obtain gwt module
        gwtMod = new File(gwtModFilePath);
        if ((gwtMod.exists()) && (gwtMod.canRead()) && (gwtMod.canWrite()))
        {
            ///System.out.println("found gwtMod and its viable");
        }
        else
        {
            ///System.out.println("gwtMod problem");
            return "supplied moduleFilePath is invalid (not present or invalid permissions) - " + gwtModFilePath;
        }

        // obtain web.xml
        webXml = new File(webXmlFilePath);
        if ((webXml.exists()) && (webXml.canRead()) && (webXml.canWrite()))
        {
            ///System.out.println("found web.xml and its viable");
        }
        else
        {
            ///System.out.println("web.xml problem");
            // TODO - create web.xml here if it does not exist?
            return "supplied webXmlFilePath is not valid (not present or invalid permissions) - " + webXmlFilePath;
        }

        // get servlet descriptors for the gwt module - and if present continue
        List gwtServlets = GwtWebInfProcessor.getGwtServletDescriptors(gwtMod);
        if ((gwtServlets != null) && (gwtServlets.size() > 0))
        {
            // get servlet descriptors for web.xml
            List webServlets = GwtWebInfProcessor.getWebServletDescriptors(webXml);            

            // get List of items that NEED TO BE ADDED to web.xml (those that are in gwt and not in web.xml)
            List servletsForWebXml = GwtWebInfProcessor.getServletsForWebXml(gwtServlets, webServlets);
            if (servletsForWebXml == null)
            {
                returnValue = "nothing to do - gwt module and web.xml already synchronized";
            }
            else
            {
                // ADD em to web.xml
                boolean success = GwtWebInfProcessor.addServletsToWebXml(servletsForWebXml, webXml, gwtModName);
                if (success)
                {
                    returnValue = "complete - synchronized gwt module and web.xml (" + servletsForWebXml.size()
                            + " servlet(s) from gwt module added to web.xml)";
                }
                else
                {
                    returnValue = "ERROR - unable to synchronize gwt module and web.xml";
                }
            }
        }
        else
        {
            returnValue = "nothing to do - gwt module contains no servlet definitions";
        }
        return returnValue;
    }

    /**
     * Based on supplied List of ServletDescriptors add items to the web.xml file.
     *
     * 
     * @param servlets
     * @return
     */
    private static boolean addServletsToWebXml(List servlets, File webXml, String gwtModName)
    {
        boolean success = false;

        // get root element 
        try
        {
            Document document = new SAXBuilder().build(webXml);
            Element root = document.getRootElement();
            if (root.getName().equalsIgnoreCase("web-app"))
            {
                // TODO - get DTD and put servlet and servlet-mapping elements in correct locations        

                if (servlets != null)
                {
                    for (int i = 0; i < servlets.size(); i++)
                    {
                        ServletDescriptor servletDesc = (ServletDescriptor) servlets.get(i);
                        String path = servletDesc.getPath();
                        if (path.startsWith("/"))
                        {
                            path = path.substring(1, path.length());
                        }
                        String className = servletDesc.getClassName();

                        // servlet section
                        Element servlet = new Element("servlet");
                        Element servletName = new Element("servlet-name");
                        servletName.setText(path);
                        Element servletClass = new Element("servlet-class");
                        servletClass.setText(className);
                        servlet.addContent(servletName);
                        servlet.addContent(servletClass);

                        // servlet mapping section
                        Element servletMapping = new Element("servlet-mapping");
                        Element servletNameAgain = new Element("servlet-name");
                        servletNameAgain.setText(path);
                        Element urlPattern = new Element("url-pattern");
                        urlPattern.setText("/" + gwtModName + "/" + path);
                        servletMapping.addContent(servletNameAgain);
                        servletMapping.addContent(urlPattern);
                        
                        root.addContent(servlet);
                        root.addContent(servletMapping);
                    }
                    XMLOutputter outputter = new XMLOutputter();
                    FileWriter writer = new FileWriter(webXml);
                    outputter.output(document, writer);
                    writer.close();
                    success = true;
                }
                
            }
            else
            {
                System.err.println("root element in web.xml not web-app");
            }
        }
        catch (IOException e)
        {
            System.err.println(e.getMessage());
        }
        catch (JDOMException e)
        {
            System.err.println(e.getMessage());
        }
        return success;
    }

    /**
     * Get List of servlet elements which are present in gwt module 
     * and NOT present in web.xml - the ServletDescriptor(s) which 
     * need to be added to the web.xml.     
     * 
     * @param gwtServlets
     * @param webServlets
     * @return
     */
    private static List getServletsForWebXml(List gwtServlets, List webServlets)
    {
        List servletsForWebXml = null;
        // parse to obtain list of servlets in gwt mod that are NOT present in web.xml
        for (int i = 0; gwtServlets != null && i < gwtServlets.size(); i++)
        {
            ServletDescriptor gwtServlet = (ServletDescriptor) gwtServlets.get(i);
            String gwtServletClass = gwtServlet.getClassName();
            boolean present = false;
            for (int j = 0; webServlets != null && j < webServlets.size(); j++)
            {
                ServletDescriptor webServlet = (ServletDescriptor) webServlets.get(j);
                String webServletClass = webServlet.getClassName();
                if (webServletClass.equals(gwtServletClass))
                {
                    present = true;
                    break;
                }
            }
            if (!present)
            {
                if (servletsForWebXml == null)
                {
                    servletsForWebXml = new ArrayList();
                }
                servletsForWebXml.add(gwtServlet);
            }
        }
        return servletsForWebXml;
    }

    /**
     * Return List of ServletDescriptor from gwt module file.
     * 
     * @param gwtModFile
     * @return
     */
    private static List getGwtServletDescriptors(File gwtModFile)
    {
        ArrayList servletElements = null;
        try
        {
            Document document = new SAXBuilder().build(gwtModFile);
            Element element = document.getRootElement();
            List servlets = element.getChildren("servlet");
            for (int i = 0; i < servlets.size(); i++)
            {
                Element servlet = (Element) servlets.get(i);
                String servletPath = servlet.getAttributeValue("path");
                String servletClass = servlet.getAttributeValue("class");
                ServletDescriptor servletDesc = new ServletDescriptor(servletPath, servletClass);
                if (servletElements == null)
                {
                    servletElements = new ArrayList();
                }
                servletElements.add(servletDesc);
            }
        }
        catch (IOException e)
        {
            System.err.println(e.getMessage());
        }
        catch (JDOMException e)
        {
            System.err.println(e.getMessage());
        }
        return servletElements;
    }


    /**
     * Return List of ServletDescriptor from web.xml file.
     * 
     * @param webXmlFile
     * @return
     */
    private static List getWebServletDescriptors(File webXmlFile)
    {
        ArrayList servletElements = null;
        try
        {
            Document document = new SAXBuilder().build(webXmlFile);
            Element element = document.getRootElement();
            List servlets = element.getChildren("servlet");
            for (int i = 0; i < servlets.size(); i++)
            {
                Element servlet = (Element) servlets.get(i);
                String servletPath = servlet.getChildText("servlet-name");
                String servletClass = servlet.getChildText("servlet-class");
                ServletDescriptor servletDesc = new ServletDescriptor(servletPath, servletClass);
                if (servletElements == null)
                {
                    servletElements = new ArrayList();
                }
                servletElements.add(servletDesc);
            }
        }
        catch (IOException e)
        {
            System.err.println(e.getMessage());
        }
        catch (JDOMException e)
        {
            System.err.println(e.getMessage());
        }
        return servletElements;
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
                if ((line.hasOption("modulePath")) && (line.hasOption("moduleName")) && (line.hasOption("webXmlPath")))
                {
                    System.out.println(GwtWebInfProcessor.process(line.getOptionValue("moduleName"), line
                            .getOptionValue("modulePath"), line.getOptionValue("webXmlPath")));
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