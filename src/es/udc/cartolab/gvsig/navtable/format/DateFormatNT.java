package es.udc.cartolab.gvsig.navtable.format;


import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.log4j.Logger;

import com.hardcode.gdbms.engine.values.DateValue;
import com.hardcode.gdbms.engine.values.NullValue;
import com.hardcode.gdbms.engine.values.Value;

/**
 * 
 * @author Andrés Maneiro <amaneiro@icarto.es>
 * @author Jorge López <jlopez@cartolab.es>
 * 
 */
public class DateFormatNT {

    
    private static final Logger logger = Logger.getLogger(DateFormatNT.class);
    
    private static SimpleDateFormat dateFormat;

    public static String convertDateValueToString(Value date) {
	String dateString;
	if(date instanceof NullValue) {
	    dateString = "";
	} else {
	    Date tmp = ((DateValue) date).getValue();
	    SimpleDateFormat formatter = getDateFormat();
	    dateString = formatter.format(tmp);
	}
	return dateString;
    }

    public static Value convertStringToValue(String date) {
	if(date == "") {
	    return ValueFactoryNT.createNullValue();
	} else {
	    SimpleDateFormat formatter = getDateFormat();
	    try {
		return ValueFactoryNT.createValue(formatter.parse(date));
	    } catch (ParseException e) {
		e.printStackTrace();
		return ValueFactoryNT.createNullValue();
	    }
	}
    }
    
    public static java.util.Date convertStringToDate(String strDate) {
	java.util.Date date = null;
	if ((strDate == null) || (strDate.isEmpty())) {
	    return null;
	}
	SimpleDateFormat formatter = getDateFormat();
	try {
	    date = formatter.parse(strDate);
	} catch (ParseException e) {
	    logger.error(e.getStackTrace(), e);
	}
	return date;
    }
	

    public static SimpleDateFormat getDateFormat() {
	if (dateFormat == null) {
	    dateFormat = (SimpleDateFormat) DateFormat
		    .getDateInstance(DateFormat.SHORT);
	    String p = dateFormat.toLocalizedPattern();
	    if (!p.contains("yyyy")) {
		p = p.replaceAll("yy", "yyyy");
		dateFormat = new SimpleDateFormat(p);
	    }
	}
	return dateFormat;
    }

}
