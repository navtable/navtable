package es.icarto.gvsig.navtable.gvsig2;

import java.sql.Timestamp;

public class TimestampValue extends Value {

	public TimestampValue(Object t) {
		super(t);
	}

	public void setValue(Timestamp t) {
		// TODO Auto-generated method stub
		
	}
	
	public String getStringValue(ValueWriter valueWriter) {
		return valueWriter.getStatementString((Timestamp) value);
	}

}
