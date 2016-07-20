/*
 * Copyright (c) 2011. iCarto
 *
 * This file is part of extNavTableForms
 *
 * extNavTableForms is free software: you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or any later version.
 *
 * extNavTableForms is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with extNavTableForms.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package es.udc.cartolab.gvsig.navtable.dataacces;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.gvsig.fmap.dal.exception.DataException;
import org.gvsig.fmap.mapcontext.layers.vectorial.FLyrVect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.icarto.gvsig.commons.gvsig2.SelectableDataSource;
import es.icarto.gvsig.commons.gvsig2.ValueWriter;
import es.udc.cartolab.gvsig.navtable.AbstractNavTable;
import es.udc.cartolab.gvsig.navtable.ToggleEditing;
import es.udc.cartolab.gvsig.navtable.format.ValueFormatNT;

/**
 * Class to manage CRUD (Create, Read, Update, Delete) operations on a Layer.
 *
 * @author Andr�s Maneiro <amaneiro@icarto.es>
 * @author Francisco Puga <fpuga@cartolab.es>
 *
 */
public class LayerController implements IController {

	private static final Logger logger = LoggerFactory
			.getLogger(LayerController.class);

	private final FLyrVect layer;
	private final Map<String, Integer> indexes;
	private final Map<String, Integer> types;
	private final Map<String, String> values = new HashMap<String, String>();
	private final Map<String, String> valuesChanged = new HashMap<String, String>();

	public LayerController(FLyrVect layer) {
		this.layer = layer;
		try {
			SelectableDataSource sds = new SelectableDataSource(
					layer.getFeatureStore());
			int fieldCount = sds.getFieldCount();
			Map<String, Integer> idx = new HashMap<String, Integer>(fieldCount);
			Map<String, Integer> type = new HashMap<String, Integer>(fieldCount);
			for (int i = 0; i < fieldCount; i++) {
				String name = sds.getFieldName(i);
				idx.put(name, i);
				type.put(name, sds.getFieldType(i));
			}
			this.indexes = Collections.unmodifiableMap(idx);
			this.types = Collections.unmodifiableMap(type);
		} catch (DataException e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	@Override
	/**
	 * Not implemented jet
	 * the field with "geom" identifier will contain the WKT representation
	 * of the geometry
	 */
	public long create(Map<String, String> newValues) throws Exception {
		throw new RuntimeException("Not implemented jet");
	}

	@Override
	public void read(long position) throws DataException {
		SelectableDataSource sds = new SelectableDataSource(
				layer.getFeatureStore());
		if (position != AbstractNavTable.EMPTY_REGISTER) {
			ValueWriter vWriter = new ValueFormatNT();
			for (int i = 0; i < sds.getFieldCount(); i++) {
				String name = sds.getFieldName(i);
				String stringValue = sds.getFieldValue(position, i)
						.getStringValue(vWriter);
				values.put(name, stringValue);
			}
		}
	}

	@Override
	public void update(long position) throws DataException {
		ToggleEditing te = new ToggleEditing();

		boolean wasEditing = layer.isEditing();
		try {
			if (!wasEditing) {
				te.startEditing(layer);
			}
			te.modifyValues(layer, (int) position,
					this.getIndexesOfValuesChanged(), this.getValuesChanged()
					.values().toArray(new String[0]));
			if (!wasEditing) {
				te.stopEditing(layer, false);
			}
			this.read((int) position);
		} catch (DataException e) {
			logger.error(e.getMessage(), e);
			if (!wasEditing) {
				te.stopEditing(layer, true);
			}
			throw e;
		}
	}

	@Override
	public void delete(long position) {
		if (position > AbstractNavTable.EMPTY_REGISTER) {
			ToggleEditing te = new ToggleEditing();
			boolean wasEditing = layer.isEditing();
			try {
				if (!wasEditing) {
					te.startEditing(layer);
				}
				te.deleteRow(layer, (int) position);
				if (!wasEditing) {
					te.stopEditing(layer, false);
				}
			} catch (DataException e) {
				if (!wasEditing) {
					te.stopEditing(layer, true);
				}
			}
		}
	}

	@Override
	public int getIndex(String fieldName) {
		return indexes.get(fieldName);
	}

	@Override
	public int[] getIndexesOfValuesChanged() {
		int[] idxs = new int[valuesChanged.size()];
		Set<String> names = valuesChanged.keySet();
		int i = 0;
		for (String name : names) {
			idxs[i] = indexes.get(name);
			i++;
		}
		return idxs;
	}

	@Override
	public String getValue(String fieldName) {
		if (valuesChanged.containsKey(fieldName)) {
			return valuesChanged.get(fieldName);
		}
		return values.get(fieldName);
	}

	@Override
	public Map<String, String> getValues() {
		Map<String, String> val = values;
		for (String k : valuesChanged.keySet()) {
			val.put(k, valuesChanged.get(k));
		}
		return val;
	}

	@Override
	public String getValueInLayer(String fieldName) {
		return values.get(fieldName);
	}

	@Override
	public Map<String, String> getValuesOriginal() {
		return values;
	}

	@Override
	public Map<String, String> getValuesChanged() {
		return valuesChanged;
	}

	@Override
	public void setValue(String fieldName, String value) {
		valuesChanged.put(fieldName, value);
	}

	@Override
	public int getType(String fieldName) {
		return types.get(fieldName);
	}

	@Override
	public long getRowCount() throws DataException {
		return new SelectableDataSource(layer.getFeatureStore()).getRowCount();
	}

	@Override
	public LayerController clone() {
		return new LayerController(layer);
	}
}
