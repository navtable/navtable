package es.udc.cartolab.gvsig.navtable.dataacces;

import java.util.HashMap;

import com.hardcode.gdbms.driver.exceptions.InitializeWriterException;
import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.cit.gvsig.exceptions.visitors.StartWriterVisitorException;
import com.iver.cit.gvsig.exceptions.visitors.StopWriterVisitorException;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;

public interface IController extends Cloneable {

    public long create(HashMap<String, String> newValues) throws Exception;
    
    public abstract void read(long position) throws ReadDriverException;

    public abstract void update(long position) throws ReadDriverException,
	    StopWriterVisitorException;

    public abstract void delete(long position)
	    throws StopWriterVisitorException, InitializeWriterException,
	    StartWriterVisitorException, ReadDriverException;

    public abstract void clearAll();

    public abstract int getIndex(String fieldName);

    public abstract int[] getIndexesOfValuesChanged();

    public abstract String getValue(String fieldName);

    public abstract String getValueInLayer(String fieldName);

    public abstract HashMap<String, String> getValues();

    public abstract HashMap<String, String> getValuesOriginal();

    public abstract HashMap<String, String> getValuesChanged();

    /**
     * Make sure the value set is a formatted value, as the ones from layer. See
     * {@link #fill(SelectableDataSource, long)} For example: if value is a
     * double in layer, the string should be something like 1000,00 instead of
     * 1000.
     */
    public abstract void setValue(String fieldName, String value);

    public abstract int getType(String fieldName);

    public abstract long getRowCount() throws ReadDriverException;

}