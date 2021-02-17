package org.circuitsymphony.rest.services;

import org.circuitsymphony.rest.exceptions.SymphonyRestServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class FileSystemStorageService implements StorageService {

    private final StorageProperties properties;
    private Path rootLocation;

    @Autowired
    public FileSystemStorageService(StorageProperties properties) {
        this.properties = properties;
    }

    @Override
    public File store(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new SymphonyRestServiceException("Failed to store empty file " + file.getOriginalFilename());
            }
            Path target = this.rootLocation.resolve(file.getOriginalFilename());
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            return target.toFile();
        } catch (IOException e) {
            throw new SymphonyRestServiceException("Failed to store file " + file.getOriginalFilename(), e);
        }
    }

    @Override
    public File storeAsTemp(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new SymphonyRestServiceException("Failed to store empty file " + file.getOriginalFilename());
            }

            File tempFile = File.createTempFile(UUID.randomUUID().toString(), file.getOriginalFilename());

            file.transferTo(tempFile);

            return tempFile;
        } catch (IOException e) {
            throw new SymphonyRestServiceException("Failed to store file " + file.getOriginalFilename(), e);
        }
    }

    @Override
    public Stream<Path> loadAll() {
        try {
            return Files.walk(this.rootLocation, 1)
                    .filter(path -> !path.equals(this.rootLocation))
                    .map(this.rootLocation::relativize);
        } catch (IOException e) {
            throw new SymphonyRestServiceException("Failed to read stored files", e);
        }

    }

    @Override
    public Path load(String filename) {
        return rootLocation.resolve(filename);
    }

    @Override
    public Resource loadAsResource(String filename) {
        try {
            Path file = load(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new SymphonyRestServiceException("Could not read file: " + filename);

            }
        } catch (MalformedURLException e) {
            throw new SymphonyRestServiceException("Could not read file: " + filename, e);
        }
    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
    }

    @PostConstruct
    @Override
    public void init() {
        try {
            this.rootLocation = Paths.get(properties.getLocation());
            if (!Files.exists(rootLocation)) {
                Files.createDirectory(rootLocation);
            }
        } catch (IOException e) {
            throw new SymphonyRestServiceException("Could not initialize storage", e);
        }
    }
}
