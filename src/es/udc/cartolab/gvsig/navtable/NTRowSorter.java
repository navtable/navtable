package es.udc.cartolab.gvsig.navtable;

import javax.swing.DefaultRowSorter;

import org.apache.log4j.Logger;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.engine.values.ValueFactory;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;

public class NTRowSorter<M extends SelectableDataSource> extends DefaultRowSorter<M, Integer> {
    
    private static final Logger logger = Logger.getLogger(NTRowSorter.class);
    private M model;

    public NTRowSorter(M model) {
	this.model = model;
	setModelWrapper(new NTRowSorterModelWrapper());
    }
    
private class NTRowSorterModelWrapper extends ModelWrapper<M,Integer> {


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
