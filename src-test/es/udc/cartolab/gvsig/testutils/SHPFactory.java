package es.udc.cartolab.gvsig.testutils;

import java.io.File;

import com.hardcode.driverManager.DriverLoadException;
import com.hardcode.gdbms.driver.exceptions.InitializeWriterException;
import com.iver.cit.gvsig.exceptions.visitors.ProcessWriterVisitorException;
import com.iver.cit.gvsig.exceptions.visitors.StartWriterVisitorException;
import com.iver.cit.gvsig.exceptions.visitors.StopWriterVisitorException;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.crs.CRSFactory;
import com.iver.cit.gvsig.fmap.drivers.FieldDescription;
import com.iver.cit.gvsig.fmap.drivers.SHPLayerDefinition;
import com.iver.cit.gvsig.fmap.drivers.VectorialFileDriver;
import com.iver.cit.gvsig.fmap.edition.DefaultRowEdited;
import com.iver.cit.gvsig.fmap.edition.IRowEdited;
import com.iver.cit.gvsig.fmap.edition.writers.shp.ShpWriter;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.LayerFactory;

public class SHPFactory {

    public static void createSHP(File file, FieldDescription[] fieldsDesc,
	    int geometryType, IFeature[] features) throws DriverLoadException, InitializeWriterException, StartWriterVisitorException, ProcessWriterVisitorException, StopWriterVisitorException {

	SHPLayerDefinition lyrDef = new SHPLayerDefinition();
	lyrDef.setFieldsDesc(fieldsDesc);
	lyrDef.setFile(file);
	lyrDef.setName(FileNameUtils.removeExtension(file.getName()));
	lyrDef.setShapeType(geometryType);
	ShpWriter writer = (ShpWriter) LayerFactory.getWM().getWriter(
		"Shape Writer");
	// String charSetName = prefs.get("dbf_encoding", DbaseFile
	// .getDefaultCharset().toString());
	// writer.loadDbfEncoding(newFile.getAbsolutePath(),
	// Charset.forName(charSetName));
	// writer.setCharset(Charset.forName(charSetName));

	// if (writer instanceof ShpWriter) {
	// String charSetName = prefs.get("dbf_encoding", DbaseFile
	// .getDefaultCharset().toString());
	// if (lyrVect.getSource() instanceof VectorialFileAdapter) {
	// ((ShpWriter) writer).loadDbfEncoding(
	// ((VectorialFileAdapter) lyrVect.getSource())
	// .getFile().getAbsolutePath(), Charset
	// .forName(charSetName));
	// } else {
	// Object s = lyrVect.getProperty("DBFFile");
	// if (s != null && s instanceof String) {
	// ((ShpWriter) writer).loadDbfEncoding((String) s,
	// Charset.forName(charSetName));
	// }
	// }
	// }
	writer.setFile(file);
	writer.initialize(lyrDef);
	writer.preProcess();

	for (int i = 0; i < features.length; i++) {
	    DefaultRowEdited row = new DefaultRowEdited(features[i],
		    IRowEdited.STATUS_ADDED, i);
	    writer.process(row);
	}

	writer.postProcess();
    }

    public static FLyrVect getFLyrVectFromSHP(File file)
	    throws DriverLoadException {
	FLyrVect layer = (FLyrVect) LayerFactory.createLayer(
		FileNameUtils.removeExtension(file.getName()),
		(VectorialFileDriver) LayerFactory.getDM().getDriver(
			"gvSIG shp driver"), file,
		CRSFactory.getCRS("EPSG:23039"));
	return layer;
    }
}
