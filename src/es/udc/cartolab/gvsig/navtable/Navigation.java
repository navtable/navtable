package es.udc.cartolab.gvsig.navtable;

import static es.udc.cartolab.gvsig.navtable.AbstractNavTable.EMPTY_REGISTER;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.fmap.layers.FBitSet;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;

import es.udc.cartolab.gvsig.navtable.listeners.PositionEvent;
import es.udc.cartolab.gvsig.navtable.listeners.PositionEventSource;
import es.udc.cartolab.gvsig.navtable.listeners.PositionListener;

public class Navigation implements ActionListener {

    private static final Logger logger = Logger.getLogger(Navigation.class);

    private final PositionEventSource positionEventSource = new PositionEventSource();

    // navigation buttons
    private JButton firstB = null;
    private JButton beforeB = null;
    private JTextField posTF = null;
    private JLabel totalLabel = null;
    private JButton nextB = null;
    private JButton lastB = null;

    private long currentPosition = 0;
    private JPanel navToolBar;

    private AbstractNavTable nt;

    public Navigation(AbstractNavTable nt) {
	this.nt = nt;
	this.onlySelectedCB = nt.onlySelectedCB;
	initWidgets();
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

    /**
     * Goes to the next row of the data.
     * 
     */
    public void next() {
	if (showWarning()) {
	    try {
		if (onlySelectedCB.isSelected()) {
		    nextSelected();
		} else {
		    if (getPosition() < getRecordset().getRowCount()) {
			setPosition(getPosition() + 1);
		    }
		}
	    } catch (ReadDriverException e) {
		logger.error(e.getMessage(), e);
	    }
	}
    }

    /**
     * Goes to the next selected row of the data.
     */
    private void nextSelected() {
	FBitSet bitset = getRecordset().getSelection();
	int currentPos = Long.valueOf(getPosition()).intValue();
	int pos = bitset.nextSetBit(currentPos + 1);
	if (pos != EMPTY_REGISTER) {
	    setPosition(pos);
	}
    }

    /**
     * Goes to the last row of the data.
     * 
     */
    public void last() {
	if (showWarning()) {
	    try {
		if (onlySelectedCB.isSelected()) {
		    lastSelected();
		} else {
		    setPosition(getRecordset().getRowCount() - 1);
		}
	    } catch (ReadDriverException e) {
		logger.error(e.getMessage(), e);
	    }
	}
    }

    /**
     * Goes to the last selected row of the data.
     * 
     */
    protected void lastSelected() {
	FBitSet bitset = getRecordset().getSelection();
	int pos = bitset.length();
	if (pos != 0) {
	    setPosition(pos - 1);
	}
    }

    /**
     * Goes to the first row of the data.
     * 
     */
    public void first() {
	if (showWarning()) {
	    if (onlySelectedCB.isSelected()) {
		firstSelected();
	    } else {
		setPosition(0);
	    }
	}
    }

    /**
     * Goes to the first selected row of the data.
     * 
     */
    protected void firstSelected() {
	FBitSet bitset = getRecordset().getSelection();
	int pos = bitset.nextSetBit(0);
	if (pos != EMPTY_REGISTER) {
	    setPosition(pos);
	}
    }

    /**
     * Goes to the previous row of the data.
     * 
     */
    public void previous() {
	if (showWarning()) {
	    if (onlySelectedCB.isSelected()) {
		previousSelected();
	    } else {
		if (getPosition() > 0) {
		    setPosition(getPosition() - 1);
		}
	    }
	}
    }

    /**
     * Goes to the previous selected row of the data.
     * 
     */
    private void previousSelected() {
	FBitSet bitset = getRecordset().getSelection();
	int currentPos = Long.valueOf(getPosition()).intValue() - 1;
	int pos = currentPos;
	for (; pos >= 0 && !bitset.get(pos); pos--) {
	    ;
	}
	if (pos != EMPTY_REGISTER) {
	    setPosition(pos);
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

    private boolean isValidPosition(Long pos) {
	if (pos == null) {
	    return false;
	}
	if (onlySelectedCB.isSelected()) {
	    return isRecordSelected(pos.longValue());
	}
	return true;
    }

    private void setTotalLabelText() throws ReadDriverException {
	long numberOfRowsInRecordset = getRecordset().getRowCount();
	if (onlySelectedCB.isSelected()) {
	    totalLabel.setText("/" + "(" + getNumberOfRowsSelected() + ") "
		    + numberOfRowsInRecordset);
	} else {
	    totalLabel.setText("/" + numberOfRowsInRecordset);
	}
    }

    private void posTFChanged() {
	String pos = posTF.getText();
	Long posNumber = null;
	try {
	    posNumber = new Long(pos);
	} catch (NumberFormatException e) {
	    logger.error(e.getMessage(), e);
	    posNumber = getPosition();
	} finally {
	    if (showWarning()) {
		// user will set a 1-based index to navigate through layer,
		// so we need to adapt it to currentPosition (a zero-based
		// index)
		setPosition(posNumber - 1);
	    }
	}
    }

    @Override
    public void actionPerformed(ActionEvent e) {
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

    public boolean isEmptyRegister() {
	return getPosition() == EMPTY_REGISTER;
    }

    public void addEventListener(PositionListener l) {
	positionEventSource.addEventListener(l);
    }

    public void removeEventListener(PositionListener l) {
	positionEventSource.removeEventListener(l);
    }

    public void refreshGUI(boolean navEnabled) throws ReadDriverException {
	this.onlySelectedCB = nt.onlySelectedCB; //TODO: remove
	firstB.setEnabled(navEnabled);
	beforeB.setEnabled(navEnabled);
	nextB.setEnabled(navEnabled);
	lastB.setEnabled(navEnabled);
	if (isEmptyRegister()) {
	    posTF.setText("");
	} else {
	    posTF.setText(String.valueOf(getPosition() + 1));
	}
	setTotalLabelText();
	if (isRecordSelected()) {
	    posTF.setBackground(Color.YELLOW);
	} else {
	    posTF.setBackground(Color.WHITE);
	}

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
    
    //TODO
    private JCheckBox onlySelectedCB;
    private SelectableDataSource getRecordset() {return nt.getRecordset();}
    private boolean isRecordSelected() {return nt.isRecordSelected();}
    private boolean isRecordSelected(long n) {return nt.isRecordSelected(n);}
    private int getNumberOfRowsSelected() {return nt.getNumberOfRowsSelected();}
    private boolean showWarning() { return nt.showWarning(); }
}
