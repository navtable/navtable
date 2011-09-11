package es.udc.cartolab.gvsig.navtable.refactoring;

import java.util.EventListener;

public interface CurrentPositionListener extends EventListener {

    public void onChange(CurrentPositionEvent e);

}
