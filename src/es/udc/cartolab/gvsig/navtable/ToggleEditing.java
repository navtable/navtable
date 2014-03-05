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
 *   Jorge Lopez Fernandez <jlopez (at) cartolab (dot) es>
 */
package es.udc.cartolab.gvsig.navtable;

import java.io.IOException;
import java.text.ParseException;

import org.apache.log4j.Logger;

import com.hardcode.gdbms.driver.exceptions.InitializeWriterException;
import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.engine.data.driver.DriverException;
import com.hardcode.gdbms.engine.values.Value;
import com.hardcode.gdbms.engine.values.ValueFactory;
import com.iver.andami.PluginServices;
import com.iver.andami.messages.NotificationManager;
import com.iver.cit.gvsig.EditionUtilities;
import com.iver.cit.gvsig.ProjectExtension;
import com.iver.cit.gvsig.exceptions.expansionfile.ExpansionFileReadException;
import com.iver.cit.gvsig.exceptions.expansionfile.ExpansionFileWriteException;
import com.iver.cit.gvsig.exceptions.layers.CancelEditingLayerException;
import com.iver.cit.gvsig.exceptions.layers.StartEditionLayerException;
import com.iver.cit.gvsig.exceptions.table.CancelEditingTableException;
import com.iver.cit.gvsig.exceptions.validate.ValidateRowException;
import com.iver.cit.gvsig.exceptions.visitors.StartWriterVisitorException;
import com.iver.cit.gvsig.exceptions.visitors.StopWriterVisitorException;
import com.iver.cit.gvsig.fmap.core.DefaultFeature;
import com.iver.cit.gvsig.fmap.core.DefaultRow;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.IRow;
import com.iver.cit.gvsig.fmap.drivers.FieldDescription;
import com.iver.cit.gvsig.fmap.drivers.ILayerDefinition;
import com.iver.cit.gvsig.fmap.drivers.ITableDefinition;
import com.iver.cit.gvsig.fmap.edition.EditionEvent;
import com.iver.cit.gvsig.fmap.edition.EditionExceptionOld;
import com.iver.cit.gvsig.fmap.edition.IEditableSource;
import com.iver.cit.gvsig.fmap.edition.IRowEdited;
import com.iver.cit.gvsig.fmap.edition.ISpatialWriter;
import com.iver.cit.gvsig.fmap.edition.IWriteable;
import com.iver.cit.gvsig.fmap.edition.IWriter;
import com.iver.cit.gvsig.fmap.edition.VectorialEditableAdapter;
import com.iver.cit.gvsig.fmap.edition.rules.IRule;
import com.iver.cit.gvsig.fmap.edition.rules.RulePolygon;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.project.documents.table.ProjectTable;
import com.iver.cit.gvsig.project.documents.table.gui.Table;

import es.udc.cartolab.gvsig.navtable.format.ValueFactoryNT;

/**
 * Class for start, stop or toggle the editing on a vector layer. Based on the
 * StartingEditing Extension of gvSIG
 * 
 * @author Nacho Varela
 * @author Javier Estevez
 * @author Pablo Sanxiao
 * @author Francisco Puga
 * @author Andres Maneiro
 * @author Jorge Lopez
 */

public class ToggleEditing {

    protected static Logger logger = Logger.getLogger("ToggleEditing");

    /**
     * @param layer - The vectorial layer to be edited.
     */
    public boolean startEditing(FLayer layer) {

	if (layer instanceof FLyrVect) {
	    layer.setActive(true);

	    FLyrVect lv = (FLyrVect) layer;

	    try {
		lv.setEditing(true);
	    } catch (StartEditionLayerException e) {
		logger.error(e.getMessage(), e);
	    }
	    VectorialEditableAdapter vea = (VectorialEditableAdapter) lv
		    .getSource();

	    vea.getRules().clear();
	    try {
		if (vea.getShapeType() == FShape.POLYGON) {
		    IRule rulePol = new RulePolygon();
		    vea.getRules().add(rulePol);
		}
	    } catch (ReadDriverException e) {
		logger.error(e.getMessage(), e);
		return false;
	    }

	    // If there's a table linked to this layer, its model is changed
	    // to VectorialEditableAdapter.
	    ProjectExtension pe = (ProjectExtension) PluginServices
		    .getExtension(ProjectExtension.class);
	    if (pe != null) {
		ProjectTable pt = pe.getProject().getTable(lv);
		if (pt != null) {
		    pt.setModel(vea);
		    Table table = getModelTable(pt);
		    if(table != null) {
			table.setModel(pt);
			vea.getCommandRecord().addCommandListener(table);
		    }
		}
	    }
	}
	return true;
    }

