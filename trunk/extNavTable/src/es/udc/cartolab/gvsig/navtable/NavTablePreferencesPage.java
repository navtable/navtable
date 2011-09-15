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
    private HashMap<String, JCheckBox> chbMap;

    public NavTablePreferencesPage() {
	super();
	id = this.getClass().getName();
	title = PluginServices.getText(this, "navtable_preferences_title");

	JLabel addonsVisibility = new JLabel(PluginServices.getText(this, "contextMenuAddonsEnabled"));
	addComponent(addonsVisibility);

	ExtensionPoint extensionPoint = (ExtensionPoint) ExtensionPointsSingleton
		.getInstance().get(NavTableExtension.NAVTABLE_CONTEXT_MENU);

	for (Object contextMenuAddon : extensionPoint.values()) {
	    try {
		INavTableContextMenu c = (INavTableContextMenu) contextMenuAddon;
		String name = c.getName();
		JCheckBox chb = new JCheckBox(name);
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
    }

    public void initializeDefaults() {
    }

    public ImageIcon getIcon() {
	return null;
    }

    public boolean isValueChanged() {
	return super.hasChanged();
    }

    @Override
    public void storeValues() throws StoreException {
    }

    @Override
    public void setChangesApplied() {
	// TODO: fpuga, i'm not sure how this method works.
	setChanged(false);
    }

}
