package es.udc.cartolab.gvsig.navtable.dataacces;

import java.util.Map;

import org.gvsig.fmap.dal.exception.DataException;

public interface IController extends Cloneable {

    public long create(Map<String, String> newValues) throws Exception;
    
    public abstract void read(long position) throws DataException;

    public abstract void update(long position) throws DataException;

    public abstract void delete(long position) throws DataException;

    public abstract int getIndex(String fieldName);

    public abstract int[] getIndexesOfValuesChanged();

    public abstract String getValue(String fieldName);

    public abstract String getValueInLayer(String fieldName);

    public abstract Map<String, String> getValues();

    public abstract Map<String, String> getValuesOriginal();

    public abstract Map<String, String> getValuesChanged();

    /**
     * Make sure the value set is a formatted value, as the ones from layer. See
     * {@link #fill(SelectableDataSource, long)} For example: if value is a
     * double in layer, the string should be something like 1000,00 instead of
     * 1000.
     */
    public abstract void setValue(String fieldName, String value);

    public abstract int getType(String fieldName);

    public abstract long getRowCount() throws DataException;

}