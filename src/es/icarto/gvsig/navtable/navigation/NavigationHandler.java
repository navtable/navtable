package es.icarto.gvsig.navtable.navigation;

import static es.udc.cartolab.gvsig.navtable.AbstractNavTable.EMPTY_REGISTER;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.RowSorter;
import javax.swing.RowSorter.SortKey;

import org.gvsig.andami.PluginServices;
import org.gvsig.fmap.dal.DataStoreNotification;
import org.gvsig.fmap.dal.exception.DataException;
import org.gvsig.fmap.mapcontext.layers.SelectionEvent;
import org.gvsig.fmap.mapcontext.layers.SelectionListener;
import org.gvsig.tools.observer.Observable;
import org.gvsig.tools.observer.Observer;
import org.gvsig.utils.extensionPointsOld.ExtensionPoints;
import org.gvsig.utils.extensionPointsOld.ExtensionPointsSingleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.icarto.gvsig.commons.gvsig2.FBitSet;
import es.icarto.gvsig.commons.gvsig2.SelectableDataSource;
import es.udc.cartolab.gvsig.navtable.AbstractNavTable;
import es.udc.cartolab.gvsig.navtable.listeners.PositionEvent;
import es.udc.cartolab.gvsig.navtable.listeners.PositionEventSource;
import es.udc.cartolab.gvsig.navtable.listeners.PositionListener;

public class NavigationHandler implements ActionListener, Observer {

private static final Logger logger = LoggerFactory
		.getLogger(NavigationHandler.class);
    
    private final PositionEventSource positionEventSource = new PositionEventSource();

    private JButton firstB = null;
    private JButton beforeB = null;
    private JTextField posTF = null;
    private JLabel totalLabel = null;
    private JButton nextB = null;
    private JButton lastB = null;
    private JPanel navToolBar;

    // Selection widgets
    private JCheckBox onlySelectedCB = null;
    private JCheckBox alwaysSelectCB = null;
    private JButton selectionB = null;
    private JPanel optionsPanel;

    private long currentPosition = 0;

    private final AbstractNavTable nt;

    private RowSorter<? extends SelectableDataSource> sorter;

    public NavigationHandler(AbstractNavTable nt) {
	this.nt = nt;
	initWidgets();
	sorter = new NTRowSorter<SelectableDataSource>(nt.getRecordset());
    }

    public JPanel getToolBar() {
	return navToolBar;
    }

    private void initWidgets() {
	initNavigationWidgets();
	initSelectionWidgets();
    }

    private void initNavigationWidgets() {
	registerNavTableButtonsOnNavigationToolBarExtensionPoint();
	navToolBar = new JPanel(new FlowLayout());
	navToolBar.add(firstB);
	navToolBar.add(beforeB);
	navToolBar.add(posTF);
	navToolBar.add(totalLabel);
	navToolBar.add(nextB);
	navToolBar.add(lastB);
    }

    private void initSelectionWidgets() {
	onlySelectedCB = getNavTableCheckBox(onlySelectedCB, "selectedCheckBox");
	alwaysSelectCB = getNavTableCheckBox(alwaysSelectCB, "selectCheckBox");
	optionsPanel = new JPanel(new FlowLayout());
	optionsPanel.add(onlySelectedCB);
	optionsPanel.add(alwaysSelectCB);
    }

    private void registerNavTableButtonsOnNavigationToolBarExtensionPoint() {
	firstB = getNavTableButton(firstB, "/go-first.png",
		"goFirstButtonTooltip");
	beforeB = getNavTableButton(beforeB, "/go-previous.png",
		"goPreviousButtonTooltip");
	posTF = new JTextField(5);
	posTF.addActionListener(this);
	totalLabel = new JLabel();
	nextB = getNavTableButton(nextB, "/go-next.png", "goNextButtonTooltip");
	lastB = getNavTableButton(lastB, "/go-last.png", "goLastButtonTooltip");
    }

    public void next() {
	if (isOnlySelected()) {
	    nextSelected();
	} else {
	    // setPosition(getPosition() + 1);
	    int viewPos = sorter.convertRowIndexToView((int) getPosition());
	    int viewLastPos = sorter.getViewRowCount() - 1;
	    if (viewPos < viewLastPos) {
		int modelNextPos = sorter.convertRowIndexToModel(viewPos + 1);
		setPosition(modelNextPos);
	    }
	}
    }

