package org.circuitsymphony.ui;

import org.apache.commons.io.FilenameUtils;
import org.circuitsymphony.util.JarUtils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;
import java.util.function.Supplier;

/**
 * Class that allows to save and open files in .cmf (CircuitSymphony File) extension.
 * What it does is save the text created by Export in a .cmf file and read it back when opened.
 *
 */
public class SaveOpenDialog {
    private final static String FILE_EXTENSION_TXT = "txt";
    private final static String FILE_EXTENSION_CMF = "cmf";
    private final static String FILE_EXTENSION_JSON = "json";
    private final static String ELEMENT_ID_CHANGED_MSG = "Element ID was changed, you must save this file as .CMF " +
            "file to avoid information loss.";

    private final Frame parent;
    private final JFileChooser chooser;
    private File activeFile;

    public SaveOpenDialog(Frame parent) {
        this.parent = parent;

        // Disable renaming of files
        Boolean prevReadOnly = UIManager.getBoolean("FileChooser.readOnly");
        UIManager.put("FileChooser.readOnly", Boolean.TRUE);

        // Create file chooser with custom file filter
        chooser = new JFileChooser() {
            @Override
            public void approveSelection() {
                String s = addExtensionIfNeeded(getSelectedFile().getAbsolutePath());
                File f = new File(s);

                if (f.exists() && getDialogType() == SAVE_DIALOG) {
                    int result = JOptionPane.showConfirmDialog(this, "The file exists, overwrite?", "Existing file", JOptionPane.YES_NO_CANCEL_OPTION);
                    switch (result) {
                        case JOptionPane.YES_OPTION:
                            super.approveSelection();
                            return;
                        case JOptionPane.NO_OPTION:
                            return;
                        case JOptionPane.CLOSED_OPTION:
                            return;
                        case JOptionPane.CANCEL_OPTION:
                            cancelSelection();
                            return;
                    }
                }
                super.approveSelection();
            }
        };
        chooser.setCurrentDirectory(new File(JarUtils.getJarPath(SaveOpenDialog.class)));
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("CircuitSymphony | *." + FILE_EXTENSION_CMF, FILE_EXTENSION_CMF));
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("CircuitSymphony | *." + FILE_EXTENSION_JSON, FILE_EXTENSION_JSON));
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("Old format compatible circuits | *." + FILE_EXTENSION_TXT, FILE_EXTENSION_TXT));

        UIManager.put("FileChooser.readOnly", prevReadOnly);
    }

    /**
     * Save file as operation.
     *
     * @return true if saved, false otherwise.
     */
    public boolean saveAs(String txtFormat, String cmfFormat, Supplier<String> lazyJsonFormat, boolean elementIdChanged) {
        while (true) {
            int result = chooser.showSaveDialog(parent);
            if (result == JFileChooser.APPROVE_OPTION) {
                activeFile = new File(addExtensionIfNeeded(chooser.getSelectedFile().getAbsolutePath()));
                String extension = FilenameUtils.getExtension(activeFile.getName());
                if (extension.equals(FILE_EXTENSION_TXT) && elementIdChanged) {
                    JOptionPane.showMessageDialog(parent, ELEMENT_ID_CHANGED_MSG);
                    continue;
                } else if (extension.equals(FILE_EXTENSION_TXT)) {
                    writeFile(txtFormat);
                } else if (extension.equals(FILE_EXTENSION_JSON)) {
                    writeFile(lazyJsonFormat.get());
                } else {
                    writeFile(cmfFormat);
                }
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * Save file operation.
     *
     * @return true if saved, false otherwise.
     */
    public boolean save(String txtFormat, String cmfFormat, Supplier<String> lazyJsonFormat, boolean elementIdChanged) {
        if (activeFile == null) {
            return saveAs(txtFormat, cmfFormat, lazyJsonFormat, elementIdChanged);
        } else {
            String extension = FilenameUtils.getExtension(activeFile.getName());
            if (extension.equals(FILE_EXTENSION_TXT) && elementIdChanged) {
                JOptionPane.showMessageDialog(parent, ELEMENT_ID_CHANGED_MSG);
                return saveAs(txtFormat, cmfFormat, lazyJsonFormat, true);
            } else if (extension.equals(FILE_EXTENSION_TXT)) {
                writeFile(txtFormat);
            } else if (extension.equals(FILE_EXTENSION_JSON)) {
                writeFile(lazyJsonFormat.get());
            } else {
                writeFile(cmfFormat);
            }
            return true;
        }
    }

    private void writeFile(String s) {
        try {
            PrintWriter out = new PrintWriter(activeFile);
            out.println(s);
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public String open() {
        int accept = chooser.showOpenDialog(parent);
        if (accept == JFileChooser.APPROVE_OPTION) {
            return load(chooser.getSelectedFile());
        }
        return null;
    }

    private String load(File f) {
        return load(f.getAbsolutePath());
    }

    public String load(String s) {
        try {
            activeFile = new File(s);
            return readFile(s);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private String readFile(String fileName) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append("\n");

                line = br.readLine();
            }

            return sb.toString();
        }
    }

    private String addExtensionIfNeeded(String s) {
        if (s.toLowerCase().endsWith("." + FILE_EXTENSION_CMF) ||
                s.toLowerCase().endsWith("." + FILE_EXTENSION_JSON)) {
            return s;
        }
        return s + "." + FILE_EXTENSION_CMF;
    }

    public String getCurrentFileNameExtension() {
        return FilenameUtils.getExtension(activeFile.getName());
    }

    public String getFileName() {
        return activeFile.getName();
    }
}
