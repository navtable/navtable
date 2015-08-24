package es.udc.cartolab.gvsig.testutils;

import java.util.Collections;
import java.util.List;

import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.drivers.DBLayerDefinition;
import com.iver.cit.gvsig.fmap.drivers.VectorialDriver;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.VectorialAdapter;
import com.iver.cit.gvsig.fmap.layers.VectorialDBAdapter;

import es.icarto.gvsig.commons.datasources.FieldDescriptionFactory;

public class FLyrVectDBStub extends FLyrVect {

    public FLyrVectDBStub(String layerName) {
	super();
	setName(layerName);
	final VectorialAdapter source = new VectorialDBAdapter();
	FieldDescriptionFactory fieldDescriptionFactory = new FieldDescriptionFactory();
	fieldDescriptionFactory.addInteger("fid");
	DBLayerDefinition layerDefinition = new DBLayerDefinition();
	layerDefinition.setFieldsDesc(fieldDescriptionFactory.getFields());
	VectorialDriver driver = new VectorialDBDriverStub("foo",
		Collections.<IFeature> emptyList(), layerDefinition);
	source.setDriver(driver);
	setSource(source);
    }

    public void setData(List<IFeature> featList, DBLayerDefinition lyrDef) {
	final VectorialAdapter source = new VectorialDBAdapter();
	VectorialDriver driver = new VectorialDBDriverStub("foo", featList,
		lyrDef);
	source.setDriver(driver);
	setSource(source);
    }
}
