package es.udc.cartolab.gvsig.navtable.contextualmenu;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextField;

import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.FiltroExtension;

import es.udc.cartolab.gvsig.navtable.NavTable;

public class StringFilterActionListener extends JDialog implements ActionListener {

	Point point;
	String st_expr;
	FiltroExtension filterExt;
	JRadioButton rbContains;
	JRadioButton rbStartsWith;
	JRadioButton rbEndsWith; 

	public StringFilterActionListener(final Point point, 
			final String st_expr, 
			final FiltroExtension filterExt) {

		super();
		setTitle(PluginServices.getText(this,
				"Filter_substring"));
		
		Container cp = this.getContentPane();
		cp.setLayout(new FlowLayout());

		rbContains = new JRadioButton(PluginServices.getText(this,
		"filter_contains"));
		rbStartsWith = new JRadioButton(PluginServices.getText(this,
		"filter_startswith"));
		rbEndsWith = new JRadioButton(PluginServices.getText(this,
		"filter_endswith"));

		rbContains.setSelected(true);

		ButtonGroup bgroup = new ButtonGroup();
		bgroup.add(rbContains);
		bgroup.add(rbStartsWith);
		bgroup.add(rbEndsWith);

		cp.add(rbStartsWith);
		cp.add(rbEndsWith);
		cp.add(rbContains);

		cp.add(new JLabel(PluginServices.getText(this,
		"filter_write_string")));
		final JTextField tf = new JTextField(20);
		cp.add(tf);
		JButton ok = new JButton("OK");
		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String attr = tf.getText();
				executeFilter(attr);
				dispose(); // Closes the dialog
			}
		});
		cp.add(ok);
		setSize(250, 225);

		this.st_expr = st_expr;
		this.filterExt = filterExt;
		this.point = point;
	}

	private void executeFilter(final String attr){
		if (attr != null) {
			// TODO: We need to escape special characters
			// like '%', "'", ...
			String expr = "";
			if (rbStartsWith.isSelected()) {
				expr = st_expr + " like '" + attr + "%';";
			} else if (rbEndsWith.isSelected()) {
				expr = st_expr + " like '%" + attr + "';";
			} else {
				//rbContains.isSelected()
				expr = st_expr + " like '%" + attr + "%';";
			}
			filterExt.newSet(expr);
		}
	}

	public void actionPerformed(ActionEvent evt) {
		setLocation(point.x, point.y);
		setVisible(true);
	}
}