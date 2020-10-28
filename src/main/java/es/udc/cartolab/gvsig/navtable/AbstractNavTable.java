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

import static es.icarto.gvsig.commons.i18n.I18n._;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.gvsig.andami.PluginServices;
import org.gvsig.andami.ui.mdiFrame.MainFrame;
import org.gvsig.andami.ui.mdiManager.IWindowListener;
import org.gvsig.app.ApplicationLocator;
import org.gvsig.fmap.dal.exception.DataException;
import org.gvsig.fmap.dal.feature.Feature;
import org.gvsig.fmap.dal.feature.FeatureAttributeDescriptor;
import org.gvsig.fmap.dal.feature.FeatureSelection;
import org.gvsig.fmap.dal.feature.FeatureStore;
import org.gvsig.fmap.dal.feature.FeatureType;
import org.gvsig.fmap.geom.Geometry;
import org.gvsig.fmap.geom.primitive.Envelope;
import org.gvsig.fmap.mapcontext.MapContext;
import org.gvsig.fmap.mapcontext.layers.vectorial.FLyrVect;
import org.gvsig.tools.dataTypes.DataTypes;
import org.gvsig.tools.dispose.DisposableIterator;
import org.gvsig.tools.dispose.DisposeUtils;
import org.gvsig.tools.exception.BaseException;
import org.gvsig.utils.extensionPointsOld.ExtensionPoint;
import org.gvsig.utils.extensionPointsOld.ExtensionPoints;
import org.gvsig.utils.extensionPointsOld.ExtensionPointsSingleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.icarto.gvsig.commons.utils.Field;
import es.icarto.gvsig.navtable.actions.FilterButton;
import es.icarto.gvsig.navtable.navigation.NavigationHandler;
import es.udc.cartolab.gvsig.navtable.dataacces.IController;
import es.udc.cartolab.gvsig.navtable.dataacces.LayerController;
import es.udc.cartolab.gvsig.navtable.listeners.PositionEvent;
import es.udc.cartolab.gvsig.navtable.listeners.PositionListener;
import es.udc.cartolab.gvsig.navtable.preferences.Preferences;
import es.udc.cartolab.gvsig.navtable.utils.EditionListener;
import net.miginfocom.swing.MigLayout;

/**
 * <img src="images/NavTableWindow.png">
 *
 * If there is an image on
 * 'gvSIG/extensiones/es.udc.cartolab.gvsig.navtable/images/navtable_header.png'
 * it will be loaded on the NorthPanel.
 *
 */

@SuppressWarnings("serial")
public abstract class AbstractNavTable extends NTIWindow implements ActionListener, IWindowListener, PositionListener {

	private static final Logger logger = LoggerFactory.getLogger(AbstractNavTable.class);

	public static final long EMPTY_REGISTER = Long.MIN_VALUE;
	protected static final int BUTTON_REMOVE = 0;
	protected static final int BUTTON_SAVE = 1;
	protected static final int BUTTON_SELECTION = 2;
	protected static final int BUTTON_ZOOM = 3;
	protected static final int BUTTON_COPY_PREVIOUS = 4;
	protected static final int BUTTON_COPY_SELECTED = 5;
	protected String deleteMessageKey = "confirm_delete_register";
	protected String saveErrorTitleKey = "save_layer_error";
	protected String saveErrorGenericMessageKey = "errorSavingData";
	public static final String NAVTABLE_ACTIONS_TOOLBAR = "navtable_extension_point_actions_toolbar";
	public static final String NAVTABLE_CONTEXT_MENU = "navtable_extension_point_context_menu";

	protected JPanel northPanel = null;
	protected JPanel centerPanel = null;
	protected JPanel southPanel = null;

	protected IController layerController;
	protected NavigationHandler navigation;

	protected FLyrVect layer = null;

	protected boolean changedValues = false;

	protected JCheckBox fixScaleCB = null;
	protected JCheckBox alwaysZoomCB = null;

	protected JButton copyPreviousB = null;
	protected JButton copySelectedB = null;
	protected JButton zoomB = null;
	protected JButton saveB = null;
	protected JButton removeB = null;
	protected JButton undoB = null;
	private final FilterButton filterAction;

	protected EditionListener listener;

	private JPanel actionsToolBar;
	private JPanel optionsPanel;

	protected boolean openEmptyLayers = false;

	private boolean isSavingValues = false;

	public AbstractNavTable(FLyrVect layer) {
		super();
		this.layer = layer;
		filterAction = new FilterButton(layer);
		layerController = new LayerController(this.layer);
		navigation = new NavigationHandler(this);
	}

