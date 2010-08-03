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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Types;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import net.miginfocom.swing.MigLayout;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.engine.values.NullValue;
import com.hardcode.gdbms.engine.values.Value;
import com.hardcode.gdbms.engine.values.ValueWriter;
import com.iver.andami.PluginServices;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.andami.ui.mdiManager.WindowInfo;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.ReadableVectorial;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;
import com.iver.cit.gvsig.fmap.layers.VectorialDBAdapter;
import com.iver.cit.gvsig.fmap.layers.VectorialFileAdapter;
import com.iver.cit.gvsig.fmap.layers.layerOperations.AlphanumericData;
import com.vividsolutions.jts.geom.Geometry;

/**
 * <p>NavTable's main panel is a two-column table that shows
 * data row of the layer. The first column contains the
 * attributes names and the second one contais its values.</p>
 * 
 * <img src="images/NavTable.png" />
 *
 * <p>NOTE: the <i>data table</i> is the original data storage, not
 * the table to be shown in this window.</p>
 * 
 * @author Nacho Varela
 * @author Pablo Sanxiao
 */
public class NavTable extends AbstractNavTable {

	private static final long serialVersionUID = 1L;
	private IWindow window;

	protected WindowInfo viewInfo = null;

	protected JTable table = null;
	private AttribTableCellRenderer cellRenderer = null;

	private JPanel CenterPanel;
	private JPanel SouthPanel;
	private JPanel NorthPanel;

	public NavTable(FLyrVect layer) {
		super(layer);
	}

	public NavTable(SelectableDataSource recordset) {
		super(recordset);
	}

	private JPanel getThisNorthPanel() {
		if(NorthPanel == null) {
			NorthPanel = new JPanel();
		}
		return NorthPanel;
	}

	private JPanel getThisSouthPanel() {
		if(SouthPanel == null) {
			SouthPanel = new JPanel();
		}
		return SouthPanel;
	}

	private JPanel getThisCenterPanel() {
		if(CenterPanel == null) {
			CenterPanel = new JPanel();
			BorderLayout CenterPanelLayout = new BorderLayout();
			CenterPanel.setLayout(CenterPanelLayout);
		}
		return CenterPanel;
	}

	private void initGUI() {
		MigLayout thisLayout = new MigLayout("inset 0, align center",
				"[grow]",
		"[][grow][]");
		this.setLayout(thisLayout);

		this.add(getThisNorthPanel(), "shrink, wrap, align center");
		this.add(getThisCenterPanel(), "shrink, growx, growy, wrap");
		this.add(getThisSouthPanel(), "shrink, align center" );

	}

	/**
	 * It creates a panel with a table that shows the
	 * data linked to a feature of the layer. Each row
	 * is a attribute-value pair.
	 * 
	 * @return the panel.
	 */
	@Override
	public JPanel getCenterPanel(){

		GridBagLayout glayout = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();

		c.weightx = 1.0;
		c.weighty = 1.0;
		c.fill = GridBagConstraints.BOTH;

		centerPanel = new JPanel(glayout);
		NavTableModel model = new NavTableModel();
		table = new JTable(model);

		table.addKeyListener(new KeyListener(){
			public void keyPressed(KeyEvent e) {
			}
			public void keyReleased(KeyEvent e) {
				//TODO If control + cursor ---> Inicio / Fin
				if (e.getKeyCode() == KeyEvent.VK_RIGHT){
					next();
				}
				if (e.getKeyCode() == KeyEvent.VK_LEFT){
					before();
				}
				if (e.getKeyCode() == KeyEvent.VK_HOME){
					first();
				}
				if (e.getKeyCode() == KeyEvent.VK_END){
					last();
				}
			}
			public void keyTyped(KeyEvent e) {
			}
		});

		model.addTableModelListener(new TableModelListener(){

			public void tableChanged(TableModelEvent e) {
				//TODO
				//System.out.println(e.getType() );
				//				if (e.getType() == TableModelEvent.UPDATE){
				//				hasChanged = true;
				//				}
			}
		});

		this.cellRenderer = new AttribTableCellRenderer();

		model.addColumn(PluginServices.getText(this, "headerTableAttribute"));
		model.addColumn(PluginServices.getText(this, "headerTableValue"));

		TableColumn attribColumn = table.getColumn(PluginServices.getText(this,"headerTableAttribute"));
		attribColumn.setCellRenderer(this.cellRenderer);
		attribColumn = table.getColumn(PluginServices.getText(this,"headerTableValue"));
		attribColumn.setCellRenderer(this.cellRenderer);


		JScrollPane scrollPane = new JScrollPane(table);
		centerPanel.add(scrollPane, c);
		//centerPanel.setMinimumSize(new Dimension(300, 400));
		return centerPanel;

	}


