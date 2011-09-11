package es.udc.cartolab.gvsig.navtable;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import com.hardcode.driverManager.DriverLoadException;
import com.hardcode.gdbms.engine.data.driver.DriverException;
import com.hardcode.gdbms.engine.values.ValueFactory;
import com.iver.andami.PluginServices;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.andami.ui.mdiManager.WindowInfo;
import com.iver.cit.gvsig.fmap.drivers.FieldDescription;
import com.iver.cit.gvsig.fmap.drivers.ITableDefinition;
import com.iver.cit.gvsig.fmap.edition.VectorialEditableAdapter;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.ReadableVectorial;

public class EditTableSchemaPanel extends JPanel implements IWindow, ActionListener{

	private JTable table;
	private WindowInfo viewInfo = null;
	private FLyrVect layer;
	
	private JButton upButton;
	private JButton downButton;
	
	final static int ATR_NAME_IDX = 0;
	final static int ATR_NSHW_IDX = 1;
	final static int ATR_TYPE_IDX = 2;
	final static int ATR_LEN_IDX = 3;
	final static int ATR_DEC_IDX = 4;
	final static int ATR_VIS_IDX = 5;
	
	final static String [] header = {"Atribute name", "Name shown", "Type", "Length", "Decimal count", "Visible"};
	
	private String[] items = {"Boolean", "Date", "Integer", "Double", "String"};
	
	private int getIndexByType(int type) {
		int returnValue = 4;
		switch (type) {
		case Types.INTEGER:
		case Types.BIGINT:
		case Types.SMALLINT:
		case Types.TINYINT:
			returnValue = 2;
			break;
		case Types.BIT:
		case Types.BOOLEAN:
			returnValue = 0;
			break;
		case Types.CHAR:
		case Types.VARCHAR:
		case Types.LONGVARCHAR:
			returnValue = 4;
			break;
		case Types.DATE:
			returnValue = 1;
			break;
		case Types.DECIMAL:
		case Types.NUMERIC:
		case Types.FLOAT:
		case Types.DOUBLE:
		case Types.REAL:
			returnValue = 3;
			break;
		case Types.BINARY:
		case Types.VARBINARY:
		case Types.LONGVARBINARY:
			break;

		case Types.TIMESTAMP:
			break;

		case Types.TIME:
			break;
		default:
			returnValue = 4;
		}
		
		return returnValue;
	}
	
	public WindowInfo getWindowInfo() {
		// TODO Auto-generated method stub
		if (viewInfo == null) {
			viewInfo = new WindowInfo(WindowInfo.MODALDIALOG | WindowInfo.RESIZABLE | WindowInfo.PALETTE);
			viewInfo.setTitle(PluginServices.getText(this,
			"View / Edit table"));
			viewInfo.setWidth(600);
			viewInfo.setHeight(250);
		}
		return viewInfo;
	}
	
