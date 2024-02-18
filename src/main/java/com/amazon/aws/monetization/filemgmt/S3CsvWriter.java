package com.amazon.aws.monetization.filemgmt;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.opencsv.CSVWriter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class S3CsvWriter {
    private final AmazonS3 s3Client;
    private final ByteArrayOutputStream stream;

    private final CSVWriter writer;
    private final OutputStreamWriter streamWriter;

    private final String bucket;

    private final String key;

    private final String expectedBucketOwner;


    public S3CsvWriter(AmazonS3 s3Client, String bucket, String key, String expectedBucketOwner) {
        this.bucket = bucket;
        this.key = key;
        this.expectedBucketOwner = expectedBucketOwner;
        this.s3Client = s3Client;

        this.stream = new ByteArrayOutputStream();
        this.streamWriter = new OutputStreamWriter(stream, StandardCharsets.UTF_8);
        this.writer = new CSVWriter(streamWriter);
    }

    public void append(String[] nextLine) throws IOException {
        writer.writeNext(nextLine);
    }

    public void write() throws IOException {
        writer.flush();

        GetBucketEncryptionResult getBucketEncryptionResult = this.s3Client.getBucketEncryption(bucket);
        ServerSideEncryptionByDefault serverSideEncryptionByDefault =
                getBucketEncryptionResult.getServerSideEncryptionConfiguration().getRules().get(0).getApplyServerSideEncryptionByDefault();
        String algorithm = serverSideEncryptionByDefault.getSSEAlgorithm();
        String kmsMasterKeyId = serverSideEncryptionByDefault.getKMSMasterKeyID();

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
