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

import static es.icarto.gvsig.commons.i18n.I18n._;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import javax.swing.JOptionPane;

import org.gvsig.andami.PluginServices;
import org.gvsig.andami.messages.NotificationManager;
import org.gvsig.andami.ui.mdiManager.IWindow;
import org.gvsig.app.ApplicationLocator;
import org.gvsig.app.project.documents.view.gui.DefaultViewPanel;
import org.gvsig.editing.CancelException;
import org.gvsig.editing.EditionLocator;
import org.gvsig.editing.IEditionManager;
import org.gvsig.editing.layers.VectorialLayerEdited;
import org.gvsig.fmap.dal.exception.DataException;
import org.gvsig.fmap.dal.exception.ReadException;
import org.gvsig.fmap.geom.Geometry;
import org.gvsig.fmap.mapcontext.layers.FLayer;
import org.gvsig.fmap.mapcontext.layers.vectorial.FLyrVect;
import org.gvsig.tools.exception.DriverException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.icarto.gvsig.commons.gvsig2.DefaultFeature;
import es.icarto.gvsig.commons.gvsig2.IEditableSource;
import es.icarto.gvsig.commons.gvsig2.IRowEdited;
import es.icarto.gvsig.commons.gvsig2.SelectableDataSource;
import es.icarto.gvsig.commons.gvsig2.Value;
import es.icarto.gvsig.commons.gvsig2.ValueFactory;
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

	private static final Logger logger = LoggerFactory
			.getLogger(ToggleEditing.class);

	/**
	 * @param layer
	 *            - The vectorial layer to be edited.
	 */
	public boolean startEditing(FLayer layer) throws DataException {
		if (!(layer instanceof FLyrVect)) {
			return true;
		}
		FLyrVect lv = (FLyrVect) layer;

		if (!lv.getFeatureStore().getTransforms().isEmpty()) {
			String message = _("_Cannot_start_edition_in_transformed_layer")
					+ ": '" + lv.getName() + "'";
			throw new RuntimeException(message);
		}
		if (!lv.isWritable()) {
			String message = _("this_layer_is_not_self_editable");
			throw new RuntimeException(message);
		}

		IEditionManager editionManager = EditionLocator.getEditionManager();
		// mapControl.setRefentEnabled(true);
		List snaplist = layer.getMapContext().getLayersToSnap();
		if (!snaplist.contains(lv)) {
			snaplist.add(lv);
		}
		try {
			editionManager.editLayer(lv, getViewFromLayer(layer));
		} catch (CancelException e) {
			// Do nothing
			return false;
		} catch (DataException e) {
			logger.info(e.getMessage(), e);
			ApplicationLocator.getManager().message(
					_("_Unable_to_start_edition_in_layer") + ": "
							+ lv.getName(), JOptionPane.ERROR_MESSAGE);
		}
		return true;
	}

	/**
	 * @param layer
	 *            - The layer wich edition will be stoped.
	 * @param cancel
	 *            - false if we want to save the layer, true if we don't.
	 * @throws StopWriterVisitorException
	 */
	public boolean stopEditing(FLayer layer, boolean cancel) {

		if (layer instanceof FLyrVect) {
			try {
				if (cancel) {
					cancelEdition(layer);
				} else {
					saveLayer((FLyrVect) layer);
				}
			} catch (DataException e) {
				logger.error(e.getMessage(), e);
				return false;
			} finally {
				layer.setActive(true);
			}
			return true;
		}
		return false;
	}

	private void cancelEdition(FLayer layer) {
		IEditionManager edMan = EditionLocator.getEditionManager();
		VectorialLayerEdited lyrEd = (VectorialLayerEdited) edMan
				.getLayerEdited(layer);
		try {
			lyrEd.clearSelection();
			try {
				edMan.stopEditLayer(null, (FLyrVect) layer,
						IEditionManager.CANCEL_EDITING);
			} catch (CancelException ex) {
				logger.error("Layer save canceled", ex);
			} catch (Exception ex) {
				logger.error("While stopping layer editing.", ex);
			}

		} catch (ReadException e1) {
			NotificationManager.showMessageError(e1.getMessage(), e1);
		} catch (DataException e) {
			NotificationManager.showMessageError(e.getMessage(), e);
		}
	}

	private boolean saveLayer(FLyrVect layer) throws DataException {
		IEditionManager edMan = EditionLocator.getEditionManager();
		VectorialLayerEdited lyrEd = (VectorialLayerEdited) edMan
				.getLayerEdited(layer);
		boolean isStop = false;
		try {
			lyrEd.clearSelection();
			if (!layer.isWritable()) {
				throw new RuntimeException("Layer is not writeble");
			}
			try {
				edMan.stopEditLayer(null, layer, IEditionManager.ACCEPT_EDITING);
				isStop = true;
			} catch (CancelException ex) {
				logger.error("Layer save canceled", ex);
				isStop = false;
			} catch (Exception ex) {
				logger.error("While stopping layer editing.", ex);
				isStop = false;
			}

		} catch (ReadException e1) {
			NotificationManager.showMessageError(e1.getMessage(), e1);
		} catch (DataException e) {
			NotificationManager.showMessageError(e.getMessage(), e);
		}
		return isStop;
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

		IEditableSource source = new SelectableDataSource(
				layer.getFeatureStore());
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

		IEditableSource source = new SelectableDataSource(
				layer.getFeatureStore());
		IRowEdited row = source.getRow(rowPosition);
		Value[] attributes = row.getAttributes();
		attributes[colPosition] = newValue;
		Geometry geometry = row.getGeometry();
		DefaultFeature newRow = new DefaultFeature(geometry, attributes);
		source.modifyRow(rowPosition, newRow);
	}

	public void modifyValue(IEditableSource source, int rowPosition,
			int colPosition, String newValue) throws Exception {

		IRowEdited row = source.getRow(rowPosition);
		Value[] attributes = row.getAttributes();
		attributes[colPosition] = getNewAttribute(source, colPosition, newValue);
		DefaultFeature newRow = new DefaultFeature(null, attributes);
		source.modifyRow(rowPosition, newRow);
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
	public void modifyValues(FLyrVect layer, long rowPosition,
			int[] attIndexes, String[] attValues) {

		try {
			IEditableSource source = new SelectableDataSource(
					layer.getFeatureStore());

			Geometry geometry = source.getGeometry(rowPosition);
			Value[] values = getNewAttributes(source, rowPosition, attIndexes,
					attValues);
			DefaultFeature newRow = new DefaultFeature(geometry, values);
			source.modifyRow(rowPosition, newRow);
		} catch (DataException e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void deleteRow(FLyrVect layer, int position) {
		try {
			IEditableSource source = new SelectableDataSource(
					layer.getFeatureStore());
			source.removeRow(position);
		} catch (DataException e) {
			logger.error(e.getMessage(), e);
		}
	}

	private Value[] getNewAttributes(IEditableSource source, long rowPosition,
			int[] attIndexes, String[] attValues) {

		int type;

		Value[] attributes = source.getRow(rowPosition).getAttributes();
		for (int i = 0; i < attIndexes.length; i++) {
			String att = attValues[i];
			int idx = attIndexes[i];
			if (att == null || att.length() == 0) {
				attributes[idx] = ValueFactory.createNullValue();
			} else {
				type = source.getFieldType(idx);
				try {
					attributes[idx] = ValueFactoryNT.createValueByType(att,
							type);
				} catch (ParseException e) {
					logger.warn(e.getMessage(), e);
				}
			}
		}
		return attributes;

	}

	private Value getNewAttribute(IEditableSource source, int colPosition,
			String newValue) {
		if (newValue == null) {
			newValue = "";
		}
		try {
			int type = source.getFieldType(colPosition);
			Value val;
			if (newValue.length() == 0) {
				val = ValueFactory.createNullValue();
			} else {
				val = ValueFactoryNT.createValueByType(newValue, type);
			}
			return val;
		} catch (ParseException e) {
			logger.warn(e.getMessage(), e);
			return null;
		}
	}

	private DefaultViewPanel getViewFromLayer(FLayer layer) {
		// TODO: see how drop this IWindow dependence
		IWindow[] views = PluginServices.getMDIManager().getAllWindows();
		for (int j = 0; j < views.length; j++) {
			if (views[j] instanceof DefaultViewPanel) {
				DefaultViewPanel view = (DefaultViewPanel) views[j];
				return view;
			}
		}

		return null;
	}
}
