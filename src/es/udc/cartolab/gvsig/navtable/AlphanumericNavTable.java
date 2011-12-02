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
import java.net.URL;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;

import com.hardcode.gdbms.driver.exceptions.InitializeWriterException;
import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.engine.values.Value;
import com.hardcode.gdbms.engine.values.ValueFactory;
import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.exceptions.visitors.StartWriterVisitorException;
import com.iver.cit.gvsig.exceptions.visitors.StopWriterVisitorException;
import com.iver.cit.gvsig.fmap.core.DefaultRow;
import com.iver.cit.gvsig.fmap.core.IRow;
import com.iver.cit.gvsig.fmap.drivers.ITableDefinition;
import com.iver.cit.gvsig.fmap.edition.EditionEvent;
import com.iver.cit.gvsig.fmap.edition.IEditableSource;
import com.iver.cit.gvsig.fmap.edition.IWriteable;
import com.iver.cit.gvsig.fmap.edition.IWriter;


/**
 * @author Nacho Varela
 * @author Javier Estevez
 * @author Pablo Sanxiao
 * @author Andres Maneiro
 */
public class AlphanumericNavTable extends NavTable {

    JButton newB = null;
    protected IEditableSource model;
    protected HashMap<String, String> defaultValues = null;

    public AlphanumericNavTable(IEditableSource model, String dataName)
	    throws ReadDriverException {
	super(model.getRecordset(), dataName);
	this.isAlphanumericNT = true;
	this.model = model;
	this.model.addEditionListener(listener);
    }

    public AlphanumericNavTable(IEditableSource model, String dataName,
	    HashMap<String, String> defaultValues) throws ReadDriverException {
	super(model.getRecordset(), dataName);
	this.isAlphanumericNT = true;
	this.model = model;
	this.defaultValues = defaultValues;
    }

    @Override
    public boolean init() {
	if (!super.init()) {
	    return false;
	}

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
    protected boolean isSaveable() {
	stopCellEdition();
	if (model instanceof IWriteable) {
	    return true;
	} else {
	    JOptionPane.showMessageDialog(this, String.format(
		    PluginServices.getText(this, "non_editable"),
		    layer.getName()));
	    return false;
	}
    }

    @Override
    @Deprecated
    protected void saveRegister() {
	saveRecord();
    }

    @Override
    protected boolean saveRecord() {
	if (isSaveable()) {
	    int[] attIndexes = getIndexes();
	    String[] attValues = getValues();
	    int currentPos = Long.valueOf(currentPosition).intValue();
	    try {
		ToggleEditing te = new ToggleEditing();
		if (!model.isEditing()) {
		    te.startEditing(model);
		}
		te.modifyValues(model, currentPos, attIndexes, attValues);
		if (model.isEditing()) {
		    te.stopEditing(model);
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

    @Deprecated
    private void addRow() {
	addRecord();
    }

    private void addRecord() {
	// Create a new empty record
	// showWarning();
	if (onlySelectedCB.isSelected()) {
	    onlySelectedCB.setSelected(false);
	}
	try {
	    model.startEdition(EditionEvent.ALPHANUMERIC);
	    if (model instanceof IWriteable) {
		IRow row;
		int numAttr = recordset.getFieldCount();
		Value[] values = new Value[numAttr];
		if (defaultValues == null)
		    for (int i = 0; i < numAttr; i++) {
			values[i] = ValueFactory.createNullValue();
		    }
		else
		    for (int i = 0; i < numAttr; i++) {
			if (defaultValues.get(recordset.getFieldAlias(i)) == null)
			    values[i] = ValueFactory.createNullValue();
			else
			    values[i] = ValueFactory.createValue(defaultValues
				    .get(recordset.getFieldAlias(i)));
		    }
		row = new DefaultRow(values);
		model.doAddRow(row, EditionEvent.ALPHANUMERIC);

		IWriteable w = (IWriteable) model;
		IWriter writer = w.getWriter();

		ITableDefinition tableDef = model.getTableDefinition();
		writer.initialize(tableDef);

		model.stopEdition(writer, EditionEvent.ALPHANUMERIC);
		last();
		setChangedValues(true);
		refreshGUI();
	    }
	} catch (StartWriterVisitorException e) {
	    logger.error(e.getMessage(), e);
	} catch (ReadDriverException e) {
	    logger.error(e.getMessage(), e);
	} catch (InitializeWriterException e) {
	    logger.error(e.getMessage(), e);
	} catch (StopWriterVisitorException e) {
	    logger.error(e.getMessage(), e);
	}
    }

    /**
     * Shows a warning to the user if there's unsaved data.
     * 
     */
    protected void showWarning() {
	if (currentPosition == -1) {
	    return;
	}
	boolean changed = isChangedValues();
	boolean save = changed && model.isEditing();
	if (changed && !save) {
	    Object[] options = {
		    PluginServices.getText(this, "saveButtonTooltip"),
		    PluginServices.getText(this, "ignoreButton") };
	    int response = JOptionPane.showOptionDialog(this,
		    PluginServices.getText(this, "unsavedDataMessage"),
		    PluginServices.getText(this, "unsavedDataTitle"),
		    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
		    null, // do not use a custom Icon
		    options, // the titles of buttons
		    options[1]); // default button title
	    if (response == 0) {
		save = true;
	    } else {
		setChangedValues(false);
	    }
	}
	if (save) {
	    saveRecord();
	}
    }

    @Override
    protected void deleteRecord() {
	try {
	    model.startEdition(EditionEvent.ALPHANUMERIC);

	    IWriteable w = (IWriteable) model;
	    IWriter writer = w.getWriter();

	    ITableDefinition tableDef = model.getTableDefinition();
	    writer.initialize(tableDef);

	    model.doRemoveRow((int) currentPosition, EditionEvent.ALPHANUMERIC);
	    model.stopEdition(writer, EditionEvent.ALPHANUMERIC);

	    // Refresh
	    refreshGUI();

	} catch (StartWriterVisitorException e) {
	    logger.error(e.getMessage(), e);
	} catch (ReadDriverException e) {
	    logger.error(e.getMessage(), e);
	} catch (InitializeWriterException e) {
	    logger.error(e.getMessage(), e);
	} catch (StopWriterVisitorException e) {
	    logger.error(e.getMessage(), e);
	}

    }

    @Override
    public void actionPerformed(ActionEvent e) {
	if (e.getSource() == newB) {
	    addRecord();
	} else if (e.getSource() == removeB) {
	    int answer = JOptionPane.showConfirmDialog(null,
		    PluginServices.getText(null, "confirm_delete_register"),
		    null, JOptionPane.YES_NO_OPTION);
	    if (answer == JOptionPane.YES_OPTION) {
		deleteRecord();
	    }
	} else {
	    super.actionPerformed(e);
	}
    }

    @Override
    public void windowClosed() {
	this.newB.removeActionListener(this);
	super.windowClosed();
	this.model.removeEditionListener(listener);
    }
}