    // int pos = bitset.nextSetBit((int) getPosition() + 1);
    // if (pos != EMPTY_REGISTER) {
    // setPosition(pos);
    // }
    /**
     * This implementation should be tested with spare selections on big files
     * to check if has an acceptable performance
     */
    private void nextSelected() {
	FBitSet bitset = nt.getRecordset().getSelection();
	int viewPos = sorter.convertRowIndexToView((int) getPosition());
	for (int i = viewPos + 1; i < sorter.getViewRowCount(); i++) {
	    int modelPos = sorter.convertRowIndexToModel(i);
	    if (bitset.get(modelPos)) {
		setPosition(modelPos);
		return;
	    }
	}
    }

    public void last() {
	if (isOnlySelected()) {
	    lastSelected();
	} else {
	    // setPosition(getRecordset().getRowCount() - 1);
	    int viewLastPos = sorter.getViewRowCount() - 1;
	    int modelLastPos = sorter.convertRowIndexToModel(viewLastPos);
	    setPosition(modelLastPos);
	}
    }

    // int pos = bitset.length();
    // if (pos != 0) {
    // setPosition(pos - 1);
    // }
    public void lastSelected() {
	FBitSet bitset = nt.getRecordset().getSelection();
	int viewLastPos = sorter.getViewRowCount() - 1;
	for (int i = viewLastPos; i >= 0; i--) {
	    int modelPos = sorter.convertRowIndexToModel(i);
	    if (bitset.get(modelPos)) {
		setPosition(modelPos);
		return;
	    }
	}
    }

    public void first() {
	if (isOnlySelected()) {
	    firstSelected();
	} else {
	    // setPosition(0);
	    int viewFirstPos = 0;
	    int modelFirstPos = sorter.convertRowIndexToModel(viewFirstPos);
	    setPosition(modelFirstPos);
	}
    }

    public void firstSelected() {
	FBitSet bitset = nt.getRecordset().getSelection();
	int viewFirstPos = 0;
	for (int i = viewFirstPos; i < sorter.getViewRowCount(); i++) {
	    int modelPos = sorter.convertRowIndexToModel(i);
	    if (bitset.get(modelPos)) {
		setPosition(modelPos);
		return;
	    }
	}
    }

    // returns the index of the record in the datasource that matches the
    // previous (getPosition() - 1) record in the view
    // if the view position is the first (0) it returns -1
    public int getPreviousPositionInModel() {
	int viewPos = sorter.convertRowIndexToView((int) getPosition());
	if (viewPos == 0) {
	    return -1;
	}
	int modelPrevPos = sorter.convertRowIndexToModel(viewPos - 1);
	return modelPrevPos;
    }

    public void goToPreviousInView() {
	if (isOnlySelected()) {
	    goToPreviousSelectedInView();
	} else {
	    int modelPrevPos = getPreviousPositionInModel();
	    if (modelPrevPos > -1) {
		setPosition(modelPrevPos);
	    }
	}
    }

    private int getPreviusPositionSelectedInModel() {
	FBitSet bitset = nt.getRecordset().getSelection();
	int viewPos = sorter.convertRowIndexToView((int) getPosition());
	for (int i = viewPos - 1; i >= 0; i--) {
	    int modelPos = sorter.convertRowIndexToModel(i);
	    if (bitset.get(modelPos)) {
		return modelPos;
	    }
	}

	return -1;
    }

    // int pos = (int) (getPosition() - 1);
    // for (; pos >= 0 && !bitset.get(pos); pos--) {
    // ;
    // }
    // if (pos != EMPTY_REGISTER) {
    // setPosition(pos);
    // }
    private void goToPreviousSelectedInView() {
	int modelPos = getPreviusPositionSelectedInModel();
	if (modelPos != -1) {
	    setPosition(modelPos);
	}
    }

    /**
     * {@link #init()} method must be called before this
     * 
     * @param newPosition
     *            zero-based index on recordset
     */
    public void setPosition(long newPosition) {
	if (!isValidPosition(newPosition)) {
	    return;
	}
	try {
	    if (newPosition >= nt.getRecordset().getRowCount()) {
		newPosition = nt.getRecordset().getRowCount() - 1;
	    } else if (newPosition < EMPTY_REGISTER) {
		newPosition = 0;
	    }

	    PositionEvent evt = new PositionEvent(this, currentPosition,
		    newPosition);
	    positionEventSource.fireBeforePositionChange(evt);
	    currentPosition = newPosition;
	    positionEventSource.fireOnPositionChange(evt);
	} catch (DataException e) {
		logger.error(e.getMessage(), e);
	}
    }

    public long getPosition() {
	return currentPosition;
    }

    private boolean isValidPosition(long pos) {
	if (isOnlySelected()) {
	    return isRecordSelected(pos);
	}
	return true;
    }

