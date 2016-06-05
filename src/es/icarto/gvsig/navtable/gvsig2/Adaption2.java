package es.icarto.gvsig.navtable.gvsig2;

import org.gvsig.fmap.dal.exception.DataException;
import org.gvsig.fmap.dal.feature.Feature;
import org.gvsig.fmap.dal.feature.FeatureAttributeDescriptor;
import org.gvsig.fmap.dal.feature.FeatureReference;
import org.gvsig.fmap.dal.feature.FeatureSet;
import org.gvsig.fmap.dal.feature.FeatureStore;
import org.gvsig.fmap.dal.feature.FeatureType;
import org.gvsig.tools.dispose.DisposableIterator;
import org.gvsig.tools.dispose.DisposeUtils;
import org.gvsig.tools.dynobject.DynObject;

public class Adaption2 {
	
	public static FeatureReference getFeature(FeatureStore fs , long feature) {
		FeatureReference ref = null;
		FeatureSet featSet = null;
		DisposableIterator fastIterator = null;
		try {
			featSet = fs.getFeatureSet();
			fastIterator = featSet.fastIterator(feature);
			Feature feat = (Feature) fastIterator.next();
			ref = feat.getReference();
		} catch (DataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			DisposeUtils.dispose(fastIterator);
			DisposeUtils.dispose(featSet);
		}
		return ref;
	}
	
	public static int getFieldIndexByName(FeatureStore fs, String name) {
		try {
			FeatureAttributeDescriptor[] attributeDescriptors = fs.getDefaultFeatureType().getAttributeDescriptors();
			for (FeatureAttributeDescriptor fad : attributeDescriptors) {
				if (fad.getName().equals(name)) {
					return fad.getIndex();
				}
			}
		} catch (DataException e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	public static int getFieldCount(FeatureStore fs) {
		FeatureType defaultFeatureType;
		try {
			defaultFeatureType = fs.getFeatureSet().getDefaultFeatureType();
			return defaultFeatureType.getAttributeDescriptors().length;
		} catch (DataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
	
	 

}
