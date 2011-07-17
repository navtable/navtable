package es.udc.cartolab.gvsig.navtable.refactoring;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;

import es.udc.cartolab.gvsig.navtable.actions.GoToFirstRecordAction;
import es.udc.cartolab.gvsig.navtable.actions.GoToLastRecordAction;
import es.udc.cartolab.gvsig.navtable.actions.GoToNextRecordAction;
import es.udc.cartolab.gvsig.navtable.actions.GoToPreviousRecordAction;

public class NavigationTable implements INavigationTable {

    public static final int GO_FIRST = 0;
    public static final int GO_LAST = 1;
    public static final int GO_NEXT = 2;
    public static final int GO_PREV = 3;

    private SelectableDataSource sds;
    private boolean onlyNavigateThroughSelection;
    private int currentPosition;
    private GoToFirstRecordAction goToFirstRecordAction;
    private GoToLastRecordAction goToLastRecordAction;
    private GoToNextRecordAction goToNextRecordAction;
    private GoToPreviousRecordAction goToPreviousRecordAction;
    private DefaultTableModel model;
    private TableRenderer tableRenderer;

    public NavigationTable(SelectableDataSource sds) {
	this.sds = sds;
	initTableModel();
	initActions();
	setStatusOfActions();
    }

    public void initActions() {
	goToFirstRecordAction = new GoToFirstRecordAction(this);
	goToPreviousRecordAction = new GoToPreviousRecordAction(this);
	goToNextRecordAction = new GoToNextRecordAction(this);
	goToLastRecordAction = new GoToLastRecordAction(this);
    }

    public AbstractAction getAction(int actionName) {
	switch (actionName) {
	case GO_FIRST:
	    return goToFirstRecordAction;
	case GO_LAST:
	    return goToLastRecordAction;
	case GO_NEXT:
	    return goToNextRecordAction;
	case GO_PREV:
	    return goToPreviousRecordAction;
	default:
	    return null;
	}
    }

    public void setActionIcon(int actionName, ImageIcon icon) {
	switch (actionName) {
	case GO_FIRST:
	    goToFirstRecordAction.setIcon(icon);
	    break;
	case GO_LAST:
	    goToLastRecordAction.setIcon(icon);
	    break;
	case GO_NEXT:
	    goToNextRecordAction.setIcon(icon);
	    break;
	case GO_PREV:
	    goToPreviousRecordAction.setIcon(icon);
	    break;
	}
    }

    @Override
    public boolean isOnlyNavigateThroughSelection() {
	return onlyNavigateThroughSelection;
    }

    @Override
    public void setOnlyNavigateThroughSelected(boolean b) {
	onlyNavigateThroughSelection = b;
    }

    @Override
    public int getCurrentPosition() {
	return currentPosition;
    }

    public TableModel getTableModel() {
	return model;
    }

    private void initTableModel() {
	tableRenderer = new TableRenderer((INavigationTable) this);
	model = new DefaultTableModel();
	model.addColumn(PluginServices.getText(this, "headerTableAttribute"));
	model.addColumn(PluginServices.getText(this, "headerTableValue"));
	tableRenderer.updateModel(model, sds);
    }

    @Override
    public void setCurrentPosition(int index) {
	currentPosition = index;
	setStatusOfActions();
	tableRenderer.updateModel(model, sds);
    }

    private void setStatusOfActions() {
	goToFirstRecordAction.setEnabled(true);
	goToLastRecordAction.setEnabled(true);
	goToNextRecordAction.setEnabled(true);
	goToPreviousRecordAction.setEnabled(true);
	if (currentPosition == 0) {
	    goToFirstRecordAction.setEnabled(false);
	    goToPreviousRecordAction.setEnabled(false);
	}
	if (currentPosition == getIndexOfLastRecord()) {
	    goToLastRecordAction.setEnabled(false);
	    goToNextRecordAction.setEnabled(false);
	}
    }

    public int getIndexOfLastRecord() {
	try {
	    return (int) (sds.getRowCount() - 1);
	} catch (ReadDriverException e) {
	    e.printStackTrace();
	    return -1;
	}
    }

    @Override
    public SelectableDataSource getSelectableDataSource() {
	return sds;
    }

}
