package com.amazon.aws.monetization.filemgmt.excel.poi;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;

import java.text.NumberFormat;
import java.util.Locale;

@Builder
@Getter
public class ExcelCell {


    private Cell cell;

    public String getStringValue() {
        return cell.getStringCellValue();
    }

    public Object getValue() {
        return ExcelCellUtil.getCellValue(cell);
    }

    public CellStyle getStyle(){
        return cell.getCellStyle();
    }

    public void setValue(Object value) {
        ExcelCellUtil.SetCellValue(cell, value);
    }

    public void setStyle(CellStyle style) {
        cell.setCellStyle(style);
    }

    public boolean isNumericValue(){
        return this.getValue() instanceof Number;
    }

    public boolean isErrorValue(){
        return this.cell == null;
    }

    public int getColumn(){
        return cell.getColumnIndex();
    }

    public Double doubleValue() {
        Double retVal = null;
        try {
            retVal = Double.parseDouble(cell.getStringCellValue());
        } catch (Exception e) {
        }

        if (retVal == null) {
            retVal = currencyValue( Locale.US);
        }

        return retVal;
    }

    public  Double currencyValue( Locale locale) {
        try {
            final NumberFormat format = NumberFormat.getCurrencyInstance();
            Double d = (Double) format.parse(cell.getStringCellValue().trim()); //.replaceAll("[^\\d.,]", ""));
            // System.out.println("Converted value... {" + cell.getStringValue().trim()+"}");
            return d;
        } catch (Exception e) {
            // System.out.println("Cannt covert value... {" + cell.getStringValue().trim()+"}");
            return null;
        }
    }


    public Double getDoubleValue(){
        return doubleValue();
    }

    public String getFormula(){
        return cell.getCellFormula();
    }
}
