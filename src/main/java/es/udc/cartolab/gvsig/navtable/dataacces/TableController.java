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

import org.gvsig.app.project.documents.table.TableDocument;
import org.gvsig.fmap.dal.exception.DataException;
import org.gvsig.fmap.dal.feature.EditableFeature;
import org.gvsig.fmap.dal.feature.Feature;
import org.gvsig.fmap.dal.feature.FeatureAttributeDescriptor;
import org.gvsig.fmap.dal.feature.FeatureStore;
import org.gvsig.fmap.dal.feature.FeatureType;
import org.gvsig.fmap.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.icarto.gvsig.commons.gvsig2.Value;
import es.icarto.gvsig.commons.gvsig2.ValueFactory;
import es.icarto.gvsig.commons.gvsig2.ValueWriter;
import es.icarto.gvsig.navtable.edition.TableEdition;
import es.udc.cartolab.gvsig.navtable.format.ValueFactoryNT;
import es.udc.cartolab.gvsig.navtable.format.ValueFormatNT;

/**
 * Class to manage CRUD (Create, Read, Update, Delete) operations on a Table.
 *
 * @author Andrés Maneiro <amaneiro@icarto.es>
 * @author @author Francisco Puga <fpuga@cartolab.es>
 *
 */
public class TableController implements IController {

	private static final Logger logger = LoggerFactory.getLogger(TableController.class);

	public final static long NO_ROW = -1;

	private final TableDocument model;
	private final Map<String, Integer> indexes;
	private final Map<String, Integer> types;
	private final List<String> fieldNames;
	private Map<String, String> values = new HashMap<String, String>();
	private Map<String, String> valuesChanged = new HashMap<String, String>();

	public TableController(TableDocument model) {
		this.model = model;
		try {
			// FeatureStore store = model.getFeatureStore();
			FeatureStore store = model.getStore();
			FeatureType featType = store.getDefaultFeatureType();
			int fieldCount = featType.size();
			Map<String, Integer> idx = new HashMap<String, Integer>(fieldCount);
			Map<String, Integer> type = new HashMap<String, Integer>(fieldCount);
			List<String> fNames = new ArrayList<String>(fieldCount);
			for (int i = 0; i < fieldCount; i++) {
				FeatureAttributeDescriptor attDesc = featType.getAttributeDescriptor(i);
				String name = attDesc.getName();
				idx.put(name, i);
				type.put(name, attDesc.getType());
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
	public void create(Map<String, String> newValues) throws Exception {
		TableEdition te = new TableEdition();
		if (!model.getStore().isEditing()) {
			te.startEditing(model);
		}
		EditableFeature feature = createFeatureFromHashMap(newValues);
		model.getStore().insert(feature);
		te.stopEditing(model, false);
	}

	private EditableFeature createFeatureFromHashMap(Map<String, String> newValues) throws Exception {
		EditableFeature f = model.getStore().createNewFeature();
		for (String key : newValues.keySet()) {
			int index = getIndex(key);
			Object value = ValueFactoryNT.createValueByType2(newValues.get(key), types.get(key)).getObjectValue();
			f.set(index, value);
		}
		return f;
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
	}

	@Override
	public void update(Feature feat) throws DataException {
		TableEdition te = new TableEdition();
		boolean wasEditing = model.getStore().isEditing();
		try {
			if (!wasEditing) {
				te.startEditing(model);
			}

			te.modifyValues(model, feat, getIndexesOfValuesChanged(),
					getValuesChanged().values().toArray(new String[0]));
			if (!wasEditing) {
				te.stopEditing(model, false);
			}
		} catch (DataException e) {
			logger.error(e.getMessage(), e);
			if (!wasEditing) {
				te.stopEditing(model, true);
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
	public String getValueInLayer(String fieldName) {
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
	public Map<String, String> getValuesOriginal() {
		return values;
	}

	@Override
	public Map<String, String> getValuesChanged() {
		return valuesChanged;
	}

	@Override
	public void setValue(String fieldName, String value) {
		String orgValue = values.get(fieldName);
		if ((orgValue == null) || (!orgValue.equals(value))) {
			valuesChanged.put(fieldName, value);
		}
	}

	@Override
	public int getType(String fieldName) {
		return types.get(fieldName);
	}

	@Override
	public long getRowCount() throws DataException {
		return model.getStore().getFeatureCount();
	}

	@Override
	public IController clone() {
		return new TableController(model);
	}

	@Override
	public List<String> getFieldNames() {
		return fieldNames;
	}

	@Override
	public Geometry getGeom() {
		throw new RuntimeException("Not geom for alfa tables");
	}

	@Override
	public void delete(Feature feat) {
		FeatureStore store = model.getStore();
		boolean wasEditing = store.isEditing();
		TableEdition te = new TableEdition();
		if (!wasEditing) {
			te.startEditing(model);
		}
		try {
			store.delete(feat);
			if (!wasEditing) {
				te.stopEditing(model, false);
			}
		} catch (DataException e) {
			logger.error(e.getMessage(), e);
			if (!wasEditing) {
				te.stopEditing(model, true);
			}
		}

	}

	@Override
	public Feature newEmptyRecord() {
		values.clear();
		valuesChanged.clear();
		FeatureStore store = model.getStore();
		try {
			return store.createNewFeature();
		} catch (DataException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	@Override
	public boolean isEditing() {
		FeatureStore store = model.getStore();
		return store.isEditing();
	}

}
