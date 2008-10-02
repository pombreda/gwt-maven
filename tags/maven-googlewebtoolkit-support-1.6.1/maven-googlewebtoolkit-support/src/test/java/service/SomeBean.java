/*
 * SomeBean.java
 *
 * Created on May 29, 2007, 1:32 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package service;

/**
 *
 * @author rcooper
 */
public class SomeBean {
    
    private String stringProperty;
    private int[] arrayOfInts;
    private boolean booleanProperty;
    
    /** Creates a new instance of SomeBean */
    public SomeBean() {
    }

    public String getStringProperty() {
        return stringProperty;
    }

    public void setStringProperty(String stringProperty) {
        this.stringProperty = stringProperty;
    }

    public int[] getArrayOfInts() {
        return arrayOfInts;
    }

    public void setArrayOfInts(int[] arrayOfInts) {
        this.arrayOfInts = arrayOfInts;
    }

    public boolean isBooleanProperty() {
        return booleanProperty;
    }

    public void setBooleanProperty(boolean booleanProperty) {
        this.booleanProperty = booleanProperty;
    }
    
}
