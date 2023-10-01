package kr.easw.lesson02.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import kr.easw.lesson02.model.dto.AWSKeyDto;
import lombok.SneakyThrows;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.UUID;
@Service
public class AWSService {
    private static final String BUCKET_NAME = "easw-random-bucket-" + UUID.randomUUID();
    private static AmazonS3 s3Client = null;

    public ByteArrayResource download(String fileName) {
        try {
           GetObjectRequest getObjectRequest = new GetObjectRequest(BUCKET_NAME, fileName);
            S3Object s3Object = s3Client.getObject(getObjectRequest);
            byte[] objectBytes = StreamUtils.copyToByteArray(s3Object.getObjectContent());
            ByteArrayResource byteArrayResource = new ByteArrayResource(objectBytes);
            return byteArrayResource;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public void initAWSAPI(AWSKeyDto awsKey) {
        s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(awsKey.getApiKey(), awsKey.getApiSecretKey())))
                .withRegion(Regions.AP_NORTHEAST_2)
                .build();
        for (Bucket bucket : s3Client.listBuckets()) {
            if (bucket.getName().startsWith("easw-random-bucket-")) {
                s3Client.listObjects(bucket.getName())
                        .getObjectSummaries()
                        .forEach(it -> s3Client.deleteObject(bucket.getName(), it.getKey()));
                s3Client.deleteBucket(bucket.getName());
            }
        }
        s3Client.createBucket(BUCKET_NAME);
    }

    public boolean isInitialized() {
        return s3Client != null;
    }

    public List<String> getFileList() {
        return s3Client.listObjects(BUCKET_NAME).getObjectSummaries().stream().map(S3ObjectSummary::getKey).toList();
    }

    @SneakyThrows
    public void upload(MultipartFile file) {
        s3Client.putObject(BUCKET_NAME, file.getOriginalFilename(), new ByteArrayInputStream(file.getResource().getContentAsByteArray()), new ObjectMetadata());
    }
}
