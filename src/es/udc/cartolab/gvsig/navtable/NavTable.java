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
import com.hardcode.gdbms.engine.values.DateValue;
import com.hardcode.gdbms.engine.values.NullValue;
import com.hardcode.gdbms.engine.values.Value;
import com.hardcode.gdbms.engine.values.ValueWriter;
import com.iver.andami.PluginServices;
import com.iver.andami.ui.mdiManager.WindowInfo;
import com.iver.cit.gvsig.fmap.MapControl;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.ReadableVectorial;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;
import com.iver.cit.gvsig.fmap.layers.VectorialDBAdapter;
import com.iver.cit.gvsig.fmap.layers.VectorialFileAdapter;
import com.iver.cit.gvsig.fmap.layers.layerOperations.AlphanumericData;
import com.iver.utiles.extensionPoints.ExtensionPoint;
import com.iver.utiles.extensionPoints.ExtensionPoints;
import com.iver.utiles.extensionPoints.ExtensionPointsSingleton;
import com.vividsolutions.jts.geom.Geometry;

import es.udc.cartolab.gvsig.navtable.contextualmenu.INavTableContextMenu;
import es.udc.cartolab.gvsig.navtable.format.DateFormatNT;
import es.udc.cartolab.gvsig.navtable.listeners.PositionEvent;
import es.udc.cartolab.gvsig.navtable.listeners.PositionListener;
import es.udc.cartolab.gvsig.navtable.preferences.Preferences;
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
 */
public class NavTable extends AbstractNavTable implements PositionListener {

    private static final long serialVersionUID = 1L;
    protected WindowInfo viewInfo = null;

    private boolean isFillingValues = false;
    private boolean isSavingValues = false;

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

    public NavTable(MapControl mapControl, FLyrVect layer) {
	super(mapControl, layer);
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
	table.getTableHeader().setReorderingAllowed(false);

	myKeyListener = new MyKeyListener();
	table.addKeyListener(myKeyListener);

	myMouseListener = new MyMouseListener(this);
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

	private NavTable navtable;

	public MyMouseListener(NavTable navTable) {
	    this.navtable = navTable;
	}

	public void mouseClicked(MouseEvent e) {
	    /*
	     * TODO At the moment, filters do not work with automatic calculated
	     * fields "length" and "area". Besides, the filter panel only is
	     * activated if only 1 row is selected.
	     */
	    if (e.getButton() == BUTTON_RIGHT) {

		JPopupMenu popup = new JPopupMenu();

		ExtensionPoint extensionPoint = (ExtensionPoint) ExtensionPointsSingleton
			.getInstance().get(
				AbstractNavTable.NAVTABLE_CONTEXT_MENU);
		for (Object contextMenuAddon : extensionPoint.values()) {
		    try {
			INavTableContextMenu c = (INavTableContextMenu) contextMenuAddon;
			c.setNavtableInstance(navtable);
			if (c.isVisible()) {
			    for (JMenuItem m : c.getMenuItems()) {
				popup.add(m);
			    }
			}
		    } catch (ClassCastException cce) {
			logger.error("Class is not a navtable context menu",
				cce);
		    }
		}
		if (popup.getComponents().length != 0) {
		    popup.show(table, e.getX(), e.getY());
		}

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
		// if layer is on edition, we need to update the recordset
		// with the values that changed. If not, the values will be
		// saved in "batch mode" when clicking on saving button
		if (isEditing()) {
		    updateValueInRecordset(e);
		} else {
		    setChangedValues();
		    enableSaveButton(isChangedValues());
		}
	    }
	}

	private void updateValueInRecordset(TableModelEvent e) {
	    int col = 1; // edition only happens on editing column
	    int row = e.getFirstRow();
	    String newValue = ((DefaultTableModel) table.getModel())
		    .getValueAt(row, col).toString();
	    updateValue(Long.valueOf(getPosition()).intValue(), row, newValue);
	}
    }

    protected boolean isEditing() {
	return layer.isEditing();
    }

