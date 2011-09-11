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
		if (selectedItems[0].isAvailable()) {
			return true;
		}
		return false;
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
