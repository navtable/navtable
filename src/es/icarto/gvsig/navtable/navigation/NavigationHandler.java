package es.icarto.gvsig.navtable.navigation;

import static es.udc.cartolab.gvsig.navtable.AbstractNavTable.EMPTY_REGISTER;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.RowSorter;
import javax.swing.RowSorter.SortKey;

import org.apache.log4j.Logger;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.fmap.edition.EditionEvent;
import com.iver.cit.gvsig.fmap.layers.FBitSet;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;

import es.udc.cartolab.gvsig.navtable.AbstractNavTable;
import es.udc.cartolab.gvsig.navtable.listeners.PositionEvent;
import es.udc.cartolab.gvsig.navtable.listeners.PositionEventSource;
import es.udc.cartolab.gvsig.navtable.listeners.PositionListener;

public class NavigationHandler implements ActionListener {

    private static final Logger logger = Logger.getLogger(NavigationHandler.class);

    private final PositionEventSource positionEventSource = new PositionEventSource();

    private JButton firstB = null;
    private JButton beforeB = null;
    private JTextField posTF = null;
    private JLabel totalLabel = null;
    private JButton nextB = null;
    private JButton lastB = null;

    private long currentPosition = 0;
    private JPanel navToolBar;

    private final AbstractNavTable nt;

    private RowSorter<? extends SelectableDataSource> sorter;

    public NavigationHandler(AbstractNavTable nt) {
	this.nt = nt;
	initWidgets();
	sorter = new NoRowSorter<SelectableDataSource>(nt.getRecordset());
    }

    public JPanel getToolBar() {
	return navToolBar;
    }

    private void initWidgets() {
	registerNavTableButtonsOnNavigationToolBarExtensionPoint();
	navToolBar = new JPanel(new FlowLayout());
	navToolBar.add(firstB);
	navToolBar.add(beforeB);
	navToolBar.add(posTF);
	navToolBar.add(totalLabel);
	navToolBar.add(nextB);
	navToolBar.add(lastB);
    }

    private void registerNavTableButtonsOnNavigationToolBarExtensionPoint() {
	firstB = getNavTableButton(firstB, "/go-first.png",
		"goFirstButtonTooltip");
	beforeB = getNavTableButton(beforeB, "/go-previous.png",
		"goPreviousButtonTooltip");
	posTF = new JTextField(5);
	posTF.addActionListener(this);
	totalLabel = new JLabel();
	nextB = getNavTableButton(nextB, "/go-next.png", "goNextButtonTooltip");
	lastB = getNavTableButton(lastB, "/go-last.png", "goLastButtonTooltip");
    }

    public void next() {
	if (onlySelectedCB.isSelected()) {
	    nextSelected();
	} else {
	    // setPosition(getPosition() + 1);
	    int viewPos = sorter.convertRowIndexToView((int) getPosition());
	    int viewLastPos = sorter.getViewRowCount() - 1;
	    if (viewPos < viewLastPos) {
		int modelNextPos = sorter.convertRowIndexToModel(viewPos + 1);
		setPosition(modelNextPos);
	    }
	}
    }

    // int pos = bitset.nextSetBit((int) getPosition() + 1);
    // if (pos != EMPTY_REGISTER) {
    // setPosition(pos);
    // }
    /**
     * This implementation should be tested with spare selections on big files
     * to check if has an acceptable performance
     */
    private void nextSelected() {
	FBitSet bitset = getRecordset().getSelection();
	int viewPos = sorter.convertRowIndexToView((int) getPosition());
	for (int i = viewPos + 1; i < sorter.getViewRowCount(); i++) {
	    int modelPos = sorter.convertRowIndexToModel(i);
	    if (bitset.get(modelPos)) {
		setPosition(modelPos);
		return;
	    }
	}
    }

    public void last() {
	if (onlySelectedCB.isSelected()) {
	    lastSelected();
	} else {
	    // setPosition(getRecordset().getRowCount() - 1);
	    int viewLastPos = sorter.getViewRowCount() - 1;
	    int modelLastPos = sorter.convertRowIndexToModel(viewLastPos);
	    setPosition(modelLastPos);
	}
    }

