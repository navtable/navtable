package es.icarto.gvsig.navtable.edition;

import org.gvsig.app.ApplicationLocator;
import org.gvsig.app.project.documents.table.TableDocument;
import org.gvsig.fmap.dal.EditingNotification;
import org.gvsig.fmap.dal.EditingNotificationManager;
import org.gvsig.fmap.dal.exception.DataException;
import org.gvsig.fmap.dal.feature.EditableFeature;
import org.gvsig.fmap.dal.feature.Feature;
import org.gvsig.fmap.dal.feature.FeatureStore;
import org.gvsig.fmap.dal.feature.FeatureType;
import org.gvsig.fmap.dal.swing.DALSwingLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.icarto.gvsig.commons.gvsig2.Value;
import es.udc.cartolab.gvsig.navtable.format.ValueFactoryNT;

public class TableEdition {

	private static final Logger logger = LoggerFactory
			.getLogger(TableEdition.class);

	/**
	 * Based on org.gvsig.app.extension.TableEditStartExtension Returns true is
	 * the table has been put correctly in edition mode
	 */
	public boolean startEditing(TableDocument doc) {
		try {
			doStartEditing(doc);
		} catch (DataException e) {
			logger.warn("Problems starting table editing.", e);
			return false;
		}
		return true;
	}

	public void doStartEditing(TableDocument doc) throws DataException {
		EditingNotificationManager editingNotification = DALSwingLocator
				.getEditingNotificationManager();
		EditingNotification notification = editingNotification.notifyObservers(
				this, EditingNotification.BEFORE_ENTER_EDITING_STORE, doc,
				doc.getStore());
		if (notification.isCanceled()) {
			return;
		}
		doc.getStore().edit(FeatureStore.MODE_FULLEDIT);
		ApplicationLocator.getManager().refreshMenusAndToolBars();
		editingNotification.notifyObservers(this,
				EditingNotification.AFTER_ENTER_EDITING_STORE, doc,
				doc.getStore());
	}

	public Feature modifyValues(TableDocument doc, Feature feat,
			int[] attIndexes, String[] attValues) throws DataException {
		EditableFeature f = feat.getEditable();
		FeatureStore store = doc.getStore();
		setNewAttributes(f, attIndexes, attValues);
		store.update(f);
		return f;
	}

	private void setNewAttributes(EditableFeature f, int[] attIndexes,
			String[] attValues) {
		FeatureType featType = f.getType();
		for (int i = 0; i < attIndexes.length; i++) {
			String att = attValues[i];
			int idx = attIndexes[i];
			if (att == null || att.trim().length() == 0) {
				f.set(idx, null);
			} else {
				int type = featType.getAttributeDescriptor(idx).getType();
				try {
					Value value = ValueFactoryNT.createValueByType2(att, type);
					Object o = value.getObjectValue();
					f.set(idx, o);
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
	}

	/**
	 * Based on org.gvsig.app.extension.TableEditStopExtension Returns true is
	 * the table has been put correctly in NO edition mode
	 */
	public boolean stopEditing(TableDocument doc, boolean cancel) {
		EditingNotificationManager editingNotification = DALSwingLocator
				.getEditingNotificationManager();
		EditingNotification notification = editingNotification.notifyObservers(
				this, EditingNotification.BEFORE_ENTER_EDITING_STORE, doc,
				doc.getStore());
		if (notification.isCanceled()) {
			return false;
		}

		try {
			doStopEditing(doc, cancel);
		} catch (DataException e) {
			logger.error(e.getMessage(), e);
			return false;
		}

		ApplicationLocator.getManager().refreshMenusAndToolBars();
		editingNotification.notifyObservers(this,
				EditingNotification.AFTER_ENTER_EDITING_STORE, doc,
				doc.getStore());

		return true;
	}

	private void doStopEditing(TableDocument doc, boolean cancel)
			throws DataException {
		if (cancel) {
			doc.getStore().cancelEditing();
		} else {
			doc.getStore().finishEditing();
		}
	}
}
