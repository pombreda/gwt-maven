/*
 * ABean.java
 *
 * Created on November 25, 2006, 9:26 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package test.server;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;

/**
 *
 * @author cooper
 */
public class ABean {
    
    private List<String> strings;
    private HashMap<String, ? extends Number> hashOfNumbers;
    private String stringProperty;
    private int intProperty;
    private List<BBean> bbeans;
    
    /** Creates a new instance of ABean */
    public ABean() {
    }
    
    public List<String> getStrings() {
        return strings;
    }
    
    public void setStrings(List<String> strings) {
        this.strings = strings;
    }

    public HashMap<String, ? extends Number> getHashOfNumbers() {
        return hashOfNumbers;
    }

    public void setHashOfNumbers(HashMap<String, ? extends Number> hashOfNumbers) {
        this.hashOfNumbers = hashOfNumbers;
    }

    public String getStringProperty() {
        return stringProperty;
    }

    public void setStringProperty(String stringProperty) {
        this.stringProperty = stringProperty;
    }

    public int getIntProperty() {
        return intProperty;
    }

    public void setIntProperty(int intProperty) {
        this.intProperty = intProperty;
    }

    public List<BBean> getBbeans() {
        return bbeans;
    }

    public void setBbeans(List<BBean> bbeans) {
        this.bbeans = bbeans;
    }


  public boolean equals(Object object) {
    ABean t = (ABean) object;

    EqualsBuilder eb = new EqualsBuilder();

    eb.append(strings, t.strings)
        .append(hashOfNumbers, t.hashOfNumbers)
        .append(stringProperty, t.stringProperty)
        .append(intProperty, t.intProperty)
        .append(bbeans, t.bbeans);

    return eb.isEquals();
  }
}
