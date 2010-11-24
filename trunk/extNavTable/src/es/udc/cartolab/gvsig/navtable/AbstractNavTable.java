/*
 * This file is part of NavTable
 * Copyright (C) 2009 - 2010  Cartolab (Universidade da Coruña)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * 
 * Authors:
 *   Juan Ignacio Varela García <nachouve (at) gmail (dot) com>
 *   Pablo Sanxiao Roca <psanxiao (at) gmail (dot) com>
 *   Javier Estévez Valiñas <valdaris (at) gmail (dot) com>
 */
package es.udc.cartolab.gvsig.navtable;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import com.hardcode.gdbms.driver.exceptions.InitializeDriverException;
import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.andami.PluginServices;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.andami.ui.mdiManager.IWindowListener;
import com.iver.andami.ui.mdiManager.WindowInfo;
import com.iver.cit.gvsig.CADExtension;
import com.iver.cit.gvsig.FiltroExtension;
import com.iver.cit.gvsig.exceptions.expansionfile.ExpansionFileReadException;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.edition.EditionEvent;
import com.iver.cit.gvsig.fmap.edition.VectorialEditableAdapter;
import com.iver.cit.gvsig.fmap.layers.FBitSet;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.ReadableVectorial;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;
import com.iver.cit.gvsig.fmap.layers.SelectionEvent;
import com.iver.cit.gvsig.fmap.layers.SelectionListener;
import com.iver.cit.gvsig.fmap.layers.layerOperations.AlphanumericData;
import com.iver.cit.gvsig.layers.VectorialLayerEdited;

/**
 * 
 * AbstractNavTable is the base class that defines the layout of the window that
 * allows to navigate between the elements of the layer.
 * 
 * It has three panels:
 * <ul>
 * <li>The north panel, with the controls to handle the navigation behavior.
 * <li>The main panel, with the representation of the layer data, it must be
 * implemented in the subclasses.
 * <li>The south panel, with the navigation controls.
 * </ul>
 * 
 * <img src="images/NavTableWindow.png">
 * 
 * If there are a image on
 * 'gvSIG/extensiones/es.udc.cartolab.gvsig.navtable/images/navtable_header.png'
 * is loaded on the NorthPanel.
 * 
 * @author Nacho Varela
 * @author Javier Estevez
 * @author Pablo Sanxiao
 * @author Andres Maneiro
 * 
 */
public abstract class AbstractNavTable extends JPanel implements IWindow, ActionListener, SelectionListener, IWindowListener {

    private static final int EMPTY_REGISTER = -1;
    protected static final int BUTTON_REMOVE = 0;
    protected static final int BUTTON_SAVE = 1;
    protected static final int BUTTON_SELECTION = 2;
    protected static final int BUTTON_ZOOM = 3;
    protected static final int BUTTON_COPY_PREVIOUS = 4;
    protected static final int BUTTON_COPY_SELECTED = 5;
    private static final long serialVersionUID = 1L;
    protected static Logger logger = Logger.getLogger("NavTable");

    protected JPanel northPanel = null;
    protected JPanel centerPanel = null;
    protected JPanel southPanel = null;

    protected WindowInfo viewInfo = null;
    protected long currentPosition = 0;

    protected FLyrVect layer = null;
    protected SelectableDataSource recordset = null;

    protected boolean changedValues = false;

    // NORTH
    protected JCheckBox onlySelectedCB = null;
    protected JCheckBox fixScaleCB = null;
    protected JCheckBox alwaysZoomCB = null;
    protected JCheckBox alwaysSelectCB = null;

    protected JButton filterB = null;
    protected JButton noFilterB = null;

    // SOUTH
    protected JButton firstB = null;
    protected JButton beforeB = null;
    protected JTextField posTF = null;
    protected JLabel totalLabel = null;
    protected JButton nextB = null;
    protected JButton lastB = null;
    protected JButton copyPreviousB = null;
    protected JButton copySelectedB = null;
    protected JButton zoomB = null;
    protected JButton selectionB = null;
    protected JButton saveB = null;
    protected JButton removeB = null;

