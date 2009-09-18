package es.udc.cartolab.gvsig.navtable;

import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.project.documents.view.toc.AbstractTocContextMenuAction;
import com.iver.cit.gvsig.project.documents.view.toc.ITocItem;

public class NavTableTocMenuEntry extends AbstractTocContextMenuAction {
	
	private NavTable viewer = null;

	public void execute(ITocItem item, FLayer[] selectedItems) {
		
		
		viewer = new NavTable((FLyrVect) selectedItems[0]);
		if (viewer.init()){
			PluginServices.getMDIManager().addCentredWindow(viewer);
		}
		
	}

	public String getText() {
		// TODO Auto-generated method stub
		return PluginServices.getText(this, "open_navigation_table");

	}

	public String getGroup() {
		return "navtable"; //FIXME
	}

	public int getGroupOrder() {
		return 100;
	}

	public int getOrder() {
		return 1;
	}

	public boolean isEnabled(ITocItem item, FLayer[] selectedItems) {
		return true;
	}

	public boolean isVisible(ITocItem item, FLayer[] selectedItems) {
		if (isTocItemBranch(item) && ! (selectedItems == null || selectedItems.length <= 0)) {
			if (selectedItems.length == 1 && selectedItems[0] instanceof FLyrVect) {
				return true;
			}
		}
		return false;
	}
	
}