    public boolean startEditing(IEditableSource source) {
	try {
	    source.startEdition(EditionEvent.ALPHANUMERIC);
	    return true;
	} catch (StartWriterVisitorException e) {
	    logger.error(e.getMessage(), e);
	    return false;
	}
    }

    /**
     * @param layer
     *            - The layer wich edition will be stoped.
     * @param cancel
     *            - false if we want to save the layer, true if we don't.
     * @throws StopWriterVisitorException
     */
    public boolean stopEditing(FLayer layer, boolean cancel)
	    throws StopWriterVisitorException {

	if (layer instanceof FLyrVect) {
	    try {
		if (cancel) {
		    cancelEdition(layer);
		} else {
		    saveLayer((FLyrVect) layer);
		}
	    } catch (DriverException e) {
		logger.error(e.getMessage(), e);
		return false;
	    } catch (EditionExceptionOld e) {
		logger.error(e.getMessage(), e);
		return false;
	    } finally {
		try {
		    layer.setActive(true);
		    layer.setEditing(false);
		} catch (StartEditionLayerException e) {
		    logger.error(e.getMessage(), e);
		    return false;
		}
	    }
	    return true;
	}
	return false;
    }

    private void cancelEdition(FLayer layer) {
	layer.setProperty("stoppingEditing", new Boolean(true));
	VectorialEditableAdapter vea = (VectorialEditableAdapter)
		((FLyrVect) layer).getSource();
	try {
	    vea.cancelEdition(EditionEvent.GRAPHIC);
	} catch (CancelEditingLayerException e) {
	    logger.error(e.getMessage(), e);
	}
	Table table = getTableFromLayer(layer);
	if(table != null){
	    try {
		table.cancelEditing();
	    } catch (CancelEditingTableException e) {
		logger.error(e.getMessage(), e);
	    }
	}
	layer.setProperty("stoppingEditing", new Boolean(false));
    }

    public boolean stopEditing(IEditableSource source) {
	try {
	    IWriteable w = (IWriteable) source;
	    IWriter writer = w.getWriter();
	    if (writer == null) {
		NotificationManager.addError(
			"No existe driver de escritura para la tabla"
				+ source.getRecordset().getName(),
				new EditionExceptionOld());
		return false;
	    } else {
		ITableDefinition tableDef = source.getTableDefinition();
		writer.initialize(tableDef);
		source.stopEdition(writer, EditionEvent.ALPHANUMERIC);
		return true;
	    }
	} catch (ReadDriverException e) {
	    logger.error(e.getMessage(), e);
	    return false;
	} catch (InitializeWriterException e) {
	    logger.error(e.getMessage(), e);
	    return false;
	} catch (StopWriterVisitorException e) {
	    logger.error(e.getMessage(), e);
	    return false;
	}
    }

    private void saveLayer(FLyrVect layer) throws DriverException,
	    EditionExceptionOld, StopWriterVisitorException {
	layer.setProperty("stoppingEditing", new Boolean(true));
	VectorialEditableAdapter vea = (VectorialEditableAdapter) layer
		.getSource();

	ISpatialWriter writer = (ISpatialWriter) vea.getWriter();
	Table table = getTableFromLayer(layer);
	if(table != null){
	    table.stopEditingCell();
	}
	vea.cleanSelectableDatasource();
	try {
	    layer.setRecordset(vea.getRecordset());
	} catch (ReadDriverException e) {
	    logger.error(e.getMessage(), e);
	}
	// The layer recordset must have the changes we made
	ILayerDefinition lyrDef;
	try {
	    lyrDef = EditionUtilities.createLayerDefinition(layer);
	    String aux = "FIELDS:";
	    FieldDescription[] flds = lyrDef.getFieldsDesc();
	    for (int i = 0; i < flds.length; i++) {
		aux = aux + ", " + flds[i].getFieldAlias();
	    }

	    lyrDef.setShapeType(layer.getShapeType());
	    writer.initialize(lyrDef);
	    vea.stopEdition(writer, EditionEvent.GRAPHIC);
	    layer.setProperty("stoppingEditing", new Boolean(false));
	} catch (ReadDriverException e) {
	    logger.error(e.getMessage(), e);
	} catch (InitializeWriterException e) {
	    logger.error(e.getMessage(), e);
	}
    }

