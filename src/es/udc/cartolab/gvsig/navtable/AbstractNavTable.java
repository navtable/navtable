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
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.RowSorter.SortKey;

import net.miginfocom.swing.MigLayout;

import org.gvsig.andami.PluginServices;
import org.gvsig.andami.ui.mdiFrame.MDIFrame;
import org.gvsig.andami.ui.mdiManager.IWindow;
import org.gvsig.andami.ui.mdiManager.IWindowListener;
import org.gvsig.andami.ui.mdiManager.WindowInfo;
import org.gvsig.app.extension.SelectByAttributesExtension;
import org.gvsig.fmap.dal.exception.DataException;
import org.gvsig.fmap.dal.feature.FeatureStoreNotification;
import org.gvsig.fmap.geom.Geometry;
import org.gvsig.fmap.geom.primitive.Envelope;
import org.gvsig.fmap.mapcontext.layers.LayerEvent;
import org.gvsig.fmap.mapcontext.layers.vectorial.FLyrVect;
import org.gvsig.fmap.mapcontext.layers.vectorial.VectorLayer;
import org.gvsig.tools.exception.BaseException;
import org.gvsig.utils.extensionPointsOld.ExtensionPoint;
import org.gvsig.utils.extensionPointsOld.ExtensionPoints;
import org.gvsig.utils.extensionPointsOld.ExtensionPointsSingleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.icarto.gvsig.navtable.gvsig2.FBitSet;
import es.icarto.gvsig.navtable.gvsig2.SelectByAttributes;
import es.icarto.gvsig.navtable.gvsig2.SelectableDataSource;
import es.icarto.gvsig.navtable.navigation.NavigationHandler;
import es.udc.cartolab.gvsig.navtable.dataacces.IController;
import es.udc.cartolab.gvsig.navtable.dataacces.LayerController;
import es.udc.cartolab.gvsig.navtable.listeners.PositionEvent;
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
	ActionListener, IWindowListener, PositionListener {

private static final Logger logger = LoggerFactory
		.getLogger(AbstractNavTable.class);

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
    public static final String NAVTABLE_ACTIONS_TOOLBAR = "navtable_extension_point_actions_toolbar";
    public static final String NAVTABLE_CONTEXT_MENU = "navtable_extension_point_context_menu";

    protected JPanel northPanel = null;
    protected JPanel centerPanel = null;
    protected JPanel southPanel = null;

    protected IController layerController;
    private NavigationHandler navigation;

    protected FLyrVect layer = null;
    protected String dataName = "";

    protected boolean changedValues = false;

    // NORTH
    protected JCheckBox fixScaleCB = null;
    protected JCheckBox alwaysZoomCB = null;

    // SOUTH
    // actions buttons
    protected JButton filterB = null;
    protected JButton copyPreviousB = null;
    protected JButton copySelectedB = null;
    protected JButton zoomB = null;
    protected JButton saveB = null;
    protected JButton removeB = null;
    protected JButton undoB = null;

    // private boolean isSomeNavTableFormOpen = false;

    protected EditionListener listener;

    private JPanel actionsToolBar;
    private JPanel optionsPanel;

    protected boolean openEmptyLayers = false;
    protected boolean isAlphanumericNT = false;

    protected WindowInfo windowInfo = null;

    public AbstractNavTable(FLyrVect layer) {
	super();
	this.layer = layer;
	this.dataName = layer.getName();
	navigation = new NavigationHandler(this);
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

    public boolean isAlphanumericNT() {
	return this.isAlphanumericNT;
    }

    public boolean init() {

	try {
	    if ((!openEmptyLayers) && (getRecordset().getRowCount() <= 0)) {
		showEmptyLayerMessage();
		return false;
	    }
	} catch (DataException e) {
	    logger.error(e.getMessage(), e);
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
	// setOpenNavTableForm(true);
	setFocusCycleRoot(true);

	setLayerListeners();
	return true;
    }

    /**
     * In NavTable it will get the attribute names from the layer and set it on
     * the left column of the table. On AbstractForm it will initialize the
     * widget vector from the Abeille file
     */
    protected abstract void initWidgets();

    protected void initGUI() {
	MigLayout thisLayout = new MigLayout("inset 0, align center", "[grow]",
		"[][grow][]");
	this.setLayout(thisLayout);
	this.add(getNorthPanel(), "shrink, wrap, align center");
	this.add(getCenterPanel(), "shrink, growx, growy, wrap");
	this.add(getSouthPanel(), "shrink, align center");
    }

    protected boolean initController() {
	try {
	    layerController = new LayerController(this.layer);
	    layerController.read(getPosition());
	} catch (DataException e) {
	    logger.error(e.getMessage(), e);
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
	// setOpenNavTableForm(true);
    }

    protected void setLayerListeners() {
	if (layer != null) {
	    // TODO. Alphanumeric navtable adds the listener to the model
	    listener = new EditionListener(this, layer);
	    layer.addLayerListener(listener);
	}
	navigation.setListeners();
	addPositionListener(this);
    }

    protected void removeLayerListeners() {
	if (layer != null) {
	    layer.removeLayerListener(listener);
	}
	navigation.removeListeners();
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

    public abstract boolean saveRecord() throws DataException;

    protected void enableSaveButton(boolean bool) {
	if (!isChangedValues()) {
	    saveB.setEnabled(false);
	} else {
	    saveB.setEnabled(bool);
	}
    }

    public void setOnlySelected(boolean bool) {
	navigation.setOnlySelected(bool);
    }

    public boolean isOnlySelected() {
	return navigation.isOnlySelected();
    }

    private void initNorthPanelButtons() {
	// alwaysSelectCB and onlySelectedCB are init in SelectionHandler
	alwaysZoomCB = getNavTableCheckBox(alwaysZoomCB, "alwaysZoomCheckBox");
	fixScaleCB = getNavTableCheckBox(fixScaleCB, "fixedScaleCheckBox");
    }

    private JPanel getOptionsPanel() {
	if (optionsPanel == null) {
	    optionsPanel = new JPanel(new FlowLayout());
	    optionsPanel.add(navigation.getOptionsPanel());
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
	File folder = PluginServices.getPluginServices(this)
		.getPluginDirectory();
	File header = new File(folder.getAbsolutePath() + File.separator
		+ "images" + File.separator + "navtable_header.png");

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

    // Probably should be removed and use a factory instead
    // is duplicated with NavigationHandler
    private JButton getNavTableButton(JButton button, String iconName,
	    String toolTipName) {
	JButton but = new JButton(getIcon(iconName));
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

	// TODO. This is a hack. It's more logic that navigation registers the
	// bt
	// itself. But as NavTable overrides this methods, and cleans the list
	// it can not be done
	navigation.registerNavTableButtonsOnActionToolBarExtensionPoint();

	saveB = getNavTableButton(saveB, "/save.png", "saveButtonTooltip");
	saveB.setEnabled(false);
	extensionPoints.add(NAVTABLE_ACTIONS_TOOLBAR, "button-save", saveB);

	removeB = getNavTableButton(removeB, "/delete.png", "delete_register");
	extensionPoints.add(NAVTABLE_ACTIONS_TOOLBAR, "button-remove", removeB);

	undoB = getNavTableButton(undoB, "/edit-undo.png",
		"clearChangesButtonTooltip");
	undoB.setEnabled(false);
	extensionPoints.add(NAVTABLE_ACTIONS_TOOLBAR, "button-clear-changes",
		undoB);
    }

    protected JPanel getSouthPanel() {
	if (southPanel == null) {
	    southPanel = new JPanel(new BorderLayout());
	    southPanel.add(navigation.getToolBar(), BorderLayout.SOUTH);
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
		// The values will be restored as they are
		// when filling again the table/form,
		// so it's not need to revert the changes done.
	    }
	    if (save) {
		return tryToSave();
	    }
	}
	return true;
    }

    /**
     * Zooms to the current feature. The feature will fill the visualization
     * area.
     * 
     */
    public void zoom() {
	
	if (layer instanceof VectorLayer) {
	    
		Geometry geometry = getRecordset().getGeometry(getPosition());

		if (geometry != null) {
		    /*
		     * fix to avoid zoom problems when layer and view
		     * projections aren't the same.
		     */
		    if (layer.getCoordTrans() != null) {
		    	geometry.reProject(layer.getCoordTrans());
		    }
		    Envelope envelope = geometry.getEnvelope();
		    if (envelope.getLength(0) < 200) {
//		    	rectangle.setFrameFromCenter(rectangle.getCenterX(),
//						rectangle.getCenterY(),
//						rectangle.getCenterX() + 100,
//						rectangle.getCenterY() + 100);
		    	
		    }
		    
		    if (envelope != null) {
			layer.getMapContext().getViewPort().setEnvelope(envelope);
		    }
		} else {
		    JOptionPane.showMessageDialog(this, PluginServices.getText(
			    this, "feature_has_no_geometry_to_zoom"));
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
	    if (isOnlySelected()) {
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
     * Removes all selections of the layer.
     * 
     */
    public void clearSelection() {
	getRecordset().clearSelection();
    }

    @Override
    public WindowInfo getWindowInfo() {
	if (windowInfo == null) {
	    windowInfo = new WindowInfo(WindowInfo.MODELESSDIALOG
		    | WindowInfo.PALETTE | WindowInfo.RESIZABLE);

	    windowInfo.setTitle("NavTable: " + dataName);
	    Dimension dim = getPreferredSize();
	    // To calculate the maximum size of a form we take the size of the
	    // main frame minus a "magic number" for the menus, toolbar, state
	    // bar
	    // Take into account that in edition mode there is less available
	    // space
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

	    // getPreferredSize doesn't take into account the borders and other
	    // stuff
	    // introduced by Andami, neither scroll bars so we must increase the
	    // "preferred"
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
	if (getRecordset() == null) {
	    return;
	}

	if (navigation.isEmptyRegister()) {
	    navEnabled = false;
	    fillEmptyValues();
	} else {
	    navEnabled = true;
	    fillValues();
	}

	// north panel buttons
	alwaysZoomCB.setEnabled(navEnabled);

	fixScaleCB.setEnabled(navEnabled);

	if (isSomeRowToWorkOn()) {
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
	}

	// south panel option buttons
	enableCopySelectedButton(navEnabled);
	enableCopyPreviousButton(navEnabled);
	zoomB.setEnabled(navEnabled);

	setIconForFiltering();
	enableSaveButton(navEnabled);
	removeB.setEnabled(navEnabled);
	navigation.refreshGUI(navEnabled);
    }

    private void setIconForFiltering() {
	if (getRecordset().getSelection().isEmpty()) {
	    ImageIcon imagenFilter = getIcon("/filter.png");
	    filterB.setIcon(imagenFilter);
	    filterB.setToolTipText(PluginServices
		    .getText(this, "filterTooltip"));
	} else {
	    ImageIcon imagenRemoveFilter = getIcon("/nofilter.png");
	    filterB.setIcon(imagenRemoveFilter);
	    filterB.setToolTipText(PluginServices.getText(this,
		    "noFilterTooltip"));
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

    private boolean isSomeRowToWorkOn() {
	if (isOnlySelected() && getNumberOfRowsSelected() == 0) {
	    return false;
	} else {
	    return true;
	}
    }

    // TODO. Visibility changed after the refactor to implement the sorter
    public int getNumberOfRowsSelected() {
	FBitSet bitset = getRecordset().getSelection();
	return bitset.cardinality();
    }

    public void copyPrevious() {
	long current = navigation.getPosition();
	navigation.setPosition(navigation.getPreviousPositionInModel(), false);
	fillValues();
	navigation.setPosition(current, false);
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
	    navigation.setPosition(selectedRow, false);
	    fillValues();
	    navigation.setPosition(current, false);
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
	// if (!isSomeNavTableFormOpen()) {
	// return;
	// }

	if (getRecordset() == null) {
	    // If there is an error on the recordset of the layer
	    // do nothing.
	    return;
	}
	if (e.getSource() == alwaysZoomCB) {
	    fixScaleCB.setSelected(false);
	    refreshGUI();
	} else if (e.getSource() == fixScaleCB) {
	    alwaysZoomCB.setSelected(false);
	    refreshGUI();
	} else if (e.getSource() == filterB) {
	    filterButtonClicked();
	} else if (e.getSource() == copySelectedB) {
	    if (copySelected()) {
		setChangedValues(true);
		enableSaveButton(true);
	    }
	} else if (e.getSource() == copyPreviousB) {
	    copyPrevious();
	} else if (e.getSource() == zoomB) {
	    zoom();
	} else if (e.getSource() == saveB) {
	    tryToSave();
	} else if (e.getSource() == removeB) {
	    int answer = JOptionPane.showConfirmDialog(null,
		    PluginServices.getText(null, deleteMessageKey), null,
		    JOptionPane.YES_NO_OPTION);
	    if (answer == 0) {
		try {
		    deleteRecord();
		} catch (DataException ex) {
		    logger.error(ex.getMessage(), ex);
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

    private void filterButtonClicked() {
	if (getRecordset().getSelection().isEmpty()) {
	    SelectByAttributes fe = new SelectByAttributes();
	    fe.setDatasource(layer.getFeatureStore(), layer.getName());
	    fe.execute();
	} else {
	    clearSelection();
	}
    }

    private boolean tryToSave() {
	try {
	    if (saveRecord()) {
		refreshGUI();
	    } else {
		JOptionPane.showMessageDialog(this, PluginServices.getText(
			this, saveErrorGenericMessageKey), "",
			JOptionPane.ERROR_MESSAGE);
	    }
	} catch (DataException ex) {
	    logger.error(ex.getMessage(), ex);
	    String errorMessage = (ex.getCause() != null) ? ex.getCause()
		    .getMessage() : ex.getMessage(), auxMessage = errorMessage
		    .replace("ERROR: ", "").replace(" ", "_").replace("\n", ""), auxMessageIntl = PluginServices
		    .getText(this, auxMessage);
	    if (auxMessageIntl.compareToIgnoreCase(auxMessage) != 0) {
		errorMessage = auxMessageIntl;
	    }
	    JOptionPane.showMessageDialog(
		    (Component) PluginServices.getMainFrame(), errorMessage,
		    PluginServices.getText(this, saveErrorTitleKey),
		    JOptionPane.ERROR_MESSAGE);
	    return false;
	}
	return true;
    }

    protected void undoAction() {
	fillValues();
	setChangedValues(false);
	refreshGUI();
    }

    public void deleteRecord() throws DataException {
	try {
	    long position = getPosition();
	    layerController.delete(position);
	    // keep the current position within boundaries
	    setPosition(position);
	    if (layerController.getRowCount() <= 0) {
		showEmptyLayerMessage();
	    }
	} catch (DataException e) {
	    throw e;
	}
    }

    @Override
    public void windowClosed() {
	showWarning();
	removeLayerListeners();
	// setOpenNavTableForm(false);
    }

    // private boolean isSomeNavTableFormOpen() {
    // return isSomeNavTableFormOpen;
    // }
    //
    // protected void setOpenNavTableForm(boolean b) {
    // isSomeNavTableFormOpen = b;
    // }

    @Override
    public void windowActivated() {
    }

    /**
     * Reloads recordset from layer, if possible.
     * 
     * @throws ReadDriverException
     */
    public void reloadRecordset() throws DataException {
	getRecordset().reload();
    }

    public String getDataName() {
	return this.dataName;
    }

    public void addPositionListener(PositionListener l) {
	navigation.addEventListener(l);
    }

    public void removePositionListener(PositionListener l) {
	navigation.removeEventListener(l);
    }

    public abstract boolean isSavingValues();

    public SelectableDataSource getRecordset() {
	try {
		return new SelectableDataSource(layer.getFeatureStore());
	} catch (DataException e) {
	    logger.error(e.getMessage(), e);
	    return null;
	}
    }

    @Override
    public void onPositionChange(PositionEvent e) {
	try {
	    layerController.read(getPosition());
	    refreshGUI();
	} catch (DataException rde) {
	    logger.error(rde.getMessage(), e);
	    layerController.clearAll();
	    refreshGUI();
	}
    }

    @Override
    public void beforePositionChange(PositionEvent e) {
	showWarning();
	// TODO. If showWarning returns false, the record was not correctly
	// saved so we should not move to the next position. Or if is a movement
	// for other reason like click onlySelected, the click should be avoided
    }

    public void next() {
	navigation.next();
    }

    public void last() {
	navigation.last();
    }

    private void lastSelected() {
	navigation.lastSelected();
    }

    public void first() {
	navigation.first();
    }

    // TODO. Visibility changed after the refactor to implement the sorter
    public void firstSelected() {
	navigation.firstSelected();
    }

    public void before() {
	navigation.goToPreviousInView();
    }

    public void setPosition(long newPosition) {
	navigation.setPosition(newPosition);
    }

    public long getPosition() {
	return navigation.getPosition();
    }

    public void setSortKeys(List<? extends SortKey> keys) {
	navigation.setSortKeys(keys);
    }

    public List<? extends SortKey> getSortKeys() {
	return navigation.getSortKeys();
    }

    /**
     * Only process stop edition events. And when this occurs all the sort is
     * recalculated. In case that recalculate the full sorting has bad
     * performance we should process FIELD_EDITION, ROW_EDITION EditionEvents,
     * and reorder only the affected rows, or test the performance of not create
     * a new instance of the sorter.
     */
    public void layerEvent(LayerEvent e) {
	if ((e.getEventType() == LayerEvent.EDITION_CHANGED)
		&& !layer.isEditing()) {
	    navigation.modelChanged();
	}
    }

    public void editionEvent(FeatureStoreNotification e) {
//	if ((e instanceof AfterFieldEditEvent)
//		&& (e.getChangeType() == EditionEvent.CHANGE_TYPE_DELETE)) {
//	    navigation.setSortKeys(null);
//	}
    }

	public FLyrVect getLayer() {
		return layer;
	}

}
