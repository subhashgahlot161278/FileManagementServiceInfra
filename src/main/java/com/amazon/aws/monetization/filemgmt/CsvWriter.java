package com.amazon.aws.monetization.filemgmt;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.opencsv.CSVWriter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class CsvWriter {
    private AmazonS3 s3Client;

    public CsvWriter(AmazonS3 s3Client) {
        this.s3Client = s3Client;
    }

    public void writeRecordsToS3(String bucket, String key, String expectedBucketOwner, List<String[]> records) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        OutputStreamWriter streamWriter = new OutputStreamWriter(stream, StandardCharsets.UTF_8);

        // Retrieve s3 bucket SSE-KMS configuration
        GetBucketEncryptionResult getBucketEncryptionResult = this.s3Client.getBucketEncryption(bucket);
        ServerSideEncryptionByDefault serverSideEncryptionByDefault =
                getBucketEncryptionResult.getServerSideEncryptionConfiguration().getRules().get(0).getApplyServerSideEncryptionByDefault();
        String algorithm = serverSideEncryptionByDefault.getSSEAlgorithm();
        String kmsMasterKeyId = serverSideEncryptionByDefault.getKMSMasterKeyID();

        try (CSVWriter writer = new CSVWriter(streamWriter)) {
            writer.writeAll(records, true);
            writer.flush();
            ObjectMetadata meta = new ObjectMetadata();
            meta.setContentLength(stream.toByteArray().length);
            meta.setSSEAlgorithm(algorithm);

            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    bucket,
                    key,
                    new ByteArrayInputStream(stream.toByteArray()),
                    meta
            );

            if(null != expectedBucketOwner) {
                putObjectRequest.setExpectedBucketOwner(expectedBucketOwner);
            }

            putObjectRequest.withSSEAwsKeyManagementParams(new SSEAwsKeyManagementParams(kmsMasterKeyId));

            this.s3Client.putObject(putObjectRequest);
        }
    }
}
