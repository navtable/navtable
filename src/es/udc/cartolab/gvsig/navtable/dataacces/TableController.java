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
import java.util.HashMap;
import java.util.Set;

import org.gvsig.app.project.documents.table.TableDocument;
import org.gvsig.fmap.dal.exception.DataException;

import es.icarto.gvsig.navtable.gvsig2.SelectableDataSource;
import es.icarto.gvsig.navtable.gvsig2.Value;
import es.udc.cartolab.gvsig.navtable.AbstractNavTable;
import es.udc.cartolab.gvsig.navtable.ToggleEditing;
import es.udc.cartolab.gvsig.navtable.format.ValueFactoryNT;
import es.udc.cartolab.gvsig.navtable.format.ValueFormatNT;

/**
 * Class to manage CRUD (Create, Read, Update, Delete) operations on a Table.
 * 
 * @author Andr�s Maneiro <amaneiro@icarto.es>
 * @author @author Francisco Puga <fpuga@cartolab.es>
 * 
 */
public class TableController implements IController {

    public static int NO_ROW = -1;

    private TableDocument model;
    private HashMap<String, Integer> indexes;
    private HashMap<String, Integer> types;
    private HashMap<String, String> values;
    private HashMap<String, String> valuesChanged;

    public TableController(TableDocument model) {
	this.model = model;
	this.indexes = new HashMap<String, Integer>();
	this.types = new HashMap<String, Integer>();
	this.values = new HashMap<String, String>();
	this.valuesChanged = new HashMap<String, String>();
    }

    public void initMetadata() {
	try {
	    SelectableDataSource sds = new SelectableDataSource(model.getStore());
	    for (int i = 0; i < sds.getFieldCount(); i++) {
		String name = sds.getFieldName(i);
		indexes.put(name, i);
		types.put(name, sds.getFieldType(i));
	    }
	} catch (DataException e) {
	    e.printStackTrace();
	    clearAll();
	}
    }

    public long create(HashMap<String, String> newValues) throws Exception {

	initMetadata();
	Value[] vals = createValuesFromHashMap(newValues);

	ToggleEditing te = new ToggleEditing();
	if (!model.isEditing()) {
	    te.startEditing(model);
	}
	long newPosition = NO_ROW;
	if (model instanceof IWriteable) {
	    IRow row = new DefaultRow(vals);
	    newPosition = model.doAddRow(row, EditionEvent.ALPHANUMERIC);
	}
	te.stopEditing(model);
	read(newPosition);
	return newPosition;
    }

    private Value[] createValuesFromHashMap(HashMap<String, String> newValues) {
	Value[] vals = new Value[indexes.size()];
	for (int i = 0; i < indexes.size(); i++) {
	    vals[i] = ValueFactoryNT.createNullValue();
	}
	for (String key : newValues.keySet()) {
	    try {
		vals[getIndex(key)] = ValueFactoryNT.createValueByType(
			newValues.get(key), types.get(key));
	    } catch (ParseException e) {
		vals[getIndex(key)] = ValueFactoryNT.createNullValue();
	    }
	}
	return vals;
    }

    @Override
    public void read(long position) throws DataException {
	SelectableDataSource sds = new SelectableDataSource(model.getStore());
	clearAll();
	if (position != AbstractNavTable.EMPTY_REGISTER) {
	    for (int i = 0; i < sds.getFieldCount(); i++) {
		String name = sds.getFieldName(i);
		indexes.put(name, i);
		types.put(name, sds.getFieldType(i));
		values.put(
			name,
			sds.getFieldValue(position, i).getStringValue(
				new ValueFormatNT()));
	    }
	}
    }

    @Override
    public void update(long position) {
	ToggleEditing te = new ToggleEditing();
	boolean wasEditing = model.isEditing();
	if (!wasEditing) {
	    te.startEditing(model);
	}
	te.modifyValues(model, (int) position, getIndexesOfValuesChanged(),
		getValuesChanged().values().toArray(new String[0]));
	if (!wasEditing) {
	    te.stopEditing(model);
	}
	read((int) position);
    }

    @Override
    public void delete(long position) {

	model.startEdition(EditionEvent.ALPHANUMERIC);

	IWriteable w = (IWriteable) model;
	IWriter writer = w.getWriter();

	ITableDefinition tableDef = model.getTableDefinition();
	writer.initialize(tableDef);

	model.doRemoveRow((int) position, EditionEvent.ALPHANUMERIC);
	model.stopEdition(writer, EditionEvent.ALPHANUMERIC);
	model.getStore().refresh();
	clearAll();
    }

    @Override
    public void clearAll() {
	indexes.clear();
	types.clear();
	values.clear();
	valuesChanged.clear();
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
    public HashMap<String, String> getValues() {
	HashMap<String, String> val = values;
	for (String k : valuesChanged.keySet()) {
	    val.put(k, valuesChanged.get(k));
	}
	return val;
    }

    @Override
    public HashMap<String, String> getValuesOriginal() {
	return values;
    }

    @Override
    public HashMap<String, String> getValuesChanged() {
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
