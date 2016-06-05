package es.icarto.gvsig.navtable.gvsig2;

import java.lang.reflect.Constructor;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;



/**
 * Factoría abstracta de objetos value que dado un tipo básico, devuelve el
 * wrapper apropiado
 *
 * @author $author$
 * @version $Revision$
 */
public class ValueFactory {
    final static int BYTE = 0;
    final static int SHORT = 1;
    final static int INTEGER = 2;
    final static int LONG = 3;
    final static int FLOAT = 5;
    final static int DOUBLE = 6;
    /**
     * Crea un objeto de tipo Value a partir de un int
     *
     * @param n valor que se quiere representar
     *
     * @return objeto Value con el valor que se pasa como parámetro
     */
    public static IntValue createValue(int n) {
        IntValue ret = new IntValue();
        ret.setValue(n);

        return ret;
    }

    /**
     * Crea un objeto de tipo Value a partir de un long
     *
     * @param l valor que se quiere representar
     *
     * @return objeto Value con el valor que se pasa como parámetro
     */
    public static LongValue createValue(long l) {
        LongValue ret = new LongValue();
        ret.setValue(l);

        return ret;
    }

    /**
     * Crea un objeto de tipo Value a partir de un String
     *
     * @param s valor que se quiere representar
     *
     * @return objeto Value con el valor que se pasa como parámetro
     */
    public static StringValue createValue(String s) {
        StringValue ret = new StringValue();
        ret.setValue(s);

        return ret;
    }

    /**
     * Crea un objeto de tipo Value a partir de un float
     *
     * @param f valor que se quiere representar
     *
     * @return objeto Value con el valor que se pasa como parámetro
     */
    public static FloatValue createValue(float f) {
        FloatValue ret = new FloatValue();
        ret.setValue(f);

        return ret;
    }

    /**
     * Crea un objeto de tipo Value a partir de un double
     *
     * @param d valor que se quiere representar
     *
     * @return objeto Value con el valor que se pasa como parámetro
     */
    public static DoubleValue createValue(double d) {
        DoubleValue ret = new DoubleValue();
        ret.setValue(d);

        return ret;
    }

    /**
     * Crea un objeto de tipo Date a partir de un Date
     *
     * @param d valor que se quiere representar
     *
     * @return objeto Value con el valor que se pasa como parámetro
     */
    public static DateValue createValue(Date d) {
        DateValue ret = new DateValue();
        ret.setValue(d);

        return ret;
    }

    /**
     * Creates a TimeValue object
     *
     * @param t Time value
     *
     * @return TimeValue
     */
    public static TimeValue createValue(Time t) {
        TimeValue ret = new TimeValue();
        ret.setValue(t);

        return ret;
    }

    /**
     * Creates a TimestampValue object
     *
     * @param t Timestamp value
     *
     * @return TimestampValue
     */
    public static TimestampValue createValue(Timestamp t) {
        TimestampValue ret = new TimestampValue();
        ret.setValue(t);

        return ret;
    }

    /**
     * Crea un objeto de tipo Value a partir de un booleano
     *
     * @param b valor que se quiere representar
     *
     * @return objeto Value con el valor que se pasa como parámetro
     */
    public static BooleanValue createValue(boolean b) {
        BooleanValue ret = new BooleanValue();
        ret.setValue(b);

        return ret;
    }

    /**
     * Creates an ArrayValue
     *
     * @param values DOCUMENT ME!
     *
     * @return ArrayValue
     */
//    public static ValueCollection createValue(Value[] values) {
//        ValueCollection v = new ValueCollection();
//        v.setValues(values);
//
//        return v;
//    }

