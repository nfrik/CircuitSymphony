package org.circuitsymphony.ui;
/*
 * Code by Federico Garc�a Garc�a.
 * 08/11/2015.
 * 
 * Frame that shows the About info.
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.net.URI;

public class AboutFrame extends JDialog implements WindowListener {

    private final CirSim cirSim;

    public AboutFrame(CirSim cirSim) {
        // Set parent
        super(cirSim);
        this.cirSim = cirSim;

        // Set frame properties.
        String TITLE = "About CircuitSymphony";
        setTitle(TITLE);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(this);

        // Get default Font
        Font font = getFont();

        // Create main panel.
        JPanel panelMain = new JPanel();
        final int border = 16;
        panelMain.setBorder(BorderFactory.createEmptyBorder(border, border, border, border));
        panelMain.setLayout(new BoxLayout(panelMain, BoxLayout.Y_AXIS));

        // Create panel with program name and icon.
        JPanel panelTitle = new JPanel();

        ImageIcon img = new ImageIcon(getClass().getResource("/images/about.png"));
        JLabel labelImage = new JLabel(img);
        panelTitle.add(labelImage);

        JLabel labelProgramName = new JLabel(CirSim.PROGRAM_NAME + "         ");
        labelProgramName.setFont(new Font("Serif", Font.BOLD, font.getSize() * 2)); // Set big bold serif font
        panelTitle.add(labelProgramName);

        panelMain.add(panelTitle);

        // Create panel with names.
        JPanel panelNames = new JPanel();
        String names = "";

        String[] NAMES = {"Nikolay Frick, " +
                          "Thomas LaBean & " +
                          "Freescale LLC"};
        for (String s : NAMES) {
            names += s + "<br>";
        }

        // We use HTML for new lines.
        String NAMES_TITLE = "Developed by:";
        String CURRENT_CONTRIBS = "Main features:";
        String CURRENT_FEATURES = "Headless server mode, sparse and CUDA solvers, enhanced UI features, additional memristor models, unit tests and more";
        String NAME_ORIGINAL_TITLE = "Original code by:";
        String OTHER_CONTRIBUTORS = "The first version of this code was taken from:";
        String OTHER_CONTRIBUTORS_FEATURES = "https://sourceforge.net/projects/circuitmod/";
        String NAME_ORIGINAL = "Paul Falstad";
        String CODE_URL = "https://www.falstad.com/circuit/";
        JLabel labelNamesTitle = new JLabel(
                "<html>"
                        + "<center>"
                        + "<b>" + NAMES_TITLE + "</b>"
                        + "<br>"
                        + names
                        + "<br>"
                        + "<b>" + CURRENT_CONTRIBS + "</b>"
                        + "<br>"
                        + CURRENT_FEATURES
                        + "<br>"
                        + "<br>"
                        + "<b>" + OTHER_CONTRIBUTORS + "</b>"
                        + "<br>"
                        + OTHER_CONTRIBUTORS_FEATURES
                        + "<br>"
                        + "<br>"
                        + "<b>" + NAME_ORIGINAL_TITLE + "</b>"
                        + "<br>"
                        + NAME_ORIGINAL
                        + "<br>"
                        + "<b>" + CODE_URL + "</b>"
                        + "</center>"
                        + "</html>");
        panelNames.add(labelNamesTitle);

        panelMain.add(panelNames);

        // Create panel with license.
        JPanel panelLicense = new JPanel();

        String LICENSE_TITLE = "GNU GENERAL PUBLIC LICENSE";
        String LICENSE_VERSION = "Version 3, 29 June 2007";
        String LICENSE = "<HTML>"
                + "Copyright (C) 2007 Free Software Foundation, Inc. "
                + "&lt;http://fsf.org/&gt;"
                + "<br>"
                + "Everyone is permitted to copy and distribute verbatim copies"
                + "<br>"
                + "of this license document, but changing it is not allowed."
                + "</HTML>";
        JLabel labelLicense = new JLabel(
                "<html>"
                        + "<center>"
                        + "<b>" + LICENSE_TITLE + "</b>"
                        + "<br>"
                        + LICENSE_VERSION
                        + "</center>"
                        + "<br>"
                        + LICENSE
                        + "</html>");

        panelLicense.add(labelLicense);

        panelMain.add(panelLicense);

        // Create panel with website.
        JPanel panelWebsite = new JPanel();

        JLabel labelWebsite = new JLabel();
        String WEBSITE_TITLE = "Repository:";
        String WEBSITE = "https://github.com/nfrik/CircuitSymphony";
        goToWebsite(labelWebsite, WEBSITE, WEBSITE_TITLE, WEBSITE);
        panelWebsite.add(labelWebsite);

        panelMain.add(panelWebsite);

        // Create panel with button
        JPanel panelButton = new JPanel();
        JButton button = new JButton("   Ok   ");
        button.addActionListener(arg0 -> close());

        panelButton.add(button);

        panelMain.add(panelButton);

        // Add main panel to frame.
        add(panelMain);

        // Show frame.
        pack();
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(((int) d.getWidth() - getWidth()) / 2, ((int) d.getHeight() - getHeight()) / 2);
        setVisible(true);
    }

    private void close() {
        cirSim.setEnabled(true);
        dispose();
    }

    private void goToWebsite(JLabel website, final String url, String title, String text) {
        website.setText("<html><center><b>" + title + "</b></center><a href=\"\">" + text + "</a></html>");
        website.setCursor(new Cursor(Cursor.HAND_CURSOR));
        website.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI(url));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    @Override
    public void windowClosing(WindowEvent arg0) {
        // Enable cirSim frame and dispose About.
        close();
    }

    @Override
    public void windowActivated(WindowEvent arg0) {
    }

    @Override
    public void windowClosed(WindowEvent arg0) {
    }

    @Override
    public void windowDeactivated(WindowEvent arg0) {
    }

    @Override
    public void windowDeiconified(WindowEvent arg0) {
    }

    @Override
    public void windowIconified(WindowEvent arg0) {
    }

    @Override
    public void windowOpened(WindowEvent arg0) {
    }
}