    /**
     * 
     * Constructor of the class. It gets the data from the layer and stores it
     * in recordset to later uses.
     * 
     * @param layer
     *            Vectorial layer whose data will be accessed.
     */
    public AbstractNavTable(FLyrVect layer) {
	super();
	this.layer = layer;
	WindowInfo window = this.getWindowInfo();
	String title = window.getTitle();
	window.setTitle(title + ": " + layer.getName());
	try {
	    this.recordset = layer.getRecordset();
	    this.recordset.addSelectionListener(this);
	} catch (ReadDriverException e) {
	    logger.error(e.getMessage(), e);
	}
    }

    /**
     * Constructor of the class. This constructor is used by
     * AlphanumericNavTable
     * 
     * @param recordset
     */
    public AbstractNavTable(SelectableDataSource recordset) {
	super();
	this.layer = null;
	WindowInfo window = this.getWindowInfo();
	String title = window.getTitle();
	// TODO When the table is on edition, on title window
	// is shown a weird identify instead of the layer name
	window.setTitle(title + "*: " + recordset.getName());

	this.recordset = recordset;
	this.recordset.addSelectionListener(this);
    }

    protected JButton getButton(int buttonName) {
	switch (buttonName) {
	case BUTTON_COPY_SELECTED:
	    return copySelectedB;
	case BUTTON_COPY_PREVIOUS:
	    return copyPreviousB;
	case BUTTON_ZOOM:
	    return zoomB;
	case BUTTON_SELECTION:
	    return selectionB;
	case BUTTON_SAVE:
	    return saveB;
	case BUTTON_REMOVE:
	    return removeB;
	default:
	    return null;
	}
    }

    /**
     * It initializes the window.
     * 
     * @return true if it is successful, false if not.
     */
    public abstract boolean init();

    /**
     * It shows the values of a data row in the main panel.
     * 
     * @param rowPosition
     *            the row of the data to be shown.
     */
    public abstract void fillValues();


    /**
     * It fills NavTable with empty values. Used when "Selected" option is set
     * on, but there are any selection registers.
     * 
     */
    public void fillEmptyValues() {
	currentPosition = EMPTY_REGISTER;
	// TODO Set not enabled all navButtons, seleccion, etc.
    }

    /**
     * It selects a specific row into the table.
     * 
     * @param row
     */
    public abstract void selectRow(int row);

    /**
     * Checks if there's changed values.
     * 
     * @return a vector with the position of the values that have changed.
     */
    @Deprecated
    protected Vector checkChangedValues() {
	return new Vector();
    }

    /**
     * @return true is some value has changed, false otherwise
     */
    protected boolean isChangedValues() {
	return changedValues;
    }

    /**
     * Set true or false the boolean variable changedValues
     */
    protected void setChangedValues(boolean bool) {
	changedValues = bool;
    }

    /**
     * Saves the changes of the current data row.
     * 
     */
    @Deprecated
    protected void saveRegister() {
	saveRecord();
    }

    /**
     * Saves the changes of the current data row.
     * 
     */
    protected abstract boolean saveRecord();

    /**
     * 
     * @param true to enable the save button, false to disable it
     */
    protected void enableSaveButton(boolean bool) {
	if (layer != null && layer.isEditing()) {
	    saveB.setEnabled(false);
	} else if (!isChangedValues()) {
	    saveB.setEnabled(false);
	} else {
	    saveB.setEnabled(bool);
	}
    }

    protected void initNorthPanelButtons() {
	filterB = getNavTableButton(filterB, "/filter.png", "filterTooltip");
	noFilterB = getNavTableButton(noFilterB, "/nofilter.png",
		"noFilterTooltip");
	onlySelectedCB = getNavTableCheckBox(onlySelectedCB, "selectedCheckBox");
	alwaysSelectCB = getNavTableCheckBox(alwaysSelectCB, "selectCheckBox");
	alwaysZoomCB = getNavTableCheckBox(alwaysZoomCB, "alwaysZoomCheckBox");
	fixScaleCB = getNavTableCheckBox(fixScaleCB, "fixedScaleCheckBox");
    }

