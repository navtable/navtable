package es.udc.cartolab.gvsig.navtable;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;

public class NavTablePopupMenu extends JPopupMenu implements ActionListener {
	
	private JCheckBoxMenuItem showNullMenuItem;
	private JCheckBoxMenuItem showEmptyMenuItem;
	private JMenuItem preferencesMenuItem;
	private JMenuItem editFieldsMenuItem;
	private JMenuItem ascOrderMenuItem;
	private JMenuItem descOrderMenuItem;
	private FLyrVect layer;
	
	public NavTablePopupMenu (FLyrVect lyr) {
		super();
		
		layer = lyr;
		
		//create all menu items
		ascOrderMenuItem = new JMenuItem("Ascending order",
				new ImageIcon("gvSIG/extensiones/es.udc.cartolab.gvsig.navtable/images/orderasc.png"));
		descOrderMenuItem = new JMenuItem("Descending order",
				new ImageIcon("gvSIG/extensiones/es.udc.cartolab.gvsig.navtable/images/orderdesc.png"));
		
		showNullMenuItem = new JCheckBoxMenuItem("Show null values");
		showNullMenuItem.setSelected(true);
		
		showEmptyMenuItem = new JCheckBoxMenuItem("Show empty strings");
		showEmptyMenuItem.setSelected(true);
		
		//TODO add an icon to edit fields menu item
		editFieldsMenuItem = new JMenuItem("View / Edit fields...");
		editFieldsMenuItem.addActionListener(this);
		
		preferencesMenuItem = new JMenuItem("Preferences...", 
				new ImageIcon("gvSIG/extensiones/es.udc.cartolab.gvsig.navtable/images/preferences.png"));
		preferencesMenuItem.addActionListener(this);
		
		//add all the menu items
		add(showNullMenuItem);
		add(showEmptyMenuItem);
		add(ascOrderMenuItem);
		add(descOrderMenuItem);
		addSeparator();
		add(editFieldsMenuItem);
		addSeparator();
		add(preferencesMenuItem);
	}

	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if (e.getSource()==editFieldsMenuItem) {
			EditTableSchemaPanel view = new EditTableSchemaPanel(layer);
			PluginServices.getMDIManager().addWindow(view);
		}
		if (e.getSource()==preferencesMenuItem) {
			PreferencesPanel preferencesPanel = new PreferencesPanel();
			PluginServices.getMDIManager().addCentredWindow(preferencesPanel);
		}
	}
}
