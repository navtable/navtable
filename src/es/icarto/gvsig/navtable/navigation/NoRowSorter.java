package es.icarto.gvsig.navtable.navigation;

import java.util.List;

import javax.swing.RowSorter;

import org.apache.log4j.Logger;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;

public class NoRowSorter<M extends SelectableDataSource> extends RowSorter<M> {

    private static final Logger logger = Logger.getLogger(NoRowSorter.class);

    private final M model;

    public NoRowSorter(M model) {
	this.model = model;
    }

    @Override
    public M getModel() {
	return model;
    }

    @Override
    public void toggleSortOrder(int column) {
    }

    @Override
    public int convertRowIndexToModel(int index) {
	return index;
    }

    @Override
    public int convertRowIndexToView(int index) {
	return index;
    }

    @Override
    public void setSortKeys(List<? extends RowSorter.SortKey> keys) {
    }

    @Override
    public List<? extends RowSorter.SortKey> getSortKeys() {
	return null;
    }

    @Override
    public int getViewRowCount() {
	try {
	    return (int) model.getRowCount();
	} catch (ReadDriverException e) {
	    logger.error(e.getStackTrace(), e);
	}
	return 0;
    }

    @Override
    public int getModelRowCount() {
	return getViewRowCount();
    }

    @Override
    public void modelStructureChanged() {
    }

    @Override
    public void allRowsChanged() {
    }

    @Override
    public void rowsInserted(int firstRow, int endRow) {
    }

    @Override
    public void rowsDeleted(int firstRow, int endRow) {
    }

    @Override
    public void rowsUpdated(int firstRow, int endRow) {
    }

    @Override
    public void rowsUpdated(int firstRow, int endRow, int column) {
    }

}
