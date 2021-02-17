package org.circuitsymphony.util;

import org.circuitsymphony.element.CircuitElm;
import org.circuitsymphony.engine.CircuitEngine;

import java.lang.reflect.Constructor;


public class CirSimUtil {

    public static CircuitElm constructElement(CircuitEngine engine, Class<?> c, int x0, int y0) {
        // find element class
        Class<?> carr[] = new Class[3];
        carr[0] = CircuitEngine.class;
        carr[1] = carr[2] = int.class;
        Constructor<?> cstr;
        try {
            cstr = c.getConstructor(carr);
        } catch (NoSuchMethodException ee) {
            // ee.printStackTrace();
            System.out.println("NoSuchMethodException for " + c);
            return null;
        } catch (Exception ee) {
            ee.printStackTrace();
            return null;
        }

        // invoke constructor with starting coordinates
        Object oarr[] = new Object[3];
        oarr[0] = engine;
        oarr[1] = x0;
        oarr[2] = y0;
        try {
            return (CircuitElm) cstr.newInstance(oarr);
        } catch (Exception ee) {
            ee.printStackTrace();
        }
        return null;
    }


}
