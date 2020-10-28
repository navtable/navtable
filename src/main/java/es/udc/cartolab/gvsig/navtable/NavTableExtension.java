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
import org.gvsig.andami.plugins.Extension;
import org.gvsig.andami.preferences.IPreference;
import org.gvsig.andami.preferences.IPreferenceExtension;
import org.gvsig.andami.ui.mdiManager.IWindow;
import org.gvsig.andami.ui.mdiManager.MDIManagerFactory;
import org.gvsig.app.ApplicationLocator;
import org.gvsig.app.ApplicationManager;
import org.gvsig.app.project.ProjectManager;
import org.gvsig.app.project.documents.table.gui.FeatureTableDocumentPanel;
import org.gvsig.app.project.documents.table.gui.toc.ShowAttributesTableTocMenuEntry;
import org.gvsig.app.project.documents.view.ViewManager;
import org.gvsig.app.project.documents.view.gui.IView;
import org.gvsig.app.project.documents.view.toc.AbstractTocContextMenuAction;
import org.gvsig.fmap.mapcontext.layers.FLayer;
import org.gvsig.fmap.mapcontext.layers.vectorial.FLyrVect;
import org.gvsig.fmap.mapcontext.layers.vectorial.VectorLayer;
import org.gvsig.utils.extensionPointsOld.ExtensionPoints;
import org.gvsig.utils.extensionPointsOld.ExtensionPointsSingleton;

import es.udc.cartolab.gvsig.navtable.contextualmenu.FiltersAddon;
import es.udc.cartolab.gvsig.navtable.contextualmenu.INavTableContextMenu;
import es.udc.cartolab.gvsig.navtable.contextualmenu.SorterAddon;
import es.udc.cartolab.gvsig.navtable.preferences.NavTablePreferencesPage;
import es.udc.cartolab.gvsig.navtable.preferences.Preferences;

public class NavTableExtension extends Extension implements
IPreferenceExtension {

	private IPreference[] preferencesPage;

	@Override
	public void execute(String actionCommand) {
		openNavtable();
	}

	private void openNavtable() {
		IWindow iWindow = MDIManagerFactory.getManager().getActiveWindow();
		if (iWindow instanceof IView) {
			for (FLyrVect vectorialLyr : getActiveVectorialLayersOnTheActiveWindow()) {
				NavTable navtable = new NavTable(vectorialLyr);
				if (navtable.init()) {
					MDIManagerFactory.getManager().addCentredWindow(navtable);
				}
			}
		} else if (isActiveWindowAttTableFromLayer()) {
			VectorLayer data = ((FeatureTableDocumentPanel) iWindow).getModel()
					.getAssociatedLayer();
			if (data instanceof FLyrVect) {
				NavTable nt = new NavTable((FLyrVect) data);
				if (nt.init()) {
					MDIManagerFactory.getManager().addCentredWindow(nt);
				}
			}
		}
	}

	@Override
	public void initialize() {
		ApplicationManager application = ApplicationLocator.getManager();
		AboutManager about = application.getAbout();
		about.addDeveloper("NavTable",
				getClass().getClassLoader().getResource("/about.htm"), 1);
		String id = this.getClass().getName();
		IconThemeHelper.registerIcon("action", id, this);
		initTOCMenuEntry();
		// Entry at TOC contextual menu
		ExtensionPoints extensionPoints = ExtensionPointsSingleton
				.getInstance();
		// Add NavTable "official" context menu addons to the extension point
		INavTableContextMenu filtersAddon = new FiltersAddon();
		extensionPoints.add(AbstractNavTable.NAVTABLE_CONTEXT_MENU,
				filtersAddon.getName(), filtersAddon);
		INavTableContextMenu sorterAddon = new SorterAddon();
		extensionPoints.add(AbstractNavTable.NAVTABLE_CONTEXT_MENU,
				sorterAddon.getName(), sorterAddon);

		// Creating config Dir
		String configDirStr = Launcher.getAppHomeDir() + File.separator
				+ "NavTable";
		File configDir = new File(configDirStr);
		Preferences p = new Preferences(configDir);
	}

	private void initTOCMenuEntry() {
		ProjectManager projectManager = ApplicationLocator.getProjectManager();
		ViewManager viewManager = (ViewManager) projectManager
				.getDocumentManager(ViewManager.TYPENAME);
		AbstractTocContextMenuAction tableMenuEntry = new ShowAttributesTableTocMenuEntry();
		String group = tableMenuEntry.getGroup();
		int groupOrder = tableMenuEntry.getGroupOrder();
		viewManager.addTOCContextAction("navtable", group, groupOrder, 1000);
	}

	@Override
	public boolean isVisible() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return enableNavtable();
	}

	protected boolean enableNavtable() {
		return !getActiveVectorialLayersOnTheActiveWindow().isEmpty()
				|| isActiveWindowAttTableFromLayer();
	}

	private boolean isActiveWindowAttTableFromLayer() {
		IWindow w = MDIManagerFactory.getManager().getActiveWindow();
		if (!(w instanceof FeatureTableDocumentPanel)) {
			return false;
		}
		return ((FeatureTableDocumentPanel) w).getModel().getAssociatedLayer() instanceof FLyrVect;
	}

	private ArrayList<FLyrVect> getActiveVectorialLayersOnTheActiveWindow() {
		IWindow iWindow = MDIManagerFactory.getManager().getActiveWindow();
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

	@Override
	public IPreference[] getPreferencesPages() {
		if (preferencesPage == null) {
			preferencesPage = new IPreference[] { new NavTablePreferencesPage() };
		}
		return preferencesPage;
	}
}