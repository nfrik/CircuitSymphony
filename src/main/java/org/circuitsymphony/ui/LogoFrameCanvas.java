package org.circuitsymphony.ui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;


public class LogoFrameCanvas extends JComponent {
    private BufferedImage img = null;

    public LogoFrameCanvas() {
        try {
            img = ImageIO.read(getClass().getResource("/images/logo.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        if (img != null) {
            g2.drawImage(img, 0, 0, null);
        }
    }
}
