package es.udc.cartolab.gvsig.testutils;

import java.util.Collections;

import com.iver.cit.gvsig.fmap.drivers.FeatureCollectionMemoryDriver;
import com.iver.cit.gvsig.fmap.drivers.LayerDefinition;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.VectorialDefaultAdapter;


public class FLyrVectStub extends FLyrVect {

    public FLyrVectStub(String layerName) {
	super();
	setName(layerName);
	final VectorialDefaultAdapter rv = new VectorialDefaultAdapter();
	FieldDescriptionFactory fieldDescriptionFactory = new FieldDescriptionFactory();
	fieldDescriptionFactory.addInteger("fid");
	LayerDefinition layerDefinition = new LayerDefinition();
	layerDefinition.setFieldsDesc(fieldDescriptionFactory.getFields());
	FeatureCollectionMemoryDriver fcmd = new FeatureCollectionMemoryDriver("foo", Collections.EMPTY_LIST, layerDefinition);
	rv.setDriver(fcmd);
	setSource(rv);
    }
}
