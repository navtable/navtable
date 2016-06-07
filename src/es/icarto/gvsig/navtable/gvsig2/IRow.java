package es.icarto.gvsig.navtable.gvsig2;

import org.gvsig.fmap.geom.Geometry;

public interface IRow {
	public Object getID();
	public Value[] getAttributes();
	public Geometry getGeometry();

}
