package es.udc.cartolab.gvsig.navtable.refactoring;

import java.awt.BorderLayout;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;

import com.iver.andami.PluginServices;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.andami.ui.mdiManager.WindowInfo;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;

import es.udc.cartolab.gvsig.navtable.AbstractNavTable;

public class NavigationTablePanel extends JPanel implements IWindow {

    private NavigationTable nt;

    private JButton goToFirstRecord;
    private JButton goToPreviousRecord;
    private JButton goToNextRecord;
    private JButton goToLastRecord;
    private JToolBar navigationToolBar;
    private JTable table;

    private WindowInfo windowInfo;

    private JScrollPane scrollPane;

    public NavigationTablePanel(SelectableDataSource sds) {
	super(new BorderLayout());
	navigationToolBar = new JToolBar("Navigation actions");
	nt = new NavigationTable(sds);
	init();
    }

    private void init() {
	initializeTable();
	add(scrollPane, BorderLayout.PAGE_START);
	initializeButtons();
	add(navigationToolBar, BorderLayout.PAGE_END);
    }

    private void initializeTable() {
	table = new JTable(nt.getTableModel());
	scrollPane = new JScrollPane(table);
    }

    public void initializeButtons() {
	navigationToolBar = new JToolBar();

	goToFirstRecord = new JButton(nt.getAction(nt.GO_FIRST));
	nt.setActionIcon(nt.GO_FIRST, getIcon("/go-first.png"));
	goToPreviousRecord = new JButton(nt.getAction(nt.GO_PREV));
	nt.setActionIcon(nt.GO_PREV, getIcon("/go-previous.png"));
	goToNextRecord = new JButton(nt.getAction(nt.GO_NEXT));
	nt.setActionIcon(nt.GO_NEXT, getIcon("/go-next.png"));
	goToLastRecord = new JButton(nt.getAction(nt.GO_LAST));
	nt.setActionIcon(nt.GO_LAST, getIcon("/go-last.png"));

	navigationToolBar.add(goToFirstRecord);
	navigationToolBar.add(goToPreviousRecord);
	navigationToolBar.add(goToNextRecord);
	navigationToolBar.add(goToLastRecord);
    }

    private ImageIcon getIcon(String iconName) {
	java.net.URL imgURL = getClass().getResource(iconName);
	if (imgURL == null) {
	    imgURL = AbstractNavTable.class.getResource(iconName);
	}
	ImageIcon icon = new ImageIcon(imgURL);
	return icon;
    }

    @Override
    public WindowInfo getWindowInfo() {
	if (windowInfo == null) {
	    windowInfo = new WindowInfo(WindowInfo.MODELESSDIALOG
		    | WindowInfo.RESIZABLE | WindowInfo.PALETTE);
	    windowInfo.setTitle(PluginServices.getText(this, "NavTable"));
	    windowInfo.setWidth(this.getPreferredSize().width);
	    windowInfo.setHeight(this.getPreferredSize().height);
	}
	return windowInfo;
    }

    @Override
    public Object getWindowProfile() {
	return WindowInfo.PROPERTIES_PROFILE;
    }

}
