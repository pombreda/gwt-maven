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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;


/**
 * This is a class that uses reflection to
 * @author cooper
 */
public class BeanMapping {
    static final Class[] BASE_TYPES = {
            java.lang.Integer.class, java.lang.Long.class, java.lang.Byte.class,
            java.lang.Character.class, java.lang.Boolean.class,
            java.lang.Double.class, java.lang.Float.class,
            java.lang.String.class, java.lang.Number.class,
            java.lang.CharSequence.class
        };

    private static final HashMap<Class, HashMap<String, Object>> inspections = new HashMap<Class, HashMap<String, Object>>();
        
    /** Creates a new instance of BeanMapping */
    private BeanMapping() {
        super();
    }

    
    
    public static Object convert(Properties mappings, Object bean)throws IntrospectionException, ClassNotFoundException, 
            InstantiationException, IllegalAccessException, 
            InvocationTargetException, MappingException {
        IdentityHashMap<Object, Object> instances = new IdentityHashMap<Object, Object>();
        return convertInternal( instances, mappings, bean );
    }
    
    private static Object convertInternal(IdentityHashMap<Object,Object> instances, Properties mappings, Object bean)
        throws IntrospectionException, ClassNotFoundException, 
            InstantiationException, IllegalAccessException, 
            InvocationTargetException, MappingException {
        
        // if null
        if( bean == null ){
            return null;
        }
        // if we have already seen this instance.
        if( instances.containsKey( bean ) ){
            return instances.get( bean );
        }
        
        // if this is an array, backstep the array and return it.
        if(bean.getClass().isArray()) {
            Object[] beans = (Object[]) bean;
            Class arrayClass = resolveArray(mappings, bean);
            Object[] destination = (Object[]) Array.newInstance(
                    arrayClass, beans.length);

            for(int i = 0; i < beans.length; i++) {
                destination[i] = convertInternal(instances, mappings, beans[i]);
            }

            return destination;
        }

        // if this is a primitve or a common type, just return it.
        if(
            bean.getClass().isPrimitive() ||
                arrayContains(BASE_TYPES, bean.getClass())) {
            return bean;
        }

        
        // if we have gotten here,
        // this is a class that requires resolution mapping.
        Class destinationClass = resolveClass(mappings, bean.getClass());

        if(destinationClass == null) {
            throw new MappingException(
                "Unable to resolve class" + bean.getClass().getName());
        }
        
        Object dest = destinationClass.newInstance();
        
        // store the instance so it is there when we recurse into the properties.
        instances.put( bean, dest );
        HashMap<String,Object> sourceProperties = inspectObject(bean);
        HashMap<String,Object> destinationProperties = inspectObject(dest);

        for(
            Iterator<String> it = sourceProperties.keySet().iterator();
                it.hasNext();) {
            String propertyName = it.next();

            if(!destinationProperties.containsKey(propertyName)) {
                continue;
            }

            Object sourceAccessor = sourceProperties.get(propertyName);
            Object destinationAccessor = destinationProperties.get(
                    propertyName);
            Class valueClass = null;
            Object valueObject = null;

            if(sourceAccessor instanceof Field) {
                Field f = (Field) sourceAccessor;
                valueClass = f.getType();
                valueObject = f.get(bean);
            } else {
                PropertyDescriptor pd = (PropertyDescriptor) sourceAccessor;
                valueClass = pd.getPropertyType();
                valueObject = pd.getReadMethod().invoke(bean);
            }

            Class valueDestinationClass = null;

            if(destinationAccessor instanceof Field) {
                Field f = (Field) destinationAccessor;
                valueDestinationClass = f.getType();

                if(
                    isInterface(Map.class, valueClass) &&
                        isInterface(Map.class, valueDestinationClass)) {
                    Map map = (Map) resolveMapType(
                            valueClass, valueDestinationClass).newInstance();
                    convertMap(instances, mappings, (Map) valueObject, map);
                    f.set(dest, map);
                } else if(
                    isInterface(List.class, valueClass) &&
                        isInterface(List.class, valueDestinationClass)) {
                    List list = (List) resolveListType(
                            valueClass, valueDestinationClass).newInstance();
                    convertCollection(instances, mappings, (List) valueObject, list);
                    f.set(dest, list);
                } else if(
                    isInterface(Collection.class, valueClass) &&
                        isInterface(Collection.class, valueDestinationClass)) {
                    Collection collection = (Collection) resolveCollecitonType(
                            valueClass, valueDestinationClass).newInstance();
                    convertCollection(instances,
                        mappings, (Collection) valueObject, collection);
                    f.set(dest, collection);
                } else if(valueClass == valueDestinationClass) {
                    f.set(dest, valueObject);
                } else if(
                    (valueDestinationClass == resolveClass(
                            mappings, valueClass)) ||
                        (valueDestinationClass.isArray() &&
                        valueClass.isArray())) {
                    f.set(dest, convertInternal(instances, mappings, valueObject));
                } else {
                    continue;
                }
            } else {
                PropertyDescriptor pd = (PropertyDescriptor) destinationAccessor;
                valueDestinationClass = pd.getPropertyType();

                if(
                    isInterface(Map.class, valueClass) &&
                        isInterface(Map.class, valueDestinationClass)) {
                    Map map = (Map) resolveMapType(
                            valueClass, valueDestinationClass).newInstance();
                    convertMap(instances,
                            mappings, (Map) valueObject, map);
                    pd.getWriteMethod().invoke(dest, map);
                } else if(
                    isInterface(List.class, valueClass) &&
                        isInterface(List.class, valueDestinationClass)) {
                    List list = (List) resolveListType(
                            valueClass, valueDestinationClass).newInstance();
                    convertCollection(instances,
                            mappings, (List) valueObject, list);
                    pd.getWriteMethod().invoke(dest, list);
                } else if(
                    isInterface(Collection.class, valueClass) &&
                        isInterface(Collection.class, valueDestinationClass)) {
                    Collection collection = (Collection) resolveCollecitonType(
                            valueClass, valueDestinationClass).newInstance();
                    convertCollection(instances, 
                        mappings, (Collection) valueObject, collection);
                    pd.getWriteMethod().invoke(dest, collection);
                } else if(valueClass == valueDestinationClass) { 
                    pd.getWriteMethod().invoke(dest, valueObject);
                } else if(
                    (valueDestinationClass == resolveClass(
                            mappings, valueClass)) ||
                        (valueDestinationClass.isArray() &&
                        valueClass.isArray())) {
                    pd.getWriteMethod().invoke(
                        dest, convertInternal(instances, mappings, valueObject));
                } else {
                    continue;
                }
            }
        }
        
        return dest;
    }

