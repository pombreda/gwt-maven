/*
 * BeanMappingTest.java
 * JUnit based test
 *
 * Created on November 29, 2006, 3:45 PM
 */

package com.totsp.gwt.beans.server;

import java.util.HashMap;
import junit.framework.*;
import java.util.Properties;
import java.util.Random;

/**
 *
 * @author cooper
 */
public class BeanMappingTest extends TestCase {
    
    public BeanMappingTest(String testName) {
        super(testName);
    }

    public void testConvert() throws Exception {
        System.out.println("convert");
        
        Properties mappings = new Properties();
        mappings.setProperty( "test.source.*", "test.destination.*");
        
        test.source.ABean source = new test.source.ABean();
        source.setStringProperty( "Foo");
        source.setIntProperty( -2 );
        String[] arr = { "Foo", "Bar", "Baz" };
        source.setArrayProperty(arr);
        Random r = new Random();
        test.source.BBean[][] bbeans = new test.source.BBean[3][5];
        for( int i=0; i < bbeans.length; i++ ){
            for( int j=0; j < bbeans[i].length; j++){
                bbeans[i][j] = new test.source.BBean();
                bbeans[i][j].setValue( ""+ r.nextLong() );
            }
        }
        
        HashMap<String,  test.source.BBean> map = new HashMap<String,  test.source.BBean>();
        map.put( "Foo", bbeans[0][0] );
        map.put( "Bar", bbeans[0][1] );
        source.setBeanMap( map );
        
        source.setBeanArray( bbeans );
        test.destination.ABean result = (test.destination.ABean) 
            BeanMapping.convert(mappings, source);
        
        assertEquals( "stringProperty", source.getStringProperty(), result.stringProperty);
        assertEquals( "intProperty", source.getIntProperty(), result.intProperty);
        this.assertEquals( "arrayProperty", source.getArrayProperty()[0], result.arrayProperty[0]);
        this.assertEquals( "arrayProperty", source.getArrayProperty()[1], result.arrayProperty[1]);
        this.assertEquals( "arrayProperty", source.getArrayProperty()[2], result.arrayProperty[2]);
        for( int i=0; i < bbeans.length; i++ ){
            for( int j=0; j < bbeans[i].length; j++){
                System.out.println(result.beanArray[i][j].getValue() );
                this.assertEquals("arrayOfBeans", bbeans[i][j].getValue(), 
                        result.beanArray[i][j].getValue());
            }
        }
        this.assertEquals("beanMap", map.get("Foo").getValue(), result.beanMap.get("Foo").getValue() );
        this.assertEquals("beanMap", map.get("Bar").getValue(), result.beanMap.get("Bar").getValue() );
    }
    
}
