/*
 * IndexedPropertyChangeEvent.java
 *
 * Created on November 26, 2006, 9:47 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.totsp.gwt.beans.client;

/**
 *
 * @author cooper
 */
public class IndexedPropertyChangeEvent extends PropertyChangeEvent{
    
    private int index;
    /** Creates a new instance of IndexedPropertyChangeEvent */
    public IndexedPropertyChangeEvent(Object source, String propertyName, 
            Object oldValue, Object newValue, int index) {
        super(source, propertyName, oldValue, newValue);
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
    
}
