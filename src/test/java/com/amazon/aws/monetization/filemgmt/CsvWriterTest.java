package com.amazon.aws.monetization.filemgmt;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CsvWriterTest {

    @Mock
    AmazonS3 s3Client;

    @Mock
    GetBucketEncryptionResult getBucketEncryptionResult;

    @Mock
    ServerSideEncryptionConfiguration serverSideEncryptionConfiguration;

    @Mock
    ServerSideEncryptionByDefault serverSideEncryptionByDefault;

    @Mock
    ServerSideEncryptionRule serverSideEncryptionRule;

    CsvWriter csvWriter;

    private static String BUCKET = "fake_bucket";
    private static String KEY = "fake_key";
    private static String EXPECTED_BUCKET_OWNER = "fake_bucket";
    private static String[] RECORD_ROW = {"test_record"};
    private List<String[]> RECORDS = new ArrayList<String[]>();
    private List<ServerSideEncryptionRule> RULES = new ArrayList<ServerSideEncryptionRule>();

    @BeforeEach
    public void setup() {
        csvWriter = new CsvWriter(s3Client);
        RECORDS.add(RECORD_ROW);
        RULES.add(serverSideEncryptionRule);
    }

    @AfterEach
    public void teardown() {
        RECORDS.clear();
        RULES.clear();
    }

    @Test
    public void testWriteCsvRecords() throws Exception {
        when(s3Client.getBucketEncryption(BUCKET)).thenReturn(getBucketEncryptionResult);
        when(getBucketEncryptionResult.getServerSideEncryptionConfiguration()).thenReturn(serverSideEncryptionConfiguration);
        when(serverSideEncryptionConfiguration.getRules()).thenReturn(RULES);
        when(serverSideEncryptionRule.getApplyServerSideEncryptionByDefault()).thenReturn(serverSideEncryptionByDefault);
        when(serverSideEncryptionByDefault.getSSEAlgorithm()).thenReturn("algorithm");
        when(serverSideEncryptionByDefault.getKMSMasterKeyID()).thenReturn("kms_key");

        csvWriter.writeRecordsToS3(BUCKET, KEY, EXPECTED_BUCKET_OWNER, RECORDS);
        Mockito.verify(s3Client, Mockito.times(1)).putObject(Mockito.any(PutObjectRequest.class));
    }
}


