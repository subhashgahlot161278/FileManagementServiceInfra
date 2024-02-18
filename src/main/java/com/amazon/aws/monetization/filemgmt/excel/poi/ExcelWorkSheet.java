package com.amazon.aws.monetization.filemgmt.excel.poi;

import com.amazonaws.util.StringUtils;
import com.google.common.collect.ImmutableList;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.RangeCopier;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFPivotCacheDefinition;
import org.apache.poi.xssf.usermodel.XSSFPivotTable;
import org.apache.poi.xssf.usermodel.XSSFRangeCopier;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import java.util.ArrayList;
import java.util.List;


@Getter
@Builder
public class ExcelWorkSheet {

    @NonNull
    XSSFSheet worksheet;

    RangeCopier rangeCopier;

    @Builder.Default
    List<ExcelRow> rows = new ArrayList<>();

     public ExcelWorkSheet getRows(){
         return this;
     }

    public int getCount(){
         return this.rows.size();
    }

    public ExcelRow get(int rowIndex){
         return rows.get(rowIndex);
    }


    public ExcelRow getRow(int index) {
        if (index >= rows.size()) {
            rows.add(ExcelRow.builder().row(worksheet.createRow(index)).build());
        }

        return rows.get(index);
    }



    public ExcelWorkSheet getCells() {
        return this;
    }

    public ExcelRow checkRow(int index){
         if ( index >= rows.size()){
             return null;
         }
         return rows.get(index);
    }

    public String getSheetName() {
        return worksheet.getSheetName();
    }

    public String getName() {
        return worksheet.getSheetName();
    }

    public int getMaxDataRow() {
        return rows.size();
    }

    public ExcelCell get(int row, int col) {
        return getRow(row).get(col);
    }

    public void applySharedFormula(int beginRowIndex, int rowCount, int beginColIndex, int endColIndex) {

        int col = beginColIndex;

        while (col <= endColIndex) {
            Cell cell = getRow(beginRowIndex).get(col).getCell();

            if (cell != null && cell.getCellType() == CellType.FORMULA && !StringUtils.isNullOrEmpty(cell.getCellFormula())) {

                //"I2:K2"
                CellRangeAddress rangeToCopy = CellRangeAddress.valueOf(String.format("%s%s:%s%s",
                        CellReference.convertNumToColString(col), beginRowIndex + 1,
                        CellReference.convertNumToColString(col), beginRowIndex + 1));
                System.out.println("rangeTo copy " + rangeToCopy.formatAsString());

                CellRangeAddress destRange = CellRangeAddress.valueOf(String.format("%s%s:%s%s",
                        CellReference.convertNumToColString(col), beginRowIndex + 1,
                        CellReference.convertNumToColString(col), rowCount));

                System.out.println("dest range " + destRange.formatAsString());


                rangeCopier.copyRange(rangeToCopy, destRange);

            }

            col++;
        }
    }

    public void refreshPivotTables() {
        List<XSSFPivotTable> pivotTablesInSheet = worksheet.getPivotTables();
        if (!pivotTablesInSheet.isEmpty()) {
            XSSFPivotTable pivotTable = pivotTablesInSheet.get(0);
            pivotTable.getRelations().stream()
                    .filter(XSSFPivotCacheDefinition.class::isInstance)
                    .map(XSSFPivotCacheDefinition.class::cast)
                    .forEach(xssfPivotCacheDefinition -> xssfPivotCacheDefinition.getCTPivotCacheDefinition().setRefreshOnLoad(true));
        }
    }

    public List<Object> getColumnValues(int columnIndex) {
        ImmutableList.Builder<Object> builder = ImmutableList.builder();
        for (int rowIndex = 1; rowIndex < this.rows.size(); rowIndex++) {
            ExcelRow row = rows.get(rowIndex);
            if (!row.isNull()) {
                Object value = row.get(columnIndex).getValue();
                System.out.println(value);
                if (value != null) {
                    builder.add(value);
                }
            }
        }
        return builder.build();
    }

    public void copy(int rowIndex, ExcelRow row) {
        ExcelRow destinationRow = getRow(rowIndex);
        destinationRow.copy(row);
    }


    private ExcelWorkSheet process() {
        System.out.println("worksheet.getLastRowNum(): " + worksheet.getLastRowNum());
        for (int index = 0; index <= worksheet.getLastRowNum(); index++) {
            rows.add(ExcelRow.builder().row(worksheet.getRow(index)).build());
        }

        rangeCopier = new XSSFRangeCopier(worksheet, worksheet);

        return this;
    }

    public static ExcelWorkSheetBuilder builder() {
        return new CustomExcelWorkSheetBuilder();
    }

    /**
     * Cusotm  builder class
     */
    private static class CustomExcelWorkSheetBuilder extends ExcelWorkSheetBuilder {
        @Override
        public ExcelWorkSheet build() {
            // Validates required fields
            return super.build().process();
        }
    }
}
