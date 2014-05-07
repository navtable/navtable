package es.udc.cartolab.gvsig.testutils;

import com.iver.cit.gvsig.fmap.MapContext;
import com.iver.cit.gvsig.fmap.MapControl;
import com.iver.cit.gvsig.fmap.ViewPort;
import com.iver.cit.gvsig.fmap.layers.FLayer;

@SuppressWarnings("serial")
public class MapControlStub extends MapControl {

    public MapControlStub() {
	super();
	ViewPort vp = null;
	MapContext mapContext = new MapContext(vp);
	setMapContext(mapContext);
    }

    public void addLayer(FLayer layer) {
	getMapContext().getLayers().addLayer(layer);
    }
}
