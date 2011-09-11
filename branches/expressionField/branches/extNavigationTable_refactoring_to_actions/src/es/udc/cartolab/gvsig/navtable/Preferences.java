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

public class Preferences {

    private static File configDir;
    private static File aliasDir;

    public Preferences(File configDir) {
	Preferences.configDir = configDir;
	if (!configDir.exists()) {
	    configDir.mkdir();
	    createAliasDir();
	}
    }

    public static File getConfigDir() {
	return configDir;
    }

    public static String getAliasDir() {
	return configDir.getAbsolutePath() + File.separator + "alias";
    }

    private void createAliasDir() {
	aliasDir = new File(configDir.getAbsolutePath() + File.separator
		+ "alias");
	aliasDir.mkdir();
    }

}
