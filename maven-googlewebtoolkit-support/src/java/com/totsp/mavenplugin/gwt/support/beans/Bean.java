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
import java.lang.reflect.*;
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
    Bean  parent;
    private InnerParameterizedType ipt;
    HashMap<String, Bean> properties = new HashMap<String, Bean>();
    private ArrayList<Bean> parameterTypes = new ArrayList<Bean>();
    private static HashMap<Type, Bean> ALL_TYPES = new HashMap<Type, Bean>();
    private int arrayDepth;

    public static Bean loadBean(Type type) throws IntrospectionException {
      Bean  retVal = ALL_TYPES.get( type );
      if ( retVal == null)
        retVal = new Bean(type);
      return retVal;
    }

    /** Creates a new instance of Bean */
    public Bean(Type type) throws IntrospectionException {
        super();
        if( ALL_TYPES.get( type ) == null ){
            ALL_TYPES.put( type, this );
        }

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
                this.parameterTypes.add( Bean.loadBean(param) );
            }
        }


        if (clazz.getSuperclass() != null && !clazz.getSuperclass().equals(Object.class)) {
          parent = Bean.loadBean(clazz.getSuperclass());
        }
      
        if( !arrayContains( BASE_TYPES, this.clazz) ){
            
            PropertyDescriptor[] pds = Introspector
                    .getBeanInfo( this.clazz )
                    .getPropertyDescriptors();
            for( PropertyDescriptor pd: pds ){
                String propertyName = pd.getName();

                if( propertyName.equals( "class") || pd.getReadMethod() == null || !pd.getReadMethod().getDeclaringClass().equals(clazz)){
                    continue;
                }
                Type returnType = pd.getReadMethod().getGenericReturnType();
                if( ALL_TYPES.containsKey( returnType ) ){
                    properties.put( propertyName, ALL_TYPES.get( returnType ) );
                } else {
                    properties.put( propertyName, Bean.loadBean(returnType) );
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

    public Class[] getParameterizedTypes() {
      if( this.ipt == null ){
            return null;
        } else {
            return this.ipt.types;
        }
    }

    public boolean isCustom(){
        return !(arrayContains( BASE_TYPES, this.clazz) || arrayContains( COLLECTION_TYPES, this.clazz ) );
    }

    public boolean isBaseType() {
      return arrayContains( BASE_TYPES, this.clazz);
    }

    public boolean isCollectionType() {
      return arrayContains( COLLECTION_TYPES, this.clazz);
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

  static public String getCamelCase(String attName) {
    if (attName == null)
      return null;
    return Character.toUpperCase(attName.charAt(0)) + attName.substring(1, attName.length());
  }

  public String getGetPrefix() {
    if (this.clazz == Boolean.TYPE) {
      return "is";
    } else {
      return "get";
    }
  }

  public String getGeneratedGet(String object, String attr, boolean isGetSet) {
    if (isGetSet) {

      return object + "." + properties.get(attr).getGetPrefix() + getCamelCase(attr) + "()";
    } else {
      return object + "." + attr;
    }
  }

  public String getGeneratedSet(String object, String attr, String value, boolean isGetSet) {
    if (isGetSet) {
      return object + ".set" + getCamelCase(attr) + "(" + value + ")";
    } else {
      return object + "." + attr + "=" + value;
    }
  }

  enum GetSet {
    Get,
    Set
  }

  enum AccessorUsage {
    Accessor,
    Direct,
    None
  }

  protected AccessorUsage beanUseAccessors(GetSet getset, String attName) {

    AccessorUsage retVal = AccessorUsage.None;
    try {
      if (getset==GetSet.Get) {
        Bean  prop = properties.get(attName);
        String propName = prop.getGetPrefix() + getCamelCase(attName);
        Method accessor = clazz.getMethod(propName);
        if (Modifier.isPublic(accessor.getModifiers()))
          return AccessorUsage.Accessor;

      } else {

        String propName = "set" + getCamelCase(attName);
        Method[] methods = clazz.getMethods();
        for (Method curr : methods) {
          if (curr.getName().equals(propName) &&
              curr.getParameterTypes().length == 1 &&
              Modifier.isPublic(curr.getModifiers()))
            return AccessorUsage.Accessor;
        }
      }
    } catch (NoSuchMethodException e) {
      //Do nothing, we now check to see if direct access is possible
    }

    try {
      Field field = clazz.getField(attName);
      if (Modifier.isPublic(field.getModifiers())) {
        return AccessorUsage.Direct;
      }
    } catch (NoSuchFieldException e) {
      //Need to fall through and return that we cannot access this field
    }

    return AccessorUsage.None;
  }


  public static final String FUNNY_MSG = "/* Cannot Access Getter */";
  public static final String FUNNY_MSG2 = "/* Cannot Access Setter */";

  public String getOriginalGet(String object, String attr) {
    AccessorUsage accessor = beanUseAccessors(GetSet.Get, attr);

    switch(accessor) {
      case Accessor:
        Bean  prop = properties.get(attr);
        return object + "." + prop.getGetPrefix() + getCamelCase(attr) + "()";

      case Direct:
        return object + "." + attr;

      case None:
        try {
          Field field = clazz.getField(attr);
          Class cls = field.getClass();
          if (!field.getClass().isPrimitive())
            return FUNNY_MSG + "null";
          else {
            if (cls == Boolean.TYPE) {
              return FUNNY_MSG + "false";
            } else if (cls != Void.TYPE) {
              return FUNNY_MSG + "0";
            } else {
              //Don't know what to do if we have a field of type void.  Shouldn't be possible. . .
              assert(false);
              return FUNNY_MSG + "0";
            }
          }

        } catch (NoSuchFieldException e) {
          //If we cannot access such a field, things are bad.  Just guess.
        }
    }
    return "null";
  }

  public String getOriginalSet(String object, String attr, String value) {
    AccessorUsage accessor = beanUseAccessors(GetSet.Get, attr);

    switch(accessor) {
      case Accessor:
        return object + ".set" + getCamelCase(attr) + "(" + value + ")";

      case Direct:
        return object + "." + attr + "=" + value;
    }
    return FUNNY_MSG2;
  }
    
}
