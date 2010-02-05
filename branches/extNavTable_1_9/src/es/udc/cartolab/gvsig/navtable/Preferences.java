package es.udc.cartolab.gvsig.navtable;

import java.io.File;

public class Preferences {
	
	private static File configDir;
	private static File aliasDir;
	
	public Preferences(File configDir) {
		Preferences.configDir = configDir;
		if (!configDir.exists()) {
			configDir.mkdir();
			createAliasDir();
		}
	}
	
	public static File getConfigDir() {
		return configDir;
	}
	
	public static String getAliasDir() {
		return configDir.getAbsolutePath() + File.separator + "alias";
	}

	private void createAliasDir() {
		aliasDir = new File(configDir.getAbsolutePath() + File.separator + "alias");
		aliasDir.mkdir();
	}
	
}
