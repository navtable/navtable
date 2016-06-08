package es.icarto.gvsig.navtable.gvsig2;

import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gvsig.fmap.dal.DataStoreNotification;
import org.gvsig.fmap.dal.exception.DataException;
import org.gvsig.fmap.dal.feature.EditableFeature;
import org.gvsig.fmap.dal.feature.Feature;
import org.gvsig.fmap.dal.feature.FeatureAttributeDescriptor;
import org.gvsig.fmap.dal.feature.FeatureSelection;
import org.gvsig.fmap.dal.feature.FeatureSet;
import org.gvsig.fmap.dal.feature.FeatureStore;
import org.gvsig.fmap.dal.feature.FeatureType;
import org.gvsig.fmap.geom.Geometry;
import org.gvsig.fmap.mapcontext.layers.SelectionEvent;
import org.gvsig.fmap.mapcontext.layers.SelectionListener;
import org.gvsig.gui.beans.editabletextcomponent.IEditableText;
import org.gvsig.tools.dataTypes.DataTypes;
import org.gvsig.tools.dispose.DisposableIterator;
import org.gvsig.tools.dispose.DisposeUtils;
import org.gvsig.tools.observer.Observable;
import org.gvsig.tools.observer.Observer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SelectableDataSource implements FBitSet, IEditableSource {

	private static final Logger logger = LoggerFactory
		.getLogger(SelectableDataSource.class);	
	private final FeatureStore fs;
	private FeatureAttributeDescriptor[] attDesc;
	private int geomIdx = -1;
	
	/**
	 * pointers to the feature in the currentPos in the recordset
	 */
	private Feature currentFeat = null;
	private long currentPos = -1 ;


	public SelectableDataSource(FeatureStore fs) throws DataException {
		this.fs = fs;
		FeatureType defaultFeatureType = fs.getFeatureSet().getDefaultFeatureType();
		geomIdx = defaultFeatureType.getDefaultGeometryAttributeIndex();
		if (geomIdx != -1) {
			FeatureAttributeDescriptor[] foo = defaultFeatureType.getAttributeDescriptors();
			attDesc = new FeatureAttributeDescriptor[foo.length - 1];
			for (int i=0; i< foo.length; i++) {
				if (i == geomIdx) {
					continue;
				} else if (i > geomIdx) {
					attDesc[i-1] = foo[i];
				} else {
					attDesc[i] = foo[i];
				}
			}
		} else {
			attDesc = defaultFeatureType.getAttributeDescriptors();			
		}
	}
	
	public int getFieldCount() throws DataException {
		return attDesc.length;
	}
	
	public int getFieldIndexByName(String name) {
		
			for (FeatureAttributeDescriptor fad : attDesc) {
				if (fad.getName().equals(name)) {
					return fad.getIndex();
				}
			}
	
		return -1;
	}
	
	

	public Value getFieldValue(long position, int i) {
		if (currentPos != position) {
			setCurrentFeature(position);
		}
		if (i == geomIdx) {
			throw new RuntimeException("Geometry field can not ge got with this method");
		}
		Object o = currentFeat.get(i);
		return ValueFactory.createValue(o);
	}
	
	private void setCurrentFeature(long pos) {
		FeatureSet featSet = null;
		DisposableIterator fastIterator = null;
		try {
			featSet = fs.getFeatureSet();
			fastIterator = featSet.fastIterator(pos);
			Feature f = (Feature) fastIterator.next();
			currentFeat = f.getCopy();
			currentPos = pos;
		} catch (DataException e) {
			logger.error(e.getMessage(), e);
		} finally {
			DisposeUtils.dispose(fastIterator);
			DisposeUtils.dispose(featSet);
		}
	}

	@Override
	public int getFieldType(int i) {
		int gvsig2Type = attDesc[i].getType();
		return type2gvsig1(gvsig2Type);
	}

	private int type2gvsig1(int gvsig2Type) {
		switch (gvsig2Type) {
		case DataTypes.BOOLEAN:
			return Types.BOOLEAN;
		case DataTypes.BYTE:
			return Types.SMALLINT;
		case DataTypes.INT:
			return Types.INTEGER;
		case DataTypes.LONG:
			return Types.BIGINT;
		case DataTypes.CHAR:
			return Types.CHAR;
		case DataTypes.FLOAT:
			return Types.FLOAT;
		case DataTypes.DOUBLE:
			return Types.DOUBLE;
		case DataTypes.STRING:
			return Types.VARCHAR;
		case DataTypes.DATE:
			return Types.DATE;
		case DataTypes.TIME:
			return Types.TIME;
		case DataTypes.TIMESTAMP:
			return Types.TIMESTAMP;
		default:
			throw new RuntimeException("Not supported type");
		}
	}

	@Override
	public String getName() {
		return fs.getName();
	}

	public long getRowCount() throws DataException {
		return fs.getFeatureCount();
	}

	public String getFieldName(int i) {
		return attDesc[i].getName();
	}

	public FBitSet getSelection() {
		return this;
	}

	@Override
	public boolean get(int pos) {
		try {
			FeatureSelection featureSelection = fs.getFeatureSelection();
			if (currentPos != pos) {
				setCurrentFeature(pos);
			}
			return featureSelection.isSelected(currentFeat);
		} catch (DataException e) {
			logger.error(e.getMessage(), e);
		}
		return false;
	}

	@Override
	public void set(int pos) {
		try {
			FeatureSelection featureSelection = fs.getFeatureSelection();
			if (currentPos != pos) {
				setCurrentFeature(pos);
			}
			featureSelection.select(currentFeat);
		} catch (DataException e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public void clear(int pos) {
		try {
			FeatureSelection featureSelection = fs.getFeatureSelection();
			if (currentPos != pos) {
				setCurrentFeature(pos);
			}
			featureSelection.deselect(currentFeat);
		} catch (DataException e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void setSelection(FBitSet bitset) {
		// Nothing to do here
	}

	public void clearSelection() {
		try {
			fs.getFeatureSelection().deselectAll();
		} catch (DataException e) {
			logger.error(e.getMessage(), e);
		}
		
	}

	@Override
	public boolean isEmpty() {
		try {
			return fs.getFeatureSelection().isEmpty();
		} catch (DataException e) {
			logger.error(e.getMessage(), e);
		}
		return false;
	}

	@Override
	public int cardinality() {
		try {
			return (int) fs.getFeatureSelection().getSize();
		} catch (DataException e) {
			logger.error(e.getMessage(), e);
		}
		return 0;
	}

	@Override
	public long nextSetBit(int i) {
		FeatureSet set = null;
		DisposableIterator it = null;
		try {
			FeatureSelection selection = fs.getFeatureSelection();
			set = fs.getFeatureSet();
			it = set.fastIterator(i);
			int counter = i;
			while (it.hasNext()) {
				Feature f = (Feature) it.next();
				if (selection.isSelected(f)) {
					return counter;
				}
				counter++;
			}
		} catch (DataException e) {
			logger.error(e.getMessage(), e);
		} finally {
			DisposeUtils.dispose(it);
			DisposeUtils.dispose(set);
		}
		
		return -1;
	}

	public void reload() throws DataException {
		fs.refresh();
	}

	public String[] getFieldNames() {
		String[] names = new String[attDesc.length];
		for (int i=0; i<attDesc.length; i++) {
			names[i] = attDesc[i].getName();
		}
		return names;
	}

	@Override
	public long posOfFeature(Feature feat) {
		FeatureSet set = null;
		DisposableIterator it = null;
		try {
			set = fs.getFeatureSet();
			it = set.fastIterator();
			long counter = 0;
			while (it.hasNext()) {
				Feature f = (Feature) it.next();
				if (f.equals(feat)) {
					return counter;
				}
				counter++;
			}
		} catch (DataException e){
			e.printStackTrace();
		} finally {
			DisposeUtils.dispose(it);
			DisposeUtils.dispose(set);
			
		}
		return -1;
	}

	@Override
	public Geometry getGeometry(long pos) {
		if (currentPos != pos) {
			setCurrentFeature(pos);
		}
		return currentFeat.getGeometry(geomIdx);
	}

	@Override
	public boolean isWritable() {
		return fs.allowWrite();
	}

	@Override
	public DefaultFeature getRow(int pos) {
		if (currentPos != pos) {
			setCurrentFeature(pos);
		}
		
		return new DefaultFeature(currentFeat);
	}

	@Override
	public void modifyRow(int pos, DefaultFeature newRow) throws DataException {
		if (currentPos != pos) {
			setCurrentFeature(pos);
		}
		EditableFeature editable = currentFeat.getEditable();
		for (int i=0; i<attDesc.length; i++) {
			editable.set(i, newRow.getAttributes()[i].value);
		}
		if (geomIdx != -1) {
			editable.setGeometry(geomIdx, newRow.getGeometry());
		}
		fs.update(editable);
	}

	@Override
	public void removeRow(int pos) throws DataException {
		if (currentPos != pos) {
			setCurrentFeature(pos);
		}
		fs.delete(currentFeat);
	}
}
