package es.udc.cartolab.gvsig.navtable.listeners;

import java.util.EventListener;

public interface PositionListener extends EventListener {

    public void onPositionChange(PositionEvent e);

}
