package es.udc.cartolab.gvsig.navtable;

import javax.swing.JMenuItem;

/**
 *
 * @author Francisco Puga <fpuga@cartolab.es>
 *
 */
public interface INavTableContextMenu {

    /**
     * This is the string that must be used to register this addon in the
     * extension point. Example:
     *
     * ExtensionPoints extPoints = ExtensionPointsSingleton.getInstance();
     * INavTableContextMenu filtersAddon = new FiltersAddon();
     * String name = filtersAddon.getName();
     * extPoints.add(NavTableExtension.NAVTABLE_CONTEXT_MENU, name, filtersAddon);
     *
     */
    String getName();

    JMenuItem[] getMenuItems();

    boolean isVisible();

    void setNavtableInstance(NavTable navtable);

    /**
     * In the navtable preference page the visibility of the addon can be set.
     * If this method returns true the addon can be used from navtable by
     * default. If returns false, the user must enable it in the preferences
     * page before be able to use it
     *
     * In case of any exceptions occurs the default value will be false
     *
     */
    boolean getDefaultVisibiliy();
}
