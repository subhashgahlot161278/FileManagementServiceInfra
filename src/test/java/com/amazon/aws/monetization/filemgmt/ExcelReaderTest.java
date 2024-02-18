package com.amazon.aws.monetization.filemgmt;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ExcelReaderTest {

    @Mock
    AmazonS3 s3Client;

    @Mock
    S3Object s3Object;

    ExcelReader excelReader;

    @BeforeEach
    public void setup() {
        excelReader = new ExcelReader(s3Client);
    }

    @Test
    public void testExcelRecords() throws Exception {
        File initialFile = new File("src/test/java/com/amazon/aws/monetization/filemgmt/resources/202201101500.xlsx");
        InputStream targetStream = new FileInputStream(initialFile);
        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(s3Object);
        when(s3Object.getObjectContent()).thenReturn(new S3ObjectInputStream(targetStream, null));
        List<Map<String, String>> data = excelReader.getExcelRecordsFromS3("test_bucket", "key", "");
        assertEquals(3, data.size());
        assertEquals(3, data.get(0).size());
        assertEquals(3, data.get(1).size());
        assertEquals(3, data.get(2).size());
        assertEquals(ImmutableMap.<String, String>builder()
                .put("AWS Account ID", "006666660000")
                .put("Account: Account Name", "ABC Inc")
                .put("Agreement Request Name", "CA-20170101-aaaa-SNA")
                .build(), data.get(0));

        assertEquals(ImmutableMap.<String, String>builder()
                .put("AWS Account ID", "006666660000")
                .put("Account: Account Name", "ABC Inc")
                .put("Agreement Request Name", "CA-20170101-aaaa-SNA")
                .build(), data.get(0));

        assertEquals(ImmutableMap.<String, String>builder()
                .put("AWS Account ID", "111111111111")
                .put("Account: Account Name", "XYZ Inc")
                .put("Agreement Request Name", "CA-20170101-bbbb-SNA")
                .build(), data.get(1));

        assertEquals(ImmutableMap.<String, String>builder()
                .put("AWS Account ID", "222222222222")
                .put("Account: Account Name", "LMN Inc")
                .put("Agreement Request Name", "CA-20170101-cccc-SNA")
                .build(), data.get(2));
    }

    @Test
    public void testExcelRecordsByColumn() throws Exception {
        File initialFile = new File("src/test/java/com/amazon/aws/monetization/filemgmt/resources/202201101500.xlsx");
        InputStream targetStream = new FileInputStream(initialFile);
        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(s3Object);
        when(s3Object.getObjectContent()).thenReturn(new S3ObjectInputStream(targetStream, null));
        Map<String, List<String>> data = excelReader.getExcelRecordsByColumnsFromS3("test_bucket", "key", "");
        assertEquals(3, data.size());
        assertEquals(ImmutableList.of("006666660000", "111111111111", "222222222222"), data.get("AWS Account ID"));
        assertEquals(ImmutableList.of("ABC Inc", "XYZ Inc", "LMN Inc"), data.get("Account: Account Name"));
        assertEquals(ImmutableList.of("CA-20170101-aaaa-SNA", "CA-20170101-bbbb-SNA", "CA-20170101-cccc-SNA"), data.get("Agreement Request Name"));
    }
}


