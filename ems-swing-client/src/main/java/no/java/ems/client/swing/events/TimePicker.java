package no.java.ems.client.swing.events;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import no.java.swing.DefaultPanel;
import org.jdesktop.beansbinding.*;
import org.jdesktop.swingbinding.SwingBindings;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:erlend@escenic.com">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class TimePicker extends DefaultPanel {
    private JComboBox hourSelector;
    private JComboBox minuteSelector;    
    private List<Integer> hours = new ArrayList<Integer>();
    private List<Integer> minutes = new ArrayList<Integer>();
    private BindingGroup bindings = new BindingGroup();

    TimePicker() {
        initialize();
    }

    @Override
    public void initModels() {
        for (int i = 0; i < 24; i++) {
            hours.add(i);
        }
        minutes.add(0);
        minutes.add(15);
        minutes.add(30);
        minutes.add(45);
    }

    @Override
    public void initActions() {
    }

    @Override
    public void initComponents() {
        hourSelector = new JComboBox();
        minuteSelector = new JComboBox();
    }

    @Override
    public void initBindings() {
        bindings.addBinding(SwingBindings.createJComboBoxBinding(AutoBinding.UpdateStrategy.READ, hours, hourSelector));
        bindings.addBinding(SwingBindings.createJComboBoxBinding(AutoBinding.UpdateStrategy.READ, minutes, minuteSelector));
    }

    @Override
    public void initListeners() {
    }

    @Override
    public void initLayout() {
        setLayout(new FormLayout("p,p","f:p"));
        CellConstraints cc = new CellConstraints();
        add(hourSelector, cc.xy(1,1));
        add(minuteSelector, cc.xy(2,1));
    }

    @Override
    public void initState() {
        bindings.bind();
        setSelectedTime(new LocalTime());
    }

    private int findSelectedMinuteIndex(LocalTime selected) {
        int minuteOfHour = selected.getMinuteOfHour();
        int index = 0;
        if (minuteOfHour <= 11 || minuteOfHour >= 51) {
            index = 0;
        }
        if (minuteOfHour >= 12 || minuteOfHour <= 21) {
            index = 1;
        }
        if (minuteOfHour >= 22 || minuteOfHour >= 37) {
            index = 2;
        }
        if (minuteOfHour >= 38 || minuteOfHour >= 50) {
            index = 3;
        }
        return index;
    }

    public void setSelectedTime(LocalTime selected) {
        minuteSelector.setSelectedIndex(findSelectedMinuteIndex(selected));
        hourSelector.setSelectedItem(selected.getHourOfDay());
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        hourSelector.setEnabled(enabled);
        minuteSelector.setEnabled(enabled);
    }

    public LocalTime getSelectedTime() {
        return new LocalTime().
                withHourOfDay(hours.get(hourSelector.getSelectedIndex())).
                withMinuteOfHour(minutes.get(minuteSelector.getSelectedIndex())).
                withSecondOfMinute(0).
                withMillisOfSecond(0);
    }
}
