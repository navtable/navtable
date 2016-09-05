package es.udc.cartolab.gvsig.navtable.format;

import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;

import org.gvsig.tools.dataTypes.DataTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.icarto.gvsig.commons.gvsig2.Value;
import es.icarto.gvsig.commons.gvsig2.ValueFactory;

/**
 * ValueFactory to create values from its type, taking into account the format
 * of the value as stated in NavTableFormats.
 */
public class ValueFactoryNT extends ValueFactory {

	private static final Logger logger = LoggerFactory
			.getLogger(ValueFactoryNT.class);

	/**
	 * for gvsig v1 types
	 */
	@Deprecated
	public static Value createValueByType(String text, int type)
			throws ParseException {

		Value value;

		if ((text == null) || (text.trim().isEmpty())) {
			return ValueFactory.createNullValue();
		}

		switch (type) {
		case Types.BIGINT:
			value = ValueFactory.createValue(Long.parseLong(text));
			break;

		case Types.BIT:
		case Types.BOOLEAN:
			value = ValueFactory.createValue(Boolean.valueOf(text)
					.booleanValue());
			break;

		case Types.CHAR:
		case Types.VARCHAR:
		case Types.LONGVARCHAR:
			value = ValueFactory.createValue(text);
			break;

		case Types.DATE:
			try {
				value = DateFormatNT.convertStringToValue(text);
			} catch (IllegalArgumentException e) {
				throw new ParseException(e.getMessage(), 0);
			}
			break;

		case Types.DECIMAL:
		case Types.NUMERIC:
		case Types.FLOAT:
		case Types.DOUBLE:
			NumberFormat doubleFormat = DoubleFormatNT.getDisplayingFormat();
			value = ValueFactory.createValue(doubleFormat.parse(text)
					.doubleValue());
			break;

		case Types.INTEGER:
			NumberFormat integerFormat = IntegerFormatNT.getDisplayingFormat();
			value = ValueFactory.createValue(integerFormat.parse(text)
					.intValue());
			break;

		case Types.REAL:
			value = ValueFactory.createValue(Float.parseFloat(text));
			break;

		case Types.SMALLINT:
			value = ValueFactory.createValue(Short.parseShort(text));
			break;

		case Types.TINYINT:
			value = ValueFactory.createValue(Byte.parseByte(text));
			break;

		case Types.BINARY:
		case Types.VARBINARY:
		case Types.LONGVARBINARY:
			if ((text.length() / 2) != (text.length() / 2.0)) {
				throw new ParseException(
						"binary fields must have even number of characters.", 0);
			}
			byte[] array = new byte[text.length() / 2];
			for (int i = 0; i < (text.length() / 2); i++) {
				String byte_ = text.substring(2 * i, (2 * i) + 2);
				array[i] = (byte) Integer.parseInt(byte_, 16);
			}
			value = ValueFactory.createValue(array);
			break;

		case Types.TIMESTAMP:
			value = ValueFactory.createValue(Timestamp.valueOf(text));
			break;

		case Types.TIME:
			DateFormat tf = DateFormat.getTimeInstance();
			value = ValueFactory
					.createValue(new Time(tf.parse(text).getTime()));
			break;

		case Types.OTHER:
			// We check if the text can be parsed as a number after stripping
			// starting and trailing zeroes
			doubleFormat = DoubleFormatNT.getDisplayingFormat();
			String aux = removeStartingTrailingZeros(text);
			try {
				Double doubleValue = doubleFormat.parse(aux).doubleValue();
				// If the parsed number has the same length as the string, we
				// can confirm it can be represented as a number
				if ((doubleValue.toString().length() == aux.length())
						&& (doubleValue >= 0.0)) {
					// If double value and int value are the same, then we
					// return an int
					if (doubleValue.intValue() == doubleValue.doubleValue()) {
						return ValueFactory.createValue(doubleValue.intValue());
					}
					return ValueFactory.createValue(doubleValue);
				}
			} catch (ParseException e) {
				logger.error(e.getMessage(), e);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}

		default:
			// By default, we return the original string
			value = ValueFactory.createValue(text);
		}

		return value;
	}