    private void setTotalLabelText() {
	try {
	    long numberOfRowsInRecordset = nt.getRecordset().getRowCount();
	    if (isOnlySelected()) {
		totalLabel.setText("/" + "(" + nt.getNumberOfRowsSelected()
			+ ") " + numberOfRowsInRecordset);
	    } else {
		totalLabel.setText("/" + numberOfRowsInRecordset);
	    }
	} catch (DataException e) {
		logger.error(e.getMessage(), e);
	}

    }

    private void posTFChanged() {
	String pos = posTF.getText();
	try {
	    long userViewPos = Long.parseLong(pos) - 1;
	    int modelPos = sorter.convertRowIndexToModel((int) userViewPos);
	    setPosition(modelPos);
	} catch (NumberFormatException e) {
	    logger.error(e.getMessage(), e);
	    refreshGUI(firstB.isEnabled());
	}
    }

    @Override
    public void actionPerformed(ActionEvent e) {
	if (e.getSource() == nextB) {
	    next();
	} else if (e.getSource() == lastB) {
	    last();
	} else if (e.getSource() == firstB) {
	    first();
	} else if (e.getSource() == beforeB) {
	    goToPreviousInView();
	} else if (e.getSource() == posTF) {
	    posTFChanged();
	} else if (e.getSource() == onlySelectedCB) {
	    if (alwaysSelectCB.isSelected()) {
		alwaysSelectCB.setSelected(false);
		try {
			nt.getLayer().getFeatureStore().getFeatureSelection().addObserver(this);
		} catch (DataException e1) {
			logger.error(e1.getMessage(), e);
		}
	    }

	    if (onlySelectedCB.isSelected()) {
		if (!isEmptyRegister()) {
		    viewOnlySelected();
		}
	    } else {
		if (isEmptyRegister()) {
		    setPosition(0);
		}
	    }
	    nt.refreshGUI();
	} else if (e.getSource() == alwaysSelectCB) {
	    onlySelectedCB.setSelected(false);
	    if (alwaysSelectCB.isSelected()) {
	    	try {
				nt.getLayer().getFeatureStore().getFeatureSelection().deleteObserver(this);
			} catch (DataException e1) {
				logger.error(e1.getMessage(), e);
			}
	    } else {
	    	try {
				nt.getLayer().getFeatureStore().getFeatureSelection().addObserver(this);
			} catch (DataException e1) {
				logger.error(e1.getMessage(), e1);
			}
	    }
	    nt.refreshGUI();
	} else if (e.getSource() == selectionB) {
	    nt.selectCurrentFeature();
	    nt.refreshGUI();
	}
    }

    /**
     * Forces the application to navigate only between selected features.
     */
    private void viewOnlySelected() {
	if (nt.getNumberOfRowsSelected() == 0) {
	    setPosition(EMPTY_REGISTER);
	}
	if (!isRecordSelected()) {
	    nt.firstSelected();
	} else {
	    refreshGUI(onlySelectedCB.isEnabled());
	}
    }

    /**
     * @return true if the current row is selected, false if not.
     */
    private boolean isRecordSelected() {
	return isRecordSelected(getPosition());
    }

    private boolean isRecordSelected(long position) {
	FBitSet bitset = null;
	if (position == EMPTY_REGISTER) {
	    return false;
	}
	int pos = Long.valueOf(position).intValue();
	if (nt.getRecordset() == null) {
	    return false;
	}
	bitset = nt.getRecordset().getSelection();
	return bitset.get(pos);
    }

    public boolean isEmptyRegister() {
	return getPosition() == EMPTY_REGISTER;
    }

    public void addEventListener(PositionListener l) {
	positionEventSource.addEventListener(l);
    }

    public void removeEventListener(PositionListener l) {
	positionEventSource.removeEventListener(l);
    }

    public Component getOptionsPanel() {
	return optionsPanel;
    }

    public void setOnlySelected(boolean bool) {
	if (bool != onlySelectedCB.isSelected()) {
	    onlySelectedCB.doClick();
	}
    }

    public boolean isOnlySelected() {
	return onlySelectedCB.isSelected();
    }

    public void refreshGUI(boolean navEnabled) {
	refreshGUISelection(navEnabled);
	refreshGUINavigation(navEnabled);
    }

    private void refreshGUISelection(boolean navEnabled) {
	if (alwaysSelectCB.isSelected()) {
	    nt.clearSelection();
	    nt.selectCurrentFeature();
	}

	if (isRecordSelected()) {
	    ImageIcon imagenUnselect = nt.getIcon("/Unselect.png");
	    selectionB.setIcon(imagenUnselect);
	} else {
	    ImageIcon imagenSelect = nt.getIcon("/Select.png");
	    selectionB.setIcon(imagenSelect);
	}
	selectionB.setEnabled(navEnabled);
	alwaysSelectCB.setEnabled(navEnabled);
    }

