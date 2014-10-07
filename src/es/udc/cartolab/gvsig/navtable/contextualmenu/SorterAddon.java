package es.udc.cartolab.gvsig.navtable.contextualmenu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.swing.JMenuItem;
import javax.swing.JTable;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;

import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;

import es.udc.cartolab.gvsig.navtable.NTRowSorter;
import es.udc.cartolab.gvsig.navtable.NavTable;

public class SorterAddon implements INavTableContextMenu {

    private NavTable navtable;
    private JTable table;

    private boolean userVisibility = true;

    @Override
    public String getName() {
	return this.getClass().getName();
    }

    @Override
    public String getDescription() {
	return "sorter_addon_description";
    }

    @Override
    public JMenuItem[] getMenuItems() {
	final int rowSelected = table.getSelectedRow();
	JMenuItem[] menu = new JMenuItem[4];
	JMenuItem asc = new JMenuItem(PluginServices.getText(this, "sort_asc"));
	asc.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		SortKey sortKey = new SortKey(rowSelected, SortOrder.ASCENDING);
		NTRowSorter<SelectableDataSource> sorter = new NTRowSorter<SelectableDataSource>(
			navtable.getRecordset());
		sorter.setSortKeys(Arrays.asList(sortKey));
		navtable.setRowSorter(sorter);
	    }
	});
	menu[0] = asc;

	JMenuItem desc = new JMenuItem(
		PluginServices.getText(this, "sort_desc"));
	desc.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		SortKey sortKey = new SortKey(rowSelected, SortOrder.DESCENDING);
		NTRowSorter<SelectableDataSource> sorter = new NTRowSorter<SelectableDataSource>(
			navtable.getRecordset());
		sorter.setSortKeys(Arrays.asList(sortKey));
		navtable.setRowSorter(sorter);
	    }
	});
	menu[1] = desc;

	JMenuItem advanced = new JMenuItem(PluginServices.getText(this,
		"sort_advanced"));
	menu[2] = advanced;

	JMenuItem defaultSort = new JMenuItem(PluginServices.getText(this,
		"sort_default"));
	defaultSort.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		navtable.setRowSorter(null);
	    }
	});
	menu[3] = defaultSort;
	return menu;
    }

    @Override
    public boolean isVisible() {
	return (userVisibility && table.getSelectedRowCount() == 1);
    }

    @Override
    public void setUserVisibility(boolean userVisibility) {
	this.userVisibility = userVisibility;
    }

    @Override
    public void setNavtableInstance(NavTable navtable) {
	this.navtable = navtable;
	this.table = navtable.getTable();
    }

    @Override
    public boolean getDefaultVisibiliy() {
	return true;
    }
}
