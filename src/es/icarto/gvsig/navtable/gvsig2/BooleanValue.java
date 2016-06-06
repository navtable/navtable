package es.icarto.gvsig.navtable.gvsig2;


public class BooleanValue extends Value {
	
	public BooleanValue(Object o) {
		super(o);
	}
	public boolean getValue() {
		return false;
	}

	public void setValue(boolean b) {
		// TODO Auto-generated method stub
		
	}
	
	public String getStringValue(ValueWriter valueWriter) {
		return valueWriter.getStatementString((Boolean)value);
	}

}
