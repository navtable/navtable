package es.icarto.gvsig.navtable.edition;

import java.text.ParseException;

import org.gvsig.app.ApplicationLocator;
import org.gvsig.app.project.documents.table.TableDocument;
import org.gvsig.editing.EditingNotification;
import org.gvsig.editing.EditingNotificationManager;
import org.gvsig.fmap.dal.exception.DataException;
import org.gvsig.fmap.dal.feature.EditableFeature;
import org.gvsig.fmap.dal.feature.Feature;
import org.gvsig.fmap.dal.feature.FeatureStore;
import org.gvsig.fmap.mapcontrol.MapControlLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.icarto.gvsig.commons.gvsig2.DefaultFeature;
import es.icarto.gvsig.commons.gvsig2.IEditableSource;
import es.icarto.gvsig.commons.gvsig2.SelectableDataSource;
import es.icarto.gvsig.commons.gvsig2.Value;
import es.udc.cartolab.gvsig.navtable.format.ValueFactoryNT;

public class TableEdition {
	
	private static final Logger logger = LoggerFactory
			.getLogger(TableEdition.class);

	/**
	 * Based on org.gvsig.app.extension.TableEditStartExtension
	 * Returns true is the table has been put correctly in edition mode
	 */
	public boolean startEditing(TableDocument doc) {
        try {
            EditingNotificationManager editingNotification = MapControlLocator.getEditingNotificationManager();
            EditingNotification notification = editingNotification.notifyObservers(
                    this,
                    EditingNotification.BEFORE_ENTER_EDITING_STORE,
                    doc,
                    doc.getStore());
            if( notification.isCanceled() ) {
                return false;
            }
            doc.getStore().edit(FeatureStore.MODE_FULLEDIT);
            ApplicationLocator.getManager().refreshMenusAndToolBars();
            editingNotification.notifyObservers(
                    this,
                    EditingNotification.AFTER_ENTER_EDITING_STORE,
                    doc,
                    doc.getStore());
        } catch (DataException e) {
            logger.warn("Problems starting table editing.",e);
            return false;
        }
	return true;
	}
	
    public void modifyValues(TableDocument doc, int rowPosition, int[] attIndexes, String[] attValues) {
    	try {
    		SelectableDataSource sds = new SelectableDataSource(doc.getStore());
    		EditableFeature f = sds.getFeature(rowPosition).getEditable();
    		for (int i=0; i< attIndexes.length; i++) {
    			String att = attValues[i];
    	    	int idx = attIndexes[i];
    	    	if (att == null || att.length() == 0) {
    	    	    f.set(idx, null);
    	    	} else {
    	    		int type = sds.getFieldType(idx);
    	    		Object value = ValueFactoryNT.createValueByType(att, type).getObjectValue();
    	    	    f.set(idx, value);
    	    	}
    		}
    		doc.getStore().update(f);
    	} catch (DataException e) {
    	    logger.error(e.getMessage(), e);
    	} catch (ParseException e) {
			logger.error(e.getMessage(), e);
		} 
    }
    
	/**
	 * Based on org.gvsig.app.extension.TableEditStopExtension
	 * Returns true is the table has been put correctly in NO edition mode
	 */
	public boolean stopEditing(TableDocument doc, boolean cancel) {
        EditingNotificationManager editingNotification = MapControlLocator.getEditingNotificationManager();
        EditingNotification notification = editingNotification.notifyObservers(
                this,
                EditingNotification.BEFORE_ENTER_EDITING_STORE,
                doc,
                doc.getStore());
        if( notification.isCanceled() ) {
            return false;
        }
        try {
			doStopEditing(doc, cancel);
		} catch (DataException e) {
			logger.error(e.getMessage(), e);
			return false;
		}
        ApplicationLocator.getManager().refreshMenusAndToolBars();
        editingNotification.notifyObservers(
                this,
                EditingNotification.AFTER_ENTER_EDITING_STORE,
                doc,
                doc.getStore());
        return true;
	}
	
	private void doStopEditing(TableDocument doc, boolean cancel) throws DataException {
		if (cancel) {
			doc.getStore().cancelEditing();			
		} else {
			doc.getStore().finishEditing();			
		}	
    }
}
