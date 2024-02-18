package com.amazon.aws.monetization.filemgmt.excel.poi;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

@Builder
public class ExcelRow {

    @Getter
    private Row row;

    public static ExcelRowBuilder builder() {
        return new CustomExcelRowBuilder();
    }

    public ExcelCell get(int index) {
        return ExcelCell.builder()
                .cell(row.getCell(index) == null ? row.createCell(index) : row.getCell(index))
                .build();
    }

    public ExcelCell get(int index, boolean createNewOnNull) {
        if(createNewOnNull) {
            return get(index);
        }
        return ExcelCell.builder().cell(row.getCell(index)).build();
    }

    public ExcelCell getLastCell(){
        int lastColumnIndex = row.getLastCellNum()-1;
        return ExcelCell.builder().cell(row.getCell(lastColumnIndex)).build();
    }



    public void copy(ExcelRow srcRow) {
        for (int index = 0; index < row.getLastCellNum(); index++) {
            ExcelCellUtil.copyCells(row.getCell(index), this.get(index).getCell());
        }
    }

    public void print(int index) {
        System.out.print(ExcelCellUtil.getCellValue(row.getCell(index)));
    }

    public int getLastCellNum(){
        return this.row.getLastCellNum();
    }

    public boolean isNull() {
        return row == null;
    }

    /**
     * Cusotm  builder class
     */
    private static class CustomExcelRowBuilder extends ExcelRowBuilder {
        @Override
        public ExcelRow build() {
            // Validates required fields
            return super.build();
        }
    }

}
