package es.udc.cartolab.gvsig.navtable.dataacces;

import java.math.BigDecimal;
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
import org.gvsig.tools.dynobject.DynObject;
import org.gvsig.tools.evaluator.EvaluatorData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.icarto.gvsig.navtable.navigation.NavigationHandler;

public class EmptyFeature implements Feature {
	private static final Logger logger = LoggerFactory.getLogger(NavigationHandler.class);

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
	public DynObject getAsDynObject() {
		return null;
	}

	@Override
	public EvaluatorData getEvaluatorData() {
		return null;
	}

	@Override
	public FeatureStore getStore() {
		return null;
	}

	@Override
	public Object getFromProfile(int index) {
		return null;
	}

	@Override
	public Object getFromProfile(String name) {
		return null;
	}

	@Override
	public int size() {
		return 0;
	}

	@Override
	public List<String> getKeys() {
		return null;
	}

	@Override
	public Object getOrDefault(String name, Object defaultValue) {
		return null;
	}

	@Override
	public String getStringOrDefault(String name, String defaultValue) {
		return null;
	}

	@Override
	public int getIntOrDefault(String name, int defaultValue) {
		return 0;
	}

	@Override
	public long getLongOrDefault(String name, long defaultValue) {
		return 0;
	}

	@Override
	public float getFloatOrDefault(String name, float defaultValue) {
		return 0;
	}

	@Override
	public double getDoubleOrDefault(String name, double defaultValue) {
		return 0;
	}

	@Override
	public BigDecimal getDecimalOrDefault(String name, BigDecimal defaultValue) {
		return null;
	}

	@Override
	public Date getDateOrDefault(String name, Date defaultValue) {
		return null;
	}

	@Override
	public Object getOrDefault(int index, Object defaultValue) {
		return null;
	}

	@Override
	public String getStringOrDefault(int index, String defaultValue) {
		return null;
	}

	@Override
	public int getIntOrDefault(int index, int defaultValue) {
		return 0;
	}

	@Override
	public long getLongOrDefault(int index, long defaultValue) {
		return 0;
	}

	@Override
	public float getFloatOrDefault(int index, float defaultValue) {
		return 0;
	}

	@Override
	public double getDoubleOrDefault(int index, double defaultValue) {
		return 0;
	}

	@Override
	public BigDecimal getDecimalOrDefault(int index, BigDecimal defaultValue) {
		return null;
	}

	@Override
	public Date getDateOrDefault(int index, Date defaultValue) {
		return null;
	}

	@Override
	public boolean isNull(int index) {
		return false;
	}

	@Override
	public boolean isNull(String name) {
		return false;
	}

	@Override
	public BigDecimal getDecimal(String name) {
		return null;
	}

	@Override
	public BigDecimal getDecimal(int index) {
		return null;
	}

	@Override
	public Date getTime(String name) {
		return null;
	}

	@Override
	public Date getTime(int index) {
		return null;
	}

	@Override
	public Date getTimestamp(String name) {
		return null;
	}

	@Override
	public Date getTimestamp(int index) {
		return null;
	}

	@Override
	public byte[] getByteArray(String name) {
		return null;
	}

	@Override
	public byte[] getByteArray(int index) {
		return null;
	}

	@Override
	public String getLabelOfValue(String name) {
		return null;
	}

	@Override
	public Object getExtraValue(int index) {
		return null;
	}

	@Override
	public Object getExtraValue(String name) {
		return null;
	}

	@Override
	public boolean hasExtraValue(String name) {
		return false;
	}

	@Override
	public void setExtraValue(String name, Object value) {

	}

	@Override
	public boolean hasValue(String name) {
		return false;
	}

}
