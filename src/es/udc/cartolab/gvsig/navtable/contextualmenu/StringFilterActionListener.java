package es.udc.cartolab.gvsig.navtable.contextualmenu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.iver.andami.PluginServices;
import com.iver.andami.ui.mdiFrame.MDIFrame;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.andami.ui.mdiManager.WindowInfo;
import com.iver.cit.gvsig.FiltroExtension;

import es.udc.cartolab.gvsig.navtable.NavTable;

public class StringFilterActionListener extends JPanel implements ActionListener, IWindow {

	String st_expr;
	String attrValue;
	FiltroExtension filterExt;

	JCheckBox cbStartsWith;
	JCheckBox cbEndsWith;
	
	WindowInfo windowInfo;

	public StringFilterActionListener(final String attrValue,
			final String st_expr, 
			final FiltroExtension filterExt) {

		super();
		
		this.st_expr = st_expr;
		this.attrValue = attrValue;
		this.filterExt = filterExt;

		initGUI();
	}

	private void initGUI() {

		windowInfo = this.getWindowInfo();
		
		add(new JLabel(PluginServices.getText(this,
		"filter_write_string")));
		final JTextField tf = new JTextField(16);
		add(tf);
		
		cbStartsWith = new JCheckBox(PluginServices.getText(this,
		"filter_startswith"));
		cbStartsWith.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt) {
				if (cbStartsWith.isSelected()){
					cbEndsWith.setSelected(false);
				}
			}
		});
		cbEndsWith = new JCheckBox(PluginServices.getText(this,
		"filter_endswith"));
		cbEndsWith.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt) {
				if (cbEndsWith.isSelected()){
					cbStartsWith.setSelected(false);
				}
			}
		});

		add(cbStartsWith);
		add(cbEndsWith);		
		
		JPanel btnPanel = new JPanel();
		
		JButton okBtn = new JButton(PluginServices.getText(this,"ok"));
		okBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String attr = tf.getText();
				executeFilter(attr);
				PluginServices.getMDIManager().closeWindow(StringFilterActionListener.this);
			}
		});
		btnPanel.add(okBtn);
		
		JButton cancelBtn = new JButton(PluginServices.getText(this,"cancel"));
		cancelBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				PluginServices.getMDIManager().closeWindow(StringFilterActionListener.this);
			}
		});
		btnPanel.add(cancelBtn);
		add(btnPanel);		
	}

	private void executeFilter(final String attr){
		if (attr != null) {
			// TODO: We need to escape special characters
			// like '%', "'", ...
			String expr = "";
			if (cbStartsWith.isSelected()) {
				expr = st_expr + " like '" + attr + "%';";
			} else if (cbEndsWith.isSelected()) {
				expr = st_expr + " like '%" + attr + "';";
			} else {
				//rbContains.isSelected()
				expr = st_expr + " like '%" + attr + "%';";
			}
			filterExt.newSet(expr);
		}
	}

	public void actionPerformed(ActionEvent evt) {
		PluginServices.getMDIManager().addWindow(this);
	}

	@Override
	public WindowInfo getWindowInfo() {
		if (windowInfo ==null){
			windowInfo = new WindowInfo(WindowInfo.MODALDIALOG
				| WindowInfo.PALETTE);
			windowInfo.setTitle(PluginServices.getText(this,
				"Filter_substring"));
			windowInfo.setWidth(220);
			windowInfo.setHeight(120);
		}
		return windowInfo;
	}

	@Override
	public Object getWindowProfile() {
		// TODO Auto-generated method stub
		return null;
	}

}