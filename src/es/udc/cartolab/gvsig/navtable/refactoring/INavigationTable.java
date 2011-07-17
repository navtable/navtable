package es.udc.cartolab.gvsig.navtable.refactoring;

import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;

public interface INavigationTable {

    /**
     * Returns true if we should only navigate through selected features. False,
     * otherwise.
     */
    public boolean isOnlyNavigateThroughSelection();

    public void setOnlyNavigateThroughSelected(boolean b);

    public SelectableDataSource getSelectableDataSource();

    /* getter & setter for current position */

    public int getCurrentPosition();

    public void setCurrentPosition(int index);

    public int getIndexOfLastRecord();

}