	public EditTableSchemaPanel(FLyrVect lyr) {
		
		layer = lyr;
		
		if (viewInfo == null) {
			getWindowInfo();
		}
		viewInfo.setTitle(PluginServices.getText(this, 
				"View / Edit table: " + layer.getName()));
		
		
		//set the table
		DefaultTableModel model = new DefaultTableModel() {
			public Class getColumnClass (int c) {
				return getValueAt(0, c).getClass();
			}
		};
		for (int i = 0; i<header.length; i++) {
			model.addColumn(header[i]);
		}
		table = new JTable(model);
		
		//set combobox cell in the type column
		TableColumn col = table.getColumnModel().getColumn(ATR_TYPE_IDX);
		col.setCellEditor(new MyComboBoxEditor(items));
		
		//fill the table
		ToggleEditing te = new ToggleEditing();
		te.startEditing(layer);
		VectorialEditableAdapter edAdapter = (VectorialEditableAdapter) layer.getSource();
		try {
			ITableDefinition tableDef = edAdapter.getTableDefinition();
			FieldDescription[] fieldDesc = tableDef.getFieldsDesc();
			for (int i = 0; i<fieldDesc.length; i++) {
				int type = fieldDesc[i].getFieldType();
				int index = getIndexByType(type);
				Object aux[] = new Object[header.length];
				for (int j=0; j<header.length; j++) {
					switch (j) {
					case ATR_NSHW_IDX : //TODO de momento toma el nombre de atributo
					case ATR_NAME_IDX : aux[j] = fieldDesc[i].getFieldName();
					break;
					case ATR_TYPE_IDX : aux[j] = items[index];
					break;
					case ATR_LEN_IDX : aux[j] = new Integer(fieldDesc[i].getFieldLength());
					break;
					case ATR_DEC_IDX : aux[j] = new Integer(fieldDesc[i].getFieldDecimalCount());
					break;
					case ATR_VIS_IDX : aux[j] = new Boolean(true);
					}
				}
				model.addRow(aux);
			}
		} catch (DriverLoadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DriverException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//next line should be moved?
		te.stopEditing(layer, true);
		
		init();
	}
	
	private void init() {
		
		GridBagLayout layout = new GridBagLayout();
		super.setLayout(layout);
		
		//table area
		JScrollPane tablePane = new JScrollPane(table);
		super.add(tablePane, new GridBagConstraints(
				1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTH,
				GridBagConstraints.BOTH, new Insets(0, 0, 0, 0),
				0, 0));
		
		//buttons to add, remove and move rows
		JToolBar toolBar = new JToolBar();
		upButton = new JButton(
				new ImageIcon("gvSIG/extensiones/es.udc.cartolab.gvsig.navtable/images/go-up.png"));
		upButton.setToolTipText("Move attribute up");
		upButton.addActionListener(this);
		
		downButton = new JButton(
				new ImageIcon("gvSIG/extensiones/es.udc.cartolab.gvsig.navtable/images/go-down.png"));
		downButton.setToolTipText("Move attribute down");
		downButton.addActionListener(this);
		
		JButton addButton = new JButton(
				new ImageIcon("gvSIG/extensiones/es.udc.cartolab.gvsig.navtable/images/list-add.png"));
		addButton.setToolTipText("Add new attribute");
		
		JButton removeButton = new JButton(
				new ImageIcon("gvSIG/extensiones/es.udc.cartolab.gvsig.navtable/images/list-remove.png"));
		removeButton.setToolTipText("Remove attribute");
		removeButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				DefaultTableModel model = (DefaultTableModel)table.getModel();
				for (int i = table.getSelectedRowCount()-1; i>=0; i--) {
					int selectedRow = table.getSelectedRows()[i];
					model.removeRow(selectedRow);
				}
			}
			
		});
		
		toolBar.add(addButton);
		toolBar.add(removeButton);
		toolBar.add(upButton);
		toolBar.add(downButton);
		
		toolBar.setOrientation(JToolBar.VERTICAL);
		
		super.add(toolBar, new GridBagConstraints(
				0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0),
				0, 0));
		
		
		//buttons area
		JPanel buttonPanel = new JPanel();
		JButton okButton = new JButton("Save changes");
		JButton cancelButton = new JButton("Discard changes");
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);
		
		super.add(buttonPanel, new GridBagConstraints(
				1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.SOUTH,
				GridBagConstraints.BOTH, new Insets(0, 0, 0, 0),
				0, 0));
	}

	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if (e.getSource() == upButton) {
			goUp();
		}
		if (e.getSource() == downButton) {
			goDown();
		}
		
	}
	
	private void goUp() {
		
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		ListSelectionModel selectionModel = table.getSelectionModel();
		int[] selectedRows = table.getSelectedRows();
		table.clearSelection();
		int beginPos = 0;
		int endPos = 0;
		for (int i=0; i<selectedRows.length; i++) {
			beginPos = selectedRows[i];
			endPos = selectedRows[i];
			// determinate wich rows will be moved 
			// (in order not to move not selected rows)
			for (int j = i+1; i<selectedRows.length-1; j++) {
				if (selectedRows[j]-endPos == 1) {
					endPos++;
				} else {
					break;
				}
				i = j;
			}
			if (beginPos-1 >= 0) {
				//reorder the table
				model.moveRow(beginPos, endPos, beginPos-1);
				selectionModel.addSelectionInterval(beginPos-1, endPos-1);
			} else {
				//already at the top, don't move rows
				selectionModel.addSelectionInterval(beginPos, endPos);
			}
		}
		
	}
	
	private void goDown() {
		
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		ListSelectionModel selectionModel = table.getSelectionModel();
		int[] selectedRows = table.getSelectedRows();
		table.clearSelection();
		int beginPos = 0;
		int endPos = 0;
		for (int i=0;i<selectedRows.length; i++) {
			beginPos = selectedRows[i];
			endPos = selectedRows[i];
			for (int j = i+1; j<selectedRows.length; j++) {
				if (selectedRows[j]-endPos == 1) {
					endPos++;
				} else {
					break;
				}
				i = j;
			}
			if (table.getRowCount()>endPos+1) {
				//reorder the table
				model.moveRow(beginPos, endPos, beginPos+1);
				selectionModel.addSelectionInterval(beginPos+1, endPos+1);

			} else {
				selectionModel.addSelectionInterval(beginPos, endPos);
			}
		}
	}

}
