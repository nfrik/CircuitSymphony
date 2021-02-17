package org.circuitsymphony.ui;

import javax.swing.*;
import java.awt.*;

/*
 * Code by Federico Garc�a Garc�a.
 * 06/12/2015.
 * 
 * Frame that shows the logo at startup.
 */


public class LogoFrame extends JDialog {
    public LogoFrame() {
        super();
        setUndecorated(true);
        setSize(400, 240);

        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(dim.width / 2 - getSize().width / 2, dim.height / 2 - getSize().height / 2);

        setContentPane(new LogoFrameCanvas());
        setVisible(true);
    }
}
