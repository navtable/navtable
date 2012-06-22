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

import java.io.IOException;
import java.text.ParseException;

import org.apache.log4j.Logger;

import com.hardcode.gdbms.driver.exceptions.InitializeWriterException;
import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.engine.data.driver.DriverException;
import com.hardcode.gdbms.engine.values.Value;
import com.iver.andami.PluginServices;
import com.iver.andami.messages.NotificationManager;
import com.iver.cit.gvsig.CADExtension;
import com.iver.cit.gvsig.EditionManager;
import com.iver.cit.gvsig.EditionUtilities;
import com.iver.cit.gvsig.ProjectExtension;
import com.iver.cit.gvsig.exceptions.expansionfile.ExpansionFileReadException;
import com.iver.cit.gvsig.exceptions.expansionfile.ExpansionFileWriteException;
import com.iver.cit.gvsig.exceptions.layers.CancelEditingLayerException;
import com.iver.cit.gvsig.exceptions.layers.LegendLayerException;
import com.iver.cit.gvsig.exceptions.layers.StartEditionLayerException;
import com.iver.cit.gvsig.exceptions.table.CancelEditingTableException;
import com.iver.cit.gvsig.exceptions.validate.ValidateRowException;
import com.iver.cit.gvsig.exceptions.visitors.StartWriterVisitorException;
import com.iver.cit.gvsig.exceptions.visitors.StopWriterVisitorException;
import com.iver.cit.gvsig.fmap.MapControl;
import com.iver.cit.gvsig.fmap.core.DefaultFeature;
import com.iver.cit.gvsig.fmap.core.DefaultRow;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.IRow;
import com.iver.cit.gvsig.fmap.drivers.FieldDescription;
import com.iver.cit.gvsig.fmap.drivers.ILayerDefinition;
import com.iver.cit.gvsig.fmap.drivers.ITableDefinition;
import com.iver.cit.gvsig.fmap.drivers.shp.IndexedShpDriver;
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
import com.iver.cit.gvsig.fmap.layers.FLayers;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.rendering.ILegend;
import com.iver.cit.gvsig.fmap.rendering.IVectorLegend;
import com.iver.cit.gvsig.layers.VectorialLayerEdited;
import com.iver.cit.gvsig.project.documents.table.ProjectTable;
import com.iver.cit.gvsig.project.documents.table.gui.Table;
import com.iver.cit.gvsig.project.documents.view.IProjectView;
import com.iver.cit.gvsig.project.documents.view.gui.BaseView;

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

    public boolean toggle(FLayer layer) {
	return true;
    }

    /**
     * @param layer
     *            The vectorial layer to be edited.
     */
    public void startEditing(FLayer layer) {

	CADExtension.initFocus();
	com.iver.andami.ui.mdiManager.IWindow f = PluginServices
		.getMDIManager().getActiveWindow();

	if (f instanceof BaseView) {
	    BaseView vista = (BaseView) f;
	    MapControl mapControl = vista.getMapControl();

	    IProjectView model = vista.getModel();
	    FLayers layers = model.getMapContext().getLayers();
	    layers.setAllActives(false);

	    if (layer instanceof FLyrVect) {
		layer.setActive(true);
		EditionManager editionManager = CADExtension
			.getEditionManager();
		editionManager.setMapControl(mapControl);

		FLyrVect lv = (FLyrVect) layer;

		lv.addLayerListener(editionManager);
		ILegend legendOriginal = lv.getLegend();

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
		}

		if (!(lv.getSource().getDriver() instanceof IndexedShpDriver)) {
		    VectorialLayerEdited vle = (VectorialLayerEdited) editionManager
			    .getLayerEdited(lv);
		    vle.setLegend(legendOriginal);
		}
		vea.getCommandRecord().addCommandListener(mapControl);
		// If there's a table linked to this layer, its model is changed
		// to VectorialEditableAdapter.
		ProjectExtension pe = (ProjectExtension) PluginServices
			.getExtension(ProjectExtension.class);
		ProjectTable pt = pe.getProject().getTable(lv);
		if (pt != null) {
		    pt.setModel(vea);
		    changeModelTable(pt, vea);
		}
		vista.repaintMap();
	    }
	}
    }

    public void startEditing(IEditableSource source) {
	try {
	    source.startEdition(EditionEvent.ALPHANUMERIC);
	} catch (StartWriterVisitorException e) {
	    logger.error(e.getMessage(), e);
	}
    }

    /**
     * Checks the layers from the TOC and get the layer to be edited with its
     * name.
     * 
     * @param layerName
     *            The name of the layer to be edited.
     */
    @Deprecated
    public void startEditing(String layerName) {
	FLayer layer = null;
	com.iver.andami.ui.mdiManager.IWindow f = PluginServices
		.getMDIManager().getActiveWindow();
	if (f instanceof BaseView) {
	    BaseView vista = (BaseView) f;
	    IProjectView model = vista.getModel();
	    FLayers layers = model.getMapContext().getLayers();
	    layers.setAllActives(false);
	    layer = layers.getLayer(layerName);
	}
	startEditing(layer);
    }

    /**
     * @param layer
     *            The layer wich edition will be stoped.
     * @param cancel
     *            false if we want to save the layer, true if we don't.
     */
    public void stopEditing(FLayer layer, boolean cancel) {

	EditionManager edMan = CADExtension.getEditionManager();
	com.iver.andami.ui.mdiManager.IWindow f = PluginServices
		.getMDIManager().getActiveWindow();

	try {
	    if (f instanceof BaseView) {
		BaseView vista = (BaseView) f;
		MapControl mapControl = vista.getMapControl();

		IProjectView model = vista.getModel();
		FLayers layers = model.getMapContext().getLayers();
		layers.setAllActives(false);
		if (cancel) {
		    cancelEdition((FLyrVect) layer);
		} else {
		    saveLayer((FLyrVect) layer);
		}

		VectorialLayerEdited lyrEd = (VectorialLayerEdited) edMan
			.getActiveLayerEdited();
		try {
		    ((FLyrVect) layer).getRecordset().removeSelectionListener(
			    lyrEd);
		} catch (ReadDriverException e) {
		    NotificationManager
		    .addError("Remove Selection Listener", e);
		}

		VectorialEditableAdapter vea = (VectorialEditableAdapter) ((FLyrVect) layer)
			.getSource();
		vea.getCommandRecord().removeCommandListener(mapControl);
		if (!(((FLyrVect) layer).getSource().getDriver() instanceof IndexedShpDriver)) {
		    VectorialLayerEdited vle = (VectorialLayerEdited) CADExtension
			    .getEditionManager().getLayerEdited(layer);
		    ((FLyrVect) layer).setLegend((IVectorLegend) vle
			    .getLegend());
		}
		layer.setEditing(false);
		layer.setActive(true);
	    }
	} catch (DriverException e) {
	    logger.error(e.getMessage(), e);
	} catch (IOException e) {
	    logger.error(e.getMessage(), e);
	} catch (LegendLayerException e) {
	    logger.error(e.getMessage(), e);
	} catch (StartEditionLayerException e) {
	    logger.error(e.getMessage(), e);
	} catch (EditionExceptionOld e) {
	    logger.error(e.getMessage(), e);
	}
    }

    /**
     * Used when editing a layer that doesn't have
     * an open View that has to be updated.
     *
     * @param layer
     *            The layer wich edition will be stoped.
     * @param cancel
     *            false if we want to save the layer, true if we don't.
     */
    public void stopEditingWoUpdate(FLayer layer, boolean cancel) {

	try {

	    if (cancel) {
	        cancelEdition((FLyrVect) layer);
	    } else {
	        saveLayer((FLyrVect) layer);
	    }

	    layer.setEditing(false);
	    layer.setActive(true);
	} catch (DriverException e) {
	    logger.error(e.getMessage(), e);
	} catch (IOException e) {
	    logger.error(e.getMessage(), e);
	} catch (StartEditionLayerException e) {
	    logger.error(e.getMessage(), e);
	} catch (EditionExceptionOld e) {
	    logger.error(e.getMessage(), e);
	}
    }

    /**
     * @param layer
     *            The name of the layer wich edition will be stoped.
     * @param cancel
     *            false if we want to save the layer, true if we don't.
     */
    @Deprecated
    public void stopEditing(String layerName, boolean cancel) {

	FLayer layer = null;
	com.iver.andami.ui.mdiManager.IWindow f = PluginServices
		.getMDIManager().getActiveWindow();

	if (f instanceof BaseView) {
	    BaseView vista = (BaseView) f;
	    IProjectView model = vista.getModel();
	    FLayers layers = model.getMapContext().getLayers();
	    layers.setAllActives(false);
	    layer = layers.getLayer(layerName);
	}

	stopEditing(layer, cancel);
    }

    public void stopEditing(IEditableSource source) {
	try {
	    IWriteable w = (IWriteable) source;
	    IWriter writer = w.getWriter();
	    if (writer == null) {
		NotificationManager.addError(
			"No existe driver de escritura para la tabla"
				+ source.getRecordset().getName(),
				new EditionExceptionOld());
	    } else {
		ITableDefinition tableDef = source.getTableDefinition();
		writer.initialize(tableDef);
		source.stopEdition(writer, EditionEvent.ALPHANUMERIC);
	    }
	} catch (ReadDriverException e) {
	    logger.error(e.getMessage(), e);
	} catch (InitializeWriterException e) {
	    logger.error(e.getMessage(), e);
	} catch (StopWriterVisitorException e) {
	    logger.error(e.getMessage(), e);
	}
    }

    private void changeModelTable(ProjectTable pt, VectorialEditableAdapter vea) {
	com.iver.andami.ui.mdiManager.IWindow[] views = PluginServices
		.getMDIManager().getAllWindows();
	for (int i = 0; i < views.length; i++) {
	    if (views[i] instanceof Table) {
		Table table = (Table) views[i];
		ProjectTable model = table.getModel();
		if (model.equals(pt)) {
		    table.setModel(pt);
		    vea.getCommandRecord().addCommandListener(table);
		}
	    }
	}
    }

    private void cancelEdition(FLyrVect layer) throws IOException {
	layer.setProperty("stoppingEditing", new Boolean(true));
	com.iver.andami.ui.mdiManager.IWindow[] views = PluginServices
		.getMDIManager().getAllWindows();
	VectorialEditableAdapter vea = (VectorialEditableAdapter) layer
		.getSource();
	try {
	    vea.cancelEdition(EditionEvent.GRAPHIC);
	} catch (CancelEditingLayerException e) {
	    logger.error(e.getMessage(), e);
	}
	for (int j = 0; j < views.length; j++) {
	    if (views[j] instanceof Table) {
		Table table = (Table) views[j];
		if (table.getModel().getAssociatedTable() != null
			&& table.getModel().getAssociatedTable().equals(layer)) {
		    try {
			table.cancelEditing();
		    } catch (CancelEditingTableException e) {
			logger.error(e.getMessage(), e);
		    }
		}
	    }
	}
	layer.setProperty("stoppingEditing", new Boolean(false));
    }

    private void saveLayer(FLyrVect layer) throws DriverException,
    EditionExceptionOld {
	layer.setProperty("stoppingEditing", new Boolean(true));
	VectorialEditableAdapter vea = (VectorialEditableAdapter) layer
		.getSource();

	ISpatialWriter writer = (ISpatialWriter) vea.getWriter();
	com.iver.andami.ui.mdiManager.IWindow[] views = PluginServices
		.getMDIManager().getAllWindows();
	for (int j = 0; j < views.length; j++) {
	    if (views[j] instanceof Table) {
		Table table = (Table) views[j];
		if (table.getModel().getAssociatedTable() != null
			&& table.getModel().getAssociatedTable().equals(layer)) {
		    table.stopEditingCell();
		}
	    }
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
	    // System.err.println("Escribiendo la capa " + lyrDef.getName() +
	    // " con los campos " + aux);
	    lyrDef.setShapeType(layer.getShapeType());
	    writer.initialize(lyrDef);
	    vea.stopEdition(writer, EditionEvent.GRAPHIC);
	    layer.setProperty("stoppingEditing", new Boolean(false));
	} catch (ReadDriverException e) {
	    logger.error(e.getMessage(), e);
	} catch (InitializeWriterException e) {
	    logger.error(e.getMessage(), e);
	} catch (StopWriterVisitorException e) {
	    logger.error(e.getMessage(), e);
	}
    }

    /**
     * Modidify a single value of a register. It creates the new value from its
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

    public void deleteRow(FLyrVect layer, int rowPosition) {
	try {
	    IEditableSource source = (IEditableSource) layer.getSource();
	    source.removeRow(rowPosition, "NAVTABLE DELETE", EditionEvent.ALPHANUMERIC);
	} catch (ExpansionFileReadException e) {
		e.printStackTrace();
	} catch (ReadDriverException e) {
		e.printStackTrace();
	}
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

    private Value[] getNewAttributes(IEditableSource source, 
	    int rowPosition, 
	    int[] attIndexes, 
	    String[] attValues) {

	Value val;
	int type;
	try {
	    FieldDescription[] fieldDesc = source.getTableDefinition().getFieldsDesc();
	    Value[] attributes = source.getRow(rowPosition).getAttributes();
	    for (int i = 0; i < attIndexes.length; i++) {
		if (attValues[i].length() == 0 || attValues[i] == null) {
		    val = ValueFactoryNT.createNullValue();
		} else {
		    type = fieldDesc[attIndexes[i]].getFieldType();
		    val = ValueFactoryNT.createValueByType(
			    attValues[i], type);
		}
		attributes[attIndexes[i]] = val;
	    }
	    return attributes;
	} catch (ParseException e) {
	    logger.error(e.getMessage(), e);
	    return null;
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
	    e.printStackTrace();
	    return null;
	} catch (ParseException e) {
	    e.printStackTrace();
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

    /**
     * Used when editing a layer that doesn't have
     * an open View that has to be updated.
     *
     * @param layer
     *            The vectorial layer to be edited.
     */
    public void startEditingWoUpdate(FLyrVect layer) {

	    if (layer instanceof FLyrVect) {
	        layer.setActive(true);

	        FLyrVect lv = (FLyrVect) layer;

	        ILegend legendOriginal = lv.getLegend();

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
	        }

	        // If there's a table linked to this layer, its model is changed
	        // to VectorialEditableAdapter.
	        ProjectExtension pe = (ProjectExtension) PluginServices
	            .getExtension(ProjectExtension.class);
	        ProjectTable pt = pe.getProject().getTable(lv);
	        if (pt != null) {
	            pt.setModel(vea);
	            changeModelTable(pt, vea);
	        }
	    }
	}

}
