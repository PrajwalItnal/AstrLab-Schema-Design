package com.astralab.schema.service;

import com.astralab.schema.exception.FileStorageException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path storageDirectory;

    public FileStorageService(@Value("${app.storage.upload-dir:./data/uploads}") String uploadDir) {
        this.storageDirectory = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(this.storageDirectory);
        } catch (IOException e) {
            throw new FileStorageException("Could not initialize persistent storage directory: " + storageDirectory, e);
        }
    }

    public static class StoredFileInfo {
        private final String storedFilename;
        private final String absolutePath;
        private final String relativePath;
        private final long sizeBytes;
        private final String checksumSha256;

        public StoredFileInfo(String storedFilename, String absolutePath, String relativePath, long sizeBytes, String checksumSha256) {
            this.storedFilename = storedFilename;
            this.absolutePath = absolutePath;
            this.relativePath = relativePath;
            this.sizeBytes = sizeBytes;
            this.checksumSha256 = checksumSha256;
        }

        public String getStoredFilename() {
            return storedFilename;
        }

        public String getAbsolutePath() {
            return absolutePath;
        }

        public String getRelativePath() {
            return relativePath;
        }

        public long getSizeBytes() {
            return sizeBytes;
        }

        public String getChecksumSha256() {
            return checksumSha256;
        }
    }

    public StoredFileInfo storeFile(MultipartFile file, String datasetId) {
        if (file.isEmpty()) {
            throw new FileStorageException("Cannot store empty file.");
        }

        try {
            String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "dataset";
            String extension = "";
            int extIdx = originalFilename.lastIndexOf('.');
            if (extIdx >= 0) {
                extension = originalFilename.substring(extIdx);
            }

            // Create dataset specific directory for isolation and persistent structure
            Path datasetDir = this.storageDirectory.resolve(datasetId).normalize();
            Files.createDirectories(datasetDir);

            String storedFilename = "data_" + UUID.randomUUID() + extension;
            Path targetLocation = datasetDir.resolve(storedFilename).normalize();

            // Stream directly to disk to minimize memory footprint
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }

            long sizeBytes = Files.size(targetLocation);
            String checksum = computeSha256(targetLocation);
            String relativePath = this.storageDirectory.relativize(targetLocation).toString().replace('\\', '/');

            return new StoredFileInfo(
                    storedFilename,
                    targetLocation.toString(),
                    relativePath,
                    sizeBytes,
                    checksum
            );
        } catch (IOException e) {
            throw new FileStorageException("Failed to store uploaded file persistently", e);
        }
    }

    public Path getFilePath(String storedPathOrKey) {
        Path path = Paths.get(storedPathOrKey);
        if (!path.isAbsolute()) {
            path = this.storageDirectory.resolve(storedPathOrKey).normalize();
        }
        if (!Files.exists(path)) {
            // Also check if storedPathOrKey is relative to storageDirectory
            Path candidate = this.storageDirectory.resolve(storedPathOrKey).normalize();
            if (Files.exists(candidate)) {
                return candidate;
            }
            throw new FileStorageException("File not found on persistent storage: " + storedPathOrKey);
        }
        return path;
    }

    public InputStream openFileStream(String storedPathOrKey) throws IOException {
        Path path = getFilePath(storedPathOrKey);
        return Files.newInputStream(path);
    }

    public boolean fileExists(String storedPathOrKey) {
        try {
            Path path = Paths.get(storedPathOrKey);
            if (!path.isAbsolute()) {
                path = this.storageDirectory.resolve(storedPathOrKey).normalize();
            }
            return Files.exists(path);
        } catch (Exception e) {
            return false;
        }
    }

    private String computeSha256(Path file) throws IOException {
        try (InputStream is = Files.newInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int read;
            while ((read = is.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
            byte[] hash = digest.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
