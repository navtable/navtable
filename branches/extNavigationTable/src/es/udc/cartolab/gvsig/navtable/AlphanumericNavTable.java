package es.udc.cartolab.gvsig.navtable;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;

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
		for (int i = 0; i < zoomB.getParent().getComponentCount(); i++) {			
			System.out.println(zoomB.getParent().getComponents()[i]);
			if (zoomB.getParent().getComponents()[i] != zoomB){
				index = i;
			}
		}
		
		URL imgURL = getClass().getResource("/filter.png");
		ImageIcon imagenNewRegister = new ImageIcon(imgURL);
		zoomB.setIcon(imagenNewRegister);
		//zoomB = new JButton(imagenNewRegister);
		
		//Change ZoomB for newB		
		zoomB.removeActionListener(zoomB.getActionListeners()[0]);
		//zoomB.setEnabled(false);
		zoomB.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				System.out.println("Prueba del Zoom para hacer de crear Nuevo");
			}			
		});
		alwaysZoomCB.setEnabled(false);
		
		// We must to rewrite selectionB listener and the others

		return true;
	}

	
}
