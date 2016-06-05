package es.icarto.gvsig.navtable.gvsig2;

import org.gvsig.fmap.dal.exception.DataException;
import org.gvsig.fmap.dal.feature.Feature;
import org.gvsig.fmap.dal.feature.FeatureAttributeDescriptor;
import org.gvsig.fmap.dal.feature.FeatureSet;
import org.gvsig.fmap.dal.feature.FeatureStore;
import org.gvsig.fmap.dal.feature.FeatureType;
import org.gvsig.tools.dispose.DisposableIterator;
import org.gvsig.tools.dispose.DisposeUtils;

public class SelectableDataSource {
	
	private final FeatureStore fs;
	private FeatureAttributeDescriptor[] attDesc;
	
	/**
	 * pointers to the feature in the currentPos in the recordset
	 */
	private Feature currentFeat;
	private long currentPos;


	public SelectableDataSource(FeatureStore fs) {
		this.fs = fs;
		FeatureType defaultFeatureType;
		try {
			defaultFeatureType = fs.getFeatureSet().getDefaultFeatureType();
			attDesc = defaultFeatureType.getAttributeDescriptors();
		} catch (DataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public int getFieldCount() throws DataException {
		return attDesc.length;
	}

	public Value getFieldValue(long position, int i) {
		if (currentPos != position) {
			setCurrentFeature(position);
		}
		return (Value) currentFeat.get(i);
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			DisposeUtils.dispose(fastIterator);
			DisposeUtils.dispose(featSet);
		}
	}

	public int getFieldType(int i) {
		return attDesc[i].getType();
	}

}
