package org.circuitsymphony.ui;

import java.awt.*;

/**
 * CircuitSymphony main class. Launches main application window and controls splash screen.
 */
public class CircuitSymphony {
    public static void main(String args[]) {
        EventQueue.invokeLater(() -> {
            CirSim cirSim = new CirSim();
//            LogoFrame logoFrame = new LogoFrame();
            EventQueue.invokeLater(() -> {
                try {
                    cirSim.init(args.length > 0 ? args[0] : null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
//                logoFrame.dispose();
                cirSim.setFrameAndShow();
            });
        });
    }
}
