package es.udc.cartolab.gvsig.navtable.refactoring;

import javax.swing.event.EventListenerList;

public class CurrentPositionEventSource {

    private EventListenerList listenerList = new EventListenerList();

    public synchronized void addEventListener(CurrentPositionListener listener) {
	listenerList.add(CurrentPositionListener.class, listener);
    }

    public synchronized void removeEventListener(
	    CurrentPositionListener listener) {
	listenerList.remove(CurrentPositionListener.class, listener);
    }

    void fireEvent(CurrentPositionEvent evt) {
	Object[] listeners = listenerList.getListenerList();
	for (int i = 0; i < listeners.length; i += 2) {
	    if (listeners[i] == CurrentPositionListener.class) {
		((CurrentPositionListener) listeners[i + 1]).onChange(evt);
	    }
	}
    }

}
