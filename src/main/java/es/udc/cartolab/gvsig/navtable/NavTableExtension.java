/*
 * This file is part of NavTable
 * Copyright (C) 2009 - 2010  Cartolab (Universidade da Coruña)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 * Authors:
 *   Juan Ignacio Varela García <nachouve (at) gmail (dot) com>
 *   Pablo Sanxiao Roca <psanxiao (at) gmail (dot) com>
 *   Javier Estévez Valiñas <valdaris (at) gmail (dot) com>
 *   Francisco Puga Alonso <fpuga (at) cartolab (dot) com>
 *   Jorge Lopez Fernandez <jlopez (at) cartolab (dot) es>
 */
package es.udc.cartolab.gvsig.navtable;

import java.io.File;
import java.util.ArrayList;

import org.gvsig.about.AboutManager;
import org.gvsig.andami.IconThemeHelper;
import org.gvsig.andami.Launcher;
import org.gvsig.andami.PluginServices;
import org.gvsig.andami.PluginsLocator;
import org.gvsig.andami.PluginsManager;
import org.gvsig.andami.plugins.Extension;
import org.gvsig.andami.preferences.IPreference;
import org.gvsig.andami.preferences.IPreferenceExtension;
import org.gvsig.andami.ui.mdiManager.IWindow;
import org.gvsig.app.ApplicationLocator;
import org.gvsig.app.ApplicationManager;
import org.gvsig.app.project.documents.table.gui.FeatureTableDocumentPanel;
import org.gvsig.app.project.documents.view.gui.IView;
import org.gvsig.fmap.dal.exception.DataException;
import org.gvsig.fmap.mapcontext.layers.FLayer;
import org.gvsig.fmap.mapcontext.layers.vectorial.FLyrVect;
import org.gvsig.fmap.mapcontext.layers.vectorial.VectorLayer;
import org.gvsig.tools.observer.Observable;
import org.gvsig.tools.observer.Observer;
import org.gvsig.utils.extensionPointsOld.ExtensionPoints;
import org.gvsig.utils.extensionPointsOld.ExtensionPointsSingleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.udc.cartolab.gvsig.navtable.contextualmenu.FiltersAddon;
import es.udc.cartolab.gvsig.navtable.contextualmenu.INavTableContextMenu;
import es.udc.cartolab.gvsig.navtable.contextualmenu.SorterAddon;
import es.udc.cartolab.gvsig.navtable.preferences.NavTablePreferencesPage;
import es.udc.cartolab.gvsig.navtable.preferences.Preferences;
import es.udc.cartolab.gvsig.navtable.utils.NavTableTocMenuEntry;

public class NavTableExtension extends Extension implements IPreferenceExtension {

private static final Logger logger = LoggerFactory
		.getLogger(NavTableExtension.class);
    private IPreference[] preferencesPage;


    public void execute(String actionCommand) {
	if (enableNavtable()) {
	    openNavtable();
	} 
    }

    private void openNavtable() {
	IWindow iWindow = PluginServices.getMDIManager().getActiveWindow();
	if(iWindow instanceof IView){
	    for (FLyrVect vectorialLyr : getActiveVectorialLayersOnTheActiveWindow()) {
		NavTable navtable = new NavTable(vectorialLyr);
		if (navtable.init()) {
		    PluginServices.getMDIManager().addCentredWindow(navtable);
		}
	    }
	} else if(isActiveWindowAttTableFromLayer()){
	    VectorLayer data = ((FeatureTableDocumentPanel) iWindow).getModel().getAssociatedLayer();
	    if(data instanceof FLyrVect){
		NavTable nt = new NavTable((FLyrVect) data);
		if(nt.init()){
		    PluginServices.getMDIManager().addCentredWindow(nt);
		}
	    }
	}
    }

    public void initialize() {
	ApplicationManager application = ApplicationLocator.getManager();
	AboutManager about = application.getAbout();
	about.addDeveloper("NavTable", getClass().getClassLoader().getResource("/about.htm"), 1);
	
    IconThemeHelper.registerIcon("action", "navtable", this);

	// Entry at TOC contextual menu
	ExtensionPoints extensionPoints = ExtensionPointsSingleton
		.getInstance();
	extensionPoints.add("View_TocActions", "NavTable",
		new NavTableTocMenuEntry());

	// Add NavTable "official" context menu addons to the extension point
	INavTableContextMenu filtersAddon = new FiltersAddon();
	extensionPoints.add(AbstractNavTable.NAVTABLE_CONTEXT_MENU,
		filtersAddon.getName(), filtersAddon);
	INavTableContextMenu sorterAddon = new SorterAddon();
	extensionPoints.add(AbstractNavTable.NAVTABLE_CONTEXT_MENU,
		sorterAddon.getName(), sorterAddon);

	// Creating config Dir
	File configDir;
	String configDirStr;
	try {
	    configDirStr = Launcher.getAppHomeDir()
		    + "NavTable";
	} catch (java.lang.NoSuchMethodError e) {
	    configDirStr = System.getProperty("user.home") + File.separator
		    + "gvSIG" + File.separator + "navTable";

	}
	configDir = new File(configDirStr);
	Preferences p = new Preferences(configDir);
    }


    public boolean isVisible() {
	return true;
    }

    public boolean isEnabled() {
    	return enableNavtable();
    }

    protected boolean enableNavtable() {
    	return !getActiveVectorialLayersOnTheActiveWindow().isEmpty() || isActiveWindowAttTableFromLayer();
    }

    private boolean isActiveWindowAttTableFromLayer() {
	IWindow w = PluginServices.getMDIManager().getActiveWindow();
	if(!(w instanceof FeatureTableDocumentPanel)){
	    return false;
	}
	return ((FeatureTableDocumentPanel) w).getModel().getAssociatedLayer() instanceof FLyrVect;
    }


    private ArrayList<FLyrVect> getActiveVectorialLayersOnTheActiveWindow() {
	IWindow iWindow = PluginServices.getMDIManager().getActiveWindow();
	ArrayList<FLyrVect> activeVectorialLayers = new ArrayList<FLyrVect>();

	if (iWindow instanceof IView) {
	    FLayer[] activeLayers = ((IView) iWindow).getMapControl()
		    .getMapContext().getLayers().getActives();
	    for (FLayer lyr : activeLayers) {
		if (lyr instanceof FLyrVect) {
		    activeVectorialLayers.add((FLyrVect) lyr);
		}
	    }
	}
	return activeVectorialLayers;
    }


    public IPreference[] getPreferencesPages() {
	if (preferencesPage == null) {
	    preferencesPage = new IPreference[] { new NavTablePreferencesPage() };
	}
	return preferencesPage;
    }
}