package es.udc.cartolab.gvsig.navtable.contextualmenu;

import static es.icarto.gvsig.commons.i18n.I18n._;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.TextAttribute;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;

import org.gvsig.andami.ui.mdiManager.MDIManagerFactory;

import es.icarto.gvsig.commons.gui.AbstractIWindow;
import es.icarto.gvsig.commons.gui.OkCancelPanel;
import es.icarto.gvsig.commons.gui.WidgetFactory;
import es.icarto.gvsig.commons.utils.Field;
import net.miginfocom.swing.MigLayout;

@SuppressWarnings("serial")
public class ChooseSortFieldDialog extends AbstractIWindow implements ActionListener {

	List<ChooseSortFieldPanel> list = new ArrayList<ChooseSortFieldPanel>();
	private String status = OkCancelPanel.CANCEL_ACTION_COMMAND;
	private final OkCancelPanel okPanel;

	public ChooseSortFieldDialog(final List<Field> fields) {
		super(new MigLayout("insets 10, wrap 1"));
		okPanel = WidgetFactory.okCancelPanel(this, this, this);

		final JButton addAnother = linkButton(_("sort_add_another"));

		addAnother.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				ChooseSortFieldPanel field = new ChooseSortFieldPanel(list.size(), fields);
				list.add(field);
				add(field);

				revalidate();
				repaint();
				getWindowInfo().setHeight(getWindowInfo().getHeight() + field.getPreferredSize().height
						+ addAnother.getPreferredSize().height);
			}
		});
		add(addAnother, "dock south");
		ChooseSortFieldPanel field = new ChooseSortFieldPanel(list.size(), fields);
		list.add(field);
		add(field);

	}

	/**
	 * Returns a JButton which looks like an hyperlink
	 */
	private JButton linkButton(String text) {
		final JButton addAnother = new JButton(text);
		addAnother.setFocusPainted(false);
		addAnother.setCursor(new Cursor(Cursor.HAND_CURSOR));
		addAnother.setMargin(new Insets(0, 0, 0, 0));
		addAnother.setContentAreaFilled(false);
		addAnother.setBorderPainted(false);
		addAnother.setOpaque(false);
		Font font = new Font("Arial", Font.BOLD | Font.ITALIC, 11);
		addAnother.setForeground(new Color(0, 60, 140));
		Map<TextAttribute, Object> attributes = new HashMap<TextAttribute, Object>();
		attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
		Font newFont = font.deriveFont(attributes);
		addAnother.setFont(newFont);
		return addAnother;
	}

	/**
	 * Returns the fields that have a SortOrder != UNSORTED
	 */
	public List<Field> getFields() {
		List<Field> orderedFields = new ArrayList<Field>();
		for (ChooseSortFieldPanel l : list) {
			if (l.getSelected() != null) {
				orderedFields.add(l.getSelected());
			}
		}
		return orderedFields;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		status = e.getActionCommand();
		MDIManagerFactory.getManager().closeWindow(this);
	}

	public String open() {
		super.openDialog();
		return status;
	}

	@Override
	protected JButton getDefaultButton() {
		return okPanel.getOkButton();
	}

	@Override
	protected Component getDefaultFocusComponent() {
		return null;
	}

}
