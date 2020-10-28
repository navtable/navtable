/*
 * This file is part of NavTable
 * Copyright (C) 2009 - 2010  Cartolab (Universidade da Coru�a)
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
 * Authors:
 *   Juan Ignacio Varela Garc�a <nachouve (at) gmail (dot) com>
 *   Pablo Sanxiao Roca <psanxiao (at) gmail (dot) com>
 *   Javier Est�vez Vali�as <valdaris (at) gmail (dot) com>
 *   Andres Maneiro <andres.maneiro@gmail.com>
 *   Jorge Lopez Fernandez <jlopez (at) cartolab (dot) es>
 */
package es.udc.cartolab.gvsig.navtable;

import static es.icarto.gvsig.commons.i18n.I18n._;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.Types;
import java.util.List;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.gvsig.fmap.dal.exception.DataException;
import org.gvsig.fmap.geom.Geometry;
import org.gvsig.fmap.geom.operation.GeometryOperationException;
import org.gvsig.fmap.geom.operation.GeometryOperationNotSupportedException;
import org.gvsig.fmap.mapcontext.layers.vectorial.FLyrVect;
import org.gvsig.utils.extensionPointsOld.ExtensionPoint;
import org.gvsig.utils.extensionPointsOld.ExtensionPoints;
import org.gvsig.utils.extensionPointsOld.ExtensionPointsSingleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.udc.cartolab.gvsig.navtable.format.ValueFormatNT;
import es.udc.cartolab.gvsig.navtable.listeners.MyMouseListener;
import es.udc.cartolab.gvsig.navtable.listeners.PositionEvent;
import es.udc.cartolab.gvsig.navtable.table.AttribTableCellRenderer;
import es.udc.cartolab.gvsig.navtable.table.NavTableModel;

/**
 * <p>
 * NavTable's main panel is a two-column table that shows data row of the layer.
 * The first column contains the attributes names and the second one contains
 * its values.
 * </p>
 *
 * <img src="images/NavTable.png" />
 *
 * <p>
 * NOTE: the <i>data table</i> is the original data storage, not the table to be
 * shown in this window.
 * </p>
 *
 * @author Nacho Varela
 * @author Pablo Sanxiao
 * @author Andres Maneiro
 * @author Jorge Lopez
 * @author Francisco Puga
 */
public class NavTable extends AbstractNavTable {

	private static final Logger logger = LoggerFactory.getLogger(NavTable.class);
	private static final long serialVersionUID = 1L;

	private boolean isFillingValues = false;

	protected JTable table = null;
	private AttribTableCellRenderer cellRenderer = null;

	private MyTableModelListener myTableModelListener;
	private MyKeyListener myKeyListener;
	private MyMouseListener myMouseListener;

	private final ValueFormatNT valueFormatNT = new ValueFormatNT();

	// Mouse buttons constants
	public static final int BUTTON_RIGHT = 3;

	public NavTable(FLyrVect layer) {
		super(layer);
		setTitle("NavTable: " + layer.getName());
	}

	public boolean isFillingValues() {
		return isFillingValues;
	}

	public void setFillingValues(boolean isFillingValues) {
		this.isFillingValues = isFillingValues;
	}

	/**
	 * It creates a panel with a table that shows the data linked to a feature of
	 * the layer. Each row is a attribute-value pair.
	 *
	 */
	@Override
	public JPanel getCenterPanel() {

		NavTableModel model = new NavTableModel();
		table = new JTable(model);
		table.getTableHeader().setReorderingAllowed(false);

		myKeyListener = new MyKeyListener();
		table.addKeyListener(myKeyListener);

		myMouseListener = new MyMouseListener(this);
		table.addMouseListener(myMouseListener);

		this.cellRenderer = new AttribTableCellRenderer();

		model.addColumn(_("headerTableAttribute"));
		model.addColumn(_("headerTableValue"));

		TableColumn attribColumn = table.getColumn(_("headerTableAttribute"));
		attribColumn.setCellRenderer(this.cellRenderer);
		attribColumn = table.getColumn(_("headerTableValue"));
		attribColumn.setCellRenderer(this.cellRenderer);

		myTableModelListener = new MyTableModelListener();
		model.addTableModelListener(myTableModelListener);

		JScrollPane scrollPane = new JScrollPane(table);
		centerPanel = new JPanel(new BorderLayout());
		centerPanel.add(scrollPane, BorderLayout.CENTER);
		return centerPanel;
	}

	class MyKeyListener implements KeyListener {
		@Override
		public void keyPressed(KeyEvent e) {
		}

		@Override
		public void keyReleased(KeyEvent e) {
			// TODO If control + cursor ---> Inicio / Fin
			if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
				navigation.next();
			}
			if (e.getKeyCode() == KeyEvent.VK_LEFT) {
				navigation.previous();
			}
			if (e.getKeyCode() == KeyEvent.VK_HOME) {
				navigation.first();
			}
			if (e.getKeyCode() == KeyEvent.VK_END) {
				navigation.last();
			}
		}