    private static boolean isInterface(Class interfaceClass, Class check) {
        return check.equals(interfaceClass) ||
        arrayContains(check.getInterfaces(), interfaceClass);
    }

    private static boolean arrayContains(Object[] array, Object find) {
        for(Object match : array) {
            if((match == find) || match.equals(find)) {
                return true;
            }
        }

        return false;
    }

    private static void convertCollection(IdentityHashMap<Object,Object> instances,
        Properties mappings, Collection source, Collection destination)
        throws IntrospectionException, ClassNotFoundException, 
            InstantiationException, IllegalAccessException, 
            InvocationTargetException, MappingException {
        for(Iterator it = source.iterator(); it.hasNext();) {
            Object o = it.next();

            if(!arrayContains(BASE_TYPES, o.getClass())) {
                o = convertInternal(instances, mappings, o);
            }

            destination.add(o);
        }
    }

    private static void convertMap(IdentityHashMap<Object,Object> instances,
        Properties mappings, Map source, Map destination)
        throws IntrospectionException, ClassNotFoundException, 
            InstantiationException, IllegalAccessException, 
            InvocationTargetException, MappingException {
        for(
            Iterator<Entry<Object,Object>> it = source.entrySet().iterator();
                it.hasNext();) {
            Entry<Object,Object> entry = it.next();
            Object key = entry.getKey();

            if(!arrayContains(BASE_TYPES, key.getClass())) {
                key = convertInternal(instances, mappings, key);
            }

            Object value = entry.getValue();

            if(!arrayContains(BASE_TYPES, value.getClass())) {
                value = convertInternal(instances, mappings, value);
            }

            destination.put(key, value);
        }
    }

