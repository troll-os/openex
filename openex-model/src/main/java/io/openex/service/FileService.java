package io.openex.service;

import io.minio.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import io.openex.config.MinioConfig;
import io.openex.database.model.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

@Service
public class FileService {

    private static final Logger LOGGER = Logger.getLogger(FileService.class.getName());
    private MinioConfig minioConfig;
    private MinioClient minioClient;

    @Autowired
    public void setMinioConfig(MinioConfig minioConfig) {
        this.minioConfig = minioConfig;
    }

    @Autowired
    public void setMinioClient(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    public void uploadFile(String name, InputStream data, long size, String contentType) throws Exception {
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(minioConfig.getBucket())
                        .object(name)
                        .stream(data, size, -1)
                        .contentType(contentType)
                        .build());
    }

    public String uploadStream(String path, String name, InputStream data) throws Exception {
        String file = path + "/" + name;
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(minioConfig.getBucket())
                        .object(file)
                        .userMetadata(Map.of("filename", name))
                        .stream(data, data.available(), -1)
                        .build());
        return file;
    }

    public void deleteFile(String name) throws Exception {
        minioClient.removeObject(RemoveObjectArgs
                .builder()
                .bucket(minioConfig.getBucket())
                .object(name)
                .build());
    }

    public void deleteDirectory(String directory) {
        Iterable<Result<Item>> files = minioClient.listObjects(ListObjectsArgs
                .builder()
                .bucket(minioConfig.getBucket())
                .recursive(true)
                .prefix(directory)
                .build());
        List<DeleteObject> deleteObjects = new ArrayList<>();
        files.forEach(itemResult -> {
            try {
                Item item = itemResult.get();
                deleteObjects.add(new DeleteObject(item.objectName()));
            } catch (Exception e) {
                // Dont care
            }
        });
        Iterable<Result<DeleteError>> removedObjects = minioClient.removeObjects(RemoveObjectsArgs
                .builder()
                .bucket(minioConfig.getBucket())
                .objects(deleteObjects)
                .build());
        for (Result<DeleteError> result : removedObjects) {
            try {
                DeleteError error = result.get();
                LOGGER.severe("Error in deleting object " + error.objectName() + "; " + error.message());
            } catch (Exception e) {
                // Nothing to do
            }
        }
    }

    public void uploadFile(String name, MultipartFile file) throws Exception {
        uploadFile(name, file.getInputStream(), file.getSize(), file.getContentType());
    }

    private Optional<InputStream> getFilePath(String name) {
        try {
            GetObjectResponse objectStream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(minioConfig.getBucket())
                            .object(name)
                            .build());
            InputStreamResource streamResource = new InputStreamResource(objectStream);
            return Optional.of(streamResource.getInputStream());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Optional<InputStream> getFile(Document document) {
        return getFilePath(document.getTarget());
    }

    public Optional<FileContainer> getFileContainer(String fileTarget) {
        try {
            StatObjectResponse response = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(minioConfig.getBucket())
                            .object(fileTarget)
                            .build());
            String filename = response.userMetadata().get("filename");
            Optional<InputStream> inputStream = getFilePath(fileTarget);
            FileContainer fileContainer = new FileContainer(filename, response.contentType(), inputStream.orElseThrow());
            return Optional.of(fileContainer);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
