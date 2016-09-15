package es.udc.cartolab.gvsig.navtable.dataacces;

import java.util.Date;
import java.util.List;

import org.cresques.cts.IProjection;
import org.gvsig.fmap.dal.exception.DataException;
import org.gvsig.fmap.dal.feature.EditableFeature;
import org.gvsig.fmap.dal.feature.Feature;
import org.gvsig.fmap.dal.feature.FeatureReference;
import org.gvsig.fmap.dal.feature.FeatureStore;
import org.gvsig.fmap.dal.feature.FeatureType;
import org.gvsig.fmap.geom.Geometry;
import org.gvsig.fmap.geom.primitive.Envelope;
import org.gvsig.timesupport.Instant;
import org.gvsig.timesupport.Interval;
import org.gvsig.tools.dynobject.DynObject;
import org.gvsig.tools.evaluator.EvaluatorData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.icarto.gvsig.navtable.navigation.NavigationHandler;

public class EmptyFeature implements Feature {
	private static final Logger logger = LoggerFactory
			.getLogger(NavigationHandler.class);

	private final FeatureType type;

	public EmptyFeature(FeatureStore store) {
		EditableFeature feat = null;
		try {
			feat = store.createNewFeature();
		} catch (DataException e) {

			logger.error(e.getMessage(), e);
		}
		if (feat != null) {
			type = feat.getType().getCopy();
		} else {
			type = null;
		}
	}

	private EmptyFeature(FeatureType type) {
		this.type = type.getCopy();
	}

	@Override
	public FeatureReference getReference() {
		return null;
	}

	@Override
	public FeatureType getType() {
		return type;
	}

	@Override
	public Feature getCopy() {
		return new EmptyFeature(type);
	}

	@Override
	public void validate(int mode) throws DataException {
	}

	@Override
	public EditableFeature getEditable() {
		return null;
	}

	@Override
	public Object get(String name) {
		return null;
	}

	@Override
	public Object get(int index) {
		return null;
	}

	@Override
	public int getInt(String name) {
		return 0;
	}

	@Override
	public int getInt(int index) {
		return 0;
	}

	@Override
	public boolean getBoolean(String name) {
		return false;
	}

	@Override
	public boolean getBoolean(int index) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public long getLong(String name) {
		return 0;
	}

	@Override
	public long getLong(int index) {
		return 0;
	}

	@Override
	public float getFloat(String name) {
		return 0;
	}

	@Override
	public float getFloat(int index) {
		return 0;
	}

	@Override
	public double getDouble(String name) {
		return 0;
	}

	@Override
	public double getDouble(int index) {
		return 0;
	}

	@Override
	public Date getDate(String name) {
		return null;
	}

	@Override
	public Date getDate(int index) {
		return null;
	}

	@Override
	public String getString(String name) {
		return null;
	}

	@Override
	public String getString(int index) {
		return null;
	}

	@Override
	public byte getByte(String name) {
		return 0;
	}

	@Override
	public byte getByte(int index) {
		return 0;
	}

	@Override
	public Geometry getGeometry(String name) {
		return null;
	}

	@Override
	public Geometry getGeometry(int index) {
		return null;
	}

	@Override
	public Object[] getArray(String name) {
		return null;
	}

	@Override
	public Object[] getArray(int index) {
		return null;
	}

	@Override
	public Feature getFeature(String name) {
		return null;
	}

	@Override
	public Feature getFeature(int index) {
		return null;
	}

	@Override
	public Envelope getDefaultEnvelope() {
		return null;
	}

	@Override
	public Geometry getDefaultGeometry() {
		return null;
	}

	@Override
	public List getGeometries() {
		return null;
	}

	@Override
	public IProjection getDefaultSRS() {
		return type.getDefaultSRS();
	}

	@Override
	public List getSRSs() {
		return null;
	}

	@Override
	public Instant getInstant(int index) {
		return null;
	}

	@Override
	public Instant getInstant(String name) {
		return null;
	}

	@Override
	public Interval getInterval(int index) {
		return null;
	}

	@Override
	public Interval getInterval(String name) {
		return null;
	}

	@Override
	public DynObject getAsDynObject() {
		return null;
	}

	@Override
	public EvaluatorData getEvaluatorData() {
		return null;
	}

	// @Override
	// public FeatureStore getStore() {
	// return null;
	// }

}
