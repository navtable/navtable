package es.udc.cartolab.gvsig.navtable;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import com.iver.andami.PluginServices;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.andami.ui.mdiManager.WindowInfo;

public class PreferencesPanel extends JPanel implements IWindow, ActionListener {
	
	private WindowInfo viewInfo = null;
	
	// general panel options
	private JCheckBox nullValuesCheckBox;
	private JCheckBox emptyStringsCheckBox;
	private JCheckBox autoSaveCheckBox;
	private JCheckBox noSaveWarningCheckBox;
	private JTextField minScaleField;
	private JTextField maxScaleField;
	
	//buttons
	private JButton okButton;
	private JButton cancelButton;
	private JButton applyButton;
	

	public WindowInfo getWindowInfo() {
		// TODO Auto-generated method stub
		if (viewInfo == null) {
			viewInfo = new WindowInfo(WindowInfo.MODELESSDIALOG | WindowInfo.RESIZABLE | WindowInfo.PALETTE);
			viewInfo.setTitle(PluginServices.getText(this,
			"Preferences"));
			viewInfo.setWidth(400);
			viewInfo.setHeight(250);
		}
		return viewInfo;
	}
	
	public PreferencesPanel() {
		
		GridBagLayout layout = new GridBagLayout();
		setLayout(layout);
		
		JTabbedPane tabbedPane = new JTabbedPane();
		
		// general tab
		JPanel generalPanel = new JPanel();
		generalPanel.setLayout(new GridBagLayout());
		
		nullValuesCheckBox = new JCheckBox("Show null values");
		emptyStringsCheckBox = new JCheckBox("Show empty strings values");
		autoSaveCheckBox = new JCheckBox("Save values automatically while navigating");
		autoSaveCheckBox.addActionListener(this);
		noSaveWarningCheckBox = new JCheckBox("Show warning when modified values haven't been saved");
		noSaveWarningCheckBox.addActionListener(this);
		minScaleField = new JTextField(7);
		maxScaleField = new JTextField(7);
		
		generalPanel.add(nullValuesCheckBox, new GridBagConstraints(
				0, 0, 2, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0),
				0, 0));
		generalPanel.add(emptyStringsCheckBox, new GridBagConstraints(
				0, 1, 2, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0),
				0, 0));
		generalPanel.add(autoSaveCheckBox, new GridBagConstraints(
				0, 2, 2, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0),
				0, 0));
		generalPanel.add(noSaveWarningCheckBox, new GridBagConstraints(
				0, 3, 2, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0),
				0, 0));
		
		generalPanel.add(new JLabel("Min scale zoom"),
				new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, 
						GridBagConstraints.WEST,
						GridBagConstraints.NONE,
						new Insets(0, 0, 0, 0),
						0, 0));
		
		generalPanel.add(minScaleField, new GridBagConstraints(
				1, 4, 1, 1, 0.0, 0.0, GridBagConstraints.EAST,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0),
				0, 0));
		
		generalPanel.add(new JLabel("Max scale zoom"),
				new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0,
						GridBagConstraints.WEST,
						GridBagConstraints.NONE,
						new Insets(0, 0, 0, 0),
						0, 0));
		
		generalPanel.add(maxScaleField, new GridBagConstraints(
				1, 5, 1, 1, 0.0, 0.0, GridBagConstraints.EAST,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0),
				0, 0));
		
		JScrollPane generalScrollPane = new JScrollPane(generalPanel);
		
		tabbedPane.addTab("General", generalScrollPane);
		
		// files tab
		tabbedPane.addTab("Files", new JPanel());
		
		// copy tab
		tabbedPane.addTab("Copy", new JPanel());
		
		
		add(tabbedPane, new GridBagConstraints(
				0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTH,
				GridBagConstraints.BOTH, new Insets(0, 0, 0, 0),
				0, 0));
		
		
		//buttons
		okButton = new JButton("Accept");
		okButton.addActionListener(this);
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);
		applyButton = new JButton("Apply");
		applyButton.addActionListener(this);
		
		JPanel buttonsPanel = new JPanel();
		FlowLayout buttonsLayout = new FlowLayout();
		buttonsLayout.setAlignment(FlowLayout.RIGHT);
		buttonsPanel.setLayout(buttonsLayout);
		
		buttonsPanel.add(okButton);
		
		buttonsPanel.add(cancelButton);
		
		buttonsPanel.add(applyButton);
		
		add(buttonsPanel, new GridBagConstraints(
				0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0),
				0, 0));
		
	}

	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
		if (e.getSource() == cancelButton) {
			PluginServices.getMDIManager().closeWindow(this);
		}
		if (e.getSource() == autoSaveCheckBox) {
			noSaveWarningCheckBox.setSelected(false);
		}
		if (e.getSource() == noSaveWarningCheckBox) {
			autoSaveCheckBox.setSelected(false);
		}
	}

}
