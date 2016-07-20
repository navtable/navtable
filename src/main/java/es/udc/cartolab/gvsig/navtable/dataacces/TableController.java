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

import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.gvsig.app.project.documents.table.TableDocument;
import org.gvsig.fmap.dal.exception.DataException;
import org.gvsig.fmap.dal.feature.EditableFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.icarto.gvsig.commons.gvsig2.SelectableDataSource;
import es.icarto.gvsig.navtable.edition.TableEdition;
import es.udc.cartolab.gvsig.navtable.AbstractNavTable;
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

	private static final Logger logger = LoggerFactory
			.getLogger(TableController.class);

	public final static long NO_ROW = -1;

	private final TableDocument model;
	private final Map<String, Integer> indexes;
	private final Map<String, Integer> types;
	private Map<String, String> values = new HashMap<String, String>();
	private Map<String, String> valuesChanged = new HashMap<String, String>();

	public TableController(TableDocument model) {
		this.model = model;

		try {
			SelectableDataSource sds = new SelectableDataSource(
					model.getStore());
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
	public long create(Map<String, String> newValues) throws DataException,
			ParseException {
		if (!model.getStore().allowWrite()) {
			throw new RuntimeException("Write is not allowed in this table");
		}

		TableEdition te = new TableEdition();
		if (!model.getStore().isEditing()) {
			te.startEditing(model);
		}
		EditableFeature feature = createFeatureFromHashMap(newValues);
		model.getStore().insert(feature);
		te.stopEditing(model, false);
		long newPosition = model.getStore().getFeatureCount() - 1;
		read(newPosition);
		return newPosition;
	}

	private EditableFeature createFeatureFromHashMap(
			Map<String, String> newValues) throws DataException, ParseException {
		EditableFeature f = model.getStore().createNewFeature();
		for (String key : newValues.keySet()) {
			int index = getIndex(key);
			Object value = ValueFactoryNT.createValueByType(newValues.get(key),
					types.get(key)).getObjectValue();
			f.set(index, value);
		}
		return f;
	}

	@Override
	public void read(long position) throws DataException {
		SelectableDataSource sds = new SelectableDataSource(model.getStore());
		if (position != AbstractNavTable.EMPTY_REGISTER) {
			for (int i = 0; i < sds.getFieldCount(); i++) {
				String name = sds.getFieldName(i);
				values.put(
						name,
						sds.getFieldValue(position, i).getStringValue(
								new ValueFormatNT()));
			}
		}
	}

	@Override
	public void update(long position) throws DataException {
		TableEdition te = new TableEdition();
		boolean wasEditing = model.getStore().isEditing();
		if (!wasEditing) {
			te.startEditing(model);
		}

		te.modifyValues(model, (int) position, getIndexesOfValuesChanged(),
				getValuesChanged().values().toArray(new String[0]));
		if (!wasEditing) {
			te.stopEditing(model, false);
		}
		read((int) position);
	}

	@Override
	public void delete(long position) {
		TableEdition te = new TableEdition();
		te.startEditing(model);
		SelectableDataSource sds;
		try {
			sds = new SelectableDataSource(model.getStore());
			sds.removeRow(position);
		} catch (DataException e) {
			logger.error(e.getMessage(), e);
		}
		te.stopEditing(model, false);
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
		valuesChanged.put(fieldName, value);
	}

	@Override
	public int getType(String fieldName) {
		return types.get(fieldName);
	}

	@Override
	public long getRowCount() throws DataException {
		return new SelectableDataSource(model.getStore()).getRowCount();
	}

	@Override
	public TableController clone() {
		return new TableController(model);
	}
}
