package es.udc.cartolab.gvsig.navtable.dataaccess.layercontroller;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.cresques.cts.IProjection;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hardcode.driverManager.DriverLoadException;
import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.cit.gvsig.exceptions.layers.LoadLayerException;
import com.iver.cit.gvsig.exceptions.visitors.StopWriterVisitorException;
import com.iver.cit.gvsig.fmap.crs.CRSFactory;
import com.iver.cit.gvsig.fmap.drivers.VectorialFileDriver;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.LayerFactory;

import es.icarto.gvsig.commons.testutils.TestProperties;
import es.udc.cartolab.gvsig.navtable.dataacces.IController;
import es.udc.cartolab.gvsig.navtable.dataacces.LayerController;

public class LayerControllerUpdateTests {

    public static IProjection TEST_PROJECTION = CRSFactory.getCRS("EPSG:23030");

    @BeforeClass
    public static void loadDrivers() throws Exception {
	doSetup();
    }

    private static void doSetup() throws Exception {
	String fwAndamiDriverPath = TestProperties.driversPath;
	File baseDriversPath = new File(fwAndamiDriverPath);
	if (!baseDriversPath.exists()) {
	    throw new Exception("Can't find drivers path: "
		    + fwAndamiDriverPath);
	}

	LayerFactory.setDriversPath(baseDriversPath.getAbsolutePath());
	if (LayerFactory.getDM().getDriverNames().length < 1) {
	    throw new Exception("Can't find drivers in path: "
		    + fwAndamiDriverPath);
	}
    }

    @Test
    public void testUpdateTextFieldFromShapeFile() throws LoadLayerException,
	    ReadDriverException, DriverLoadException,
	    StopWriterVisitorException {
	FLyrVect layer = getFLyrVectFromFile();
	IController lc = new LayerController(layer);
	lc.read(0);
	final String expected = "test working";
	lc.setValue("f_text", expected);
	lc.update(0);
	lc.read(0);
	String actual = lc.getValue("f_text");
	assertEquals(expected, actual);
    }

    @Test
    public void testUpdateDoubleFieldFromShapeFile() throws LoadLayerException,
	    ReadDriverException, DriverLoadException,
	    StopWriterVisitorException {
	FLyrVect layer = getFLyrVectFromFile();
	IController lc = new LayerController(layer);
	lc.read(0);
	final String expected = "5,9";
	lc.setValue("f_double", expected);
	lc.update(0);
	lc.read(0);
	String actual = lc.getValue("f_double");
	assertEquals(expected, actual);
    }

    @Test
    public void testUpdateFloatFieldFromShapeFile() throws LoadLayerException,
	    ReadDriverException, DriverLoadException,
	    StopWriterVisitorException {
	FLyrVect layer = getFLyrVectFromFile();
	IController lc = new LayerController(layer);
	lc.read(0);
	final String expected = "666,333";
	lc.setValue("f_float", expected);
	lc.update(0);
	lc.read(0);
	String actual = lc.getValue("f_float");
	assertEquals(expected, actual);
    }

    @Test
    public void testUpdateDateFieldFromShapeFile() throws LoadLayerException,
	    ReadDriverException, DriverLoadException,
	    StopWriterVisitorException {
	FLyrVect layer = getFLyrVectFromFile();
	IController lc = new LayerController(layer);
	lc.read(0);
	final String expected = "27/02/2002";
	lc.setValue("f_date", expected);
	lc.update(0);
	lc.read(0);
	String actual = lc.getValue("f_date");
	assertEquals(expected, actual);
    }

    @Test
    public void testUpdateShortIntFieldFromShapeFile()
	    throws LoadLayerException, ReadDriverException,
	    DriverLoadException, StopWriterVisitorException {
	FLyrVect layer = getFLyrVectFromFile();
	IController lc = new LayerController(layer);
	lc.read(0);
	final String expected = "123";
	lc.setValue("f_int_shor", expected);
	lc.update(0);
	lc.read(0);
	String actual = lc.getValue("f_int_shor");
	assertEquals(expected, actual);
    }

    @Test
    public void testUpdateLongIntFieldFromShapeFile()
	    throws LoadLayerException, ReadDriverException,
	    DriverLoadException, StopWriterVisitorException {
	FLyrVect layer = getFLyrVectFromFile();
	IController lc = new LayerController(layer);
	lc.read(0);
	final String expected = "987";
	lc.setValue("f_int_long", expected);
	lc.update(0);
	lc.read(0);
	String actual = lc.getValue("f_int_long");
	assertEquals(expected, actual);
    }

    private FLyrVect getFLyrVectFromFile() throws DriverLoadException {
	File file = new File("data-test/", "test.shp");
	// fields and values:
	// f_text=test; f_double=2.4; f_float=2.9; f_int_shor=2; f_int_long=290;
	// f_date=1983-08-25;
	FLyrVect layer = (FLyrVect) LayerFactory.createLayer(
		"Countries",
		(VectorialFileDriver) LayerFactory.getDM().getDriver(
			"gvSIG shp driver"), file,
		CRSFactory.getCRS("EPSG:23030"));
	return layer;
    }

}
