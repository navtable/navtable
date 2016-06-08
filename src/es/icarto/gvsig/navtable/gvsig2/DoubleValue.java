package es.icarto.gvsig.navtable.gvsig2;


public class DoubleValue extends NumericValue {

	public DoubleValue(Object o) {
		super(o);
	}
	
	public String getStringValue(ValueWriter valueWriter) {
		return valueWriter.getStatementString((Double) value, -1);
	}

}
