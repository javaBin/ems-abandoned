package no.java.swing;

import org.apache.commons.lang.Validate;
import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.TimingTargetAdapter;
import org.jdesktop.application.Application;
import org.jdesktop.application.SessionStorage;

import javax.swing.*;
import java.awt.*;

/**
 * @author <a href="mailto:yngvars@gmail.no">Yngvar S&oslash;rensen</a>
 */
public class ExpandablePanel extends JPanel {

    public enum Direction {

        Up,
        Down,
        Left,
        Right

    }

    public static final String EXPANDED_PROPERTY = "expanded";

    private final Direction direction;
    private Animator animator;
    private boolean expanded = true;
    private float percentVisible = 1f;

    public ExpandablePanel(final JComponent contents, final Direction direction) {
        Validate.notNull(contents, "Contents may not be null");
        Validate.notNull(direction, "Direction may not be null");
        setLayout(new BorderLayout());
        this.direction = direction;
        super.addImpl(contents, null, 0);
    }

    public Direction getDirection() {
        return direction;
    }

    @Override
    protected void addImpl(final Component component, final Object constraints, final int index) {
        throw new IllegalArgumentException("Please do not add components directly to this component.");
    }

    @Override
    public void doLayout() {
        Insets i = getInsets();
        Component child = getComponent(0);
        Dimension size = child.getPreferredSize();
        switch (direction) {
            case Up:
                child.setSize(getWidth() - i.left - i.right, size.height);
                child.setLocation(i.left, getHeight() - i.bottom - Math.round(child.getHeight() * percentVisible));
                break;
            case Down:
                child.setSize(getWidth() - i.left - i.right, size.height);
                child.setLocation(i.left, i.top - Math.round(child.getHeight() * (1f - percentVisible)));
                break;
            case Left:
                child.setSize(size.width, getHeight() - i.top - i.bottom);
                child.setLocation(getWidth() - i.right - Math.round(child.getWidth() * percentVisible), i.top);
                break;
            case Right:
                child.setSize(size.width, getHeight() - i.top - i.bottom);
                child.setLocation(i.left - Math.round(child.getWidth() * (1f - percentVisible)), i.top);
                break;
        }
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension size = getComponent(0).getPreferredSize();
        switch (direction) {
            case Up:
            case Down:
                size.height = Math.round(size.height * percentVisible);
                break;
            case Left:
            case Right:
                size.width = Math.round(size.width * percentVisible);
                break;
        }
        Insets insets = getInsets();
        size.width += insets.left + insets.right;
        size.height += insets.top + insets.bottom;
        return size;
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(final boolean expanded) {
        setExpanded(expanded, true);
    }

    public void setExpanded(final boolean expanded, final boolean animate) {
        if (this.expanded == expanded) {
            return;
        }
        if (animator != null) {
            animator.stop();
            animator = null;
        }
        this.expanded = expanded;
        if (animate) {
            animator = new Animator(
                    Math.round(200),
                    new TimingTargetAdapter() {
                        @Override
                        public void timingEvent(float fraction) {
                            percentVisible = isExpanded() ? fraction : 1f - fraction;
                            setVisible(percentVisible > 0f);
                            revalidate();
                            if (percentVisible == 1f) {
                                scrollRectToVisible(getBounds());
                            }
                        }
                    }
            );
            animator.setDeceleration(.5f);
            animator.setStartFraction(isExpanded() ? percentVisible : 1f - percentVisible);
            animator.start();
        } else {
            percentVisible = expanded ? 1f : 0f;
            setVisible(percentVisible > 0f);
            revalidate();
            if (percentVisible == 1f) {
                scrollRectToVisible(getBounds());
            }
        }
        firePropertyChange(EXPANDED_PROPERTY, !expanded, expanded);
    }

    /**
     * Session storage handler for persisting the component's state between session.
     */
    private static class SessionHandler implements SessionStorage.Property {

        public Object getSessionState(final Component component) {
            return ((ExpandablePanel)component).isExpanded();
        }

        public void setSessionState(final Component component, final Object state) {
            ((ExpandablePanel)component).setExpanded(Boolean.TRUE.equals(state), false);
        }

    }

    static {
        // Registers SessionHandler as the SessionStorage.Property for expandable panels.
        Application.getInstance().getContext().getSessionStorage().putProperty(
                ExpandablePanel.class,
                new SessionHandler()
        );
    }

}
