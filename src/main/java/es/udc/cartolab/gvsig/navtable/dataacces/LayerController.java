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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gvsig.fmap.dal.DataTypes;
import org.gvsig.fmap.dal.exception.DataException;
import org.gvsig.fmap.dal.feature.Feature;
import org.gvsig.fmap.dal.feature.FeatureAttributeDescriptor;
import org.gvsig.fmap.dal.feature.FeatureStore;
import org.gvsig.fmap.dal.feature.FeatureType;
import org.gvsig.fmap.geom.Geometry;
import org.gvsig.fmap.mapcontext.layers.vectorial.FLyrVect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.icarto.gvsig.commons.gvsig2.Value;
import es.icarto.gvsig.commons.gvsig2.ValueFactory;
import es.icarto.gvsig.commons.gvsig2.ValueWriter;
import es.icarto.gvsig.navtable.edition.LayerEdition;
import es.udc.cartolab.gvsig.navtable.format.ValueFormatNT;

public class LayerController implements IController {

	private static final Logger logger = LoggerFactory
			.getLogger(LayerController.class);

	private final FLyrVect layer;
	private final Map<String, Integer> indexes;
	private final Map<String, Integer> types;
	private final List<String> fieldNames;
	private final Map<String, String> values = new HashMap<String, String>();
	private Geometry geom;
	private final Map<String, String> valuesChanged = new HashMap<String, String>();

	public LayerController(FLyrVect layer) {
		this.layer = layer;
		try {
			FeatureStore store = layer.getFeatureStore();
			FeatureType featType = store.getDefaultFeatureType();
			int fieldCount = featType.size() - 1; // Geometry is ignored
			Map<String, Integer> idx = new HashMap<String, Integer>(fieldCount);
			Map<String, Integer> type = new HashMap<String, Integer>(fieldCount);
			List<String> fNames = new ArrayList<String>(fieldCount);
			for (int i = 0; i < fieldCount; i++) {
				FeatureAttributeDescriptor attDesc = featType
						.getAttributeDescriptor(i);
				int attType = attDesc.getDataType().getType();
				if (attType == DataTypes.GEOMETRY) {
					continue;
				}
				String name = attDesc.getName();
				idx.put(name, i);
				type.put(name, attType);
				fNames.add(name);
			}
			this.indexes = Collections.unmodifiableMap(idx);
			this.types = Collections.unmodifiableMap(type);
			this.fieldNames = Collections.unmodifiableList(fNames);
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
	public void create(Map<String, String> newValues) throws Exception {
		throw new RuntimeException("Not implemented jet");
	}

	@Override
	public void read(Feature feat) {
		values.clear();
		valuesChanged.clear();
		ValueWriter vWriter = new ValueFormatNT();
		for (String name : indexes.keySet()) {
			Object o = feat.get(name);
			Value value = ValueFactory.createValue(o);
			values.put(name, value.getStringValue(vWriter));
		}
		geom = feat.getDefaultGeometry();
	}

	@Override
	public void update(Feature feat) throws DataException {
		LayerEdition te = new LayerEdition();

		boolean wasEditing = layer.isEditing();
		try {
			if (!wasEditing) {
				te.startEditing(layer);
			}
			Feature f = te.modifyValues(layer, feat,
					this.getIndexesOfValuesChanged(), this.getValuesChanged()
					.values().toArray(new String[0]));
			read(f);
			if (!wasEditing) {
				te.stopEditing(layer, false);
			}
		} catch (DataException e) {
			logger.error(e.getMessage(), e);
			if (!wasEditing) {
				te.stopEditing(layer, true);
			}
			throw e;
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
	public Geometry getGeom() {
		return geom;
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
		String oldValue = values.get(fieldName);
		if ((oldValue == null) || (!oldValue.equals(value))) {
			valuesChanged.put(fieldName, value);
		}
	}

	@Override
	public int getType(String fieldName) {
		return types.get(fieldName);
	}

	@Override
	public long getRowCount() throws DataException {
		return layer.getFeatureStore().getFeatureCount();
	}

	@Override
	public IController clone() {
		return new LayerController(layer);
	}

	/**
	 * Returns an ordered list with the field names in the layer
	 */
	@Override
	public List<String> getFieldNames() {
		return fieldNames;
	}

	@Override
	public void delete(Feature feat) {
		FeatureStore store = layer.getFeatureStore();
		boolean wasEditing = store.isEditing();
		LayerEdition te = new LayerEdition();
		if (!wasEditing) {
			te.startEditing(layer);
		}
		try {
			store.delete(feat);
			if (!wasEditing) {
				te.stopEditing(layer, false);
			}
		} catch (DataException e) {
			logger.error(e.getMessage(), e);
			if (!wasEditing) {
				te.stopEditing(layer, true);
			}
		}

	}

	@Override
	public Feature newEmptyRecord() {
		FeatureStore store = layer.getFeatureStore();
		try {
			return store.createNewFeature();
		} catch (DataException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	@Override
	public boolean isEditing() {
		FeatureStore store = layer.getFeatureStore();
		return store.isEditing();
	}
}
