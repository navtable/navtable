package es.udc.cartolab.gvsig.navtable;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import com.hardcode.gdbms.engine.values.Value;
import com.iver.andami.PluginServices;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.andami.ui.mdiManager.WindowInfo;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
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
 */
public class NavTable extends AbstractNavTable implements MouseListener{

	private static final long serialVersionUID = 1L;
	private IWindow window;

	protected WindowInfo viewInfo = null;
	
	private JTable table = null;
	
	private NavTablePopupMenu popupMenu = null;
		
	public NavTable(FLyrVect layer) {
		super(layer);
		popupMenu = new NavTablePopupMenu(layer);
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

		model.addTableModelListener(new TableModelListener(){
			//TODO
			public void tableChanged(TableModelEvent e) {
				//System.out.println(e.getType() );
//				if (e.getType() == TableModelEvent.UPDATE){
//				hasChanged = true;
//				}
			}

		});

		model.addColumn(PluginServices.getText(this, "headerTableAttribute"));
		model.addColumn(PluginServices.getText(this, "headerTableValue"));

		TableColumn attribColumn = table.getColumn(PluginServices.getText(this,"headerTableAttribute"));
		attribColumn.setCellRenderer(new AttribTableCellRenderer());
		
		table.addMouseListener(this);
		
		JScrollPane scrollPane = new JScrollPane(table);
		centerPanel.add(scrollPane, c);
		centerPanel.setMinimumSize(new Dimension(300, 400));
		scrollPane.addMouseListener(this);
		//centerPanel.setComponentPopupMenu(popupMenu);
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
	 * It gets the attributes names from the data table and
	 * sets them on the left column.
	 *
	 */
	private void fillAttributes(){	
		try {			
			DefaultTableModel model = (DefaultTableModel)table.getModel();
			for (int i = 0; i < recordset.getFieldCount(); i++){									
				Vector aux = new Vector();	
				aux.add(recordset.getFieldName(i));
				aux.add(" ");				
				model.addRow(aux);
				model.fireTableRowsInserted(model.getRowCount()-1, model.getRowCount()-1);				
			}				
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
			currentPosition = rowPosition;
			refreshGUI();
							
		} catch (com.hardcode.gdbms.engine.data.driver.DriverException e) {
			e.printStackTrace();
		}
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
	
			DefaultTableModel model = (DefaultTableModel)table.getModel();
			ToggleEditing te = new ToggleEditing();
			te.startEditing(layer);
			for (int i = 0; i < model.getRowCount(); i++) {
				Object value = model.getValueAt(i, 1);
				
				//only edit modified values, the cells that 
				//contains String instead of Value
				if (value instanceof String) {
					try {
						te.modifyValue(layer, currentPos, i, (String) value);

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
	
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("click click click");
		if (arg0.isMetaDown()) {
			if (arg0.getSource() == table) {
				Point point = arg0.getPoint();
				int rowNumber = table.rowAtPoint(point);
				ListSelectionModel model = table.getSelectionModel();
				model.setSelectionInterval(rowNumber, rowNumber);
			}
			System.out.println("**++**++**++** Right-click");
			popupMenu.show(arg0.getComponent(), arg0.getX(), arg0.getY());
		}
	}

	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}

