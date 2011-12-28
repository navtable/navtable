package es.udc.cartolab.gvsig.navtable.listeners;

import javax.swing.event.EventListenerList;

public class PositionEventSource {

    private EventListenerList listenerList = new EventListenerList();
    
    public synchronized void addEventListener(PositionListener listener) {
	listenerList.add(PositionListener.class, listener);
    }
    
    public synchronized void removeEventListener(PositionListener listener) {
	listenerList.remove(PositionListener.class, listener);
    }
    
    public void fireEvent(PositionEvent evt) {
	Object[] listeners = listenerList.getListenerList();
	for (int i=0; i<listeners.length; i+=2) {
	    if(listeners[i] == PositionListener.class) {
		((PositionListener) listeners[i+1]).onPositionChange(evt);
	    }
	}
    }

}