	public boolean init() {

		if ((!openEmptyLayers) && (navigation.getLastPos() < 0)) {
			showEmptyLayerMessage();
			return false;
		}

		initGUI();
		initWidgets();
		setLayerListeners();
		navigation.setSortKeys(null);
		super.repaint();
		super.setVisible(true);
		setFocusCycleRoot(true);

		return true;
	}

	protected void initGUI() {
		MigLayout thisLayout = new MigLayout("inset 0, align center", "[grow]", "[][grow][]");
		this.setLayout(thisLayout);
		this.add(getNorthPanel(), "shrink, wrap, align center");
		this.add(getCenterPanel(), "shrink, growx, growy, wrap");
		this.add(getSouthPanel(), "shrink, align center");
	}

	/**
	 * In NavTable it will get the attribute names from the layer and set it on the
	 * left column of the table. On AbstractForm it will initialize the widget
	 * vector from the Abeille file
	 */
	protected abstract void initWidgets();

	public void refreshGUI() {
		boolean navEnabled = false;

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

		filterAction.refreshGUI();
		enableSaveButton(navEnabled);
		removeB.setEnabled(navEnabled);
		navigation.refreshGUI(navEnabled);
	}

	/**
	 * It fills NavTable with empty values. Used when "Selected" option is set on,
	 * but there are any selection registers.
	 *
	 */
	public abstract void fillEmptyValues();

	/**
	 * It shows the values of a data row in the main panel.
	 */
	public abstract void fillValues();

	private boolean isSomeRowToWorkOn() {
		if (isOnlySelected() && getNumberOfRowsSelected() == 0) {
			return false;
		} else {
			return true;
		}
	}

	public void resetListeners() {
		removeLayerListeners();
		setLayerListeners();
	}

	public void reinit() {
		resetListeners();
	}

	protected void setLayerListeners() {
		listener = new EditionListener(this);
		layer.addLayerListener(listener);
		navigation.setListeners();
		addPositionListener(this);
	}

	protected void removeLayerListeners() {
		layer.removeLayerListener(listener);
		navigation.removeListeners();
		removePositionListener(this);
	}

	public void showEmptyLayerMessage() {

		if ((!openEmptyLayers)) {

			MainFrame frame = ApplicationLocator.getManager().getMainFrame();
			JOptionPane.showMessageDialog((Component) frame, _("emptyLayer"), "", JOptionPane.ERROR_MESSAGE);
		}
	}

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
		File folder = PluginServices.getPluginServices(this).getPluginDirectory();
		File header = new File(
				folder.getAbsolutePath() + File.separator + "images" + File.separator + "navtable_header.png");

		if (!header.exists()) {
			header = new File(Preferences.getConfigDir() + "/navtable_header.png");
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
		java.net.URL imgURL = getClass().getResource("/images/" + iconName);
		if (imgURL == null) {
			imgURL = AbstractNavTable.class.getResource("/images/" + iconName);
		}

		ImageIcon icon = new ImageIcon(imgURL);
		return icon;
	}

	// Probably should be removed and use a factory instead
	// is duplicated with NavigationHandler
	private JButton getNavTableButton(JButton button, String iconName, String toolTipName) {
		JButton but = new JButton(getIcon(iconName));
		but.setToolTipText(_(toolTipName));
		but.addActionListener(this);
		return but;
	}

	// Probably should be removed and use a factory instead
	// is duplicated with NavigationHandler
	private JCheckBox getNavTableCheckBox(JCheckBox cb, String toolTipName) {
		cb = new JCheckBox(_(toolTipName));
		cb.addActionListener(this);
		return cb;
	}

	public JPanel getActionsToolBar() {
		if (actionsToolBar == null) {
			actionsToolBar = new JPanel(new FlowLayout());
			registerNavTableButtonsOnActionToolBarExtensionPoint();
			ExtensionPoint actionToolBarEP = (ExtensionPoint) ExtensionPointsSingleton.getInstance()
					.get(NAVTABLE_ACTIONS_TOOLBAR);
			actionsToolBar.add(filterAction.filterB);
			for (Object button : actionToolBarEP.values()) {
				actionsToolBar.add((JButton) button);
			}
		}
		return actionsToolBar;
	}

