package es.udc.cartolab.gvsig.navtable.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;

import es.udc.cartolab.gvsig.navtable.refactoring.INavigationTable;

public class GoToNextRecordAction extends AbstractAction {

    INavigationTable nt;

    public GoToNextRecordAction(INavigationTable nt) {
	super("Go next");
	putValue(SHORT_DESCRIPTION, "Goes to the next record in recordset");
	// TODO: putValue(MNEMONIC_KEY, mnemonic);
	this.nt = nt;
    }

    public void setIcon(ImageIcon icon) {
	putValue(Action.SMALL_ICON, icon);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
	if (nt.getCurrentPosition() < nt.getIndexOfLastRecord()) {
	    nt.setCurrentPosition(nt.getCurrentPosition() + 1);
	}
    }

}