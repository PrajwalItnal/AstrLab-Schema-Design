package com.astralab.schema.service;

import com.astralab.schema.dto.ColumnSchemaDto;
import com.astralab.schema.dto.InferredSchemaResult;
import com.astralab.schema.exception.InvalidFileException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SchemaInferenceServiceTest {

    private SchemaInferenceService inferenceService;

    @BeforeEach
    void setUp() {
        inferenceService = new SchemaInferenceService();
    }

    @Test
    @DisplayName("Infer mixed types correctly from CSV file")
    void testInferCsvTypes(@TempDir Path tempDir) throws IOException {
        Path csvFile = tempDir.resolve("sample.csv");
        String csvContent = """
                id,username,active,balance,birth_date,created_at
                101,alice,true,1500.50,1995-04-12,2024-01-15 10:30:00
                102,bob,false,240.00,1988-11-23,2024-02-20 14:15:30
                103,charlie,,99.95,2001-07-09,2024-03-01 08:00:00
                """;
        Files.writeString(csvFile, csvContent);

        InferredSchemaResult result = inferenceService.inferSchemaAndSample(csvFile, "CSV", 5);

        assertThat(result.getTotalRowsEstimated()).isEqualTo(3);
        assertThat(result.getColumns()).hasSize(6);

        Map<String, ColumnSchemaDto> colMap = result.getColumns().stream()
                .collect(java.util.stream.Collectors.toMap(ColumnSchemaDto::getColumnName, c -> c));

        assertThat(colMap.get("id").getLogicalDataType()).isEqualTo("Integer");
        assertThat(colMap.get("id").getNullable()).isFalse();

        assertThat(colMap.get("username").getLogicalDataType()).isEqualTo("String");
        assertThat(colMap.get("username").getNullable()).isFalse();

        assertThat(colMap.get("active").getLogicalDataType()).isEqualTo("Boolean");
        assertThat(colMap.get("active").getNullable()).isTrue(); // row 3 has empty value

        assertThat(colMap.get("balance").getLogicalDataType()).isEqualTo("Decimal");

        assertThat(colMap.get("birth_date").getLogicalDataType()).isEqualTo("Date");

        assertThat(colMap.get("created_at").getLogicalDataType()).isEqualTo("DateTime");

        // Preview rows check
        assertThat(result.getPreviewRows()).hasSize(3);
        assertThat(result.getPreviewRows().get(0)).containsEntry("id", "101");
        assertThat(result.getPreviewRows().get(0)).containsEntry("username", "alice");
    }

    @Test
    @DisplayName("Fallback to String on inconsistent or mixed column types")
    void testInconsistentTypesFallbackToString(@TempDir Path tempDir) throws IOException {
        Path csvFile = tempDir.resolve("mixed.csv");
        String csvContent = """
                code,value
                100,250
                200,INVALID_NUMBER
                300,500
                """;
        Files.writeString(csvFile, csvContent);

        InferredSchemaResult result = inferenceService.inferSchemaAndSample(csvFile, "CSV", 5);

        ColumnSchemaDto valueCol = result.getColumns().stream()
                .filter(c -> c.getColumnName().equals("value"))
                .findFirst()
                .orElseThrow();

        assertThat(valueCol.getLogicalDataType()).isEqualTo("String");
    }

    @Test
    @DisplayName("Throw InvalidFileException on empty CSV file")
    void testEmptyFileThrowsException(@TempDir Path tempDir) throws IOException {
        Path emptyFile = tempDir.resolve("empty.csv");
        Files.writeString(emptyFile, "");

        assertThatThrownBy(() -> inferenceService.inferSchemaAndSample(emptyFile, "CSV", 5))
                .isInstanceOf(InvalidFileException.class)
                .hasMessageContaining("empty");
    }

    @Test
    @DisplayName("Read limited sample rows for preview without loading entire file")
    void testReadSampleRowsStreaming(@TempDir Path tempDir) throws IOException {
        Path csvFile = tempDir.resolve("many_rows.csv");
        StringBuilder sb = new StringBuilder("col_a,col_b\n");
        for (int i = 1; i <= 100; i++) {
            sb.append("A").append(i).append(",B").append(i).append("\n");
        }
        Files.writeString(csvFile, sb.toString());

        List<Map<String, Object>> preview = inferenceService.readSampleRows(csvFile, "CSV", List.of("col_a", "col_b"), 5);

        assertThat(preview).hasSize(5);
        assertThat(preview.get(0)).containsEntry("col_a", "A1").containsEntry("col_b", "B1");
        assertThat(preview.get(4)).containsEntry("col_a", "A5").containsEntry("col_b", "B5");
    }
}
