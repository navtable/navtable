package es.udc.cartolab.gvsig.navtable.format;

import java.text.NumberFormat;
import java.util.Locale;

public class IntegerFormatNT {

    private static NumberFormat integerFormatOnDisplay;
    private static NumberFormat integerFormatOnEdit;

    public static NumberFormat getDisplayingFormat() {
	if (integerFormatOnDisplay == null) {
	    integerFormatOnDisplay = NumberFormat.getInstance(Locale
		    .getDefault());
	    integerFormatOnDisplay.setGroupingUsed(false);
	}
	return integerFormatOnDisplay;
    }

    public static NumberFormat getEditingFormat() {
	if (integerFormatOnEdit == null) {
	    integerFormatOnEdit = NumberFormat.getNumberInstance(Locale
		    .getDefault());
	    integerFormatOnEdit.setGroupingUsed(false);
	}
	return integerFormatOnEdit;
    }

}
