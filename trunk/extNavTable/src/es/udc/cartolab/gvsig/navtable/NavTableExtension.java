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
 */
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

public class NavTableExtension extends Extension {

    private NavTable viewer = null;

    public void execute(String actionCommand) {

	BaseView view = (BaseView) PluginServices.getMDIManager()
		.getActiveWindow();
	MapControl mapControl = view.getMapControl();
	FLayers flayers = mapControl.getMapContext().getLayers();
	FLyrVect actLayer = null;
	for (int i = 0; i < flayers.getActives().length; i++) {
	    if (!(flayers.getActives()[i] instanceof FLayers)) {
		actLayer = (FLyrVect) flayers.getActives()[i];
		viewer = new NavTable(actLayer);
		if (viewer.init()) {
		    PluginServices.getMDIManager().addCentredWindow(viewer);
		}
	    }
	}

	// TODO: throw a message on the else (when there's no data)
	// or something like that
    }

    public void initialize() {
	About about = (About) PluginServices.getExtension(About.class);
	FPanelAbout panelAbout = about.getAboutPanel();
	java.net.URL aboutURL = this.getClass().getResource("/about.htm");
	panelAbout.addAboutUrl("NavTable", aboutURL);

	// Entry at TOC contextual menu
	ExtensionPoints extensionPoints = ExtensionPointsSingleton
		.getInstance();
	extensionPoints.add("View_TocActions", "NavTable",
		new NavTableTocMenuEntry());

	// Creating config Dir
	File configDir;
	String configDirStr;
	try {
	    configDirStr = com.iver.andami.Launcher.getAppHomeDir()
		    + "NavTable";
	} catch (java.lang.NoSuchMethodError e) {
	    configDirStr = System.getProperty("user.home") + File.separator
		    + "gvSIG" + File.separator + "navTable";

	}
	configDir = new File(configDirStr);
	Preferences p = new Preferences(configDir);
    }

    public boolean isEnabled() {
	boolean enabled = false;
	int status = EditionUtilities.getEditionStatus();
	if ((status == EditionUtilities.EDITION_STATUS_ONE_VECTORIAL_LAYER_ACTIVE || status == EditionUtilities.EDITION_STATUS_ONE_VECTORIAL_LAYER_ACTIVE_AND_EDITABLE)
		|| (status == EditionUtilities.EDITION_STATUS_MULTIPLE_VECTORIAL_LAYER_ACTIVE)
		|| (status == EditionUtilities.EDITION_STATUS_MULTIPLE_VECTORIAL_LAYER_ACTIVE_AND_EDITABLE)) {
	    BaseView view = (BaseView) PluginServices.getMDIManager()
		    .getActiveWindow();
	    MapControl mapControl = view.getMapControl();
	    FLayers flayers = mapControl.getMapContext().getLayers();
	    int pos = flayers.getActives().length - 1;
	    FLayer actLayer = flayers.getActives()[pos];
	    if (actLayer instanceof FLyrVect) {
		enabled = true;
	    }
	}
	return enabled;
    }

    public boolean isVisible() {
	com.iver.andami.ui.mdiManager.IWindow f = PluginServices
		.getMDIManager().getActiveWindow();
	if (f == null) {
	    return false;
	}
	if (f instanceof BaseView) {
	    return true;
	}
	return false;
    }

}