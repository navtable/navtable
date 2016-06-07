package es.icarto.gvsig.navtable.gvsig2;

import java.util.Iterator;

import javax.swing.JOptionPane;

import org.gvsig.andami.IconThemeHelper;
import org.gvsig.andami.PluginServices;
import org.gvsig.andami.messages.NotificationManager;
import org.gvsig.andami.plugins.Extension;
import org.gvsig.app.ApplicationLocator;
import org.gvsig.app.ApplicationManager;
import org.gvsig.app.gui.filter.ExpressionListener;
import org.gvsig.app.gui.filter.FilterDialog;
import org.gvsig.app.project.documents.view.ViewDocument;
import org.gvsig.app.project.documents.view.gui.IView;
import org.gvsig.fmap.dal.DALLocator;
import org.gvsig.fmap.dal.DataManager;
import org.gvsig.fmap.dal.exception.DataException;
import org.gvsig.fmap.dal.feature.Feature;
import org.gvsig.fmap.dal.feature.FeatureQuery;
import org.gvsig.fmap.dal.feature.FeatureSelection;
import org.gvsig.fmap.dal.feature.FeatureSet;
import org.gvsig.fmap.dal.feature.FeatureStore;
import org.gvsig.fmap.mapcontext.layers.FLayer;
import org.gvsig.fmap.mapcontext.layers.vectorial.FLyrVect;
import org.gvsig.i18n.Messages;
import org.gvsig.utils.exceptionHandling.ExceptionListener;

/**
 * Extensión que abre un diálogo para poder hacer un filtro de una capa o tabla.
 * Forked from SelectByAttributesExtension
 */
public class SelectByAttributes implements ExpressionListener {

    private FeatureStore featureStore = null;
    private String filterTitle;

    public void execute() {
    	FilterDialog dlg = new FilterDialog(filterTitle);
        dlg.addExpressionListener(this);
        dlg.addExceptionListener(new ExceptionListener() {

            public void exceptionThrown(Throwable t) {
                NotificationManager.addError(t.getMessage(), t);
            }
        });
        dlg.setModel(featureStore);
        PluginServices.getMDIManager().addWindow(dlg);
    }

    // By Pablo: if no filter expression -> no element selected
    public void newSet(String expression) throws DataException {
        if (!this.filterExpressionFromWhereIsEmpty(expression)) {
            FeatureSet set = null;
            try {
                set = doSet(expression);

                if (set == null) {
                    // throw new RuntimeException("Not a 'where' clause?");
                    return;
                }
                featureStore.setSelection(set);

            } catch (Exception e) {

                JOptionPane.showMessageDialog(
                        ApplicationLocator.getManager().getRootComponent(),
                        Messages.getText("expresion_error") + ":\n"
                        + getLastMessage(e),
                        Messages.getText("expresion_error"),
                        JOptionPane.ERROR_MESSAGE);
            } finally {
                if (set != null) {
                    set.dispose();
                }
            }
        } else {
            // By Pablo: if no expression -> no element selected
            featureStore.getFeatureSelection().deselectAll();
        }
    }

    private FeatureSet doSet(String expression) throws DataException {
        FeatureQuery query = featureStore.createFeatureQuery();
        DataManager manager = DALLocator.getDataManager();
        query.setFilter(manager.createExpresion(expression));
        return featureStore.getFeatureSet(query);
    }

    public void addToSet(String expression) throws DataException {
        // By Pablo: if no filter expression -> don't add more elements to set
        if (!this.filterExpressionFromWhereIsEmpty(expression)) {
            FeatureSet set = null;
            try {
                set = doSet(expression);

                if (set == null) {
                    // throw new RuntimeException("Not a 'where' clause?");
                    return;
                }
                featureStore.getFeatureSelection().select(set);
            } finally {
                if (set != null) {
                    set.dispose();
                }
            }
        }
    }

    public void fromSet(String expression) throws DataException {
        // By Pablo: if no filter expression -> no element selected
        try {
            if (!this.filterExpressionFromWhereIsEmpty(expression)) {
                // NotificationManager.showMessageInfo("Falta por implementar",
                // null);

                FeatureSet set = null;
                set = doSet(expression);

                if (set == null) {
                    throw new RuntimeException("Not a 'where' clause?");
                }

                FeatureSelection oldSelection
                        = featureStore.getFeatureSelection();

                FeatureSelection newSelection
                        = featureStore.createFeatureSelection();
                Iterator iterator = set.iterator();
                while (iterator.hasNext()) {
                    Feature feature = (Feature) iterator.next();
                    if (oldSelection.isSelected(feature)) {
                        newSelection.select(feature);
                    }
                }
                featureStore.setSelection(newSelection);
                set.dispose();

            } else {
                // By Pablo: if no expression -> no element selected
                featureStore.getFeatureSelection().deselectAll();
                ;
            }
        } catch (DataException e) {
            NotificationManager.addError(e);
        }

    }

    /**
     * Returns true if the WHERE subconsultation of the filterExpression is
     * empty ("")
     *
     * @author Pablo Piqueras Bartolomé (p_queras@hotmail.com)
     * @param expression An string
     * @return A boolean value
     */
    private boolean filterExpressionFromWhereIsEmpty(String expression) {

        if (expression == null) {
            return true;
        }

        String subExpression = expression.trim();

        if (subExpression.length() == 0) {
            return true;
        }

        int pos;

        // Remove last ';' if exists
        if (subExpression.charAt(subExpression.length() - 1) == ';') {
            subExpression
                    = subExpression.substring(0, subExpression.length() - 1).trim();
        }

        // If there is no 'where' clause
        if ((pos = subExpression.indexOf("where")) == -1) {
            return false;
        }

        // If there is no subexpression in the WHERE clause -> true
        // + 5 is the length of 'where'
        subExpression = subExpression.substring(pos + 5, subExpression.length()).trim();
        if (subExpression.length() == 0) {
            return true;
        } else {
            return false;
        }
    }

    private String getLastMessage(Throwable ex) {

        Throwable p = ex;
        while (p.getCause() != null && p.getCause() != p) {
            p = p.getCause();
        }
        return p.getMessage();
    }

	public void setDatasource(FeatureStore featureStore, String dialogTitle) {
		this.featureStore = featureStore;
		this.filterTitle = dialogTitle;
	}
}
