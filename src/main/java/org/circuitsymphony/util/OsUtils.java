package org.circuitsymphony.util;

/**
 * Operating system related utils.
 *
 */
public class OsUtils {
    private static final String OS = System.getProperty("os.name").toLowerCase();
    private static final boolean WINDOWS = OS.contains("win");
    private static final boolean MAC = OS.contains("mac");
    private static final boolean UNIX = OS.contains("nix") || OS.contains("nux") || OS.contains("aix");

    /**
     * @return {@code true} if the current OS is Windows
     */
    public static boolean isWindows() {
        return WINDOWS;
    }

    /**
     * @return {@code true} if the current OS is Mac
     */
    public static boolean isMac() {
        return MAC;
    }

    /**
     * @return {@code true} if the current OS is Unix
     */
    public static boolean isUnix() {
        return UNIX;
    }
}
