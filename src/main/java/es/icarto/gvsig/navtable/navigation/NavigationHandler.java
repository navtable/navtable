package es.icarto.gvsig.navtable.navigation;

import static es.icarto.gvsig.commons.i18n.I18n._;
import static es.udc.cartolab.gvsig.navtable.AbstractNavTable.EMPTY_REGISTER;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SortOrder;

import org.gvsig.fmap.dal.DALLocator;
import org.gvsig.fmap.dal.DataManager;
import org.gvsig.fmap.dal.DataStoreNotification;
import org.gvsig.fmap.dal.exception.DataException;
import org.gvsig.fmap.dal.feature.Feature;
import org.gvsig.fmap.dal.feature.FeatureAttributeDescriptor;
import org.gvsig.fmap.dal.feature.FeatureQuery;
import org.gvsig.fmap.dal.feature.FeatureQueryOrder;
import org.gvsig.fmap.dal.feature.FeatureSelection;
import org.gvsig.fmap.dal.feature.FeatureStore;
import org.gvsig.fmap.dal.feature.paging.FeaturePagingHelper;
import org.gvsig.tools.exception.BaseException;
import org.gvsig.tools.observer.Observable;
import org.gvsig.tools.observer.Observer;
import org.gvsig.utils.extensionPointsOld.ExtensionPoints;
import org.gvsig.utils.extensionPointsOld.ExtensionPointsSingleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.icarto.gvsig.commons.utils.Field;
import es.udc.cartolab.gvsig.navtable.AbstractNavTable;
import es.udc.cartolab.gvsig.navtable.dataacces.EmptyFeature;
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

	private long currentPosition;
	private Feature currentFeature;

	private final AbstractNavTable nt;

	private FeaturePagingHelper set;

	private long lastPos;

	public NavigationHandler(AbstractNavTable nt) {
		this.nt = nt;
		initNavigationWidgets();
		initSelectionWidgets();
		FeatureStore store = nt.getLayer().getFeatureStore();
		try {
			lastPos = store.getFeatureCount() - 1;
		} catch (DataException e) {
			lastPos = -1;
		}
	}

	public void setSortKeys(List<Field> sortFields) {
		FeatureStore store = nt.getLayer().getFeatureStore();

		DataManager manager = DALLocator.getDataManager();
		try {

			if (sortFields == null) {
				sortFields = new ArrayList<Field>();
				FeatureAttributeDescriptor[] primaryKey = store
						.getDefaultFeatureType().getPrimaryKey();
				for (FeatureAttributeDescriptor k : primaryKey) {
					Field field = new Field(k.getName());
					field.setSortOrder(SortOrder.ASCENDING);
					sortFields.add(field);
				}
			}

			FeatureQuery query = createQuery(store, sortFields);

			lastPos = store.getFeatureCount() - 1;
			set = manager.createFeaturePagingHelper(store, query, 1000);
		} catch (BaseException e) {
			logger.error(e.getMessage(), e);
		}
		setPosition(0);
	}

	private FeatureQuery createQuery(FeatureStore store, List<Field> sortFields) {
		FeatureQuery query = store.createFeatureQuery();
		FeatureQueryOrder order = new FeatureQueryOrder();

		for (Field k : sortFields) {
			boolean asc = k.getSortOrder() == SortOrder.ASCENDING ? true
					: false;
			order.add(k.getKey(), asc);
		}
		query.setOrder(order);
		return query;
	}

	public JPanel getToolBar() {
		return navToolBar;
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
			setPosition(getPosition() + 1);
		}
	}

	public void last() {
		if (isOnlySelected()) {
			lastSelected();
		} else {
			setPosition(lastPos);
		}
	}

	public void first() {
		if (isOnlySelected()) {
			firstSelected();
		} else {
			setPosition(0);
		}
	}

	public void previous() {
		if (isOnlySelected()) {
			previousSelected();
		} else {
			setPosition(getPosition() - 1);
		}
	}

	private void firstSelected() {
		long nextPosition = getPosition();
		try {
			FeatureStore store = nt.getLayer().getFeatureStore();
			FeatureSelection selection = store.getFeatureSelection();
			for (long i = 0; i < lastPos; i++) {
				Feature f = set.getFeatureAt(i);
				if (selection.isSelected(f)) {
					nextPosition = i;
					break;
				}
			}
		} catch (BaseException e) {
			logger.error(e.getMessage(), e);
		}
		setPosition(nextPosition);
	}

	private void nextSelected() {
		long nextPosition = getPosition();
		try {
			FeatureStore store = nt.getLayer().getFeatureStore();
			FeatureSelection selection = store.getFeatureSelection();
			for (long i = getPosition() + 1; i <= lastPos; i++) {
				Feature f = set.getFeatureAt(i);
				if (selection.isSelected(f)) {
					nextPosition = i;
					break;
				}
			}
		} catch (BaseException e) {
			logger.error(e.getMessage(), e);
		}

		setPosition(nextPosition);
	}

	private void lastSelected() {
		long nextPosition = getPosition();
		try {
			FeatureStore store = nt.getLayer().getFeatureStore();
			FeatureSelection selection = store.getFeatureSelection();
			for (long i = lastPos; i > getPosition(); i--) {
				Feature f = set.getFeatureAt(i);
				if (selection.isSelected(f)) {
					nextPosition = i;
					break;
				}
			}
		} catch (BaseException e) {
			logger.error(e.getMessage(), e);
		}

		setPosition(nextPosition);
	}

	private void previousSelected() {
		long nextPosition = getPosition();
		try {
			FeatureStore store = nt.getLayer().getFeatureStore();
			FeatureSelection selection = store.getFeatureSelection();
			for (long i = getPosition() - 1; i >= 0; i--) {
				Feature f = set.getFeatureAt(i);
				if (selection.isSelected(f)) {
					nextPosition = i;
					break;
				}
			}
		} catch (BaseException e) {
			logger.error(e.getMessage(), e);
		}

		setPosition(nextPosition);
	}

	/**
	 * @param newPosition
	 *            zero-based index on recordset
	 */
	public void setPosition(long newPosition) {

		newPosition = fixInvalidPosition(newPosition);

		PositionEvent evt = new PositionEvent(this, currentPosition,
				newPosition);
		positionEventSource.fireBeforePositionChange(evt);
		currentPosition = newPosition;
		if (currentPosition == EMPTY_REGISTER) {
			FeatureStore store = nt.getLayer().getFeatureStore();
			currentFeature = new EmptyFeature(store);
		} else {
			try {
				currentFeature = set.getFeatureAt(currentPosition);
			} catch (BaseException e) {
				logger.error(e.getMessage(), e);
			}
		}
		positionEventSource.fireOnPositionChange(evt);
	}

	private long fixInvalidPosition(long pos) {
		if (lastPos < 0) {
			return EMPTY_REGISTER;
		}

		if (pos == EMPTY_REGISTER) {
			return EMPTY_REGISTER;
		}

		if (pos >= lastPos) {
			pos = lastPos;
		}

		if (pos < 0) {
			pos = 0;
		}

		if (isOnlySelected()) {
			if (!isRecordSelected(pos)) {
				// TODO. Puede que hubiera que calcular la previousSelected y
				// devolver ese valor
				return EMPTY_REGISTER;
			}
		}
		return pos;
	}

	public long getPosition() {
		return currentPosition;
	}

	public Feature getFeature() {
		return currentFeature;
	}

	public Feature getFeature(long l) {
		try {
			return set.getFeatureAt(l);
		} catch (BaseException e) {
			logger.error(e.getMessage(), e);
		}
		FeatureStore store = nt.getLayer().getFeatureStore();
		return new EmptyFeature(store);
	}

	private void setTotalLabelText() {

		long numberOfRowsInRecordset = lastPos + 1;
		if (isOnlySelected()) {
			totalLabel.setText("/" + "(" + nt.getNumberOfRowsSelected() + ") "
					+ numberOfRowsInRecordset);
		} else {
			totalLabel.setText("/" + numberOfRowsInRecordset);
		}

	}

	private void posTFChanged() {
		String pos = posTF.getText();
		try {
			long userViewPos = Long.parseLong(pos) - 1;
			setPosition(userViewPos);
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
			previous();
		} else if (e.getSource() == posTF) {
			posTFChanged();
		} else if (e.getSource() == onlySelectedCB) {
			if (alwaysSelectCB.isSelected()) {
				alwaysSelectCB.setSelected(false);
				addThisAsSelectionObserver();
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
				deleteThisAsSelectionObserver();

			} else {
				addThisAsSelectionObserver();
			}
			nt.refreshGUI();
		} else if (e.getSource() == selectionB) {
			nt.selectCurrentFeature();
			nt.refreshGUI();
		}
	}

	private void deleteThisAsSelectionObserver() {
		try {
			nt.getLayer().getFeatureStore().getFeatureSelection()
					.deleteObserver(this);
		} catch (DataException e1) {
			logger.error(e1.getMessage(), e1);
		}
	}

	private void addThisAsSelectionObserver() {
		try {
			nt.getLayer().getFeatureStore().getFeatureSelection()
					.addObserver(this);
		} catch (DataException e1) {
			logger.error(e1.getMessage(), e1);
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
			firstSelected();
		} else {
			refreshGUI(onlySelectedCB.isEnabled());
		}
	}

	private boolean isRecordSelected() {
		try {
			FeatureStore store = nt.getLayer().getFeatureStore();
			FeatureSelection selection = store.getFeatureSelection();
			return selection.isSelected(currentFeature);
		} catch (BaseException e) {
			logger.error(e.getMessage(), e);
		}
		return false;
	}

	private boolean isRecordSelected(long position) {
		try {
			FeatureStore store = nt.getLayer().getFeatureStore();
			FeatureSelection selection = store.getFeatureSelection();
			Feature f = set.getFeatureAt(position);
			return selection.isSelected(f);
		} catch (BaseException e) {
			logger.error(e.getMessage(), e);
		}
		return false;
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
				posTF.setText(String.valueOf(getPosition() + 1));

			} catch (IndexOutOfBoundsException e) {
				/*
				 * fpuga. 10/12/2014. Workaround When the user delete the last
				 * record the editionChanged in EditionListener is called, and
				 * the gui is refreshed, but getPosition returns a removed
				 * position
				 */
				logger.error(e.getMessage(), e);
				posTF.setText(String.valueOf(getPosition() - 1));
			}
		}
		setTotalLabelText();
		if (isRecordSelected()) {
			posTF.setBackground(Color.YELLOW);
		} else {
			posTF.setBackground(Color.WHITE);
		}
	}

	public void modelChanged() {
		removeListeners();
		try {
			set.reload();
			lastPos = set.getTotalSize() - 1;
			setPosition(currentPosition);
		} catch (BaseException e) {
			logger.error(e.getMessage(), e);
		}
		setListeners();
	}

	public void setListeners() {
		addThisAsSelectionObserver();
	}

	public void removeListeners() {
		deleteThisAsSelectionObserver();
	}

	// Probably should be removed and use a factory instead
	// is duplicated with NavigationHandler
	private JButton getNavTableButton(JButton button, String iconName,
			String toolTipName) {
		JButton but = new JButton(nt.getIcon(iconName));
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

	public void registerNavTableButtonsOnActionToolBarExtensionPoint() {
		ExtensionPoints extensionPoints = ExtensionPointsSingleton
				.getInstance();
		selectionB = getNavTableButton(selectionB, "/Select.png",
				"selectionButtonTooltip");
		extensionPoints.add(AbstractNavTable.NAVTABLE_ACTIONS_TOOLBAR,
				"button-selection", selectionB);
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		if (arg1.equals(DataStoreNotification.SELECTION_CHANGE)) {
			// TODO. Cuando en la misma acción del usuario se deselecciona
			// un elemento y se selecciona otro, pinchando en otro elemento
			// de la tabla por ejemplo se produce dos veces el evento
			if (onlySelectedCB.isSelected() && !isRecordSelected()) {
				first();
			}

			nt.refreshGUI();
			// nt.enableCopySelectedButton(navEnabled);
			// filterAction.refreshGUI();
			// if (isRecordSelected()) {
			// ImageIcon imagenUnselect = nt.getIcon("/Unselect.png");
			// selectionB.setIcon(imagenUnselect);
			// } else {
			// ImageIcon imagenSelect = nt.getIcon("/Select.png");
			// selectionB.setIcon(imagenSelect);
			// }
			// refreshGUINavigation(navEnabled);

		}

	}

	public void deleteFeature() throws BaseException {
		// borra del featurestore, se producen las notificaciones
		// luego recarga el set de forma interna
		// El delete igual tenía que ir en otro sitio. Y ver si se arranca con
		// notificaciones y demás
		FeatureStore store = nt.getLayer().getFeatureStore();
		boolean editing = store.isEditing();
		if (!editing) {
			store.edit();
		}
		set.delete(currentFeature);
		if (!editing) {
			store.finishEditing();
		}
		lastPos = set.getTotalSize() - 1;
		setPosition(currentPosition);
	}

	public long getLastPos() {
		return lastPos;
	}

}
