/*
 * Copyright 2009 JavaBin
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

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
