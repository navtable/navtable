package es.udc.cartolab.gvsig.navtable;

import com.iver.andami.PluginServices;
import com.iver.andami.plugins.Extension;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.cit.gvsig.fmap.edition.IEditableSource;
import com.iver.cit.gvsig.project.documents.table.gui.Table;

public class AlphanumericNavTableExtension extends Extension {

	public boolean isEnabled() {
		
        IWindow v = PluginServices.getMDIManager().getActiveWindow();

        if (v == null) {
                return false;
        }

        if (v.getClass() == Table.class) {
                Table t = (Table) v;
                return true;
        }

        return false;
	}

	public boolean isVisible() {		
        IWindow v = PluginServices.getMDIManager().getActiveWindow();

        if (v == null) {
                return false;
        }

        if (v instanceof Table) {
                return true;
        } else {
                return false;
        }

		
	}

	public void execute(String s) {

		Table table = (Table) PluginServices.getMDIManager().getActiveWindow();
		
		IEditableSource model = table.getModel().getModelo();
		
		AlphanumericNavTable viewer = new AlphanumericNavTable(model);
		if (viewer.init()){
			PluginServices.getMDIManager().addCentredWindow(viewer);
		}
	}
	
	
	public void initialize() {
	}
}
