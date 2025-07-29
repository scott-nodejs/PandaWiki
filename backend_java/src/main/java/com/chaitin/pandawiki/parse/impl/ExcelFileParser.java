package com.chaitin.pandawiki.parse.impl;

import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

/**
 * Excel文件解析器
 * 
 * 注意：此解析器需要Apache POI依赖（与Word解析器相同）
 * 
 * @author chaitin
 */
@Slf4j
public class ExcelFileParser extends AbstractFileParser {
    
    @Override
    public List<String> getSupportedExtensions() {
        return Arrays.asList("xlsx", "xls");
    }
    
    @Override
    public String parseToMarkdown(InputStream inputStream, String fileName) throws Exception {
        StringBuilder markdown = new StringBuilder();
        markdown.append(createFileMetadata(fileName, null));
        
        try {
            String content;
            if (fileName.toLowerCase().endsWith(".xlsx")) {
                content = parseXlsx(inputStream);
            } else {
                content = parseXls(inputStream);
            }
            
            markdown.append("# ").append(getFileNameWithoutExtension(fileName)).append("\n\n");
            markdown.append(content);
            
        } catch (Exception e) {
            log.warn("Excel文件解析失败，可能缺少Apache POI依赖: {}", e.getMessage());
            markdown.append("# Excel文档解析\n\n");
            markdown.append("> **注意**: Excel文档解析失败，可能需要添加Apache POI依赖。\n\n");
            markdown.append("**文件信息**:\n");
            markdown.append("- 文件名: ").append(fileName).append("\n");
            markdown.append("- 格式: Microsoft Excel文档\n");
            markdown.append("- 状态: 需要配置解析器依赖\n\n");
            markdown.append("请联系管理员配置Excel文档解析功能。");
        }
        
        return cleanText(markdown.toString());
    }
    
    /**
     * 解析XLSX文件
     */
    private String parseXlsx(InputStream inputStream) throws Exception {
        try {
            // XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
            Class<?> workbookClass = Class.forName("org.apache.poi.xssf.usermodel.XSSFWorkbook");
            Object workbook = workbookClass.getConstructor(InputStream.class).newInstance(inputStream);
            
            StringBuilder content = new StringBuilder();
            
            // int numberOfSheets = workbook.getNumberOfSheets();
            int numberOfSheets = (Integer) workbookClass.getMethod("getNumberOfSheets").invoke(workbook);
            
            for (int i = 0; i < numberOfSheets; i++) {
                // XSSFSheet sheet = workbook.getSheetAt(i);
                Object sheet = workbookClass.getMethod("getSheetAt", int.class).invoke(workbook, i);
                
                // String sheetName = sheet.getSheetName();
                String sheetName = (String) sheet.getClass().getMethod("getSheetName").invoke(sheet);
                
                content.append("## ").append(sheetName).append("\n\n");
                
                // 使用DataFormatter来格式化单元格数据
                Class<?> dataFormatterClass = Class.forName("org.apache.poi.ss.usermodel.DataFormatter");
                Object dataFormatter = dataFormatterClass.newInstance();
                
                content.append(extractSheetContent(sheet, dataFormatter));
                content.append("\n");
            }
            
            // 关闭workbook
            workbookClass.getMethod("close").invoke(workbook);
            
            return content.toString();
            
        } catch (ClassNotFoundException e) {
            throw new Exception("Apache POI OOXML library not found", e);
        }
    }
    
