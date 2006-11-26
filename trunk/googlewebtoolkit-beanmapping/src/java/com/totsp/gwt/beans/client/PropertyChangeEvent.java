/*
 * PropertyChangeEvent.java
 *
 * Created on November 26, 2006, 9:42 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.totsp.gwt.beans.client;

/**
 *
 * @author cooper
 */
public class PropertyChangeEvent extends java.util.EventObject {
    
    
    private String propertyName;
    private Object oldValue;
    private Object newValue;
    private Object propagationId;
    
    /** Creates a new instance of PropertyChangeEvent */
    public PropertyChangeEvent(Object source, String propertyName, Object oldValue, Object newValue) {
        super(source);
        this.source = source;
        if( propertyName == null ){
            throw new NullPointerException("property name cannot be null.");
        }
        this.propertyName = propertyName;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public Object getSource() {
        return super.getSource();
    }

    public String getPropertyName() {
        return propertyName;
    }

    public Object getOldValue() {
        return oldValue;
    }

    public Object getNewValue() {
        return newValue;
    }

    public Object getPropagationId() {
        return propagationId;
    }

    public void setPropagationId(Object propagationId) {
        this.propagationId = propagationId;
    }
    
    
}
