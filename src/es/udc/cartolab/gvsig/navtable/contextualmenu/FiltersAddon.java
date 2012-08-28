package es.udc.cartolab.gvsig.navtable.contextualmenu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Types;
import java.util.ArrayList;

import javax.swing.JMenuItem;
import javax.swing.JTable;

import org.apache.log4j.Logger;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.FiltroExtension;
import com.iver.cit.gvsig.fmap.layers.FBitSet;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;

import es.udc.cartolab.gvsig.navtable.NavTable;
import es.udc.cartolab.gvsig.navtable.format.FormatAdapter;

/**
 * @author Nacho Varela
 * @author Francisco Puga <fpuga@cartolab.es>
 *
 */
public class FiltersAddon implements INavTableContextMenu {

    protected static Logger logger = Logger.getLogger("NavTable");

    private NavTable navtable;
    private SelectableDataSource sds;
    private JTable table;
    private String dataName;

    private boolean userVisibility = true;

    public void setNavtableInstance(NavTable navtable) {
	this.navtable = navtable;
	this.dataName = navtable.getDataName();
	this.sds = navtable.getRecordset();
	this.table = navtable.getTable();
    }

    public String getName() {
	return this.getClass().getName();
    }

    public String getDescription() {
	return "filters_addon_description";
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


	final String st_expr = "select * from '" + sds.getName() + "' where "
		+ attrName;

	ArrayList<JMenuItem> menus = new ArrayList<JMenuItem>();
	switch (attrType) {
	case Types.VARCHAR:
	case Types.CHAR:
	case Types.LONGVARCHAR:
	    getMenuItemsForString(menus, attrValue, filterExt, st_expr);
	    break;

	case Types.INTEGER:
	case Types.BIGINT:
	case Types.SMALLINT:
	case Types.DOUBLE:
	case Types.DECIMAL:
	case Types.NUMERIC:
	case Types.FLOAT:
	case Types.REAL:
	    String attrValueWithgvSIGFormat = FormatAdapter.toGvSIGString(
		    attrType, attrValue);
	    getMenuItemsForNumeric(menus, attrValueWithgvSIGFormat, attrValue,
		    filterExt, st_expr);
	    break;

	case Types.BOOLEAN:
	case Types.BIT:
	    getMenuItemsForBoolean(menus, filterExt, st_expr);
	    break;
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

	getRemoveFilterMenuItem(menus);
	return menus;
    }



    private void getMenuItemsForBoolean(ArrayList<JMenuItem> menus,
	    final FiltroExtension filterExt, final String st_expr) {

	JMenuItem tmpMenuItem = new JMenuItem(PluginServices.getText(this,
		"filter_equals") + " = TRUE");
	tmpMenuItem.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent evt) {
		String expr = st_expr + " = boolean('true');";
		executeFilter(filterExt,expr);
	    }
	});
	menus.add(tmpMenuItem);

	tmpMenuItem = new JMenuItem(PluginServices.getText(this,
		"filter_equals") + " = FALSE");
	tmpMenuItem.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent evt) {
		String expr = st_expr + " = boolean('false');";
		executeFilter(filterExt,expr);
	    }
	});
	menus.add(tmpMenuItem);
    }

    private void getMenuItemsForNumeric(ArrayList<JMenuItem> menus,
	    final String attrValue, String attrValueAsNTFormat,
	    final FiltroExtension filterExt,
	    final String st_expr) {

	JMenuItem tmpMenuItem = new JMenuItem(PluginServices.getText(this,
		"filter_numeric_equals") + " \t'" + attrValueAsNTFormat + "'");
	tmpMenuItem.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent evt) {
		String expr = st_expr + " = " + attrValue + ";";
		executeFilter(filterExt,expr);
	    }
	});
	menus.add(tmpMenuItem);

	tmpMenuItem = new JMenuItem(PluginServices.getText(this,
		"filter_numeric_different")
		+ " \t'"
		+ attrValueAsNTFormat
		+ "'");
	tmpMenuItem.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent evt) {
		String expr = st_expr + " != " + attrValue + ";";
		executeFilter(filterExt,expr);
	    }
	});
	menus.add(tmpMenuItem);

	tmpMenuItem = new JMenuItem(PluginServices.getText(this,
		"filter_numeric_less") + " \t'" + attrValueAsNTFormat + "'");
	tmpMenuItem.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent evt) {
		// TODO: Still not working. Remove option with
		// numbers. Open a dialog to type the '%...%'?
		String expr = st_expr + " < " + attrValue + ";";
		executeFilter(filterExt,expr);
	    }
	});
	menus.add(tmpMenuItem);

	tmpMenuItem = new JMenuItem(PluginServices.getText(this,
		"filter_numeric_greater") + " \t'" + attrValueAsNTFormat + "'");
	tmpMenuItem.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent evt) {
		// TODO: Still not working. Remove option with
		// numbers. Open a dialog to type the '%...%'?
		String expr = st_expr + " > " + attrValue + ";";
		executeFilter(filterExt,expr);
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
		executeFilter(filterExt, exp);
	    }
	});
	menus.add(tmpMenuItem);

	tmpMenuItem = new JMenuItem(PluginServices.getText(this,
		"filter_different") + " '" + attrValue + "'");
	tmpMenuItem.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent evt) {
		String exp = st_expr + " != '" + attrValue + "';";
		executeFilter(filterExt, exp);
	    }
	});
	menus.add(tmpMenuItem);

	tmpMenuItem = new JMenuItem(PluginServices.getText(this,
		"filter_contains"));
	tmpMenuItem.addActionListener(new StringFilterActionListener(navtable,
		attrValue,
		st_expr,
		filterExt));
	menus.add(tmpMenuItem);
    }

    private void getRemoveFilterMenuItem(ArrayList<JMenuItem> menus) {
	FBitSet fbitset = sds.getSelection();
	boolean isFilterSet = (fbitset.cardinality() > 0);
	if (isFilterSet) {
	    JMenuItem tmpMenuItem = new JMenuItem(PluginServices.getText(this,
		    "filter_remove_filter"), navtable.getIcon("/nofilter.png"));
	    tmpMenuItem.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent evt) {
		    navtable.clearSelection();
		}
	    });
	    menus.add(tmpMenuItem);
	}
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

    private boolean isDateField() {
	int rowSelected = table.getSelectedRow();
	int attrType = getAttrTypeForValueSelected(rowSelected);
	if(attrType == Types.DATE) {
	    return true;
	}
	return false;
    }

    public boolean isVisible() {
	return (!isDateField() && userVisibility && table.getSelectedRowCount() == 1 && !isSelectedRowAreaOrLength());
    }

    private boolean isSelectedRowAreaOrLength() {
	if (navtable.isAlphanumericNT()) {
	    return false;
	}
	if (table.getSelectedRow() >= (table.getRowCount() - 2)) {
	    return true;
	} else {
	    return false;
	}
    }

    public void setUserVisibility(boolean userVisibility) {
	this.userVisibility = userVisibility;
    }

    public boolean getDefaultVisibiliy() {
	return true;
    }

    public void executeFilter(final FiltroExtension filterExt,
	    final String st_expr){
	filterExt.newSet(st_expr);
	navtable.setOnlySelected(true);
    }
}
