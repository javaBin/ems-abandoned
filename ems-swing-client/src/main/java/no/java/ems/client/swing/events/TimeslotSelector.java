package no.java.ems.client.swing.events;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import no.java.swing.DefaultPanel;
import no.java.ems.domain.Event;
import org.joda.time.*;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:erlend@escenic.com">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class TimeslotSelector extends DefaultPanel {
    private Duration duration = Minutes.minutes(15).toStandardDuration();

    private TimePicker startTimePicker;
    private TimePicker endTimePicker;
    private final Event event;
    private Action generateTimeSlotsAction;
    private PropertyChangeListener dateListener;

    protected TimeslotSelector(Event event) {
        this.event = event;
        initialize();
    }

    @Override
    public void initModels() {
    }

    @Override
    public void initActions() {
        generateTimeSlotsAction = new DefaultAction("GenerateTimeSlotAction") {
            @Override
            public void actionPerformed(ActionEvent e) {
                event.setTimeslots(generateTimeSlots());
                System.out.println("event.getTimeslots() = " + event.getTimeslots());
            }
        };
    }

    @Override
    public void initComponents() {
        startTimePicker = new TimePicker();
        endTimePicker = new TimePicker();
    }

    @Override
    public void initBindings() {
    }

    @Override
    public void initListeners() {
        dateListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                syncEnabledState();
            }
        };
        addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
                TimeslotSelector.this.event.addPropertyChangeListener(dateListener);                
            }

            @Override
            public void ancestorRemoved(AncestorEvent event) {
                TimeslotSelector.this.event.removePropertyChangeListener(dateListener);
            }

            @Override
            public void ancestorMoved(AncestorEvent event) {
            }
        });
    }

    private void syncEnabledState() {
        if (event.getStartDate() != null && event.getEndDate() != null) {
            setEnabled(true);
            generateTimeSlotsAction.setEnabled(true);
        }
        else {
            setEnabled(false);
            generateTimeSlotsAction.setEnabled(false);
        }

    }

    @Override
    public void initLayout() {
        CellConstraints cc = new CellConstraints();
        setLayout(new FormLayout("f:p", "f:p, 5dlu, f:p"));

        JPanel timePickerPanel = new JPanel(new FormLayout("d, 3dlu, f:p, 5dlu, d, 3dlu, f:p", "f:p"));
        timePickerPanel.add(createLabel("startTime", startTimePicker), cc.xy(1, 1));
        timePickerPanel.add(startTimePicker, cc.xy(3, 1));
        timePickerPanel.add(createLabel("endTime", endTimePicker), cc.xy(5, 1));
        timePickerPanel.add(endTimePicker, cc.xy(7, 1));
        add(timePickerPanel, cc.xy(1, 1));
        add(new JButton(generateTimeSlotsAction), cc.xy(1, 3));
    }

    @Override
    public void initState() {
        LocalTime now = new LocalTime();
        startTimePicker.setSelectedTime(now);
        endTimePicker.setSelectedTime(now);
        syncEnabledState();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        startTimePicker.setEnabled(enabled);
        endTimePicker.setEnabled(enabled);

    }

    private List<Interval> generateTimeSlots() {
        List<Interval> intervals = new ArrayList<Interval>();
        LocalTime time = startTimePicker.getSelectedTime();
        LocalTime endTime = endTimePicker.getSelectedTime();
        int days = findNumberOfDaysInEvent();
        if (!time.isBefore(endTime) || !time.isEqual(endTime)) {
            for (int i = 0; i < days; i++) {
                LocalDate today = event.getStartDate().plusDays(i);
                DateTime actual = today.toDateTime(time);
                DateTime endDateTime = today.toDateTime(endTime);

                while ((actual.isBefore(endDateTime))) {
                    intervals.add(new Interval(actual, duration));
                    actual = actual.plus(duration);
                }
            }
        }

        return intervals;
    }

    private int findNumberOfDaysInEvent() {
        Days days = Days.daysBetween(event.getStartDate(), event.getEndDate());
        return days.getDays() + 1;
    }
}
