package es.udc.cartolab.gvsig.navtable;

import java.io.File;

import com.iver.andami.PluginServices;
import com.iver.andami.plugins.Extension;
import com.iver.cit.gvsig.About;
import com.iver.cit.gvsig.EditionUtilities;
import com.iver.cit.gvsig.fmap.MapControl;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.FLayers;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.gui.panels.FPanelAbout;
import com.iver.cit.gvsig.project.documents.view.gui.BaseView;
import com.iver.utiles.extensionPoints.ExtensionPoints;
import com.iver.utiles.extensionPoints.ExtensionPointsSingleton;

import es.udc.cartolab.gvsig.navtable.NavTable;

public class NavTableExtension extends Extension{

	private NavTable viewer = null;
	
	public void execute(String actionCommand) {
		
		BaseView view = (BaseView)PluginServices.getMDIManager().getActiveWindow();
		MapControl mapControl = view.getMapControl();
		FLayers flayers = mapControl.getMapContext().getLayers();	
		FLyrVect actLayer = null;
		for (int i=0; i<flayers.getActives().length; i++) {
			actLayer = (FLyrVect)flayers.getActives()[i];
			viewer = new NavTable(actLayer);
			if (viewer.init()){
				PluginServices.getMDIManager().addCentredWindow(viewer);
			}
		}
		
		 // TODO: throw a message on the else (when there's no data)
		  // or something like that
	}

	public void initialize() {
		About about=(About)PluginServices.getExtension(About.class); 
		FPanelAbout panelAbout=about.getAboutPanel(); 
		java.net.URL aboutURL = this.getClass().getResource("/about.htm");
		panelAbout.addAboutUrl("NavTable", aboutURL);
		
		//Entry at TOC contextual menu
		ExtensionPoints extensionPoints = ExtensionPointsSingleton.getInstance();
		extensionPoints.add("View_TocActions", "NavTable", new NavTableTocMenuEntry());
		
		//Creating config Dir
		String configDirStr = System.getProperty("user.home") + File.separator + "gvSIG" + File.separator + "navTable";
		File configDir = new File(configDirStr);
		Preferences p = new Preferences(configDir);
	}

	public boolean isEnabled() {
		boolean enabled = false;
		int status = EditionUtilities.getEditionStatus();
		if (( status == EditionUtilities.EDITION_STATUS_ONE_VECTORIAL_LAYER_ACTIVE|| status == EditionUtilities.EDITION_STATUS_ONE_VECTORIAL_LAYER_ACTIVE_AND_EDITABLE)
				|| (status == EditionUtilities.EDITION_STATUS_MULTIPLE_VECTORIAL_LAYER_ACTIVE)|| (status == EditionUtilities.EDITION_STATUS_MULTIPLE_VECTORIAL_LAYER_ACTIVE_AND_EDITABLE))
		{
			BaseView view = (BaseView) PluginServices.getMDIManager().getActiveWindow();
			MapControl mapControl = view.getMapControl();
			FLayers flayers = mapControl.getMapContext().getLayers();
			int pos = flayers.getActives().length - 1;
			FLayer actLayer = flayers.getActives()[pos];
			if (actLayer instanceof FLyrVect) {
				// Too much files created for getRecordset()
//				try {
//					SelectableDataSource recordset = ((FLyrVect) actLayer).getRecordset();
//					if (recordset == null){
//						return false;
//					}
//					if (recordset.getRowCount() > 0) {
//						return true; 
//					}
//				} catch (DriverException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} catch (com.iver.cit.gvsig.fmap.DriverException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
				
				//TODO Check if the layer has at least one element
				enabled = true;
			}
		}
		return enabled;
	}

	public boolean isVisible() {
		com.iver.andami.ui.mdiManager.IWindow f = 
			PluginServices.getMDIManager().getActiveWindow();
		if (f == null) {
			return false;
		}
		if (f instanceof BaseView) {
			return true;
		}
		return false;
	}

}
