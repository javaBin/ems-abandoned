package no.java.swing;

import javax.swing.*;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 * @author <a href="mailto:yngvars@gmail.no">Yngvar S&oslash;rensen</a>
 */
public interface InitSequence {

    void initModels();

    void initActions();

    void initComponents();

    void initBindings();

    void initListeners();

    void initLayout();

    void initState();

    JComponent getComponent();
}
