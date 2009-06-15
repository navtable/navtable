package es.udc.cartolab.gvsig.navtable;

import com.iver.andami.PluginServices;
import com.iver.andami.messages.NotificationManager;
import com.iver.andami.plugins.Extension;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.cit.gvsig.ProjectExtension;
import com.iver.cit.gvsig.fmap.DriverException;
import com.iver.cit.gvsig.fmap.edition.EditableAdapter;
import com.iver.cit.gvsig.fmap.edition.VectorialEditableAdapter;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.ReadableVectorial;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;
import com.iver.cit.gvsig.fmap.layers.layerOperations.AlphanumericData;
import com.iver.cit.gvsig.project.ProjectFactory;
import com.iver.cit.gvsig.project.documents.table.ProjectTable;
import com.iver.cit.gvsig.project.documents.table.ProjectTableFactory;
import com.iver.cit.gvsig.project.documents.table.gui.Table;
import com.iver.cit.gvsig.project.documents.view.gui.BaseView;


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
		
		SelectableDataSource recordset = table.getModel().getModelo().getRecordset();
		
		AlphanumericNavTable viewer = new AlphanumericNavTable(recordset);
		if (viewer.init()){
			PluginServices.getMDIManager().addCentredWindow(viewer);
		}
	}
	
	
	public void initialize() {
	}
}
