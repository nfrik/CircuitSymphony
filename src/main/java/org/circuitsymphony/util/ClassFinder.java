package org.circuitsymphony.util;

import java.util.Arrays;
import java.util.List;


public class ClassFinder {

    private static final List<String> availablePackages = Arrays.asList(
            "org.circuitsymphony.element.",
            "org.circuitsymphony.element.active.",
            "org.circuitsymphony.element.cdseries.",
            "org.circuitsymphony.element.chips.",
            "org.circuitsymphony.element.devices.",
            "org.circuitsymphony.element.io.",
            "org.circuitsymphony.element.logicgates.",
            "org.circuitsymphony.element.passive.",
            "org.circuitsymphony.ui.",
            "org.circuitsymphony.util.",
            "org.circuitsymphony."
    );

    public static Class<?> forName(String className) {
        for (String availablePackage : availablePackages) {
            try {
                return Class.forName(availablePackage + className);
            } catch (ClassNotFoundException ignored) {
            }
        }

        return null;
    }

}
