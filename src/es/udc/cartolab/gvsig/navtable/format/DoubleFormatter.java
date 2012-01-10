package es.udc.cartolab.gvsig.navtable.format;

import java.text.DecimalFormat;

public class DoubleFormatter {

    private static DecimalFormat doubleFormatOnDisplay;
    private static DecimalFormat doubleFormatOnEdit;
    private static final String FORMAT_DOUBLE_ON_DISPLAY = "###,###,###,###,###,##0.00";
    private static final String FORMAT_DOUBLE_ON_EDIT = "##################.##";

    public static DecimalFormat getDisplayingFormat() {
	if(doubleFormatOnDisplay != null) {
	    return doubleFormatOnDisplay;
	}
	doubleFormatOnDisplay = new DecimalFormat(FORMAT_DOUBLE_ON_DISPLAY);
	return doubleFormatOnDisplay;
    }

    public static DecimalFormat getEditingFormat() {
	if(doubleFormatOnEdit != null) {
	    return doubleFormatOnEdit;
	}
	doubleFormatOnEdit = new DecimalFormat(FORMAT_DOUBLE_ON_EDIT);
	return doubleFormatOnEdit;	
    }
    
}
