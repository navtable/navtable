package es.udc.cartolab.gvsig.navtable.format;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.hardcode.gdbms.engine.values.DateValue;
import com.hardcode.gdbms.engine.values.NullValue;
import com.hardcode.gdbms.engine.values.Value;

public class DateFormatNT {

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
	    return ValueFactoryNT.createNullValue();
	} else {
	    SimpleDateFormat formatter = new SimpleDateFormat(DATE_PATTERN);
	    try {
		return ValueFactoryNT.createValue(formatter.parse(date));
	    } catch (ParseException e) {
		e.printStackTrace();
		return ValueFactoryNT.createNullValue();
	    }
	}
    }

    public static SimpleDateFormat getDateFormat() {
	if(dateFormat != null) {
	    return dateFormat;
	}
	dateFormat = new SimpleDateFormat(DATE_PATTERN);
	return dateFormat;
    }
}
