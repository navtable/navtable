package es.icarto.gvsig.navtable.gvsig2;

import org.gvsig.fmap.dal.feature.Feature;

public interface IEditableSource {
	
	public String getName();
	public long posOfFeature(Feature feat);

}
