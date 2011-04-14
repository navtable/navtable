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

import javax.swing.table.DefaultTableModel;

/**
 * The model of the table to be shown. It makes non-editable the first row
 * cells.
 * 
 * @author Pablo Sanxiao
 * 
 */
class NavTableModel extends DefaultTableModel {

    @Override
    public boolean isCellEditable(int row, int col) {
	if (col == 1) {
	    return true;
	} else {
	    return false;
	}
    }

}
