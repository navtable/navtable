package es.udc.cartolab.gvsig.navtable.format;

import java.sql.Time;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import es.icarto.gvsig.commons.gvsig2.ValueWriter;

/**
 * Class to manage how the strings are formatted to display them, taking into
 * account the formats declared in NavTableFormats.
 */
public class ValueFormatNT implements ValueWriter {

	private NumberFormat doubleFormat;
	private NumberFormat integerFormat;
	private SimpleDateFormat dateFormat;
	private SimpleDateFormat timeFormat;

	public ValueFormatNT() {
		doubleFormat = DoubleFormatNT.getDisplayingFormat();
		integerFormat = IntegerFormatNT.getDisplayingFormat();
		dateFormat = DateFormatNT.getDateFormat();
		timeFormat = new SimpleDateFormat("HH:mm:ss");
	}

	@Override
	public String getStatementString(long i) {
		return Long.toString(i);
	}

	@Override
	public String getStatementString(int i, int sqlType) {
		return integerFormat.format(i);
	}

	@Override
	public String getStatementString(double d, int sqlType) {
		return doubleFormat.format(d);
	}

	@Override
	public String getStatementString(String str, int sqlType) {
		return str;
	}

	@Override
	public String getStatementString(Date d) {
		return dateFormat.format(d);
	}

	@Override
	public String getStatementString(Time t) {
		return timeFormat.format(t);
	}

	@Override
	public String getStatementString(Timestamp ts) {
		return ts.toString();
	}

	@Override
	public String getStatementString(byte[] binary) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < binary.length; i++) {
			int byte_ = binary[i];
			if (byte_ < 0) {
				byte_ = byte_ + 256;
			}
			String b = Integer.toHexString(byte_);
			if (b.length() == 1) {
				sb.append("0").append(b);
			} else {
				sb.append(b);
			}

		}
		return sb.toString();
	}

	@Override
	public String getStatementString(boolean b) {
		return Boolean.toString(b);
	}

	@Override
	public String getNullStatementString() {
		return "";
	}

}
