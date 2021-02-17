package org.circuitsymphony.ui;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class ShortcutsFrame extends JDialog implements WindowListener {
    private static final long serialVersionUID = 1L;

    // private CirSim cirSim;

    public ShortcutsFrame(CirSim cirSim) {
        // Set parent
        super(cirSim);
        // this.cirSim = cirSim;
        String TITLE = CirSim.PROGRAM_NAME + " - Keyboard Shortcuts";

        // Set frame properties.
        setTitle(TITLE);
        setDefaultCloseOperation(
                WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(this);

        // Get default Font
        Font font = getFont();

        // Create main panel.
        JPanel panelMain = new JPanel();
        final int border = 16;
        panelMain.setBorder(BorderFactory.createEmptyBorder(border, border,
                border, border));
        panelMain.setLayout(new BoxLayout(panelMain, BoxLayout.Y_AXIS));

        // Create panel with program name and icon.
        JPanel panelTitle = new JPanel();

        JLabel labelProgramName = new JLabel(TITLE);
        labelProgramName
                .setFont(new Font("Serif", Font.BOLD, font.getSize() * 2));
        panelTitle.add(labelProgramName);

        panelMain.add(panelTitle);
        Border flatBorder = BorderFactory.createEmptyBorder();
        // Create panel with names.
        final JTextArea textArea = new JTextArea(30, 60);
        textArea.setFont(new Font("monospaced", Font.PLAIN, 13));
        textArea.setEditable(false);
        textArea.setBorder(flatBorder);
        try {
            FileReader fileReader = new FileReader("shortcuts.txt");
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String inputFile = "";
            String textFieldReadable = bufferedReader.readLine();
            while (textFieldReadable != null) {
                inputFile += textFieldReadable + "\n";
                textFieldReadable = bufferedReader.readLine();
            }
            textArea.setText(inputFile);
            textArea.setCaretPosition(0);
            bufferedReader.close();

        } catch (FileNotFoundException ex) {
            System.out.println("no such file exists");
        } catch (IOException ex) {
            System.out.println("unkownerror");
        }
        JScrollPane sp = new JScrollPane(textArea);
        panelMain.add(sp);

        // Create panel with button
        JPanel panelButton = new JPanel();
        JButton button = new JButton("   Close   ");
        button.addActionListener(arg0 -> close());

        panelButton.add(button);

        panelMain.add(panelButton);

        // Add main panel to frame.
        add(panelMain);

        // Show frame.
        pack();
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(((int) d.getWidth() - getWidth()) / 2,
                ((int) d.getHeight() - getHeight()) / 2);
        setVisible(true);
    }

    private void close() {
        dispose();
        // cirSim.setEnabled(true);
    }

    @Override
    public void windowActivated(WindowEvent arg0) {
    }

    @Override
    public void windowClosed(WindowEvent arg0) {
    }

    @Override
    public void windowClosing(WindowEvent arg0) {
        close();
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
