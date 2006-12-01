/*
 * PropertyChangeSupport.java
 *
 *  Copyright (C) 2006  Robert "kebernet" Cooper <cooper@screaming-penguin.com>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */

package com.totsp.gwt.emul.java.beans;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 *
 * @author cooper
 */
public class PropertyChangeSupport  {
    private ArrayList allProperties;
    private HashMap listenerArrayListMap = new HashMap();
    private Object instance;
    /** Creates a new instance of PropertyChangeSupport */
    public PropertyChangeSupport(Object instance) {
        super();
        this.instance = instance;
    }

    public PropertyChangeListener[] getPropertyChangeListeners(String string) {
        ArrayList listeners = (ArrayList) this.listenerArrayListMap.get( string );
        if (listeners == null){
            return new PropertyChangeListener[0];
        } else {
            return (PropertyChangeListener[])
                listeners.toArray( new PropertyChangeListener[ listeners.size() ] );
        }
    }

    public boolean hasListeners(String string) {
        ArrayList listeners = (ArrayList) this.listenerArrayListMap.get( string );
        return listeners != null && listeners.size() > 0;
    }

    public void firePropertyChange(PropertyChangeEvent propertyChangeEvent) {
        if( propertyChangeEvent.getOldValue() == propertyChangeEvent.getNewValue() 
                 ||
                (propertyChangeEvent.getOldValue() != null &&
                 propertyChangeEvent.getOldValue().equals( propertyChangeEvent.getNewValue()))
                 ||
                (propertyChangeEvent.getNewValue() !=null && 
                        propertyChangeEvent.getNewValue().equals(propertyChangeEvent.getOldValue() ))
         ) return;  // don't fire unchanged events.
        ArrayList listeners = (ArrayList) this.listenerArrayListMap.get( 
                propertyChangeEvent.getPropertyName() 
                );
        if( listeners != null && listeners.size() ==0 ){
            for( Iterator it = listeners.iterator(); it.hasNext() ; ){
                PropertyChangeListener l = (PropertyChangeListener) it.next();
                l.propertyChange( propertyChangeEvent );
            }
        }
        if( this.allProperties != null && this.allProperties.size() > 0){
            for( Iterator it = this.allProperties.iterator(); it.hasNext(); ){
                PropertyChangeListener l = (PropertyChangeListener) it.next();
                l.propertyChange( propertyChangeEvent );
            }
        }
        
    }

    public void fireIndexedPropertyChange(String propertyName, 
            int index, boolean oldValue, boolean newValue) {
        this.firePropertyChange(
                new IndexedPropertyChangeEvent( this.instance,
                    propertyName,  new Boolean(oldValue), new Boolean( newValue ), index ) 
                );
    }

    public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
        this.firePropertyChange( 
                new PropertyChangeEvent( this.instance, propertyName,
                     Boolean.valueOf( oldValue ), Boolean.valueOf(newValue ) )
                );
    }

    public void fireIndexedPropertyChange(String propertyName, 
            int index, Object oldValue, Object newValue) {
        this.firePropertyChange(
                new IndexedPropertyChangeEvent( this.instance,
                    propertyName,  oldValue, newValue , index ) 
                );
    }

    public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        this.firePropertyChange(
                new PropertyChangeEvent( this.instance,
                    propertyName,  oldValue, newValue  ) 
                );
    }

    public void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        this.allProperties = this.allProperties == null ? new ArrayList() : this.allProperties;
        allProperties.add( propertyChangeListener );
    }

    public void removePropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        this.allProperties.remove( propertyChangeListener);
        ArrayList listeners = null;
        for( Iterator it = this.listenerArrayListMap.entrySet().iterator(); it.hasNext();  ){
            listeners = (ArrayList) it.next();
            if( listeners != null && listeners.size() > 0){
                listeners.remove( propertyChangeListener);
            }
        }
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener propertyChangeListener) {
        ArrayList listeners = (ArrayList) this.listenerArrayListMap.get( propertyName );
        if( listeners != null && listeners.size() > 0){
            listeners.remove( propertyChangeListener );
        }
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener propertyChangeListener) {
        ArrayList listeners = (ArrayList) this.listenerArrayListMap.get( propertyName );
        if( listeners == null ){
            listeners = new ArrayList();
            this.listenerArrayListMap.put( propertyName, listeners );
        }
        listeners.add( propertyChangeListener );
    }

    public void fireIndexedPropertyChange(String propertyName, 
            int index, int oldValue, int newValue) {
        this.firePropertyChange( 
                new IndexedPropertyChangeEvent( 
                    this.instance, propertyName,
                    Integer.valueOf( oldValue ), Integer.valueOf( newValue ), index )
                );
    }

    public void firePropertyChange(String propertyName, int oldValue, int newValue) {
        this.firePropertyChange( 
                new PropertyChangeEvent( 
                    this.instance, propertyName,
                     Integer.valueOf( oldValue ) , Integer.valueOf( newValue ) )
                );
    }

    public PropertyChangeListener[] getPropertyChangeListeners() {
        ArrayList allListeners = new ArrayList();
        allListeners.addAll( this.allProperties );
        for( Iterator it = this.listenerArrayListMap.entrySet().iterator(); it.hasNext(); ){
            allListeners.addAll( (ArrayList) it.next() );
        }
        return (PropertyChangeListener[])
            allListeners.toArray( new PropertyChangeListener[ allListeners.size()]);
    }
    
}