    private JPanel getFilterPanel(File iconPath) {
	JPanel filterPanel = new JPanel(new FlowLayout());
	if (iconPath.exists()) {
	    filterPanel.setBackground(Color.WHITE);
	}
	filterPanel.add(filterB);
	filterPanel.add(noFilterB);
	return filterPanel;
    }

    protected JPanel getOptionsPanel() {
	JPanel optionsPanel = new JPanel(new FlowLayout());
	optionsPanel.add(onlySelectedCB);
	optionsPanel.add(alwaysSelectCB);
	optionsPanel.add(alwaysZoomCB);
	optionsPanel.add(fixScaleCB);
	return optionsPanel;
    }

    private JLabel getIcon(File iconPath) {
	ImageIcon logo = new ImageIcon(iconPath.getAbsolutePath());
	JLabel icon = new JLabel();
	icon.setIcon(logo);
	return icon;
    }

    private JPanel getNorthFirstRow() {
	File iconPath = new File(
		"gvSIG/extensiones/es.udc.cartolab.gvsig.navtable/images/navtable_header.png");
	JPanel northFirstRow = new JPanel(new BorderLayout());
	if (iconPath.exists()) {
	    northFirstRow.setBackground(Color.WHITE);
	    northFirstRow.add(getIcon(iconPath), BorderLayout.WEST);
	    viewInfo.setHeight(575);
	}
	northFirstRow.add(getFilterPanel(iconPath), BorderLayout.EAST);
	return northFirstRow;
    }

    /**
     * Creates the upper panel.
     * 
     * @return the panel.
     */
    protected JPanel getNorthPanel() {
	initNorthPanelButtons();
	northPanel = new JPanel(new BorderLayout());
	northPanel.add(getNorthFirstRow(), BorderLayout.NORTH);
	northPanel.add(getOptionsPanel(), BorderLayout.SOUTH);
	return northPanel;
    }

    public void fillValues(long rowPosition) {
	currentPosition = rowPosition;
	refreshGUI();
    }

    /**
     * Creates the main panel.
     * 
     * @return the panel.
     */
    public abstract JPanel getCenterPanel();

    private ImageIcon getIcon(String iconName) {
	java.net.URL imgURL = null;
	imgURL = getClass().getResource(iconName);
	ImageIcon icon = new ImageIcon(imgURL);
	return icon;
    }

    protected JButton getNavTableButton(JButton button, String iconName,
	    String toolTipName) {
	JButton but = new JButton(getIcon(iconName));
	but.setToolTipText(PluginServices.getText(this, toolTipName));
	but.addActionListener(this);
	return but;
    }

    private JCheckBox getNavTableCheckBox(JCheckBox cb, String toolTipName) {
	cb = new JCheckBox(PluginServices.getText(this, toolTipName));
	cb.addActionListener(this);
	return cb;
    }

    private JPanel getNavToolBar() {
	JPanel navToolBar = new JPanel(new FlowLayout());
	navToolBar.add(firstB);
	navToolBar.add(beforeB);
	navToolBar.add(posTF);
	navToolBar.add(totalLabel);
	navToolBar.add(nextB);
	navToolBar.add(lastB);
	return navToolBar;
    }

    protected JPanel getActionsToolBar() {
	JPanel actionsToolBar = new JPanel(new FlowLayout());
	actionsToolBar.add(copySelectedB);
	actionsToolBar.add(copyPreviousB);
	actionsToolBar.add(zoomB);
	actionsToolBar.add(selectionB);
	actionsToolBar.add(saveB);
	actionsToolBar.add(removeB);
	return actionsToolBar;
    }

