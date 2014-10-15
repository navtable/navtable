package es.udc.cartolab.gvsig.navtable.contextualmenu;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.TextAttribute;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;

import net.miginfocom.swing.MigLayout;
import sun.font.AttributeMap;

import com.iver.andami.PluginServices;

import es.icarto.gvsig.commons.Field;
import es.icarto.gvsig.commons.gui.AbstractIWindow;
import es.icarto.gvsig.commons.gui.WidgetFactory;

@SuppressWarnings("serial")
public class ChooseSortFieldDialog extends AbstractIWindow implements
	ActionListener {

    List<ChooseSortFieldPanel> list = new ArrayList<ChooseSortFieldPanel>();
    private String status;

    public ChooseSortFieldDialog(final List<Field> fields) {
	super(new MigLayout("insets 10, wrap 1"));
	WidgetFactory.acceptCancelPanel(this, this, this);

	final JButton addAnother = linkButton(PluginServices.getPluginServices(
		this).getText("sort_add_another"));
	addAnother.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		ChooseSortFieldPanel field = new ChooseSortFieldPanel(list
			.size(), fields);
		list.add(field);
		add(field);

		revalidate();
		repaint();
		getWindowInfo().setHeight(
			getWindowInfo().getHeight()
				+ field.getPreferredSize().height
				+ addAnother.getPreferredSize().height);
	    }
	});
	add(addAnother, "dock south");
	ChooseSortFieldPanel field = new ChooseSortFieldPanel(list.size(),
		fields);
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
	addAnother.setFont(font);
	addAnother.setForeground(new Color(0, 60, 140));
	AttributeMap attributes = (AttributeMap) font.getAttributes();
	attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
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
	PluginServices.getMDIManager().closeWindow(this);
    }

    public String open() {
	PluginServices.getMDIManager().addCentredWindow(this);
	return status;
    }

}