    protected void updateValue(int row, int col, String newValue) {
	ToggleEditing te = new ToggleEditing();
	try {
	    te.modifyValue(layer, row, col, newValue);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    @Override
    protected void registerNavTableButtonsOnActionToolBarExtensionPoint() {
	/*
	 * TODO: this will make the extension point mechanims not work as
	 * expected for navtable extension. It only will add the regular buttons
	 * navtable has, not all included in the extensionpoint (as the default
	 * behaviour is).
	 * 
	 * Probably we need to get rid of extensionpoints mechanism as -roughly-
	 * it is a global variable mechanism, which is not what we need. For
	 * action buttons, it'll be desirable a mechanism that:
	 * 
	 * 1) allow adding buttons for custom forms build on abstractnavtable.
	 * 2) don't share those buttons between all children of
	 * abstractnavtable, unless requested otherwise.
	 * 
	 * Check decorator pattern, as it seems to fit well here.
	 */
	ExtensionPoints extensionPoints = ExtensionPointsSingleton
		.getInstance();
	ExtensionPoint ep = (ExtensionPoint) extensionPoints
		.get(AbstractNavTable.NAVTABLE_ACTIONS_TOOLBAR);

	if (ep != null) {
	    ep.clear();
	}
	super.registerNavTableButtonsOnActionToolBarExtensionPoint();
    }

    @Override
    public boolean init() {
	SelectableDataSource sds = getRecordset();
	sds.addSelectionListener(this);
	this.addPositionListener(this);
	try {
	    if ((!openEmptyLayers) && (sds.getRowCount() <= 0)) {
		JOptionPane.showMessageDialog(this,
			PluginServices.getText(this, "emptyLayer"));
		this.layer.removeLayerListener(this.listener);
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
	setPosition(0);

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
	String pathToken = null;
	File fileAlias = null;

	// Added to tables without Layer support, but must be supported alias
	// here also
	if (layer == null) {
	    pathToken = dataName.contains(".") ? Preferences.getAliasDir()
		    + File.separator
		    + dataName.substring(0, dataName.lastIndexOf("."))
		    : Preferences.getAliasDir() + File.separator + dataName;
		    fileAlias = new File(pathToken + ".alias");
	} else {
	    ReadableVectorial source = layer.getSource();

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
	    model.setRowCount(0);
	    Vector<String> aux = null;

	    SelectableDataSource sds = getRecordset();
	    for (int i = 0; i < sds.getFieldCount(); i++) {
		String attName = sds.getFieldName(i);
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

		this.cellRenderer.emptyNoEditableRows();

		this.cellRenderer.addNoEditableRow(model.getRowCount() - 2);
		this.cellRenderer.addNoEditableRow(model.getRowCount() - 1);
	    }

	} catch (ReadDriverException e) {
	    logger.error(e.getMessage(), e);
	}
    }

    public void fillEmptyValues() {
	setFillingValues(true);
	DefaultTableModel model = (DefaultTableModel) table.getModel();
	for (int i = 0; i < model.getRowCount(); i++) {
	    model.setValueAt("", i, 1);
	}
	setFillingValues(false);
    }

    public boolean isSavingValues() {
	return isSavingValues;
    }

    public void setSavingValues(boolean bool) {
	isSavingValues = bool;
    }

    @Override
    public void fillValues() {
	SelectableDataSource sds;
	try {
	    sds = getRecordset();
	    setFillingValues(true);
	    DefaultTableModel model = (DefaultTableModel) table.getModel();
	    for (int i = 0; i < sds.getFieldCount(); i++) {
		Value value = sds.getFieldValue(getPosition(), i);
		String textoValue;
		if (value instanceof NullValue) {
		    textoValue = "";
		} else if (value instanceof DateValue) {
		    textoValue = DateFormatNT.convertDateValueToString(value);
		} else {
		    textoValue = value
			    .getStringValue(ValueWriter.internalValueWriter);
		    textoValue = textoValue.replaceAll("'", "");
		}
		model.setValueAt(textoValue, i, 1);
	    }

	    if (layer != null && layer instanceof AlphanumericData) {
		// Fill GEOM_LENGTH
		String value = "0.0";
		IGeometry g;
		ReadableVectorial source = (layer).getSource();
		source.start();
		g = source.getShape(new Long(getPosition()).intValue());
		source.stop();
		if (g == null) {
		    model.setValueAt("0", sds.getFieldCount(), 1);
		    model.setValueAt("0", sds.getFieldCount() + 1, 1);
		    return;
		}
		Geometry geom = g.toJTSGeometry();
		// TODO Format number (Set units in Preferences)
		value = String.valueOf(Math.round(geom.getLength()));
		model.setValueAt(value, sds.getFieldCount(), 1);
		// Fill GEOM_AREA
		value = "0.0";
		source.start();
		g = source.getShape(new Long(getPosition()).intValue());
		source.stop();
		geom = g.toJTSGeometry();
		// TODO Format number (Set units in Preferences)
		value = String.valueOf(Math.round(geom.getArea()));
		model.setValueAt(value, sds.getFieldCount() + 1, 1);
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
	    SelectableDataSource sds = getRecordset();
	    for (int i = 0; i < sds.getFieldCount(); i++) {
		String tableValue = model.getValueAt(i, 1).toString();
		Value value = sds.getFieldValue(getPosition(), i);
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
			if (getRecordset().getFieldType(i) == Types.DATE) {
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
    public boolean saveRecord() {
	if (isSaveable()) {
	    setSavingValues(true);
	    int[] attIndexes = getIndexes();
	    String[] attValues = getValues();
	    int currentPos = Long.valueOf(getPosition()).intValue();
	    try {
		ToggleEditing te = new ToggleEditing();
		boolean wasEditing = layer.isEditing();
		MapControl mc = getMapControl();
		if (!wasEditing) {
		    te.startEditing(mc, layer);
		}
		te.modifyValues(layer, (int) currentPos, attIndexes, attValues);
		if (!wasEditing) {
		    te.stopEditing(mc, layer, false);
		}
		setChangedValues(false);
		return true;
	    } catch (Exception e) {
		logger.error(e.getMessage(), e);
		return false;
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

    public void reloadRecordset() throws ReadDriverException {
	super.reloadRecordset();
	fillAttributes();
    }

    public JTable getTable() {
	return this.table;
    }

    @Override
    public SelectableDataSource getRecordset() {
	try {
	    return layer.getSource().getRecordset();
	} catch (ReadDriverException e) {
	    e.printStackTrace();
	    return null;
	}
    }

    public void onPositionChange(PositionEvent e) {
	refreshGUI();
    }

}
