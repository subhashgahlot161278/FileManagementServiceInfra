package com.amazon.aws.monetization.filemgmt;

import com.amazon.aws.monetization.filemgmt.utils.S3Util;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ExcelWriter {
    private AmazonS3 s3Client;

    public ExcelWriter(AmazonS3 s3Client) {
        this.s3Client = s3Client;
    }

    private Path writeToTempDirectory(String fileName, Workbook workbook) throws IOException {
        FileOutputStream fileOut = null;
        Path localFilePath = null;
        try {
            localFilePath = Files.createTempFile(fileName.replace("/", ""), ".xlsx");
            fileOut = new FileOutputStream(localFilePath.toString());
            workbook.write(fileOut);
        } finally {
            if(fileOut!=null) fileOut.close();
        }
        return localFilePath;
    }

    public void write(String srcBucket, String srcKey, String destBucket, String destKey, String expectedBucketOwner)
            throws IOException {

        S3Object s3Object = S3Util.getObjectFromS3(s3Client, srcBucket, srcKey, expectedBucketOwner);
        Workbook workbook = new XSSFWorkbook(s3Object.getObjectContent());

        write(destBucket, destKey, workbook, expectedBucketOwner);

        workbook.close();
    }
    public void write(String bucketName, String s3key, Workbook workbook, String expectedAccountOwner) throws IOException {
        S3Util.uploadtoS3(s3Client, writeToTempDirectory(s3key, workbook), bucketName, s3key, expectedAccountOwner);
    }
}
