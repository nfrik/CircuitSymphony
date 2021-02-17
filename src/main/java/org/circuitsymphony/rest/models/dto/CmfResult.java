package org.circuitsymphony.rest.models.dto;

/**
 * Result of API method used to get currently loaded graph circuit as CMF file.
 */
public class CmfResult {
    private final String key;
    private final String cmf;

    public CmfResult(String key, String cmf) {
        this.key = key;
        this.cmf = cmf;
    }

    public String getKey() {
        return key;
    }

    public String getCmf() {
        return cmf;
    }
}
