package es.udc.cartolab.gvsig.testutils;

//Simulates apache commons class
public class FileNameUtils {

    /**
     * Suppress default constructor for noninstantiability. AssertionError
     * avoids accidentally invoke the constructor within the class
     */
    private FileNameUtils() {
	throw new AssertionError();
    }

    public static String removeExtension(String name) {
	return name.substring(0, name.lastIndexOf("."));
    }

}
