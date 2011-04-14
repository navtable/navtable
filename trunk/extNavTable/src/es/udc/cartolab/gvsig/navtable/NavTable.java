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
 */
package es.udc.cartolab.gvsig.navtable;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Types;
import java.util.Vector;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
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
import com.iver.cit.gvsig.FiltroExtension;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.layers.FBitSet;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.ReadableVectorial;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;
import com.iver.cit.gvsig.fmap.layers.VectorialDBAdapter;
import com.iver.cit.gvsig.fmap.layers.VectorialFileAdapter;
import com.iver.cit.gvsig.fmap.layers.layerOperations.AlphanumericData;
import com.iver.cit.gvsig.gui.filter.DefaultExpressionDataSource;
import com.vividsolutions.jts.geom.Geometry;

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
 */
public class NavTable extends AbstractNavTable {

    private static final long serialVersionUID = 1L;
    private IWindow window;
    protected WindowInfo viewInfo = null;

    private boolean isFillingValues = false;

    protected JTable table = null;
    private AttribTableCellRenderer cellRenderer = null;

    private JPanel CenterPanel;
    private JPanel SouthPanel;
    private JPanel NorthPanel;
    private MyTableModelListener myTableModelListener;
    private MyKeyListener myKeyListener;
    private MyMouseListener myMouseListener;

    // Mouse buttons constants
    final int BUTTON_RIGHT = 3;

    public NavTable(FLyrVect layer) {
	super(layer);
    }

    public NavTable(SelectableDataSource recordset, String tableName) {
	super(recordset, tableName);
    }

    public boolean isFillingValues() {
	return isFillingValues;
    }

    public void setFillingValues(boolean isFillingValues) {
	this.isFillingValues = isFillingValues;
    }

    private JPanel getThisNorthPanel() {
	if (NorthPanel == null) {
	    NorthPanel = new JPanel();
	}
	return NorthPanel;
    }

    private JPanel getThisSouthPanel() {
	if (SouthPanel == null) {
	    SouthPanel = new JPanel();
	}
	return SouthPanel;
    }

    private JPanel getThisCenterPanel() {
	if (CenterPanel == null) {
	    CenterPanel = new JPanel();
	    BorderLayout CenterPanelLayout = new BorderLayout();
	    CenterPanel.setLayout(CenterPanelLayout);
	}
	return CenterPanel;
    }

    private void initGUI() {
	MigLayout thisLayout = new MigLayout("inset 0, align center", "[grow]",
		"[][grow][]");
	this.setLayout(thisLayout);
	this.add(getThisNorthPanel(), "shrink, wrap, align center");
	this.add(getThisCenterPanel(), "shrink, growx, growy, wrap");
	this.add(getThisSouthPanel(), "shrink, align center");
    }

    /**
     * It creates a panel with a table that shows the data linked to a feature
     * of the layer. Each row is a attribute-value pair.
     * 
     * @return the panel.
     */
    @Override
    public JPanel getCenterPanel() {
	GridBagLayout glayout = new GridBagLayout();
	GridBagConstraints c = new GridBagConstraints();

	c.weightx = 1.0;
	c.weighty = 1.0;
	c.fill = GridBagConstraints.BOTH;

	centerPanel = new JPanel(glayout);
	NavTableModel model = new NavTableModel();
	table = new JTable(model);

	myKeyListener = new MyKeyListener();
	table.addKeyListener(myKeyListener);

	myMouseListener = new MyMouseListener();
	table.addMouseListener(myMouseListener);

	this.cellRenderer = new AttribTableCellRenderer();

	model.addColumn(PluginServices.getText(this, "headerTableAttribute"));
	model.addColumn(PluginServices.getText(this, "headerTableValue"));

	TableColumn attribColumn = table.getColumn(PluginServices.getText(this,
		"headerTableAttribute"));
	attribColumn.setCellRenderer(this.cellRenderer);
	attribColumn = table.getColumn(PluginServices.getText(this,
		"headerTableValue"));
	attribColumn.setCellRenderer(this.cellRenderer);

	myTableModelListener = new MyTableModelListener();
	model.addTableModelListener(myTableModelListener);

	JScrollPane scrollPane = new JScrollPane(table);
	centerPanel.add(scrollPane, c);
	// centerPanel.setMinimumSize(new Dimension(300, 400));
	return centerPanel;
    }

