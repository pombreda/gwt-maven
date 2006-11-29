/*
 * Bean.java
 *
 * Created on November 26, 2006, 5:20 AM
 */

package com.totsp.mavenplugin.gwt.support.beans;

import com.totsp.gwt.beans.server.GwtOmit;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author cooper
 */
public class Bean {
    static final Class[] BASE_TYPES = {
        java.lang.Integer.class, java.lang.Integer.TYPE,
        java.lang.Long.class, java.lang.Long.TYPE,
        java.lang.Byte.class, java.lang.Byte.TYPE,
        java.lang.Character.class, java.lang.Character.TYPE,
        java.lang.Boolean.class, java.lang.Boolean.TYPE,
        java.lang.Double.class, java.lang.Double.TYPE,
        java.lang.Float.class, java.lang.Float.TYPE,
        java.lang.String.class, java.util.Date.class,
        java.lang.Number.class, java.lang.CharSequence.class
                
    };
    static final Class[] COLLECTION_TYPES = {
        java.util.Map.class, java.util.Collection.class,
        java.util.List.class, java.util.Set.class,
        java.util.ArrayList.class, java.util.Vector.class,
        java.util.HashMap.class, java.util.HashSet.class,
        java.util.Stack.class, java.util.Iterator.class
    };
    String name;
    Class clazz;
    private InnerParameterizedType ipt;
    HashMap<String, Bean> properties = new HashMap<String, Bean>();
    private ArrayList<Bean> parameterTypes = new ArrayList<Bean>();
    private int arrayDepth;
    
    /** Creates a new instance of Bean */
    public Bean(String name, Type type) throws IntrospectionException {
        super();
        
        this.name = name;
        while( type instanceof GenericArrayType ){
            arrayDepth++;
            type = ((GenericArrayType) type).getGenericComponentType();
        }
        if( type instanceof Class ){
            this.clazz = (Class) type;
        } else if (type instanceof ParameterizedType ){
            this.ipt = new InnerParameterizedType( name, (ParameterizedType) type );
            this.clazz = ipt.clazz;
            for( Class param: ipt.types){
                System.out.println("adding param type"+param.getName() );
                this.parameterTypes.add( new Bean( name, param ) );
            }
        }
        if( !arrayContains( BASE_TYPES, this.clazz) ){
            
            PropertyDescriptor[] pds = Introspector
                    .getBeanInfo( this.clazz )
                    .getPropertyDescriptors();
            for( PropertyDescriptor pd: pds ){
                if( pd.getReadMethod().getAnnotation( GwtOmit.class ) != null){
                    continue;
                }
                String propertyName = pd.getName();
                if( propertyName.equals( "class") ){
                    continue;
                }
                Type returnType = pd.getReadMethod().getGenericReturnType();
                properties.put( propertyName, new Bean( propertyName, returnType ));
            }
        }
        
    }
    
    public Class getType(){
        return this.clazz;
    }
    
    public String getName(){
        return this.name;
    }
    
    public String getTypeArgs(){
        if( this.ipt == null ){
            return null;
        } else {
            return this.ipt.toString();
        }
    }
    
    public boolean isCustom(){
        return !(arrayContains( BASE_TYPES, this.clazz) || arrayContains( COLLECTION_TYPES, this.clazz ) );
    }
    
    private static boolean arrayContains( Object[] array, Object find ){
        for( Object match : array ){
            if( match == find || match.equals( find ) ){
                return true;
            }
        }
        return false;
    }
    
    private static class InnerParameterizedType {
        Class clazz;
        Class[] types;
        
        InnerParameterizedType( String name, ParameterizedType pt ){
            if( !( pt.getRawType() instanceof Class)) {
                throw new RuntimeException( name +" does not have a raw type of class.");
            }
            this.clazz = (Class) pt.getRawType();
            if( !arrayContains( COLLECTION_TYPES, this.clazz ) ){
                throw new RuntimeException( name +" is a parameterized type"+
                        "(generic) that is not a standard collection:" + this.clazz);
            }
            ArrayList<Class> types = new ArrayList<Class>();
            for( Type type : pt.getActualTypeArguments() ){
                if( type instanceof WildcardType ){
                    WildcardType wt = (WildcardType) type;
                    if( wt.getUpperBounds().length > 0 ){
                        if( arrayContains( wt.getUpperBounds(), Object.class) ||
                                wt.getUpperBounds().length > 1 ){
                            types.add( com.google.gwt.user.client.rpc.IsSerializable.class );
                        } else {
                            if( wt.getUpperBounds()[0] instanceof Class){
                                types.add( (Class) wt.getUpperBounds()[0]);
                            } else {
                                throw new RuntimeException( name+" has an upper bound that is not a class.");
                            }
                        }
                    } else if( wt.getLowerBounds().length > 0){
                        if( wt.getLowerBounds().length > 1){
                            throw new RuntimeException( name + " has multiple lower bounds.");
                        } else {
                            if( wt.getLowerBounds()[0] instanceof Class){
                                types.add( (Class) wt.getLowerBounds()[0]);
                            } else {
                                throw new RuntimeException( name+" has an lower bound that is not a class.");
                            }
                        }
                        
                    }
                } else if( type instanceof Class ){
                    types.add( (Class) type );
                } else {
                    throw new RuntimeException(" Parameter types must be classes. No Nested Parameterized types.");
                }
            }
            this.types = types.toArray( new Class[types.size()]);
        }
        
        public String toString(){
            StringBuffer sb = new StringBuffer("<");
            for( int i=0; i < this.types.length; i++ ){
                sb.append( this.types[i].getName() );
                if( i + 1 < this.types.length ){
                    sb.append(", ");
                }
            }
            sb.append( ">");
            return sb.toString();
        }
    }

    public ArrayList<Bean> getParameterTypes() {
        return parameterTypes;
    }

    public int getArrayDepth() {
        return arrayDepth;
    }
    
}