    // int pos = bitset.length();
    // if (pos != 0) {
    // setPosition(pos - 1);
    // }
    public void lastSelected() {
	FBitSet bitset = getRecordset().getSelection();
	int viewLastPos = sorter.getViewRowCount() - 1;
	for (int i = viewLastPos; i >= 0; i--) {
	    int modelPos = sorter.convertRowIndexToModel(i);
	    if (bitset.get(modelPos)) {
		setPosition(modelPos);
		return;
	    }
	}
    }

    public void first() {
	if (onlySelectedCB.isSelected()) {
	    firstSelected();
	} else {
	    // setPosition(0);
	    int viewFirstPos = 0;
	    int modelFirstPos = sorter.convertRowIndexToModel(viewFirstPos);
	    setPosition(modelFirstPos);
	}
    }

    public void firstSelected() {
	FBitSet bitset = getRecordset().getSelection();
	int viewFirstPos = 0;
	for (int i = viewFirstPos; i < sorter.getViewRowCount(); i++) {
	    int modelPos = sorter.convertRowIndexToModel(i);
	    if (bitset.get(modelPos)) {
		setPosition(modelPos);
		return;
	    }
	}
    }

    public void previous() {
	if (onlySelectedCB.isSelected()) {
	    previousSelected();
	} else {
	    // setPosition(getPosition() - 1);
	    int viewPos = sorter.convertRowIndexToView((int) getPosition());
	    int viewFirstPos = 0;
	    if (viewPos > viewFirstPos) {
		int modelPrevPos = sorter.convertRowIndexToModel(viewPos - 1);
		setPosition(modelPrevPos);
	    }
	}
    }

    // int pos = (int) (getPosition() - 1);
    // for (; pos >= 0 && !bitset.get(pos); pos--) {
    // ;
    // }
    // if (pos != EMPTY_REGISTER) {
    // setPosition(pos);
    // }
    private void previousSelected() {
	FBitSet bitset = getRecordset().getSelection();
	int viewPos = sorter.convertRowIndexToView((int) getPosition());
	for (int i = viewPos - 1; i >= 0; i--) {
	    int modelPos = sorter.convertRowIndexToModel(i);
	    if (bitset.get(modelPos)) {
		setPosition(modelPos);
		return;
	    }
	}
    }

    /**
     * {@link #init()} method must be called before this
     * 
     * @param newPosition
     *            zero-based index on recordset
     */
    public void setPosition(long newPosition) {
	if (!isValidPosition(newPosition)) {
	    return;
	}
	try {
	    if (newPosition >= getRecordset().getRowCount()) {
		newPosition = getRecordset().getRowCount() - 1;
	    } else if (newPosition < EMPTY_REGISTER) {
		newPosition = 0;
	    }
	    currentPosition = newPosition;
	    positionEventSource.fireEvent(new PositionEvent(this));
	} catch (ReadDriverException e) {
	    e.printStackTrace();
	}
    }

    public long getPosition() {
	return currentPosition;
    }

    private boolean isValidPosition(long pos) {
	if (onlySelectedCB.isSelected()) {
	    return isRecordSelected(pos);
	}
	return true;
    }

    private void setTotalLabelText() {
	try {
	    long numberOfRowsInRecordset = getRecordset().getRowCount();
	    if (onlySelectedCB.isSelected()) {
		totalLabel.setText("/" + "(" + getNumberOfRowsSelected() + ") "
			+ numberOfRowsInRecordset);
	    } else {
		totalLabel.setText("/" + numberOfRowsInRecordset);
	    }
	} catch (ReadDriverException e) {
	    logger.error(e.getStackTrace(), e);
	}

    }

