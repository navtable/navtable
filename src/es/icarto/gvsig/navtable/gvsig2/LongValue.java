package es.icarto.gvsig.navtable.gvsig2;

public class LongValue extends NumericValue {

	public LongValue(Object o) {
		super(o);
	}
	
	public String getStringValue(ValueWriter valueWriter) {
		return valueWriter.getStatementString((Long) value);
	}

}
