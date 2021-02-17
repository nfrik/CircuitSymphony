package org.circuitsymphony.ui;

import org.circuitsymphony.Editable;
import org.circuitsymphony.element.CircuitElm;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.*;
import java.text.NumberFormat;
import java.text.ParseException;

public class EditDialog extends Dialog implements AdjustmentListener, ActionListener, ItemListener {

    private final Editable elm;
    private final CirSim cframe;
    private final Button applyButton;
    private final Button okButton;
    private final EditInfo[] einfos;
    private final int einfocount;
    private final int barmax = 1000;
    private final NumberFormat noCommaFormat;
    private JFormattedTextField elementIdField;

    public EditDialog(Editable ce, CirSim sim) {
        super(sim, "Edit Component", false);
        cframe = sim;
        elm = ce;
        setLayout(new EditDialogLayout());
        einfos = new EditInfo[15];
        noCommaFormat = NumberFormat.getInstance();
        noCommaFormat.setMaximumFractionDigits(10);
        noCommaFormat.setGroupingUsed(false);
        if (ce instanceof CircuitElm) {
            add(new Label("Element ID"));
            NumberFormat format = NumberFormat.getIntegerInstance();
            format.setGroupingUsed(false);
            NumberFormatter formatter = new NumberFormatter(format) {
                @Override
                public Object stringToValue(String text) throws ParseException {
                    if (text.isEmpty()) return null;
                    return super.stringToValue(text);
                }
            };
            formatter.setValueClass(Integer.class);
            formatter.setAllowsInvalid(false);
            formatter.setMinimum(0);
            elementIdField = new JFormattedTextField(formatter);
            elementIdField.setColumns(10);
            elementIdField.setText(Integer.toString(((CircuitElm) ce).flags2));
            add(elementIdField);
        }
        int i;
        for (i = 0; ; i++) {
            einfos[i] = elm.getEditInfo(i);
            if (einfos[i] == null)
                break;
            EditInfo ei = einfos[i];
            add(new Label(ei.getName()));
            if (ei.getChoice() != null) {
                add(ei.getChoice());
                ei.getChoice().addItemListener(this);
            } else if (ei.getCheckbox() != null) {
                add(ei.getCheckbox());
                ei.getCheckbox().addItemListener(this);
            } else {
                ei.setTextf(new TextField(unitString(ei), 10));
                add(ei.getTextf());
                if (ei.getText() != null)
                    ei.getTextf().setText(ei.getText());
                ei.getTextf().addActionListener(this);
                if (ei.getText() == null) {
                    ei.setBar(new Scrollbar(Scrollbar.HORIZONTAL, 50, 10, 0, barmax + 2));
                    add(ei.getBar());
                    setBar(ei);
                    ei.getBar().addAdjustmentListener(this);
                }
            }
        }
        einfocount = i;
        add(applyButton = new Button("Apply"));
        applyButton.addActionListener(this);
        add(okButton = new Button("OK"));
        okButton.addActionListener(this);
        Point x = cframe.getLocationOnScreen();
        Dimension d = getSize();
        setLocation(x.x + (cframe.winSize.width - d.width) / 2,
                x.y + (cframe.winSize.height - d.height) / 2);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                apply();
                finish();
            }
        });
    }

    private String unitString(EditInfo ei) {
        double v = ei.getValue();
        double va = Math.abs(v);
        if (ei.isDimensionless())
            return noCommaFormat.format(v);
        if (v == 0) return "0";
        if (va < 1e-9)
            return noCommaFormat.format(v * 1e12) + "p";
        if (va < 1e-6)
            return noCommaFormat.format(v * 1e9) + "n";
        if (va < 1e-3)
            return noCommaFormat.format(v * 1e6) + "u";
        if (va < 1 && !ei.isForceLargeM())
            return noCommaFormat.format(v * 1e3) + "m";
        if (va < 1e3)
            return noCommaFormat.format(v);
        if (va < 1e6)
            return noCommaFormat.format(v * 1e-3) + "k";
        if (va < 1e9)
            return noCommaFormat.format(v * 1e-6) + "M";
        return noCommaFormat.format(v * 1e-9) + "G";
    }

    private double parseUnits(EditInfo ei) throws java.text.ParseException {
        String s = ei.getTextf().getText();
        s = s.trim();
        int len = s.length();
        char uc = s.charAt(len - 1);
        double mult = 1;
        switch (uc) {
            case 'p':
            case 'P':
                mult = 1e-12;
                break;
            case 'n':
            case 'N':
                mult = 1e-9;
                break;
            case 'u':
            case 'U':
                mult = 1e-6;
                break;

            // for ohm values, we assume mega for lowercase m, otherwise milli
            case 'm':
                mult = (ei.isForceLargeM()) ? 1e6 : 1e-3;
                break;

            case 'k':
            case 'K':
                mult = 1e3;
                break;
            case 'M':
                mult = 1e6;
                break;
            case 'G':
            case 'g':
                mult = 1e9;
                break;
        }
        if (mult != 1)
            s = s.substring(0, len - 1).trim();
        return noCommaFormat.parse(s).doubleValue() * mult;
    }

    private void apply() {
        int i;
        for (i = 0; i != einfocount; i++) {
            EditInfo ei = einfos[i];
            if (ei.getTextf() == null)
                continue;
            if (ei.getText() == null) {
                try {
                    ei.setValue(parseUnits(ei));
                } catch (Exception ex) { /* ignored */ }
            }
            elm.setEditValue(i, ei);
            if (ei.getText() == null)
                setBar(ei);
        }
        if (elm instanceof CircuitElm) {
            try {
                if (!elementIdField.getText().isEmpty()) {
                    CircuitElm cElm = (CircuitElm) elm;
                    cElm.flags2 = Integer.parseInt(elementIdField.getText());
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        cframe.needAnalyze();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int i;
        Object src = e.getSource();
        for (i = 0; i != einfocount; i++) {
            EditInfo ei = einfos[i];
            if (src == ei.getTextf()) {
                if (ei.getText() == null) {
                    try {
                        ei.setValue(parseUnits(ei));
                    } catch (Exception ex) { /* ignored */ }
                }
                elm.setEditValue(i, ei);
                if (ei.getText() == null)
                    setBar(ei);
                cframe.needAnalyze();
            }
        }
        if (e.getSource() == okButton) {
            apply();
            finish();
        }
        if (e.getSource() == applyButton) {
            apply();
        }
    }

    @Override
    public void adjustmentValueChanged(AdjustmentEvent e) {
        Object src = e.getSource();
        int i;
        for (i = 0; i != einfocount; i++) {
            EditInfo ei = einfos[i];
            if (ei.getBar() == src) {
                double v = ei.getBar().getValue() / 1000.;
                if (v < 0)
                    v = 0;
                if (v > 1)
                    v = 1;
                ei.setValue((ei.getMaxval() - ei.getMinval()) * v + ei.getMinval());
                ei.setValue(Math.round(ei.getValue() / ei.getMinval()) * ei.getMinval());
                elm.setEditValue(i, ei);
                ei.getTextf().setText(unitString(ei));
                cframe.needAnalyze();
            }
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        Object src = e.getItemSelectable();
        int i;
        boolean changed = false;
        for (i = 0; i != einfocount; i++) {
            EditInfo ei = einfos[i];
            if (ei.getChoice() == src || ei.getCheckbox() == src) {
                elm.setEditValue(i, ei);
                if (ei.isNewDialog())
                    changed = true;
                cframe.needAnalyze();
            }
        }
        if (changed) {
            setVisible(false);
            CirSim.editDialog = new EditDialog(elm, cframe);
            CirSim.editDialog.setVisible(true);
        }
    }

    @Override
    protected void processEvent(AWTEvent e) {
        if (e.getID() == Event.WINDOW_DESTROY) {
            finish();
        }
        super.processEvent(e);
    }

    private void finish() {
        cframe.requestFocus();
        setVisible(false);
        CirSim.editDialog = null;
    }

    private void setBar(EditInfo ei) {
        int x = (int) (barmax * (ei.getValue() - ei.getMinval()) / (ei.getMaxval() - ei.getMinval()));
        ei.getBar().setValue(x);
    }

    public Editable getElm() {
        return elm;
    }
}

