package es.icarto.gvsig.navtable.gvsig2;

import java.sql.Time;

public class TimeValue extends Value {

	public TimeValue(Object t) {
		super(t);
	}

	public void setValue(Time t) {
		// TODO Auto-generated method stub
		
	}
	
	public String getStringValue(ValueWriter valueWriter) {
		return valueWriter.getStatementString((Time) value);
	}

}
