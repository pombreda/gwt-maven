/*
 * Bean.java
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
    Class clazz;
    private InnerParameterizedType ipt;
    HashMap<String, Bean> properties = new HashMap<String, Bean>();
    private ArrayList<Bean> parameterTypes = new ArrayList<Bean>();
    private static HashMap<Type, Bean> ALL_TYPES = new HashMap<Type, Bean>();
    private int arrayDepth;
    
    /** Creates a new instance of Bean */
    public Bean(Type type) throws IntrospectionException {
        super();
        System.out.println("Checking type "+type.toString());
       
        while( type instanceof GenericArrayType ){
            arrayDepth++;
            type = ((GenericArrayType) type).getGenericComponentType();
        }
        if( type instanceof Class ){
            this.clazz = (Class) type;
            while( clazz.isArray()  ){             //You have to check here
                                                   // to get primitive types. 
                                                   // I don't know why.
                arrayDepth++;
                clazz = clazz.getComponentType();
            }
        } else if (type instanceof ParameterizedType ){
            this.ipt = new InnerParameterizedType( (ParameterizedType) type );
            this.clazz = ipt.clazz;
            for( Class param: ipt.types){
                this.parameterTypes.add( new Bean( param ) );
            }
        }
        if( ALL_TYPES.get( type ) != null ){
            System.out.println("---Cyclic reference? "+ clazz.getName() );
        } else {
            ALL_TYPES.put( type, this );
        }
        if( !arrayContains( BASE_TYPES, this.clazz) ){
            
            PropertyDescriptor[] pds = Introspector
                    .getBeanInfo( this.clazz )
                    .getPropertyDescriptors();
            for( PropertyDescriptor pd: pds ){
                String propertyName = pd.getName();
                if( propertyName.equals( "class") || pd.getReadMethod() == null ){
                    continue;
                }
                Type returnType = pd.getReadMethod().getGenericReturnType();
                if( ALL_TYPES.containsKey( returnType ) ){
                    properties.put( propertyName, ALL_TYPES.get( returnType ) );
                } else {
                    properties.put( propertyName, new Bean( returnType ));
                }
            }
        }
        
    }
    
    public Class getType(){
        return this.clazz;
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
        
        InnerParameterizedType( ParameterizedType pt ){
            if( !( pt.getRawType() instanceof Class)) {
                throw new RuntimeException( pt.toString() +" does not have a raw type of class.");
            }
            this.clazz = (Class) pt.getRawType();
            if( !arrayContains( COLLECTION_TYPES, this.clazz ) ){
                throw new RuntimeException( pt.toString() +" is a parameterized type"+
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
                                throw new RuntimeException( pt.toString()+" has an upper bound that is not a class.");
                            }
                        }
                    } else if( wt.getLowerBounds().length > 0){
                        if( wt.getLowerBounds().length > 1){
                            throw new RuntimeException( pt.toString() + " has multiple lower bounds.");
                        } else {
                            if( wt.getLowerBounds()[0] instanceof Class){
                                types.add( (Class) wt.getLowerBounds()[0]);
                            } else {
                                throw new RuntimeException( pt.toString()+" has an lower bound that is not a class.");
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
