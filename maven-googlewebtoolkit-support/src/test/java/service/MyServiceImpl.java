/*
 * MyServiceImpl.java
 *
 * Created on May 29, 2007, 1:26 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package service;

/**
 *
 * @author rcooper
 */
public class MyServiceImpl implements MyService{
    
    /** Creates a new instance of MyServiceImpl */
    public MyServiceImpl() {
    }

    public int someService(SomeBean[] beans) throws MyException {
        return beans.length;
    }
    
}
