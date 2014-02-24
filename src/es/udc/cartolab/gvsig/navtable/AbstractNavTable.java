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
 *   Jorge Lopez Fernandez <jlopez (at) cartolab (dot) es>
 */
package es.udc.cartolab.gvsig.navtable;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;
import org.gvsig.exceptions.BaseException;

import com.hardcode.gdbms.driver.exceptions.InitializeDriverException;
import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.andami.PluginServices;
import com.iver.andami.ui.mdiFrame.MDIFrame;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.andami.ui.mdiManager.IWindowListener;
import com.iver.andami.ui.mdiManager.WindowInfo;
import com.iver.cit.gvsig.FiltroExtension;
import com.iver.cit.gvsig.exceptions.visitors.StopWriterVisitorException;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.layers.FBitSet;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.ReadableVectorial;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;
import com.iver.cit.gvsig.fmap.layers.SelectionEvent;
import com.iver.cit.gvsig.fmap.layers.SelectionListener;
import com.iver.cit.gvsig.fmap.layers.layerOperations.AlphanumericData;
import com.iver.utiles.extensionPoints.ExtensionPoint;
import com.iver.utiles.extensionPoints.ExtensionPoints;
import com.iver.utiles.extensionPoints.ExtensionPointsSingleton;

import es.udc.cartolab.gvsig.navtable.dataacces.IController;
import es.udc.cartolab.gvsig.navtable.dataacces.LayerController;
import es.udc.cartolab.gvsig.navtable.listeners.PositionEvent;
import es.udc.cartolab.gvsig.navtable.listeners.PositionEventSource;
import es.udc.cartolab.gvsig.navtable.listeners.PositionListener;
import es.udc.cartolab.gvsig.navtable.preferences.Preferences;
import es.udc.cartolab.gvsig.navtable.utils.EditionListener;

/**
 * <img src="images/NavTableWindow.png">
 * 
 * If there is an image on
 * 'gvSIG/extensiones/es.udc.cartolab.gvsig.navtable/images/navtable_header.png'
 * it will be loaded on the NorthPanel.
 * 
 */
