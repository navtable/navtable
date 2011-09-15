package es.udc.cartolab.gvsig.navtable;

import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import com.iver.andami.PluginServices;
import com.iver.andami.preferences.AbstractPreferencePage;
import com.iver.andami.preferences.StoreException;
import com.iver.utiles.NotExistInXMLEntity;
import com.iver.utiles.XMLEntity;
import com.iver.utiles.extensionPoints.ExtensionPoint;
import com.iver.utiles.extensionPoints.ExtensionPointsSingleton;

/**
 *
 * @author Francisco Puga <fpuga@cartolab.es>
 *
 */
public class NavTablePreferencesPage extends AbstractPreferencePage {

    protected static Logger logger = Logger.getLogger("NavTable");

    private String id;
    private String title;
    private HashMap<String, JCheckBox> chbMap = new HashMap<String, JCheckBox>();

    ExtensionPoint extensionPoint;


    public NavTablePreferencesPage() {
	super();
	id = this.getClass().getName();
	title = PluginServices.getText(this, "navtable_preferences_title");

	JLabel addonsVisibility = new JLabel(PluginServices.getText(this,
	"contextMenuAddonsEnabled"));
	addComponent(addonsVisibility);

	extensionPoint = (ExtensionPoint) ExtensionPointsSingleton
	.getInstance().get(NavTableExtension.NAVTABLE_CONTEXT_MENU);

	for (Object contextMenuAddon : extensionPoint.values()) {
	    try {
		INavTableContextMenu c = (INavTableContextMenu) contextMenuAddon;
		String name = c.getName();
		JCheckBox chb = new JCheckBox(name);
		chbMap.put(name, chb);
		addComponent(chb);
	    } catch (ClassCastException cce) {
		logger.error("Class is not a navtable context menu", cce);
	    }
	}
    }

    public String getID() {
	return id;
    }

    public String getTitle() {
	return title;
    }

    public JPanel getPanel() {
	return this;
    }

    public void initializeValues() {
	XMLEntity xml = PluginServices.getPluginServices(this)
	.getPersistentXML();
	for (String chb : chbMap.keySet()) {
	    boolean value = false;
	    try {
		value = xml.getBooleanProperty(chb);
	    } catch (NotExistInXMLEntity e) {
		value = getDefaultValue(chb);
		xml.putProperty(chb, value);
	    }
	    chbMap.get(chb).setSelected(value);
	}
    }


    public void initializeDefaults() {

	for (String chb : chbMap.keySet()) {
	    getDefaultValue(chb);
	}
    }

    private boolean getDefaultValue(String chb) {
	boolean defaultValue = false;
	try {
	    INavTableContextMenu c = (INavTableContextMenu) extensionPoint
	    .get(chb);
	    defaultValue = c.getDefaultVisibiliy();
	} catch (ClassCastException cce) {
	    logger.error("Class is not a navtable context menu", cce);
	}
	return defaultValue;
    }

    public ImageIcon getIcon() {
	return null;
    }

    public boolean isValueChanged() {
	return super.hasChanged();
    }

    @Override
    public void storeValues() throws StoreException {
	XMLEntity xml = PluginServices.getPluginServices(this)
	.getPersistentXML();
	for (String chb : chbMap.keySet()) {
	    xml.putProperty(chb, chbMap.get(chb).isSelected());
	}
    }

    @Override
    public void setChangesApplied() {
	// TODO: fpuga, i'm not sure how this method works.
	setChanged(false);
    }

}