    protected void initNavTableSouthPanelButtons() {
	firstB = getNavTableButton(firstB, "/go-first.png",
		"goFirstButtonTooltip");
	beforeB = getNavTableButton(beforeB, "/go-previous.png",
		"goPreviousButtonTooltip");
	posTF = new JTextField(5);
	posTF.addActionListener(this);
	totalLabel = new JLabel();
	nextB = getNavTableButton(nextB, "/go-next.png", "goNextButtonTooltip");
	lastB = getNavTableButton(lastB, "/go-last.png", "goLastButtonTooltip");
	copySelectedB = getNavTableButton(copySelectedB, "/copy-selected.png",
		"copySelectedButtonTooltip");
	copyPreviousB = getNavTableButton(copyPreviousB, "/copy.png",
		"copyPreviousButtonTooltip");
	zoomB = getNavTableButton(zoomB, "/zoom.png", "zoomButtonTooltip");
	selectionB = getNavTableButton(selectionB, "/Select.png",
		"selectionButtonTooltip");
	saveB = getNavTableButton(saveB, "/save.png", "saveButtonTooltip");
	saveB.setEnabled(false);
	removeB = getNavTableButton(removeB, "/delete.png", "delete_register");
    }

    /**
     * Creates the bottom panel.
     * 
     * @return the panel.
     */
    protected JPanel getSouthPanel() {
	initNavTableSouthPanelButtons();
	southPanel = new JPanel(new BorderLayout());
	southPanel.add(getNavToolBar(), BorderLayout.SOUTH);
	southPanel.add(getActionsToolBar(), BorderLayout.NORTH);
	return southPanel;
    }

    /**
     * Shows a warning to the user if there's unsaved data.
     * 
     */
    protected void showWarning() {
	if (currentPosition == EMPTY_REGISTER) {
	    return;
	}
	if (isChangedValues()) {
	    Object[] options = {
		    PluginServices.getText(this, "saveButtonTooltip"),
		    PluginServices.getText(this, "ignoreButton") };
	    int response = JOptionPane.showOptionDialog(this,
		    PluginServices.getText(this, "unsavedDataMessage"),
		    PluginServices.getText(this, "unsavedDataTitle"),
		    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
		    null, // do not use a custom Icon
		    options, // the titles of buttons
		    options[1]); // default button title
	    if (response == 0) {
		saveRecord();
	    }
	}
    }

    /**
     * Goes to the next row of the data.
     * 
     */
    public void next() {
	showWarning();
	try {
	    if (onlySelectedCB.isSelected()) {
		nextSelected();
	    } else {
		if (currentPosition < recordset.getRowCount()) {
		    currentPosition = currentPosition + 1;
		    // fillValues();
		}
	    }
	    refreshGUI();
	} catch (ReadDriverException e) {
	    logger.error(e.getMessage(), e);
	}
    }

    /**
     * Goes to the next selected row of the data.
     */
    protected void nextSelected() {
	FBitSet bitset = recordset.getSelection();
	int currentPos = Long.valueOf(currentPosition).intValue();
	int pos = bitset.nextSetBit(currentPos + 1);
	if (pos != EMPTY_REGISTER) {
	    currentPosition = pos;
	    // fillValues();
	}
    }

    /**
     * Goes to the last row of the data.
     * 
     */
    public void last() {
	showWarning();
	try {
	    if (onlySelectedCB.isSelected()) {
		lastSelected();
	    } else {
		currentPosition = recordset.getRowCount() - 1;
		// fillValues();
	    }
	    refreshGUI();
	} catch (ReadDriverException e) {
	    logger.error(e.getMessage(), e);
	}
    }

    /**
     * Goes to the last selected row of the data.
     * 
     */
    private void lastSelected() {
	FBitSet bitset = recordset.getSelection();
	int pos = bitset.length();
	if (pos != 0) {
	    currentPosition = pos - 1;
	    // fillValues();
	}
	refreshGUI();
    }

    /**
     * Goes to the first row of the data.
     * 
     */
    public void first() {
	showWarning();
	if (onlySelectedCB.isSelected()) {
	    firstSelected();
	} else {
	    currentPosition = 0;
	    // fillValues();
	}
	refreshGUI();
    }

    /**
     * Goes to the first selected row of the data.
     * 
     */
    private void firstSelected() {
	FBitSet bitset = recordset.getSelection();
	int pos = bitset.nextSetBit(0);
	if (pos != EMPTY_REGISTER) {
	    currentPosition = pos;
	    // fillValues();
	}
    }