    /**
     * Modify a single value of a register. It creates the new value from its
     * String representation. IMPORTANT: StartEditing and StopEditing is
     * required before and after call this method.
     * 
     * @param layer
     *            the layer that contains the feature to be changed.
     * @param rowPosition
     *            the data row to be changed.
     * @param colPosition
     *            the data column position to be changed.
     * @param newValue
     *            the String representation of the value to be saved.
     * 
     */
    public void modifyValue(FLyrVect layer, int rowPosition, int colPosition,
	    String newValue) throws Exception {

	IEditableSource source = (IEditableSource) layer.getSource();
	Value value = getNewAttribute(source, colPosition, newValue);
	modifyValue(layer, rowPosition, colPosition, value);
    }

    /**
     * Modidify a single value of a register. IMPORTANT: StartEditing and
     * StopEditing is required before and after call this method.
     * 
     * @param layer
     *            the layer that contains the feature to be changed.
     * @param rowPosition
     *            the data row to be changed.
     * @param colPosition
     *            the data column position to be changed.
     * @param newValue
     *            the value to be saved.
     * 
     * @throws DriverException
     * @throws IOException
     * 
     */
    public void modifyValue(FLyrVect layer, int rowPosition, int colPosition,
	    Value newValue) throws Exception {

	IEditableSource source = (IEditableSource) layer.getSource();
	IRowEdited row = source.getRow(rowPosition);
	Value[] attributes = row.getAttributes();
	if (row.getLinkedRow() instanceof IFeature) {
	    attributes[colPosition] = newValue;
	    IGeometry geometry = getTheGeom(source, rowPosition);
	    IRow newRow = new DefaultFeature(geometry, attributes,
		    row.getID());
	    source.modifyRow(rowPosition, newRow, "NAVTABLE MODIFY",
		    EditionEvent.ALPHANUMERIC);
	} else {
	    logger.error("Row has no geometry");
	}
    }

    public void modifyValue(IEditableSource source, int rowPosition, int colPosition,
	    String newValue) throws Exception {

	IRowEdited row = source.getRow(rowPosition);
	Value[] attributes = row.getAttributes();
	attributes[colPosition] = getNewAttribute(source, colPosition, newValue);
	IRow newRow = new DefaultRow(attributes);
	source.modifyRow(rowPosition, newRow, "NAVTABLE MODIFY",
		EditionEvent.ALPHANUMERIC);
    }

    /**
     * Modify an the desired of values of a register IMPORTANT: StartEditing and
     * StopEditing is required before and after call this method.
     * 
     * @param layer
     *            the layer that contains the feature to be changed.
     * @param rowPosition
     *            the data row to be changed.
     * @param attIndexes
     *            An array that contains the index of the columns that will be
     *            modified.
     * @param attValues
     *            the new values to save (in correlative order with attPos)
     * 
     * @throws DriverException
     * @throws IOException
     * 
     */
    public void modifyValues(FLyrVect layer, 
	    int rowPosition, 
	    int[] attIndexes,
	    String[] attValues) {

	try {
	    IEditableSource source = (IEditableSource) layer.getSource();

	    IGeometry geometry = getTheGeom(source, rowPosition);
	    Value[] values = getNewAttributes(source, rowPosition, attIndexes, attValues);

	    IRowEdited row = source.getRow(rowPosition);
	    IRow newRow = new DefaultFeature(geometry, values, row.getID());
	    source.modifyRow(rowPosition, newRow, "NAVTABLE MODIFY",
		    EditionEvent.ALPHANUMERIC);
	} catch (ExpansionFileWriteException e) {
	    logger.error(e.getMessage(), e);
	} catch (ExpansionFileReadException e) {
	    logger.error(e.getMessage(), e);
	} catch (ValidateRowException e) {
	    logger.error(e.getMessage(), e);
	} catch (ReadDriverException e) {
	    logger.error(e.getMessage(), e);
	}
    }

