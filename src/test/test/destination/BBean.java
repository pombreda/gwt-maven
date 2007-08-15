/*
 * BBean.java
 *
 * Created on November 29, 2006, 4:08 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package test.destination;

/**
 *
 * @author cooper
 */
public class BBean {
    
    private String value;
    private ABean parent;
    
    /** Creates a new instance of BBean */
    public BBean() {
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public ABean getParent() {
        return parent;
    }

    public void setParent(ABean parent) {
        this.parent = parent;
    }
    
}