    class MyMouseListener implements MouseListener {

	public void mouseClicked(MouseEvent e) {
	    /*
	     * TODO At the moment, filters do not work with automatic calculated
	     * fields "length" and "area". Besides, the filter panel only is
	     * activated if only 1 row is selected.
	     */
	    if (e.getButton() == BUTTON_RIGHT
		    && table.getSelectedRowCount() == 1
		    && !isSelectedRowAreaOrLength()) {

		JMenuItem[] menus = getFilterMenusForRowSelected();
		if (menus != null) {
		    JPopupMenu popup = new JPopupMenu();
		    for (byte x = 0; x < menus.length; x++) {
			popup.add(menus[x]);
		    }
		    popup.show(table, e.getX(), e.getY());
		}
	    }
	}

	private boolean isSelectedRowAreaOrLength() {
	    if (table.getSelectedRow() > (table.getRowCount() - 2)) {
		return true;
	    } else {
		return false;
	    }
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}
    }

    class MyKeyListener implements KeyListener {
	public void keyPressed(KeyEvent e) {
	}

	public void keyReleased(KeyEvent e) {
	    // TODO If control + cursor ---> Inicio / Fin
	    if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
		next();
	    }
	    if (e.getKeyCode() == KeyEvent.VK_LEFT) {
		before();
	    }
	    if (e.getKeyCode() == KeyEvent.VK_HOME) {
		first();
	    }
	    if (e.getKeyCode() == KeyEvent.VK_END) {
		last();
	    }
	}

