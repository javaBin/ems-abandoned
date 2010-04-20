package no.java.ems.client.swing;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * @author <a href="mailto:yngvars@gmail.no">Yngvar S&oslash;rensen</a>
 */
public class ApplicationHeader extends JComponent {

    private BufferedImage logo;
    private BufferedImage gradient;

    public ApplicationHeader() {
        try {
            logo = ImageIO.read(getClass().getResource("/gfx/logo.png"));
            gradient = ImageIO.read(getClass().getResource("/gfx/gradient.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        setPreferredSize(new Dimension(0, 80));
    }

    @Override
    protected void paintComponent(final Graphics g) {
        if (logo != null && gradient != null) {
            g.drawImage(gradient, 0, 0, getWidth(), 80, null);
            g.drawImage(logo, 0, 0, null);
        }
    }

}
