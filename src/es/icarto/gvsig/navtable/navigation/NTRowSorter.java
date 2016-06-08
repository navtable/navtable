package es.icarto.gvsig.navtable.navigation;

import java.util.Comparator;

import javax.swing.DefaultRowSorter;





import org.gvsig.fmap.dal.exception.DataException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.icarto.gvsig.navtable.gvsig2.SelectableDataSource;
import es.icarto.gvsig.navtable.gvsig2.Value;
import es.icarto.gvsig.navtable.gvsig2.ValueFactory;
import es.udc.cartolab.gvsig.navtable.utils.ValueComparator;

public class NTRowSorter<M extends SelectableDataSource> extends
	DefaultRowSorter<M, Integer> {
	
	private static final Logger logger = LoggerFactory
			.getLogger(NTRowSorter.class);
	
    private final M model;
    private final ValueComparator comparator;

    public NTRowSorter(M model) {
	super();
	this.model = model;
	setMaxSortKeys(5);
	setModelWrapper(new NTRowSorterModelWrapper());
	setSortsOnUpdates(true);
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
	    } catch (DataException e) {
		logger.error(e.getMessage(), e);
	    }
	    return 0;
	}

	@Override
	public int getRowCount() {
	    try {
		return (int) model.getRowCount();
	    } catch (DataException e) {
		logger.error(e.getMessage(), e);
	    }
	    return 0;
	}

	@Override
	public Object getValueAt(int row, int column) {
		return model.getFieldValue(row, column);
	}

	@Override
	public Integer getIdentifier(int row) {
	    return row;
	}
    }
}
