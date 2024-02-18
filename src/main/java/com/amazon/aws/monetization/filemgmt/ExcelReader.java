package com.amazon.aws.monetization.filemgmt;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.google.common.base.Strings;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


public class ExcelReader {
    private final AmazonS3 s3Client;

    public ExcelReader(AmazonS3 s3Client) {
        this.s3Client = s3Client;
    }

    /**
     * Pulls data in a xlsx/xls file from an S3 bucket, and converts data into a list of maps, with each key maps to a column name
     *
     * @param bucket the bucket to read from
     * @param key the S3 key of the Excel to read
     * @param expectedBucketOwner account ID of bucket owner
     * @return a list of maps containing data from the xlsx/xls file
     * @throws IOException from error reading Excel Files
     */
    public List<Map<String, String>> getExcelRecordsFromS3(String bucket,
                                                           String key,
                                                           String expectedBucketOwner) throws IOException {
        S3Object s3Object = null;
        XSSFWorkbook workbook = null;

        try {
            List<Map<String, String>> records = new ArrayList<>();

            s3Object = getObjectFromS3(bucket, key, expectedBucketOwner);
            workbook = new XSSFWorkbook(s3Object.getObjectContent());
            XSSFSheet sheet = workbook.getSheetAt(0);

            List<String> headers = getRowData(sheet, 0);

            for(int rowIndex = 1; rowIndex < sheet.getLastRowNum() + 1; rowIndex ++) {
                AtomicInteger index = new AtomicInteger();
                List<String> rowData = getRowData(sheet, rowIndex);
                if( !isRowDataEmpty(rowData) ) {
                    records.add(rowData.stream().collect(
                            Collectors.toMap(s -> headers.get(index.getAndIncrement()), s -> s)));
                }
            }

            workbook.close();
            return records;

        } catch  (IOException | SdkClientException e) {
            throw new IOException(String.format("Error Reading Excel Records at [%s] due to an S3/IO Error, " +
                    " error message received was [%s]", bucket + "/" + key, e.getMessage()), e);
        } catch (Exception e) {
            throw new RuntimeException(String.format("Error Reading Excel Records at [%s] due to errors which could include  " +
                    "duplicate column header errors error message received was [%s]", bucket + "/" + key, e.getMessage()), e);
        } finally {
            if (workbook != null) {
                workbook.close();
            }
            if (s3Object != null) {
                s3Object.close();
            }
        }
    }

