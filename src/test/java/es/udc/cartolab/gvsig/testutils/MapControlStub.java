package es.udc.cartolab.gvsig.testutils;

import org.gvsig.fmap.mapcontext.MapContext;
import org.gvsig.fmap.mapcontext.ViewPort;
import org.gvsig.fmap.mapcontext.layers.FLayer;
import org.gvsig.fmap.mapcontrol.MapControl;


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
