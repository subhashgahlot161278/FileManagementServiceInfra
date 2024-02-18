package com.amazon.aws.monetization.filemgmt;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReaderHeaderAware;
import com.opencsv.CSVReaderHeaderAwareBuilder;
import com.opencsv.exceptions.CsvValidationException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CsvReader {
    private AmazonS3 s3Client;

    public CsvReader(AmazonS3 s3Client) {
        this.s3Client = s3Client;
    }

    /**
     * Pulls data in a csv file from an S3 bucket, and converts data into a list of maps, with each key maps to a column name
     * @param bucket
     * @param key
     * @return a list of maps containing data from the csv file
     */
    public List<Map<String, String>> getCsvRecordsFromS3(String bucket, String key, String expectedBucketOwner) {
        final List<Map<String, String>> records = new ArrayList<>();
        try (CSVReaderHeaderAware reader = getCsvReader(bucket, key, expectedBucketOwner)) {
            Map<String, String> values;

            while ((values = reader.readMap()) != null) {
                boolean isEmptyRow = values.values().stream().allMatch(s -> s == null || s.trim().isEmpty());
                if (isEmptyRow) continue;

                records.add(values);
            }
            return records;
        }
        catch (IOException e) {
            String errorMessage = String.format("I/O exception occurred in getCsvRecordsFromS3 for bucket %s and key %s", bucket, key);
            throw new RuntimeException(errorMessage, e);
        } catch (CsvValidationException e) {
            String errorMessage =
                    String.format("Csv validation exception occurred in getCsvRecordsFromS3 for bucket %s and key %s", bucket, key);
            throw new RuntimeException(errorMessage, e);
        }
    }

    /**
     * Builds CSV reader for the given S3 object.
     * @param bucket
     * @param key
     * @return a csv reader
     */
    private CSVReaderHeaderAware getCsvReader(String bucket, String key, String expectedBucketOwner) {
        S3Object s3Object = getObjectFromS3(bucket, key, expectedBucketOwner);
        CSVParser csvParser = new CSVParserBuilder()
                .withIgnoreLeadingWhiteSpace(true)
                .build();
        InputStreamReader inputReader = new InputStreamReader(s3Object.getObjectContent(), StandardCharsets.UTF_8);

        return (CSVReaderHeaderAware) new CSVReaderHeaderAwareBuilder(inputReader)
                .withCSVParser(csvParser)
                .build();
    }

    private S3Object getObjectFromS3(String bucket, String key, String expectedBucketOwner) {
        GetObjectRequest request = new GetObjectRequest(bucket, key);

        if (null != expectedBucketOwner) {
            request.setExpectedBucketOwner(expectedBucketOwner);
        }

        return s3Client.getObject(request);
    }
}
