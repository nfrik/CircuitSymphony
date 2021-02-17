package org.circuitsymphony.rest.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class StorageProperties {

    /**
     * Folder location for storing files
     */
    @Value("${storage.file.directory.location}")
    private String location;

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

}