public abstract class AbstractNavTable extends JPanel implements IWindow,
ActionListener, SelectionListener, IWindowListener, PositionListener {

    public static final int EMPTY_REGISTER = -1;
    protected static final int BUTTON_REMOVE = 0;
    protected static final int BUTTON_SAVE = 1;
    protected static final int BUTTON_SELECTION = 2;
    protected static final int BUTTON_ZOOM = 3;
    protected static final int BUTTON_COPY_PREVIOUS = 4;
    protected static final int BUTTON_COPY_SELECTED = 5;
    private static final long serialVersionUID = 1L;
    protected String deleteMessageKey = "confirm_delete_register";
    protected String saveErrorTitleKey = "save_layer_error";
    protected String saveErrorGenericMessageKey = "errorSavingData";
    protected static Logger logger = Logger.getLogger("NavTable");
    public static final String NAVTABLE_ACTIONS_TOOLBAR = "navtable_extension_point_actions_toolbar";
    public static final String NAVTABLE_CONTEXT_MENU = "navtable_extension_point_context_menu";

    protected JPanel northPanel = null;
    protected JPanel centerPanel = null;
    protected JPanel southPanel = null;

    protected WindowInfo windowInfo = null;
    private long currentPosition = 0;

    protected IController layerController;
    protected FLyrVect layer = null;
    protected String dataName = "";

    protected boolean changedValues = false;

    // NORTH
    protected JCheckBox onlySelectedCB = null;
    protected JCheckBox fixScaleCB = null;
    protected JCheckBox alwaysZoomCB = null;
    protected JCheckBox alwaysSelectCB = null;

    // SOUTH
    // actions buttons
    protected JButton filterB = null;
    protected JButton copyPreviousB = null;
    protected JButton copySelectedB = null;
    protected JButton zoomB = null;
    protected JButton selectionB = null;
    protected JButton saveB = null;
    protected JButton removeB = null;
    protected JButton undoB = null;
    // navigation buttons
    protected JButton firstB = null;
    protected JButton beforeB = null;
    protected JTextField posTF = null;
    protected JLabel totalLabel = null;
    protected JButton nextB = null;
    protected JButton lastB = null;

    private boolean isSomeNavTableFormOpen = false;

    protected EditionListener listener;
    private PositionEventSource positionEventSource = new PositionEventSource();

    private JPanel actionsToolBar;
    private JPanel optionsPanel;

    protected boolean openEmptyLayers = false;
    protected boolean isAlphanumericNT = false;

    public AbstractNavTable(FLyrVect layer) {
	super();
	this.layer = layer;
	this.dataName = layer.getName();
    }

    // [nachouve] Check this method because
    // When the table is on edition a weird identify
    // is shown the title window instead of the layer name.
    // Maybe can be set as deprecated and be replaced by:
    // {@link AbstractNavTable(SelectableDataSource, String)}
    // with a properly name as string parameter.
    @Deprecated
    public AbstractNavTable(SelectableDataSource recordset) {
	this(recordset, recordset.getName());
    }

    /**
     * Constructor of the class. This constructor is used by
     * AlphanumericNavTable
     */
    @Deprecated
    public AbstractNavTable(SelectableDataSource recordset, String tableName) {
	this(tableName);
    }
    
    public AbstractNavTable(String tableName) {
	super();
	this.dataName = tableName;
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

    public boolean isAlphanumericNT() {
	return this.isAlphanumericNT;
    }

    public boolean init() {

	try {
	    if ((!openEmptyLayers) && (getRecordset().getRowCount() <= 0)) {
		showEmptyLayerMessage();
		return false;
	    }
	} catch (ReadDriverException e) {
	    logger.error(e.getStackTrace());
	    return false;
	}

	if (!initController()) {
	    return false;
	}

	initGUI();
	initWidgets();
	
	refreshGUI();
	super.repaint();
	super.setVisible(true);
	setOpenNavTableForm(true);
	setFocusCycleRoot(true);
	
	setLayerListeners();
	return true;
    }
    
    /**
     * In NavTable it will get the attribute names from the layer and
     * set it on the left column of the table. On AbstractForm it will
     * initialize the widget vector from the Abeille file
     */
    protected abstract void initWidgets();
    
    protected void initGUI() {
    	MigLayout thisLayout = new MigLayout("inset 0, align center", "[grow]","[][grow][]");
    	this.setLayout(thisLayout);
    	this.add(getNorthPanel(), "shrink, wrap, align center");
    	this.add(getCenterPanel(), "shrink, growx, growy, wrap");
    	this.add(getSouthPanel(), "shrink, align center");
    }

    protected boolean initController() {
	try {
	    layerController = new LayerController(this.layer);
	    layerController.read(getPosition());
	} catch (ReadDriverException e) {
	    e.printStackTrace();
	    return false;
	}
	return true;
    }

    public void resetListeners() {
	removeLayerListeners();
	setLayerListeners();
    }

    public void reinit() {
	resetListeners();
	setOpenNavTableForm(true);
    }

    protected void setLayerListeners() {
	listener = new EditionListener(this, layer);
	layer.addLayerListener(listener);
	getRecordset().addSelectionListener(this);
	addPositionListener(this);
    }

    protected void removeLayerListeners() {
	layer.removeLayerListener(listener);
	getRecordset().removeSelectionListener(this);
	removePositionListener(this);
    }

    public void showEmptyLayerMessage() {

	if ((!openEmptyLayers)) {
	    JOptionPane.showMessageDialog(this,
		    PluginServices.getText(this, "emptyLayer"));
	}
    }

    /**
     * It shows the values of a data row in the main panel.
     */
    public abstract void fillValues();

    /**
     * It fills NavTable with empty values. Used when "Selected" option is set
     * on, but there are any selection registers.
     * 
     */
    public abstract void fillEmptyValues();

    /**
     * It selects a specific row into the table.
     */
    public abstract void selectRow(int row);

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
	undoB.setEnabled(bool);
	changedValues = bool;
    }

    public abstract boolean saveRecord() throws StopWriterVisitorException;

    protected void enableSaveButton(boolean bool) {
	if (layer != null && layer.isEditing()) {
	    saveB.setEnabled(false);
	} else if (!isChangedValues()) {
	    saveB.setEnabled(false);
	} else {
	    saveB.setEnabled(bool);
	}
    }

    public void setOnlySelected(boolean bool) {
	if (bool != onlySelectedCB.isSelected()){
	    onlySelectedCB.doClick();
	}
    }

    @Deprecated
    public void unselectFeature(long feature) {
	FBitSet bitset = null;
	int pos = Long.valueOf(feature).intValue();
	bitset = getRecordset().getSelection();
	if (bitset.get(pos)) {
	    bitset.clear(pos);
	    if (onlySelectedCB.isSelected()) {
		lastSelected();
	    }
	}
	getRecordset().setSelection(bitset);
    }

    public void clearSelectedFeatures() {
	getRecordset().clearSelection();
    }

    protected void initNorthPanelButtons() {
	onlySelectedCB = getNavTableCheckBox(onlySelectedCB, "selectedCheckBox");
	alwaysSelectCB = getNavTableCheckBox(alwaysSelectCB, "selectCheckBox");
	alwaysZoomCB = getNavTableCheckBox(alwaysZoomCB, "alwaysZoomCheckBox");
	fixScaleCB = getNavTableCheckBox(fixScaleCB, "fixedScaleCheckBox");
    }

    protected JPanel getOptionsPanel() {
	if (optionsPanel == null) {
	    optionsPanel = new JPanel(new FlowLayout());
	    optionsPanel.add(onlySelectedCB);
	    optionsPanel.add(alwaysSelectCB);
	    optionsPanel.add(alwaysZoomCB);
	    optionsPanel.add(fixScaleCB);
	}
	return optionsPanel;
    }

    /**
     * Gets the file of the image header for the upper panel. Subclasses can
     * override this method to get their custom image header.
     * 
     * @return the File of the image.
     */
    protected File getHeaderFile() {
	File folder = PluginServices.getPluginServices(this).getPluginDirectory();
	File header = new File (folder.getAbsolutePath() + File.separator + "images" + File.separator + "navtable_header.png");
	
	if (!header.exists()) {
	    header = new File(Preferences.getConfigDir()
		    + "/navtable_header.png");
	}
	return header;
    }

    protected JPanel getNorthPanel() {
	if (northPanel == null) {
	    initNorthPanelButtons();
	    northPanel = new JPanel(new BorderLayout());
	    File iconFile = getHeaderFile();
	    if (iconFile != null && iconFile.exists()) {
		northPanel.setBackground(Color.WHITE);
		ImageIcon logo = new ImageIcon(iconFile.getAbsolutePath());
		JLabel icon = new JLabel();
		icon.setIcon(logo);
		northPanel.add(icon, BorderLayout.WEST);
	    }
	    northPanel.add(getOptionsPanel(), BorderLayout.SOUTH);
	}
	return northPanel;
    }

    public abstract JPanel getCenterPanel();

    public ImageIcon getIcon(String iconName) {
	java.net.URL imgURL = getClass().getResource(iconName);
	if (imgURL == null) {
	    imgURL = AbstractNavTable.class.getResource(iconName);
	}

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

    private JPanel createNavigationToolBar() {
	registerNavTableButtonsOnNavigationToolBarExtensionPoint();
	JPanel navToolBar = new JPanel(new FlowLayout());
	navToolBar.add(firstB);
	navToolBar.add(beforeB);
	navToolBar.add(posTF);
	navToolBar.add(totalLabel);
	navToolBar.add(nextB);
	navToolBar.add(lastB);
	return navToolBar;
    }

    public JPanel getActionsToolBar() {
	if (actionsToolBar == null) {
	    actionsToolBar = new JPanel(new FlowLayout());
	    registerNavTableButtonsOnActionToolBarExtensionPoint();
	    ExtensionPoint actionToolBarEP = (ExtensionPoint) ExtensionPointsSingleton
		    .getInstance().get(NAVTABLE_ACTIONS_TOOLBAR);
	    for (Object button : actionToolBarEP.values()) {
		actionsToolBar.add((JButton) button);
	    }
	}
	return actionsToolBar;
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

    protected void registerNavTableButtonsOnActionToolBarExtensionPoint() {
	ExtensionPoints extensionPoints = ExtensionPointsSingleton
		.getInstance();

	filterB = getNavTableButton(filterB, "/filter.png", "filterTooltip");
	extensionPoints.add(NAVTABLE_ACTIONS_TOOLBAR, "button-enable-filter",
		filterB);

	copySelectedB = getNavTableButton(copySelectedB, "/copy-selected.png",
		"copySelectedButtonTooltip");
	extensionPoints.add(NAVTABLE_ACTIONS_TOOLBAR, "button-copy-selected",
		copySelectedB);

	copyPreviousB = getNavTableButton(copyPreviousB, "/copy.png",
		"copyPreviousButtonTooltip");
	extensionPoints.add(NAVTABLE_ACTIONS_TOOLBAR, "button-copy-previous",
		copyPreviousB);

	zoomB = getNavTableButton(zoomB, "/zoom.png", "zoomButtonTooltip");
	extensionPoints.add(NAVTABLE_ACTIONS_TOOLBAR, "button-zoom", zoomB);

	selectionB = getNavTableButton(selectionB, "/Select.png",
		"selectionButtonTooltip");
	extensionPoints.add(NAVTABLE_ACTIONS_TOOLBAR, "button-selection",
		selectionB);

	saveB = getNavTableButton(saveB, "/save.png", "saveButtonTooltip");
	saveB.setEnabled(false);
	extensionPoints.add(NAVTABLE_ACTIONS_TOOLBAR, "button-save", saveB);

	removeB = getNavTableButton(removeB, "/delete.png", "delete_register");
	extensionPoints.add(NAVTABLE_ACTIONS_TOOLBAR, "button-remove", removeB);

	undoB = getNavTableButton(undoB, "/edit-undo.png", "clearChangesButtonTooltip");
	undoB.setEnabled(false);
	extensionPoints.add(NAVTABLE_ACTIONS_TOOLBAR, "button-clear-changes", undoB);
    }

    protected JPanel getSouthPanel() {
	if (southPanel == null) {
	    southPanel = new JPanel(new BorderLayout());
	    southPanel.add(createNavigationToolBar(), BorderLayout.SOUTH);
	    southPanel.add(getActionsToolBar(), BorderLayout.NORTH);
	}
	return southPanel;
    }

    /**
     * Shows a warning to the user if there's unsaved data.
     * 
     */
    protected boolean showWarning() {
	if (getPosition() == EMPTY_REGISTER) {
	    return true;
	}
	if (isChangedValues()) {
	    boolean save = false;
	    Object[] options = {
		    PluginServices.getText(this, "saveButtonTooltip"),
		    PluginServices.getText(this, "ignoreButton") };
	    int response = JOptionPane.showOptionDialog(this,
		    PluginServices.getText(this, "unsavedDataMessage"),
		    PluginServices.getText(this, "unsavedDataTitle"),
		    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
		    null, // do not use a custom Icon
		    options, // the titles of buttons
		    options[0]); // default button title
	    if (response == JOptionPane.YES_OPTION) {
		save = true;
	    } else {
		save = false;
		setChangedValues(false);
		//The values will be restored as they are
		// when filling again the table/form,
		// so it's not need to revert the changes done.
	    }
	    if (save) {
		try {
		saveRecord();
		} catch (StopWriterVisitorException ex) {
		    ex.printStackTrace();
		    String errorMessage = ex.getCause().getMessage();
		    int startIndex = errorMessage.indexOf("«"), endIndex = errorMessage
			    .indexOf("»");
		    if ((startIndex > -1) && (endIndex > startIndex)) {
			String auxMessage = errorMessage.substring(
				startIndex + 1, endIndex), aux_message_intl = PluginServices
				.getText(this, auxMessage);
			if (!aux_message_intl.equals(auxMessage)) {
			    errorMessage = aux_message_intl;
			}
		    }
		    JOptionPane.showMessageDialog(
			    (Component) PluginServices.getMainFrame(),
			    errorMessage,
			    PluginServices.getText(this, saveErrorTitleKey),
			    JOptionPane.ERROR_MESSAGE);
		    return false;
		}
	    }
	}
	return true;
    }

    /**
     * Goes to the next row of the data.
     * 
     */
    public void next() {
	if (showWarning()) {
	    try {
		if (onlySelectedCB.isSelected()) {
		    nextSelected();
		} else {
		    if (getPosition() < getRecordset().getRowCount()) {
			setPosition(getPosition() + 1);
		    }
		}
	    } catch (ReadDriverException e) {
		logger.error(e.getMessage(), e);
	    }
	}
    }

    /**
     * Goes to the next selected row of the data.
     */
    protected void nextSelected() {
	FBitSet bitset = getRecordset().getSelection();
	int currentPos = Long.valueOf(getPosition()).intValue();
	int pos = bitset.nextSetBit(currentPos + 1);
	if (pos != EMPTY_REGISTER) {
	    setPosition(pos);
	}
    }

    /**
     * Goes to the last row of the data.
     * 
     */
    public void last() {
	if (showWarning()) {
	    try {
		if (onlySelectedCB.isSelected()) {
		    lastSelected();
		} else {
		    setPosition(getRecordset().getRowCount() - 1);
		}
	    } catch (ReadDriverException e) {
		logger.error(e.getMessage(), e);
	    }
	}
    }

    /**
     * Goes to the last selected row of the data.
     * 
     */
    private void lastSelected() {
	FBitSet bitset = getRecordset().getSelection();
	int pos = bitset.length();
	if (pos != 0) {
	    setPosition(pos - 1);
	}
    }

    /**
     * Goes to the first row of the data.
     * 
     */
    public void first() {
	if (showWarning()) {
	    if (onlySelectedCB.isSelected()) {
		firstSelected();
	    } else {
		setPosition(0);
	    }
	}
    }

    /**
     * Goes to the first selected row of the data.
     * 
     */
    private void firstSelected() {
	FBitSet bitset = getRecordset().getSelection();
	int pos = bitset.nextSetBit(0);
	if (pos != EMPTY_REGISTER) {
	    setPosition(pos);
	}
    }

    /**
     * Goes to the previous row of the data.
     * 
     */
    public void before() {
	if (showWarning()) {
	    if (onlySelectedCB.isSelected()) {
		beforeSelected();
	    } else {
		if (getPosition() > 0) {
		    setPosition(getPosition() - 1);
		}
	    }
	}
    }

    /**
     * Goes to the previous selected row of the data.
     * 
     */
    private void beforeSelected() {
	FBitSet bitset = getRecordset().getSelection();
	int currentPos = Long.valueOf(getPosition()).intValue() - 1;
	int pos = currentPos;
	for (; pos >= 0 && !bitset.get(pos); pos--) {
	    ;
	}
	if (pos != EMPTY_REGISTER) {
	    setPosition(pos);
	}
    }

    /**
     * Zooms to the current feature. The feature will fill the visualization
     * area.
     * 
     */
    public void zoom() {
	Rectangle2D rectangle = null;
	int pos = Long.valueOf(getPosition()).intValue();
	if (layer instanceof AlphanumericData) {
	    try {
		IGeometry g;
		ReadableVectorial source = (layer).getSource();
		source.start();
		g = source.getShape(pos);
		source.stop();

		if (g != null) {
		    /*
		     * fix to avoid zoom problems when layer and view
		     * projections aren't the same.
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
			layer.getMapContext().getViewPort()
			.setExtent(rectangle);
		    }
		} else {
		    JOptionPane.showMessageDialog(this, PluginServices.getText(
			    this, "feature_has_no_geometry_to_zoom"));
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

    public void selectCurrentFeature() {
	FBitSet bitset = null;
	int pos = Long.valueOf(getPosition()).intValue();
	bitset = getRecordset().getSelection();
	if (!bitset.get(pos)) {
	    bitset.set(pos);
	} else {
	    bitset.clear(pos);
	    if (onlySelectedCB.isSelected()) {
		lastSelected();
	    }
	}
	getRecordset().setSelection(bitset);
    }

    @Deprecated
    public void selectFeature(long feature) {
	FBitSet bitset = null;
	int pos = Long.valueOf(feature).intValue();
	bitset = getRecordset().getSelection();
	if (!bitset.get(pos)) {
	    bitset.set(pos);
	}
	getRecordset().setSelection(bitset);
    }

    /**
     * 
     * @return true if the current row is selected, false if not.
     */
    private boolean isRecordSelected() {
	return isRecordSelected(getPosition());
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
	if (getRecordset() == null) {
	    return false;
	}
	bitset = getRecordset().getSelection();
	return bitset.get(pos);
    }

    @Deprecated
    public boolean isFeatureSelected(long feature) {
	return isRecordSelected(feature);
    }

    /**
     * Removes all selections of the layer.
     * 
     */
    public void clearSelection() {
	FBitSet bitset = null;
	if (layer instanceof AlphanumericData) {
	    bitset = getRecordset().getSelection();
	    bitset.clear();
	    getRecordset().setSelection(bitset);
	}
    }

    /**
     * Forces the application to navigate only between selected features.
     * 
     */
    private void viewOnlySelected() {
	if (getNumberOfRowsSelected() == 0) {
	    setPosition(EMPTY_REGISTER);
	}
	if (!isRecordSelected()) {
	    firstSelected();
	}
    }

    @Override
    public WindowInfo getWindowInfo() {
	if (windowInfo == null) {
	    windowInfo = new WindowInfo(WindowInfo.MODELESSDIALOG
		    | WindowInfo.PALETTE | WindowInfo.RESIZABLE);
	    
	    windowInfo.setTitle("NavTable: " + dataName);
	    Dimension dim = getPreferredSize();
	    // To calculate the maximum size of a form we take the size of the 
	    // main frame minus a "magic number" for the menus, toolbar, state bar
	    // Take into account that in edition mode there is less available space
	    MDIFrame a = (MDIFrame) PluginServices.getMainFrame();
	    final int MENU_TOOL_STATE_BAR = 205;
	    int maxHeight = a.getHeight() - MENU_TOOL_STATE_BAR;
	    int maxWidth = a.getWidth() - 15;

	    int width, heigth = 0;
	    if (dim.getHeight() > maxHeight) {
		heigth = maxHeight;
	    } else {
		heigth = new Double(dim.getHeight()).intValue();
	    }
	    if (dim.getWidth() > maxWidth) {
		width = maxWidth;
	    } else {
		width = new Double(dim.getWidth()).intValue();
	    }
	    
	    // getPreferredSize doesn't take into account the borders and other stuff
	    // introduced by Andami, neither scroll bars so we must increase the "preferred"
	    // dimensions
	    windowInfo.setWidth(width + 25);
	    windowInfo.setHeight(heigth + 15);
	}
	return windowInfo;
    }
    /**
     * Repaints the window.
     * 
     */
    public void refreshGUI() {
	boolean navEnabled = false;
	try {
	    if (getRecordset() == null) {
		return;
	    }

	    if (getPosition() == EMPTY_REGISTER) {
		posTF.setText("");
		navEnabled = false;
		fillEmptyValues();
	    } else {
		navEnabled = true;
		fillValues();
	    }

	    // north panel buttons
	    alwaysZoomCB.setEnabled(navEnabled);
	    alwaysSelectCB.setEnabled(navEnabled);
	    fixScaleCB.setEnabled(navEnabled);

	    if (isSomeRowToWorkOn()) {
		//we need to adapt a zero-based index (currentPosition)
		// to what user introduces (a 1-based index)
		posTF.setText(String.valueOf(getPosition() + 1));
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
	    setIconForFiltering();
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

    private void setIconForFiltering() {
	try {
	    if (layer.getRecordset().getSelection().isEmpty()) {
		ImageIcon imagenFilter = getIcon("/filter.png");
		filterB.setIcon(imagenFilter);
		filterB.setToolTipText(PluginServices.getText(this, "filterTooltip"));
	    } else {
		ImageIcon imagenRemoveFilter = getIcon("/nofilter.png");
		filterB.setIcon(imagenRemoveFilter);
		filterB.setToolTipText(PluginServices.getText(this, "noFilterTooltip"));
	    }
	} catch (ReadDriverException e) {
	    logger.error(e.getMessage(), e);
	}
    }

    private void setTotalLabelText() throws ReadDriverException {
	long numberOfRowsInRecordset = getRecordset().getRowCount();
	if (onlySelectedCB.isSelected()) {
	    totalLabel.setText("/" + "(" + getNumberOfRowsSelected() + ") "
		    + numberOfRowsInRecordset);
	} else {
	    totalLabel.setText("/" + numberOfRowsInRecordset);
	}
    }

    private void enableCopyPreviousButton(boolean navEnabled) {
	if (getPosition() == 0 || !navEnabled) {
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

    private void setIconAndPositionBackgroundForSelection() {
	if (isRecordSelected()) {
	    ImageIcon imagenUnselect = getIcon("/Unselect.png");
	    selectionB.setIcon(imagenUnselect);
	    posTF.setBackground(Color.YELLOW);
	} else {
	    ImageIcon imagenSelect = getIcon("/Select.png");
	    selectionB.setIcon(imagenSelect);
	    posTF.setBackground(Color.WHITE);
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
	FBitSet bitset = getRecordset().getSelection();
	return bitset.cardinality();
    }

    protected void posTFChanged() {
	String pos = posTF.getText();
	Long posNumber = null;
	try {
	    posNumber = new Long(pos);
	} catch (NumberFormatException e) {
	    logger.error(e.getMessage(), e);
	    posNumber = getPosition();
	} finally {
	    if (showWarning()) {
		// user will set a 1-based index to navigate through layer,
		// so we need to adapt it to currentPosition (a zero-based
		// index)
		setPosition(posNumber - 1);
	    }
	}
    }

    /**
     * {@link #init()} method must be called before this
     * 
     * @param newPosition zero-based index on recordset
     */
    public void setPosition(long newPosition) {
	if(!isValidPosition(newPosition)) {
	    return;
	}
	try {
	    if (newPosition >= getRecordset().getRowCount()) {
		newPosition = getRecordset().getRowCount() - 1;
	    } else if (newPosition < EMPTY_REGISTER) {
		newPosition = 0;
	    }
	    currentPosition = newPosition;
	    positionEventSource.fireEvent(new PositionEvent(this));
	} catch (ReadDriverException e) {
	    e.printStackTrace();
	}
    }

    public long getPosition() {
	return currentPosition;
    }

    public void copyPrevious() {
	long current = getPosition();
	currentPosition = current - 1;
	fillValues();
	currentPosition = current;
	setChangedValues(true);
	enableSaveButton(true);
    }

    public boolean copySelected() {
	if (getNumberOfRowsSelected() != 1) {
	    // show error
	    JOptionPane.showMessageDialog(null,
		    PluginServices.getText(this, "justOneRecordMessage"),
		    PluginServices.getText(this, "justOneRecordTitle"),
		    JOptionPane.WARNING_MESSAGE);
	    return false;
	} else {
	    long current = getPosition();
	    FBitSet selection = getRecordset().getSelection();
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
    @Override
    public void actionPerformed(ActionEvent e) {
	/*
	 * Variable isSomeNavTableForm open is used as workaround to control
	 * null pointers exceptions when all forms using navtable are closed
	 * but, for some strange reason, some of the listeners is still active.
	 */
	if (!isSomeNavTableFormOpen()) {
	    return;
	}

	if (getRecordset() == null) {
	    // If there is an error on the recordset of the layer 
	    // do nothing.
	    return;
	}
	if (e.getSource() == onlySelectedCB) {
	    if(alwaysSelectCB.isSelected()) {
		alwaysSelectCB.setSelected(false);
		getRecordset().addSelectionListener(this);
	    }
	    if (showWarning()) {
		if (onlySelectedCB.isSelected()) {
		    if (getPosition() != EMPTY_REGISTER) {
			viewOnlySelected();
		    }
		} else {
		    if (getPosition() == EMPTY_REGISTER) {
			setPosition(0);
		    }
		}
	    } else {
		onlySelectedCB.setSelected(false);
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
		getRecordset().removeSelectionListener(this);
	    } else {
		getRecordset().addSelectionListener(this);
	    }
	    refreshGUI();
	} else if (e.getSource() == filterB) {
	    try {
		if (layer.getRecordset().getSelection().isEmpty()) {
		    FiltroExtension fe = new FiltroExtension();
		    fe.initialize();
		    fe.setDatasource(getRecordset(), dataName);
		    fe.execute("FILTER_DATASOURCE");
		} else {
		    clearSelection();
		    }
		} catch (ReadDriverException exception) {
		    logger.error(exception.getMessage(), exception);
		    }
	} else if (e.getSource() == nextB) {
	    next();
	} else if (e.getSource() == lastB) {
	    last();
	} else if (e.getSource() == firstB) {
	    first();
	} else if (e.getSource() == beforeB) {
	    before();
	} else if (e.getSource() == posTF) {
	    posTFChanged();
	} else if (e.getSource() == copySelectedB) {
	    if (copySelected()) {
		setChangedValues(true);
		enableSaveButton(true);
	    }
	} else if (e.getSource() == copyPreviousB) {
	    copyPrevious();
	} else if (e.getSource() == zoomB) {
	    zoom();
	} else if (e.getSource() == selectionB) {
	    selectCurrentFeature();
	    refreshGUI();
	} else if (e.getSource() == saveB) {
	    try {
		if (saveRecord()) {
		    refreshGUI();
		} else {
		    JOptionPane.showMessageDialog(this,
			    PluginServices.getText(this, saveErrorGenericMessageKey),
			    "", JOptionPane.ERROR_MESSAGE);
		}
	    } catch (StopWriterVisitorException ex) {
		ex.printStackTrace();
		String errorMessage = (ex.getCause() != null) ? ex.getCause()
			.getMessage() : ex.getMessage(), auxMessage = errorMessage
			.replace("ERROR: ", "").replace(" ", "_")
			.replace("\n", ""), auxMessageIntl = PluginServices
			.getText(this, auxMessage);
		if (auxMessageIntl.compareToIgnoreCase(auxMessage) != 0) {
		    errorMessage = auxMessageIntl;
		}
		JOptionPane.showMessageDialog(
			(Component) PluginServices.getMainFrame(),
			errorMessage,
			PluginServices.getText(this, saveErrorTitleKey),
			JOptionPane.ERROR_MESSAGE);
	    }
	} else if (e.getSource() == removeB) {
	    int answer = JOptionPane.showConfirmDialog(null,
		    PluginServices.getText(null, deleteMessageKey),
		    null, JOptionPane.YES_NO_OPTION);
	    if (answer == 0) {
		try {
		deleteRecord();
		} catch (StopWriterVisitorException ex) {
		    ex.printStackTrace();
		    String errorMessage = (ex.getCause() != null) ? ex
			    .getCause().getMessage() : ex.getMessage(), auxMessage = errorMessage
			    .replace("ERROR: ", "").replace(" ", "_")
			    .replace("\n", ""), auxMessageIntl = PluginServices
			    .getText(this, auxMessage);
		    if (auxMessageIntl.compareToIgnoreCase(auxMessage) != 0) {
			errorMessage = auxMessageIntl;
		    }
		    JOptionPane.showMessageDialog(
			    (Component) PluginServices.getMainFrame(),
			    errorMessage,
			    PluginServices.getText(this, saveErrorTitleKey),
			    JOptionPane.ERROR_MESSAGE);
		}
	    }
	} else if (e.getSource() == undoB) {
	    undoAction();
	}
    }

    protected void undoAction() {
	fillValues();
	setChangedValues(false);
	refreshGUI();
    }

    public void deleteRecord() throws StopWriterVisitorException {
	try {
	    long position = getPosition();
	    layerController.delete(position);
	    // keep the current position within boundaries
	    setPosition(position);
	    if (layerController.getRowCount() <= 0) {
		showEmptyLayerMessage();
	    }
	} catch (StopWriterVisitorException e) {
	    throw e;
	} catch (BaseException e) {
	    logger.error(e.getMessage(), e.getCause());
	}
    }

    private boolean isValidPosition(Long pos) {
	if (pos == null) {
	    return false;
	}
	if (onlySelectedCB.isSelected()) {
	    return isRecordSelected(pos.longValue());
	}
	return true;
    }

    @Override
    public void selectionChanged(SelectionEvent e) {
	/*
	 * Variable isSomeNavTableForm open is used as workaround to control
	 * null pointers exceptions when all forms using navtable are closed
	 * but, for some strange reason, some of the listeners is still active.
	 */
	if (!isSomeNavTableFormOpen()) {
	    return;
	}

	if(onlySelectedCB.isSelected()
		&& !isRecordSelected()) {
	    first();
	}
	refreshGUI();
    }

    @Override
    public void windowClosed() {
	showWarning();
	getRecordset().removeSelectionListener(this);
	if (this.layer != null) {
	    this.layer.removeLayerListener(this.listener);
	}
	setOpenNavTableForm(false);
    }

    private boolean isSomeNavTableFormOpen() {
	return isSomeNavTableFormOpen;
    }

    protected void setOpenNavTableForm(boolean b) {
	isSomeNavTableFormOpen = b;
    }

    @Override
    public void windowActivated() {
    }

    /**
     * Reloads recordset from layer, if possible.
     * 
     * @throws ReadDriverException
     */
    public void reloadRecordset() throws ReadDriverException {
	getRecordset().reload();
    }

    public String getDataName() {
	return this.dataName;
    }

    public void addPositionListener(PositionListener l) {
	positionEventSource.addEventListener(l);
    }

    public void removePositionListener(PositionListener l) {
	positionEventSource.removeEventListener(l);
    }

    public abstract boolean isSavingValues();

    public SelectableDataSource getRecordset() {
        try {
            return layer.getSource().getRecordset();
        } catch (ReadDriverException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onPositionChange(PositionEvent e) {
	try {
	    layerController.read(getPosition());
	    refreshGUI();
	} catch (ReadDriverException rde) {
	    rde.printStackTrace();
	    layerController.clearAll();
	    refreshGUI();
	}
    }
}
