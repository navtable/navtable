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
import com.hardcode.gdbms.engine.values.Value;
import com.hardcode.gdbms.engine.values.ValueFactory;
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
	//JButton removeB = null;
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
		//TODO Add the string to the i18n to traslate
		newB.setToolTipText(PluginServices.getText(this,
		"new_register"));

		newB.addActionListener(this);
		zoomB.getParent().add(newB);
		// We must to rewrite selectionB listener and the others

		return true;
	}

	@Override
	protected void saveRegister() {

		//cerrar la edición de la celda
		stopCellEdition();
		ToggleEditing te = new ToggleEditing();
		try {
			//			poner en edición la tabla
			model.startEdition(EditionEvent.ALPHANUMERIC);
			if (model instanceof IWriteable)
			{

				Vector changedValues = checkChangedValues();
				if (changedValues.size()>0) {
					DefaultTableModel tableModel = (DefaultTableModel)table.getModel();

					for (int i = 0; i < tableModel.getRowCount(); i++) {

						if (changedValues.contains(new Integer(i))) {
							Object value = tableModel.getValueAt(i, 1);

							//only edit modified values, the cells that
							//contains String instead of Value

							try {
								String text = value.toString();
								if (recordset.getFieldType(i)==Types.DATE) {
									text = text.replaceAll("-", "/");
								}
								//te.modifyValue(layer, currentPos, i, text);
								int currentPos = Long.valueOf(currentPosition).intValue();
								te.modifyValue(model, currentPos, i, text);

							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}



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
						//						EditableAdapter edAdapter = (EditableAdapter) ies;
						//						// Restaura el datasource a su estado original
						//						edAdapter.setOriginalDataSource(edAdapter.getRecordset());
						//model.getSelection().clear();
						//refreshControls();
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

	private void addRow() {
		//crear una row vacía
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

				//				ir a ella en navtable
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

	protected void deleteRow() {
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
			addRow();
		}else if (e.getSource() == removeB) {
			int answer = JOptionPane.showConfirmDialog(null, PluginServices.getText(null, "confirm_delete_register"), null, JOptionPane.YES_NO_OPTION);
			if (answer == 0) {
				deleteRow();
			}
		}else {
			super.actionPerformed(e);
		}
	}

}//Class
