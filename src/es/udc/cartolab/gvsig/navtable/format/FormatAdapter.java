package es.udc.cartolab.gvsig.navtable.format;

import java.sql.Types;
import java.text.NumberFormat;
import java.text.ParseException;

/**
 * This class is responsible for adapting a value with the format displayed in
 * NavTable to a value with the format used by gvSIG.
 *
 */
public class FormatAdapter {

    public static String toGvSIGString(int type, String value) {
	String gvSIGValue = null;
	try {
	    switch (type) {
	    case Types.INTEGER:
	    case Types.BIGINT:
	    case Types.SMALLINT:
		gvSIGValue = toGvSIGInteger(value);
		break;
	    case Types.DOUBLE:
	    case Types.DECIMAL:
	    case Types.NUMERIC:
	    case Types.FLOAT:
	    case Types.REAL:
		gvSIGValue = toGvSIGDouble(value);
		break;
	    default:
		gvSIGValue = value;
	    }
	} catch (ParseException e) {
	    gvSIGValue = value;
	}
	return gvSIGValue;
    }

    public static String toNavTableString(int type, String value) {
	String navTableValue = null;
	try {
	    switch (type) {
	    case Types.INTEGER:
	    case Types.BIGINT:
	    case Types.SMALLINT:
		navTableValue = toNavTableInteger(value);
		break;
	    case Types.DOUBLE:
	    case Types.DECIMAL:
	    case Types.NUMERIC:
	    case Types.FLOAT:
	    case Types.REAL:
		navTableValue = toNavTableDouble(value);
		break;
	    default:
		navTableValue = value;
	    }
	} catch (NumberFormatException e) {
	    navTableValue = value;
	}
	return navTableValue;
    }

    private static String toGvSIGDouble(String doubleString)
	    throws ParseException {
	NumberFormat doubleFormat = DoubleFormatNT.getDisplayingFormat();
	double d = doubleFormat.parse(doubleString).doubleValue();
	ValueWriterImpl v = new ValueWriterImpl();
	return v.getStatementString(d, Types.DOUBLE);
    }

    private static String toGvSIGInteger(String value) throws ParseException {
	NumberFormat integerFormat = IntegerFormatNT.getDisplayingFormat();
	int i = integerFormat.parse(value).intValue();
	ValueWriterImpl v = new ValueWriterImpl();
	return v.getStatementString(i, Types.INTEGER);
    }

    private static String toNavTableDouble(String value)
	    throws NumberFormatException {
	double d = Double.parseDouble(value);
	NumberFormat doubleFormat = DoubleFormatNT.getDisplayingFormat();
	return doubleFormat.format(d);
    }

    private static String toNavTableInteger(String value)
	    throws NumberFormatException {
	int i = Integer.parseInt(value);
	NumberFormat integerFormat = IntegerFormatNT.getDisplayingFormat();
	return integerFormat.format(i);
    }

}