    /**
     * Goes to the previous row of the data.
     * 
     */
    public void before() {
	showWarning();
	if (onlySelectedCB.isSelected()) {
	    beforeSelected();
	} else {
	    // '-1' is used by NavTable to show empty values, so I must check it
	    if (currentPosition > 0) {
		currentPosition = currentPosition - 1;
	    }
	    // fillValues();
	}
	refreshGUI();
    }

    /**
     * Goes to the previous selected row of the data.
     * 
     */
    protected void beforeSelected() {
	FBitSet bitset = recordset.getSelection();
	int currentPos = Long.valueOf(currentPosition).intValue() - 1;
	int pos = currentPos;
	for (; pos >= 0 && !bitset.get(pos); pos--) {
	    ;
	}
	if (pos != EMPTY_REGISTER) {
	    currentPosition = pos;
	    // fillValues();
	}
    }

    /**
     * Zooms to the current feature. The feature will fill the visualization
     * area.
     * 
     */
    private void zoom() {
	Rectangle2D rectangle = null;
	int pos = Long.valueOf(currentPosition).intValue();
	if (layer instanceof AlphanumericData) {
	    // TODO gvSIG comment: Esta comprobacion se hacia con Selectable
	    try {
		IGeometry g;
		ReadableVectorial source = (layer).getSource();
		source.start();
		g = source.getShape(pos);
		source.stop();
		/*
		 * fix to avoid zoom problems when layer and view projections
		 * aren't the same.
		 */
		if (layer.getCoordTrans() != null) {
		    g.reProject(layer.getCoordTrans());
		}
		rectangle = g.getBounds2D();
		if (rectangle.getWidth() < 200) {
		    rectangle.setFrameFromCenter(rectangle.getCenterX(),
			    rectangle.getCenterY(),
			    rectangle.getCenterX() + 100,
			    rectangle.getCenterY() + 100);
		}
		if (rectangle != null) {
		    layer.getMapContext().getViewPort().setExtent(rectangle);
		}
	    } catch (InitializeDriverException e) {
		logger.error(e.getMessage(), e);
	    } catch (ReadDriverException e) {
		logger.error(e.getMessage(), e);
	    }
	}
    }

    /**
     * It forces the application to use the current scale when navigating
     * between features. It also centers the visualization to the feature when
     * navigating.
     * 
     */
    private void fixScale() {
	long scale = layer.getMapContext().getScaleView();
	zoom();
	layer.getMapContext().setScaleView(scale);
    }

    private void selectCurrentFeature() {
	FBitSet bitset = null;
	int pos = Long.valueOf(currentPosition).intValue();
	bitset = recordset.getSelection();
	if (!bitset.get(pos)) {
	    bitset.set(pos);
	} else {
	    bitset.clear(pos);
	    if (onlySelectedCB.isSelected()) {
		lastSelected();
	    }
	}
	recordset.setSelection(bitset);
    }

    /**
     * 
     * @return true if the current row is selected, false if not.
     */
    private boolean isRecordSelected() {
	return isRecordSelected(currentPosition);
    }

    /**
     * 
     * @return true if the current row is selected, false if not.
     */
    private boolean isRecordSelected(long position) {
	FBitSet bitset = null;
	if (position == EMPTY_REGISTER) {
	    return false;
	}
	int pos = Long.valueOf(position).intValue();
	if (recordset == null) {
	    return false;
	}
	bitset = recordset.getSelection();
	return bitset.get(pos);
    }


    /**
     * Removes all selections of the layer.
     * 
     */
    private void clearSelection() {
	FBitSet bitset = null;
	if (layer instanceof AlphanumericData) {
	    bitset = recordset.getSelection();
	    bitset.clear();
	    recordset.setSelection(bitset);
	}
    }

    /**
     * Forces the application to navigate only between selected features.
     * 
     */
    private void viewOnlySelected() {
	if (getNumberOfRowsSelected() == 0) {
	    currentPosition = EMPTY_REGISTER;
	}

	if (!isRecordSelected()) {
	    // System.out.println("View only selected... getFirstSelected");
	    firstSelected();
	}
    }

