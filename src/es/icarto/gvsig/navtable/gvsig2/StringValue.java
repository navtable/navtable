package es.icarto.gvsig.navtable.gvsig2;

public class StringValue extends Value {

	public StringValue(Object o) {
		super(o);
	}
	
	public String getStringValue(ValueWriter valueWriter) {
		return valueWriter.getStatementString((String) value, -1);
	}

}
