package es.udc.cartolab.gvsig.navtable;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTable;

import org.apache.log4j.Logger;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.FiltroExtension;
import com.iver.cit.gvsig.fmap.layers.FBitSet;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;

/**
 *
 * @author Francisco Puga <fpuga@cartolab.es>
 *
 */
public class FiltersAddon implements INavTableContextMenu {

    protected static Logger logger = Logger.getLogger("NavTable");

    private NavTable navtable;
    private SelectableDataSource sds;
    private JTable table;
    private String dataName;


    public void setNavtableInstance(NavTable navtable) {
	this.navtable = navtable;
	this.dataName = navtable.dataName;
	this.sds = navtable.recordset;
	this.table = navtable.table;
    }

    public String getName() {
	return this.getClass().getName();
    }

    public JMenuItem[] getMenuItems() {
	return getFilterMenusForRowSelected().toArray(new JMenuItem[0]);
    }

    private ArrayList<JMenuItem> getFilterMenusForRowSelected() {
	int rowSelected = table.getSelectedRow();

	final String attrName = (String) table.getModel().getValueAt(
		rowSelected, 0);
	final String attrValue = (String) table.getModel().getValueAt(
		rowSelected, 1);
	final int attrType = getAttrTypeForValueSelected(rowSelected);

	final FiltroExtension filterExt = new FiltroExtension();
	filterExt.setDatasource(sds, "");

	FBitSet fbitset = sds.getSelection();
	boolean isFilterSet = (fbitset.cardinality() > 0);

	final String st_expr = "select * from '" + sds.getName()
		+ "' where " + attrName;

	ArrayList<JMenuItem> menus = new ArrayList<JMenuItem>();
	if (attrType == java.sql.Types.VARCHAR) {
	    getMenuItemsForString(menus, attrValue, filterExt, st_expr);

	} else if (attrType == java.sql.Types.DOUBLE
		|| attrType == java.sql.Types.INTEGER) {

	    getMenuItemsForNumeric(menus, attrValue, filterExt, st_expr);

	} else if (attrType == java.sql.Types.BOOLEAN
		|| attrType == java.sql.Types.BIT) {
	    getMenuItemsFormBoolean(menus, filterExt, st_expr);

	} else {
	    // TODO OTHER TYPES (like DATE)
	    // the filter menu will not be shown on these types
	    menus = null;
	}

	JMenuItem tmpMenuItem = new JMenuItem(PluginServices.getText(this,
		"filter_filter"), navtable.getIcon("/filter.png"));
	tmpMenuItem.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent evt) {
		filterExt.initialize();
		filterExt.setDatasource(sds, dataName);
		filterExt.execute("FILTER_DATASOURCE");
	    }
	});
	menus.add(tmpMenuItem);

	if (isFilterSet) {
	    tmpMenuItem = new JMenuItem(PluginServices.getText(this,
		    "filter_remove_filter"), navtable.getIcon("/nofilter.png"));
	    tmpMenuItem.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent evt) {
		    navtable.clearSelection();
		}
	    });
	    menus.add(tmpMenuItem);
	}
	return menus;
    }

    private void getMenuItemsFormBoolean(ArrayList<JMenuItem> menus,
	    final FiltroExtension filterExt, final String st_expr) {

	JMenuItem tmpMenuItem = new JMenuItem(PluginServices.getText(this,
		"filter_equals") + " = TRUE");
	tmpMenuItem.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent evt) {
		String expr = st_expr + " = boolean('true');";
		filterExt.newSet(expr);
	    }
	});
	menus.add(tmpMenuItem);

	tmpMenuItem = new JMenuItem(PluginServices.getText(this,
		"filter_equals") + " = FALSE");
	tmpMenuItem.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent evt) {
		String expr = st_expr + " = boolean('false');";
		filterExt.newSet(expr);
	    }
	});
	menus.add(tmpMenuItem);
    }

    private void getMenuItemsForNumeric(ArrayList<JMenuItem> menus,
	    final String attrValue, final FiltroExtension filterExt,
	    final String st_expr) {

	JMenuItem tmpMenuItem = new JMenuItem(PluginServices.getText(this,
		"filter_numeric_equals") + " \t'" + attrValue + "'");
	tmpMenuItem.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent evt) {
		String expr = st_expr + " = " + attrValue + ";";
		filterExt.newSet(expr);
	    }
	});
	menus.add(tmpMenuItem);

	tmpMenuItem = new JMenuItem(PluginServices.getText(this,
		"filter_numeric_different") + " \t'" + attrValue + "'");
	tmpMenuItem.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent evt) {
		String expr = st_expr + " != " + attrValue + ";";
		filterExt.newSet(expr);
	    }
	});
	menus.add(tmpMenuItem);

	tmpMenuItem = new JMenuItem(PluginServices.getText(this,
		"filter_numeric_less") + " \t'" + attrValue + "'");
	tmpMenuItem.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent evt) {
		// TODO: Still not working. Remove option with
		// numbers. Open a dialog to type the '%...%'?
		String expr = st_expr + " < " + attrValue + ";";
		filterExt.newSet(expr);
	    }
	});
	menus.add(tmpMenuItem);

	tmpMenuItem = new JMenuItem(PluginServices.getText(this,
		"filter_numeric_greater") + " \t'" + attrValue + "'");
	tmpMenuItem.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent evt) {
		// TODO: Still not working. Remove option with
		// numbers. Open a dialog to type the '%...%'?
		String expr = st_expr + " > " + attrValue + ";";
		filterExt.newSet(expr);
	    }
	});
	menus.add(tmpMenuItem);
    }

    private void getMenuItemsForString(ArrayList<JMenuItem> menus,
	    final String attrValue, final FiltroExtension filterExt,
	    final String st_expr) {

	JMenuItem tmpMenuItem = new JMenuItem(PluginServices.getText(this,
		"filter_equals") + " '" + attrValue + "'");
	tmpMenuItem.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent evt) {
		String exp = st_expr + " = '" + attrValue + "';";
		filterExt.newSet(exp);
	    }
	});
	menus.add(tmpMenuItem);

	tmpMenuItem = new JMenuItem(PluginServices.getText(this,
		"filter_different") + " '" + attrValue + "'");
	tmpMenuItem.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent evt) {
		String exp = st_expr + " != '" + attrValue + "';";
		filterExt.newSet(exp);
	    }
	});
	menus.add(tmpMenuItem);

	tmpMenuItem = new JMenuItem(PluginServices.getText(this,
		"filter_contains"));
	tmpMenuItem.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent evt) {
		String attr = (String) JOptionPane.showInputDialog(
navtable,
			PluginServices.getText(this,
				"filter_write_string"), PluginServices.getText(
				this, "filter_contains_window_title"),
			JOptionPane.PLAIN_MESSAGE);

		if (attr != null) {
		    // TODO: We need to escape special characters
		    // like '%', "'", ...
		    String expr = st_expr + " like '%" + attr + "%';";
		    filterExt.newSet(expr);
		}
	    }
	});
	menus.add(tmpMenuItem);
    }

    private int getAttrTypeForValueSelected(int fieldIndex) {
	int attrType = -1;
	try {
	    attrType = sds.getFieldType(fieldIndex);
	} catch (ReadDriverException e1) {
	    logger.error(e1.getMessage());
	}
	return attrType;
    }

    public boolean isVisible() {
	return (table.getSelectedRowCount() == 1 && !isSelectedRowAreaOrLength());
    }

    private boolean isSelectedRowAreaOrLength() {
	if (table.getSelectedRow() > (table.getRowCount() - 2)) {
	    return true;
	} else {
	    return false;
	}
    }

}
