package es.icarto.gvsig.navtable.gvsig2;

public class IntValue extends NumericValue {

	public IntValue(Object o) {
		super(o);
	}
	
	public String getStringValue(ValueWriter valueWriter) {
		return valueWriter.getStatementString((Integer) value, -1); 
	}

}
