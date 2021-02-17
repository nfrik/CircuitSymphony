package org.circuitsymphony.manager;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * Used to write result of simulation measurement to file.
 */
public class MeasurementWriter {
    private boolean replaceDotWithComma;
    private String separationString = "\t";

    public MeasurementWriter() {
    }

    /**
     * Writes measurement of single element to output file
     *
     * @param file         output file, will be overridden
     * @param elementId    elementId that measurement data will be written
     * @param measurements measurements results
     */
    public void writeSingleElementToFile(File file, int elementId, ArrayList<Measurement> measurements) {
        if (measurements.size() == 0) return;
        try {
            PrintWriter writer = new PrintWriter(file, "UTF-8");
            writer.println(elementId);
            for (Measurement measurement : measurements) {
                writeRecord(writer, elementId, measurement);
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Writes measurement of all elements to output file
     *
     * @param file         output file, will be overridden
     * @param measurements measurements results
     */
    public void writeAllToFile(File file, ArrayList<Measurement> measurements) {
        if (measurements.size() == 0) return;
        try {
            PrintWriter writer = new PrintWriter(file, "UTF-8");
            for (Integer elementId : measurements.get(0).getRecords().keySet()) { // for each tracked element
                writer.println(elementId);
                for (Measurement measurement : measurements) {
                    writeRecord(writer, elementId, measurement);
                }
                writer.println();
            }

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeRecord(PrintWriter writer, int elementId, Measurement m) {
        ElementRecord elmRecord = m.getRecords().get(elementId);
        String outString = m.getTime() + separationString + elmRecord.getCurrent() + separationString + elmRecord.getVoltageDiff();
        if (replaceDotWithComma) {
            outString = outString.replace(".", ",");
        }
        writer.println(outString);
    }

    public boolean isReplaceDotWithComma() {
        return replaceDotWithComma;
    }

    /**
     * @param replaceDotWithComma if true then comma will be used instead of dot to separate decimal part of number
     */
    public void setReplaceDotWithComma(boolean replaceDotWithComma) {
        this.replaceDotWithComma = replaceDotWithComma;
    }

    public String getSeparationString() {
        return separationString;
    }

    /**
     * Text that will be used to separate values.
     */
    public void setSeparationString(String separationString) {
        this.separationString = separationString;
    }
}
