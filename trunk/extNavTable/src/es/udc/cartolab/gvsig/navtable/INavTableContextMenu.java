package es.udc.cartolab.gvsig.navtable;

import javax.swing.JMenuItem;

/**
 *
 * @author Francisco Puga <fpuga@cartolab.es>
 *
 */
public interface INavTableContextMenu {

    String getName();

    JMenuItem[] getMenuItems();

    boolean isVisible();

    void setNavtableInstance(NavTable navtable);
}
