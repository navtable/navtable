package es.udc.cartolab.gvsig.navtable.listeners;

import java.util.EventListener;

public interface PositionListener extends EventListener {

    /**
     * Fired before the position is updated
     */
    public void beforePositionChange(PositionEvent e);
    
    /**
     * Fired after the position is updated
     */
    public void onPositionChange(PositionEvent e);

}
