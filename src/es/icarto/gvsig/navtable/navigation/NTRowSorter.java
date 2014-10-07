package es.icarto.gvsig.navtable.navigation;

import java.util.Comparator;

import javax.swing.DefaultRowSorter;

import org.apache.log4j.Logger;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.engine.values.Value;
import com.hardcode.gdbms.engine.values.ValueFactory;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;

import es.udc.cartolab.gvsig.navtable.utils.ValueComparator;

public class NTRowSorter<M extends SelectableDataSource> extends
	DefaultRowSorter<M, Integer> {

    private static final Logger logger = Logger.getLogger(NTRowSorter.class);
    private final M model;
    private final ValueComparator comparator;

    public NTRowSorter(M model) {
	this.model = model;
	setModelWrapper(new NTRowSorterModelWrapper());
	setSortsOnUpdates(true); // TODO: listen for changes
	comparator = new ValueComparator();
    }

    /**
     * DefaultRowSorter translates all values to string and then uses
     * Collator.getInstace() as Comparator. To avoid it a custom comparator for
     * each column should be set using setComparator, but in our case, as the
     * ValueComparator handles all the available object is easy to override this
     * method
     */
    @Override
    public Comparator<Value> getComparator(int column) {
	return comparator;
    }

    private class NTRowSorterModelWrapper extends ModelWrapper<M, Integer> {

	@Override
	public M getModel() {
	    return model;
	}

	@Override
	public int getColumnCount() {

	    try {
		return model.getFieldCount();
	    } catch (ReadDriverException e) {
		logger.error(e.getStackTrace(), e);
	    }
	    return 0;
	}

	@Override
	public int getRowCount() {
	    try {
		return (int) model.getRowCount();
	    } catch (ReadDriverException e) {
		logger.error(e.getStackTrace(), e);
	    }
	    return 0;
	}

	@Override
	public Object getValueAt(int row, int column) {
	    try {
		return model.getFieldValue(row, column);
	    } catch (ReadDriverException e) {
		logger.error(e.getStackTrace(), e);
	    }
	    return ValueFactory.createNullValue();
	}

	@Override
	public Integer getIdentifier(int row) {
	    return row;
	}
    }
}