    public void modifyValues(IEditableSource source, int rowPosition,
	    int[] attIndexes, String[] attValues) {
	try {
	    Value[] attributes = getNewAttributes(
		    source, rowPosition, attIndexes, attValues);

	    IRow newRow = new DefaultRow(attributes);
	    source.modifyRow(rowPosition, newRow, "NAVTABLE MODIFY",
		    EditionEvent.ALPHANUMERIC);
	} catch (ExpansionFileReadException e) {
	    logger.error(e.getMessage(), e);
	} catch (ReadDriverException e) {
	    logger.error(e.getMessage(), e);
	} catch (ValidateRowException e) {
	    logger.error(e.getMessage(), e);
	}
    }

    public void deleteRow(FLyrVect layer, int position) {
	try {
	    IEditableSource source = (IEditableSource) layer.getSource();
	    source.removeRow(position, "NAVTABLE DELETE", EditionEvent.ALPHANUMERIC);
	} catch (ExpansionFileReadException e) {
	    e.printStackTrace();
	} catch (ReadDriverException e) {
	    e.printStackTrace();
	}
    }

    private Value[] getNewAttributes(IEditableSource source, 
	    int rowPosition, 
	    int[] attIndexes, 
	    String[] attValues) {

	int type;
	try {
	    FieldDescription[] fieldDesc = source.getTableDefinition().getFieldsDesc();
	    Value[] attributes = source.getRow(rowPosition).getAttributes();
	    for (int i = 0; i < attIndexes.length; i++) {
		String att = attValues[i];
		int idx = attIndexes[i];
		if (att == null || att.length() == 0) {
		    attributes[idx] = ValueFactoryNT.createNullValue();
		} else {
		    type = fieldDesc[idx].getFieldType();
		    try {
			attributes[idx] = ValueFactoryNT.createValueByType(att, type);
		    } catch (ParseException e) {
		        logger.warn(e.getStackTrace(), e);
		    }
		}
	    }
	    return attributes;
	} catch (ReadDriverException e) {
	    logger.error(e.getMessage(), e);
	    return null;
	}
    }

    private Value getNewAttribute(IEditableSource source, int colPosition, String newValue) {
	if (newValue == null) {
	    newValue = "";
	}
	try {
	    ITableDefinition tableDef = source.getTableDefinition();
	    FieldDescription[] fieldDesc = tableDef.getFieldsDesc();
	    int type = fieldDesc[colPosition].getFieldType();
	    Value val;
	    if (newValue.length() == 0) {
		val = ValueFactoryNT.createNullValue();
	    } else {
		val = ValueFactoryNT.createValueByType(newValue, type);
	    }
	    return val;
	} catch (ReadDriverException e) {
	    logger.error(e.getStackTrace(), e);
	    return null;
	} catch (ParseException e) {
	    logger.warn(e.getStackTrace(), e);
	    return null;
	}
    }

    private IGeometry getTheGeom(IEditableSource source, int rowPosition) {
	try {
	    IRowEdited row = source.getRow(rowPosition);
	    return ((DefaultFeature) row.getLinkedRow()).getGeometry();
	} catch (Exception e) {
	    logger.error(e.getMessage(), e);
	    return null;
	}
    }

    private Table getModelTable(ProjectTable pt) {
	// TODO: see how drop this IWindow dependence, by getting the Table
	// from internal info (layer, MapControl) instead of iterating
	// through all windows
	com.iver.andami.ui.mdiManager.IWindow[] views = PluginServices
		.getMDIManager().getAllWindows();
	for (int i = 0; i < views.length; i++) {
	    if (views[i] instanceof Table) {
		Table table = (Table) views[i];
		ProjectTable model = table.getModel();
		if (model.equals(pt)) {
		    return table;
		}
	    }
	}
	return null;
    }

    private Table getTableFromLayer(FLayer layer) {
	//TODO: see how drop this IWindow dependence
	com.iver.andami.ui.mdiManager.IWindow[] views = null;
	try {
	    views = PluginServices.getMDIManager().getAllWindows();
	    for (int j = 0; j < views.length; j++) {
		    if (views[j] instanceof Table) {
			Table table = (Table) views[j];
			if (table.getModel().getAssociatedTable() != null
				&& table.getModel().getAssociatedTable().equals(layer)) {
			    return table;
			}
		    }
	    }
	} catch (NullPointerException e) {}
	
	return null;
    }

}
