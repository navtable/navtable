package es.udc.cartolab.gvsig.navtable;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.TableCellRenderer;

/**
 * With this class, the navTable first column text is forced to
 * be bold.
 * 
 * @author Pablo Sanxiao
 *
 */
public class AttribTableCellRenderer extends JTextArea implements TableCellRenderer {

	private static final long serialVersionUID = 1L;

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		Font f = new Font("Serif", Font.BOLD, 12);
		this.setFont(f);
		this.setText((String)value);
		this.setBackground(new Color(240, 240, 240));
		if (isSelected){
			this.setBackground(new Color(195, 212, 232));
		}		
		return this;
	}	
}
