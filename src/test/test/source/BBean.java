/*
 * BBean.java
 *
 * Created on November 29, 2006, 4:09 PM
 *
 */

package test.source;

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