/*
 * MyException.java
 *
 * Created on May 29, 2007, 1:26 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package test.service;

import com.google.gwt.user.client.rpc.SerializableException;

/**
 *
 * @author rcooper
 */
public class MyException extends SerializableException{
    
    /** Creates a new instance of MyException */
    public MyException() {
        super("This is my message. There are many like it but this one is mine.");
    }
    
}
