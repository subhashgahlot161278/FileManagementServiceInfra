package com.amazon.aws.monetization.filemgmt.excel.poi;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.BuiltinFormats;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellUtil;

import java.math.BigDecimal;
import java.sql.Time;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ExcelCellUtil {
    static Font copyFont(Font font1, Workbook wb2) {
        boolean isBold = font1.getBold();
        short color = font1.getColor();
        short fontHeight = font1.getFontHeight();
        String fontName = font1.getFontName();
        boolean isItalic = font1.getItalic();
        boolean isStrikeout = font1.getStrikeout();
        short typeOffset = font1.getTypeOffset();
        byte underline = font1.getUnderline();

        Font font2 = wb2.findFont(isBold, color, fontHeight, fontName, isItalic, isStrikeout, typeOffset, underline);
        if (font2 == null) {
            font2 = wb2.createFont();
            font2.setBold(isBold);
            font2.setColor(color);
            font2.setFontHeight(fontHeight);
            font2.setFontName(fontName);
            font2.setItalic(isItalic);
            font2.setStrikeout(isStrikeout);
            font2.setTypeOffset(typeOffset);
            font2.setUnderline(underline);
        }

        return font2;
    }

    public static void copyStyles(Cell cell1, Cell cell2) {
        CellStyle style1 = cell1.getCellStyle();
        Map<String, Object> properties = new HashMap<String, Object>();

        //CellUtil.DATA_FORMAT
        short dataFormat1 = style1.getDataFormat();
        if (BuiltinFormats.getBuiltinFormat(dataFormat1) == null) {
            String formatString1 = style1.getDataFormatString();
            DataFormat format2 = cell2.getSheet().getWorkbook().createDataFormat();
            dataFormat1 = format2.getFormat(formatString1);
        }
        properties.put(CellUtil.DATA_FORMAT, dataFormat1);

        //CellUtil.FILL_PATTERN
        //CellUtil.FILL_FOREGROUND_COLOR
        FillPatternType fillPattern = style1.getFillPattern();
        short fillForegroundColor = style1.getFillForegroundColor(); //gets only indexed colors, no custom HSSF or XSSF colors
        properties.put(CellUtil.FILL_PATTERN, fillPattern);
        properties.put(CellUtil.FILL_FOREGROUND_COLOR, fillForegroundColor);

        //CellUtil.FONT
        Font font1 = cell1.getSheet().getWorkbook().getFontAt(style1.getFontIndexAsInt());
        Font font2 = copyFont(font1, cell2.getSheet().getWorkbook());
        properties.put(CellUtil.FONT, font2.getIndexAsInt());

        //BORDERS
        BorderStyle borderStyle = null;
        short borderColor = -1;
        //CellUtil.BORDER_LEFT
        //CellUtil.LEFT_BORDER_COLOR
        borderStyle = style1.getBorderLeft();
        properties.put(CellUtil.BORDER_LEFT, borderStyle);
        borderColor = style1.getLeftBorderColor();
        properties.put(CellUtil.LEFT_BORDER_COLOR, borderColor);
        //CellUtil.BORDER_RIGHT
        //CellUtil.RIGHT_BORDER_COLOR
        borderStyle = style1.getBorderRight();
        properties.put(CellUtil.BORDER_RIGHT, borderStyle);
        borderColor = style1.getRightBorderColor();
        properties.put(CellUtil.RIGHT_BORDER_COLOR, borderColor);
        //CellUtil.BORDER_TOP
        //CellUtil.TOP_BORDER_COLOR
        borderStyle = style1.getBorderTop();
        properties.put(CellUtil.BORDER_TOP, borderStyle);
        borderColor = style1.getTopBorderColor();
        properties.put(CellUtil.TOP_BORDER_COLOR, borderColor);
        //CellUtil.BORDER_BOTTOM
        //CellUtil.BOTTOM_BORDER_COLOR
        borderStyle = style1.getBorderBottom();
        properties.put(CellUtil.BORDER_BOTTOM, borderStyle);
        borderColor = style1.getBottomBorderColor();
        properties.put(CellUtil.BOTTOM_BORDER_COLOR, borderColor);

        CellUtil.setCellStyleProperties(cell2, properties);
    }

    public static void copyCells(Cell srcCell, Cell destnCell) {
        switch (srcCell.getCellType()) {
            case STRING:
   /*
    //TODO: copy HSSFRichTextString to XSSFRichTextString
    RichTextString rtString1 = cell1.getRichStringCellValue();
    cell2.setCellValue(rtString1); // this fails if cell2 is XSSF and rtString1 is HSSF
   */
                String string1 = srcCell.getStringCellValue();
                destnCell.setCellValue(string1);
                break;
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(srcCell)) {
                    Date date1 = srcCell.getDateCellValue();
                    destnCell.setCellValue(date1);
                } else {
                    double cellValue1 = srcCell.getNumericCellValue();
                    destnCell.setCellValue(cellValue1);
                }
                break;
            case FORMULA:
                String formula1 = srcCell.getCellFormula();
                destnCell.setCellFormula(formula1);
                break;

            //case : //TODO: further cell types

        }
    }

    //This is basic - needs to be updated
    public static String updateFormula(String formula, int rowIndex) {
        return formula.replaceAll("(?i)([A-Z]+)(\\d+)", "$1" + (rowIndex + 1));
    }


    public static Object getCellValue(Cell cell) {
        if (cell == null)
            return null;
        switch (cell.getCellType()) {
            case STRING:
   /*
    //TODO: copy HSSFRichTextString to XSSFRichTextString
    RichTextString rtString1 = cell1.getRichStringCellValue();
    cell2.setCellValue(rtString1); // this fails if cell2 is XSSF and rtString1 is HSSF
   */
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                } else {
                    return cell.getNumericCellValue();
                }
            case BOOLEAN:
                return cell.getBooleanCellValue();

            case FORMULA:
                return cell.getCellFormula();

            default: {
                //    System.out.print("default: " + cell.getCellType() + ":" + cell.toString());
                return cell.toString();
            }

            //case : //TODO: further cell types
        }
    }


    public static void SetCellValue(Cell cell, Object value) {
        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof BigDecimal) {
            cell.setCellValue(((BigDecimal) value).doubleValue());
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else if (value instanceof Byte) {
            cell.setCellValue((Byte) value);
        } else if (value instanceof Short) {
            cell.setCellValue((Short) value);
        } else if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else if (value instanceof Long) {
            cell.setCellValue((Long) value);
        } else if (value instanceof Float) {
            cell.setCellValue((Float) value);
        } else if (value instanceof Double) {
            cell.setCellValue((Double) value);
        } else if (value instanceof java.sql.Date) {
            cell.setCellValue((java.sql.Date) value);
        } else if (value instanceof Time) {
            cell.setCellValue((Time) value);
        } else if (value instanceof java.sql.Timestamp) {
            cell.setCellValue((java.sql.Timestamp) value);
        } else {
            cell.setCellValue(value.toString());
            //  System.out.println("SetCellValue::String:" + value.toString() + ":" + cell.getStringCellValue());
        }
    }

}
