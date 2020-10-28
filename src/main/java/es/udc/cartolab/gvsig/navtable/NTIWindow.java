package es.udc.cartolab.gvsig.navtable;

import java.awt.Dimension;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import org.gvsig.andami.Launcher;
import org.gvsig.andami.PluginServices;
import org.gvsig.andami.ui.mdiFrame.MDIFrame;
import org.gvsig.andami.ui.mdiManager.IWindow;
import org.gvsig.andami.ui.mdiManager.WindowInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.icarto.gvsig.navtable.windowproperties.FormWindowProperties;
import es.icarto.gvsig.navtable.windowproperties.FormWindowPropertiesSerializator;

@SuppressWarnings("serial")
public abstract class NTIWindow extends JPanel implements IWindow {

	private static final Logger logger = LoggerFactory.getLogger(NTIWindow.class);

	private WindowInfo windowInfo = null;
	private String title;
	private FormWindowProperties props = null;

	protected void setTitle(String title) {
		this.title = title;
	}

	protected void setProps(FormWindowProperties props) {
		this.props = props;
	}

	@Override
	public Object getWindowProfile() {
		return null;
	}

	@Override
	public WindowInfo getWindowInfo() {
		if (windowInfo == null) {
			windowInfo = new WindowInfo(WindowInfo.MODELESSDIALOG | WindowInfo.PALETTE | WindowInfo.RESIZABLE);

			if (title != null) {
				windowInfo.setTitle(title);
			}
			Dimension dim = getPreferredSize();
			// To calculate the maximum size of a form we take the size of the
			// main frame minus a "magic number" for the menus, toolbar, state
			// bar
			// Take into account that in edition mode there is less available
			// space
			MDIFrame a = (MDIFrame) PluginServices.getMainFrame();
			final int MENU_TOOL_STATE_BAR = 205;
			int maxHeight = a.getHeight() - MENU_TOOL_STATE_BAR;
			int maxWidth = a.getWidth() - 15;

			int width, heigth = 0;
			if (dim.getHeight() > maxHeight) {
				heigth = maxHeight;
			} else {
				heigth = new Double(dim.getHeight()).intValue();
			}
			if (dim.getWidth() > maxWidth) {
				width = maxWidth;
			} else {
				width = new Double(dim.getWidth()).intValue();
			}

			// getPreferredSize doesn't take into account the borders and other
			// stuff
			// introduced by Andami, neither scroll bars so we must increase the
			// "preferred"
			// dimensions
			windowInfo.setWidth(width + 25);
			windowInfo.setHeight(heigth + 15);

			if (props != null) {
				updateFromProps();
			}
		}
		return windowInfo;
	}

	private void updateFromProps() {
		// WindowInfoSupport.getWindowInfo adds 40 to the
		// getWindowInfo declared
		// by IWindow objects
		final int ANDAMI_CORRECTION = 40;
		windowInfo.setHeight(props.getFormWindowHeight() - ANDAMI_CORRECTION);
		windowInfo.setWidth(props.getFormWindowWidth());
		windowInfo.setX(props.getFormWindowXPosition());
		windowInfo.setY(props.getFormWindowYPosition());
	}

	protected void writeFormWindowProperties() {
		boolean update = false;
		List<FormWindowProperties> formWindowPropertiesList = getFormWindowProperties();
		for (FormWindowProperties fwp : formWindowPropertiesList) {
			if (fwp.getFormName().equalsIgnoreCase(getClass().getName())) {
				fwp.setFormWindowHeight(windowInfo.getHeight());
				fwp.setFormWindowWidth(windowInfo.getWidth());
				fwp.setFormWindowXPosition(windowInfo.getX());
				fwp.setFormWindowYPosition(windowInfo.getY());
				update = true;
				break;
			}
		}

		if (!update) {
			FormWindowProperties fwpToAdd = new FormWindowProperties();
			fwpToAdd.setFormName(getClass().getName());
			fwpToAdd.setFormWindowHeight(windowInfo.getHeight());
			fwpToAdd.setFormWindowWidth(windowInfo.getWidth());
			fwpToAdd.setFormWindowXPosition(windowInfo.getX());
			fwpToAdd.setFormWindowYPosition(windowInfo.getY());
			formWindowPropertiesList.add(fwpToAdd);
		}

		String xml = FormWindowPropertiesSerializator.toXML(formWindowPropertiesList);
		try {
			FileWriter fileWriter = new FileWriter(new File(getFormWindowPropertiesXMLPath()));
			Writer writer = new BufferedWriter(fileWriter);
			writer.write(xml);
			writer.close();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	protected List<FormWindowProperties> getFormWindowProperties() {
		if (!new File(getFormWindowPropertiesXMLPath()).exists()) {
			return new ArrayList<FormWindowProperties>();
		} else {
			return FormWindowPropertiesSerializator.fromXML(new File(getFormWindowPropertiesXMLPath()));
		}
	}

	protected String getFormWindowPropertiesXMLPath() {
		return Launcher.getAppHomeDir() + File.separator + "FormWindowProperties.xml";
	}
}
