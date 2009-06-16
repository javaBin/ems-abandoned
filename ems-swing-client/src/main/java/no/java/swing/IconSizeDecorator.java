package no.java.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * @author <a href="mailto:yngvars@gmail.no">Yngvar S&oslash;rensen</a>
 */
public class IconSizeDecorator implements Icon {

    private final int width;
    private final int height;
    private final int scaledWidth;
    private final int scaledHeight;
    private final int translateX;
    private final int translateY;
    private final boolean renderOnce;
    private Icon icon;
    private Image image;

    public IconSizeDecorator(final Icon icon, final int width, final int height) {
        this(icon, width, height, 1f, true);
    }

    public IconSizeDecorator(final Icon icon, final int width, final int height, final float maxScaleFactor, final boolean renderOnce) {
        this.icon = icon;
        this.width = width;
        this.height = height;
        this.renderOnce = renderOnce;
        double scale = Math.min(Math.min(width / (double)icon.getIconWidth(), height / (double)icon.getIconHeight()), maxScaleFactor);
        translateX = Math.round(Math.round((width - icon.getIconWidth() * scale) / 2.0));
        translateY = Math.round(Math.round((height - icon.getIconHeight() * scale) / 2.0));
        scaledWidth = Math.round(Math.round(icon.getIconWidth() * scale));
        scaledHeight = Math.round(Math.round(icon.getIconHeight() * scale));
    }

    public void paintIcon(final Component component, final Graphics graphics, final int x, final int y) {
        if (image == null) {
            BufferedImage tempImage = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D tempGraphics = null;
            try {
                tempGraphics = tempImage.createGraphics();
                icon.paintIcon(component, tempGraphics, 0, 0);
            } finally {
                if (tempGraphics != null) {
                    tempGraphics.dispose();
                }
            }
            image = tempImage.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
        }
        graphics.drawImage(image, translateX, translateY, null);
        if (!renderOnce) {
            image = null;
        } else if (icon != null) {
            icon = null;
        }
    }

    public int getIconWidth() {
        return width;
    }

    public int getIconHeight() {
        return height;
    }

}
