package es.udc.cartolab.gvsig.navtable;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.hardcode.gdbms.engine.values.Value;
import com.hardcode.gdbms.engine.values.ValueFactory;
import com.iver.cit.gvsig.fmap.core.DefaultFeature;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.ShapeFactory;
import com.iver.cit.gvsig.fmap.drivers.FieldDescription;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;

import es.udc.cartolab.gvsig.navtable.ToggleEditing;
import es.udc.cartolab.gvsig.testutils.Drivers;
import es.udc.cartolab.gvsig.testutils.FieldDescriptionFactory;
import es.udc.cartolab.gvsig.testutils.SHPFactory;

public class ToggleEditingTest {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();
    private FLyrVect layer;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
	Drivers.initgvSIGDrivers(TestProperties.driversPath);
    }


    @Test
    public void writeStringInNumericField() throws Exception {
	File file = temp.newFile("layer.shp");
	
	FieldDescriptionFactory fdFactory = new FieldDescriptionFactory();
	fdFactory.addString("control");
	fdFactory.addDouble("doublefld");
	fdFactory.addString("stringfld");
	FieldDescription[] fieldsDesc = fdFactory.getFields();

	Value[] values = new Value[fieldsDesc.length];
	values[0] = ValueFactory.createValue("Works");
	values[1] = ValueFactory.createValue(1.2);
	values[2] = ValueFactory.createValue("string value");

	IGeometry geom = ShapeFactory.createPoint2D(0, 0);
	IFeature feat = new DefaultFeature(geom, values, "0");

	SHPFactory.createSHP(file, fieldsDesc, FShape.POINT,
		new IFeature[] { feat });
	layer = SHPFactory.getFLyrVectFromSHP(file);
	
	ToggleEditing te = new ToggleEditing();
	te.startEditing(layer);
	te.modifyValues(layer, 0, new int[] { 1, 2 },
		new String[] { "string in numeric field", "string in string field" });
	te.stopEditing(layer, false);

	Value[] actualAttributes = layer.getSource().getFeature(0).getAttributes();
	
	// control field is not modified or blanked
	assertEquals(values[0], actualAttributes[0]);
	
	//numeric field is not modified
	assertEquals(values[1], actualAttributes[1]);
	
	// rest of the fields are correctly modified
	assertEquals("string in string field", actualAttributes[2].toString());
	
    }
    
    

}
