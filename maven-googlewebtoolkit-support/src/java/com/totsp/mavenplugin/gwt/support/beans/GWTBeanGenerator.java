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
public class GWTBeanGenerator {
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
        Bean root = new Bean( "", startBean );
        String packageName = line.getOptionValue( "destinationPackage");
        String packagePath = packageName.replace( '.', File.separatorChar );
        File packageDirectory = new File( directory, packagePath );
        
        packageDirectory.mkdirs();
        boolean getSet = line.hasOption("withGetSet");
        boolean propertyChangeSupport = line.hasOption("withPropertyChangeSupport");
        writeBean( packageName, packageDirectory, getSet, propertyChangeSupport, root );
        
    }
    
    private static void writeBean( String packageName, File packageDirectory,
            boolean getSet, boolean propSupport, Bean bean) throws IOException {
        String beanName = bean.clazz.getSimpleName();
        File javaFile = new File( packageDirectory, beanName+".java");
        for( int i=0; javaFile.exists() ; i++ ){
            javaFile = new File( packageDirectory, beanName+i+".java");
        }
        bean.name = javaFile.getName();
        PrintWriter pw = new PrintWriter( javaFile );
        pw.print( "package ");
        pw.print( packageName );
        pw.println(";");
        pw.println("import com.google.gwt.user.client.rpc.IsSerializable;");
        
        if( propSupport ){
            pw.println( "import java.beans.PropertyChangeSupport;");
            pw.println( "import java.beans.PropertyChangeListener;");
        }
        
        pw.print( "public class ");
        pw.print( bean.name.substring(0, bean.name.indexOf(".") ) );
        pw.println( " implements IsSerializable {");
        String access = getSet || propSupport ? "private " : "public ";
        String propSupportName = "changes"+System.currentTimeMillis();
        if( propSupport ){
            pw.print( "private PropertyChangeSupport ");
            pw.print( propSupportName );
            pw.println( "= new PropertyChangeSupport( this );");
            pw.println( "public void addPropertyChangeListener( PropertyChangeListener l ){ ");
            pw.print( "\t");
            pw.print( propSupportName );
            pw.println(".addPropertyChangeListener( l ); ");
            pw.println("}");
            
            pw.println( "public void addPropertyChangeListener( String propertyName, PropertyChangeListener l ){");
            pw.print( "\t");
            pw.print( propSupportName );
            pw.println(".addPropertyChangeListener( propertyName, l );");
            pw.println("}");
            
            pw.println( "public void removePropertyChangeListener( PropertyChangeListener l ){ ");
            pw.print( "\t");
            pw.print( propSupportName );
            pw.println(".removePropertyChangeListener( l );");
            pw.println("}");
            
            pw.println( "public void removePropertyChangeListener( String propertyName, PropertyChangeListener l ){ ");
            pw.print( "\t");
            pw.print( propSupportName );
            pw.println(".removePropertyChangeListener( propertyName, l );");
            pw.println("}");
            
            pw.println( "public PropertyChangeListener[] getPropertyChangeListeners(){ ");
            pw.print( "\t return ");
            pw.print( propSupportName );
            pw.println( ".getPropertyChangeListeners();");
            pw.println("}");
        }
        
        for( Iterator<String> attributes = bean.properties.keySet().iterator();
        attributes.hasNext(); ){
            String att = attributes.next();
            Bean attType = bean.properties.get( att );
            String typeString = resolveType( attType.getType() );
            for( int i=0; i < attType.getArrayDepth() ; i++){
                typeString +="[]";
            }
            typeString +=" ";
            if( attType.getTypeArgs() != null ){
                pw.println("/**");
                pw.print( " * @gwt.typeArgs ");
                pw.println( attType.getTypeArgs() );
                pw.println( " */");
            }
            
            pw.print( access );
            pw.print( typeString );
            pw.print( att );
            pw.println(";");
            
            if( getSet || propSupport ){
                String methodName = att.substring(0,1).toUpperCase();
                if( att.length() > 1 ){
                    methodName += att.substring(1, att.length());
                }
                if( attType.getTypeArgs() != null ){
                    pw.println("/**");
                    pw.print( " * @gwt.typeArgs ");
                    pw.println( attType.getTypeArgs() );
                    pw.println( " */");
                }
                pw.print( "public ");
                pw.print( typeString );
                pw.print( "get");
                pw.print( methodName );
                pw.println( "(){");
                pw.print("\t return this.");
                pw.print( att );
                pw.print(";");
                pw.println("}");
                
                if( attType.getTypeArgs() != null ){
                    pw.println("/**");
                    pw.print( " * @gwt.typeArgs newValue ");
                    pw.println( attType.getTypeArgs() );
                    pw.println( " */");
                }
                pw.print("public void set");
                pw.print( methodName );
                pw.print("( ");
                pw.print( typeString );
                pw.println( " newValue ){");
                if( propSupport ){
                    pw.print("\t");
                    pw.print( typeString );
                    pw.print("oldValue = this.");
                    pw.print( att );
                    pw.println( ";");
                }
                pw.print( "\tthis.");
                pw.print( att );
                pw.println( " = newValue;");
                
                if( propSupport ){
                    pw.print( "\tthis.");
                    pw.print( propSupportName );
                    pw.print(".firePropertyChange( \"");
                    pw.print( att );
                    pw.println( "\", oldValue, newValue );");
                }
                pw.println("}");
            }
        }// end atts loop
        pw.println("}");
        pw.flush();
        pw.close();
        //Write custom beans
        for( Iterator<String> attributes = bean.properties.keySet().iterator();
        attributes.hasNext(); ){
            String att = attributes.next();
            Bean attType = bean.properties.get( att );
            if( attType.isCustom() ){
                writeBean( packageName, packageDirectory, getSet, propSupport, attType );
            }
            for(Bean b : attType.getParameterTypes() ){
                if( b.isCustom() ){
                    writeBean( packageName, packageDirectory, getSet, propSupport, b );
                }
            }
        }
        
        
    }
    
    
    private static String resolveType( Class clazz ){
        String type = null;
        if( clazz == Integer.TYPE ){
            type = "int";
        } else if( clazz == Long.TYPE ){
            type = "long";
        } else if( clazz == Float.TYPE ){
            type = "float";
        } else if( clazz == Double.TYPE ){
            type = "double";
        } else if( clazz == Boolean.TYPE ){
            type = "boolean";
        } else if( clazz == Character.TYPE ){
            type = "char";
        } else if( clazz == Byte.TYPE ){
            type = "byte";
        } else {
            type = clazz.getName();
        }
        return type;
    }
}
