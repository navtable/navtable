package es.udc.cartolab.gvsig.navtable.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;

import es.udc.cartolab.gvsig.navtable.refactoring.INavigationTable;

public class GoToFirstRecordAction extends AbstractAction {

    INavigationTable nt;

    public GoToFirstRecordAction(INavigationTable nt) {
	super("Go first");
	putValue(SHORT_DESCRIPTION, "Goes to the first record in recordset");
	// TODO: putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_LEFT));
	this.nt = nt;
    }

    public void setIcon(ImageIcon icon) {
	putValue(Action.SMALL_ICON, icon);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
	nt.setCurrentPosition(0);
    }

}