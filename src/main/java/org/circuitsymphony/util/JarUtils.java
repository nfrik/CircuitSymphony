package org.circuitsymphony.util;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;

/**
 * Jar related utils.
 */
public class JarUtils {
    public static String getJarPath(Class<?> caller) {
        try {
            URL url = caller.getProtectionDomain().getCodeSource().getLocation();
            String path = URLDecoder.decode(url.getFile(), "UTF-8");

            // remove jar name from path
            if (OsUtils.isWindows())
                path = path.substring(1, path.lastIndexOf('/')); // cut first '/' for Windows
            else
                path = path.substring(0, path.lastIndexOf('/'));

            if (path.endsWith("target/classes")) //launched from ide
                path = path.substring(0, path.length() - "/target/classes".length());

            path = path.replace("/", File.separator);
            return path + File.separator;
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Failed to get jar path due to unsupported encoding!", e);
        }
    }
}
