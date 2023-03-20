package com.MedLakh.uplofile.controllers;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import com.MedLakh.uplofile.utils.DownloadProgress;
import com.MedLakh.uplofile.utils.MessageResponse;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.transfer.Download;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;

@RestController
@RequestMapping("/api/services/documents")
@CrossOrigin("*")
public class DocumentController {

    @Autowired
    private AmazonS3 amazonS3;

    @Value("${digitalocean.spaces.bucket-name}")
    private String bucketName;

    @Value("${frontend.url}")
    private String frontendUrl;

    private Map<String, Float> progressUpload = new HashMap<String, Float>();
    private Map<String, DownloadProgress> progressDownload = new HashMap<String, DownloadProgress>();

    @GetMapping("/upload-progress/{id}")
    public ResponseEntity<Object> getUploadProgress(@PathVariable(required = true) String id) {
        return ResponseEntity.ok(progressUpload.get(id));
    }

    @GetMapping("/download-progress/{id}")
    public ResponseEntity<Object> getDownloadProgress(@PathVariable(required = true) String id) {
        return ResponseEntity.ok(progressDownload.get(id));
    }

    @PostMapping
    public ResponseEntity<String> uploadFile(@RequestPart("file") MultipartFile file, @RequestPart("id") String id) {
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());
            Instant instant = Instant.now();
            String fileName = instant.getEpochSecond() + "_" + file.getOriginalFilename();

            TransferManager transferManager = TransferManagerBuilder.standard().withS3Client(amazonS3).build();
            Upload upload = transferManager.upload(bucketName, fileName, file.getInputStream(), metadata);

            this.progressUpload.put(id, 0f);
            while (!upload.isDone()) {
                this.progressUpload.put(id, (float) upload.getProgress().getPercentTransferred());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    System.out.println("Erreur thread sleep");
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println("Erreur thread sleep");
            }
            this.progressUpload.remove(id);

            String fileUrl = frontendUrl + "?file=" + UriUtils.encode(fileName, StandardCharsets.UTF_8);
            return ResponseEntity.ok(fileUrl);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload file");
        }
    }

    @GetMapping
    public ResponseEntity<Object> downloadFile(@RequestParam(required = true) String file,
            @RequestParam("id") String id) {
        File tempFile = null;
        byte[] fileBytes = null;
        try {
            String fileName = UriUtils.decode(file, StandardCharsets.UTF_8);
            S3Object s3Object = amazonS3.getObject(bucketName, fileName);

            TransferManager transferManager = TransferManagerBuilder.standard().withS3Client(amazonS3).build();
            tempFile = File.createTempFile("download", ".tmp");

            Download download = transferManager.download(bucketName, fileName, tempFile);

            this.progressDownload.put(id, new DownloadProgress(0f, download.getProgress().getBytesTransferred(),
                    download.getProgress().getTotalBytesToTransfer()));
            while (!download.isDone()) {
                this.progressDownload.put(id,
                        new DownloadProgress((float) download.getProgress().getPercentTransferred(),
                                download.getProgress().getBytesTransferred(),
                                download.getProgress().getTotalBytesToTransfer()));
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    System.out.println("Erreur thread sleep");
                }
            }
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                System.out.println("Erreur thread sleep");
            }
            this.progressDownload.remove(id);

            fileBytes = Files.readAllBytes(tempFile.toPath());

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");

            return ResponseEntity.ok()
                    .contentLength(tempFile.length())
                    .contentType(MediaType.parseMediaType(s3Object.getObjectMetadata().getContentType()))
                    .headers(headers)
                    .body(fileBytes);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("A technical error has occurred. Please contact support."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("The requested document was not found."));
        } finally {
            if (tempFile != null) {
                tempFile.delete();
            }
        }
    }
}