	/**
	 * for gvsig v2 types
	 */
	public static Value createValueByType2(String text, int type)
			throws Exception {

		Value value;

		if ((text == null) || (text.trim().isEmpty())) {
			return ValueFactory.createNullValue();
		}

		switch (type) {
		case DataTypes.LONG:
			value = ValueFactory.createValue(Long.parseLong(text));
			break;

		case DataTypes.BOOLEAN:
			value = ValueFactory.createValue(Boolean.valueOf(text)
					.booleanValue());
			break;

		case DataTypes.STRING:
			value = ValueFactory.createValue(text);
			break;

		case DataTypes.DATE:
			try {
				value = DateFormatNT.convertStringToValue(text);
			} catch (IllegalArgumentException e) {
				throw new ParseException(e.getMessage(), 0);
			}
			break;

		case DataTypes.BIGDECIMAL:
		case DataTypes.FLOAT:
		case DataTypes.DOUBLE:
			NumberFormat doubleFormat = DoubleFormatNT.getDisplayingFormat();
			value = ValueFactory.createValue(doubleFormat.parse(text)
					.doubleValue());
			break;

		case DataTypes.INT:
			NumberFormat integerFormat = IntegerFormatNT.getDisplayingFormat();
			value = ValueFactory.createValue(integerFormat.parse(text)
					.intValue());
			break;

		case DataTypes.BYTE:
			value = ValueFactory.createValue(Short.parseShort(text));
			// value = ValueFactory.createValue(Byte.parseByte(text));
			break;

		case DataTypes.BYTEARRAY:
		case DataTypes.ARRAY:
			if ((text.length() / 2) != (text.length() / 2.0)) {
				throw new ParseException(
						"binary fields must have even number of characters.", 0);
			}
			byte[] array = new byte[text.length() / 2];
			for (int i = 0; i < (text.length() / 2); i++) {
				String byte_ = text.substring(2 * i, (2 * i) + 2);
				array[i] = (byte) Integer.parseInt(byte_, 16);
			}
			value = ValueFactory.createValue(array);
			break;

		case DataTypes.TIMESTAMP:
			value = ValueFactory.createValue(Timestamp.valueOf(text));
			break;

		case DataTypes.TIME:
			DateFormat tf = DateFormat.getTimeInstance();
			value = ValueFactory
					.createValue(new Time(tf.parse(text).getTime()));
			break;

		default:
			// By default, we return the original string
			value = ValueFactory.createValue(text);

			// // We check if the text can be parsed as a number after stripping
			// // starting and trailing zeroes
			// doubleFormat = DoubleFormatNT.getDisplayingFormat();
			// String aux = removeStartingTrailingZeros(text);
			// try {
			// Double doubleValue = doubleFormat.parse(aux).doubleValue();
			// // If the parsed number has the same length as the string, we
			// // can confirm it can be represented as a number
			// if ((doubleValue.toString().length() == aux.length())
			// && (doubleValue >= 0.0)) {
			// // If double value and int value are the same, then we
			// // return an int
			// if (doubleValue.intValue() == doubleValue.doubleValue()) {
			// return ValueFactory.createValue(doubleValue.intValue());
			// }
			// return ValueFactory.createValue(doubleValue);
			// }
			// } catch (ParseException e) {
			// logger.error(e.getMessage(), e);
			// } catch (Exception e) {
			// logger.error(e.getMessage(), e);
			// }

		}

		return value;
	}

	private static String removeStartingTrailingZeros(String number) {
		char decimalSeparator = DoubleFormatNT.getDisplayingFormat()
				.format(1.1).charAt(1);
		String aux;
		if (!number.contains(decimalSeparator + "")) {
			aux = number.replaceAll("^[0]*", "");
		} else {
			aux = number.replaceAll("^[0]*", "").replaceAll("[0]*$", "")
					.replaceAll("\\" + decimalSeparator + "$", "");
			if (!aux.contains("" + decimalSeparator)) {
				aux += decimalSeparator + "0";
			}
			if (aux.startsWith("" + decimalSeparator)) {
				aux = "0" + aux;
			}
		}
		return aux;
	}

}