    /**
     * 解析XLS文件
     */
    private String parseXls(InputStream inputStream) throws Exception {
        try {
            // HSSFWorkbook workbook = new HSSFWorkbook(inputStream);
            Class<?> workbookClass = Class.forName("org.apache.poi.hssf.usermodel.HSSFWorkbook");
            Object workbook = workbookClass.getConstructor(InputStream.class).newInstance(inputStream);
            
            StringBuilder content = new StringBuilder();
            
            // int numberOfSheets = workbook.getNumberOfSheets();
            int numberOfSheets = (Integer) workbookClass.getMethod("getNumberOfSheets").invoke(workbook);
            
            for (int i = 0; i < numberOfSheets; i++) {
                // HSSFSheet sheet = workbook.getSheetAt(i);
                Object sheet = workbookClass.getMethod("getSheetAt", int.class).invoke(workbook, i);
                
                // String sheetName = sheet.getSheetName();
                String sheetName = (String) sheet.getClass().getMethod("getSheetName").invoke(sheet);
                
                content.append("## ").append(sheetName).append("\n\n");
                
                // 使用DataFormatter来格式化单元格数据
                Class<?> dataFormatterClass = Class.forName("org.apache.poi.ss.usermodel.DataFormatter");
                Object dataFormatter = dataFormatterClass.newInstance();
                
                content.append(extractSheetContent(sheet, dataFormatter));
                content.append("\n");
            }
            
            // 关闭workbook
            workbookClass.getMethod("close").invoke(workbook);
            
            return content.toString();
            
        } catch (ClassNotFoundException e) {
            throw new Exception("Apache POI library not found", e);
        }
    }
    
    /**
     * 提取工作表内容
     */
    private String extractSheetContent(Object sheet, Object dataFormatter) throws Exception {
        StringBuilder content = new StringBuilder();
        
        // Iterator<Row> rowIterator = sheet.iterator();
        Object rowIterator = sheet.getClass().getMethod("iterator").invoke(sheet);
        
        int rowCount = 0;
        StringBuilder tableHeader = new StringBuilder();
        StringBuilder tableSeparator = new StringBuilder();
        
        // while (rowIterator.hasNext())
        while ((Boolean) rowIterator.getClass().getMethod("hasNext").invoke(rowIterator)) {
            // Row row = rowIterator.next();
            Object row = rowIterator.getClass().getMethod("next").invoke(rowIterator);
            
            if (rowCount == 0) {
                // 处理表头
                String headerRow = processRow(row, dataFormatter);
                if (headerRow.trim().isEmpty()) {
                    rowCount++;
                    continue;
                }
                tableHeader.append("| ").append(headerRow).append(" |\n");
                
                // 计算列数并创建分隔符
                int columnCount = headerRow.split("\\|").length;
                tableSeparator.append("|");
                for (int i = 0; i < columnCount; i++) {
                    tableSeparator.append(" --- |");
                }
                tableSeparator.append("\n");
            } else {
                // 处理数据行
                String dataRow = processRow(row, dataFormatter);
                if (!dataRow.trim().isEmpty()) {
                    if (rowCount == 1) {
                        // 第一次添加数据行时，先添加表头和分隔符
                        content.append(tableHeader);
                        content.append(tableSeparator);
                    }
                    content.append("| ").append(dataRow).append(" |\n");
                }
            }
            rowCount++;
        }
        
        if (content.length() == 0 && tableHeader.length() > 0) {
            // 只有表头没有数据的情况
            content.append(tableHeader);
            content.append(tableSeparator);
        }
        
        content.append("\n");
        return content.toString();
    }
    
    /**
     * 处理单行数据
     */
    private String processRow(Object row, Object dataFormatter) throws Exception {
        StringBuilder rowContent = new StringBuilder();
        
        // Iterator<Cell> cellIterator = row.cellIterator();
        Object cellIterator = row.getClass().getMethod("cellIterator").invoke(row);
        
        // while (cellIterator.hasNext())
        while ((Boolean) cellIterator.getClass().getMethod("hasNext").invoke(cellIterator)) {
            // Cell cell = cellIterator.next();
            Object cell = cellIterator.getClass().getMethod("next").invoke(cellIterator);
            
            // String cellValue = dataFormatter.formatCellValue(cell);
            String cellValue = (String) dataFormatter.getClass()
                .getMethod("formatCellValue", cell.getClass().getInterfaces()[0])
                .invoke(dataFormatter, cell);
            
            if (rowContent.length() > 0) {
                rowContent.append(" | ");
            }
            rowContent.append(cellValue.replace("|", "\\|")); // 转义管道符
        }
        
        return rowContent.toString();
    }
    
    private String getFileNameWithoutExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex > 0 ? fileName.substring(0, lastDotIndex) : fileName;
    }
    
    @Override
    public String getParserName() {
        return "Excel文档解析器";
    }
    
    @Override
    public int getPriority() {
        return 80;
    }
} 