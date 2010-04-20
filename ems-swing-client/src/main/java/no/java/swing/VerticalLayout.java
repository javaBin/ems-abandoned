package no.java.swing;

import com.jgoodies.forms.layout.ConstantSize;
import com.jgoodies.forms.layout.Sizes;
import org.apache.commons.lang.Validate;

import java.awt.*;

/**
 * VerticalLayout is a layout manager that stacks coponents vertically top down.
 * All components get the same width as the one with the greates preferred width.
 * All components get their preferred height.
 *
 * @author <a href="mailto:yngvars@gmail.no">Yngvar S&oslash;rensen</a>
 */
public class VerticalLayout implements LayoutManager {

    private final ConstantSize componentGapInDLU;

    public VerticalLayout() {
        this(0);
    }

    public VerticalLayout(final int verticalGapAsDLU) {
        Validate.isTrue(verticalGapAsDLU >= 0, "Vertical gap must be >= 0: " + verticalGapAsDLU);
        componentGapInDLU = Sizes.dluY(verticalGapAsDLU);
    }

    public void layoutContainer(final Container container) {
        Insets insets = container.getInsets();
        int x = insets.left;
        int y = insets.top;
        int width = container.getWidth() - insets.left - insets.right;
        int gap = componentGapInDLU.getPixelSize(container);
        for (Component component : container.getComponents()) {
            int height = component.getPreferredSize().height;
            component.setBounds(x, y, width, height);
            y += height + gap;
        }
    }

    public Dimension preferredLayoutSize(final Container container) {
        int width = 0;
        int height = 0;
        int gap = componentGapInDLU.getPixelSize(container);
        for (Component component : container.getComponents()) {
            // Need gap if not first component
            if (height > 0) {
                height += gap;
            }
            Dimension size = component.getPreferredSize();
            width = Math.max(width, size.width);
            height += size.height;
        }
        Insets insets = container.getInsets();
        return new Dimension(
                width + insets.left + insets.right,
                height + insets.top + insets.bottom
        );
    }

    public Dimension minimumLayoutSize(final Container container) {
        return preferredLayoutSize(container);
    }

    public void removeLayoutComponent(final Component component) {
    }

    public void addLayoutComponent(final String name, final Component component) {
    }

}
