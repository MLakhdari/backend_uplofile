package com.MedLakh.uplofile.services;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class PurgeService {

	@Autowired
    private AmazonS3 amazonS3;
    
    @Value("${digitalocean.spaces.bucket-name}")
    private String bucketName;


    @Scheduled(fixedDelay = 3600000)
    public void purgeOldFiles() {
        Instant oneHourAgo = Instant.now().minus(1, ChronoUnit.HOURS);
        ObjectListing objects = amazonS3.listObjects(bucketName);
        for (S3ObjectSummary objectSummary : objects.getObjectSummaries()) {
            if (objectSummary.getLastModified().toInstant().isBefore(oneHourAgo)) {
            	amazonS3.deleteObject(new DeleteObjectRequest(bucketName, objectSummary.getKey()));
            }
        }
    }
}