    /**
     * Sets the configuration of the window.
     * 
     * @return the configuration of the window.
     */
    public WindowInfo getWindowInfo() {
	if (viewInfo == null) {
	    viewInfo = new WindowInfo(WindowInfo.MODELESSDIALOG
		    | WindowInfo.RESIZABLE | WindowInfo.PALETTE);
	    viewInfo.setTitle(PluginServices.getText(this, "NavTable"));
	    // [NachoV] This Frame is just a trick to get the packed size
	    // IWindow
	    java.awt.Frame f = new java.awt.Frame();
	    f.setLayout(new BorderLayout());
	    f.add(getNorthPanel(), BorderLayout.NORTH);
	    f.pack();
	    viewInfo.setWidth(f.getWidth() + 25);
	    f.add(getSouthPanel(), BorderLayout.SOUTH);
	    JPanel centerPanel = getCenterPanel();
	    if (centerPanel != null) {
		f.add(centerPanel, BorderLayout.CENTER);
	    }
	    f.pack();
	    viewInfo.setHeight(f.getHeight());
	}
	return viewInfo;
    }

    /**
     * Repaints the window.
     * 
     */
    protected void refreshGUI() {
	boolean navEnabled = false;
	try {
	    if (recordset == null) return;

	    checkAndUpdateCurrentPositionBoundaries();
	    navEnabled = checkNavEnabledAndFillValues();

	    // north panel buttons
	    alwaysZoomCB.setEnabled(navEnabled);
	    alwaysSelectCB.setEnabled(navEnabled);
	    fixScaleCB.setEnabled(navEnabled);

	    if (isSomeRowToWorkOn()) {
		posTF.setText(String.valueOf(currentPosition + 1));
		if (alwaysSelectCB.isSelected()) {
		    selectionB.setEnabled(false);
		    clearSelection();
		    selectCurrentFeature();
		} else {
		    selectionB.setEnabled(true);
		}

		if (alwaysZoomCB.isSelected()) {
		    zoomB.setEnabled(false);
		    zoom();
		} else {
		    zoomB.setEnabled(true);
		}

		if (fixScaleCB.isSelected()) {
		    fixScale();
		}
	    } else {
		fillEmptyValues();
		posTF.setText("");
	    }

	    // south panel option buttons
	    enableCopySelectedButton(navEnabled);
	    enableCopyPreviousButton(navEnabled);
	    zoomB.setEnabled(navEnabled);
	    selectionB.setEnabled(navEnabled);
	    setIconAndPositionBackgroundForSelection();
	    enableSaveButton(navEnabled);
	    removeB.setEnabled(navEnabled);

	    // south panel navigation buttons
	    firstB.setEnabled(navEnabled);
	    beforeB.setEnabled(navEnabled);
	    setTotalLabelText();
	    nextB.setEnabled(navEnabled);
	    lastB.setEnabled(navEnabled);

	} catch (ReadDriverException e) {
	    logger.error(e.getMessage(), e);
	}
    }

    private void setTotalLabelText() throws ReadDriverException {
	long numberOfRowsInRecordset = recordset.getRowCount();
	if (onlySelectedCB.isSelected()) {
	    totalLabel.setText("/" + "(" + getNumberOfRowsSelected() + ") "
		    + numberOfRowsInRecordset);
	} else {
	    totalLabel.setText("/" + numberOfRowsInRecordset);
	}
    }

    private void enableCopyPreviousButton(boolean navEnabled) {
	if (currentPosition == 0 || !navEnabled) {
	    copyPreviousB.setEnabled(false);
	} else {
	    copyPreviousB.setEnabled(true);
	}
    }

    private void enableCopySelectedButton(boolean navEnabled) {
	if (getNumberOfRowsSelected() == 0 || !navEnabled) {
	    copySelectedB.setEnabled(false);
	} else {
	    copySelectedB.setEnabled(true);
	}
    }

