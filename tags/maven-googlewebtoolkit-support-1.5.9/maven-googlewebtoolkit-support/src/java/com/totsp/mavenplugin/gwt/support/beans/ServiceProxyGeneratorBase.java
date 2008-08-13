/*
 * ServiceProxyGeneratorBase.java
 *
 * Created on May 19, 2007, 1:13 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.totsp.mavenplugin.gwt.support.beans;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.transform.JDOMSource;

import de.hunsicker.jalopy.Jalopy;

/**
 *
 * @author rcooper
 */
public class ServiceProxyGeneratorBase {
    
    private static HashSet<Class> beansToGenerate = new HashSet();
    private static HashSet<Class> exceptionsToGenerate = new HashSet();
    
    /** Creates a new instance of ServiceProxyGeneratorBase */
    public ServiceProxyGeneratorBase() {
    }
    public static void writeService(String packageName, File packageDirectory, boolean getSet,
            boolean propSupport, boolean overwrite, Class serviceInterface, Class implementation) throws Exception {
        Element element = buildServiceElement( serviceInterface, implementation );
        
        String beanName = serviceInterface.getSimpleName();
        
        
        TransformerFactory xformFactory =
                TransformerFactory.newInstance();
        try {
            
            ArrayList<GeneratedFile> transforms = new ArrayList();
            GeneratedFile service = new GeneratedFile();
            service.name = beanName;
            service.xsl = BeanGeneratorBase.class.getResourceAsStream("./Service.xsl");
            service.destinationDirectory = new File(packageDirectory, "client");
            service.element = element;
            transforms.add( service );
            
            GeneratedFile async = new GeneratedFile();
            async.name = beanName+"Async";
            async.xsl = BeanGeneratorBase.class.getResourceAsStream("./ServiceAsync.xsl");
            async.destinationDirectory = service.destinationDirectory;
            async.element = element;
            transforms.add( async );
            
            GeneratedFile servlet = new GeneratedFile();
            servlet.name = beanName+"Servlet";
            servlet.xsl = BeanGeneratorBase.class.getResourceAsStream("./ServiceServlet.xsl");
            servlet.destinationDirectory = new File( packageDirectory, "server");
            servlet.element = element;
            transforms.add( servlet );
            
            for(Class e : exceptionsToGenerate ){
                GeneratedFile exception = new GeneratedFile();
                exception.name = e.getSimpleName();
                exception.xsl = BeanGeneratorBase.class.getResourceAsStream("./Exception.xsl");
                exception.destinationDirectory = service.destinationDirectory;
                exception.element = buildExceptionElement( e );
                transforms.add( exception );
            }
            
            for( GeneratedFile entry : transforms ){
                entry.destinationDirectory.mkdirs();
                File javaFile = new File(entry.destinationDirectory, entry.name + ".java");
                System.out.print( "Generating: "+ serviceInterface.getCanonicalName() );
                System.out.println( " to file: "+ javaFile.getAbsolutePath() );
                //If we're not overwriting anything, we simply make sure our new
                //classes avoid any files.  If we are overwriting, we need to make sure
                //we avoid any of the classes we have written in the generation.
                if (!overwrite) {
                    for (int i = 0; javaFile.exists(); i++) {
                        beanName += i;
                        javaFile = new File(entry.destinationDirectory, entry.name + ".java");
                    }
                }
                //Get an XSL Transformer object
                Transformer transformer =
                        xformFactory.newTransformer( new StreamSource( entry.xsl ));
                transformer.setParameter( "propertyChangeSupport", propSupport ? "yes" : "no" );
                transformer.setParameter("gettersAndSetters", getSet ? "yes" : "no" );
                transformer.setParameter("destinationPackage", packageName );
                
                Document doc = new Document();
                doc.setRootElement( entry.element );
                transformer.transform( new JDOMSource(doc), new StreamResult( new FileOutputStream(javaFile)));
                doc.detachRootElement();
                Jalopy jalopy = new Jalopy();
                
                // specify input and output target
                jalopy.setInput(javaFile);
                jalopy.setOutput(javaFile);
                
                // format and overwrite the given input file
                jalopy.format();
                
                if (jalopy.getState() == Jalopy.State.OK)
                    System.out.println(javaFile + " successfully formatted");
                else if (jalopy.getState() == Jalopy.State.WARN)
                    System.out.println(javaFile + " formatted with warnings");
                else if (jalopy.getState() == Jalopy.State.ERROR)
                    System.out.println(javaFile + " could not be formatted");
                
            }
            writeDTOs( packageName+".client", service.destinationDirectory, getSet, propSupport, overwrite );
        } catch (TransformerConfigurationException ex) {
            ex.printStackTrace();
        }catch (TransformerException ex) {
            ex.printStackTrace();
        }
        
        
        
    }
    
