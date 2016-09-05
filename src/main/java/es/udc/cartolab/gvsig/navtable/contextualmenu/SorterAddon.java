package es.udc.cartolab.gvsig.navtable.contextualmenu;

import static es.icarto.gvsig.commons.i18n.I18n._;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JTable;
import javax.swing.SortOrder;
import javax.swing.table.TableModel;

import es.icarto.gvsig.commons.gui.OkCancelPanel;
import es.icarto.gvsig.commons.utils.Field;
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

	// private final boolean notSortBigLayers() {
	// long rows;
	// boolean nosort = false;
	// try {
	// rows = navtable.getRecordset().getRowCount();
	// if (rows > 500) {
	// JOptionPane.showMessageDialog(navtable,
	// _("sorter_addon_experimental"));
	// nosort = true;
	// }
	// } catch (DataException e) {
	// logger.error(e.getMessage(), e);
	// JOptionPane.showMessageDialog(navtable,
	// _("sorter_addon_experimental"));
	// nosort = true;
	// }
	// return nosort;
	// }

	@Override
	public JMenuItem[] getMenuItems() {
		final int rowSelected = table.getSelectedRow();
		final String fieldname = table.getValueAt(rowSelected, 0).toString();
		final Field field = new Field(fieldname);
		JMenuItem[] menu = new JMenuItem[4];
		JMenuItem asc = new JMenuItem(_("sort_asc"));
		asc.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// if (notSortBigLayers()) {
				// return;
				// }
				field.setSortOrder(SortOrder.ASCENDING);
				navtable.setSortKeys(Arrays.asList(field));
			}
		});
		menu[0] = asc;

		JMenuItem desc = new JMenuItem(_("sort_desc"));
		desc.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// if (notSortBigLayers()) {
				// return;
				// }
				field.setSortOrder(SortOrder.DESCENDING);
				navtable.setSortKeys(Arrays.asList(field));
			}
		});
		menu[1] = desc;

		JMenuItem advanced = new JMenuItem(_("sort_advanced"));
		menu[2] = advanced;
		advanced.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// if (notSortBigLayers()) {
				// return;
				// }

				List<Field> fields = new ArrayList<Field>();
				TableModel model = table.getModel();
				for (int i = 0; i < table.getRowCount() - 1; i++) {
					String name = model.getValueAt(i, 0).toString();
					fields.add(new Field(name, name));
				}

				ChooseSortFieldDialog dialog = new ChooseSortFieldDialog(fields);

				if (dialog.open().equals(OkCancelPanel.OK_ACTION_COMMAND)) {
					List<Field> sortedFields = dialog.getFields();
					navtable.setSortKeys(sortedFields);
				}
			}
		});

		JMenuItem defaultSort = new JMenuItem(_("sort_default"));
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
