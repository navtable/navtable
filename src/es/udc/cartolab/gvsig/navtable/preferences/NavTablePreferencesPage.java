package es.udc.cartolab.gvsig.navtable.preferences;

import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.gvsig.andami.PluginServices;
import org.gvsig.andami.preferences.AbstractPreferencePage;
import org.gvsig.andami.preferences.StoreException;
import org.gvsig.utils.NotExistInXMLEntity;
import org.gvsig.utils.XMLEntity;
import org.gvsig.utils.extensionPointsOld.ExtensionPoint;
import org.gvsig.utils.extensionPointsOld.ExtensionPointsSingleton;

import es.udc.cartolab.gvsig.navtable.AbstractNavTable;
import es.udc.cartolab.gvsig.navtable.contextualmenu.INavTableContextMenu;

/**
 *
 * @author Francisco Puga <fpuga@cartolab.es>
 *
 */
public class NavTablePreferencesPage extends AbstractPreferencePage {

    protected static Logger logger = Logger.getLogger("NavTable");

    private String id;
    private String title;
    private HashMap<INavTableContextMenu, JCheckBox> contextMenuMap = new HashMap<INavTableContextMenu, JCheckBox>();

    ExtensionPoint extensionPoint;


    public NavTablePreferencesPage() {
	super();
	id = this.getClass().getName();
	title = PluginServices.getText(this, "navtable_preferences_title");

	JLabel addonsVisibility = new JLabel(PluginServices.getText(this,
	"contextMenuAddonsEnabled"));
	addComponent(addonsVisibility);

	extensionPoint = (ExtensionPoint) ExtensionPointsSingleton
	.getInstance().get(AbstractNavTable.NAVTABLE_CONTEXT_MENU);

	for (Object contextMenuAddon : extensionPoint.values()) {
	    try {
		INavTableContextMenu c = (INavTableContextMenu) contextMenuAddon;
		JCheckBox chb = new JCheckBox(PluginServices.getText(this,
			c.getDescription()));
		contextMenuMap.put(c, chb);
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
	for (INavTableContextMenu contextMenu : contextMenuMap.keySet()) {
	    boolean value = false;
	    try {
		value = xml.getBooleanProperty(contextMenu.getName());
	    } catch (NotExistInXMLEntity e) {
		value = contextMenu.getDefaultVisibiliy();
		xml.putProperty(contextMenu.getName(), value);
	    }
	    contextMenuMap.get(contextMenu).setSelected(value);
	    contextMenu.setUserVisibility(value);
	}
    }


    public void initializeDefaults() {

	for (INavTableContextMenu contextMenu : contextMenuMap.keySet()) {
	    JCheckBox chb = contextMenuMap.get(contextMenu);
	    chb.setSelected(contextMenu.getDefaultVisibiliy());
	}
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
	for (INavTableContextMenu contextMenu : contextMenuMap.keySet()) {
	    boolean visibility = contextMenuMap.get(contextMenu).isSelected();
	    xml.putProperty(contextMenu.getName(), visibility);
	    contextMenu.setUserVisibility(visibility);
	}
    }

    @Override
    public void setChangesApplied() {
	// TODO: fpuga, i'm not sure how this method works.
	setChanged(false);
    }

}
