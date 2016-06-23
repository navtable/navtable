package es.udc.cartolab.gvsig.navtable.listeners;

import java.util.EventObject;

@SuppressWarnings("serial")
public class PositionEvent extends EventObject {

    private long previousPosition;
    private long nextPosition;

    public PositionEvent(Object source, long previousPos, long nextPos) {
	super(source);
	this.previousPosition = previousPos;
	this.nextPosition = nextPos;
    }

    public long getPreviousPosition() {
	return previousPosition;
    }
    
    public long getNextPosition() {
	return nextPosition;
    }
}
