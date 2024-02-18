package com.amazon.aws.monetization.filemgmt.utils;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.CopyObjectResult;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.DeleteObjectsResult;
import com.amazonaws.services.s3.model.GetBucketEncryptionResult;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.SSEAwsKeyManagementParams;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.ServerSideEncryptionByDefault;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class S3Util {
    public static void uploadtoS3(AmazonS3 s3Client,
                                  Path localFilePath,
                                  String bucketName,
                                  String s3key,
                                  String expectedBucketOwner) throws IOException, SdkClientException {
        InputStream in = null;
        String algorithm = getSSEAlgorithm(s3Client, bucketName);
        String kmsMasterKeyId = getBucketKMSKey(s3Client, bucketName);

        try {
            ObjectMetadata meta = new ObjectMetadata();
            meta.setSSEAlgorithm(algorithm);

            in = new FileInputStream(localFilePath.toFile());
            final PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName,
                    s3key,
                    in,
                    new ObjectMetadata());

            if(null != expectedBucketOwner) {
                putObjectRequest.setExpectedBucketOwner(expectedBucketOwner);
            }

            putObjectRequest.withSSEAwsKeyManagementParams(new SSEAwsKeyManagementParams(kmsMasterKeyId));

            s3Client.putObject(putObjectRequest);
            in.close();
        } finally {
            if(in != null) in.close();
        }
    }

    public static void uploadtoS3(AmazonS3 s3Client,
                                  Path localFilePath,
                                  String bucketName,
                                  String s3key,
                                  String expectedBucketOwner,
                                  ObjectMetadata meta
                                  ) throws IOException, SdkClientException {
        InputStream in = null;
        String kmsMasterKeyId = getBucketKMSKey(s3Client, bucketName);
        try {
            in = new FileInputStream(localFilePath.toFile());
            final PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName,
                    s3key,
                    in,
                    meta);

            if(null != expectedBucketOwner) {
                putObjectRequest.setExpectedBucketOwner(expectedBucketOwner);
            }

            putObjectRequest.withSSEAwsKeyManagementParams(new SSEAwsKeyManagementParams(kmsMasterKeyId));

            s3Client.putObject(putObjectRequest);
            in.close();
        } finally {
            if(in != null) in.close();
        }
    }

    public static void createFolder(AmazonS3 s3Client,
                                  String bucketName,
                                  String folderName,
                                  String expectedBucketOwner) throws IOException, SdkClientException {
        InputStream in = null;
        // Retrieve s3 bucket SSE-KMS configuration
        String algorithm = getSSEAlgorithm(s3Client, bucketName);
        String kmsMasterKeyId = getBucketKMSKey(s3Client, bucketName);

        final String SUFFIX = "/";
        try {
            ObjectMetadata meta = new ObjectMetadata();
            meta.setSSEAlgorithm(algorithm);
            meta.setContentLength(0L);

            in = new ByteArrayInputStream(new byte[0]);

            String key = folderName;

            if (!folderName.endsWith(SUFFIX)) {
                key += SUFFIX;
            }

            final PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName,
                    key,
                    in,
                    new ObjectMetadata());

            if(null != expectedBucketOwner) {
                putObjectRequest.setExpectedBucketOwner(expectedBucketOwner);
            }

            putObjectRequest.withSSEAwsKeyManagementParams(new SSEAwsKeyManagementParams(kmsMasterKeyId));

            s3Client.putObject(putObjectRequest);
            in.close();
        } finally {
            if(in != null) in.close();
        }
    }

    public static S3Object getObjectFromS3(AmazonS3 s3Client, String bucket, String key, String expectedBucketOwner) {
        GetObjectRequest request = new GetObjectRequest(bucket, key);

        if (null != expectedBucketOwner) {
            request.setExpectedBucketOwner(expectedBucketOwner);
        }

        return s3Client.getObject(request);
    }

    public static CopyObjectResult copyS3Object(AmazonS3 s3Client, String sourceBucket, String sourceKey,
                                                String destinationBucket, String destinationKey,
                                                String expectedBucketOwner, String fileContentType) throws SdkClientException{
        CopyObjectRequest request = new CopyObjectRequest(sourceBucket, sourceKey, destinationBucket, destinationKey);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(fileContentType);
        request.setNewObjectMetadata(metadata);
        String kmsMasterKeyId = getBucketKMSKey(s3Client, destinationBucket);

        request.withSSEAwsKeyManagementParams(new SSEAwsKeyManagementParams(kmsMasterKeyId));
        if (null != expectedBucketOwner) {
            request.setExpectedBucketOwner(expectedBucketOwner);
        }

        return s3Client.copyObject(request);
    }

    //TODO Fix this function name would require lots of changes though
    public static List<String>listObjectFromFromS3(AmazonS3 s3Client, String bucket, String key) {
        ListObjectsV2Result listObjectsV2Result = s3Client.listObjectsV2(bucket, key);
        return listObjectsV2Result.getObjectSummaries().stream().map(S3ObjectSummary::getKey).collect(Collectors.toList());
    }


    public static DeleteObjectsResult deleteS3ObjectsByPrefix(AmazonS3 s3Client,
                                                              String bucketName,
                                                              String prefix,
                                                              String expectedBucketOwner) {

        List<DeleteObjectsRequest.KeyVersion> objects =
                listObjectFromFromS3(s3Client, bucketName, prefix)
                        .stream().map(s -> new DeleteObjectsRequest.KeyVersion(s))
                        .collect(Collectors.toList());

        if (objects.isEmpty()) {
            return null;
        }

        DeleteObjectsRequest request = new DeleteObjectsRequest(bucketName)
                .withKeys(objects)
                .withQuiet(false);

        if (null != expectedBucketOwner) {
            request.setExpectedBucketOwner(expectedBucketOwner);
        }

        return s3Client.deleteObjects(request);
    }

    public static String getDocumentName(String s3DocumentKey) {
        String[] documentKeySplit = s3DocumentKey.split("/");
        String documentName = documentKeySplit[documentKeySplit.length - 1];
        return String.format("%s.pdf",documentName);
    }

    /**
     * replaces all special characters from the document file/folder name to be stored in S3. Please be aware if you are using folder
     * structure in your document Key split it based on `/` and call this method for removal of any special characters
     * for each splitted part
     * @param s3Document
     * @return document name without special characters in them.
     */
    public static String formatDocumentName(String s3Document) {
        String s3DocumentWithExtraSpaces = s3Document.replaceAll("[^\\p{L}0-9-\\s]", "");
        return  s3DocumentWithExtraSpaces.replaceAll("\\s+", " ");
    }

    private static String getBucketKMSKey(AmazonS3 s3Client, String bucketName) throws SdkClientException {
        try{
            GetBucketEncryptionResult getBucketEncryptionResult = s3Client.getBucketEncryption(bucketName);
            ServerSideEncryptionByDefault serverSideEncryptionByDefault =
                    getBucketEncryptionResult.getServerSideEncryptionConfiguration().getRules()
                            .get(0).getApplyServerSideEncryptionByDefault();
            String kmsMasterKeyId = serverSideEncryptionByDefault.getKMSMasterKeyID();
            return kmsMasterKeyId;
        }catch (SdkClientException exception){
            System.out.println("Unable to read KMS data for bucket "+ bucketName);
            throw exception;
        }
    }

    private static String getSSEAlgorithm(AmazonS3 s3Client, String bucketName) throws SdkClientException {
        try{
            GetBucketEncryptionResult getBucketEncryptionResult = s3Client.getBucketEncryption(bucketName);
            ServerSideEncryptionByDefault serverSideEncryptionByDefault =
                    getBucketEncryptionResult.getServerSideEncryptionConfiguration().getRules()
                            .get(0).getApplyServerSideEncryptionByDefault();
            String algorithm = serverSideEncryptionByDefault.getSSEAlgorithm();
            return algorithm;
        }catch (SdkClientException exception){
            System.out.println("Unable to read KMS data for bucket "+ bucketName);
            throw exception;
        }
    }
}
