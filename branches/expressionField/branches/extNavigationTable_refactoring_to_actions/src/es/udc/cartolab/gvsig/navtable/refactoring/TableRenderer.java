package es.udc.cartolab.gvsig.navtable.refactoring;

import java.util.Vector;

import javax.swing.table.DefaultTableModel;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.engine.values.Value;
import com.hardcode.gdbms.engine.values.ValueWriter;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;

public class TableRenderer {

    INavigationTable nt;

    public TableRenderer(INavigationTable nt) {
	this.nt = nt;
    }

    public void updateModel(DefaultTableModel model, SelectableDataSource sds) {
	try {
	    model.setRowCount(0);
	    if (sds.getRowCount() > 0) {
		for (int i = 0; i < sds.getFieldCount(); i++) {
		    String name = sds.getFieldName(i);
		    Vector<String> attr = new Vector<String>(2);
		    attr.add(name);
		    model.addRow(attr);
		    if (sds.getRowCount() > 0) {
			Value value = sds.getFieldValue(
				nt.getCurrentPosition(), i);
			String textoValue = value
				.getStringValue(ValueWriter.internalValueWriter);
			model.setValueAt(textoValue, i, 1);
		    }
		}
	    }
	} catch (ReadDriverException e) {
	    e.printStackTrace();
	}
    }
}
