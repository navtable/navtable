package es.udc.cartolab.gvsig.navtable;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.iver.andami.PluginServices;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.andami.ui.mdiManager.WindowInfo;
import com.iver.cit.gvsig.FiltroExtension;
import com.iver.cit.gvsig.fmap.DriverException;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.drivers.DriverIOException;
import com.iver.cit.gvsig.fmap.layers.FBitSet;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.ReadableVectorial;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;
import com.iver.cit.gvsig.fmap.layers.layerOperations.AlphanumericData;

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
 * @author Nacho Varela
 * @author Javier Estevez
 * 
 */
public abstract class AbstractNavTable extends JPanel implements IWindow, ActionListener{

	private static final long serialVersionUID = 1L;

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
	JButton previousCopyB = null;
	JButton zoomB = null;
	JButton selectionB = null;
	JButton saveB = null;
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
		} catch (DriverException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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
	protected abstract void fillValues(long rowPosition);

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

		JPanel filterPanel = new JPanel(new FlowLayout());

		ImageIcon filterImage = new ImageIcon("gvSIG/extensiones/es.udc.cartolab.gvsig.navtable/images/filter.png");
		ImageIcon noFilterImage = new ImageIcon("gvSIG/extensiones/es.udc.cartolab.gvsig.navtable/images/nofilter.png");

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

	/**
	 * Creates the main panel.
	 * 
	 * @return the panel.
	 */
	protected abstract JPanel getCenterPanel();

	/**
	 * Creates the bottom panel.
	 * 
	 * @return the panel.
	 */
	protected JPanel getSouthPanel(){
		ImageIcon imagenFirst = new ImageIcon("gvSIG/extensiones/es.udc.cartolab.gvsig.navtable/images/go-first.png");
		firstB = new JButton(imagenFirst);
		firstB.setToolTipText(PluginServices.getText(this, "goFirstButtonTooltip"));
		firstB.addActionListener(this);
		ImageIcon imagenPrevious = new ImageIcon("gvSIG/extensiones/es.udc.cartolab.gvsig.navtable/images/go-previous.png");
		beforeB = new JButton(imagenPrevious);
		beforeB.setToolTipText(PluginServices.getText(this, "goPreviousButtonTooltip"));
		beforeB.addActionListener(this);
		posTF = new JTextField(5);
		posTF.addActionListener(this);
		totalLabel = new JLabel();
		ImageIcon imagenNext = new ImageIcon("gvSIG/extensiones/es.udc.cartolab.gvsig.navtable/images/go-next.png");
		nextB = new JButton(imagenNext);
		nextB.setToolTipText(PluginServices.getText(this, "goNextButtonTooltip"));
		nextB.addActionListener(this);
		ImageIcon imagenLast = new ImageIcon("gvSIG/extensiones/es.udc.cartolab.gvsig.navtable/images/go-last.png");
		lastB = new JButton(imagenLast);
		lastB.setToolTipText(PluginServices.getText(this, "goLastButtonTooltip"));
		lastB.addActionListener(this);
		ImageIcon imagenPreviousCopy = new ImageIcon("gvSIG/extensiones/es.udc.cartolab.gvsig.navtable/images/copy.png");
		previousCopyB = new JButton(imagenPreviousCopy);
		previousCopyB.setToolTipText(PluginServices.getText(this, "copyPreviousButtonTooltip"));
		previousCopyB.addActionListener(this);
		ImageIcon imagenZoom = new ImageIcon("gvSIG/extensiones/es.udc.cartolab.gvsig.navtable/images/zoom.png");
		zoomB = new JButton(imagenZoom);
		zoomB.setToolTipText(PluginServices.getText(this, "zoomButtonTooltip"));
		zoomB.addActionListener(this);
		ImageIcon imagenSelect = new ImageIcon("gvSIG/extensiones/es.udc.cartolab.gvsig.navtable/images/Select.png");
		selectionB = new JButton(imagenSelect);
		selectionB.setToolTipText(PluginServices.getText(this, "selectionButtonTooltip"));
		selectionB.addActionListener(this);
		ImageIcon imagenSave = new ImageIcon("gvSIG/extensiones/es.udc.cartolab.gvsig.navtable/images/save.png");
		saveB = new JButton(imagenSave);
		saveB.setToolTipText(PluginServices.getText(this, "saveButtonTooltip"));
		saveB.addActionListener(this);
		ImageIcon imagenClose = new ImageIcon("gvSIG/extensiones/es.udc.cartolab.gvsig.navtable/images/close.png");
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
		actionsToolBar.add(previousCopyB);
		actionsToolBar.add(zoomB);
		actionsToolBar.add(selectionB);
		actionsToolBar.add(saveB);
		actionsToolBar.add(cancelB);

		JPanel buttonsPanel = new JPanel(new FlowLayout());
		buttonsPanel.add(navToolBar);		
		buttonsPanel.add(actionsToolBar);

		return buttonsPanel;

	}

	/**
	 * Goes to the next row of the data.
	 *
	 */
	protected void next(){
		try {
			if (onlySelectedCB.isSelected()){
				nextSelected();
			} else {
				if (currentPosition < recordset.getRowCount()){				
					fillValues(currentPosition+1);
				}
			}
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
		if (pos != -1){
			fillValues(pos);	
		}		
	}

	/**
	 * Goes to the last row of the data.
	 *
	 */
	protected void last(){
		try {
			if (onlySelectedCB.isSelected()){
				lastSelected();
			} else {
				fillValues(recordset.getRowCount()-1);				
			}
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
			fillValues(pos-1);	
		}				
	}

	/**
	 * Goes to the first row of the data.
	 *
	 */
	protected void first(){
		if (onlySelectedCB.isSelected()){
			firstSelected();
		} else {
			fillValues(0);
		}	
	}

	/**
	 * Goes to the first selected row of the data.
	 *
	 */
	private void firstSelected(){
		FBitSet bitset= recordset.getSelection();		
		int pos = bitset.nextSetBit(0);
		if (pos != -1){
			fillValues(pos);	
		}
	}

	/**
	 * Goes to the previous row of the data.
	 *
	 */
	protected void before() {		
		if (onlySelectedCB.isSelected()){
			beforeSelected();
		} else {
			fillValues(currentPosition-1);
		}

	}

	/**
	 * Goes to the previos selected row of the data.
	 *
	 */
	protected void beforeSelected() {

		FBitSet bitset= recordset.getSelection();
		int currentPos = Long.valueOf(currentPosition).intValue()-1;
		int pos = currentPos;
		for (; pos>= 0 && !bitset.get(pos); pos--);		
		if (pos != -1){
			fillValues(pos);
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
		//TODO gvSIG comment: Esta comprobacion se hacia con Selectable
		if (layer instanceof AlphanumericData) {                                     
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
		}        

	}

	/**
	 * 
	 * @return true if the current row is selected, false if not.
	 */
	private boolean isRecordSelected() {

		FBitSet bitset = null;
		int pos = Long.valueOf(currentPosition).intValue();
		if (layer instanceof AlphanumericData) {
			//TODO Esta comprobacion se hacia con Selectable                                     
				if (recordset == null){
					return false;
				}
				bitset = recordset.getSelection();
				return bitset.get(pos);
		}        
		return false;

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

		if (!isRecordSelected()){
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
			viewInfo.setTitle(PluginServices.getText(this,
			"NavTable"));
			viewInfo.setWidth(560);
			viewInfo.setHeight(540);			
		}
		return viewInfo;
	}

	/**
	 * Repaints the window.
	 *
	 */
	protected void refreshGUI(){
		try {
			if (recordset == null){
				// TODO 
				// If there is some problem with the recordset don't do anything.
				return; 
			}
			long rows = recordset.getRowCount();
			totalLabel.setText("/"+rows);			
		}
		catch (com.hardcode.gdbms.engine.data.driver.DriverException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

		posTF.setText(String.valueOf(currentPosition+1));
		if (alwaysSelectCB.isSelected()){
			selectionB.setEnabled(false);
			clearSelection();
			selectCurrentFeature();
		} else {
			selectionB.setEnabled(true);
		}

		if (isRecordSelected()){
			ImageIcon imagenUnselect = new ImageIcon("gvSIG/extensiones/es.udc.cartolab.gvsig.navtable/images/Unselect.png");
			selectionB.setIcon(imagenUnselect);
			posTF.setBackground(Color.YELLOW);
		} else {
			ImageIcon imagenSelect = new ImageIcon("gvSIG/extensiones/es.udc.cartolab.gvsig.navtable/images/Select.png");
			selectionB.setIcon(imagenSelect);
			posTF.setBackground(Color.WHITE);
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
		if (e.getSource() == fixScaleCB) {
			alwaysZoomCB.setSelected(false);
		}
		if (e.getSource() == onlySelectedCB){
			alwaysSelectCB.setSelected(false);
			if (onlySelectedCB.isSelected()){
				viewOnlySelected();
			} 
		}
		if (e.getSource() == alwaysZoomCB){
			fixScaleCB.setSelected(false);
			//refreshGUI();
		}
		if (e.getSource() == alwaysSelectCB){
			onlySelectedCB.setSelected(false);
			//refreshGUI();
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
			String pos = posTF.getText();
			try {				
				long posNumber = Long.parseLong(pos);
				fillValues(posNumber-1);
			} catch (Exception e1){
				e1.printStackTrace();
			}
		}
		if (e.getSource() == previousCopyB){			
			fillValues(currentPosition-1);
			//TODO when it is extreme
			currentPosition = currentPosition + 1;
		}
		if (e.getSource() == zoomB){
			zoom();
		}
		if (e.getSource() == selectionB){
			selectCurrentFeature();
		}
		if (e.getSource() == cancelB){
			PluginServices.getMDIManager().closeWindow(this);
		}
		if (e.getSource() == saveB){
			saveRegister();
		}
		refreshGUI();

	}

}
