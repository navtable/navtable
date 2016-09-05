package es.udc.cartolab.gvsig.navtable.contextualmenu;

import static es.icarto.gvsig.commons.i18n.I18n._;

import java.util.Collections;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SortOrder;

import net.miginfocom.swing.MigLayout;
import es.icarto.gvsig.commons.gui.WidgetFactory;
import es.icarto.gvsig.commons.utils.Field;

@SuppressWarnings("serial")
public class ChooseSortFieldPanel extends JPanel {

	private static final String PROTOTYPE_DISPLAY_VALUE = "XXXXXXXXXXXXXXXXXXXXXXXX";

	private final ButtonGroup buttonGroup;

	private final JComboBox jComboBox;

	public ChooseSortFieldPanel(int priority, List<Field> fields) {
		super(new MigLayout("insets 10", "[90!][][]"));
		String jLabelText = _("sort_then_by");
		if (priority == 0) {
			jLabelText = _("sort_by");
		}
		add(new JLabel(jLabelText), "cell 0 0 1 2");
		Collections.sort(fields);
		jComboBox = WidgetFactory.combobox();
		jComboBox.addItem(Field.EMPTY_FIELD);
		for (Field f : fields) {
			jComboBox.addItem(f);
		}
		jComboBox.setPrototypeDisplayValue(PROTOTYPE_DISPLAY_VALUE);

		add(jComboBox, "cell 1 0 1 2");
		buttonGroup = new ButtonGroup();

		JRadioButton asc = new JRadioButton(_("sort_asc"));
		asc.setActionCommand(SortOrder.ASCENDING.toString());
		JRadioButton desc = new JRadioButton(_("sort_desc"));
		desc.setActionCommand(SortOrder.DESCENDING.toString());
		buttonGroup.add(asc);
		buttonGroup.add(desc);
		buttonGroup.setSelected(asc.getModel(), true);

		add(asc, "cell 2 0");
		add(desc, "cell 2 1");
	}

	private SortOrder getSortOrder() {
		String actionCommand = buttonGroup.getSelection().getActionCommand();
		return SortOrder.valueOf(actionCommand);
	}

	public Field getSelected() {
		Field field = null;
		Object selected = jComboBox.getSelectedItem();
		if ((selected != null) && (selected != Field.EMPTY_FIELD)) {
			field = (Field) selected;
			field.setSortOrder(getSortOrder());
		}
		return field;
	}
}
