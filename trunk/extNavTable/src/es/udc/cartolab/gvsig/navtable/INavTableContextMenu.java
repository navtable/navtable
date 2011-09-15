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

    /**
     * When the user change the visibility of the addon in the preferences page
     * this method is used to pass to the addon the visibility selected by the
     * user. The addons that implement this interface should store this
     * visibility and check it in the isVisible() method before check its own
     * visibility requirements. Example:
     *
     * private boolean userVisibility;
     *
     * public void setUserVisibility (boolean userVisibility) {
     *     this.userVisibility = userVisibility;
     * }
     *
     * public boolean isVisible() {
     *     return (userVisibility && myOwnConditions);
     * }
     */
    void setUserVisibility(boolean userVisibility);
}