    private boolean checkNavEnabledAndFillValues() {
	boolean navEnabled;
	if (currentPosition == EMPTY_REGISTER) {
	    posTF.setText("");
	    navEnabled = false;
	    fillEmptyValues();
	} else {
	    navEnabled = true;
	    fillValues();
	}
	return navEnabled;
    }

    private void setIconAndPositionBackgroundForSelection() {
	java.net.URL imgURL = null;
	if (isRecordSelected()) {
	    imgURL = getClass().getResource("/Unselect.png");
	    ImageIcon imagenUnselect = new ImageIcon(imgURL);
	    selectionB.setIcon(imagenUnselect);
	    posTF.setBackground(Color.YELLOW);
	} else {
	    imgURL = getClass().getResource("/Select.png");
	    ImageIcon imagenSelect = new ImageIcon(imgURL);
	    selectionB.setIcon(imagenSelect);
	    posTF.setBackground(Color.WHITE);
	}
    }

    private void checkAndUpdateCurrentPositionBoundaries() throws ReadDriverException {
	if (currentPosition >= recordset.getRowCount()) {
	    currentPosition = recordset.getRowCount() - 1;
	}
	if (currentPosition < EMPTY_REGISTER) {
	    currentPosition = 0;
	}
    }

    protected boolean isSomeRowToWorkOn() {
	if (onlySelectedCB == null) {
	    return false;
	}
	if (onlySelectedCB.isSelected() && getNumberOfRowsSelected() == 0) {
	    return false;
	} else {
	    return true;
	}
    }

    private int getNumberOfRowsSelected() {
	FBitSet bitset = recordset.getSelection();
	return bitset.cardinality();
    }

    protected void copyPrevious() {
	String pos = posTF.getText();
	Long posNumber = null;
	try {
	    posNumber = new Long(pos);
	} catch (NumberFormatException e) {
	    logger.error(e.getMessage(), e);
	    posNumber = currentPosition;
	}
	if (!isValidPosition(posNumber)) {
	    if (currentPosition == EMPTY_REGISTER) {
		posTF.setText("");
	    } else {
		posTF.setText(String.valueOf(currentPosition + 1));
	    }
	    return;
	}
	if (onlySelectedCB.isSelected()) {
	    if (!isRecordSelected(posNumber)) {
		posTF.setText(String.valueOf(currentPosition + 1));
		return;
	    }
	}
	showWarning();
	try {
	    currentPosition = posNumber.longValue() - 1;
	    refreshGUI();
	} catch (Exception e1) {
	    e1.printStackTrace();
	}
    }

    protected boolean copySelected() {
	if (getNumberOfRowsSelected() != 1) {
	    // show error
	    JOptionPane.showMessageDialog(null,
		    PluginServices.getText(this, "justOneRecordMessage"),
		    PluginServices.getText(this, "justOneRecordTitle"),
		    JOptionPane.WARNING_MESSAGE);
	    return false;
	} else {
	    // TODO Check this code
	    long current = currentPosition;
	    FBitSet selection = recordset.getSelection();
	    long selectedRow = selection.nextSetBit(0);
	    currentPosition = selectedRow;
	    fillValues();
	    currentPosition = current;
	    return true;
	}
    }

