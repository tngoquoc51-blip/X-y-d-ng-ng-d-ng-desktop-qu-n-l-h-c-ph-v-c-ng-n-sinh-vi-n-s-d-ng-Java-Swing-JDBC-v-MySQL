package vn.edu.eaut.qlhocphi.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Xuat du lieu dang bang ra file Excel (.xlsx) bang Apache POI.
 * Yeu cau dependency org.apache.poi:poi-ooxml da khai bao trong pom.xml.
 */
public class ExcelExporter {

    /**
     * Xuat danh sach du lieu ra file Excel.
     *
     * @param filePath  duong dan file .xlsx can luu
     * @param sheetName ten sheet
     * @param headers   tieu de cac cot
     * @param rows      du lieu tung dong (moi dong la 1 mang String)
     */
    public static void export(String filePath, String sheetName,
                              String[] headers, List<String[]> rows) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(sheetName);

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (String[] rowData : rows) {
                Row row = sheet.createRow(rowIdx++);
                for (int i = 0; i < rowData.length; i++) {
                    row.createCell(i).setCellValue(rowData[i] == null ? "" : rowData[i]);
                }
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }
        }
    }
}