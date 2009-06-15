package es.udc.cartolab.gvsig.navtable;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.fmap.layers.FBitSet;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;

public class AlphanumericNavTable extends NavTable {

	JButton newB = null;	
	
	public AlphanumericNavTable(SelectableDataSource recordset) {
		super(recordset);
	}

	public boolean init() {
		super.init();
		
		int index = -1;
		zoomB.setVisible(false);
		alwaysZoomCB.setVisible(false);
		fixScaleCB.setVisible(false);
		
		URL imgURL = getClass().getResource("/table_add.png");
		ImageIcon imagenNewRegister = new ImageIcon(imgURL);
		newB = new JButton(imagenNewRegister);
		//TODO Add the string to the i18n to traslate
		newB.setToolTipText(PluginServices.getText(this,
							"new_register"));
		
		newB.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				System.out.println("Prueba del Zoom para hacer de crear Nuevo");
				// TODO Create a new register on the table
				// Refresh recordset
				// Go to the last record on the 
			}			
		});
		zoomB.getParent().add(newB);
		// We must to rewrite selectionB listener and the others

		return true;
	}

}
