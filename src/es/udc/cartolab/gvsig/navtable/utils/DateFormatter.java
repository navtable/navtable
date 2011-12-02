package es.udc.cartolab.gvsig.navtable.utils;

import java.sql.Date;
import java.text.DateFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.hardcode.gdbms.engine.values.DateValue;
import com.hardcode.gdbms.engine.values.NullValue;
import com.hardcode.gdbms.engine.values.Value;
import com.hardcode.gdbms.engine.values.ValueFactory;

public class DateFormatter {

    //see java Date API
    public static final String DATE_PATTERN = "dd/MM/yyyy";

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
	    return ValueFactory.createNullValue();
	} else {
	    String[] vars = date.split("/");
	    if (vars.length == 3) {
		String gvsigDate = vars[0]+"-"+vars[1]+"-"+vars[2];
		return ValueFactory.createValue(gvsigDate);
	    } else {
		return ValueFactory.createNullValue();
	    }
	}
    }

}
