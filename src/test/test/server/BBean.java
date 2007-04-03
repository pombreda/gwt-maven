/*
 * ABean.java
 *
 * Created on November 25, 2006, 9:26 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package test.server;

import java.util.List;

/**
 *
 * @author cooper
 */
public class BBean {
    
    private Float floatProperty;
    private boolean booleanProperty;
    private List<String>[] strings;
    
    /** Creates a new instance of ABean */
    public BBean() {
    }

    public Float getFloatProperty() {
        return floatProperty;
    }

    public void setFloatProperty(Float floatProperty) {
        this.floatProperty = floatProperty;
    }

    public boolean isBooleanProperty() {
        return booleanProperty;
    }

    public void setBooleanProperty(boolean booleanProperty) {
        this.booleanProperty = booleanProperty;
    }

    public List<String>[] getStrings() {
        return strings;
    }

    public void setStrings(List<String>[] strings) {
        this.strings = strings;
    }
    
    
    
    
    
}
