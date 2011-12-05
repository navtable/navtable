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

    private static final int NO_REGISTER = -1;
    public static final int GO_FIRST = 0;
    public static final int GO_LAST = 1;
    public static final int GO_NEXT = 2;
    public static final int GO_PREV = 3;

    private SelectableDataSource sds;
    private boolean onlyNavigateThroughSelection;
    private int currentPosition = NO_REGISTER;
    private GoToFirstRecordAction goToFirstRecordAction;
    private GoToLastRecordAction goToLastRecordAction;
    private GoToNextRecordAction goToNextRecordAction;
    private GoToPreviousRecordAction goToPreviousRecordAction;
    private DefaultTableModel model;
    private TableRenderer tableRenderer;

    private CurrentPositionEventSource currentPositionEventSource = new CurrentPositionEventSource();

    public NavigationTable(SelectableDataSource sds) {
	this.sds = sds;
	init();
    }

    private void init() {
	initActions();
	initTableModel();
	try {
	    if (sds.getRowCount() > 0) {
		setCurrentPosition(0);
	    }
	} catch (ReadDriverException e) {
	    e.printStackTrace();
	}
    }

    public void initActions() {
	goToFirstRecordAction = new GoToFirstRecordAction(this);
	goToPreviousRecordAction = new GoToPreviousRecordAction(this);
	goToNextRecordAction = new GoToNextRecordAction(this);
	goToLastRecordAction = new GoToLastRecordAction(this);
	setStatusOfActions();
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
	currentPositionEventSource.fireEvent(new CurrentPositionEvent(this));
    }

    private void setStatusOfActions() {
	goToFirstRecordAction.setEnabled(false);
	goToLastRecordAction.setEnabled(false);
	goToNextRecordAction.setEnabled(false);
	goToPreviousRecordAction.setEnabled(false);
	if (currentPosition > getIndexOfFirstRecord()) {
	    goToFirstRecordAction.setEnabled(true);
	    goToPreviousRecordAction.setEnabled(true);
	}
	if (currentPosition < getIndexOfLastRecord()) {
	    goToLastRecordAction.setEnabled(true);
	    goToNextRecordAction.setEnabled(true);
	}
    }

    public int getIndexOfFirstRecord() {
	return 0;
    }

    public int getIndexOfLastRecord() {
	try {
	    return (int) (sds.getRowCount() - 1);
	} catch (ReadDriverException e) {
	    e.printStackTrace();
	    return NO_REGISTER;
	}
    }

    @Override
    public SelectableDataSource getSelectableDataSource() {
	return sds;
    }

    public void addCurrentPositionListener(CurrentPositionListener listener) {
	currentPositionEventSource.addEventListener(listener);
    }

    public void removeCurrentPositionListener(CurrentPositionListener listener) {
	currentPositionEventSource.removeEventListener(listener);
    }
}
