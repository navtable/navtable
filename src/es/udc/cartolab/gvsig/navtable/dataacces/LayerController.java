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

import java.util.HashMap;
import java.util.Set;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;

import es.udc.cartolab.gvsig.navtable.AbstractNavTable;
import es.udc.cartolab.gvsig.navtable.ToggleEditing;
import es.udc.cartolab.gvsig.navtable.format.ValueFormatNT;

/**
 * Class to manage CRUD (Create, Read, Update, Delete) operations on a Layer.
 * 
 * @author Andrés Maneiro <amaneiro@icarto.es>
 * @author Francisco Puga <fpuga@cartolab.es>
 * 
 */
public class LayerController implements IController {

    private FLyrVect layer;
    private HashMap<String, Integer> indexes;
    private HashMap<String, Integer> types;
    private HashMap<String, String> values;
    private HashMap<String, String> valuesChanged;

    public LayerController(FLyrVect layer) {
	this.layer = layer;
	this.indexes = new HashMap<String, Integer>();
	this.types = new HashMap<String, Integer>();
	this.values = new HashMap<String, String>();
	this.valuesChanged = new HashMap<String, String>();
    }
    
    @Override
    /**
     * Not implemented jet
     * the field with "geom" identifier will contain the WKT representation
     * of the geometry
     */
    public long create(HashMap<String, String> newValues) throws Exception {
	throw new NotImplementedException();
    }

    @Override
    public void read(long position) throws ReadDriverException {
	SelectableDataSource sds = layer.getSource().getRecordset();
	if(position != AbstractNavTable.EMPTY_REGISTER) {
	    clearAll();
	    for (int i = 0; i < sds.getFieldCount(); i++) {
		String name = sds.getFieldName(i);
		values.put(
			name,
			sds.getFieldValue(position, i).getStringValue(
				new ValueFormatNT()));
		indexes.put(name, i);
		types.put(name, sds.getFieldType(i));
	    }
	}
    }

    @Deprecated
    public void save(long position) throws ReadDriverException {
	update(position);
    }

    @Override
    public void update(long position) throws ReadDriverException {
	ToggleEditing te = new ToggleEditing();
	boolean wasEditing = layer.isEditing();
	if (!wasEditing) {
	    te.startEditing(layer);
	}
	te.modifyValues(layer, (int) position,
		this.getIndexesOfValuesChanged(),
		this.getValuesChanged().values().toArray(new String[0]));
	if (!wasEditing) {
	    te.stopEditing(layer, false);
	}
	this.read((int) position);
    }

    @Override
    public void delete(long position) throws ReadDriverException {
	    if (position > AbstractNavTable.EMPTY_REGISTER) {
		ToggleEditing te = new ToggleEditing();
		boolean wasEditing = layer.isEditing();
		if (!wasEditing) {
		    te.startEditing(layer);
		}
		te.deleteRow(layer, (int) position);
		if (!wasEditing) {
		    te.stopEditing(layer, false);
		}
	    }
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
    public HashMap<String, String> getValues() {
	HashMap<String, String> val = values;
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
    public long getRowCount() throws ReadDriverException {
	return layer.getRecordset().getRowCount();
    }
}
