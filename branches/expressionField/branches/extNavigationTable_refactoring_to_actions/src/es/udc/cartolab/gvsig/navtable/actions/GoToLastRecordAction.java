package es.udc.cartolab.gvsig.navtable.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;

import es.udc.cartolab.gvsig.navtable.refactoring.INavigationTable;

public class GoToLastRecordAction extends AbstractAction {

    INavigationTable nt;

    public GoToLastRecordAction(INavigationTable nt) {
	super("Go last");
	putValue(SHORT_DESCRIPTION, "Go to the last record in recordset");
	// TODO: putValue(MNEMONIC_KEY, mnemonic);
	this.nt = nt;
    }

    public void setIcon(ImageIcon icon) {
	putValue(Action.SMALL_ICON, icon);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
	nt.setCurrentPosition(nt.getIndexOfLastRecord());
    }

}
