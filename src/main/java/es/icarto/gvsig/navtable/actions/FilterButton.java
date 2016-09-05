package es.icarto.gvsig.navtable.actions;

import static es.icarto.gvsig.commons.i18n.I18n._;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import org.gvsig.fmap.dal.exception.DataException;
import org.gvsig.fmap.dal.feature.FeatureSelection;
import org.gvsig.fmap.mapcontext.layers.vectorial.FLyrVect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.icarto.gvsig.commons.gvsig2.SelectByAttributes;
import es.udc.cartolab.gvsig.navtable.AbstractNavTable;

public class FilterButton implements ActionListener {

	private static final Logger logger = LoggerFactory
			.getLogger(FilterButton.class);

	public final JButton filterB;
	private final FLyrVect layer;

	public FilterButton(FLyrVect layer) {
		this.layer = layer;
		filterB = getNavTableButton("/filter.png", "filterTooltip");
	}

	public void refreshGUI() {
		if (selectionIsEmpty()) {
			ImageIcon imagenFilter = getIcon("/filter.png");
			filterB.setIcon(imagenFilter);
			filterB.setToolTipText(_("filterTooltip"));
		} else {
			ImageIcon imagenRemoveFilter = getIcon("/nofilter.png");
			filterB.setIcon(imagenRemoveFilter);
			filterB.setToolTipText(_("noFilterTooltip"));
		}
	}

	// Probably should be removed and use a factory instead
	// is duplicated with NavigationHandler
	private JButton getNavTableButton(String iconName, String toolTipName) {
		JButton but = new JButton(getIcon(iconName));
		but.setToolTipText(_(toolTipName));
		but.addActionListener(this);
		return but;
	}

	public ImageIcon getIcon(String iconName) {
		java.net.URL imgURL = getClass().getResource("/images/" + iconName);
		if (imgURL == null) {
			imgURL = AbstractNavTable.class.getResource("/images/" + iconName);
		}

		ImageIcon icon = new ImageIcon(imgURL);
		return icon;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		filterButtonClicked();
	}

	private void filterButtonClicked() {
		if (selectionIsEmpty()) {
			SelectByAttributes fe = new SelectByAttributes();
			fe.setDatasource(layer.getFeatureStore(), layer.getName());
			fe.execute();
		} else {
			clearSelection();
		}
	}

	private boolean selectionIsEmpty() {
		try {
			FeatureSelection selection = layer.getFeatureStore()
					.getFeatureSelection();
			return selection.isEmpty();
		} catch (DataException e) {
			logger.error(e.getMessage(), e);
		}
		return false;
	}

	private void clearSelection() {
		try {
			FeatureSelection selection = layer.getFeatureStore()
					.getFeatureSelection();
			selection.deselectAll();
		} catch (DataException e) {
			logger.error(e.getMessage(), e);
		}
	}

}