	@Override
	public boolean init() {

		window = PluginServices.getMDIManager().getActiveWindow();

		try {
			if (recordset.getRowCount() <= 0){
				JOptionPane.showMessageDialog(this, PluginServices.getText(this, "emptyLayer"));
				return false;
			}
		} catch (HeadlessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ReadDriverException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		initGUI();

		//Getting NavTable parent panels and add them on the tableLayoutPanels
		JPanel northPanel = getNorthPanel();
		getThisNorthPanel().add(northPanel);

		JPanel centerPanel = getCenterPanel();
		getThisCenterPanel().add(centerPanel);

		JPanel southPanel = getSouthPanel();
		getThisSouthPanel().add(southPanel);

		fillAttributes();
		currentPosition = 0;

		refreshGUI();
		super.repaint();
		super.setVisible(true);
		return true;

	}

	/**
	 * It gets the alias name of the attributes in the
	 * alias file if exists
	 * 
	 * @param fieldName
	 * @return alias
	 */
	private String getAlias(String fieldName) {
		File layerFile = null;
		String filePath = null;
		String alias = fieldName;

		// Added to tables without Layer support, but must be supported alias here also
		if (layer == null) {
			return fieldName;
		}

		ReadableVectorial source = layer.getSource();
		String pathToken = null;
		File fileAlias = null;

		if (source != null && source instanceof VectorialFileAdapter) {
			layerFile = ((VectorialFileAdapter) source).getFile();
			filePath = layerFile.getAbsolutePath();
			pathToken = filePath.substring(0, filePath.lastIndexOf("."));
			fileAlias = new File(pathToken + ".alias");

			if (!fileAlias.exists()) {
				pathToken = Preferences.getAliasDir() + File.separator + layer.getName();
				fileAlias = new File(pathToken + ".alias");
			}
		}else if (source instanceof VectorialDBAdapter) {
			pathToken = Preferences.getAliasDir() + File.separator + layer.getName();
			fileAlias = new File(pathToken + ".alias");
		}else {
			return fieldName;
		}

		if (!fileAlias.exists()){
			return fieldName;
		}

		try {
			String line;
			BufferedReader fileReader = new BufferedReader(new FileReader(fileAlias));
			while ((line = fileReader.readLine())!=null) {
				String tokens[] = line.split("=");
				if (tokens.length == 2) {
					String attrName = tokens[0].toUpperCase();
					if (fieldName.toUpperCase().compareTo(attrName) == 0) {
						alias = tokens[1];
						break;
					}
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return alias;
	}

	/**
	 * It gets the attributes names from the data table and
	 * sets them on the left column.
	 *
	 */
	private void fillAttributes(){
		try {
			DefaultTableModel model = (DefaultTableModel)table.getModel();
			Vector<String> aux = null;

			for (String attName: recordset.getFieldNames()) {
				aux = new Vector<String>(2);
				aux.add(getAlias(attName));
				aux.add(" ");
				model.addRow(aux);
			}

			if (layer != null) {
				// Geom_LENGTH
				aux = new Vector<String>(2);
				aux.add(PluginServices.getText(this, "Geom_LENGTH"));
				aux.add("0.0");
				model.addRow(aux);

				// Geom_AREA
				aux = new Vector<String>(2);
				aux.add(PluginServices.getText(this, "Geom_AREA"));
				aux.add("0.0");
				model.addRow(aux);

				this.cellRenderer.addNoEditableRow(model.getRowCount()-2);
				this.cellRenderer.addNoEditableRow(model.getRowCount()-1);
			}

		} catch (ReadDriverException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void fillEmptyValues() {
		super.fillEmptyValues();
		DefaultTableModel model = (DefaultTableModel)table.getModel();
		for (int i = 0; i < model.getRowCount(); i++){
			model.setValueAt("", i, 1);
		}
	}

	@Override
	public void fillValues(){
		try {

			DefaultTableModel model = (DefaultTableModel)table.getModel();
			for (int i = 0; i < recordset.getFieldCount(); i++){
				Value value = recordset.getFieldValue(currentPosition, i);
				String textoValue = value.getStringValue(ValueWriter.internalValueWriter);
				textoValue = textoValue.replaceAll("'", "");
				if (value instanceof NullValue) {
					textoValue ="";
				}
				model.setValueAt(textoValue, i, 1);
			}

			if (layer != null && layer instanceof AlphanumericData) {
				// Fill GEOM_LENGTH
				String value = "0.0";
				IGeometry g;
				ReadableVectorial source = (layer).getSource();
				source.start();
				g = source.getShape(new Long(currentPosition).intValue());
				source.stop();
				if (g == null) {
					model.setValueAt("0", recordset.getFieldCount(), 1);
					model.setValueAt("0", recordset.getFieldCount()+1, 1);
					return;
				}
				Geometry geom = g.toJTSGeometry();
				//	TODO Format number (Set units in Preferences)
				value = String.valueOf(Math.round(geom.getLength()));
				model.setValueAt(value, recordset.getFieldCount(), 1);
				// Fill GEOM_AREA
				value = "0.0";
				source.start();
				g = source.getShape(new Long(currentPosition).intValue());
				source.stop();
				geom = g.toJTSGeometry();
				//TODO Format number  (Set units in Preferences)
				value = String.valueOf(Math.round(geom.getArea()));
				model.setValueAt(value, recordset.getFieldCount()+1, 1);
			}

		} catch (ReadDriverException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public Vector checkChangedValues() {
		Vector changedValues = new Vector();
		for (int i=0; i<table.getRowCount()-2; i++) {
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
			} catch (ReadDriverException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return changedValues;
	}

	@Override
	public void selectRow(int row){
		table.setRowSelectionInterval(row, row);
	}

	@Override
	@Deprecated
	protected void saveRegister(){

		saveRecord();

	}

	@Override
	protected void saveRecord(){
		//TODO check if the values type are correct
		boolean layerEditing = true;

		//Stoping edition if some cell is being edited when the save button is clicked.
		stopCellEdition();

		// close all windows until get the view we're working on as the active window.
		while (!window.equals(PluginServices.getMDIManager().getActiveWindow())) {
			PluginServices.getMDIManager().closeWindow(PluginServices.getMDIManager().getActiveWindow());
		}
		int currentPos = Long.valueOf(currentPosition).intValue();

		if (layer.isWritable()) {

			Vector changedValues = checkChangedValues();
			if (changedValues.size()>0) {
				DefaultTableModel model = (DefaultTableModel)table.getModel();
				ToggleEditing te = new ToggleEditing();
				if (!layer.isEditing()) {
					layerEditing = false;
					te.startEditing(layer);
				}

				// attPos: the index of columns of the values that changed
				int[] attPos = new int[changedValues.size()];
				// attValues: The values that changed themselves
				String[] attValues = new String[changedValues.size()];
				int j = 0;
				for (int i = 0; i < model.getRowCount(); i++) {

					if (changedValues.contains(new Integer(i))) {
						//only edit modified values, the cells that
						//contains String instead of Value

						try {
							Object value = model.getValueAt(i, 1);
							attPos[j] = i;
							attValues[j] = value.toString();
							if (recordset.getFieldType(i)==Types.DATE) {
								attValues[j] = attValues[j].replaceAll("-", "/");
							}
						} catch (ReadDriverException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} finally {
							j++;
						}
					}
				}

				try {
					te.modifyValues(layer, currentPos, attPos, attValues);
				} catch (Exception e) {
					e.printStackTrace();
				}

				if (!layerEditing) {
					te.stopEditing(layer, false);
				}
				layer.setActive(true);
				//refreshGUI();
			}
		} else {
			JOptionPane.showMessageDialog(this, String.format(PluginServices.getText(this, "non_editable"),
					layer.getName()));
		}

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
	public void next(){
		stopCellEdition();
		super.next();
	}

	@Override
	public void before(){
		stopCellEdition();
		super.before();
	}

	@Override
	public void last(){
		stopCellEdition();
		super.last();
	}

	@Override
	public void first(){
		stopCellEdition();
		super.first();
	}

	@Override
	public void windowClosed() {
		stopCellEdition();
		super.windowClosed();
	}

	public Object getWindowProfile() {
		return WindowInfo.PROPERTIES_PROFILE;
	}

}

