package es.udc.cartolab.gvsig.navtable.format;


import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.icarto.gvsig.commons.gvsig2.DateValue;
import es.icarto.gvsig.commons.gvsig2.NullValue;
import es.icarto.gvsig.commons.gvsig2.Value;

/**
 * 
 * @author Andrés Maneiro <amaneiro@icarto.es>
 * @author Jorge López <jlopez@cartolab.es>
 * 
 */
public class DateFormatNT {


private static final Logger logger = LoggerFactory
		.getLogger(DateFormatNT.class);

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
		logger.error(e.getMessage(), e);
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
		logger.error(e.getMessage(), e);
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
