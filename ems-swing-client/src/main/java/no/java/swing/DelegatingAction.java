package no.java.swing;

import org.jdesktop.application.Application;
import org.jdesktop.application.ApplicationAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author <a href="mailto:yngvars@gmail.com">Yngvar S&oslash;rensen</a>
 */
public class DelegatingAction extends ApplicationAction {

    private static final Set<DelegatingAction> INSTANCES = new CopyOnWriteArraySet<DelegatingAction>();
    private static Component focusOwner;

    static {
        SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener(
                                "permanentFocusOwner",
                                new FocusedOwnerListener()
                        );
                    }
                }
        );
    }

    private static void updateDelegates() {
        Window windfocusOwnersWindow = focusOwner == null ? null : getWindow(focusOwner);
        for (DelegatingAction action : INSTANCES) {
            if (action.context == null || getWindow(action.context) == windfocusOwnersWindow) {
                action.setDelegate(findDelegate(action.actionKey, focusOwner));
            }
        }
    }

    private static Window getWindow(final Component component) {
        return (Window)(component instanceof Window ? component : SwingUtilities.getWindowAncestor(component));
    }

    private static Action findDelegate(final Object actionKey, final Component component) {
        if (component instanceof JComponent) {
            Action delegate = ((JComponent)component).getActionMap().get(actionKey);
            if (delegate != null) {
                return delegate;
            }
        }
        return component == null ? null : findDelegate(actionKey, component.getParent());
    }

    private final Map<String, Object> defaults;
    private final String actionKey;
    private Action delegate;
    private PropertyChangeListener delegateListener;
    private Component context;

    public DelegatingAction(final String actionKey, final Component context) {
        super(
                Application.getInstance().getContext().getActionMap(),
                Application.getInstance().getContext().getResourceMap(DelegatingAction.class),
                actionKey,
                null,
                null,
                null,
                null
        );
        Application.getInstance().getContext().getActionMap().put(actionKey, this);
        this.actionKey = actionKey;
        this.context = context;
        defaults = new HashMap<String, Object>();
        Object[] keys = getKeys();
        if (keys != null) {
            for (Object key : keys) {
                if (key instanceof String) {
                    defaults.put((String)key, getValue((String)key));
                }
            }
        }
        delegateListener = new PropertyChangeListener() {
            public void propertyChange(final PropertyChangeEvent event) {
                syncWithDelegate();
            }
        };
        setEnabled(false);
        INSTANCES.add(this);
    }

    public Action getDelegate() {
        return delegate;
    }

    public void setDelegate(final Action delegate) {
        if (this.delegate != null) {
            this.delegate.removePropertyChangeListener(delegateListener);
        }
        this.delegate = delegate;
        if (this.delegate != null) {
            this.delegate.addPropertyChangeListener(delegateListener);
        }
        syncWithDelegate();
    }

    private void syncWithDelegate() {
        setEnabled(delegate != null && delegate.isEnabled());
        syncProperties(Action.SHORT_DESCRIPTION, Action.LONG_DESCRIPTION, Action.SELECTED_KEY);
    }

    private void syncProperties(final String... keys) {
        for (String key : keys) {
            Object defaultValue = defaults.get(key);
            Object delegateValue = delegate == null ? null : delegate.getValue(key);
            putValue(key, delegateValue == null ? defaultValue : delegateValue);
        }
    }

    @Override
    public void actionPerformed(final ActionEvent actionEvent) {
        if (delegate != null) {
            actionEvent.setSource(focusOwner);
            delegate.actionPerformed(actionEvent);
        }
    }

    private static class FocusedOwnerListener implements PropertyChangeListener {

        public void propertyChange(final PropertyChangeEvent event) {
            final Component newFocusOwner = (Component)event.getNewValue();
            if (newFocusOwner != null && focusOwner != newFocusOwner) {
                focusOwner = newFocusOwner;
                updateDelegates();
            }
        }

    }

}
