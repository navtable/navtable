package es.udc.cartolab.gvsig.navtable.format;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class DoubleFormatNT {

    private static final String pattern = "0.##########";

    private static DecimalFormat doubleFormatOnDisplay;
    private static DecimalFormat doubleFormatOnEdit;
    private static DecimalFormat bigDecimalFormat;

    static {
	bigDecimalFormat = (DecimalFormat) NumberFormat
		.getNumberInstance(Locale.getDefault());
	bigDecimalFormat.applyPattern(pattern);
	bigDecimalFormat.setParseBigDecimal(true);
    }

    public static NumberFormat getDisplayingFormat() {
	if (doubleFormatOnDisplay == null) {
	    doubleFormatOnDisplay = (DecimalFormat) NumberFormat
		    .getNumberInstance(Locale.getDefault());
	    // Display a maximum of 10 decimals
	    doubleFormatOnDisplay.applyPattern(pattern);
	}
	return doubleFormatOnDisplay;
    }

    public static NumberFormat getEditingFormat() {
	if (doubleFormatOnEdit == null) {
	    doubleFormatOnEdit = (DecimalFormat) NumberFormat
		    .getNumberInstance(Locale.getDefault());
	    doubleFormatOnEdit.setGroupingUsed(false);
	}
	return doubleFormatOnEdit;
    }

    public static NumberFormat getBigDecimalFormat() {
	return bigDecimalFormat;
    }

}
