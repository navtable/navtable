package es.icarto.gvsig.navtable.gvsig2;

import java.sql.Date;


public class DateValue extends Value {
	
	public DateValue(Object o) {
		super(o);
	}

	public Date getValue() {
        return (Date) value;
    }

	public void setValue(Date d) {
		// TODO Auto-generated method stub
		
	}
	
	public String getStringValue(ValueWriter valueWriter) {
		return valueWriter.getStatementString((Date) value);
	}

}
