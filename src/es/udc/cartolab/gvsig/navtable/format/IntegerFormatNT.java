package es.udc.cartolab.gvsig.navtable.format;

import java.text.DecimalFormat;

public class IntegerFormatNT {

    private static final String INTEGER_PATTERN_ON_DISPLAY = "###,###,###,###,###,##0";
    private static DecimalFormat integerFormatOnDisplay;
    private static final String INTEGER_PATTERN_ON_EDIT = "#################0";
    private static DecimalFormat integerFormatOnEdit;

    public static DecimalFormat getDisplayingFormat() {
	if(integerFormatOnDisplay != null) {
	    return integerFormatOnDisplay;
	}
	integerFormatOnDisplay = new DecimalFormat(INTEGER_PATTERN_ON_DISPLAY);
	return integerFormatOnDisplay;
    }

    public static DecimalFormat getEditingFormat() {
	if(integerFormatOnEdit != null) {
	    return integerFormatOnEdit;
	}
	integerFormatOnEdit = new DecimalFormat(INTEGER_PATTERN_ON_EDIT);
	return integerFormatOnEdit;
    }

}
