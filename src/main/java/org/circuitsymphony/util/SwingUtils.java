package org.circuitsymphony.util;

import java.awt.*;

/**
 * General AWT / Swing utilities
 */
public class SwingUtils {
    public static boolean isModalDialogPresent() {
        Window[] windows = Window.getWindows();
        if (windows == null) return false;
        for (Window w : windows) {
            if (w.isShowing() && w instanceof Dialog && ((Dialog) w).isModal()) {
                return true;
            }
        }
        return false;
    }

    public static void setLocationToCenter(Component caller, Component owner) {
        if (owner == null)
            caller.setLocation(100, 100);
        else {
            int xw = (owner.getWidth() - caller.getWidth()) / 2;
            int x = owner.getX() + xw;
            int yw = (owner.getHeight() - caller.getHeight()) / 2;
            int y = owner.getY() + yw;

            caller.setLocation(x, y);
        }
    }
}