    private static HashMap<String,Object> inspectObject(Object o)
        throws IntrospectionException {
        if( inspections.containsKey( o.getClass() ) ){
            return inspections.get( o.getClass() );
        }
        PropertyDescriptor[] pds = Introspector.getBeanInfo(o.getClass())
                                               .getPropertyDescriptors();
        HashMap<String,Object> values = new HashMap<String,Object>();

        for(PropertyDescriptor pd : pds) {
            if(pd.getName().equals("class")) {
                continue;
            }

            values.put(pd.getName(), pd);
        }

        for(Field field : o.getClass().getFields()) {
            if(
                ((field.getModifiers() & Modifier.PUBLIC) != 0) &&
                    ((field.getModifiers() & Modifier.FINAL) == 0) &&
                    ((field.getModifiers() & Modifier.STATIC) == 0) &&
                    (values.get(field.getName()) == null)) {
                values.put(field.getName(), field);
            }
        }
        inspections.put( o.getClass(), values);
        return values;
    }

    private static Class resolveArray(Properties mappings, Object bean)
        throws ClassNotFoundException {
        int arrayDepth = 0;
        Class clazz = bean.getClass().getComponentType();

        while(clazz.isArray()) {
            clazz = clazz.getComponentType();
            arrayDepth++;
        }

        if(!clazz.isPrimitive() && !arrayContains(BASE_TYPES, clazz)) {
            clazz = resolveClass(mappings, clazz);

            if(clazz == null) {
                return null;
            }
        }

        Object array = null;

        for(int i = 0; i < arrayDepth; i++) {
            array = Array.newInstance(clazz, 0);
            clazz = array.getClass();
        }

        return clazz;
    }

    private static Class resolveClass(Properties mappings, Class clazz)
        throws ClassNotFoundException {
        assert ((mappings != null) && (mappings.size() > 0));
        assert (clazz != null);

        if(mappings.containsKey(clazz.getName())) {
            return Class.forName(mappings.getProperty(clazz.getName()));
        } else if(mappings.containsValue(clazz.getName())) {
            for(
                Iterator<Entry<Object,Object>> it = mappings.entrySet()
                                                            .iterator();
                    it.hasNext();) {
                Entry entry = it.next();

                if(entry.getValue().equals(clazz.getName())) {
                    return Class.forName(entry.getValue().toString());
                }
            }
        } else if(mappings.containsKey(trimPackage(clazz.getName()) + ".*")) {
            return Class.forName(
                trimPackage(
                    mappings.getProperty(trimPackage(clazz.getName()) + ".*")) +
                "." + clazz.getSimpleName());
        } else if(mappings.containsValue(trimPackage(clazz.getName()) + ".*")) {
            for(
                Iterator<Entry<Object,Object>> it = mappings.entrySet()
                                                            .iterator();
                    it.hasNext();) {
                Entry entry = it.next();

                if(entry.getValue().equals(trimPackage(clazz.getName()) + ".*")) {
                    return Class.forName(
                        trimPackage(entry.getValue().toString()) + "." +
                        clazz.getSimpleName());
                }
            }
        }

        return null;
    }

    private static Class resolveCollecitonType(Class source, Class destination) {
        if(
            source.equals(Collection.class) &&
                destination.equals(Collection.class)) {
            return ArrayList.class;
        } else if(destination.equals(Collection.class)) {
            return source;
        } else {
            return destination;
        }
    }

    private static Class resolveListType(Class source, Class destination) {
        if(source.equals(List.class) && destination.equals(List.class)) {
            return ArrayList.class;
        } else if(destination.equals(List.class)) {
            return source;
        } else {
            return destination;
        }
    }

    private static Class resolveMapType(Class source, Class destination) {
        if(source.equals(Map.class) && destination.equals(Map.class)) {
            return HashMap.class;
        } else if(destination.equals(Map.class)) {
            return source;
        } else {
            return destination;
        }
    }

    private static Class resolveSetType(Class source, Class destination) {
        if(source.equals(Set.class) && destination.equals(Set.class)) {
            return HashSet.class;
        } else if(destination.equals(Set.class)) {
            return source;
        } else {
            return destination;
        }
    }

    private static String trimPackage(String packageString) {
        return packageString.substring(0, packageString.lastIndexOf("."));
    }
}
