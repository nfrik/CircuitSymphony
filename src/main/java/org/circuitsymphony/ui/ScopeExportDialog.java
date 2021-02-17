package org.circuitsymphony.ui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import org.circuitsymphony.util.Scope;
import org.circuitsymphony.util.SwingUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Dialog shown after user decides to export data from {@link Scope}. Allows to configure how many entries to write
 * and what data should be exported.
 */
public class ScopeExportDialog extends JDialog {
    private static final String SEPARATOR = ",";

    private Frame owner;
    private JFileChooser fileChooser;

    private JPanel contentPane;
    private JButton buttonExport;
    private JButton buttonCancel;
    private JTextField outFilePath;
    private JButton chooseFileButton;
    private JCheckBox writeFileHeaderCheck;
    private JCheckBox writeTimeStampCheck;
    private JCheckBox writeVoltageValuesCheck;
    private JCheckBox writeCurrentValuesCheck;
    private JCheckBox exportAllScopesInStackCheck;
    private JSpinner entryCountSpinner;

    private ArrayList<Scope> stackedScopes;
    private Scope mouseOverScope;
    private CirSim sim;
    private boolean shouldResumeSim;

    public ScopeExportDialog(Frame owner) {
        super(owner);
        this.owner = owner;
        setTitle("Export Scope Data");
        setContentPane(contentPane);
        setModal(true);
        setResizable(false);
        getRootPane().setDefaultButton(buttonExport);
        setSize(440, 260);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        initStdButtons();

        fileChooser = new JFileChooser();
        entryCountSpinner.setModel(new SpinnerNumberModel(Scope.SCOPE_DATA_ENTRY_COUNT, 1, Scope.SCOPE_DATA_ENTRY_COUNT, 1));
        chooseFileButton.addActionListener(e -> {
            int result = fileChooser.showSaveDialog(ScopeExportDialog.this);
            if (result == JFileChooser.APPROVE_OPTION) {
                outFilePath.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        });
    }

    private void initStdButtons() {
        buttonExport.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    public void begin(CirSim sim, ArrayList<Scope> stackedScopes, Scope mouseOverScope) {
        if (this.sim != null) {
            throw new IllegalStateException("Can't begin ScopeExportDialog twice without ending it first");
        }
        if (!sim.stoppedCheck.getState()) {
            sim.stoppedCheck.setState(true);
            shouldResumeSim = true;
        }
        this.sim = sim;
        this.stackedScopes = stackedScopes;
        this.mouseOverScope = mouseOverScope;
        if (stackedScopes.size() <= 1) {
            exportAllScopesInStackCheck.setEnabled(false);
        } else {
            exportAllScopesInStackCheck.setEnabled(true);
        }
        SwingUtils.setLocationToCenter(this, owner);
        setVisible(true);
    }

    private void end() {
        if (shouldResumeSim) {
            sim.stoppedCheck.setState(false);
        }
        sim = null;
        stackedScopes = null;
        mouseOverScope = null;
        shouldResumeSim = false;
    }

    private boolean exportData() {
        try {
            PrintWriter out = new PrintWriter(outFilePath.getText(), "UTF-8");

            ArrayList<Scope> scopesToExport = new ArrayList<>();
            if (exportAllScopesInStackCheck.isEnabled() && exportAllScopesInStackCheck.isSelected()) {
                scopesToExport.addAll(stackedScopes);
            } else {
                scopesToExport.add(mouseOverScope);
            }

            ArrayList<ArrayList<Scope.ScopeDataEntry>> allEntries = new ArrayList<>();

            // Collect entries from all scopes that should be exported
            for (Scope s : scopesToExport) {
                // Collect last n entries from scope and reverse them for chronological order
                int entriesToExport = (int) entryCountSpinner.getValue();
                ArrayList<Scope.ScopeDataEntry> entries = new ArrayList<>(entriesToExport);
                for (int i = 0; i < entriesToExport; i++) {
                    Scope.ScopeDataEntry entry = s.getDataEntries().lookBack(i);
                    if (entry == null) break; // no more entries in scope buffer
                    entries.add(entry);
                }
                allEntries.add(entries);
            }

            if (writeFileHeaderCheck.isSelected()) {
                String header = "";

                for (Scope s : scopesToExport) {
                    if (writeTimeStampCheck.isSelected()) {
                        header += s.getElm().flags2 + " " + s.getElm().getClass().getSimpleName() + " time" + SEPARATOR;
                    }
                    if (writeVoltageValuesCheck.isSelected()) {
                        header += s.getElm().flags2 + " " + s.getElm().getClass().getSimpleName() + " v" + SEPARATOR;
                    }
                    if (writeCurrentValuesCheck.isSelected()) {
                        header += s.getElm().flags2 + " " + s.getElm().getClass().getSimpleName() + " i" + SEPARATOR;
                    }
                }

                if (header.endsWith(SEPARATOR)) header = header.substring(0, header.length() - SEPARATOR.length());
                out.println(header);
            }

            int maxEntries = 0;
            for (ArrayList<Scope.ScopeDataEntry> scopeEntries : allEntries) {
                maxEntries = Math.max(maxEntries, scopeEntries.size());
            }

            // Write data to buffer
            ArrayList<String> outLines = new ArrayList<>();
            for (int i = 0; i < maxEntries; i++) {
                String outText = "";

                for (int s = 0; s < scopesToExport.size(); s++) {
                    Scope.ScopeDataEntry e = null;
                    try {
                        e = allEntries.get(s).get(i);
                    } catch (IndexOutOfBoundsException ignored) {
                    }

                    if (e != null) {
                        if (writeTimeStampCheck.isSelected()) outText += e.t + SEPARATOR;
                        if (writeVoltageValuesCheck.isSelected()) outText += e.v + SEPARATOR;
                        if (writeCurrentValuesCheck.isSelected()) outText += e.i + SEPARATOR;
                    } else {
                        if (writeTimeStampCheck.isSelected()) outText += "NaN" + SEPARATOR;
                        if (writeVoltageValuesCheck.isSelected()) outText += "NaN" + SEPARATOR;
                        if (writeCurrentValuesCheck.isSelected()) outText += "NaN" + SEPARATOR;
                    }
                }

                if (outText.endsWith(SEPARATOR)) {
                    outText = outText.substring(0, outText.length() - SEPARATOR.length());
                }
                outLines.add(outText);
            }

            // Reverse and write data buffer to file
            Collections.reverse(outLines);
            for (String s : outLines) {
                out.println(s);
            }
            out.close();
            return true;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "IO error while saving to file");
            e.printStackTrace();
        }
        return false;
    }

    private void onOK() {
        if (outFilePath.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Output file path can't be empty");
            return;
        }
        if (writeTimeStampCheck.isSelected() == false && writeVoltageValuesCheck.isSelected() == false
                && writeCurrentValuesCheck.isSelected() == false) {
            JOptionPane.showMessageDialog(this, "You must select at least one element to export");
            return;
        }
        boolean success = exportData();
        if (success) {
            JOptionPane.showMessageDialog(this, "Data exported");
        }
        dispose();
        end();
    }

    private void onCancel() {
        dispose();
        end();
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayoutManager(2, 1, new Insets(10, 10, 10, 10), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1, true, false));
        panel1.add(panel2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonExport = new JButton();
        buttonExport.setText("Export");
        panel2.add(buttonExport, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonCancel = new JButton();
        buttonCancel.setText("Cancel");
        panel2.add(buttonCancel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(8, 3, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Output file:");
        panel3.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel3.add(spacer2, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        outFilePath = new JTextField();
        panel3.add(outFilePath, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        chooseFileButton = new JButton();
        chooseFileButton.setText("Choose...");
        panel3.add(chooseFileButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        writeFileHeaderCheck = new JCheckBox();
        writeFileHeaderCheck.setSelected(true);
        writeFileHeaderCheck.setText("Write file header");
        panel3.add(writeFileHeaderCheck, new GridConstraints(2, 0, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        writeVoltageValuesCheck = new JCheckBox();
        writeVoltageValuesCheck.setSelected(true);
        writeVoltageValuesCheck.setText("Write voltage values");
        panel3.add(writeVoltageValuesCheck, new GridConstraints(4, 0, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        writeCurrentValuesCheck = new JCheckBox();
        writeCurrentValuesCheck.setSelected(true);
        writeCurrentValuesCheck.setText("Write current values");
        panel3.add(writeCurrentValuesCheck, new GridConstraints(5, 0, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Entries to write:");
        panel3.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        entryCountSpinner = new JSpinner();
        panel3.add(entryCountSpinner, new GridConstraints(1, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        writeTimeStampCheck = new JCheckBox();
        writeTimeStampCheck.setSelected(true);
        writeTimeStampCheck.setText("Write time stamp");
        panel3.add(writeTimeStampCheck, new GridConstraints(3, 0, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        exportAllScopesInStackCheck = new JCheckBox();
        exportAllScopesInStackCheck.setEnabled(false);
        exportAllScopesInStackCheck.setSelected(true);
        exportAllScopesInStackCheck.setText("Export all scopes in stack");
        panel3.add(exportAllScopesInStackCheck, new GridConstraints(6, 0, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
