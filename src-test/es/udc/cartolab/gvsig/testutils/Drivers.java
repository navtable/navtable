package es.udc.cartolab.gvsig.testutils;

import java.io.File;

import com.iver.cit.gvsig.fmap.layers.LayerFactory;

public class Drivers {

    public static void initgvSIGDrivers(String driversPath) {

	throwIfPathNotExists(driversPath);
	initgvSIGReadDrivers(driversPath);
	initgvSIGWriteDrivers(driversPath);

    }

    private static void throwIfPathNotExists(String driversPath) {
	if (!new File(driversPath).exists()) {
	    throw new RuntimeException("Can't find drivers path: "
		    + driversPath);
	}
    }

    public static void initgvSIGReadDrivers(String driversPath) {
	LayerFactory.setDriversPath(driversPath);
	if (LayerFactory.getDM().getDriverNames().length < 1) {
	    throw new RuntimeException("Can't find drivers in path: "
		    + driversPath);
	}

    }

    public static void initgvSIGWriteDrivers(String driversPath) {
	LayerFactory.setWritersPath(driversPath);
	if (LayerFactory.getWM().getWriterNames().length < 1) {
	    throw new RuntimeException("Can't find writers in path: "
		    + driversPath);
	}
    }

}
