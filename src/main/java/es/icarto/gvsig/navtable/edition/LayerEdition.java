package es.icarto.gvsig.navtable.edition;

import org.gvsig.andami.PluginServices;
import org.gvsig.andami.ui.mdiManager.IWindow;
import org.gvsig.app.project.documents.view.gui.IView;
import org.gvsig.fmap.dal.exception.DataException;
import org.gvsig.fmap.dal.feature.EditableFeature;
import org.gvsig.fmap.dal.feature.Feature;
import org.gvsig.fmap.dal.feature.FeatureStore;
import org.gvsig.fmap.dal.feature.FeatureType;
import org.gvsig.fmap.mapcontext.layers.FLayer;
import org.gvsig.fmap.mapcontext.layers.vectorial.FLyrVect;
import org.gvsig.fmap.mapcontrol.MapControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.udc.cartolab.gvsig.navtable.format.ValueFactoryNT;

public class LayerEdition {

	private static final Logger logger = LoggerFactory
			.getLogger(LayerEdition.class);

	public boolean startEditing(FLyrVect layer) {
		IView view = getViewFromLayer(layer);
		assertLayerIsEditable(view, layer);
		try {
			layer.getFeatureStore().edit();
		} catch (DataException e) {
			logger.error(e.getMessage(), e);
			return false;
		}
		return true;
	}

	private void assertLayerIsEditable(IView view, FLyrVect layer) {
		if (view == null) {
			throw new RuntimeException("Layer is not editable");
		}
		if (!layer.isAvailable()) {
			throw new RuntimeException("Layer is not editable");
		}
		if (layer.isEditing()) {
			throw new RuntimeException("Layer is not editable");
		}
		if (!layer.getFeatureStore().getTransforms().isEmpty()) {
			throw new RuntimeException("Layer is not editable");
		}
		MapControl mapControl = view.getMapControl();
		if (!mapControl.getProjection().equals(layer.getProjection())) {
			throw new RuntimeException("Layer is not editable");
		}
		if (!layer.getFeatureStore().allowWrite()) {
			throw new RuntimeException("Layer is not editable");
		}
	}

	public boolean stopEditing(FLyrVect layer, boolean discardChanges) {
		if (!layer.isEditing()) {
			throw new RuntimeException("Layer is not in edition mode");
		}
		try {
			if (discardChanges) {
				layer.getFeatureStore().cancelEditing();
			} else {
				layer.getFeatureStore().finishEditing();
			}
		} catch (DataException e) {
			logger.error(e.getMessage(), e);
			return false;
		}
		return true;
	}

	public Feature modifyValues(FLyrVect layer, Feature feat, int[] attIndexes,
			String[] attValues) throws DataException {
		FeatureStore store = layer.getFeatureStore();
		EditableFeature f = feat.getEditable();
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
				Object value;
				try {
					value = ValueFactoryNT.createValueByType2(att, type)
							.getObjectValue();
					f.set(idx, value);
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
	}

	public void deleteRow(FLyrVect layer, Feature feat) {
		FeatureStore store = layer.getFeatureStore();
		try {
			store.delete(feat);
		} catch (DataException e) {
			logger.error(e.getMessage(), e);
		}
	}

	private IView getViewFromLayer(FLayer layer) {
		// TODO: see how drop this IWindow dependence
		IWindow[] views = PluginServices.getMDIManager().getAllWindows();
		for (int j = 0; j < views.length; j++) {
			if (views[j] instanceof IView) {
				IView view = (IView) views[j];
				return view;
			}
		}
		return null;
	}
}
