package es.icarto.gvsig.navtable.gvsig2;

public class Value {
	
	protected Object value;
	
	public Value() {
		
	}
	
	public Value(Object o) {
		this.value = o;
	}
	 
	public double doubleValue() {
		Number n = (Number) value;
		return n.doubleValue();
	}
	
	public float floatValue() {
		Number n = (Number) value;
		return n.floatValue();
	}
	
	public int intValue() {
		Number n = (Number) value;
		return n.intValue();
	}
	
	public long longValue() {
		Number n = (Number) value;
		return n.longValue();
	}
	
	public int getType() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getStringValue(ValueWriter valueWriter) {
		return value.toString();
	}
	
	@Override
	public String toString() {
		return value.toString();
	}
	
}
