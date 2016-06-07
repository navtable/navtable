package es.icarto.gvsig.navtable.gvsig2;

import org.gvsig.fmap.dal.feature.EditableFeature;
import org.gvsig.fmap.dal.feature.Feature;
import org.gvsig.fmap.dal.feature.FeatureAttributeDescriptor;
import org.gvsig.fmap.dal.feature.FeatureType;
import org.gvsig.fmap.geom.Geometry;

public class DefaultFeature implements IRowEdited{

	private int geomIdx = -1;
	private FeatureAttributeDescriptor[] attDesc = null;
	private Value[] values;
	private Geometry geometry;;

	public DefaultFeature(Geometry geometry, Value[] values) {
		this.values = values;
		this.geometry = geometry;
	}

	public DefaultFeature(Feature currentFeat) {
		
		FeatureType defaultFeatureType = currentFeat.getType();
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
			geometry = currentFeat.getGeometry(geomIdx);
		} else {
			attDesc = defaultFeatureType.getAttributeDescriptors();			
		}
		initValues(currentFeat);
	}

	private void initValues(Feature editable) {
		values = new Value[attDesc.length];
		for (int i =0; i<attDesc.length; i++) {
			Object v = editable.get(i);
			values[i] = ValueFactory.createValue(v);
		}
		
	}

	@Override
	public Object getID() {
		return "";
	}

	@Override
	public Value[] getAttributes() {
		return values;
	}

	@Override
	public Geometry getGeometry() {
		return geometry;
	}

}