    /**
     * Crea un Value a partir de un literal encontrado en una instrucción y su
     * tipo
     *
     * @param text Texto del valor
     * @param type Tipo del valor
     *
     * @return Objeto Value del tipo adecuado
     *
     * @throws SemanticException Si el tipo del literal no está soportado
     */
    public static Value createValue(String text, int type) {
    	return new Value();
//        throws SemanticException {
//        switch (type) {
//            case SQLEngineConstants.STRING_LITERAL:
//
//                StringValue r1 = new StringValue();
//                r1.setValue(text.substring(1, text.length() - 1));
//
//                return r1;
//
//            case SQLEngineConstants.INTEGER_LITERAL:
//
//                try {
//                    IntValue r2 = new IntValue();
//                    r2.setValue(Integer.parseInt(text));
//
//                    return r2;
//                } catch (NumberFormatException e) {
//                    LongValue r2 = new LongValue();
//                    r2.setValue(Long.parseLong(text));
//
//                    return r2;
//                }
//
//            case SQLEngineConstants.FLOATING_POINT_LITERAL:
//
//                try {
//                	DoubleValue r2 = new DoubleValue();
//                    r2.setValue(Double.parseDouble(text));
//                    return r2;
//                } catch (NumberFormatException e) {
//                    throw new SemanticException("Text could not be parsed as decimal (double): " + text);
//                }
//
//            default:
//                throw new SemanticException("Unexpected literal type: " + text +
//                    "->" + type);
//        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param text DOCUMENT ME!
     * @param type DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws ParseException DOCUMENT ME!
     */
    public static Value createValueByType(String text, int type)
        throws ParseException {
        Value value;

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
            	value = ValueFactory.createValue(new Date(Date.parse(text)));
            	}catch (IllegalArgumentException e) {
					throw new ParseException(e.getMessage(),0);
				}
                break;

            case Types.DECIMAL:
            case Types.NUMERIC:
            case Types.FLOAT:
            case Types.DOUBLE:
                value = ValueFactory.createValue(Double.parseDouble(text));

                break;

            case Types.INTEGER:
                value = ValueFactory.createValue(Integer.parseInt(text));

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
                    throw new ParseException("binary fields must have even number of characters.",
                        0);
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
                value = ValueFactory.createValue(new Time(
                            tf.parse(text).getTime()));

                break;

            default:
                value = ValueFactory.createValue(text);
        }

