package es.udc.cartolab.gvsig.navtable;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.hardcode.gdbms.engine.values.Value;
import com.iver.andami.PluginServices;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.andami.ui.mdiManager.IWindowListener;
import com.iver.andami.ui.mdiManager.WindowInfo;
import com.iver.cit.gvsig.CADExtension;
import com.iver.cit.gvsig.FiltroExtension;
import com.iver.cit.gvsig.fmap.DriverException;
import com.iver.cit.gvsig.fmap.MapControl;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.drivers.DriverIOException;
import com.iver.cit.gvsig.fmap.edition.EditionEvent;
import com.iver.cit.gvsig.fmap.edition.VectorialEditableAdapter;
import com.iver.cit.gvsig.fmap.layers.FBitSet;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.ReadableVectorial;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;
import com.iver.cit.gvsig.fmap.layers.SelectionEvent;
import com.iver.cit.gvsig.fmap.layers.SelectionListener;
import com.iver.cit.gvsig.fmap.layers.layerOperations.AlphanumericData;
import com.iver.cit.gvsig.layers.VectorialLayerEdited;
import com.iver.cit.gvsig.project.documents.view.gui.View;

/**
 * 
 * AbstractNavTable is the base class that defines the
 * layout of the window that allows to navigate between
 * the elements of the layer.
 * 
 * It has three panels:
 * <ul>
 * <li>The north panel, with the controls to handle the 
 * navigation behavior.
 * <li>The main panel, with the representation of the 
 * layer data, it must be implemented in the subclasses.
 * <li>The south panel, with the navigation controls.
 * </ul>
 * 
 * <img src="images/NavTableWindow.png">
 * 
 * If there are a image on 'gvSIG/extensiones/es.udc.cartolab.gvsig.navtable/images/navtable_header.png' 
 * is loaded on the NorthPanel. 
 * 
 * @author Nacho Varela
 * @author Javier Estevez
 * 
 */
public abstract class AbstractNavTable extends JPanel implements IWindow, ActionListener, SelectionListener, IWindowListener {

	private static final long serialVersionUID = 1L;

	private boolean closed = false;
	
	protected JPanel northPanel = null;
	protected JPanel centerPanel = null;
	protected JPanel southPanel = null;

	protected WindowInfo viewInfo = null;
	protected long currentPosition = 0;

	protected FLyrVect layer = null;
	protected SelectableDataSource recordset = null;
	
	// NORTH
	JCheckBox onlySelectedCB = null;
	JCheckBox fixScaleCB = null;
	JCheckBox alwaysZoomCB = null;
	JCheckBox alwaysSelectCB = null;	

	JButton filterB = null;
	JButton noFilterB = null;

	// SOUTH
	JButton firstB = null;
	JButton beforeB = null;
	JTextField posTF = null;
	JLabel totalLabel = null;
	JButton nextB = null;
	JButton lastB = null;
	JButton copyPreviousB = null;
	JButton copySelectedB = null;
	JButton zoomB = null;
	JButton selectionB = null;
	JButton saveB = null;
	JButton removeB = null;
	JButton cancelB = null;

