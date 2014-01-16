package es.udc.cartolab.gvsig.navtable.format;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class DoubleFormatNT {

    private static DecimalFormat doubleFormatOnDisplay;
    private static DecimalFormat doubleFormatOnEdit;

    public static NumberFormat getDisplayingFormat() {
	if (doubleFormatOnDisplay == null) {
	    doubleFormatOnDisplay = (DecimalFormat) NumberFormat
		    .getNumberInstance(Locale
		    .getDefault());
	    // Display a maximum of 10 decimals
	    doubleFormatOnDisplay.applyPattern("0.##########");
	}
	return doubleFormatOnDisplay;
    }

    public static NumberFormat getEditingFormat() {
	if (doubleFormatOnEdit == null) {
	    doubleFormatOnEdit = (DecimalFormat) NumberFormat
		    .getNumberInstance(Locale
		    .getDefault());
	    doubleFormatOnEdit.setGroupingUsed(false);
	}
	return doubleFormatOnEdit;
    }

}