        return value;
    }
    
    public static Value createValueByType(String text, int type, int length, int decimalCount)
    throws ParseException {
    Value value;

    switch (type) {  
        case Types.CHAR:
        case Types.VARCHAR:
        case Types.LONGVARCHAR:       
            if ((text != null) && (text.length() > length)){
            	value =  ValueFactory.createValue(text.substring(0, length)); 
            }else{
            	value = ValueFactory.createValue(text);
            }
           
        break;
        default:
            value = createValueByType(text, type);
    }

    return value;
}

    /**
     * DOCUMENT ME!
     *
     * @param text DOCUMENT ME!
     * @param className DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws SemanticException DOCUMENT ME!
     *
     * @deprecated Use createValueWithType(String, int) instead
     */
    public static Value createValue(String text, String className)
         {
        if (className.equals("com.hardcode.gdbms.engine.values.BooleanValue")) {
            return createValue(Boolean.getBoolean(text));
        }

        if (className.equals("com.hardcode.gdbms.engine.values.DateValue")) {
            
                try {
					return createValue(DateFormat.getInstance().parse(text));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            
        }

        if (className.equals("com.hardcode.gdbms.engine.values.DoubleValue")) {
            return createValue(Double.parseDouble(text));
        }

        if (className.equals("com.hardcode.gdbms.engine.values.FloatValue")) {
            return createValue(Float.parseFloat(text));
        }

        if (className.equals("com.hardcode.gdbms.engine.values.IntValue")) {
            return createValue(Integer.parseInt(text));
        }

        if (className.equals("com.hardcode.gdbms.engine.values.LongValue")) {
            return createValue(Long.parseLong(text));
        }

        if (className.equals("com.hardcode.gdbms.engine.values.StringValue")) {
            return createValue(text);
        }
        return new NullValue();

        // default:
//        throw new SemanticException(
//            "Unexpected className in createValue (GDBMS) text: " + text +
//            "-> className: " + className);
    }

    /**
     * Creates a new null Value
     *
     * @return NullValue
     */
    public static NullValue createNullValue() {
        return new NullValue();
    }

    /**
     * Dado el tipo que se pasa como parámetro, expresado con una de las
     * constantes definidas en la clase java.sql.Types se devuelve la clase
     * que implementa dicho tipo
     *
     * @param type Tipo de la columna
     *
     * @return Clase que implementa el tipo
     *
     * @throws RuntimeException if type is not recognized
     */
    public static Class getType(int type) {
        switch (type) {
            case Types.NUMERIC:
            case Types.BIGINT:
                return LongValue.class;

            case Types.BIT:
                return BooleanValue.class;

            case Types.SMALLINT:
                return ShortValue.class;

            case Types.TINYINT:
                return ByteValue.class;

            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
                return StringValue.class;

            case Types.DATE:
                return DateValue.class;

            case Types.FLOAT:
            case Types.DOUBLE:
            case Types.DECIMAL:
                return DoubleValue.class;

            case Types.INTEGER:
                return IntValue.class;

            case Types.REAL:
                return FloatValue.class;

            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
                return BinaryValue.class;

            case Types.TIMESTAMP:
                return TimestampValue.class;

            case Types.TIME:
                return TimeValue.class;

            case Types.OTHER:default:
                throw new RuntimeException("Type not recognized: " + type);
        }
    }

    /**
     * Gets a Value with the value v1 plus v2
     *
     * @param v1 first value
     * @param v2 second value
     *
     * @return a numeric value with the operation
     */
    static NumericValue suma(NumericValue v1, NumericValue v2) {
        int type = Math.max(v1.getType(), v2.getType());

        while (true) {
            switch (type) {
                /*
                 * El operador '+' en java no está definido para byte ni short, así
                 * que nosotros tampoco lo definimos.
                 * Por otro lado no conocemos manera de detectar el overflow al operar
                 * con long's ni double's de manera eficiente, así que no se detecta.
                 */
                case BYTE:
                case SHORT:
                case INTEGER:

                    int intValue = v1.intValue() + v2.intValue();

                    if ((intValue) != (v1.longValue() + v2.longValue())) {
                        type = LONG;

                        continue;
                    } else {
                        return (NumericValue) createValue(intValue);
                    }

                case LONG:
                    return (NumericValue) createValue(v1.longValue() +
                        v2.longValue());

                case FLOAT:

                    float floatValue = v1.floatValue() + v2.floatValue();

                    if ((floatValue) != (v1.doubleValue() + v2.doubleValue())) {
                        type = DOUBLE;

                        continue;
                    } else {
                        return (NumericValue) createValue(floatValue);
                    }

                case DOUBLE:
                    return (NumericValue) createValue(v1.doubleValue() +
                        v2.doubleValue());
            }
        }
    }

    /**
     * Gets the value of the operation v1  v2
     *
     * @param v1 first value
     * @param v2 second value
     *
     * @return a numeric value with the operation
     */
    static NumericValue producto(NumericValue v1, NumericValue v2) {
        int type = Math.max(v1.getType(), v2.getType());

        while (true) {
            switch (type) {
                /*
                 * El operador '+' en java no está definido para byte ni short, así
                 * que nosotros tampoco lo definimos.
                 * Por otro lado no conocemos manera de detectar el overflow al operar
                 * con long's ni double's de manera eficiente, así que no se detecta.
                 */
                case BYTE:
                case SHORT:
                case INTEGER:

                    int intValue = v1.intValue() * v2.intValue();

                    if ((intValue) != (v1.intValue() * v2.intValue())) {
                        type = LONG;

                        continue;
                    } else {
                        return (NumericValue) createValue(intValue);
                    }

                case LONG:
                    return (NumericValue) createValue(v1.longValue() * v2.longValue());

                case FLOAT:

                    float floatValue = v1.floatValue() * v2.floatValue();

                    if ((floatValue) != (v1.doubleValue() * v2.doubleValue())) {
                        type = DOUBLE;

                        continue;
                    } else {
                        return (NumericValue) createValue(floatValue);
                    }

                case DOUBLE:
                    return (NumericValue) createValue(v1.doubleValue() * v2.doubleValue());
            }
        }
    }

    /**
     * Calcula la inversa (1/v) del valor que se pasa como parámetro.
     *
     * @param v Valor cuya inversa se quiere obtener
     *
     * @return DoubleValue
     */
    static NumericValue inversa(NumericValue v) {
        int type = v.getType();

        return (NumericValue) createValue(1 / v.doubleValue());
    }

    /**
     * Creates a byte array value
     *
     * @param bytes bytes of the value
     *
     * @return
     */
    public static BinaryValue createValue(byte[] bytes) {
        BinaryValue ret = new BinaryValue(bytes);

        return ret;
    }

    /**
     * Creates a complex value based on a xml parseable
     * string
     *
     * @param string
     *
     * @return new instance of ComplexValue or null???
     */
    public static ComplexValue createComplexValue(String string) {
    	try {
    		return new ComplexValue(string);

    	} catch (Exception e) {
    		//FIXME: OJO!!! QUE HACEMOS AQUI
    		return null;
		}
    }

    /**
     *  Create a Value instance from a String value using
     * the class specified in valueName.
     * <P>
     *
     * <code>
     * Nota: Habria que ver que hacemos con esto... seguimos delegando
     * o modificar el createValueByType para que delege en esta
     * </code>
     * <P>
     * @param text String representation of value
     * @param valueName class name of the instance of Value to return.
     * 			It can be the class name without the package.
     * @return a Value instance
     * @throws ParseException
     */
    public static Value createValueByValueName(String text, String valueName)
    	throws ParseException {
    	String baseName;
    	if (valueName.indexOf(".") > -1) {
    		baseName = valueName.substring( valueName.lastIndexOf(".")+1);
    	} else {
    		baseName = valueName;
    	}
    	if (baseName.equals("BinaryValue")) {
    		return createValueByType(text,Types.BINARY);
    	} else if (baseName.equals("BooleanValue")) {
    		return createValueByType(text,Types.BOOLEAN);
    	} else if (baseName.equals("ByteValue")) {
    		return createValueByType(text,Types.TINYINT);
    	} else if (baseName.equals("ComplexValue")) {
    		return createComplexValue(text);
    	} else if (baseName.equals("DateValue")) {
    		return createValueByType(text,Types.DATE);
    	} else if (baseName.equals("DoubleValue")) {
    		return createValueByType(text,Types.DOUBLE);
    	} else if (baseName.equals("FloatValue")) {
    		return createValueByType(text,Types.REAL);
    	} else if (baseName.equals("IntValue")) {
    		return createValueByType(text,Types.INTEGER);
    	} else if (baseName.equals("LongValue")) {
    		return createValueByType(text,Types.BIGINT);
    	} else if (baseName.equals("NullValue")) {
    		return new NullValue();
    	} else if (baseName.equals("ShortValue")) {
    		return createValueByType(text,Types.SMALLINT);
    	} else if (baseName.equals("StringValue")) {
    		return createValueByType(text,Types.VARCHAR);
    	} else if (baseName.equals("TimestampValue")) {
    		return createValueByType(text,Types.TIMESTAMP);
    	} else if (baseName.equals("TimeValue")) {
    		return createValueByType(text,Types.TIME);
    	} else {
    		try {
        		Class valueClass = Class.forName(valueName);
        		Constructor constr= valueClass.getConstructor(new Class[] {String.class});
        		return (Value) constr.newInstance(new Object[] {text});
			} catch (Exception e) {
				throw new ParseException(e.getMessage(),0);
			}
    	}
    }
}
