package com.amazon.aws.monetization.filemgmt;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ExcelWriterTest {

    @Mock
    AmazonS3 s3Client;
    @Mock
    Workbook workbook;
    @Mock
    GetBucketEncryptionResult getBucketEncryptionResult;

    @Mock
    ServerSideEncryptionConfiguration serverSideEncryptionConfiguration;

    @Mock
    ServerSideEncryptionByDefault serverSideEncryptionByDefault;

    @Mock
    ServerSideEncryptionRule serverSideEncryptionRule;

    ExcelWriter excelWriter;

    private static String TO_BUCKET = "fake_bucket_2";
    private static String TO_KEY = "fake_key_2";
    private static String EXPECTED_BUCKET_OWNER = "fake_bucket_owner";
    private static String[] RECORD_ROW = {"test_record"};
    private List<String[]> RECORDS = new ArrayList<String[]>();
    private List<ServerSideEncryptionRule> RULES = new ArrayList<ServerSideEncryptionRule>();

    @BeforeEach
    public void setup() {
        excelWriter = new ExcelWriter(s3Client);
        workbook = new XSSFWorkbook();
        RECORDS.add(RECORD_ROW);
        RULES.add(serverSideEncryptionRule);
    }

    @AfterEach
    public void teardown() {
        RECORDS.clear();
        RULES.clear();
    }

    @Test
    public void testWriteToBucket() throws IOException {
        createBucket(TO_BUCKET);

        excelWriter.write(TO_BUCKET, TO_KEY, workbook, EXPECTED_BUCKET_OWNER);
        Mockito.verify(s3Client, Mockito.times(1)).putObject(Mockito.any(PutObjectRequest.class));
    }

    public void createBucket(String bucket) {
        when(s3Client.getBucketEncryption(TO_BUCKET)).thenReturn(getBucketEncryptionResult);
        when(getBucketEncryptionResult.getServerSideEncryptionConfiguration()).thenReturn(serverSideEncryptionConfiguration);
        when(serverSideEncryptionConfiguration.getRules()).thenReturn(RULES);
        when(serverSideEncryptionRule.getApplyServerSideEncryptionByDefault()).thenReturn(serverSideEncryptionByDefault);
        when(serverSideEncryptionByDefault.getSSEAlgorithm()).thenReturn("algorithm");
        when(serverSideEncryptionByDefault.getKMSMasterKeyID()).thenReturn("kms_key");
    }
}