		@Override
		public void keyTyped(KeyEvent e) {
		}
	}

	class MyTableModelListener implements TableModelListener {
		@Override
		public void tableChanged(TableModelEvent e) {
			if (e.getType() == TableModelEvent.UPDATE && !isFillingValues()) {
				TableModel model = table.getModel();

				for (int i = e.getFirstRow(); i <= e.getLastRow(); i++) {
					String fieldName = model.getValueAt(i, 0).toString();
					String tableValue = model.getValueAt(i, 1).toString();
					String valueInLayer = layerController.getValueInLayer(fieldName);
					if (!tableValue.equals(valueInLayer)) {
						int fieldType = layerController.getType(fieldName);
						if (fieldType == Types.DATE) {
							tableValue = tableValue.replaceAll("-", "/");
						}
						layerController.setValue(fieldName, tableValue);
						setChangedValues(true);
					}
				}
				enableSaveButton(isChangedValues());
			}
		}
	}

	protected boolean isEditing() {
		return layer.isEditing();
	}

	@Override
	protected void registerNavTableButtonsOnActionToolBarExtensionPoint() {
		/*
		 * TODO: this will make the extension point mechanims not work as expected for
		 * navtable extension. It only will add the regular buttons navtable has, not
		 * all included in the extensionpoint (as the default behaviour is).
		 *
		 * Probably we need to get rid of extensionpoints mechanism as -roughly- it is a
		 * global variable mechanism, which is not what we need. For action buttons,
		 * it'll be desirable a mechanism that:
		 *
		 * 1) allow adding buttons for custom forms build on abstractnavtable. 2) don't
		 * share those buttons between all children of abstractnavtable, unless
		 * requested otherwise.
		 *
		 * Check decorator pattern, as it seems to fit well here.
		 */
		ExtensionPoints extensionPoints = ExtensionPointsSingleton.getInstance();
		ExtensionPoint ep = (ExtensionPoint) extensionPoints.get(AbstractNavTable.NAVTABLE_ACTIONS_TOOLBAR);

		if (ep != null) {
			ep.clear();
		}
		super.registerNavTableButtonsOnActionToolBarExtensionPoint();
	}

	@Override
	protected void initWidgets() {
		NavTableAlias alias = new NavTableAlias(layer.getFeatureStore());
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		model.setRowCount(0);

		for (String attName : layerController.getFieldNames()) {
			initAttribute(model, alias.getAlias(attName), "");
		}
		initAttribute(model, _("Geom_LENGTH"), "0.0");
		initAttribute(model, _("Geom_AREA"), "0.0");

		this.cellRenderer.emptyNoEditableRows();

		this.cellRenderer.addNoEditableRow(model.getRowCount() - 2);
		this.cellRenderer.addNoEditableRow(model.getRowCount() - 1);
	}

	private void initAttribute(DefaultTableModel model, String att, String value) {
		Vector<String> aux = new Vector<String>(2);
		aux.add(att);
		aux.add(value);
		model.addRow(aux);
	}

	@Override
	public void fillEmptyValues() {
		setFillingValues(true);
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		for (int i = 0; i < model.getRowCount(); i++) {
			model.setValueAt("", i, 1);
		}
		setFillingValues(false);
	}

	@Override
	public void fillValues() {
		try {
			setFillingValues(true);
			DefaultTableModel model = (DefaultTableModel) table.getModel();
			List<String> fieldNames = layerController.getFieldNames();
			for (int i = 0; i < fieldNames.size(); i++) {
				String v = layerController.getValueInLayer(fieldNames.get(i));
				model.setValueAt(v, i, 1);
			}

			Geometry geometry = layerController.getGeom();

			String length = "0.0";
			String area = "0.0";
			if (geometry != null) {
				length = String.valueOf(Math.round(geometry.perimeter()));
				area = String.valueOf(Math.round(geometry.area()));
			}
			model.setValueAt(length, fieldNames.size(), 1);
			model.setValueAt(area, fieldNames.size() + 1, 1);

		} catch (GeometryOperationNotSupportedException e) {
			logger.error(e.getMessage(), e);
		} catch (GeometryOperationException e) {
			logger.error(e.getMessage(), e);
		} finally {
			setFillingValues(false);
		}
	}

	@Override
	public void selectRow(int row) {
		table.setRowSelectionInterval(row, row);
	}

	protected boolean isSaveable() {
		stopCellEdition();

		if (layer.isWritable()) {
			return true;
		} else {
			JOptionPane.showMessageDialog(this, _("non_editable", layer.getName()));
			return false;
		}
	}

	@Override
	public boolean saveRecord() throws DataException {
		if (isSaveable()) {
			try {
				setSavingValues(true);
				layerController.update(navigation.getFeature());
				setChangedValues(false);
				return true;
			} catch (DataException e) {
				throw e;
			} finally {
				setSavingValues(false);
			}
		}
		return false;
	}

	/**
	 * It stops the row editing when the save button is pressed.
	 *
	 */
	protected void stopCellEdition() {
		if (table.isEditing()) {
			if (table.getCellEditor() != null) {
				table.getCellEditor().stopCellEditing();
			}
		}
	}

	@Override
	public void beforePositionChange(PositionEvent e) {
		stopCellEdition();
		super.beforePositionChange(e);
	}

	@Override
	public void windowClosed() {
		stopCellEdition();
		this.table.getModel().removeTableModelListener(myTableModelListener);
		this.table.removeKeyListener(myKeyListener);
		super.windowClosed();
	}

	@Override
	public void reloadRecordset() throws DataException {
		super.reloadRecordset();
		initWidgets();
	}

	public JTable getTable() {
		return this.table;
	}
}
