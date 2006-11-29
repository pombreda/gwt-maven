/*
 * BeanMapping.java
 *
 */

package com.totsp.gwt.beans.server;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;

/**
 *
 * @author cooper
 */
public class BeanMapping {
    
    
    static final Class[] BASE_TYPES = {
        java.lang.Integer.class,
        java.lang.Long.class,
        java.lang.Byte.class,
        java.lang.Character.class,
        java.lang.Boolean.class,
        java.lang.Double.class,
        java.lang.Float.class,
        java.lang.String.class,
        java.lang.Number.class,
        java.lang.CharSequence.class
    };
    
    private static boolean arrayContains( Object[] array, Object find ){
        for( Object match : array ){
            if( match == find || match.equals( find ) ){
                return true;
            }
        }
        return false;
    }
    
    /** Creates a new instance of BeanMapping */
    private BeanMapping() {
        super();
    }
    
    private static Class resolveArray(Properties mappings, Object bean) throws ClassNotFoundException {
        int arrayDepth = 0;
        Class clazz = bean.getClass().getComponentType();
        while( clazz.isArray() ){
            clazz = clazz.getComponentType();
            arrayDepth++;
        }
        if( !clazz.isPrimitive() && !arrayContains( BASE_TYPES, clazz) ){
            clazz = resolveClass( mappings, clazz );
            if( clazz == null){
                return null;
            }
        }
        Object array = null;
        for( int i=0; i < arrayDepth; i++ ){
            array = Array.newInstance( clazz, 0 );
            clazz = array.getClass();
        }
        return clazz;
    }
    
    
    public static Object convert( Properties mappings, Object bean )
    throws IntrospectionException, ClassNotFoundException,
            InstantiationException, IllegalAccessException,
            InvocationTargetException, MappingException{
        
        if( bean.getClass().isArray() ){
            Object[] beans = (Object[]) bean;
            Class arrayClass = resolveArray( mappings, bean);
            Object[] destination = (Object[]) Array.newInstance( arrayClass, beans.length );
            for( int i=0; i < beans.length; i++){
                destination[i] = convert( mappings, beans[i] );
            }
            return destination;
           
        }
        if( bean.getClass().isPrimitive() ||
                arrayContains( BASE_TYPES, bean.getClass() ) ){
            return bean;
        }
        if( bean.getClass() == Date.class ){
            return new Date( ((Date) bean).getTime() );
        }
        
        Class destinationClass = resolveClass( mappings, bean.getClass() );
        if( destinationClass == null ){
            throw new MappingException("Unable to resolve class" + bean.getClass().getName() );
        }
        Object dest = destinationClass.newInstance();
        HashMap<String, Object> sourceProperties = inspectObject( bean );
        HashMap<String, Object> destinationProperties = inspectObject( dest );
        
        for(Iterator<String> it = sourceProperties.keySet().iterator(); it.hasNext(); ){
            String propertyName = it.next();
            System.out.println( "\tproperty: "+propertyName);
            
            if( !destinationProperties.containsKey( propertyName ) ){
                System.out.println("\t Not found on destination class "+ destinationClass.getName() );
                continue;
            }
            Object sourceAccessor = sourceProperties.get( propertyName );
            Object destinationAccessor = destinationProperties.get(propertyName);
            Class valueClass = null;
            Object valueObject = null;
            if( sourceAccessor instanceof Field ){
                Field f = (Field) sourceAccessor;
                valueClass = f.getType();
                valueObject = f.get( bean );
            } else {
                PropertyDescriptor pd = (PropertyDescriptor) sourceAccessor;
                valueClass = pd.getPropertyType();
                valueObject = pd.getReadMethod().invoke( bean );
            }
            
            Class valueDestinationClass = null;
            if( destinationAccessor instanceof Field ){
                Field f = (Field) destinationAccessor;
                valueDestinationClass = f.getType();
                if( valueClass == valueDestinationClass ){
                    f.set(dest, valueObject );
                } else if( valueDestinationClass == resolveClass( mappings, valueClass) ||
                        (valueDestinationClass.isArray() && valueClass.isArray() )){
                    f.set( dest, convert( mappings, valueObject) );
                } else {
                    continue;
                }
            } else{
                PropertyDescriptor pd = (PropertyDescriptor) destinationAccessor;
                valueDestinationClass = pd.getPropertyType();
                if( valueClass == valueDestinationClass ){
                    pd.getWriteMethod().invoke( dest, valueObject ) ;
                } else if( valueDestinationClass == resolveClass( mappings, valueClass)
                || (valueDestinationClass.isArray() && valueClass.isArray() )){
                    pd.getWriteMethod().invoke( dest, convert( mappings, valueObject ) );
                } else{
                    continue;
                }
            }
        }
        return dest;
        
    }
    
    
    private static HashMap<String, Object> inspectObject( Object o ) throws IntrospectionException {
        PropertyDescriptor[] pds = Introspector
                .getBeanInfo( o.getClass())
                .getPropertyDescriptors();
        HashMap<String, Object> values = new HashMap<String, Object>();
        for( PropertyDescriptor pd : pds ){
            if( pd.getName().equals("class")){
                continue;
            }
            values.put( pd.getName(), pd );
        }
        for( Field field : o.getClass().getFields() ){
            if(     (field.getModifiers() & Modifier.PUBLIC) != 0 &&
                    (field.getModifiers() & Modifier.FINAL ) == 0 &&
                    (field.getModifiers() & Modifier.STATIC) == 0 &&
                    values.get( field.getName() ) == null ){
                values.put( field.getName(), field );
            }
        }
        return values;
    }
    
    private static Class resolveClass( Properties mappings, Class clazz ) throws ClassNotFoundException {
        assert( mappings != null && mappings.size() > 0);
        assert( clazz != null );
        System.out.println( "Resolving class:"+ clazz.getName() + "::" + trimPackage( clazz.getName() ));
        
        if( mappings.containsKey( clazz.getName() ) ){
            return Class.forName( mappings.getProperty(clazz.getName() )) ;
        } else if( mappings.containsValue( clazz.getName() ) ){
            for( Iterator<Entry<Object, Object>> it = mappings.entrySet().iterator(); it.hasNext() ; ){
                Entry entry = it.next();
                if( entry.getValue().equals( clazz.getName() ) ){
                    return Class.forName( entry.getValue().toString() );
                }
            }
        } else if( mappings.containsKey( trimPackage( clazz.getName() ) +".*") ) {
            return Class.forName(
                    trimPackage(
                    mappings.getProperty(
                        trimPackage( clazz.getName())+".*" )
                    ) +"."+clazz.getSimpleName() );
        } else if( mappings.containsValue( trimPackage( clazz.getName()) +".*") ){
            for( Iterator<Entry<Object, Object>> it = mappings.entrySet().iterator(); it.hasNext() ; ){
                Entry entry = it.next();
                if( entry.getValue().equals(trimPackage( clazz.getName()) +".*" ) ){
                    return Class.forName(
                            trimPackage(
                            entry.getValue().toString()
                            ) +"."+clazz.getSimpleName() );
                }
            }
        }
        return null;
    }
    
    private static String trimPackage(String packageString){
        return packageString.substring( 0, packageString.lastIndexOf("."));
    }
    
    
}
