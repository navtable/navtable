package es.udc.cartolab.gvsig.testutils;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import com.iver.cit.gvsig.fmap.drivers.FieldDescription;

public class FieldDescriptionFactory {

    private int stringLength = 100;
    private int numericLength = 20;
    private int decimalCount = 6;

    private List<FieldDescription> fields = new ArrayList<FieldDescription>();

    public void setDefaultStringLength(int defaultStringLength) {
	this.stringLength = defaultStringLength;
    }

    public void setDefaultNumericLength(int defaultNumericLength) {
	this.numericLength = defaultNumericLength;
    }

    public void setDefaultDecimalCount(int defaultDecimalCount) {
	this.decimalCount = defaultDecimalCount;
    }

    public void addInteger(String name) {
	FieldDescription fd = getInteger(name);
	fields.add(fd);
    }

    public FieldDescription getInteger(String name) {
	FieldDescription fd = new FieldDescription();
	fd.setFieldName(name);
	fd.setFieldLength(numericLength);
	fd.setFieldDecimalCount(0);
	fd.setFieldType(Types.INTEGER);
	return fd;
    }

    public void addDouble(String name) {
	FieldDescription fd = getDouble(name);
	fields.add(fd);
    }

    public FieldDescription getDouble(String name) {
	FieldDescription fd = new FieldDescription();
	fd.setFieldName(name);
	fd.setFieldLength(numericLength);
	fd.setFieldDecimalCount(decimalCount);
	fd.setFieldType(Types.DOUBLE);
	return fd;
    }

    public void addString(String name) {
	FieldDescription fd = getString(name);
	fields.add(fd);
    }

    public FieldDescription getString(String name) {
	FieldDescription fd = new FieldDescription();
	fd.setFieldName(name);
	fd.setFieldLength(stringLength);
	fd.setFieldDecimalCount(0);
	fd.setFieldType(Types.VARCHAR);
	return fd;
    }

    public FieldDescription[] getFields() {
	return fields.toArray(new FieldDescription[0]);
    }

}