	protected void registerNavTableButtonsOnActionToolBarExtensionPoint() {
		ExtensionPoints extensionPoints = ExtensionPointsSingleton.getInstance();

		copySelectedB = getNavTableButton(copySelectedB, "/copy-selected.png", "copySelectedButtonTooltip");
		extensionPoints.add(NAVTABLE_ACTIONS_TOOLBAR, "button-copy-selected", copySelectedB);

		copyPreviousB = getNavTableButton(copyPreviousB, "/copy.png", "copyPreviousButtonTooltip");
		extensionPoints.add(NAVTABLE_ACTIONS_TOOLBAR, "button-copy-previous", copyPreviousB);

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

		undoB = getNavTableButton(undoB, "/edit-undo.png", "clearChangesButtonTooltip");
		undoB.setEnabled(false);
		extensionPoints.add(NAVTABLE_ACTIONS_TOOLBAR, "button-clear-changes", undoB);
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
	 * Zooms to the current feature. The feature will fill the visualization area.
	 *
	 */
	public void zoom() {
		Geometry geometry = navigation.getFeature().getDefaultGeometry();

		if (geometry != null) {
			/*
			 * fix to avoid zoom problems when layer and view projections aren't the same.
			 */
			if (layer.getCoordTrans() != null) {
				geometry.reProject(layer.getCoordTrans());
			}
			Envelope envelope = geometry.getEnvelope();
			if (envelope.getLength(0) < 200) {
				// TODO. En lugar de 500 debería ser un parámetro configurable.
				// Y dado que son unidades del mapa cuando trabajemos con grados
				// esto no va a funcionar
				try {
					Geometry buffer = geometry.buffer(500);
					envelope.add(buffer);
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}

			if (envelope != null) {
				MapContext mapContext = layer.getMapContext();
				mapContext.zoomToEnvelope(envelope);
			}
		} else {
			JOptionPane.showMessageDialog(this, _("feature_has_no_geometry_to_zoom"));
		}
	}

	/**
	 * It forces the application to use the current scale when navigating between
	 * features. It also centers the visualization to the feature when navigating.
	 *
	 */
	private void fixScale() {
		long scale = layer.getMapContext().getScaleView();
		zoom();
		layer.getMapContext().setScaleView(scale);
	}

	public void selectCurrentFeature() {
		FeatureStore store = layer.getFeatureStore();
		try {
			FeatureSelection selection = store.getFeatureSelection();
			if (selection.isSelected(navigation.getFeature())) {
				// Antes si se deseleccionaba una feature se iba a la anterior
				// seleccionada. Ahora se delega en el comportamiento de
				// NavigationHandler que cuando hay una actualización de la
				// selección y onlySelected está seleccionado va al primero
				selection.deselect(navigation.getFeature());
			} else {
				selection.select(navigation.getFeature());
			}
		} catch (DataException e) {
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * Removes all selections of the layer.
	 *
	 */
	public void clearSelection() {

		try {
			FeatureStore store = layer.getFeatureStore();
			FeatureSelection selection = store.getFeatureSelection();
			selection.deselectAll();
		} catch (DataException e) {
			logger.error(e.getMessage(), e);
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

	public long getNumberOfRowsSelected() {
		try {

			FeatureStore store = layer.getFeatureStore();
			FeatureSelection selection = store.getFeatureSelection();
			return selection.getSize();
		} catch (DataException e) {
			logger.error(e.getMessage(), e);
		}
		return 0;
	}

	public void copyPrevious() {
		Feature f = navigation.getFeature(getPosition() - 1);
		copyFrom(f);
	}

	public boolean copySelected() {
		if (getNumberOfRowsSelected() != 1) {
			// show error
			JOptionPane.showMessageDialog(null, _("justOneRecordMessage"), _("justOneRecordTitle"),
					JOptionPane.WARNING_MESSAGE);
			return false;
		}

		DisposableIterator it = null;

		try {
			FeatureSelection sel = layer.getFeatureStore().getFeatureSelection();
			it = sel.fastIterator();
			Feature feat = (Feature) it.next();
			copyFrom(feat);
			return true;
		} catch (DataException e) {
			logger.error(e.getMessage(), e);
		} finally {
			DisposeUtils.disposeQuietly(it);
		}
		return false;
	}

	private void copyFrom(Feature feat) {
		layerController.read(feat);
		fillValues();
		Map<String, String> newValues = new HashMap<String, String>();
		newValues.putAll(layerController.getValuesOriginal());
		layerController.read(navigation.getFeature());
		for (String attName : newValues.keySet()) {
			layerController.setValue(attName, newValues.get(attName));
		}
		setChangedValues(true);
		enableSaveButton(true);
	}

	/**
	 * Handles the user actions.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == alwaysZoomCB) {
			fixScaleCB.setSelected(false);
			refreshGUI();
		} else if (e.getSource() == fixScaleCB) {
			alwaysZoomCB.setSelected(false);
			refreshGUI();
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
			int answer = JOptionPane.showConfirmDialog(null, _(deleteMessageKey), null, JOptionPane.YES_NO_OPTION);
			if (answer == 0) {
				try {
					deleteRecord();
				} catch (BaseException ex) {
					logger.error(ex.getMessage(), ex);
					String errorMessage = (ex.getCause() != null) ? ex.getCause().getMessage() : ex.getMessage(),
							auxMessage = errorMessage.replace("ERROR: ", "").replace(" ", "_").replace("\n", ""),
							auxMessageIntl = _(auxMessage);
					if (auxMessageIntl.compareToIgnoreCase(auxMessage) != 0) {
						errorMessage = auxMessageIntl;
					}
					JOptionPane.showMessageDialog((Component) PluginServices.getMainFrame(), errorMessage,
							_(saveErrorTitleKey), JOptionPane.ERROR_MESSAGE);
				}
			}
		} else if (e.getSource() == undoB) {
			undoAction();
		}
	}

	private boolean tryToSave() {
		try {
			if (saveRecord()) {
				refreshGUI();
			} else {
				JOptionPane.showMessageDialog(this, _(saveErrorGenericMessageKey), "", JOptionPane.ERROR_MESSAGE);
			}
		} catch (DataException ex) {
			logger.error(ex.getMessage(), ex);
			String errorMessage = (ex.getCause() != null) ? ex.getCause().getMessage() : ex.getMessage(),
					auxMessage = errorMessage.replace("ERROR: ", "").replace(" ", "_").replace("\n", ""),
					auxMessageIntl = _(auxMessage);
			if (auxMessageIntl.compareToIgnoreCase(auxMessage) != 0) {
				errorMessage = auxMessageIntl;
			}
			JOptionPane.showMessageDialog((Component) PluginServices.getMainFrame(), errorMessage, _(saveErrorTitleKey),
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

	public void deleteRecord() throws BaseException {
		setSavingValues(true);
		try {
			navigation.deleteFeature();
			if (navigation.getLastPos() < 0) {
				showEmptyLayerMessage();
			}
		} catch (BaseException e) {
			throw e;
		} finally {
			setSavingValues(false);
		}
	}

	@Override
	public void windowClosed() {
		showWarning();
		removeLayerListeners();
		navigation.removeListeners();
	}

	@Override
	public void windowActivated() {
	}

	public void reloadRecordset() throws DataException {
		layer.getFeatureStore().refresh();
	}

	public void addPositionListener(PositionListener l) {
		navigation.addEventListener(l);
	}

	public void removePositionListener(PositionListener l) {
		navigation.removeEventListener(l);
	}

	@Override
	public void onPositionChange(PositionEvent e) {
		layerController.read(navigation.getFeature());
		refreshGUI();
	}

	@Override
	public void beforePositionChange(PositionEvent e) {
		if (!isSavingValues()) {
			showWarning();
		}
	}

	/**
	 * Shows a warning to the user if there's unsaved data.
	 */
	protected boolean showWarning() {
		if (getPosition() == EMPTY_REGISTER) {
			return true;
		}
		if (isChangedValues()) {
			boolean save = false;
			Object[] options = { _("saveButtonTooltip"), _("ignoreButton") };
			int response = JOptionPane.showOptionDialog(this, _("unsavedDataMessage"), _("unsavedDataTitle"),
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, // do not use a custom Icon
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

	public void setPosition(long newPosition) {
		navigation.setPosition(newPosition);
	}

	public long getPosition() {
		return navigation.getPosition();
	}

	public void setSortKeys(List<Field> sortFields) {
		navigation.setSortKeys(sortFields);
	}

	public void editionStarted() {
	}

	public void editionFinished() {
		try {
			reloadRecordset();
		} catch (DataException e) {
			logger.error(e.getMessage(), e);
		}
		navigation.modelChanged();
	}

	public FLyrVect getLayer() {
		return layer;
	}

	public int getFieldType(String fieldName) {
		try {
			FeatureType type = layer.getFeatureStore().getDefaultFeatureType();
			FeatureAttributeDescriptor attDesc = type.getAttributeDescriptor(fieldName);
			return attDesc.getType();
			// layerController.getType(fieldName);
		} catch (DataException e) {
			logger.error(e.getMessage(), e);
		}
		return DataTypes.INVALID;
	}

	protected void setSavingValues(boolean bool) {
		isSavingValues = bool;
	}

	public boolean isSavingValues() {
		return isSavingValues;
	}

}
