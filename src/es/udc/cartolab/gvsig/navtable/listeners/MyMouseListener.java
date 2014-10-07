package es.udc.cartolab.gvsig.navtable.listeners;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import com.iver.utiles.extensionPoints.ExtensionPoint;
import com.iver.utiles.extensionPoints.ExtensionPointsSingleton;

import es.udc.cartolab.gvsig.navtable.AbstractNavTable;
import es.udc.cartolab.gvsig.navtable.NavTable;
import es.udc.cartolab.gvsig.navtable.contextualmenu.INavTableContextMenu;

public class MyMouseListener implements MouseListener {

    private NavTable navtable;

    public MyMouseListener(NavTable navTable) {
	this.navtable = navTable;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
	/*
	 * TODO At the moment, filters do not work with automatic calculated
	 * fields "length" and "area". Besides, the filter panel only is
	 * activated if only 1 row is selected.
	 */
	if (e.getButton() == NavTable.BUTTON_RIGHT) {
	    if (e.getSource() != null) {

		JTable table = (JTable) e.getSource();
		// get the coordinates of the mouse click
		Point p = e.getPoint();

		// get the row index that contains that coordinate
		int rowNumber = table.rowAtPoint(p);

		if (rowNumber > -1) {
		    // Get the ListSelectionModel of the JTable
		    ListSelectionModel model = table.getSelectionModel();

		    // set the selected interval of rows. Using the "rowNumber"
		    // variable for the beginning and end selects only that one
		    // row.
		    model.setSelectionInterval(rowNumber, rowNumber);
		}

		JPopupMenu popup = new JPopupMenu();

		ExtensionPoint extensionPoint = (ExtensionPoint) ExtensionPointsSingleton
			.getInstance().get(
				AbstractNavTable.NAVTABLE_CONTEXT_MENU);
		Iterator<INavTableContextMenu> it = extensionPoint.values().iterator();
		while (it.hasNext()) {
		    INavTableContextMenu c = it.next();
		    c.setNavtableInstance(navtable);
		    if (c.isVisible()) {
			for (JMenuItem m : c.getMenuItems()) {
			    popup.add(m);
			}
		    }
		    if (it.hasNext()) {
			popup.add(new JSeparator());
		    }
		}
		if (popup.getComponents().length != 0) {
		    popup.show(table, e.getX(), e.getY());
		}

	    }
	}
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }
}
