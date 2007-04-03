/*
 * BeanGeneratorBase.java
 *
 * Created on February 17, 2007, 9:09 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.totsp.mavenplugin.gwt.support.beans;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 *
 * @author cooper
 */
public class BeanGeneratorBase {
    
    /** Creates a new instance of BeanGeneratorBase */
    protected BeanGeneratorBase() {
    }
    
    private static final HashMap TO_BIGS = new HashMap();
    static{
        TO_BIGS.put( "int ", "Integer");
        TO_BIGS.put( "double ", "Double");
        TO_BIGS.put( "float ", "Float");
        TO_BIGS.put( "long ", "Long");
        TO_BIGS.put( "byte ", "Byte");
        TO_BIGS.put( "char ", "Character");
        TO_BIGS.put( "boolean ", "Boolean" );
    }
    private static HashSet<String> done = new HashSet<String>();
    
    public static void writeBean( String packageName, File packageDirectory,
            boolean getSet, boolean propSupport, Bean bean) throws IOException {
        System.out.println( bean.getArrayDepth()+ " "+ bean.clazz );
        if( done.contains( bean.clazz.getName() ) ){
            return;
        }
        done.add( bean.clazz.getName() );
        String beanName = bean.clazz.getSimpleName();
        File javaFile = new File( packageDirectory, beanName+".java");
        for( int i=0; javaFile.exists() ; i++ ){
            javaFile = new File( packageDirectory, beanName+i+".java");
        }
        String name = javaFile.getName();
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
        pw.print( name.substring(0, name.indexOf(".") ) );
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
                    //System.out.println( typeString +" "+ TO_BIGS.containsKey( typeString));
                    if( TO_BIGS.containsKey( typeString) ){
                        pw.println("\", new "+TO_BIGS.get(typeString)+"(oldValue), new "+TO_BIGS.get(typeString)+"(newValue));");
                    }else {
                        pw.println( "\", oldValue, newValue );");
                    }
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
            if( attType.isCustom() && !done.contains( attType ) ){
                writeBean( packageName, packageDirectory, getSet, propSupport, attType );
            }
            for(Bean b : attType.getParameterTypes() ){
                if( b.isCustom()  && !done.contains( attType )){
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
