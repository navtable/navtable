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
