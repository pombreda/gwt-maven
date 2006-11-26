/*
 * PropertyChangeSupport.java
 *
 * Created on November 26, 2006, 9:32 AM
 *
 */

package com.totsp.gwt.beans.client;


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
