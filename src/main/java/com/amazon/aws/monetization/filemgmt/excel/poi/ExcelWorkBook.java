package com.amazon.aws.monetization.filemgmt.excel.poi;

import com.google.common.collect.ImmutableList;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.util.List;

@Builder
public class ExcelWorkBook {

    @Builder
    public static class WorkSheetCollection {
        List<ExcelWorkSheet> workSheets;

        public ExcelWorkSheet get(int index) {
            return workSheets.get(index);
        }

        public int getCount() {
            return workSheets.size();
        }
    }

  //  @NonNull
    @Getter
    XSSFWorkbook workbook;

    @NonNull
    @Getter
    String key;

    @Getter
    WorkSheetCollection worksheets;

    public void calculateFormula() {
        XSSFFormulaEvaluator.evaluateAllFormulaCells(workbook);
    }

    public ExcelWorkSheet addWorkSheet(String sheetName) throws Exception {
        workbook.createSheet(sheetName);
        process();
        return getValidatedWorkSheet(sheetName);

    }

    public void close() throws IOException {
        workbook.close();
    }

    public void dispose() throws IOException {
        workbook.close();
    }

    public ExcelWorkSheet getValidatedWorkSheet(String sheetName) throws Exception {
        XSSFSheet worksheet = workbook.getSheet(sheetName);

        if (worksheet == null) {
            throw new Exception(
                    String.format("worksheet {%s} is missing in workbook {%s}", sheetName, key));
        }

        return ExcelWorkSheet.builder().worksheet(worksheet).build();
    }

    public void refreshPivots() {
        worksheets.workSheets.forEach(excelWorkSheet -> excelWorkSheet.refreshPivotTables());
    }

    private ExcelWorkBook process() {

        if( workbook == null){
            workbook = new XSSFWorkbook();
        }
        ImmutableList.Builder<ExcelWorkSheet> workSheetBuilder = ImmutableList.builder();

        for (int index = 0; index < workbook.getNumberOfSheets(); index++) {
            XSSFSheet worksheet = workbook.getSheetAt(index);
            workSheetBuilder.add(ExcelWorkSheet.builder()
                    .worksheet(worksheet)
                    .build());

        }

        worksheets = WorkSheetCollection.builder().workSheets(workSheetBuilder.build()).build();

        return this;
    }

    public static ExcelWorkBookBuilder builder() {
        return new CustomExcelWorkBookBuilder();
    }

    /**
     * Cusotm  builder class
     */
    private static class CustomExcelWorkBookBuilder extends ExcelWorkBookBuilder {
        @Override
        public ExcelWorkBook build() {
            // Validates required fields
            return super.build().process();
        }
    }

}
