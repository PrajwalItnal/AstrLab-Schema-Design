package com.astralab.schema.service;

import com.astralab.schema.dto.ColumnSchemaDto;
import com.astralab.schema.dto.InferredSchemaResult;
import com.astralab.schema.exception.InvalidFileException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class SchemaInferenceService {

    private static final int DEFAULT_INFERENCE_SAMPLE_LIMIT = 500;

    private static final Pattern INTEGER_PATTERN = Pattern.compile("^[+-]?\\d+$");
    private static final Pattern DECIMAL_PATTERN = Pattern.compile("^[+-]?(\\d+(\\.\\d*)?|\\.\\d+)([eE][+-]?\\d+)?$");
    private static final Set<String> BOOLEAN_VALUES = Set.of("true", "false", "yes", "no", "1", "0", "t", "f", "y", "n");

    private static final List<DateTimeFormatter> DATE_TIME_FORMATTERS = List.of(
            DateTimeFormatter.ISO_OFFSET_DATE_TIME,
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
    );

    private static final List<DateTimeFormatter> DATE_FORMATTERS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd")
    );

    public InferredSchemaResult inferSchemaAndSample(Path filePath, String fileFormat, int maxSampleRows) {
        String format = normalizeFormat(fileFormat, filePath);
        if ("CSV".equalsIgnoreCase(format)) {
            return inferCsv(filePath, maxSampleRows);
        } else if ("XLSX".equalsIgnoreCase(format) || "XLS".equalsIgnoreCase(format)) {
            return inferExcel(filePath, maxSampleRows);
        } else {
            throw new InvalidFileException("Unsupported file format: " + format + ". Allowed formats are CSV, XLSX, XLS.");
        }
    }

    public List<Map<String, Object>> readSampleRows(Path filePath, String fileFormat, List<String> columnNames, int limit) {
        String format = normalizeFormat(fileFormat, filePath);
        if ("CSV".equalsIgnoreCase(format)) {
            return readCsvSampleRows(filePath, columnNames, limit);
        } else if ("XLSX".equalsIgnoreCase(format) || "XLS".equalsIgnoreCase(format)) {
            return readExcelSampleRows(filePath, columnNames, limit);
        } else {
            throw new InvalidFileException("Unsupported file format: " + format);
        }
    }

    public String detectFormat(String filename) {
        if (filename == null) return "UNKNOWN";
        String lower = filename.toLowerCase();
        if (lower.endsWith(".csv")) return "CSV";
        if (lower.endsWith(".xlsx")) return "XLSX";
        if (lower.endsWith(".xls")) return "XLS";
        return "UNKNOWN";
    }

    private String normalizeFormat(String fileFormat, Path filePath) {
        if (fileFormat != null && !fileFormat.isBlank() && !"UNKNOWN".equalsIgnoreCase(fileFormat)) {
            return fileFormat.toUpperCase();
        }
        return detectFormat(filePath.getFileName().toString());
    }

    // -------------------------------------------------------------
    // CSV INFERENCE & PREVIEW
    // -------------------------------------------------------------
    private InferredSchemaResult inferCsv(Path filePath, int maxSampleRows) {
        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setIgnoreHeaderCase(true)
                     .setTrim(true)
                     .setIgnoreEmptyLines(true)
                     .build()
                     .parse(reader)) {

            List<String> headers = parser.getHeaderNames();
            if (headers == null || headers.isEmpty()) {
                throw new InvalidFileException("The CSV file is empty or does not contain a valid header row.");
            }

            // Sanitize headers
            List<String> cleanHeaders = new ArrayList<>();
            for (int i = 0; i < headers.size(); i++) {
                String h = headers.get(i);
                if (h == null || h.trim().isEmpty()) {
                    cleanHeaders.add("column_" + (i + 1));
                } else {
                    cleanHeaders.add(h.trim());
                }
            }

            int numCols = cleanHeaders.size();
            ColumnInferenceTracker[] trackers = new ColumnInferenceTracker[numCols];
            for (int i = 0; i < numCols; i++) {
                trackers[i] = new ColumnInferenceTracker();
            }

            List<Map<String, Object>> previewRows = new ArrayList<>();
            long rowCount = 0;

            for (CSVRecord record : parser) {
                rowCount++;
                Map<String, Object> rowMap = new LinkedHashMap<>();

                for (int i = 0; i < numCols; i++) {
                    String colName = cleanHeaders.get(i);
                    String val = i < record.size() ? record.get(i) : null;
                    if (val != null) {
                        val = val.trim();
                        if (val.isEmpty()) {
                            val = null;
                        }
                    }

                    if (rowCount <= DEFAULT_INFERENCE_SAMPLE_LIMIT) {
                        trackers[i].inspectValue(val);
                    }
                    if (rowCount <= maxSampleRows) {
                        rowMap.put(colName, val);
                    }
                }

                if (rowCount <= maxSampleRows) {
                    previewRows.add(rowMap);
                }
            }

            if (rowCount == 0) {
                // Headers exist but no data rows; all columns default to String and nullable
                List<ColumnSchemaDto> columns = new ArrayList<>();
                for (int i = 0; i < numCols; i++) {
                    columns.add(new ColumnSchemaDto(
                            cleanHeaders.get(i),
                            "VARCHAR(255)",
                            "String",
                            i + 1,
                            true,
                            null
                    ));
                }
                return new InferredSchemaResult(columns, previewRows, 0);
            }

            List<ColumnSchemaDto> columns = new ArrayList<>();
            for (int i = 0; i < numCols; i++) {
                InferredType inferred = trackers[i].resolveType();
                columns.add(new ColumnSchemaDto(
                        cleanHeaders.get(i),
                        inferred.sourceType,
                        inferred.logicalType,
                        i + 1,
                        trackers[i].hasNulls || (rowCount > 0 && trackers[i].nonEmptyCount < rowCount),
                        inferred.formatPattern
                ));
            }

            return new InferredSchemaResult(columns, previewRows, rowCount);

        } catch (InvalidFileException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidFileException("Failed to read and infer schema from CSV file: " + e.getMessage(), e);
        }
    }

    private List<Map<String, Object>> readCsvSampleRows(Path filePath, List<String> columnNames, int limit) {
        List<Map<String, Object>> rows = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setIgnoreHeaderCase(true)
                     .setTrim(true)
                     .setIgnoreEmptyLines(true)
                     .build()
                     .parse(reader)) {

            List<String> headers = parser.getHeaderNames();
            List<String> effectiveHeaders = (columnNames != null && !columnNames.isEmpty()) ? columnNames : headers;

            int count = 0;
            for (CSVRecord record : parser) {
                if (count >= limit) break;
                Map<String, Object> rowMap = new LinkedHashMap<>();
                for (int i = 0; i < effectiveHeaders.size(); i++) {
                    String colName = effectiveHeaders.get(i);
                    String val = (i < record.size()) ? record.get(i) : null;
                    rowMap.put(colName, (val != null && !val.trim().isEmpty()) ? val.trim() : null);
                }
                rows.add(rowMap);
                count++;
            }
        } catch (Exception e) {
            throw new InvalidFileException("Error reading CSV preview rows: " + e.getMessage(), e);
        }
        return rows;
    }

    // -------------------------------------------------------------
    // EXCEL INFERENCE & PREVIEW
    // -------------------------------------------------------------
    private InferredSchemaResult inferExcel(Path filePath, int maxSampleRows) {
        try (InputStream is = Files.newInputStream(filePath);
             Workbook workbook = WorkbookFactory.create(is)) {

            if (workbook.getNumberOfSheets() == 0) {
                throw new InvalidFileException("The Excel workbook contains no sheets.");
            }

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getPhysicalNumberOfRows() == 0) {
                throw new InvalidFileException("The first sheet of the Excel file is empty.");
            }

            Row headerRow = sheet.getRow(sheet.getFirstRowNum());
            if (headerRow == null) {
                throw new InvalidFileException("The Excel sheet does not contain a valid header row.");
            }

            List<String> headers = new ArrayList<>();
            int lastCellNum = headerRow.getLastCellNum();
            if (lastCellNum <= 0) {
                throw new InvalidFileException("The Excel header row contains no columns.");
            }

            DataFormatter formatter = new DataFormatter();
            for (int c = 0; c < lastCellNum; c++) {
                Cell cell = headerRow.getCell(c);
                String val = cell != null ? formatter.formatCellValue(cell).trim() : "";
                if (val.isEmpty()) {
                    headers.add("column_" + (c + 1));
                } else {
                    headers.add(val);
                }
            }

            int numCols = headers.size();
            ColumnInferenceTracker[] trackers = new ColumnInferenceTracker[numCols];
            for (int i = 0; i < numCols; i++) {
                trackers[i] = new ColumnInferenceTracker();
            }

            List<Map<String, Object>> previewRows = new ArrayList<>();
            long rowCount = 0;

            int startRow = sheet.getFirstRowNum() + 1;
            int lastRow = sheet.getLastRowNum();

            for (int r = startRow; r <= lastRow; r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;
                rowCount++;

                Map<String, Object> rowMap = new LinkedHashMap<>();
                for (int c = 0; c < numCols; c++) {
                    String colName = headers.get(c);
                    Cell cell = row.getCell(c);
                    String val = null;
                    if (cell != null) {
                        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                            Date d = cell.getDateCellValue();
                            val = d != null ? d.toInstant().toString() : null;
                        } else {
                            val = formatter.formatCellValue(cell).trim();
                        }
                    }
                    if (val != null && val.isEmpty()) {
                        val = null;
                    }

                    if (rowCount <= DEFAULT_INFERENCE_SAMPLE_LIMIT) {
                        trackers[c].inspectValue(val);
                    }
                    if (rowCount <= maxSampleRows) {
                        rowMap.put(colName, val);
                    }
                }

                if (rowCount <= maxSampleRows) {
                    previewRows.add(rowMap);
                }
            }

            List<ColumnSchemaDto> columns = new ArrayList<>();
            for (int i = 0; i < numCols; i++) {
                InferredType inferred = trackers[i].resolveType();
                columns.add(new ColumnSchemaDto(
                        headers.get(i),
                        inferred.sourceType,
                        inferred.logicalType,
                        i + 1,
                        trackers[i].hasNulls || (rowCount > 0 && trackers[i].nonEmptyCount < rowCount),
                        inferred.formatPattern
                ));
            }

            return new InferredSchemaResult(columns, previewRows, rowCount);

        } catch (InvalidFileException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidFileException("Failed to read and infer schema from Excel file: " + e.getMessage(), e);
        }
    }

    private List<Map<String, Object>> readExcelSampleRows(Path filePath, List<String> columnNames, int limit) {
        List<Map<String, Object>> rows = new ArrayList<>();
        try (InputStream is = Files.newInputStream(filePath);
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) return rows;

            Row headerRow = sheet.getRow(sheet.getFirstRowNum());
            DataFormatter formatter = new DataFormatter();
            List<String> headers = new ArrayList<>();
            int lastCellNum = (headerRow != null) ? headerRow.getLastCellNum() : 0;

            for (int c = 0; c < lastCellNum; c++) {
                Cell cell = headerRow != null ? headerRow.getCell(c) : null;
                String val = cell != null ? formatter.formatCellValue(cell).trim() : "";
                headers.add(val.isEmpty() ? "column_" + (c + 1) : val);
            }

            List<String> effectiveHeaders = (columnNames != null && !columnNames.isEmpty()) ? columnNames : headers;
            int startRow = sheet.getFirstRowNum() + 1;
            int lastRow = sheet.getLastRowNum();
            int count = 0;

            for (int r = startRow; r <= lastRow && count < limit; r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;
                Map<String, Object> rowMap = new LinkedHashMap<>();
                for (int c = 0; c < effectiveHeaders.size(); c++) {
                    String colName = effectiveHeaders.get(c);
                    Cell cell = row.getCell(c);
                    String val = null;
                    if (cell != null) {
                        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                            Date d = cell.getDateCellValue();
                            val = d != null ? d.toInstant().toString() : null;
                        } else {
                            val = formatter.formatCellValue(cell).trim();
                        }
                    }
                    rowMap.put(colName, (val != null && !val.isEmpty()) ? val : null);
                }
                rows.add(rowMap);
                count++;
            }
        } catch (Exception e) {
            throw new InvalidFileException("Error reading Excel preview rows: " + e.getMessage(), e);
        }
        return rows;
    }

    // -------------------------------------------------------------
    // TYPE INFERENCE HELPERS
    // -------------------------------------------------------------
    private static class InferredType {
        String logicalType;
        String sourceType;
        String formatPattern;

        InferredType(String logicalType, String sourceType, String formatPattern) {
            this.logicalType = logicalType;
            this.sourceType = sourceType;
            this.formatPattern = formatPattern;
        }
    }

    private static class ColumnInferenceTracker {
        long nonEmptyCount = 0;
        long booleanCount = 0;
        long integerCount = 0;
        long decimalCount = 0;
        long dateTimeCount = 0;
        long dateCount = 0;
        boolean hasNulls = false;
        String detectedDateFormat = null;

        void inspectValue(String val) {
            if (val == null || val.trim().isEmpty()) {
                hasNulls = true;
                return;
            }

            nonEmptyCount++;
            String trimmed = val.trim();

            if (BOOLEAN_VALUES.contains(trimmed.toLowerCase())) {
                booleanCount++;
            }

            if (INTEGER_PATTERN.matcher(trimmed).matches()) {
                integerCount++;
                decimalCount++; // All integers can be represented as numbers
            } else if (DECIMAL_PATTERN.matcher(trimmed).matches()) {
                decimalCount++;
            }

            // Check DateTime / Date
            boolean matchedDate = false;
            for (DateTimeFormatter fmt : DATE_TIME_FORMATTERS) {
                try {
                    LocalDateTime.parse(trimmed, fmt);
                    dateTimeCount++;
                    matchedDate = true;
                    if (detectedDateFormat == null) {
                        detectedDateFormat = "yyyy-MM-dd HH:mm:ss";
                    }
                    break;
                } catch (Exception ignored) {
                }
            }

            if (!matchedDate) {
                for (DateTimeFormatter fmt : DATE_FORMATTERS) {
                    try {
                        LocalDate.parse(trimmed, fmt);
                        dateCount++;
                        if (detectedDateFormat == null) {
                            detectedDateFormat = "yyyy-MM-dd";
                        }
                        break;
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        InferredType resolveType() {
            if (nonEmptyCount == 0) {
                return new InferredType("String", "VARCHAR(255)", null);
            }

            if (booleanCount == nonEmptyCount) {
                return new InferredType("Boolean", "BOOLEAN", null);
            }

            if (integerCount == nonEmptyCount) {
                return new InferredType("Integer", "BIGINT", null);
            }

            if (decimalCount == nonEmptyCount) {
                return new InferredType("Decimal", "DECIMAL(18,4)", null);
            }

            if (dateTimeCount == nonEmptyCount) {
                return new InferredType("DateTime", "TIMESTAMP WITH TIME ZONE", detectedDateFormat);
            }

            if (dateCount == nonEmptyCount) {
                return new InferredType("Date", "DATE", detectedDateFormat);
            }

            // Inconsistent or mixed types fallback to String
            return new InferredType("String", "VARCHAR(255)", null);
        }
    }
}
