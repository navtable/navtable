package es.icarto.gvsig.navtable.gvsig2;

public class StringValue extends Value {

	public StringValue(Object o) {
		super(o);
	}

	public void setValue(String s) {
		// TODO Auto-generated method stub
		
	}
	
	public String getStringValue(ValueWriter valueWriter) {
		return valueWriter.getStatementString((String) value, -1);
	}

}
