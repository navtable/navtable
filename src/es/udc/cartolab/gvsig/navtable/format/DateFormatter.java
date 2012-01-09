package es.udc.cartolab.gvsig.navtable.format;

import java.sql.Date;
import java.text.SimpleDateFormat;

import com.hardcode.gdbms.engine.values.DateValue;
import com.hardcode.gdbms.engine.values.NullValue;
import com.hardcode.gdbms.engine.values.Value;

public class DateFormatter {

    //see java Date API
    private static final String DATE_PATTERN = "dd/MM/yyyy";
    private static SimpleDateFormat dateFormat;

    public static String convertDateValueToString(Value date) {
	String dateString;
	if(date instanceof NullValue) {
	    dateString = "";
	} else {
	    Date tmp = ((DateValue) date).getValue();
	    SimpleDateFormat formatter = new SimpleDateFormat(DATE_PATTERN);	
	    dateString = formatter.format(tmp);
	}
	return dateString;
    }

    public static Value convertStringToValue(String date) {
	if(date == "") {
	    return ValueFactoryNavTable.createNullValue();
	} else {
	    String[] vars = date.split("/");
	    if (vars.length == 3) {
		String gvsigDate = vars[0]+"-"+vars[1]+"-"+vars[2];
		return ValueFactoryNavTable.createValue(gvsigDate);
	    } else {
		return ValueFactoryNavTable.createNullValue();
	    }
	}
    }

    public static SimpleDateFormat getDateFormatter() {
	if(dateFormat != null) {
	    return dateFormat;
	}
	dateFormat = new SimpleDateFormat(DATE_PATTERN);
	return dateFormat;
    }
}
