package es.udc.cartolab.gvsig.navtable;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import com.hardcode.gdbms.engine.data.driver.DriverException;
import com.hardcode.gdbms.engine.values.Value;
import com.hardcode.gdbms.engine.values.ValueWriter;
import com.iver.andami.PluginServices;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.andami.ui.mdiManager.WindowInfo;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.drivers.DriverIOException;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.ReadableVectorial;
import com.iver.cit.gvsig.fmap.layers.VectorialFileAdapter;
import com.iver.cit.gvsig.fmap.layers.layerOperations.AlphanumericData;
import com.vividsolutions.jts.geom.Geometry;

import es.udc.cartolab.gvsig.navtable.ToggleEditing;

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
	
	private JTable table = null;
	private AttribTableCellRenderer cellRenderer = null;
		
	public NavTable(FLyrVect layer) {
		super(layer);
	}

	/**
	 * It creates a panel with a table that shows the
	 * data linked to a feature of the layer. Each row
	 * is a attribute-value pair.
	 * 
	 * @return the panel.
	 */
	protected JPanel getCenterPanel(){

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
			//TODO
			public void tableChanged(TableModelEvent e) {
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
		centerPanel.setMinimumSize(new Dimension(300, 400));		
		return centerPanel;

	}


	public boolean init() {
		
		window = PluginServices.getMDIManager().getActiveWindow();
				
		try {
			if (recordset.getRowCount() <= 0){
				JOptionPane.showMessageDialog(this, PluginServices.getText(this, "emptyLayer"));
				return false;
			}
		} catch (com.hardcode.gdbms.engine.data.driver.DriverException e) {
			e.printStackTrace();
		}

		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();

		super.setLayout(layout);

		c.gridy = 0;
		c.gridx = 0;
		c.anchor = GridBagConstraints.NORTH;
		c.fill = GridBagConstraints.BOTH;
		JPanel northPanel = getNorthPanel();
		super.add(northPanel, c);

		c.gridy = 1;
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.CENTER;
		JPanel centerPanel = getCenterPanel();
		fillAttributes();
		super.add(centerPanel, c);

		c.gridy = 11;		
		c.weightx = 1.0;
		c.weighty = 1.0;
		c.gridheight = 1;
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.SOUTH;
		JPanel southPanel = getSouthPanel();
		super.add(southPanel, c);

		fillValues(0);
		super.repaint();		
		super.setVisible(true);
		return true;

	}

	/**
	 * It gets the alias name of the attributes if exists in the 
	 * alias file
	 * 
	 * @param fieldName
	 * @return alias
	 */
	private String getAlias(String fieldName) {
		File layerFile = null;
		String filePath = null; 
		String alias = null;
		ReadableVectorial source = layer.getSource();
		
		if (source != null && source instanceof VectorialFileAdapter) {
			layerFile = ((VectorialFileAdapter) source).getFile();
			filePath = layerFile.getAbsolutePath();
		}
		
		String pathToken = filePath.substring(0, filePath.lastIndexOf("."));
		File fileAlias = new File(pathToken + ".alias");
		
		try {
			String line;
			BufferedReader fileReader = new BufferedReader(new FileReader(fileAlias));
			while ((line = fileReader.readLine())!=null) {
				String tokens[] = line.split("=");
				if (fieldName.compareTo(tokens[0]) == 0) {
					alias = tokens[1];
					break;
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
			String auxString = null;
			DefaultTableModel model = (DefaultTableModel)table.getModel();
			for (int i = 0; i < recordset.getFieldCount(); i++){									
				Vector aux = new Vector();
				auxString = getAlias(recordset.getFieldName(i));
				if (auxString != null) {
					aux.add(auxString);
				}else {
					aux.add(recordset.getFieldName(i));
				}
				aux.add(" ");				
				model.addRow(aux);
				model.fireTableRowsInserted(model.getRowCount()-1, model.getRowCount()-1);				
			}
			// Geom_LENGTH
			Vector aux = new Vector();
			aux.add("Geom_LENGTH");
			aux.add("0.0");		
			model.addRow(aux);
			model.fireTableRowsInserted(model.getRowCount()-1, model.getRowCount()-1);
			// Geom_AREA
			aux = new Vector();
			aux.add("Geom_AREA");
			aux.add("0.0");		
			model.addRow(aux);
			model.fireTableRowsInserted(model.getRowCount()-1, model.getRowCount()-1);

			this.cellRenderer.addNoEditableRow(model.getRowCount()-2);
			this.cellRenderer.addNoEditableRow(model.getRowCount()-1);

		} catch (com.hardcode.gdbms.engine.data.driver.DriverException e) {
			e.printStackTrace();
		}
	}

	protected void fillValues(long rowPosition){
		try {	
			if (rowPosition >= recordset.getRowCount()) {
				rowPosition = recordset.getRowCount()-1;
			}
			if (rowPosition < 0){
				rowPosition = 0;
			}

			DefaultTableModel model = (DefaultTableModel)table.getModel();
			for (int i = 0; i < recordset.getFieldCount(); i++){
				Value value = recordset.getFieldValue(rowPosition, i);
				model.setValueAt(value, i, 1);									
			}
			
			if (layer instanceof AlphanumericData) {
				try {
					// Fill GEOM_LENGTH
					String value = "0.0";
					IGeometry g;
					ReadableVectorial source = ((FLyrVect)layer).getSource();
					source.start();
					g = source.getShape(new Long(rowPosition).intValue());
					source.stop();
					Geometry geom = g.toJTSGeometry();
					//TODO Format number (Set units in Preferences)
					value = String.valueOf(Math.round(geom.getLength()));
					model.setValueAt(value, recordset.getFieldCount(), 1);
					// Fill GEOM_AREA
					value = "0.0";
					source.start();
					g = source.getShape(new Long(rowPosition).intValue());
					source.stop();
					geom = g.toJTSGeometry();
					//TODO Format number  (Set units in Preferences)	
					value = String.valueOf(Math.round(geom.getArea()));
					model.setValueAt(value, recordset.getFieldCount()+1, 1);
					
				} catch (DriverIOException e) {
					e.printStackTrace();
				}
			} 			

			
			currentPosition = rowPosition;
			refreshGUI();
							
		} catch (com.hardcode.gdbms.engine.data.driver.DriverException e) {
			e.printStackTrace();
		}
	}	

	private Vector checkChangedValues() {
		Vector changedValues = new Vector();
		for (int i=0; i<table.getRowCount()-2; i++) {
			try {
				String tableValue = table.getValueAt(i, 1).toString();
				String layerValue = recordset.getFieldValue(currentPosition, i).getStringValue(ValueWriter.internalValueWriter);
				layerValue = layerValue.replaceAll("'", "");
				if (tableValue.compareTo(layerValue)!=0) {
					changedValues.add(new Integer(i));
				}
			} catch (DriverException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		return changedValues;
	}
	
	protected void saveRegister(){
		//TODO check if the values type are correct
		
		//Stoping edition if some cell is being edited when the save button is clicked.
		stopEdition();
		
		// close all windows until get the view we're working on as the active window.
		while (!window.equals(PluginServices.getMDIManager().getActiveWindow())) {
			PluginServices.getMDIManager().closeWindow(PluginServices.getMDIManager().getActiveWindow());
		} 
			int currentPos = Long.valueOf(currentPosition).intValue();
	
			Vector changedValues = checkChangedValues();
			if (changedValues.size()>0) {
				DefaultTableModel model = (DefaultTableModel)table.getModel();
				ToggleEditing te = new ToggleEditing();
				te.startEditing(layer);

				for (int i = 0; i < model.getRowCount(); i++) {

					if (changedValues.contains(new Integer(i))) {
						Object value = model.getValueAt(i, 1);

						//only edit modified values, the cells that 
						//contains String instead of Value

						try {
							te.modifyValue(layer, currentPos, i, value.toString());

						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}

				}

				te.stopEditing(layer, false);
				layer.getMapContext().redraw();
				layer.setActive(true);
				refreshGUI();
			}
			//Removes the ProjectTable of this layer if it exists.
			//Currently commented for testing purposes...
//			ProjectExtension pe = (ProjectExtension)PluginServices.getExtension(ProjectExtension.class);
//			Project project = pe.getProject();
//			ArrayList views = project.getDocumentsByType("ProjectTable");
//			for (int i=0; i < views.size(); i++){			
//				ProjectTable pTable = (ProjectTable)views.get(i);
//				System.out.println(i+" TableName: " + pTable.getName());
//				if (pTable.getName().endsWith(" ET")){
//					project.delDocument(pTable);
//					break;
//				}
//			}
		
	}

	/**
	 * It stops the row editing when the save button is pressed.
	 *
	 */
	private void stopEdition() {
		if (table.isEditing()) {
			if (table.getCellEditor() != null) {
		        table.getCellEditor().stopCellEditing();
		    }
		}
	}
	
	protected void next(){
		stopEdition();
		super.next();
	}
	
	protected void before(){
		stopEdition();
		super.before();
	}
	
	protected void last(){
		stopEdition();
		super.last();
	}
	
	protected void first(){
		stopEdition();
		super.first();
	}
}