    /**
     * Handles the user actions.
     */
    public void actionPerformed(ActionEvent e) {
	if (this.recordset == null) {
	    // TODO
	    // If there is an error on the recordset of the layer do nothing.
	    return;
	}
	if (e.getSource() == onlySelectedCB) {
	    alwaysSelectCB.setSelected(false);
	    if (onlySelectedCB.isSelected()) {
		// areSelectedRows()
		if (currentPosition != EMPTY_REGISTER) {
		    viewOnlySelected();
		}
	    } else {
		if (currentPosition == EMPTY_REGISTER) {
		    currentPosition = 0;
		}
	    }
	    refreshGUI();
	} else if (e.getSource() == alwaysZoomCB) {
	    fixScaleCB.setSelected(false);
	    refreshGUI();
	} else if (e.getSource() == fixScaleCB) {
	    alwaysZoomCB.setSelected(false);
	    refreshGUI();
	} else if (e.getSource() == alwaysSelectCB) {
	    onlySelectedCB.setSelected(false);
	    if (alwaysSelectCB.isSelected()) {
		this.recordset.removeSelectionListener(this);
	    } else {
		this.recordset.addSelectionListener(this);
	    }
	    refreshGUI();
	} else if (e.getSource() == filterB) {
	    FiltroExtension fe = new FiltroExtension();
	    fe.initialize();
	    fe.execute("FILTRO");
	} else if (e.getSource() == noFilterB) {
	    clearSelection();
	} else if (e.getSource() == nextB) {
	    next();
	} else if (e.getSource() == lastB) {
	    last();
	} else if (e.getSource() == firstB) {
	    first();
	} else if (e.getSource() == beforeB) {
	    before();
	} else if (e.getSource() == posTF) {
	    copyPrevious();
	} else if (e.getSource() == copySelectedB) {
	    if (copySelected()) {
		setChangedValues(true);
		enableSaveButton(true);
	    }
	} else if (e.getSource() == copyPreviousB) {
	    long current = currentPosition;
	    currentPosition = currentPosition - 1;
	    fillValues();
	    currentPosition = current;
	    setChangedValues(true);
	    enableSaveButton(true);
	} else if (e.getSource() == zoomB) {
	    zoom();
	    // refreshGUI();
	} else if (e.getSource() == selectionB) {
	    selectCurrentFeature();
	    refreshGUI();
	} else if (e.getSource() == saveB) {
	    if (saveRecord()) {
		refreshGUI();
	    } else {
		JOptionPane.showMessageDialog(this,
			PluginServices.getText(this, "errorSavingData"), "",
			JOptionPane.ERROR_MESSAGE);
	    }
	} else if (e.getSource() == removeB) {
	    int answer = JOptionPane.showConfirmDialog(null,
		    PluginServices.getText(null, "confirm_delete_register"),
		    null, JOptionPane.YES_NO_OPTION);
	    if (answer == 0) {
		deleteRecord();
	    }
	}
    }

    @Deprecated
    private void deleteRow() {
	deleteRecord();
    }

    protected void deleteRecord() {
	try {
	    boolean layerEditing = true;
	    ReadableVectorial feats = layer.getSource();
	    feats.start();
	    if (currentPosition > EMPTY_REGISTER) {
		ToggleEditing te = new ToggleEditing();
		if (!layer.isEditing()) {
		    layerEditing = false;
		    te.startEditing(layer);
		}
		VectorialLayerEdited vle = CADExtension.getCADTool().getVLE();
		VectorialEditableAdapter vea = vle.getVEA();
		vea.removeRow((int) currentPosition, CADExtension.getCADTool()
			.getName(), EditionEvent.GRAPHIC);
		layer.getSelectionSupport().removeSelectionListener(vle);
		if (!layerEditing) {
		    te.stopEditing(layer, false);
		}
		layer.setActive(true);
		refreshGUI();
	    }
	} catch (ExpansionFileReadException e) {
	    logger.error(e.getMessage(), e);
	} catch (ReadDriverException e) {
	    logger.error(e.getMessage(), e);
	}
    }

    private boolean isValidPosition(Long pos) {
	if (pos == null || pos.longValue() == 0) {
	    return false;
	}
	if (onlySelectedCB.isSelected()) {
	    return isRecordSelected(pos.longValue());
	}
	return true;
    }

    public void selectionChanged(SelectionEvent e) {
	if (currentPosition == EMPTY_REGISTER && onlySelectedCB.isSelected()) {
	    firstSelected();
	} else {
	    if (onlySelectedCB.isSelected() && !isRecordSelected()) {
		firstSelected();
	    }
	    if (!isSomeRowToWorkOn()) {
		fillEmptyValues();
	    }
	}
	refreshGUI();
    }

    public void windowClosed() {
	showWarning();
	this.recordset.removeSelectionListener(this);
    }

    public void windowActivated() {
    }
}
