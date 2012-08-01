package es.udc.cartolab.gvsig.navtable.format;

import java.text.NumberFormat;
import java.util.Locale;

public class DoubleFormatNT {

    private static NumberFormat doubleFormatOnDisplay;
    private static NumberFormat doubleFormatOnEdit;

    public static NumberFormat getDisplayingFormat() {
	if (doubleFormatOnDisplay == null) {
	    doubleFormatOnDisplay = NumberFormat.getNumberInstance(Locale
		    .getDefault());
	    doubleFormatOnDisplay.setGroupingUsed(false);
	    doubleFormatOnDisplay.setMinimumFractionDigits(0);
	    doubleFormatOnDisplay.setMaximumFractionDigits(100);
	}
	return doubleFormatOnDisplay;
    }

    public static NumberFormat getEditingFormat() {
	if (doubleFormatOnEdit == null) {
	    doubleFormatOnEdit = NumberFormat.getNumberInstance(Locale
		    .getDefault());
	    doubleFormatOnEdit.setGroupingUsed(false);
	}
	return doubleFormatOnEdit;
    }

}