	/**
	 * 
	 * Constructor of the class. It gets the data from the layer
	 * and stores it in recordset to later uses.
	 * 
	 * @param layer		Vectorial layer whose data will be accessed.
	 */
	public AbstractNavTable(FLyrVect layer) {
		super();
		this.layer = layer;
		WindowInfo window = this.getWindowInfo();
		String title = window.getTitle();
		window.setTitle(title+": "+layer.getName());
		try {
			this.recordset = layer.getRecordset();
			this.recordset.addSelectionListener(this);
		} catch (DriverException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public AbstractNavTable(SelectableDataSource recordset) {
		super();
		this.layer = null;
		WindowInfo window = this.getWindowInfo();
		String title = window.getTitle();
		window.setTitle(title+": "+" NavTable de DBFs sueltos" /*layer.getName()*/);
//		try {
			this.recordset = recordset;
			this.recordset.addSelectionListener(this);
//		} catch (DriverException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

	}

	/**
	 * It initializes the window.
	 * 
	 * @return 	true if it is successful, false if not.
	 */
	public abstract boolean init();

	/**
	 * It shows the values of a data row in the main panel.
	 * 
	 * @param rowPosition the row of the data to be shown.
	 */
	public abstract void fillValues();
	
	
	/**
	 * It fills NavTable with empty values. Used when "Selected" option is set on, but 
	 *    there are any selection registers.  
	 * 
	 */
	public void fillEmptyValues(){
		currentPosition = -1;
		//TODO Set not enabled all navButtons, seleccion, etc.
		
	}

	
	/**
	 * It selects a specific row into the table.
	 * 
	 * @param row
	 */
	public abstract void selectRow(int row);
	/**
	 * Checks if there's changed values.
	 * 
	 * @return a vector with the position of the values that have changed.
	 */
	protected abstract Vector checkChangedValues();
	
	
	/**
	 * Saves the changes of the current data row.
	 *
	 */
	protected abstract void saveRegister();

	/**
	 * Creates the upper panel.
	 * 
	 * @return the panel.
	 */
	protected JPanel getNorthPanel() {

		northPanel = new JPanel(new BorderLayout());
		
		JPanel northFirstRow = new JPanel(new BorderLayout());

		File iconPath = new File("gvSIG/extensiones/es.udc.cartolab.gvsig.navtable/images/navtable_header.png");
		if (iconPath.exists()) {
			northFirstRow.setBackground(Color.WHITE);
			ImageIcon logo = new ImageIcon(iconPath.getAbsolutePath());
			JLabel icon = new JLabel();
			icon.setIcon(logo);
			northFirstRow.add(icon, BorderLayout.WEST);
			viewInfo.setHeight(575);
		}

		JPanel filterPanel = new JPanel(new FlowLayout());
		if (iconPath.exists()) {
			filterPanel.setBackground(Color.WHITE);
		}
		
		java.net.URL imgURL = null;
		imgURL = getClass().getResource("/filter.png");
		ImageIcon filterImage = new ImageIcon(imgURL);
		imgURL = getClass().getResource("/nofilter.png");
		ImageIcon noFilterImage = new ImageIcon(imgURL);

		filterB = new JButton(filterImage);
		filterB.setToolTipText(PluginServices.getText(this, "filterTooltip"));
		filterB.addActionListener(this);
		noFilterB = new JButton(noFilterImage);
		noFilterB.setToolTipText(PluginServices.getText(this, "noFilterTooltip"));
		noFilterB.addActionListener(this);

		filterPanel.add(filterB);
		filterPanel.add(noFilterB);
		northFirstRow.add(filterPanel, BorderLayout.EAST);				

		JPanel optionsPanel = new JPanel(new FlowLayout());

		onlySelectedCB = new JCheckBox(PluginServices.getText(this, "selectedCheckBox"));
		onlySelectedCB.addActionListener(this);
		optionsPanel.add(onlySelectedCB);

		alwaysSelectCB = new JCheckBox(PluginServices.getText(this, "selectCheckBox"));
		alwaysSelectCB.addActionListener(this);
		optionsPanel.add(alwaysSelectCB);

		alwaysZoomCB = new JCheckBox(PluginServices.getText(this, "alwaysZoomCheckBox"));
		alwaysZoomCB.addActionListener(this);
		optionsPanel.add(alwaysZoomCB);

		fixScaleCB = new JCheckBox(PluginServices.getText(this, "fixedScaleCheckBox"));
		fixScaleCB.addActionListener(this);
		optionsPanel.add(fixScaleCB);

		northPanel.add(northFirstRow, BorderLayout.NORTH);
		northPanel.add(optionsPanel, BorderLayout.SOUTH);

		return northPanel;

	}
	
	public void fillValues(long rowPosition) {
		currentPosition = rowPosition;
		refreshGUI();
	}


	/**
	 * Creates the main panel.
	 * 
	 * @return the panel.
	 */
	public abstract JPanel getCenterPanel();

	/**
	 * Creates the bottom panel.
	 * 
	 * @return the panel.
	 */
	protected JPanel getSouthPanel(){
		
		java.net.URL imgURL = null;
		
		imgURL = getClass().getResource("/go-first.png");
		ImageIcon imagenFirst = new ImageIcon(imgURL);
		firstB = new JButton(imagenFirst);
		firstB.setToolTipText(PluginServices.getText(this, "goFirstButtonTooltip"));
		firstB.addActionListener(this);
		imgURL = getClass().getResource("/go-previous.png");
		ImageIcon imagenPrevious = new ImageIcon(imgURL);
		beforeB = new JButton(imagenPrevious);
		beforeB.setToolTipText(PluginServices.getText(this, "goPreviousButtonTooltip"));
		beforeB.addActionListener(this);
		posTF = new JTextField(5);
		posTF.addActionListener(this);
		totalLabel = new JLabel();
		imgURL = getClass().getResource("/go-next.png");
		ImageIcon imagenNext = new ImageIcon(imgURL);
		nextB = new JButton(imagenNext);
		nextB.setToolTipText(PluginServices.getText(this, "goNextButtonTooltip"));
		nextB.addActionListener(this);
		imgURL = getClass().getResource("/go-last.png");
		ImageIcon imagenLast = new ImageIcon(imgURL);
		lastB = new JButton(imagenLast);
		lastB.setToolTipText(PluginServices.getText(this, "goLastButtonTooltip"));
		lastB.addActionListener(this);
		imgURL = getClass().getResource("/copy-selected.png");
		ImageIcon imagenSelectedCopy = new ImageIcon(imgURL);
		copySelectedB = new JButton(imagenSelectedCopy);
		copySelectedB.setToolTipText(PluginServices.getText(this, "copySelectedButtonTooltip"));
		copySelectedB.addActionListener(this);
		imgURL = getClass().getResource("/copy.png");
		ImageIcon imagenPreviousCopy = new ImageIcon(imgURL);
		copyPreviousB = new JButton(imagenPreviousCopy);
		copyPreviousB.setToolTipText(PluginServices.getText(this, "copyPreviousButtonTooltip"));
		copyPreviousB.addActionListener(this);
		imgURL = getClass().getResource("/zoom.png");
		ImageIcon imagenZoom = new ImageIcon(imgURL);
		zoomB = new JButton(imagenZoom);
		zoomB.setToolTipText(PluginServices.getText(this, "zoomButtonTooltip"));
		zoomB.addActionListener(this);
		imgURL = getClass().getResource("/Select.png");
		ImageIcon imagenSelect = new ImageIcon(imgURL);
		selectionB = new JButton(imagenSelect);
		selectionB.setToolTipText(PluginServices.getText(this, "selectionButtonTooltip"));
		selectionB.addActionListener(this);
		imgURL = getClass().getResource("/save.png");
		ImageIcon imagenSave = new ImageIcon(imgURL);
		saveB = new JButton(imagenSave);
		saveB.setToolTipText(PluginServices.getText(this, "saveButtonTooltip"));
		saveB.addActionListener(this);
		imgURL = getClass().getResource("/delete.png");
		ImageIcon imagenDeleteRegister = new ImageIcon(imgURL);
		removeB = new JButton(imagenDeleteRegister);
		removeB.setToolTipText(PluginServices.getText(this,
							   "delete_register"));
		removeB.addActionListener(this);
		imgURL = getClass().getResource("/close.png");
		ImageIcon imagenClose = new ImageIcon(imgURL);
		cancelB = new JButton(imagenClose);
		cancelB.setToolTipText(PluginServices.getText(this, "closeButtonTooltip"));
		cancelB.addActionListener(this);

		//Buttons Panels
		JPanel navToolBar = new JPanel(new FlowLayout());
		navToolBar.add(firstB);
		navToolBar.add(beforeB);
		navToolBar.add(posTF);
		navToolBar.add(totalLabel);
		navToolBar.add(nextB);		
		navToolBar.add(lastB);

		JPanel actionsToolBar = new JPanel(new FlowLayout());
		actionsToolBar.add(copySelectedB);
		actionsToolBar.add(copyPreviousB);
		actionsToolBar.add(zoomB);
		actionsToolBar.add(selectionB);
		actionsToolBar.add(saveB);
		actionsToolBar.add(removeB);
		actionsToolBar.add(cancelB);

		JPanel buttonsPanel = new JPanel(new FlowLayout());
		buttonsPanel.add(navToolBar);		
		buttonsPanel.add(actionsToolBar);

		return buttonsPanel;

	}
	
	/**
	 * Shows a warning to the user if there's unsaved data.
	 *
	 */
	protected void showWarning() {
		if (currentPosition == -1) {
			return;
		}
		Vector changedValues = checkChangedValues();
		if (changedValues.size() > 0) {
			Object[] options = {PluginServices.getText(this, "saveButtonTooltip"),
					PluginServices.getText(this, "ignoreButton")};
			int response = JOptionPane.showOptionDialog(this,
					PluginServices.getText(this, "unsavedDataMessage"),
					PluginServices.getText(this, "unsavedDataTitle"),
					JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE,
					null,     //do not use a custom Icon
					options,  //the titles of buttons
					options[1]); //default button title
			if (response == 0) {
				saveRegister();
			}
		}
	}

	/**
	 * Goes to the next row of the data.
	 *
	 */
	public void next(){
		showWarning();
		try {
			if (onlySelectedCB.isSelected()){
				nextSelected();
			} else {
				if (currentPosition < recordset.getRowCount()){
					currentPosition = currentPosition + 1; 
					//fillValues();
				}
			}
			refreshGUI();
		} catch (com.hardcode.gdbms.engine.data.driver.DriverException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Goes to the next selected row of the data.
	 */
	protected void nextSelected(){
		FBitSet bitset= recordset.getSelection();
		int currentPos = Long.valueOf(currentPosition).intValue();
		int pos = bitset.nextSetBit(currentPos+1);
		if (pos != -1) {
			currentPosition = pos;
			//fillValues();
		}
	}

	/**
	 * Goes to the last row of the data.
	 *
	 */
	public void last(){
		showWarning();
		try {
			if (onlySelectedCB.isSelected()){
				lastSelected();
			} else {
				currentPosition = recordset.getRowCount()-1; 
				//fillValues();				
			}
			refreshGUI();
		} catch (com.hardcode.gdbms.engine.data.driver.DriverException e) {
			e.printStackTrace();
		}		
	}
	
	/** 
	 * Goes to the last selected row of the data.
	 *
	 */
	private void lastSelected(){
		FBitSet bitset= recordset.getSelection();
		int pos = bitset.length();
		if (pos != 0){
			currentPosition = pos -1;
			//fillValues();	
		}
		refreshGUI();
	}

	/**
	 * Goes to the first row of the data.
	 *
	 */
	public void first(){
		showWarning();
		if (onlySelectedCB.isSelected()){
			firstSelected();
		} else {
			currentPosition = 0;
			//fillValues();
		}
		refreshGUI();
	}

	/**
	 * Goes to the first selected row of the data.
	 *
	 */
	private void firstSelected(){
		FBitSet bitset= recordset.getSelection();		
		int pos = bitset.nextSetBit(0);
		if (pos != -1){
			currentPosition = pos;
			//fillValues();
		}
	}

	/**
	 * Goes to the previous row of the data.
	 *
	 */
	public void before() {
		showWarning();
		if (onlySelectedCB.isSelected()){
			beforeSelected();
		} else {
			// '-1' is used by NavTable to show empty values, so I must check it
			if (currentPosition > 0) {
				currentPosition = currentPosition-1;
			}
			//fillValues();
		}
		refreshGUI();
	}

	/**
	 * Goes to the previous selected row of the data.
	 *
	 */
	protected void beforeSelected() {
		FBitSet bitset= recordset.getSelection();
		int currentPos = Long.valueOf(currentPosition).intValue()-1;
		int pos = currentPos;
		for (; pos>= 0 && !bitset.get(pos); pos--);		
		if (pos != -1){
			currentPosition = pos;
			//fillValues();
		}
	}

	/**
	 * Zooms to the current feature. The feature will fill the 
	 * visualization area. 
	 *
	 */
	private void zoom(){

		Rectangle2D rectangle = null;		
		int pos = Long.valueOf(currentPosition).intValue();
		if (layer instanceof AlphanumericData) {
			//TODO gvSIG comment: Esta comprobacion se hacia con Selectable
			try {				
				IGeometry g;
				ReadableVectorial source = ((FLyrVect)layer).getSource();
				source.start();
				g = source.getShape(pos);
				source.stop();
				
				/* fix to avoid zoom problems when layer and view 
				 * projections aren't the same. */
				g.reProject(layer.getProjection().getCT(layer.getMapContext().getProjection()));
				
				rectangle = g.getBounds2D();

				if (rectangle.getWidth() < 200){
					rectangle.setFrameFromCenter(rectangle.getCenterX(), 
							rectangle.getCenterY(),
							rectangle.getCenterX()+100,
							rectangle.getCenterY()+100);
				}

				if (rectangle != null) {					
					layer.getMapContext().getViewPort().setExtent(rectangle);				
				}

			} catch (DriverIOException e) {
				e.printStackTrace();
			}
		}        

	}

	/**
	 * It forces the application to use the current scale when navigating
	 * between features. It also centers the visualization to the feature
	 * when navigating. 
	 *
	 */
	private void fixScale() {
		long scale = layer.getMapContext().getScaleView();
		zoom();
		layer.getMapContext().setScaleView(scale);
	}
	
	
	private void selectCurrentFeature() {
		FBitSet bitset = null;
		int pos = Long.valueOf(currentPosition).intValue();
		//		//TODO gvSIG comment: Esta comprobacion se hacia con Selectable
		//		if (layer instanceof AlphanumericData) {                                     
		bitset = recordset.getSelection();
		if (!bitset.get(pos)){
			bitset.set(pos);
		} else {
			bitset.clear(pos);
			if (onlySelectedCB.isSelected()){
				lastSelected();
			}
		}
		recordset.setSelection(bitset);
		//		}        

	}

	/**
	 * 
	 * @return true if the current row is selected, false if not.
	 */
	private boolean isRecordSelected() {

//		FBitSet bitset = null;
//		if (currentPosition == -1){
//			return false;
//		}
//		int pos = Long.valueOf(currentPosition).intValue();
//		if (layer instanceof AlphanumericData) {
//			//TODO Esta comprobacion se hacia con Selectable                                     
//				if (recordset == null){
//					return false;
//				}
//				bitset = recordset.getSelection();
//				return bitset.get(pos);
//		}        
//		return false;
		return isRecordSelected(currentPosition);

	}

	/**
	 * 
	 * @return true if the current row is selected, false if not.
	 */
	private boolean isRecordSelected(long position) {

		FBitSet bitset = null;
		if (position == -1){
			return false;
		}
		int pos = Long.valueOf(position).intValue();
		//		if (layer instanceof AlphanumericData) {
		//			//TODO Esta comprobacion se hacia con Selectable                                     
		if (recordset == null){
			return false;
		}
		bitset = recordset.getSelection();
		return bitset.get(pos);
		//		}        
		//		return false;

	}

	
	
	/**
	 * Removes all selections of the layer.
	 *
	 */
	private void clearSelection() {
		FBitSet bitset = null;
		if (layer instanceof AlphanumericData) {
			//TODO Esta comprobacion se hacia con Selectable                                     
				bitset = recordset.getSelection();
				bitset.clear();
				//TODO
				// Deberia repintar el view
		}
	}

	/**
	 * Forces the application to navigate only between selected features.
	 *
	 */
	private void viewOnlySelected() {

		// areSelectedRows()
		FBitSet bitset= recordset.getSelection();
		if (bitset.cardinality() == 0){
			currentPosition = -1;
		}
		
		if (!isRecordSelected()){
			System.out.println("View only selected... getFirstSelected");
			firstSelected();
		}

	}

	/**
	 * Sets the configuration of the window.
	 * 
	 * @return	the configuration of the window.
	 */
	public WindowInfo getWindowInfo() {
		if (viewInfo == null) {
			viewInfo = new WindowInfo(WindowInfo.MODELESSDIALOG | WindowInfo.RESIZABLE | WindowInfo.PALETTE);
			viewInfo.setTitle(PluginServices.getText(this, "NavTable"));
			viewInfo.setWidth(425);
			viewInfo.setHeight(525);
		}
		return viewInfo;
	}

	/**
	 * Repaints the window.
	 *
	 */
	protected void refreshGUI(){
		boolean navEnabled;		
		try {
			if (recordset == null){
				// TODO 
				// If there is some problem with the recordset don't do anything.
				return; 
			}
						
			if (currentPosition >= recordset.getRowCount()) {
				currentPosition = recordset.getRowCount()-1;
			}
			// -1 means no register
			if (currentPosition < -1 ){
				currentPosition = 0;
			}

			if (currentPosition == -1) {
				posTF.setText("");
				navEnabled = false;
				fillEmptyValues();
			} else {				
				navEnabled = true;
				fillValues();
			}
			nextB.setEnabled(navEnabled);
			lastB.setEnabled(navEnabled);
			firstB.setEnabled(navEnabled);
			beforeB.setEnabled(navEnabled);			
			
			long rows = recordset.getRowCount();
			FBitSet rowsBitSet = recordset.getSelection();
			int selectedRows = rowsBitSet.cardinality();
			if (onlySelectedCB.isSelected()){
				totalLabel.setText("/" +"(" +selectedRows +") " +rows);
			}else {
				totalLabel.setText("/" +rows);
			}
		}
		catch (com.hardcode.gdbms.engine.data.driver.DriverException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

		if (valuesMustBeFilled()){
			posTF.setText(String.valueOf(currentPosition+1));

			if (alwaysSelectCB.isSelected()){
				selectionB.setEnabled(false);
				clearSelection();
				selectCurrentFeature();
			} else {
				selectionB.setEnabled(true);
			}

			if (alwaysZoomCB.isSelected()){
				zoomB.setEnabled(false);
				zoom();
			} else {
				zoomB.setEnabled(true);
			}

			if (fixScaleCB.isSelected()) {
				fixScale();
			}

		} else {
			fillEmptyValues();
			posTF.setText("");
		}

		
		java.net.URL imgURL = null;
		if (isRecordSelected()){
			imgURL = getClass().getResource("/Unselect.png");
			ImageIcon imagenUnselect = new ImageIcon(imgURL);
			selectionB.setIcon(imagenUnselect);
			posTF.setBackground(Color.YELLOW);
		} else {
			imgURL = getClass().getResource("/Select.png");
			ImageIcon imagenSelect = new ImageIcon(imgURL);
			selectionB.setIcon(imagenSelect);
			posTF.setBackground(Color.WHITE);
		}
		
		FBitSet selection = recordset.getSelection();
		if (selection.cardinality()==0) {
			copySelectedB.setEnabled(false);
		} else {
			copySelectedB.setEnabled(true);
		}
		
		if (currentPosition == 0) {
			copyPreviousB.setEnabled(false);
		} else {
			copyPreviousB.setEnabled(true);
		}

		selectionB.setEnabled(navEnabled);
		zoomB.setEnabled(navEnabled);
		saveB.setEnabled(navEnabled);
		alwaysZoomCB.setEnabled(navEnabled);
		alwaysSelectCB.setEnabled(navEnabled);
		fixScaleCB.setEnabled(navEnabled);
		
	}

	protected boolean valuesMustBeFilled() {
		FBitSet bitset= recordset.getSelection();
		
		int selectedNumber = bitset.cardinality();
		
		//TODO A veces da NULL!!!!!!!!!!!!!!!!!!!!!!!!!!!
		if (onlySelectedCB == null) {
			System.out.println("************************  onlySelectedCB: "+ onlySelectedCB);
			return false;
		}
		if (onlySelectedCB.isSelected() && selectedNumber == 0){
			return false;
		} else {
			return true;
		}
	}
	
	protected void copyPrevious() {
		String pos = posTF.getText();
		Long posNumber = null;
		
		try {
			posNumber = new Long(pos);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			posNumber = currentPosition; 
		}
		
		if (!isValidPosition(posNumber) ){
			if (currentPosition == -1) {
				posTF.setText("");
			} else {
				posTF.setText(String.valueOf(currentPosition+1));
			}
			return;
		}
		
		showWarning();
		try {
			currentPosition = posNumber.longValue()-1;
			refreshGUI();
		} catch (Exception e1){
			e1.printStackTrace();
		}
	}
	
	protected void copySelected() {
		FBitSet selection = recordset.getSelection();
		if (selection.cardinality()!=1) {
			//lanzar error
			JOptionPane.showMessageDialog(null,
				    PluginServices.getText(this, "justOneRecordMessage"),
				    PluginServices.getText(this, "justOneRecordTitle"),
				    JOptionPane.WARNING_MESSAGE);
		} else {
			//TODO Check this code
			long current = currentPosition;
			long selectedRow = selection.nextSetBit(0);
			currentPosition = selectedRow;
			fillValues();
			currentPosition = current;
		}
	}

	/**
	 * Handles the user actions.
	 */
	public void actionPerformed(ActionEvent e) {

		if (this.recordset == null){
			//TODO
			// If there is an error on the recordset of the layer do nothing.
			return;
		}
		
		if (e.getSource() == onlySelectedCB){
			alwaysSelectCB.setSelected(false);			
			if (onlySelectedCB.isSelected()){
				// areSelectedRows()
				if (currentPosition != -1 ) {
					viewOnlySelected();
				} 
			} else {
				if (currentPosition == -1 ) {
					currentPosition = 0;
				}
			}
			refreshGUI();
		}
		
		if (e.getSource() == alwaysZoomCB){
			fixScaleCB.setSelected(false);
			refreshGUI();
		}
		
		if (e.getSource() == fixScaleCB) {
			alwaysZoomCB.setSelected(false);
			refreshGUI();
		}
		
		if (e.getSource() == alwaysSelectCB){
			onlySelectedCB.setSelected(false);
			if (alwaysSelectCB.isSelected()){
				this.recordset.removeSelectionListener(this);
			} else {
				this.recordset.addSelectionListener(this);
			}
			refreshGUI();
		}
		
		if (e.getSource() == filterB){
			FiltroExtension fe = new FiltroExtension();			
			fe.initialize();
			fe.execute("FILTRO");			
		}
		
		if (e.getSource() == noFilterB){
			clearSelection();
		}
		
		if (e.getSource() == nextB){
			next();
		}
		
		if (e.getSource() == lastB){
			last();
		}
		
		if (e.getSource() == firstB){
			first();
		}
		
		if (e.getSource() == beforeB){
			before();			
		}
		
		if (e.getSource() == posTF){
			copyPrevious();
		}
		
		if (e.getSource() == copySelectedB){			
//			fillValues(currentPosition-1);
//			currentPosition = currentPosition + 1;
			
			copySelected();
			
		}
		
		if (e.getSource() == copyPreviousB){
			long current = currentPosition;
			currentPosition = currentPosition -1;
			fillValues();
			currentPosition = current;
		}
		
		if (e.getSource() == zoomB){
			zoom();
			//refreshGUI();
		}
		
		if (e.getSource() == selectionB){
			selectCurrentFeature();
			refreshGUI();
		}
		
		if (e.getSource() == saveB){
			saveRegister();			
			refreshGUI();
		}
		
		if (e.getSource() == removeB){
			int answer = JOptionPane.showConfirmDialog(null, PluginServices.getText(null, "confirm_delete_register"), null, JOptionPane.YES_NO_OPTION);
			if (answer == 0) {
				deleteRow();
			}
		}
				
		if (e.getSource() == cancelB){
			PluginServices.getMDIManager().closeWindow(this);
		}

	}

	private void deleteRow() {
		try {
			View view = (View) PluginServices.getMDIManager().getActiveWindow();
			MapControl map = view.getMapControl();
			IFeature feat;
			ReadableVectorial feats = layer.getSource();

			feats.start();
			
			if (currentPosition>-1) {
				
				feat = feats.getFeature((int)currentPosition);
				
				ToggleEditing te = new ToggleEditing();
				
				if (!layer.isEditing())
					te.startEditing(layer);
				
		        CADExtension.initFocus();
				CADExtension.setCADTool("_selection",true);		        
				CADExtension.getEditionManager().setMapControl(map);
				CADExtension.getCADToolAdapter().configureMenu();
							
				VectorialLayerEdited vle = CADExtension.getCADTool().getVLE();
				VectorialEditableAdapter vea = vle.getVEA();
				
				vea.removeRow((int)currentPosition, CADExtension.getCADTool().getName(), EditionEvent.GRAPHIC);
			
				te.stopEditing(layer, false);
				
				layer.setActive(true);
				
				//Refresh
				currentPosition = currentPosition -1;
				next();
			}
		} catch (DriverIOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DriverException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private boolean isValidPosition(Long pos) {
		if (pos == null || pos.longValue() == 0) {
			return false;
		}
		if (onlySelectedCB.isSelected()){
			return isRecordSelected(pos.longValue());
		}
		return true;
	}

	public void selectionChanged(SelectionEvent e) {
		if (currentPosition == -1 && onlySelectedCB.isSelected()){
			firstSelected();
		} else {
			if (onlySelectedCB.isSelected() && !isRecordSelected()){
				firstSelected();
			}
			if (!valuesMustBeFilled()){
				fillEmptyValues();
			}
		}
		refreshGUI();
	}
	
	public void windowClosed() {
		if (!closed) {
			showWarning();
			closed = true;
		}
	}
	
	public void windowActivated() {
		
	}
	
}
