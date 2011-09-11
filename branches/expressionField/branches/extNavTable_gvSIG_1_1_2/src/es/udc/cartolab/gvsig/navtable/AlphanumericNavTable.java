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
 *   Francisco Puga Alonso <fran.puga (at) gmail (dot) com>
 */
package es.udc.cartolab.gvsig.navtable;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URL;
import java.sql.Types;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

import com.hardcode.driverManager.DriverLoadException;
import com.hardcode.gdbms.engine.data.driver.DriverException;
import com.hardcode.gdbms.engine.values.NullValue;
import com.hardcode.gdbms.engine.values.Value;
import com.hardcode.gdbms.engine.values.ValueFactory;
import com.hardcode.gdbms.engine.values.ValueWriter;
import com.iver.andami.PluginServices;
import com.iver.andami.messages.NotificationManager;
import com.iver.cit.gvsig.fmap.core.DefaultRow;
import com.iver.cit.gvsig.fmap.core.IRow;
import com.iver.cit.gvsig.fmap.drivers.DriverIOException;
import com.iver.cit.gvsig.fmap.drivers.ITableDefinition;
import com.iver.cit.gvsig.fmap.edition.EditionEvent;
import com.iver.cit.gvsig.fmap.edition.EditionException;
import com.iver.cit.gvsig.fmap.edition.IEditableSource;
import com.iver.cit.gvsig.fmap.edition.IWriteable;
import com.iver.cit.gvsig.fmap.edition.IWriter;

public class AlphanumericNavTable extends NavTable {

	JButton newB = null;
	protected IEditableSource model;

	public AlphanumericNavTable(IEditableSource model) {
		super(model.getRecordset());
		this.model = model;
	}

	@Override
	public boolean init() {
		if (super.init() == false) {
			return false;
		}

		int index = -1;
		zoomB.setVisible(false);
		alwaysZoomCB.setVisible(false);
		fixScaleCB.setVisible(false);

		URL imgURL = getClass().getResource("/table_add.png");
		ImageIcon imagenNewRegister = new ImageIcon(imgURL);
		newB = new JButton(imagenNewRegister);
		newB.setToolTipText(PluginServices.getText(this, "new_register"));

		newB.addActionListener(this);
		zoomB.getParent().add(newB);
		// We must to rewrite selectionB listener and the others

		return true;
	}

	@Override
	public Vector checkChangedValues() {

		Vector changedValues = new Vector();
		for (int i=0; i<table.getRowCount(); i++) {
			try {
				String tableValue = table.getValueAt(i, 1).toString();
				Value value = recordset.getFieldValue(currentPosition, i);
				String layerValue = value.getStringValue(ValueWriter.internalValueWriter);
				layerValue = layerValue.replaceAll("'", "");
				if (value instanceof NullValue) {
					if (tableValue.compareTo("")!=0) {
						changedValues.add(new Integer(i));
					}
				} else {
					if (tableValue.compareTo(layerValue)!=0) {
						changedValues.add(new Integer(i));
					}
				}
			} catch (DriverException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return changedValues;
	}

	@Override
	@Deprecated
	protected void saveRegister() {
		saveRecord();
	}

	@Override
	protected void saveRecord() {

		stopCellEdition();
		ToggleEditing te = new ToggleEditing();
		try {

			model.startEdition(EditionEvent.ALPHANUMERIC);
			if (model instanceof IWriteable)
			{
				Vector changedValues = checkChangedValues();
				if (changedValues.size()>0) {
					DefaultTableModel tableModel = (DefaultTableModel)table.getModel();

					int[] newValuesPosition = new int[changedValues.size()];
					String[] newValues = new String[changedValues.size()];
					int j = 0;

					for (int i = 0; i < tableModel.getRowCount(); i++) {


						if (changedValues.contains(new Integer(i))) {
							//only edit modified values, the cells that
							//contains String instead of Value
							Object value = tableModel.getValueAt(i, 1);
							newValuesPosition[j] = i;
							newValues[j] = value.toString();
							if (recordset.getFieldType(i)==Types.DATE) {
								newValues[j] = newValues[j].replaceAll("-", "/");
							}
							j++;
						}
					}

					int currentPos = Long.valueOf(currentPosition).intValue();
					te.modifyValues(model, currentPos, newValuesPosition, newValues);

					IWriteable w = (IWriteable) model;
					IWriter writer = w.getWriter();
					if (writer == null)
					{
						NotificationManager.addError("No existe driver de escritura para la tabla"
								+ recordset.getName(), new EditionException());
					}
					else
					{
						ITableDefinition tableDef = model.getTableDefinition();
						writer.initialize(tableDef);

						model.stopEdition(writer,EditionEvent.ALPHANUMERIC);

						// TODO: RELOAD
						// EditableAdapter edAdapter = (EditableAdapter) ies;
						// Restaura el datasource a su estado original
						// edAdapter.setOriginalDataSource(edAdapter.getRecordset());
						// model.getSelection().clear();
						// refreshControls();
					}
				}
			}
		} catch (EditionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DriverLoadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DriverException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Deprecated
	private void addRow() {
		addRecord();
	}

	private void addRecord() {

		//showWarning();
		if (onlySelectedCB.isSelected()) {
			onlySelectedCB.setSelected(false);
		}
		try {
			model.startEdition(EditionEvent.ALPHANUMERIC);

			if (model instanceof IWriteable) {

				IRow row;
				int numAttr = recordset.getFieldCount();
				Value[] values = new Value[numAttr];
				for (int i=0; i<numAttr; i++) {
					values[i] = ValueFactory.createNullValue();
				}
				row = new DefaultRow(values);
				model.doAddRow(row, EditionEvent.ALPHANUMERIC);

				IWriteable w = (IWriteable) model;
				IWriter writer = w.getWriter();

				ITableDefinition tableDef = model.getTableDefinition();
				writer.initialize(tableDef);

				model.stopEdition(writer, EditionEvent.ALPHANUMERIC);

				last();
			}
		} catch (EditionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DriverException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DriverIOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Deprecated
	private void deleteRow() {
		deleteRecord();
	}

	protected void deleteRecord() {
		try {
			model.startEdition(EditionEvent.ALPHANUMERIC);

			IWriteable w = (IWriteable) model;
			IWriter writer = w.getWriter();

			ITableDefinition tableDef = model.getTableDefinition();
			writer.initialize(tableDef);

			model.doRemoveRow((int) currentPosition, EditionEvent.ALPHANUMERIC);

			model.stopEdition(writer, EditionEvent.ALPHANUMERIC);

			//Refresh
			currentPosition = currentPosition -1;
			next();

		} catch (EditionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DriverIOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DriverLoadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DriverException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void actionPerformed (ActionEvent e) {

		if (e.getSource() == newB) {
			addRecord();
		} else if (e.getSource() == removeB) {
			int answer = JOptionPane.showConfirmDialog(null,
					PluginServices.getText(null, "confirm_delete_register"),
					null,
					JOptionPane.YES_NO_OPTION);

			if (answer == 0) {
				deleteRecord();
			}
		}else {
			super.actionPerformed(e);
		}
	}

}
