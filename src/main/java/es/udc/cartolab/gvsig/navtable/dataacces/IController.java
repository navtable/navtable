package es.udc.cartolab.gvsig.navtable.dataacces;

import java.util.List;
import java.util.Map;

import org.gvsig.fmap.dal.exception.DataException;
import org.gvsig.fmap.dal.feature.Feature;
import org.gvsig.fmap.geom.Geometry;

public interface IController extends Cloneable {

	public long create(Map<String, String> newValues) throws Exception;

	public void read(Feature feat);

	public abstract int getIndex(String fieldName);

	public abstract int[] getIndexesOfValuesChanged();

	public abstract String getValue(String fieldName);

	public abstract String getValueInLayer(String fieldName);

	public abstract Map<String, String> getValues();

	public abstract Map<String, String> getValuesOriginal();

	public abstract Map<String, String> getValuesChanged();

	public abstract void setValue(String fieldName, String value);

	public abstract int getType(String fieldName);

	public abstract long getRowCount() throws DataException;

	public List<String> getFieldNames();

	public Geometry getGeom();

	public void update(Feature feat) throws DataException;

}