    /**
     * Pulls data in a xlsx/xls file from an S3 bucket by Sheet Name,
     * and converts data into a list of maps, with each key maps to a column name
     * if a column name is empty then it will map to the index of the column
     * @param bucket the bucket to read from
     * @param key the S3 key of the Excel to read
     * @param expectedBucketOwner account ID of bucket owner
     * @param sheetName sheet name to be read
     * @return a list of maps containing data from the xlsx/xls file
     * @throws IOException from error reading Excel Files
     */
    public List<Map<String, String>> getExcelRecordsFromS3(String bucket,
                                                           String key,
                                                           String expectedBucketOwner,
                                                           String sheetName) throws IOException {
        S3Object s3Object = null;
        XSSFWorkbook workbook = null;

        try {
            List<Map<String, String>> records = new ArrayList<>();

            s3Object = getObjectFromS3(bucket, key, expectedBucketOwner);
            workbook = new XSSFWorkbook(s3Object.getObjectContent());

            // Creating formula evaluator object
            FormulaEvaluator formulaEval = workbook.getCreationHelper().createFormulaEvaluator();

            XSSFSheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                workbook.close();
                return null;
            }

            List<String> headers = getRowData(sheet, 0);

            for(int rowIndex = 1; rowIndex < sheet.getLastRowNum() + 1; rowIndex ++) {
                AtomicInteger index = new AtomicInteger();
                List<String> rowData = getRowData(sheet, rowIndex, formulaEval);
                if( !isRowDataEmpty(rowData) ) {
                    //In case of Empty Headers use index to prevent merge conflicts
                    records.add(rowData.stream().collect(
                            Collectors.toMap(s ->
                                    Strings.isNullOrEmpty(headers.get(index.get()))
                                            ? String.valueOf(index.getAndIncrement())
                                            : headers.get(index.getAndIncrement()), s -> s)));
                }
            }

            workbook.close();
            return records;
        } catch  (IOException | SdkClientException e) {
            throw new IOException(String.format("Error Reading Excel Records at [%s] due to an S3/IO Error, " +
                    " error message received was [%s]", bucket + "/" + key, e.getMessage()), e);
        } catch (Exception e) {
            throw new RuntimeException(String.format("Error Reading Excel Records at [%s] due to errors which could include  " +
                    "duplicate column header errors error message received was [%s]", bucket + "/" + key, e.getMessage()), e);
        } finally {
            if (workbook != null) {
                workbook.close();
            }
            if (s3Object != null) {
                s3Object.close();
            }
        }
    }

    /**
     * Pulls data in a xlsx/xls file from an S3 bucket, and converts data into a Map of column Name and column data values
     *
     * @param bucket the bucket to read from
     * @param key the S3 key of the Excel to read
     * @param expectedBucketOwner account ID of bucket owner
     * @return Map of column Name and column data values from the xlsx/xls file
     * @throws IOException from error reading Excel Files
     */
    public Map<String, List<String>> getExcelRecordsByColumnsFromS3(String bucket,
                                                                    String key,
                                                                    String expectedBucketOwner) throws IOException {
        S3Object s3Object = null;
        XSSFWorkbook workbook = null;
        try {
            Map<String, List<String>> records = new HashMap<>();

            s3Object = getObjectFromS3(bucket, key, expectedBucketOwner);
            workbook = new XSSFWorkbook(s3Object.getObjectContent());
            XSSFSheet sheet = workbook.getSheetAt(0);

            List<String> headers = getRowData(sheet, 0);

            for(int rowIndex = 1; rowIndex < sheet.getLastRowNum() + 1; rowIndex ++) {
                List<String> rowData = getRowData(sheet, rowIndex);
                if( !isRowDataEmpty(rowData) ) {
                    for(int colIndex = 0; colIndex < rowData.size(); colIndex ++) {
                        String columnName = headers.get(colIndex);
                        if(!records.containsKey(columnName)) {
                            records.put(columnName, new ArrayList<>());
                        }
                        records.get(columnName).add(rowData.get(colIndex));
                    }
                }
            }

            workbook.close();
            return records;
        } catch  (IOException | SdkClientException e) {
            throw new IOException(String.format("Error Reading Excel Records at [%s] due to an S3/IO Error, " +
                    " error message received was [%s]", bucket + "/" + key, e.getMessage()), e);
        } catch (Exception e) {
            throw new RuntimeException(String.format("Error Reading Excel Records at [%s] due to errors which could include  " +
                    "duplicate column header errors error message received was [%s]", bucket + "/" + key, e.getMessage()), e);
        } finally {
            if (workbook != null) {
                workbook.close();
            }
            if (s3Object != null) {
                s3Object.close();
            }
        }

    }


    public List<List<String>> getExcelRecordsRowsFromS3(String bucket, String key, String expectedBucketOwner)
            throws IOException {

        List<List<String>> records = new ArrayList<>();

        S3Object s3Object = getObjectFromS3(bucket, key, expectedBucketOwner);
        XSSFWorkbook workbook = new XSSFWorkbook(s3Object.getObjectContent());
        XSSFSheet sheet = workbook.getSheetAt(0);

        // Creating formula evaluator object
        FormulaEvaluator formulaEval = workbook.getCreationHelper().createFormulaEvaluator();

        for(int rowIndex = 0; rowIndex < sheet.getLastRowNum() + 1; rowIndex ++) {
            records.add(getRowData(sheet, rowIndex, formulaEval));
        }

        workbook.close();
        return records;
    }

    private List<String> getRowData(XSSFSheet sheet, int rowIndex) {
        List<String> rowData = new ArrayList<>();
        XSSFRow row = sheet.getRow(rowIndex);
        if(row != null) {
            DataFormatter fmt = new DataFormatter();
            for (int colIndex = 0; colIndex < row.getLastCellNum(); colIndex++) {
                XSSFCell cell = row.getCell(colIndex);
                String valueAsSeenInExcel = fmt.formatCellValue(cell);
                if (null == valueAsSeenInExcel || valueAsSeenInExcel.isEmpty())
                    rowData.add("");
                else {
                    rowData.add(valueAsSeenInExcel.trim());
                }
            }
        }
        return rowData;
    }

    private List<String> getRowData(XSSFSheet sheet, int rowIndex, FormulaEvaluator formulaEval) {
        List<String> rowData = new ArrayList<>();
        XSSFRow row = sheet.getRow(rowIndex);
        if(row != null) {
            DataFormatter fmt = new DataFormatter();
            for (int colIndex = 0; colIndex < row.getLastCellNum(); colIndex++) {
                XSSFCell cell = row.getCell(colIndex);
                String valueAsSeenInExcel;
                if (cell != null && cell.getCellType() !=null
                        && cell.getCellType() == CellType.FORMULA) {
                    // Evaluating cell with Formulas
                    try {
                        CellValue c = formulaEval.evaluate(cell);
                        valueAsSeenInExcel = c.formatAsString();
                    } catch (Exception e) {
                        //Sometimes errors out on very complicated formulas
                        valueAsSeenInExcel = fmt.formatCellValue(cell);
                    }
                } else {
                    valueAsSeenInExcel = fmt.formatCellValue(cell);
                }

                if (null == valueAsSeenInExcel || valueAsSeenInExcel.isEmpty())
                    rowData.add("");
                else {
                    rowData.add(valueAsSeenInExcel.trim());
                }
            }
        }
        return rowData;
    }

    private boolean isRowDataEmpty(List<String> rowData) {
        for(String cellData: rowData) {
            if(null != cellData && !cellData.isEmpty())
                return false;
        }
        return true;
    }


    private S3Object getObjectFromS3(String bucket, String key, String expectedBucketOwner) {
        GetObjectRequest request = new GetObjectRequest(bucket, key);

        if (null != expectedBucketOwner) {
            request.setExpectedBucketOwner(expectedBucketOwner);
        }

        return s3Client.getObject(request);
    }

}
