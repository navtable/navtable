package es.udc.cartolab.gvsig.navtable.utils;

import java.text.Collator;
import java.util.Comparator;

import org.apache.log4j.Logger;

import com.hardcode.gdbms.engine.values.BinaryValue;
import com.hardcode.gdbms.engine.values.BooleanValue;
import com.hardcode.gdbms.engine.values.ComplexValue;
import com.hardcode.gdbms.engine.values.DateValue;
import com.hardcode.gdbms.engine.values.NullValue;
import com.hardcode.gdbms.engine.values.NumericValue;
import com.hardcode.gdbms.engine.values.StringValue;
import com.hardcode.gdbms.engine.values.TimeValue;
import com.hardcode.gdbms.engine.values.TimestampValue;
import com.hardcode.gdbms.engine.values.Value;

public class ValueComparator implements Comparator<Value> {

    private static final Logger logger = Logger
	    .getLogger(ValueComparator.class);

    @Override
    /**
     * Prerequisite: o1 and o2 are instances of the same class
     */
    public int compare(Value o1, Value o2) {
	// Value
	// ValueFactory.getValueByType
	if (o1 instanceof NullValue) {
	    return 1;
	} else if (o2 instanceof NullValue) {
	    return -1;
	} else if (o1 instanceof NumericValue) {
	    // } else if (o1 instanceof ByteValue) { // extends Numeric
	    // } else if (o1 instanceof DoubleValue) { // extends Numeric
	    // } else if (o1 instanceof FloatValue) { // extends Numeric
	    // } else if (o1 instanceof IntValue) { // extends Numeric
	    // } else if (o1 instanceof LongValue) { // extends Numeric
	    // } else if (o1 instanceof ShortValue) { // extends Numeric
	    return Double.compare(((NumericValue) o1).doubleValue(),
		    ((NumericValue) o2).doubleValue());

	} else if (o1 instanceof StringValue) {
	    Collator collator = Collator.getInstance();
	    collator.setStrength(Collator.IDENTICAL);
	    return collator.compare(o1.toString(), o2.toString());

	} else if (o1 instanceof BooleanValue) {
	    // false first. if equals not matters to switch it
	    int comp = ((BooleanValue) o1).getValue() ? 1 : -1;
	    return comp;
	} else if (o1 instanceof DateValue) {
	    return ((DateValue) o1).getValue().compareTo(
		    ((DateValue) o2).getValue());
	} else if (o1 instanceof BinaryValue) {
	    logger.error("\n\nNot implemented\n\n");
	} else if (o1 instanceof ComplexValue) { // extends String
	    logger.error("\n\nNot implemented\n\n");
	} else if (o1 instanceof NullValue) {
	    logger.error("\n\nNot implemented\n\n");
	} else if (o1 instanceof TimestampValue) {
	    logger.error("\n\nNot implemented\n\n");
	} else if (o1 instanceof TimeValue) {
	    logger.error("\n\nNot implemented\n\n");
	}
	return 0;
    }
}
