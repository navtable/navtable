package es.udc.cartolab.gvsig.navtable.contextualmenu;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRootPane;
import javax.swing.JTextField;

import com.iver.andami.PluginServices;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.andami.ui.mdiManager.IWindowListener;
import com.iver.andami.ui.mdiManager.WindowInfo;
import com.iver.cit.gvsig.FiltroExtension;

import es.udc.cartolab.gvsig.navtable.NavTable;

public class StringFilterActionListener extends JPanel implements ActionListener, IWindow, IWindowListener {

	NavTable navtable;
	String st_expr;
	String attrValue;
	FiltroExtension filterExt;

	JRadioButton rbContainsWith;
	JRadioButton rbStartsWith;
	JRadioButton rbEndsWith;
	
	JCheckBox cbIgnoreCase;
	JCheckBox cbIgnoreAcutes;

	WindowInfo windowInfo;
	private JRootPane jRootPane;
	private JButton okBtn;

	public StringFilterActionListener(final NavTable navtable,
			final String attrValue,
			final String st_expr, 
			final FiltroExtension filterExt) {

		super();
		
		this.navtable = navtable;
		this.st_expr = st_expr;
		this.attrValue = attrValue;
		this.filterExt = filterExt;

		initGUI();
	}

	private void initGUI() {

		windowInfo = this.getWindowInfo();
		
		add(new JLabel(PluginServices.getText(this,
		"nt_filter_write_string")));
		final JTextField tf = new JTextField(16);
		tf.setText(attrValue);
		tf.setSelectionStart(0);
		tf.setSelectionEnd(attrValue.length());
		tf.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String attr = tf.getText();
				executeFilter(attr);
				PluginServices.getMDIManager().closeWindow(StringFilterActionListener.this);
			}
		});
		add(tf);
		
		JPanel rbPanel = new JPanel(new GridLayout(3, 1));
				
		rbContainsWith = new JRadioButton(PluginServices.getText(this,
		"filter_contains"));
		rbContainsWith.setSelected(true);
		rbStartsWith = new JRadioButton(PluginServices.getText(this,
		"filter_startswith"));
		rbEndsWith = new JRadioButton(PluginServices.getText(this,
		"filter_endswith"));

		ButtonGroup bg = new ButtonGroup();
		bg.add(rbContainsWith);
		bg.add(rbStartsWith);
		bg.add(rbEndsWith);
		rbPanel.add(rbContainsWith);
		rbPanel.add(rbStartsWith);
		rbPanel.add(rbEndsWith);
		add(rbPanel);
		
		JPanel cbPanel = new JPanel(new GridLayout(2, 1));

		cbIgnoreCase = new JCheckBox(PluginServices.getText(this,
		"filter_ignorecase"));
		cbIgnoreAcutes = new JCheckBox(PluginServices.getText(this,
		"filter_ignoreacutesvowels"));

		cbPanel.add(cbIgnoreCase);
		cbPanel.add(cbIgnoreAcutes);
		add(cbPanel);

		JPanel btnPanel = new JPanel();
		JButton okBtn = new JButton(PluginServices.getText(this,"ok"));
		okBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String attr = tf.getText();
				executeFilter(attr);
				PluginServices.getMDIManager().closeWindow(StringFilterActionListener.this);
			}
		});
		btnPanel.add(okBtn);
		
		JButton cancelBtn = new JButton(PluginServices.getText(this,"cancel"));
		cancelBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				PluginServices.getMDIManager().closeWindow(StringFilterActionListener.this);
			}
		});
		btnPanel.add(cancelBtn);
		add(btnPanel);		
	}

	private String ignoreCaseAndAcutesVowels(String attr) {
	    String aux = "";
	    for (char c : attr.toCharArray()) {
		switch(c) {
		    case 'a':
		    case 'A':
		    case 'á':
		    case 'Á':
			aux += "[aAáÁ]";
			break;
		    case 'e':
		    case 'E':
		    case 'é':
		    case 'É':
			aux += "[eEéÉ]";
			break;
		    case 'i':
		    case 'I':
		    case 'í':
		    case 'Í':
			aux += "[iIíÍ]";
			break;
		    case 'o':
		    case 'O':
		    case 'ó':
		    case 'Ó':
			aux += "[oOóÓ]";
			break;
		    case 'u':
		    case 'U':
		    case 'ú':
		    case 'Ú':
			aux += "[uUúÚ]";
			break;
		    default:
			aux += "[" + Character.toLowerCase(c) + Character.toUpperCase(c) + "]";
			break;
		}
	    }
	    return aux;
	}

	private String ignoreCase(String attr) {
	    String aux = "";
	    for (char c : attr.toCharArray()) {
		switch(c) {
		    case 'a':
		    case 'A':
			aux += "[aA]";
			break;
		    case 'á':
		    case 'Á':
			aux += "[áÁ]";
			break;
		    case 'e':
		    case 'E':
			aux += "[eE]";
			break;
		    case 'é':
		    case 'É':
			aux += "[éÉ]";
			break;
		    case 'i':
		    case 'I':
			aux += "[iI]";
			break;
		    case 'í':
		    case 'Í':
			aux += "[íÍ]";
			break;
		    case 'o':
		    case 'O':
			aux += "[oO]";
			break;
		    case 'ó':
		    case 'Ó':
			aux += "[óÓ]";
			break;
		    case 'u':
		    case 'U':
			aux += "[uU]";
			break;
		    case 'ú':
		    case 'Ú':
			aux += "[úÚ]";
			break;
		    default:
			aux += "[" + Character.toLowerCase(c) + Character.toUpperCase(c) + "]";
			break;
		}
	    }
	    return aux;
	}

	private String ignoreAcutesVowels(String attr) {
	    String aux = "";
	    for (char c : attr.toCharArray()) {
		switch(c) {
		    case 'a':
		    case 'á':
			aux += "[aá]";
			break;
		    case 'A':
		    case 'Á':
			aux += "[AÁ]";
			break;
		    case 'e':
		    case 'é':
			aux += "[eé]";
			break;
		    case 'E':
		    case 'É':
			aux += "[EÉ]";
			break;
		    case 'i':
		    case 'í':
			aux += "[ií]";
			break;
		    case 'I':
		    case 'Í':
			aux += "[IÍ]";
			break;
		    case 'o':
		    case 'ó':
			aux += "[oó]";
			break;
		    case 'O':
		    case 'Ó':
			aux += "[OÓ]";
			break;
		    case 'u':
		    case 'ú':
			aux += "[uú]";
			break;
		    case 'U':
		    case 'Ú':
			aux += "[UÚ]";
			break;
		    default:
			aux += "[" + c + "]";
			break;
		}
	    }
	    return aux;
	}

	private void executeFilter(final String attr){
		if (attr != null) {
			// TODO: We need to escape special characters
			// like '%', "'", ...
			String expr = "";
			String aux = "";

			if (cbIgnoreAcutes.isSelected()) {
			    if (cbIgnoreCase.isSelected()) {
				aux = ignoreCaseAndAcutesVowels(attr);
			    } else {
				aux = ignoreAcutesVowels(attr);
			    }
			} else {
			    if (cbIgnoreCase.isSelected()) {
				aux = ignoreCase(attr);
			    } else {
				aux = attr;
			    }
			}

			if (rbStartsWith.isSelected()) {
				expr = st_expr + " like '" + aux + "%';";
			} else if (rbEndsWith.isSelected()) {
				expr = st_expr + " like '%" + aux + "';";
			} else {
				//rbContains.isSelected()
				expr = st_expr + " like '%" + aux + "%';";
			}
			filterExt.newSet(expr);
			navtable.setOnlySelected(true);
		}
	}

	public void actionPerformed(ActionEvent evt) {
		PluginServices.getMDIManager().addWindow(this);
	}

	public WindowInfo getWindowInfo() {
		if (windowInfo ==null){
			windowInfo = new WindowInfo(WindowInfo.MODALDIALOG
				| WindowInfo.PALETTE);
			windowInfo.setTitle(PluginServices.getText(this,
				"filter_contains_window_title"));
			windowInfo.setWidth(220);
			windowInfo.setHeight(150);
		}
		return windowInfo;
	}

	public Object getWindowProfile() {
		return WindowInfo.DIALOG_PROFILE;
	}
	
	public void windowActivated() {
            if (jRootPane == null) {
			jRootPane = this.getRootPane();
                    if (jRootPane != null) {
                            jRootPane.setDefaultButton(okBtn);
                    }
            }
	}

	public void windowClosed() {
	}

}