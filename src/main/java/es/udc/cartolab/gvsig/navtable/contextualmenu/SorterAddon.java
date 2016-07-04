package es.udc.cartolab.gvsig.navtable.contextualmenu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;

import org.gvsig.andami.PluginServices;
import org.gvsig.fmap.dal.exception.DataException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.icarto.gvsig.commons.gui.OkCancelPanel;
import es.icarto.gvsig.commons.gvsig2.SelectableDataSource;
import es.icarto.gvsig.commons.utils.Field;
import es.udc.cartolab.gvsig.navtable.NavTable;

public class SorterAddon implements INavTableContextMenu {
	
	
	private static final Logger logger = LoggerFactory.getLogger(SorterAddon.class);
	
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
    
    private final boolean notSortBigLayers() {
    	long rows;
    	boolean nosort = false;
		try {
			rows = navtable.getRecordset().getRowCount();
			if (rows > 500) {
	    		JOptionPane.showMessageDialog(navtable, "Esta característica es experimental y no se puede aplicar a capas con más de 500 registros");
	    		nosort = true;
	    	}
		} catch (DataException e) {
			logger.error(e.getMessage(), e);
			JOptionPane.showMessageDialog(navtable, "Esta característica es experimental y no se puede aplicar a capas con más de 500 registros");
			nosort = true;
		}
    	return nosort;
	}

    @Override
    public JMenuItem[] getMenuItems() {
	final int rowSelected = table.getSelectedRow();
	JMenuItem[] menu = new JMenuItem[4];
	JMenuItem asc = new JMenuItem(PluginServices.getText(this, "sort_asc"));
	asc.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
	    	if ( notSortBigLayers() ) {
	    		return;
	    	}
		SortKey sortKey = new SortKey(rowSelected, SortOrder.ASCENDING);
		navtable.setSortKeys(Arrays.asList(sortKey));
	    }
	});
	menu[0] = asc;

	JMenuItem desc = new JMenuItem(
		PluginServices.getText(this, "sort_desc"));
	desc.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
	    	if ( notSortBigLayers() ) {
	    		return;
	    	}
		SortKey sortKey = new SortKey(rowSelected, SortOrder.DESCENDING);
		navtable.setSortKeys(Arrays.asList(sortKey));
	    }
	});
	menu[1] = desc;

	JMenuItem advanced = new JMenuItem(PluginServices.getText(this,
		"sort_advanced"));
	menu[2] = advanced;
	advanced.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
	    	if ( notSortBigLayers() ) {
	    		return;
	    	}
		String[] fieldNames = new String[0];
		fieldNames = navtable.getRecordset().getFieldNames();

		List<Field> fields = new ArrayList<Field>();
		for (String name : fieldNames) {
		    fields.add(new Field(name, name));
		}
		ChooseSortFieldDialog dialog = new ChooseSortFieldDialog(fields);

		if (dialog.open().equals(OkCancelPanel.OK_ACTION_COMMAND)) {
		    List<Field> sortedFields = dialog.getFields();
		    List<SortKey> sortKeys = new ArrayList<SortKey>();
		    SelectableDataSource sds = navtable.getRecordset();
		    for (Field field : sortedFields) {
		    	int fieldIdx = sds.getFieldIndexByName(field.getKey());
			    sortKeys.add(new SortKey(fieldIdx, field.getSortOrder()));
		    }
		    navtable.setSortKeys(sortKeys);
		}
	    }
	});

	JMenuItem defaultSort = new JMenuItem(PluginServices.getText(this,
		"sort_default"));
	defaultSort.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		navtable.setSortKeys(null);
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