    private static void writeDTOs( String packageName, File packageDirectory, boolean getSet,
            boolean propSupport, boolean overwrite ) throws Exception {
        for( Class clazz : ServiceProxyGeneratorBase.beansToGenerate ){
            if( clazz.isPrimitive() ){
                continue;
            }
            Bean b = new Bean( clazz );
            if( b.isBaseType() ){
                continue;
            }
            
            BeanGeneratorBase.writeBean( packageName, packageDirectory, getSet, propSupport, overwrite, b );
            
        }
        
    }
    
    private static Element buildExceptionElement( Class clazz ){
        Element exception = new Element( "exception" )
        .addContent(
                new Element("shortName").setText( clazz.getSimpleName() )
                );
        return exception;
    }
    
    
    private static Element buildServiceElement( Class clazz, Class impl ){
        
        Element service = new Element( "service");
        service.addContent(
                new Element("package").setText( BeanGeneratorBase.getPackageName(clazz))
                ).addContent(
                new Element("shortName").setText( clazz.getSimpleName() )
                );
        
        if( impl != null ){
            service.addContent(
                    new Element("implementation").setText(impl.getCanonicalName() )
                    );
        }
        for( Method m : clazz.getDeclaredMethods() ){
            Element method = new Element("method");
            method.addContent(
                    new Element("name").setText( m.getName() )
                    );
            if( m.getReturnType() != null ){
                method.addContent(
                        new Element("returnType").addContent(
                        getTypeInfo( m.getReturnType() )
                        )
                        );
                Class check = m.getReturnType();
                while( check.isArray() ){
                    check = check.getComponentType();
                }
                beansToGenerate.add( check );
            }
            int argCount = 0;
            for( Class arg : m.getParameterTypes() ){
                method.addContent(
                        new Element("argument").addContent(
                        new Element("name").setText( "arg"+argCount )
                        ).addContent(
                        getTypeInfo( arg )
                        )
                        );
                Class check = arg;
                while( check.isArray() ){
                    check = check.getComponentType();
                }
                beansToGenerate.add( check );
            }
            for( Class thrown : m.getExceptionTypes() ){
                method.addContent(
                        new Element( "thrown" ).addContent( getTypeInfo( thrown ) )
                        );
                exceptionsToGenerate.add( thrown );
            }
            service.addContent( method );
        }
        return service;
    }
    
    private static List<Element> getTypeInfo( Class clazz ){
        List<Element> elements = new ArrayList();
        int arrayDepth = 0;
        Class check = clazz;
        while( check.isArray() ){
            arrayDepth++;
            check = check.getComponentType();
        }
        if( arrayDepth > 0 ){
            elements.add( new Element("arrayDepth").setText( ""+arrayDepth ) );
        }
        elements.add(
                new Element("package").setText( BeanGeneratorBase.getPackageName(check) )
                );
        
        elements.add(
                new Element("shortName").setText( check.getSimpleName() )
                );
        return elements;
    }
    
    private static class GeneratedFile{
        File destinationDirectory;
        InputStream xsl;
        String name;
        Element element;
    }
}
