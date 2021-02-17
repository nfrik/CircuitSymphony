package org.circuitsymphony.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import org.circuitsymphony.element.CircuitElm;
import org.circuitsymphony.engine.CircuitEngine;
import org.circuitsymphony.engine.CircuitLoader;
import org.circuitsymphony.evolution.EvolElement;
import org.circuitsymphony.evolution.Nimbus;
import org.circuitsymphony.evolution.TTGen;
import org.circuitsymphony.manager.CircuitManager;
import org.circuitsymphony.util.SwingUtils;
import org.jenetics.util.DoubleRange;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NimbusSimulationDialog extends JDialog {
    private CirSim cirSim;
    private CircuitEngine engine;

    private JFileChooser fileChooser;

    private ArrayList<CircuitElm> elements = new ArrayList<>();

    private JPanel contentPane;
    private JButton runButton;
    private JButton cancelButton;
    private JButton loadButton;
    private JButton saveButton;

    private JTable elemTable;
    private DefaultTableModel elemTableModel;
    private Object[][] data;

    private JComboBox<TTGen.DATA> functionCombo;
    private JSpinner piecesSpinner;
    private JSpinner generationsSpinner;
    private JSpinner populationSpinner;
    private JSpinner fitnessCutoffSpinner;
    private JSpinner mutatorSpinner;
    private JSpinner altererSpinner;

    private JLabel statusLabel;

    private ExecutorService exec = Executors.newFixedThreadPool(1);

    public NimbusSimulationDialog(CirSim cirSim) {
        super(cirSim);
        this.cirSim = cirSim;
        this.engine = cirSim.getCircuitEngine();
        UIManager.getDefaults().put("SplitPane.border", BorderFactory.createEmptyBorder());
        setTitle("Nimbus Simulation Configuration");
        setContentPane(contentPane);
        setModal(true);
        setResizable(false);
        getRootPane().setDefaultButton(runButton);
        setSize(800, 500);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        fileChooser = new JFileChooser();
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("JSON | *." + "json", "json"));
        initStdButtons();
        initSaveLoadButtons();
        initProps();
        initTable();

        SwingUtils.setLocationToCenter(this, cirSim);
        setVisible(true);
    }

    private void initProps() {
        functionCombo.setModel(new DefaultComboBoxModel<>());
        for (TTGen.DATA t : TTGen.DATA.values()) {
            functionCombo.addItem(t);
        }
        piecesSpinner.setModel(new SpinnerNumberModel(100, 1, Integer.MAX_VALUE, 1));
        generationsSpinner.setModel(new SpinnerNumberModel(10, 1, Integer.MAX_VALUE, 1));
        populationSpinner.setModel(new SpinnerNumberModel(50, 1, Integer.MAX_VALUE, 1));
        fitnessCutoffSpinner.setModel(new SpinnerNumberModel(5, 1, Integer.MAX_VALUE, 1));
        mutatorSpinner.setModel(new SpinnerNumberModel(0.03, 0, 1, 0.001));
        altererSpinner.setModel(new SpinnerNumberModel(0.6, 0, 1, 0.001));
    }

    private void initTable() {
        String[] columns = {"Element Type", "Element Id", "Type", "Min", "Max"};

        for (CircuitElm elm : engine.getElmList()) {
            if (elm.flags2 != 0) elements.add(elm);
        }
        elements.sort(Comparator.comparing(CircuitElm::getFlags2));

        data = new Object[elements.size()][5];
        for (int i = 0; i < elements.size(); i++) {
            CircuitElm elm = elements.get(i);
            data[i][0] = elm.getClass().getSimpleName();
            data[i][1] = elm.flags2;
            data[i][2] = EvolElement.ELEM.INTERNAL;
            data[i][3] = 0;
            data[i][4] = 0;
        }
        elemTableModel = new DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return isColumnEditable(column);
            }

            @Override
            public Class getColumnClass(int column) {
                return column == 3 || column == 4 ? Double.class : super.getColumnClass(column);
            }

            @Override
            public void setValueAt(Object value, int row, int column) {
                if (isColumnEditable(column)) {
                    data[row][column] = value;
                }
                super.setValueAt(value, row, column);
            }

            private boolean isColumnEditable(int column) {
                return column == 2 || column == 3 || column == 4;
            }
        };
        elemTable.setModel(elemTableModel);

        TableColumn typeColumn = elemTable.getColumnModel().getColumn(2);
        JComboBox<EvolElement.ELEM> comboBox = new JComboBox<>();
        for (EvolElement.ELEM e : EvolElement.ELEM.values()) {
            comboBox.addItem(e);
        }
        typeColumn.setCellEditor(new DefaultCellEditor(comboBox));

        elemTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    private void initStdButtons() {
        runButton.addActionListener(e -> onRun());
        cancelButton.addActionListener(e -> onCancel());
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void initSaveLoadButtons() {
        loadButton.addActionListener(event -> {
            try {
                int result = fileChooser.showOpenDialog(NimbusSimulationDialog.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    ObjectMapper mapper = new ObjectMapper();
                    Dto dto = mapper.readValue(fileChooser.getSelectedFile(), Dto.class);
                    functionCombo.setSelectedItem(dto.function);
                    piecesSpinner.setValue(dto.pieces);
                    generationsSpinner.setValue(dto.generations);
                    populationSpinner.setValue(dto.population);
                    fitnessCutoffSpinner.setValue(dto.fitnessCutoff);
                    mutatorSpinner.setValue(dto.mutator);
                    altererSpinner.setValue(dto.alterer);
                    for (Map.Entry<Integer, DoublePair> e : dto.ranges.entrySet()) {
                        for (int i = 0; i < elements.size(); i++) {
                            CircuitElm elm = elements.get(i);
                            if (elm.flags2 == e.getKey()) {
                                elemTableModel.setValueAt(e.getValue().min, i, 3);
                                elemTableModel.setValueAt(e.getValue().max, i, 4);
                            }
                        }
                    }
                    for (Map.Entry<Integer, EvolElement.ELEM> e : dto.types.entrySet()) {
                        for (int i = 0; i < elements.size(); i++) {
                            CircuitElm elm = elements.get(i);
                            if (elm.flags2 == e.getKey()) {
                                elemTableModel.setValueAt(e.getValue(), i, 2);
                            }
                        }
                    }
                    elemTableModel.fireTableDataChanged();
                }
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(cirSim, "Failed to load configuration");
            }
        });
        saveButton.addActionListener(event -> {
            int result = fileChooser.showSaveDialog(NimbusSimulationDialog.this);
            if (result == JFileChooser.APPROVE_OPTION) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    HashMap<Integer, EvolElement.ELEM> types = new HashMap<>();
                    HashMap<Integer, DoublePair> dtoRanges = new HashMap<>();
                    for (int i = 0; i < elements.size(); i++) {
                        CircuitElm elm = elements.get(i);
                        types.put(elm.flags2, (EvolElement.ELEM) data[i][2]);
                        dtoRanges.put(elm.flags2, new DoublePair(getDoubleAt(i, 3), getDoubleAt(i, 4)));
                    }
                    Dto dto = new Dto((TTGen.DATA) functionCombo.getSelectedItem(), (int) piecesSpinner.getValue(),
                            (int) generationsSpinner.getValue(), (int) populationSpinner.getValue(), (int) fitnessCutoffSpinner.getValue(),
                            (double) mutatorSpinner.getValue(), (double) altererSpinner.getValue(), types, dtoRanges);
                    mapper.writeValue(fileChooser.getSelectedFile(), dto);
                } catch (IOException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(cirSim, "Failed to save configuration");
                }
            }
        });
    }

    private void onRun() {
        statusLabel.setText("Simulation running...");
        changeCommonButtonsEnabled(false);

        exec.submit(() -> {
            try {
                // create manifest
                List<EvolElement> manifest = new ArrayList<>();
                for (int i = 0; i < elements.size(); i++) {
                    CircuitElm elm = elements.get(i);
                    manifest.add(new EvolElement(elm.flags2, (EvolElement.ELEM) data[i][2],
                            DoubleRange.of(getDoubleAt(i, 3), getDoubleAt(i, 4))));
                }
                // sort by elementId
                manifest.sort(Comparator.comparing(EvolElement::getElemId));

                String circuit = cirSim.dumpCircuit(true);

                TTGen ga = new TTGen();
                Vector<Vector<Double>> testDataOfLen = ga.getTestDataOfLen((Integer) piecesSpinner.getValue(),
                        (TTGen.DATA) functionCombo.getSelectedItem());

                Nimbus nimbus = new Nimbus(manager -> manager.loadCircuit(circuit, true), manifest, testDataOfLen);
                CircuitManager result = nimbus.runSimulation((Integer) generationsSpinner.getValue(), (Integer) populationSpinner.getValue(),
                        (Integer) fitnessCutoffSpinner.getValue(), (Double) mutatorSpinner.getValue(), (Double) altererSpinner.getValue());
                finishFromExec(true, "Simulation finished", result.dumpElements());
            } catch (Exception e) {
                e.printStackTrace();
                finishFromExec(false, "Simulation finished with error: " + e.getMessage(), null);
            }
        });
    }

    private void finishFromExec(boolean dispose, String msg, String elemDump) {
        EventQueue.invokeLater(() -> {
            if (elemDump != null) {
                cirSim.pushUndo();
                cirSim.getCircuitLoader().loadCircuit(elemDump.getBytes(), elemDump.length(),
                        EnumSet.of(CircuitLoader.RetentionPolicy.CONFIG), true);
            }
            if (dispose) {
                dispose();
            } else {
                statusLabel.setText("");
                changeCommonButtonsEnabled(true);
            }
            JOptionPane.showMessageDialog(cirSim, msg);
        });
    }

    private void changeCommonButtonsEnabled(boolean enabled) {
        runButton.setEnabled(enabled);
        saveButton.setEnabled(enabled);
        loadButton.setEnabled(enabled);
    }

    private void onCancel() {
        dispose();
    }

    private double getDoubleAt(int row, int column) {
        Object value = elemTableModel.getValueAt(row, column);
        if (value instanceof Integer) { //boxed Integer can't be cast to double directly
            return ((Integer) value).doubleValue();
        }
        return (double) value;
    }

    @Override
    public void dispose() {
        super.dispose();
        exec.shutdownNow();
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
        panel1.setLayout(new GridLayoutManager(1, 5, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1, true, false));
        panel1.add(panel2, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        runButton = new JButton();
        runButton.setText("Run");
        panel2.add(runButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cancelButton = new JButton();
        cancelButton.setText("Cancel");
        panel2.add(cancelButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        statusLabel = new JLabel();
        statusLabel.setText("");
        panel1.add(statusLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        loadButton = new JButton();
        loadButton.setText("Load");
        panel1.add(loadButton, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        saveButton = new JButton();
        saveButton.setText("Save");
        panel1.add(saveButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JSplitPane splitPane1 = new JSplitPane();
        splitPane1.setOrientation(0);
        splitPane1.setResizeWeight(1.0);
        panel3.add(splitPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        splitPane1.setRightComponent(panel4);
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(5, 4, new Insets(3, 3, 3, 3), -1, -1));
        panel4.add(panel5, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel5.setBorder(BorderFactory.createTitledBorder("Simulation options"));
        final JLabel label1 = new JLabel();
        label1.setText("Pieces");
        panel5.add(label1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel5.add(spacer2, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        piecesSpinner = new JSpinner();
        panel5.add(piecesSpinner, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Generations");
        panel5.add(label2, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Population");
        panel5.add(label3, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        generationsSpinner = new JSpinner();
        panel5.add(generationsSpinner, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        populationSpinner = new JSpinner();
        panel5.add(populationSpinner, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Function");
        panel5.add(label4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        functionCombo = new JComboBox();
        panel5.add(functionCombo, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Fitness cutoff");
        panel5.add(label5, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        fitnessCutoffSpinner = new JSpinner();
        panel5.add(fitnessCutoffSpinner, new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Mutator");
        panel5.add(label6, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        mutatorSpinner = new JSpinner();
        panel5.add(mutatorSpinner, new GridConstraints(2, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("Alterer");
        panel5.add(label7, new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        altererSpinner = new JSpinner();
        panel5.add(altererSpinner, new GridConstraints(3, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        splitPane1.setLeftComponent(scrollPane1);
        elemTable = new JTable();
        scrollPane1.setViewportView(elemTable);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

    private static class DoublePair {
        public double min;
        public double max;

        public DoublePair() {
        }

        public DoublePair(double min, double max) {
            this.min = min;
            this.max = max;
        }

        public DoubleRange toRange() {
            return DoubleRange.of(min, max);
        }
    }

    private static class Dto {
        public TTGen.DATA function;
        public int pieces;
        public int generations;
        public int population;
        public int fitnessCutoff;
        public double mutator;
        public double alterer;
        public Map<Integer, EvolElement.ELEM> types;
        public Map<Integer, DoublePair> ranges = new HashMap<>();

        public Dto() {
        }

        public Dto(TTGen.DATA function, int pieces, int generations, int population, int fitnessCutoff, double mutator,
                   double alterer, Map<Integer, EvolElement.ELEM> types, Map<Integer, DoublePair> ranges) {
            this.function = function;
            this.pieces = pieces;
            this.generations = generations;
            this.population = population;
            this.fitnessCutoff = fitnessCutoff;
            this.mutator = mutator;
            this.alterer = alterer;
            this.types = new HashMap<>(types);
            this.ranges = new HashMap<>(ranges);
        }
    }
}