    private void refreshGUINavigation(boolean navEnabled) {
	firstB.setEnabled(navEnabled);
	beforeB.setEnabled(navEnabled);
	nextB.setEnabled(navEnabled);
	lastB.setEnabled(navEnabled);
	if (isEmptyRegister()) {
	    posTF.setText("");
	} else {
	    // user will set a 1-based index to navigate through layer,
	    // so we need to adapt it to currentPosition (a zero-based
	    // index)
	    try {
		int p = sorter.convertRowIndexToView((int) getPosition());
		posTF.setText(String.valueOf(p + 1));

	    } catch (IndexOutOfBoundsException e) {
		/*
		 * fpuga. 10/12/2014. Workaround When the user delete the last
		 * record the editionChanged in EditionListener is called, and
		 * the gui is refreshed, but getPosition returns a removed
		 * position
		 */
	    	logger.error(e.getMessage(), e);
		int p = sorter.convertRowIndexToView((int) getPosition() - 1);
		posTF.setText(String.valueOf(p + 1));
	    }
	}
	setTotalLabelText();
	if (isRecordSelected()) {
	    posTF.setBackground(Color.YELLOW);
	} else {
	    posTF.setBackground(Color.WHITE);
	}
    }

    public void setSortKeys(List<? extends SortKey> keys) {
	sorter.setSortKeys(keys);
	refreshGUI(firstB.isEnabled());
    }

    public List<? extends SortKey> getSortKeys() {
	return sorter.getSortKeys();
    }

    public void modelChanged() {
	List<? extends SortKey> sortKeys = sorter.getSortKeys();
	sorter = new NTRowSorter<SelectableDataSource>(nt.getRecordset());
	sorter.setSortKeys(sortKeys);

	refreshGUI(firstB.isEnabled());
    }

    public void setListeners() {
    	try {
			nt.getLayer().getFeatureStore().getFeatureSelection().addObserver(this);
		} catch (DataException e) {
			logger.error(e.getMessage(), e);
		}
    }

    public void removeListeners() {
    	try {
			nt.getLayer().getFeatureStore().getFeatureSelection().deleteObserver(this);
		} catch (DataException e) {
			logger.error(e.getMessage(), e);
		}
    }

    // Probably should be removed and use a factory instead
    // is duplicated with NavigationHandler
    private JButton getNavTableButton(JButton button, String iconName,
	    String toolTipName) {
	JButton but = new JButton(nt.getIcon(iconName));
	but.setToolTipText(PluginServices.getText(this, toolTipName));
	but.addActionListener(this);
	return but;
    }

    // Probably should be removed and use a factory instead
    // is duplicated with NavigationHandler
    private JCheckBox getNavTableCheckBox(JCheckBox cb, String toolTipName) {
	cb = new JCheckBox(PluginServices.getText(this, toolTipName));
	cb.addActionListener(this);
	return cb;
    }

    public void registerNavTableButtonsOnActionToolBarExtensionPoint() {
	ExtensionPoints extensionPoints = ExtensionPointsSingleton
		.getInstance();
	selectionB = getNavTableButton(selectionB, "/Select.png",
		"selectionButtonTooltip");
	extensionPoints.add(AbstractNavTable.NAVTABLE_ACTIONS_TOOLBAR,
		"button-selection", selectionB);
    }

    @Deprecated
    /** fpuga. 19/11/2014. Don't use this method. It's created as a workaround to
     *  make copyPrevious and copySelected work 
     */
    public void setPosition(long newPosition, boolean b) {
	if (!isValidPosition(newPosition)) {
	    return;
	}
	try {
	    if (newPosition >= nt.getRecordset().getRowCount()) {
		newPosition = nt.getRecordset().getRowCount() - 1;
	    } else if (newPosition < EMPTY_REGISTER) {
		newPosition = 0;
	    }
	    currentPosition = newPosition;
	} catch (DataException e) {
		logger.error(e.getMessage(), e);
	}
    }

	@Override
	public void update(Observable arg0, Object arg1) {
		if (arg1.equals(DataStoreNotification.SELECTION_CHANGE)) {
			// TODO. Cuando en la misma acción del usuario se deselecciona
			// un elemento y se selecciona otro, pinchando en otro elemento
			// de la tabla por ejemplo se produce dos veces el evento
			if (onlySelectedCB.isSelected() && !isRecordSelected()) {
			    nt.first();
			}
			nt.refreshGUI();
		}
		
	}
}
