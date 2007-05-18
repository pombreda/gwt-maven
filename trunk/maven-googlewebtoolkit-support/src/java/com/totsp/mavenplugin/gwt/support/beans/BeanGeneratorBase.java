/*
 * BeanGeneratorBase.java
 *
 * Created on February 17, 2007, 9:09 PM
 */

package com.totsp.mavenplugin.gwt.support.beans;

import de.hunsicker.jalopy.Jalopy;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.beans.IntrospectionException;
import java.io.FileOutputStream;
import java.util.Map.Entry;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.transform.JDOMSource;

/**
 * @author cooper
 */
public class BeanGeneratorBase {
    
    /**
     * Creates a new instance of BeanGeneratorBase
     */
    protected BeanGeneratorBase() {
    }
    
    
    private static HashMap<String, String> translatedClasses = new HashMap<String, String>();
    private static HashSet<File> writtenFiles = new HashSet<File>();
    
    
    
    public static void writeBean(String packageName, File packageDirectory, boolean getSet,
            boolean propSupport, Bean bean) throws IOException, IntrospectionException {
        writeBean(packageName, packageDirectory, getSet, propSupport, false, bean);
    }
    
    
    public static void writeBean(String packageName, File packageDirectory, boolean getSet,
            boolean propSupport, boolean overwrite, Bean bean) throws IOException, IntrospectionException {
        System.out.println(bean.getArrayDepth() + " " + bean.clazz);
        if (translatedClasses.containsKey(bean.clazz.getName())) {
            return;
        }
        
        HashMap<String, String> importMap = new HashMap<String, String>();
        
        String beanName = bean.clazz.getSimpleName();
        File javaFile = new File(packageDirectory, beanName + ".java");
        
        //If we're not overwriting anything, we simply make sure our new
        //classes avoid any files.  If we are overwriting, we need to make sure
        //we avoid any of the classes we have written in the generation.
        if (!overwrite) {
            for (int i = 0; javaFile.exists(); i++) {
                beanName += i;
                javaFile = new File(packageDirectory, beanName + ".java");
            }
        }
        writtenFiles.add(javaFile);
        System.out.println( "Generating: "+ bean.clazz.getCanonicalName() );
        System.out.println( "Generating: "+ javaFile.getAbsolutePath() );
        
        Document doc = new Document();
        Element root = new Element("class");
        doc.setRootElement( root );
        
        Element pkg = new Element("package");
        System.err.println(  );
        pkg.setText(getPackageName(bean.clazz) );
        root.addContent(pkg);
        
        Element name = new Element("shortName");
        name.setText( beanName );
        root.addContent( name );
        
        Element ext = new Element("extends");
        Element epkg = new Element("package");
        epkg.setText( getPackageName( bean.clazz.getSuperclass()) );
        ext.addContent( epkg );
        Element ename = new Element("shortName");
        ename.setText( bean.clazz.getSuperclass().getSimpleName() );
        ext.addContent( ename );
        root.addContent(ext);
        
        for( Entry<String, Bean> entry : bean.properties.entrySet()  ){
            Element prop = new Element("property")
            .addContent( new Element("name")
            .setText( entry.getKey() )
            ).addContent(
                    new Element("package")
                    .setText( getPackageName(entry.getValue().clazz)  )
                    ).addContent(
                    new Element("shortName")
                    .setText( entry.getValue().clazz.getSimpleName() )
                    );
            if( entry.getValue().getArrayDepth() > 0 ){
                prop.addContent(
                        new Element("arrayDepth").setText(""+entry.getValue().getArrayDepth() )
                        );
            }
            for( Bean p : entry.getValue().getParameterTypes() ){
                prop.addContent(
                        new Element( "parameterType")
                        .addContent(
                        new Element("package").setText(getPackageName(p.clazz) )
                        ).addContent(
                        new Element("shortName").setText(p.clazz.getSimpleName() )
                        )
                        );
            }
            root.addContent( prop );
        } // end propertyLoop
        
        TransformerFactory xformFactory =
                TransformerFactory.newInstance();
        try {
            //Get an XSL Transformer object
            Transformer transformer =
                    xformFactory.newTransformer( new StreamSource( BeanGeneratorBase.class.getResourceAsStream("./DTO.xsl")));
            transformer.setParameter( "propertyChangeSupport", propSupport ? "yes" : "no" );
            transformer.setParameter("gettersAndSetters", getSet ? "yes" : "no" );
            transformer.setParameter("destinationPackage", packageName );
            transformer.transform( new JDOMSource(doc), new StreamResult( new FileOutputStream(javaFile)));
            
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
            
        } catch (TransformerConfigurationException ex) {
            ex.printStackTrace();
        }catch (TransformerException ex) {
            ex.printStackTrace();
        }
        
        for( Bean prop : bean.properties.values() ){
            System.out.println( "--" +prop.clazz.getCanonicalName() + prop.isCustom() );
            if( prop.isCustom() ){
                writeBean(packageName, packageDirectory,  getSet,
                        propSupport,  overwrite,  prop );
            }
            for( Bean param : prop.getParameterTypes() ){
                if( param.isCustom() ){
                    writeBean(packageName, packageDirectory,  getSet,
                            propSupport,  overwrite,  param );
                }
            }
        }
        for( Bean param : bean.getParameterTypes() ){
            if( param.isCustom() ){
                writeBean(packageName, packageDirectory,  getSet,
                        propSupport,  overwrite,  param );
            }
        }
        if( bean.parent != null && bean.parent.isCustom() ){
            writeBean(packageName, packageDirectory,  getSet,
                    propSupport,  overwrite,  bean.parent );
        }
    }
    
    private static String getPackageName(Class clazz){
        
        return clazz.getCanonicalName().indexOf(".") == -1 ?
            "java.lang" :
            clazz.getCanonicalName().substring(0, clazz.getCanonicalName().lastIndexOf("."));
    }
}
