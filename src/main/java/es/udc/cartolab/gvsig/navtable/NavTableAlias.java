package es.udc.cartolab.gvsig.navtable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;
import org.gvsig.fmap.dal.DataStoreParameters;
import org.gvsig.fmap.dal.feature.FeatureStore;
import org.gvsig.fmap.dal.serverexplorer.filesystem.FilesystemStoreParameters;
import org.gvsig.fmap.dal.store.db.DBStoreParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.udc.cartolab.gvsig.navtable.preferences.Preferences;

public class NavTableAlias {

	private static final Logger logger = LoggerFactory
			.getLogger(NavTableAlias.class);

	private final static String EXT = ".alias";

	private final Properties props = new Properties();

	public NavTableAlias(FeatureStore store) {
		File aliasFile = getAliasFile(store.getParameters());
		if (aliasFile != null) {
			loadProps(aliasFile);
		}
	}

	private File getAliasFile(DataStoreParameters parameters) {

		String filename = null;

		if (parameters instanceof FilesystemStoreParameters) {
			FilesystemStoreParameters params = (FilesystemStoreParameters) parameters;
			String shpFilePath = params.getFile().getAbsolutePath();
			String aliasPath = FilenameUtils.removeExtension(shpFilePath) + EXT;
			File aliasFile = new File(aliasPath);
			if (aliasFile.exists()) {
				return aliasFile;
			}

			filename = FilenameUtils.getBaseName(shpFilePath);
		} else if (parameters instanceof DBStoreParameters) {
			filename = ((DBStoreParameters) parameters).getTable();
		}

		if (filename == null) {
			return null;
		}

		String path = Preferences.getAliasDir() + File.separator;
		File file = new File(path + filename + EXT);
		if (file.exists()) {
			return file;
		}
		return null;
	}

	private void loadProps(File file) {
		FileInputStream is = null;
		try {
			is = new FileInputStream(file);
			props.load(is);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
	}

	public String getAlias(String fieldName) {
		return props.getProperty(fieldName, fieldName);
	}

}