    private void posTFChanged() {
	String pos = posTF.getText();
	try {
	    long userViewPos = Long.parseLong(pos) - 1;
	    int modelPos = sorter.convertRowIndexToModel((int) userViewPos);
	    setPosition(modelPos);
	} catch (NumberFormatException e) {
	    logger.error(e.getMessage(), e);
	    refreshGUI(firstB.isEnabled());
	}
    }

    @Override
    public void actionPerformed(ActionEvent e) {
	if (showWarning()) {
	    if (e.getSource() == nextB) {
		next();
	    } else if (e.getSource() == lastB) {
		last();
	    } else if (e.getSource() == firstB) {
		first();
	    } else if (e.getSource() == beforeB) {
		previous();
	    } else if (e.getSource() == posTF) {
		posTFChanged();
	    }
	}
    }

    public boolean isEmptyRegister() {
	return getPosition() == EMPTY_REGISTER;
    }

    public void addEventListener(PositionListener l) {
	positionEventSource.addEventListener(l);
    }

    public void removeEventListener(PositionListener l) {
	positionEventSource.removeEventListener(l);
    }

    public void refreshGUI(boolean navEnabled) {
	this.onlySelectedCB = nt.onlySelectedCB; // TODO: remove
	firstB.setEnabled(navEnabled);
	beforeB.setEnabled(navEnabled);
	nextB.setEnabled(navEnabled);
	lastB.setEnabled(navEnabled);
	if (isEmptyRegister()) {
	    posTF.setText("");
	} else {
	    // user will set a 1-based index to navigate through layer,
	    // so we need to adapt it to currentPosition (a zero-based
	    // index)
	    int p = sorter.convertRowIndexToView((int) getPosition());
	    posTF.setText(String.valueOf(p + 1));
	    // posTF.setText(String.valueOf(getPosition() + 1));
	}
	setTotalLabelText();
	if (isRecordSelected()) {
	    posTF.setBackground(Color.YELLOW);
	} else {
	    posTF.setBackground(Color.WHITE);
	}

    }

    public void setSortKeys(List<? extends SortKey> keys) {
	if (keys == null) {
	    sorter = new NoRowSorter<SelectableDataSource>(nt.getRecordset());
	} else if (sorter instanceof NoRowSorter) {
	    sorter = new NTRowSorter<SelectableDataSource>(nt.getRecordset());
	}
	sorter.setSortKeys(keys);
	refreshGUI(firstB.isEnabled());
    }

    public List<? extends SortKey> getSortKeys() {
	return sorter.getSortKeys();
    }

    // //////
    // / Probably should be removed and use a factory instead
    // /////

    private ImageIcon getIcon(String iconName) {
	java.net.URL imgURL = getClass().getResource(iconName);
	if (imgURL == null) {
	    imgURL = AbstractNavTable.class.getResource(iconName);
	}

	ImageIcon icon = new ImageIcon(imgURL);
	return icon;
    }

    private JButton getNavTableButton(JButton button, String iconName,
	    String toolTipName) {
	JButton but = new JButton(getIcon(iconName));
	but.setToolTipText(PluginServices.getText(this, toolTipName));
	but.addActionListener(this);
	return but;
    }

    // TODO
    private JCheckBox onlySelectedCB;

    private SelectableDataSource getRecordset() {
	return nt.getRecordset();
    }

    private boolean isRecordSelected() {
	return nt.isRecordSelected();
    }

    private boolean isRecordSelected(long n) {
	return nt.isRecordSelected(n);
    }

    private int getNumberOfRowsSelected() {
	return nt.getNumberOfRowsSelected();
    }

    private boolean showWarning() {
	return nt.showWarning();
    }

    public void modelChanged() {
	if (sorter instanceof NoRowSorter) {
	    sorter = new NoRowSorter<SelectableDataSource>(nt.getRecordset());
	} else if (sorter instanceof NTRowSorter) {
	    List<? extends SortKey> sortKeys = sorter.getSortKeys();
	    sorter = new NTRowSorter<SelectableDataSource>(nt.getRecordset());
	    sorter.setSortKeys(sortKeys);
	}
	refreshGUI(firstB.isEnabled());
    }
}
