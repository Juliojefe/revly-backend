package com.example.revly.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
public class FileUploadService {

    @Autowired
    private S3Client s3Client;

    @Autowired
    private ImageResizeService imageResizeService;

    @Value("${aws.s3.bucket.name}")
    private String bucketName;

    /**
     * unified upload method for the entire application.
     * automatically optimizes (resizes + compresses) every image before uploading.
     */
    public String uploadFile(MultipartFile file) throws IOException {
        byte[] optimizedData = imageResizeService.optimizeImage(file);
        String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "image.jpg";
        String fileName = UUID.randomUUID().toString() + "_" + originalName;
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();
        s3Client.putObject(request, RequestBody.fromBytes(optimizedData));
        return s3Client.utilities()
                .getUrl(builder -> builder.bucket(bucketName).key(fileName).build())
                .toString();
    }
}