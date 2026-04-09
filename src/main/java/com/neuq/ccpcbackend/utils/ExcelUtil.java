package com.neuq.ccpcbackend.utils;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

//内部的第一个list传表头字段，后续的传数据行
public class ExcelUtil {
    public static byte[] generateExcel(List<List<String>> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            throw new IllegalArgumentException("数据列表不能为空");
        }
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Sheet1");
            Row headerRow = sheet.createRow(0);
            List<String> header = dataList.getFirst();
            for (int colNum = 0; colNum < header.size(); colNum++) {
                Cell cell = headerRow.createCell(colNum);
                cell.setCellValue(header.get(colNum));
            }
            for (int rowNum = 1; rowNum < dataList.size(); rowNum++) {
                Row row = sheet.createRow(rowNum);
                List<String> rowData = dataList.get(rowNum);
                for (int colNum = 0; colNum < rowData.size(); colNum++) {
                    Cell cell = row.createCell(colNum);
                    cell.setCellValue(rowData.get(colNum));
                }
            }
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                workbook.write(outputStream);
                return outputStream.toByteArray();
            }
        } catch (IOException e) {
            throw new RuntimeException("生成 Excel 文件时发生错误", e);
        }
    }
}
