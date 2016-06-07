package es.icarto.gvsig.navtable.gvsig2;


public class BooleanValue extends Value {
	
	public BooleanValue(Object o) {
		super(o);
	}
	public boolean getValue() {
		return (boolean) value;
	}
	
	public String getStringValue(ValueWriter valueWriter) {
		return valueWriter.getStatementString((Boolean)value);
	}

}
