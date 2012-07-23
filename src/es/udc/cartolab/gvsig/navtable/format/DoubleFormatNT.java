package es.udc.cartolab.gvsig.navtable.format;

import java.text.NumberFormat;
import java.util.Locale;

public class DoubleFormatNT {

    private static NumberFormat doubleFormatOnDisplay;
    private static final String FORMAT_DOUBLE_ON_EDIT = "##################.######";
    private static NumberFormat doubleFormatOnEdit;

    public static NumberFormat getDisplayingFormat() {
	if (doubleFormatOnDisplay == null) {
	    doubleFormatOnDisplay = NumberFormat.getNumberInstance(Locale
		    .getDefault());
	    doubleFormatOnDisplay.setMinimumFractionDigits(0);
	    doubleFormatOnDisplay.setMaximumFractionDigits(100);
	    return doubleFormatOnDisplay;
	}
	return doubleFormatOnDisplay;
    }

    public static NumberFormat getEditingFormat() {
	if (doubleFormatOnEdit == null) {
	    doubleFormatOnEdit = NumberFormat.getNumberInstance(Locale
		    .getDefault());
	    doubleFormatOnEdit.format(FORMAT_DOUBLE_ON_EDIT);
	    return doubleFormatOnEdit;
	}
	return doubleFormatOnEdit;
    }

}
