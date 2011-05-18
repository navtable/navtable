/*
 * Copyright (c) 2010. Cartolab (Universidade da Coru�a)
 * 
 * This file is part of NavTable
 * 
 * NavTable is free software: you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or any later version.
 * 
 * NavTable is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with NavTable.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package es.udc.cartolab.gvsig.navtable;

import org.apache.log4j.Logger;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.cit.gvsig.fmap.core.IRow;
import com.iver.cit.gvsig.fmap.edition.AfterFieldEditEvent;
import com.iver.cit.gvsig.fmap.edition.AfterRowEditEvent;
import com.iver.cit.gvsig.fmap.edition.BeforeFieldEditEvent;
import com.iver.cit.gvsig.fmap.edition.BeforeRowEditEvent;
import com.iver.cit.gvsig.fmap.edition.EditionEvent;
import com.iver.cit.gvsig.fmap.edition.IEditableSource;
import com.iver.cit.gvsig.fmap.edition.IEditionListener;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.LayerEvent;
import com.iver.cit.gvsig.fmap.layers.LayerListener;

/**
 * This class handles data source edition changes for AbstractNavTable. It
 * listens for start/stop editing (editionChanged method) and alphanumeric
 * changes (afterRowEditEvent).
 * 
 * @author Javier Est�vez
 * 
 */
public class EditionListener implements LayerListener, IEditionListener {

    private AbstractNavTable nt;
    private IEditableSource source;
    protected static Logger logger = Logger.getLogger("NavTable");

    public EditionListener(AbstractNavTable nt, FLyrVect layer) {
	this(nt);
	if (layer.isEditing()) {
	    source = (IEditableSource) (layer).getSource();
	    source.addEditionListener(this);
	}
    }

    public EditionListener(AbstractNavTable nt) {
	this.nt = nt;
    }

    @Override
    public void processEvent(EditionEvent e) {
    }

    @Override
    public void beforeRowEditEvent(IRow feat, BeforeRowEditEvent e) {
    }

    @Override
    public void afterRowEditEvent(IRow feat, AfterRowEditEvent e) {
	if (nt.getPosition() == e.getNumRow()) {
	    nt.fillValues();
	}
    }

    @Override
    public void beforeFieldEditEvent(BeforeFieldEditEvent e) {
    }

    @Override
    public void afterFieldEditEvent(AfterFieldEditEvent e) {
    }

    @Override
    public void visibilityChanged(LayerEvent e) {
    }

    @Override
    public void activationChanged(LayerEvent e) {
    }

    @Override
    public void nameChanged(LayerEvent e) {
    }

    @Override
    public void editionChanged(LayerEvent e) {
	FLayer layer = e.getSource();
	if (layer instanceof FLyrVect) {
	    if (layer.isEditing()) {
		source = (IEditableSource) ((FLyrVect) layer).getSource();
		source.addEditionListener(this);
		try {
		    nt.reloadRecordset();
		} catch (ReadDriverException e2) {
		    logger.error(e2.getMessage(), e2);
		}
	    } else if (source != null) {
		source.removeEditionListener(this);
		source = null;
	    }
	}
    }

    @Override
    public void drawValueChanged(LayerEvent e) {
    }

}