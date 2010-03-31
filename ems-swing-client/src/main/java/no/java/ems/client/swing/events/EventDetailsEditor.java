package no.java.ems.client.swing.events;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import no.java.ems.client.swing.EntityEditor;
import no.java.ems.domain.Event;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.beansbinding.Converter;
import org.jdesktop.swingx.JXDatePicker;
import org.joda.time.DateMidnight;
import org.joda.time.LocalDate;

import javax.swing.*;
import java.util.Date;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class EventDetailsEditor extends EntityEditor<Event> {
    private JXDatePicker startDateSelector;
    private JXDatePicker endDateSelector;

    protected EventDetailsEditor(final Event entity) {
        super(entity, "name");
        initialize();
        getResourceMap().injectComponents(this);
    }

    @Override
    public void initModels() {
    }

    @Override
    public void initActions() {
    }

    @Override
    public void initComponents() {
        super.initComponents();
        startDateSelector = new JXDatePicker();
        startDateSelector.setFormats("dd.MM.yyyy");
        endDateSelector = new JXDatePicker();
        endDateSelector.setFormats("dd.MM.yyyy");
    }

    @Override
    public void initBindings() {
        super.initBindings();
        getBindingGroup().addBinding(createDateBinding("startDate"));
        getBindingGroup().addBinding(createDateBinding("endDate"));
    }

    private AutoBinding<Event, LocalDate, JXDatePicker, Date> createDateBinding(final String property) {
        AutoBinding<Event, LocalDate, JXDatePicker, Date> dateBinding = Bindings.createAutoBinding(
                AutoBinding.UpdateStrategy.READ_WRITE,
                getEntity(),
                BeanProperty.<Event, LocalDate>create(property),
                startDateSelector,
                BeanProperty.<JXDatePicker, Date>create("date")
        );
        dateBinding.setConverter(new DateConverter());
        return dateBinding;
    }

    @Override
    public void initLayout() {
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setLayout(new FormLayout("d, 5dlu, p", "f:p, 3dlu, f:p, 3dlu, f:p, 3dlu, f:p"));
        CellConstraints cc = new CellConstraints();
        add(createLabel(titleProperty, titleField), cc.xy(1, 1));
        add(titleField, cc.xy(3, 1));
        add(createLabel("startDate", startDateSelector), cc.xy(1, 3));
        add(startDateSelector, cc.xy(3, 3));
        add(createLabel("endDate", endDateSelector), cc.xy(1, 5));
        add(endDateSelector, cc.xy(3, 5));
    }

    private static class DateConverter extends Converter<LocalDate, Date> {
        @Override
        public Date convertForward(LocalDate value) {
            return new Date(value.toDateMidnight().getMillis());
        }

        @Override
        public LocalDate convertReverse(Date value) {
            return new DateMidnight(value.getTime()).toLocalDate();
        }
    }
}
