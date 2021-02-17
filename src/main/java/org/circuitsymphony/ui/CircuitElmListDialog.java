package org.circuitsymphony.ui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import org.circuitsymphony.element.CircuitElm;
import org.circuitsymphony.element.WireElm;
import org.circuitsymphony.engine.CircuitEngine;
import org.circuitsymphony.util.SwingUtils;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Vector;

public class CircuitElmListDialog extends JDialog {
    private CirSim cirSim;
    private CircuitEngine engine;
    private ArrayList<CircuitElm> sortedElm = new ArrayList<>();

    private JPanel contentPane;
    private JTabbedPane tabbedPane;
    private static final int TAB_ELEM_LIST = 0;
    private static final int TAB_ADJ_MATRIX = 1;
    private JButton closeButton;

    private JTable elemTable;
    private DefaultTableModel elemTableModel;
    private static final int ELEM_COLUMN_TYPE = 0;
    private static final int ELEM_COLUMN_ID = 1;
    private static final int ELEM_COLUMN_COUNT = 2;
    private JButton editElmButton;
    private JButton viewInScopeElmButton;
    private JButton deleteElmButton;
    private JButton extractElmButton;

    private JTable adjacencyTable;
    private JScrollPane adjacencyScrollPane;

    public CircuitElmListDialog(CirSim cirSim) {
        super(cirSim);
        this.cirSim = cirSim;
        this.engine = cirSim.getCircuitEngine();

        setTitle("Circuit elements");
        setContentPane(contentPane);
        setModal(true);
        setMinimumSize(new Dimension(400, 300));
        setSize(800, 500);

        getRootPane().setDefaultButton(closeButton);
        closeButton.addActionListener(e -> onClose());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onClose();
            }
        });
        contentPane.registerKeyboardAction(e -> onClose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        createSortedElmList();
        initElemTable();
        initElemTablePopupMenu();

        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedIndex() == TAB_ADJ_MATRIX) {
                createSortedElmList();
                initAdjTable();
            }
        });

        SwingUtils.setLocationToCenter(this, cirSim);
        setVisible(true);
    }

    private void createSortedElmList() {
        sortedElm.clear();
        sortedElm.addAll(engine.getElmList());
        sortedElm.sort(Comparator.comparing(CircuitElm::getFlags2));
    }

    private void initAdjTable() {
        Vector<String> elemNames = new Vector<>();
        Vector<CircuitElm> elems = new Vector<>();

        for (CircuitElm elm : sortedElm) {
            if (elm.flags2 != 0) {
                elems.add(elm);
                elemNames.add(String.valueOf(elm.flags2));
            }
        }

        Vector<Vector<Integer>> matrixData = new Vector<>();
        for (CircuitElm rowElm : elems) {
            Vector<Integer> rowData = new Vector<>();
            matrixData.add(rowData);
            for (CircuitElm columnElm : elems) {
                boolean haveCommonPoints = false;
                if (rowElm == columnElm) {
                    rowData.add(0);
                    continue;
                }
                for (int i = 0; i < rowElm.getPostCount(); i++) {
                    for (int j = 0; j < columnElm.getPostCount(); j++) {
                        if (rowElm.getPost(i).equals(columnElm.getPost(j))) {
                            haveCommonPoints = true;
                        }
                    }
                }
                if (haveCommonPoints) {
                    rowData.add(1);
                } else {
                    rowData.add(0);
                }
            }
        }

        DefaultTableModel adjacencyTableModel = new DefaultTableModel(matrixData, elemNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int column) {
                return Integer.class;
            }
        };
        adjacencyTable.setModel(adjacencyTableModel);
        adjacencyTable.setCellSelectionEnabled(true);
        adjacencyTable.setDragEnabled(false);
        adjacencyTable.getTableHeader().setReorderingAllowed(false);
        adjacencyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        adjacencyTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JTableHeader tableHeader = adjacencyTable.getTableHeader();
        FontMetrics headerFontMetrics = tableHeader.getFontMetrics(tableHeader.getFont());
        for (int i = 0; i < adjacencyTable.getColumnCount(); i++) {
            TableColumn column = adjacencyTable.getColumnModel().getColumn(i);
            String columnName = adjacencyTable.getColumnName(i);
            int size = headerFontMetrics.stringWidth(columnName);
            int margin = 10;
            column.setPreferredWidth(size + margin);
        }
        JTable rowTable = new RowNumberTable(adjacencyTable, elemNames);
        adjacencyScrollPane.setRowHeaderView(rowTable);
        adjacencyScrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, rowTable.getTableHeader());
    }

    private void initElemTable() {
        initElemTableModel();
        elemTable.setDragEnabled(false);
        elemTable.getTableHeader().setReorderingAllowed(false);
        elemTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        elemTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (elemTable.getSelectedRow() != -1) {
                    if (e.getClickCount() == 1) {
                        updateSelectedElems();
                    } else if (e.getClickCount() == 2) {
                        editElemOnDoubleClick(e);
                    }
                }
            }
        });
        elemTable.getSelectionModel().addListSelectionListener(e -> updateSelectedElems());
        editElmButton.addActionListener(e -> editSelectedElem());
        viewInScopeElmButton.addActionListener(e -> viewInScopeSelectedElem());
        deleteElmButton.addActionListener(e -> deleteSelectedElem());
        extractElmButton.addActionListener(e -> extractSelectedElm());
    }

    private void updateSelectedElems() {
        for (CircuitElm elm : sortedElm) {
            elm.setSelected(false);
        }
        for (int row : elemTable.getSelectedRows()) {
            if (row >= 0 && row < sortedElm.size()) {
                sortedElm.get(row).setSelected(true);
            }
        }
    }

    private void editElemOnDoubleClick(MouseEvent e) {
        int row = elemTable.rowAtPoint(e.getPoint());
        int col = elemTable.columnAtPoint(e.getPoint());
        if (row >= 0 && col >= 0) {
            elemTable.clearSelection();
            elemTable.setRowSelectionInterval(row, row);
            editSelectedElem();
        }
    }

    private void initElemTableModel() {
        String[] columns = {"Element Type", "Element Id"};
        Object[][] elemData = new Object[sortedElm.size()][ELEM_COLUMN_COUNT];
        for (int i = 0; i < sortedElm.size(); i++) {
            CircuitElm elm = sortedElm.get(i);
            elemData[i][ELEM_COLUMN_TYPE] = elm.getClass().getSimpleName();
            elemData[i][ELEM_COLUMN_ID] = elm.flags2;
        }
        elemTableModel = new DefaultTableModel(elemData, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int column) {
                return column == ELEM_COLUMN_ID ? Integer.class : super.getColumnClass(column);
            }

            @Override
            public void removeRow(int row) {
                super.removeRow(row);
                createSortedElmList();
            }
        };
        elemTable.setModel(elemTableModel);
    }

    private void initElemTablePopupMenu() {
        final JPopupMenu tableMenu = new JPopupMenu();
        JMenuItem editItem = new JMenuItem("Edit");
        JMenuItem viewInScopeItem = new JMenuItem("View in Scope");
        JMenuItem deleteItem = new JMenuItem("Delete");
        JMenuItem extractItem = new JMenuItem("Extract Outside Network");
        editItem.addActionListener(e -> editSelectedElem());
        viewInScopeItem.addActionListener(e -> viewInScopeSelectedElem());
        deleteItem.addActionListener(e -> deleteSelectedElem());
        extractItem.addActionListener(e -> extractSelectedElm());
        tableMenu.add(editItem);
        tableMenu.add(viewInScopeItem);
        tableMenu.add(deleteItem);
        tableMenu.addSeparator();
        tableMenu.add(extractItem);
        tableMenu.addPopupMenuListener(new PopupMenuAdapter() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                SwingUtilities.invokeLater(() -> {
                    elemTable.clearSelection();
                    int rowAtPoint = elemTable.rowAtPoint(SwingUtilities.convertPoint(tableMenu, new Point(0, 0), elemTable));
                    if (rowAtPoint > -1) {
                        elemTable.setRowSelectionInterval(rowAtPoint, rowAtPoint);
                    }
                });
            }
        });
        elemTable.setComponentPopupMenu(tableMenu);
    }

    private void editSelectedElem() {
        if (elemTable.getSelectedRow() == -1) {
            JOptionPane.showMessageDialog(this, "You must select element first");
            return;
        }
        CircuitElm elem = sortedElm.get(elemTable.getSelectedRow());
        if (elem != null) {
            if (elem.getEditInfo(0) == null) {
                JOptionPane.showMessageDialog(this, "This element can't be edited");
                return;
            }
            EditDialog edit = new EditDialog(elem, cirSim);
            edit.setModal(true);
            edit.setVisible(true);
            elemTable.setValueAt(elem.flags2, elemTable.getSelectedRow(), ELEM_COLUMN_ID);
        }
    }

    private void viewInScopeSelectedElem() {
        for (int row : elemTable.getSelectedRows()) {
            if (row >= 0 && row < sortedElm.size()) {
                CircuitElm elm = sortedElm.get(row);
                if (elm.canViewInScope()) {
                    cirSim.addToScope(sortedElm.get(row));
                }
            }
        }
    }

    private void deleteSelectedElem() {
        for (CircuitElm elm : sortedElm) {
            elm.setSelected(false);
        }
        for (int row : elemTable.getSelectedRows()) {
            if (row >= 0 && row < sortedElm.size()) {
                cirSim.deleteElm(sortedElm.get(row));
            }
        }
        createSortedElmList();
        initElemTableModel();
    }

    private void extractSelectedElm() {
        if (sortedElm.size() == 0) return;
        Rectangle area = sortedElm.get(0).getBoundingBox();
        for (CircuitElm elm : sortedElm) {
            if (!elm.isSelected()) {
                area = area.union(elm.getBoundingBox());
            }
        }
        int margin = 20;
        int newX = area.x + area.width + margin;
        int newY = area.y;
        Rectangle movedElemBounds = null;
        for (CircuitElm elm : sortedElm) {
            if (elm.isSelected()) {
                ArrayList<Point> oldPoints = new ArrayList<>();
                for (int i = 0; i < elm.getPostCount(); i++) {
                    oldPoints.add(elm.getPost(i));
                }

                Rectangle elmBounds = elm.getBoundingBox();
                int targetX = newX;
                int targetY = movedElemBounds == null ? newY : newY + movedElemBounds.height + margin;
                elm.moveWithGrid(cirSim, targetX - elmBounds.x, targetY - elmBounds.y);
                if (movedElemBounds == null) {
                    movedElemBounds = elm.getBoundingBox();
                } else {
                    movedElemBounds = movedElemBounds.union(elm.getBoundingBox());
                }

                for (int i = 0; i < elm.getPostCount(); i++) {
                    Point oldPoint = oldPoints.get(i);
                    Point newPoint = elm.getPost(i);
                    WireElm wire = new WireElm(engine, oldPoint.x, oldPoint.y, newPoint.x, newPoint.y, 0, null);
                    wire.setPoints();
                    engine.elmList.addElement(wire);
                }
            }
        }
        cirSim.needAnalyze();
        createSortedElmList();
        initElemTableModel();
    }

    private void onClose() {
        dispose();
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
        tabbedPane = new JTabbedPane();
        contentPane.add(tabbedPane, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(3, 3, 3, 3), -1, -1));
        tabbedPane.addTab("Circuit Elements", panel1);
        final JScrollPane scrollPane1 = new JScrollPane();
        panel1.add(scrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        elemTable = new JTable();
        scrollPane1.setViewportView(elemTable);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(6, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        editElmButton = new JButton();
        editElmButton.setText("Edit");
        panel2.add(editElmButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel2.add(spacer1, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        viewInScopeElmButton = new JButton();
        viewInScopeElmButton.setText("View in Scope");
        panel2.add(viewInScopeElmButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        deleteElmButton = new JButton();
        deleteElmButton.setText("Delete");
        panel2.add(deleteElmButton, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JSeparator separator1 = new JSeparator();
        panel2.add(separator1, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        extractElmButton = new JButton();
        extractElmButton.setText("Extract Outside Network");
        panel2.add(extractElmButton, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 1, new Insets(3, 3, 3, 3), -1, -1));
        tabbedPane.addTab("Adjacency Matrix", panel3);
        adjacencyScrollPane = new JScrollPane();
        panel3.add(adjacencyScrollPane, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        adjacencyTable = new JTable();
        adjacencyScrollPane.setViewportView(adjacencyTable);
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel4, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel4.add(spacer2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel4.add(panel5, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        closeButton = new JButton();
        closeButton.setText("Close");
        panel5.add(closeButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
