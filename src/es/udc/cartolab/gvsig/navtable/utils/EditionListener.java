/*
 * Copyright (c) 2010. Cartolab (Universidade da Coruña)
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

package es.udc.cartolab.gvsig.navtable.utils;

import org.apache.log4j.Logger;
import org.gvsig.editing.EditingNotificationManager;
import org.gvsig.fmap.dal.DataStoreNotification;
import org.gvsig.fmap.dal.exception.DataException;
import org.gvsig.fmap.dal.feature.FeatureStore;
import org.gvsig.fmap.dal.feature.FeatureStoreNotification;
import org.gvsig.fmap.mapcontext.layers.FLayer;
import org.gvsig.fmap.mapcontext.layers.LayerEvent;
import org.gvsig.fmap.mapcontext.layers.LayerListener;
import org.gvsig.fmap.mapcontext.layers.vectorial.FLyrVect;
import org.gvsig.fmap.mapcontrol.MapControlLocator;
import org.gvsig.tools.observer.Observable;
import org.gvsig.tools.observer.Observer;

import es.icarto.gvsig.navtable.gvsig2.IEditableSource;
import es.icarto.gvsig.navtable.gvsig2.SelectableDataSource;
import es.udc.cartolab.gvsig.navtable.AbstractNavTable;

/**
 * This class handles data source edition changes for AbstractNavTable. It
 * listens for start/stop editing (editionChanged method) and alphanumeric
 * changes (afterRowEditEvent).
 * 
 * @author Javier Estévez
 * 
 */
public class EditionListener implements LayerListener, Observer {

    private AbstractNavTable nt;
	private FLyrVect layer;
    protected static Logger logger = Logger.getLogger("NavTable");

    public EditionListener(AbstractNavTable nt, FLyrVect layer) {
	this.nt = nt;
	this.layer = layer;
	if (layer != null && layer.isEditing()) {
		layer.getFeatureStore().addObserver(this);
	}
    }

    public EditionListener(AbstractNavTable nt) {
	this.nt = nt;
    }

    /**
     * Launch after start/stop edition on the layer
     */
    public void editionChanged(LayerEvent e) {
	FLayer layer = e.getSource();
	if (layer instanceof FLyrVect) {
		FeatureStore featureStore = ((FLyrVect) layer).getFeatureStore();
	    if (layer.isEditing()) {
			featureStore.addObserver(this);
	    } else if (featureStore != null) {
		featureStore.deleteObserver(this);
	    }
	    refresh();
	}
	nt.layerEvent(e);
    }

    private void refresh() {
	try {
	    nt.reloadRecordset();
	} catch (DataException error) {
	    logger.error(error.getMessage(), error);
	}
	nt.refreshGUI();
    }

	@Override
	public void update(Observable observable, Object notification) {
		if (notification instanceof FeatureStoreNotification) {
			FeatureStoreNotification not = (FeatureStoreNotification) notification;
			String type = not.getType();
			// AfterRowEditEvent
			if (FeatureStoreNotification.AFTER_UPDATE.equals(type) || FeatureStoreNotification.AFTER_DELETE.equals(type) || FeatureStoreNotification.AFTER_INSERT.equals(type)) {
				if (!nt.isSavingValues()) {
					long posOfFeature;
					try {
						posOfFeature = new SelectableDataSource(layer.getFeatureStore()).posOfFeature(not.getFeature());
						if (nt.getPosition() == posOfFeature) {
							nt.fillValues();
						}
					} catch (DataException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
			} else {
				// AfterFieldEditEvent
				if (FeatureStoreNotification.AFTER_UPDATE_TYPE.equals(type)) {
					refresh();
					nt.editionEvent(not);
				}
			}
		}

	 }

	@Override
	public void visibilityChanged(LayerEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void activationChanged(LayerEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void nameChanged(LayerEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void drawValueChanged(LayerEvent e) {
		// TODO Auto-generated method stub
		
	}
}
