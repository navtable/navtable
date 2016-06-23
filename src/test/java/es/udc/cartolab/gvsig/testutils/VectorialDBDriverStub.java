package es.udc.cartolab.gvsig.testutils;

import java.awt.geom.Rectangle2D;
import java.sql.SQLException;
import java.util.List;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.drivers.DBException;
import com.iver.cit.gvsig.fmap.drivers.DBLayerDefinition;
import com.iver.cit.gvsig.fmap.drivers.FeatureCollectionMemoryDriver;
import com.iver.cit.gvsig.fmap.drivers.IConnection;
import com.iver.cit.gvsig.fmap.drivers.IFeatureIterator;
import com.iver.cit.gvsig.fmap.drivers.IVectorialDatabaseDriver;
import com.iver.cit.gvsig.fmap.layers.XMLException;
import com.iver.utiles.XMLEntity;

/**
 * Uses FeatureCollectionMemoryDriver as base that provides a list of IFeatures
 * an implements IVectorialDatabaseDriver mostly to allow call start and stop
 * methods
 *
 */
public class VectorialDBDriverStub extends FeatureCollectionMemoryDriver
implements IVectorialDatabaseDriver {

    private final DBLayerDefinition definition;

    public VectorialDBDriverStub(String name, List<IFeature> features,
	    DBLayerDefinition definition) {
	super(name, features, definition);
	this.definition = definition;
    }

    @Override
    public IFeatureIterator getFeatureIterator(Rectangle2D r, String strEPSG)
	    throws ReadDriverException {
	return null;
    }

    @Override
    public IFeatureIterator getFeatureIterator(Rectangle2D r, String strEPSG,
	    String[] alphaNumericFieldsNeeded) throws ReadDriverException {
	return null;
    }

    @Override
    public String[] getFields() {
	return null;
    }

    @Override
    public String getWhereClause() {
	return null;
    }

    @Override
    public String getTableName() {
	return null;
    }

    @Override
    public void close() {
    }

    @Override
    public void open() {
    }

    @Override
    public int getRowIndexByFID(IFeature FID) {
	return 0;
    }

    @Override
    public String getGeometryField(String fieldName) {
	return null;
    }

    @Override
    public XMLEntity getXMLEntity() {
	return null;
    }

    @Override
    public void setXMLEntity(XMLEntity xml) throws XMLException {
    }

    @Override
    public DBLayerDefinition getLyrDef() {
	return definition;
    }

    @Override
    public void remove() {
    }

    @Override
    public void load() throws ReadDriverException {
    }

    @Override
    public IConnection getConnection() {
	return null;
    }

    @Override
    public String getConnectionString(String _host, String _port, String _db,
	    String _user, String _pw) {
	return null;
    }

    @Override
    public int getDefaultPort() {
	return 0;
    }

    @Override
    public String getConnectionStringBeginning() {
	return null;
    }

    @Override
    public void setData(IConnection conn, DBLayerDefinition lyrDef)
	    throws DBException {
    }

    @Override
    public String[] getAllFields(IConnection conn, String tableName)
	    throws DBException {
	return null;
    }

    @Override
    public String[] getAllFieldTypeNames(IConnection conn, String tableName)
	    throws DBException {
	return null;
    }

    @Override
    public String[] getIdFieldsCandidates(IConnection conn, String tableName)
	    throws DBException {
	return null;
    }

    @Override
    public String[] getGeometryFieldsCandidates(IConnection conn,
	    String tableName) throws DBException {
	return null;
    }

    @Override
    public void setWorkingArea(Rectangle2D _wa) {
    }

    @Override
    public String[] getTableNames(IConnection conex, String dbName)
	    throws DBException {
	return null;
    }

    @Override
    public Rectangle2D getWorkingArea() {
	return null;
    }

    @Override
    public boolean canRead(IConnection iconn, String tablename)
	    throws SQLException {
	return false;
    }

}
