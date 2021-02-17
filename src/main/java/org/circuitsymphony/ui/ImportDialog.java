package org.circuitsymphony.ui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ImportDialog extends Dialog implements ActionListener {
    private final CirSim cframe;
    private final Button importButton;
    private final Button closeButton;
    private final TextArea text;

    public ImportDialog(CirSim sim, String str, boolean url) {
        super(sim, (str.length() > 0) ? "Export" : "Import", false);
        cframe = sim;
        setLayout(new ImportDialogLayout());
        add(text = new TextArea(str, 10, 60, TextArea.SCROLLBARS_BOTH));
        importButton = new Button("Import");
        if (!url)
            add(importButton);
        importButton.addActionListener(this);
        add(closeButton = new Button("Close"));
        closeButton.addActionListener(this);
        Point x = cframe.getLocationOnScreen();
        setSize(400, 300);
        Dimension d = getSize();
        setLocation(x.x + (cframe.winSize.width - d.width) / 2,
                x.y + (cframe.winSize.height - d.height) / 2);
        setVisible(true);
        if (str.length() > 0)
            text.selectAll();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == importButton) {
            cframe.getCircuitLoader().loadCircuit(text.getText());
            setVisible(false);
        }
        if (src == closeButton)
            setVisible(false);
    }

    @Override
    protected void processEvent(AWTEvent e) {
        if (e.getID() == Event.WINDOW_DESTROY) {
            cframe.requestFocus();
            setVisible(false);
            CirSim.impDialog = null;
        }
        super.processEvent(e);
    }
}
    
