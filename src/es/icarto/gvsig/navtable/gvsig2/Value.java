package es.icarto.gvsig.navtable.gvsig2;

public class Value {
	
	protected Object value;
	
	public Value() {
		
	}
	
	public Value(Object o) {
		this.value = o;
	}
	 
	public double doubleValue() {
		return 0;
	}
	
	public float floatValue() {
		return 0;
	}
	
	public int intValue() {
		return 0;
	}
	
	public long longValue() {
		return 0;
	}
	
	public int getType() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getStringValue(ValueWriter valueWriter) {
		return value.toString();
	}
	
}
