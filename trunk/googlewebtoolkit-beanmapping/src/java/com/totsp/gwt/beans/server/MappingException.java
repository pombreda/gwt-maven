/*
 * MappingException.java
 *
 * Created on November 29, 2006, 1:45 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.totsp.gwt.beans.server;

/**
 *
 * @author cooper
 */
public class MappingException extends java.lang.Exception {
    
    /**
     * Creates a new instance of <code>MappingException</code> without detail message.
     */
    public MappingException() {
    }
    
    
    /**
     * Constructs an instance of <code>MappingException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public MappingException(String msg) {
        super(msg);
    }
}
