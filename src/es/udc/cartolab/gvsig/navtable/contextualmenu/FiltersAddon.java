package es.udc.cartolab.gvsig.navtable.contextualmenu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Types;
import java.text.ParseException;
import java.util.ArrayList;

import javax.swing.JMenuItem;
import javax.swing.JTable;

import org.apache.log4j.Logger;
import org.gvsig.andami.PluginServices;

import es.icarto.gvsig.navtable.gvsig2.FBitSet;
import es.icarto.gvsig.navtable.gvsig2.FiltroExtension;
import es.icarto.gvsig.navtable.gvsig2.SelectableDataSource;
import es.udc.cartolab.gvsig.navtable.NavTable;
import es.udc.cartolab.gvsig.navtable.format.ValueFactoryNT;

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
	    menus = getMenuItemsForString(filterExt, st_expr, attrValue);
	    break;

	case Types.INTEGER:
	case Types.BIGINT:
	case Types.SMALLINT:
	case Types.DOUBLE:
	case Types.DECIMAL:
	case Types.NUMERIC:
	case Types.FLOAT:
	case Types.REAL:
	    String attrValueWithgvSIGFormat = "";
	    try {
		attrValueWithgvSIGFormat = ValueFactoryNT.createValueByType(attrValue, attrType).toString();
	    } catch (ParseException e) {
		logger.error(e.getStackTrace(), e);
	    }
	    menus = getMenuItemsForNumeric(filterExt, st_expr,
		    attrValueWithgvSIGFormat, attrValue);
	    break;

	case Types.BOOLEAN:
	case Types.BIT:
	    menus = getMenuItemsForBoolean(filterExt, st_expr);
	    break;

	default:
	    // Undefined types (like Date, etc) will be shown only "setFilter"
	    // and "unSetFilter" options
	}

	menus.add(getMenuItemForSetFilter(filterExt));
	if(isFilterSet()) {
	    menus.add(getMenuItemForUnsetFilter());
	}

	return menus;
    }

    private JMenuItem getMenuItemForSetFilter(final FiltroExtension filterExt) {
	JMenuItem tmpMenuItem = new JMenuItem(PluginServices.getText(this,
		"filter_filter"), navtable.getIcon("/filter.png"));
	tmpMenuItem.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent evt) {
		filterExt.initialize();
		filterExt.setDatasource(sds, dataName);
		filterExt.execute("FILTER_DATASOURCE");
	    }
	});
	return tmpMenuItem;
    }



    private ArrayList<JMenuItem> getMenuItemsForBoolean(
	    final FiltroExtension filterExt, final String st_expr) {

	ArrayList<JMenuItem> booleanMenu = new ArrayList<JMenuItem>();

	JMenuItem tmpMenuItem = new JMenuItem(PluginServices.getText(this,
		"filter_equals") + " = TRUE");
	tmpMenuItem.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent evt) {
		String expr = st_expr + " = boolean('true');";
		executeFilter(filterExt,expr);
	    }
	});
	booleanMenu.add(tmpMenuItem);

	tmpMenuItem = new JMenuItem(PluginServices.getText(this,
		"filter_equals") + " = FALSE");
	tmpMenuItem.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent evt) {
		String expr = st_expr + " = boolean('false');";
		executeFilter(filterExt,expr);
	    }
	});
	booleanMenu.add(tmpMenuItem);

	return booleanMenu;
    }

    private ArrayList<JMenuItem> getMenuItemsForNumeric(
	    final FiltroExtension filterExt, final String st_expr,
	    final String attrValue, String attrValueAsNTFormat) {

	ArrayList<JMenuItem> numericMenu = new ArrayList<JMenuItem>();

	JMenuItem tmpMenuItem = new JMenuItem(PluginServices.getText(this,
		"filter_numeric_equals") + " \t'" + attrValueAsNTFormat + "'");
	tmpMenuItem.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent evt) {
		String expr = st_expr + " = " + attrValue + ";";
		executeFilter(filterExt,expr);
	    }
	});
	numericMenu.add(tmpMenuItem);

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
	numericMenu.add(tmpMenuItem);

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
	numericMenu.add(tmpMenuItem);

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
	numericMenu.add(tmpMenuItem);

	return numericMenu;
    }

    private ArrayList<JMenuItem> getMenuItemsForString(
	    final FiltroExtension filterExt, final String st_expr,
	    final String attrValue) {

	ArrayList<JMenuItem> stringMenu = new ArrayList<JMenuItem>();

	JMenuItem tmpMenuItem = new JMenuItem(PluginServices.getText(this,
		"filter_equals") + " '" + attrValue + "'");
	tmpMenuItem.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent evt) {
		String exp = st_expr + " = '" + attrValue + "';";
		executeFilter(filterExt, exp);
	    }
	});
	stringMenu.add(tmpMenuItem);

	tmpMenuItem = new JMenuItem(PluginServices.getText(this,
		"filter_different") + " '" + attrValue + "'");
	tmpMenuItem.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent evt) {
		String exp = st_expr + " != '" + attrValue + "';";
		executeFilter(filterExt, exp);
	    }
	});
	stringMenu.add(tmpMenuItem);

	tmpMenuItem = new JMenuItem(PluginServices.getText(this,
		"filter_contains"));
	tmpMenuItem.addActionListener(new StringFilterActionListener(navtable,
		attrValue,
		st_expr,
		filterExt));
	stringMenu.add(tmpMenuItem);

	return stringMenu;
    }

    private JMenuItem getMenuItemForUnsetFilter() {
	JMenuItem tmpMenuItem = new JMenuItem(PluginServices.getText(this,
		"filter_remove_filter"), navtable.getIcon("/nofilter.png"));
	tmpMenuItem.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent evt) {
		navtable.clearSelection();
	    }
	});
	return tmpMenuItem;
    }

    private boolean isFilterSet() {
	FBitSet fbitset = sds.getSelection();
	return (fbitset.cardinality() > 0);
    }

    private int getAttrTypeForValueSelected(int fieldIndex) {
	return sds.getFieldType(fieldIndex);
    }

    public boolean isVisible() {
	return (userVisibility && table.getSelectedRowCount() == 1 && !isSelectedRowAreaOrLength());
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