	public void keyTyped(KeyEvent e) {
	}
    }

    class MyTableModelListener implements TableModelListener {
	public void tableChanged(TableModelEvent e) {
	    if (e.getType() == TableModelEvent.UPDATE && !isFillingValues()) {
		setChangedValues();
		enableSaveButton(isChangedValues());
	    }
	}
    }

    @Override
    public boolean init() {
	window = PluginServices.getMDIManager().getActiveWindow();
	try {
	    if (recordset.getRowCount() <= 0) {
		JOptionPane.showMessageDialog(this,
			PluginServices.getText(this, "emptyLayer"));
		return false;
	    }
	} catch (HeadlessException e) {
	    logger.error(e.getMessage(), e);
	} catch (ReadDriverException e) {
	    logger.error(e.getMessage(), e);
	}

	initGUI();

	// Getting NavTable parent panels and add them on the tableLayoutPanels
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
	setOpenNavTableForm(true);
	return true;

    }

    /**
     * It gets the alias name of the attributes in the alias file if exists
     * 
     * @param fieldName
     * @return alias
     */
    private String getAlias(String fieldName) {
	File layerFile = null;
	String filePath = null;
	String alias = fieldName;

	// Added to tables without Layer support, but must be supported alias
	// here also
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
		pathToken = Preferences.getAliasDir() + File.separator
			+ layer.getName();
		fileAlias = new File(pathToken + ".alias");
	    }
	} else if (source instanceof VectorialDBAdapter) {
	    pathToken = Preferences.getAliasDir() + File.separator
		    + layer.getName();
	    fileAlias = new File(pathToken + ".alias");
	} else {
	    return fieldName;
	}

	if (!fileAlias.exists()) {
	    return fieldName;
	}

	try {
	    String line;
	    BufferedReader fileReader = new BufferedReader(new FileReader(
		    fileAlias));
	    while ((line = fileReader.readLine()) != null) {
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
	    logger.error(e.getMessage(), e);
	} catch (IOException e) {
	    logger.error(e.getMessage(), e);
	}
	return alias;
    }

    /**
     * It gets the attributes names from the data table and sets them on the
     * left column.
     * 
     */
    private void fillAttributes() {
	try {
	    DefaultTableModel model = (DefaultTableModel) table.getModel();
	    Vector<String> aux = null;

	    // TODO: Some times it takes the deleted fields on the DBF
	    // FieldDescription[] fd = recordset.getFieldsDescription();
	    // fd[1].

	    for (String attName : recordset.getFieldNames()) {
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

		this.cellRenderer.addNoEditableRow(model.getRowCount() - 2);
		this.cellRenderer.addNoEditableRow(model.getRowCount() - 1);
	    }

	} catch (ReadDriverException e) {
	    logger.error(e.getMessage(), e);
	}
    }

    @Override
    public void fillEmptyValues() {
	super.fillEmptyValues();
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
	    for (int i = 0; i < recordset.getFieldCount(); i++) {
		Value value = recordset.getFieldValue(currentPosition, i);
		String textoValue = value
			.getStringValue(ValueWriter.internalValueWriter);
		textoValue = textoValue.replaceAll("'", "");
		if (value instanceof NullValue) {
		    textoValue = "";
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
		    model.setValueAt("0", recordset.getFieldCount() + 1, 1);
		    return;
		}
		Geometry geom = g.toJTSGeometry();
		// TODO Format number (Set units in Preferences)
		value = String.valueOf(Math.round(geom.getLength()));
		model.setValueAt(value, recordset.getFieldCount(), 1);
		// Fill GEOM_AREA
		value = "0.0";
		source.start();
		g = source.getShape(new Long(currentPosition).intValue());
		source.stop();
		geom = g.toJTSGeometry();
		// TODO Format number (Set units in Preferences)
		value = String.valueOf(Math.round(geom.getArea()));
		model.setValueAt(value, recordset.getFieldCount() + 1, 1);
	    }

	} catch (ReadDriverException e) {
	    logger.error(e.getMessage(), e);
	} finally {
	    setFillingValues(false);
	}
    }

    protected Vector<Integer> getChangedValues() {
	Vector<Integer> changedValues = new Vector<Integer>();
	DefaultTableModel model = (DefaultTableModel) table.getModel();
	try {
	    for (int i = 0; i < recordset.getFieldCount(); i++) {
		String tableValue = model.getValueAt(i, 1).toString();
		Value value = recordset.getFieldValue(currentPosition, i);
		String layerValue = value
			.getStringValue(ValueWriter.internalValueWriter);
		layerValue = layerValue.replaceAll("'", "");
		if (value instanceof NullValue) {
		    if (tableValue.compareTo("") != 0) {
			changedValues.add(new Integer(i));
		    }
		} else {
		    if (tableValue.compareTo(layerValue) != 0) {
			changedValues.add(new Integer(i));
		    }
		}
	    }
	} catch (ReadDriverException e) {
	    logger.error(e.getMessage(), e);
	}

	return changedValues;
    }

    protected void setChangedValues() {
	Vector<Integer> changedValues = getChangedValues();
	if (changedValues.size() > 0) {
	    setChangedValues(true);
	} else {
	    setChangedValues(false);
	}
    }

    @Override
    public void selectRow(int row) {
	table.setRowSelectionInterval(row, row);
    }

    @Override
    @Deprecated
    protected void saveRegister() {
	saveRecord();
    }

    protected boolean isSaveable() {
	stopCellEdition();

	// close all windows until get the view we're working on as the active
	// window.
	while (!window.equals(PluginServices.getMDIManager().getActiveWindow())) {
	    PluginServices.getMDIManager().closeWindow(
		    PluginServices.getMDIManager().getActiveWindow());
	}

	if (layer.isWritable()) {
	    return true;
	} else {
	    JOptionPane.showMessageDialog(this, String.format(
		    PluginServices.getText(this, "non_editable"),
		    layer.getName()));
	    return false;
	}
    }

    protected int[] getIndexes() {
	Vector<Integer> changedValues = getChangedValues();
	int[] attIndexes = new int[changedValues.size()];
	if (isChangedValues()) {
	    DefaultTableModel model = (DefaultTableModel) table.getModel();
	    int j = 0;
	    for (int i = 0; i < model.getRowCount(); i++) {
		if (changedValues.contains(new Integer(i))) {
		    attIndexes[j] = i;
		    j++;
		}
	    }
	}
	return attIndexes;
    }

    protected String[] getValues() {
	Vector<Integer> changedValues = getChangedValues();
	String[] attValues = new String[changedValues.size()];
	if (isChangedValues()) {
	    DefaultTableModel model = (DefaultTableModel) table.getModel();
	    int j = 0;
	    for (int i = 0; i < model.getRowCount(); i++) {
		if (changedValues.contains(new Integer(i))) {
		    try {
			Object value = model.getValueAt(i, 1);
			attValues[j] = value.toString();
			if (recordset.getFieldType(i) == Types.DATE) {
			    attValues[j] = attValues[j].replaceAll("-", "/");
			}
		    } catch (ReadDriverException e) {
			logger.error(e.getMessage(), e);
		    } finally {
			j++;
		    }
		}
	    }
	}
	return attValues;
    }

    @Override
    protected boolean saveRecord() {
	if (isSaveable()) {
	    int[] attIndexes = getIndexes();
	    String[] attValues = getValues();
	    int currentPos = Long.valueOf(currentPosition).intValue();
	    try {
		ToggleEditing te = new ToggleEditing();
		boolean wasEditing = layer.isEditing();
		if (!wasEditing) {
		    te.startEditing(layer);
		}
		te.modifyValues(layer, (int) currentPos, attIndexes, attValues);
		if (!wasEditing) {
		    te.stopEditing(layer, false);
		}
		setChangedValues(false);
		return true;
	    } catch (Exception e) {
		logger.error(e.getMessage(), e);
		return false;
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
    public void next() {
	stopCellEdition();
	super.next();
    }

    @Override
    public void before() {
	stopCellEdition();
	super.before();
    }

    @Override
    public void last() {
	stopCellEdition();
	super.last();
    }

    @Override
    public void first() {
	stopCellEdition();
	super.first();
    }

    @Override
    public void windowClosed() {
	stopCellEdition();
	this.table.getModel().removeTableModelListener(myTableModelListener);
	this.table.removeKeyListener(myKeyListener);
	super.windowClosed();
    }

    public Object getWindowProfile() {
	return WindowInfo.PROPERTIES_PROFILE;
    }

    private JMenuItem[] getFilterMenusForRowSelected() {
	int rowSelected = table.getSelectedRow();

	final String attrName = (String) table.getModel().getValueAt(
		rowSelected, 0);
	final String attrValue = (String) table.getModel().getValueAt(
		rowSelected, 1);
	final int attrType = getAttrTypeForValueSelected(rowSelected);

	final FiltroExtension filterExt = new FiltroExtension();
	filterExt.setDatasource(recordset, "");

	DefaultExpressionDataSource ds = new DefaultExpressionDataSource();
	ds.setTable(recordset);

	FBitSet fbitset = recordset.getSelection();
	boolean isFilterSet = (fbitset.cardinality() > 0);
	int itemSetOffFilter = (isFilterSet ? 1 : 0);

	String dataSourceName = ds.getDataSourceName();
	final String st_expr = "select * from '" + dataSourceName + "' where "
		+ attrName;

	// TODO USE newSet(), fromSet(), addSet?
	// TODO Maybe a good idea is using JCheckBoxMenuItem with a
	// ItemListener to get state
	JMenuItem[] menus = null;
	if (attrType == java.sql.Types.VARCHAR) {
	    menus = new JMenuItem[4 + itemSetOffFilter];

	    menus[0] = new JMenuItem(PluginServices.getText(this,
		    "filter_equals") + " '" + attrValue + "'");
	    menus[0].addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent evt) {
		    String exp = st_expr + " = '" + attrValue + "';";
		    logger.warn(exp);
		    filterExt.newSet(exp);
		}
	    });

	    menus[1] = new JMenuItem(PluginServices.getText(this,
		    "filter_different") + " '" + attrValue + "'");
	    menus[1].addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent evt) {
		    String exp = st_expr + " != '" + attrValue + "';";
		    logger.warn(exp);
		    filterExt.newSet(exp);
		}
	    });

	    menus[2] = new JMenuItem(PluginServices.getText(this,
		    "filter_contains"));
	    menus[2].addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent evt) {
		    String attr = (String) JOptionPane.showInputDialog(
			    NavTable.this, PluginServices.getText(this,
				    "filter_write_string"), PluginServices
				    .getText(this,
					    "filter_contains_window_title"),
			    JOptionPane.PLAIN_MESSAGE);

		    if (attr != null) {
			// TODO: We need to escape special characters
			// like '%', "'", ...
			String expr = st_expr + " like '%" + attr + "%';";
			logger.warn(expr);
			filterExt.newSet(expr);
		    }
		}
	    });

	    menus[3] = new JMenuItem(PluginServices.getText(this,
		    "filter_filter"), getIcon("/filter.png"));
	    menus[3].addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent evt) {
		    filterExt.initialize();
		    filterExt.setDatasource(recordset, dataName);
		    filterExt.execute("FILTER_DATASOURCE");
		}
	    });

	    if (isFilterSet) {
		menus[4] = new JMenuItem(PluginServices.getText(this,
			"filter_remove_filter"), getIcon("/nofilter.png"));
		menus[4].addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent evt) {
			clearSelection();
		    }
		});
	    }

	} else if (attrType == java.sql.Types.DOUBLE
		|| attrType == java.sql.Types.INTEGER) {

	    menus = new JMenuItem[5 + itemSetOffFilter];

	    menus[0] = new JMenuItem(PluginServices.getText(this,
		    "filter_numeric_equals") + " \t'" + attrValue + "'");
	    menus[0].addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent evt) {
		    String expr = st_expr + " = " + attrValue + ";";
		    logger.warn(expr);
		    filterExt.newSet(expr);
		}
	    });

	    menus[1] = new JMenuItem(PluginServices.getText(this,
		    "filter_numeric_different") + " \t'" + attrValue + "'");
	    menus[1].addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent evt) {
		    String expr = st_expr + " != " + attrValue + ";";
		    logger.warn(expr);
		    filterExt.newSet(expr);
		}
	    });

	    menus[2] = new JMenuItem(PluginServices.getText(this,
		    "filter_numeric_less") + " \t'" + attrValue + "'");
	    menus[2].addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent evt) {
		    // TODO: Still not working. Remove option with
		    // numbers. Open a dialog to type the '%...%'?
		    String expr = st_expr + " < " + attrValue + ";";
		    logger.warn(expr);
		    filterExt.newSet(expr);
		}
	    });

	    menus[3] = new JMenuItem(PluginServices.getText(this,
		    "filter_numeric_greater") + " \t'" + attrValue + "'");
	    menus[3].addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent evt) {
		    // TODO: Still not working. Remove option with
		    // numbers. Open a dialog to type the '%...%'?
		    String expr = st_expr + " > " + attrValue + ";";
		    logger.warn(expr);
		    filterExt.newSet(expr);
		}
	    });

	    menus[4] = new JMenuItem(PluginServices.getText(this,
		    "filter_filter"), getIcon("/filter.png"));
	    menus[4].addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent evt) {
		    filterExt.initialize();
		    filterExt.setDatasource(recordset, dataName);
		    filterExt.execute("FILTER_DATASOURCE");
		}
	    });

	    if (isFilterSet) {
		menus[5] = new JMenuItem(PluginServices.getText(this,
			"filter_remove_filter"), getIcon("/nofilter.png"));
		menus[5].addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent evt) {
			clearSelection();
		    }
		});
	    }

	} else if (attrType == java.sql.Types.BOOLEAN
		|| attrType == java.sql.Types.BIT) {
	    menus = new JMenuItem[3 + itemSetOffFilter];

	    menus[0] = new JMenuItem(PluginServices.getText(this,
		    "filter_equals") + " = TRUE");
	    menus[0].addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent evt) {
		    String expr = st_expr + " = boolean('true');";
		    logger.warn(expr);
		    filterExt.newSet(expr);
		}
	    });

	    menus[1] = new JMenuItem(PluginServices.getText(this,
		    "filter_equals") + " = FALSE");
	    menus[1].addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent evt) {
		    String expr = st_expr + " = boolean('false');";
		    logger.warn(expr);
		    filterExt.newSet(expr);
		}
	    });

	    menus[2] = new JMenuItem(PluginServices.getText(this,
		    "filter_filter"), getIcon("/filter.png"));
	    menus[2].addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent evt) {
		    filterExt.initialize();
		    filterExt.setDatasource(recordset, dataName);
		    filterExt.execute("FILTER_DATASOURCE");
		}
	    });

	    if (isFilterSet) {
		menus[3] = new JMenuItem(PluginServices.getText(this,
			"filter_remove_filter"), getIcon("/nofilter.png"));
		menus[3].addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent evt) {
			clearSelection();
		    }
		});
	    }

	} else {
	    // TODO OTHER TYPES (like DATE)
	    // the filter menu will not be shown on these types
	    menus = null;
	}
	return menus;
    }

    private int getAttrTypeForValueSelected(int fieldIndex) {
	int attrType = -1;
	try {
	    attrType = recordset.getFieldType(fieldIndex);
	} catch (ReadDriverException e1) {
	    attrType = -1;
	    logger.error(e1.getMessage());
	} finally {
	    return attrType;
	}
    }
}
