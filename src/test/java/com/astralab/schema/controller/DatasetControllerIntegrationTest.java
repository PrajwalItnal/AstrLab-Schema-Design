package com.astralab.schema.controller;

import com.astralab.schema.AstraLabApplication;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = AstraLabApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class DatasetControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("End-to-end: Upload dataset, preview schema and sample rows, list, and filter by zone/owner")
    void testUploadPreviewAndListFlow() throws Exception {
        String csvData = """
                order_id,customer_name,amount,is_paid,order_date
                1001,John Doe,149.99,true,2026-05-10
                1002,Jane Smith,29.50,false,2026-05-11
                1003,Bob Johnson,499.00,true,2026-05-12
                """;

        MockMultipartFile csvFile = new MockMultipartFile(
                "file",
                "orders_2026.csv",
                "text/csv",
                csvData.getBytes()
        );

        // 1. Upload dataset
        MvcResult uploadResult = mockMvc.perform(multipart("/api/v1/datasets/upload")
                        .file(csvFile)
                        .param("name", "E-Commerce Orders")
                        .param("description", "Daily order transactions")
                        .param("zone", "bronze")
                        .param("owner", "sales_analyst"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("E-Commerce Orders"))
                .andExpect(jsonPath("$.zone").value("bronze"))
                .andExpect(jsonPath("$.owner").value("sales_analyst"))
                .andExpect(jsonPath("$.fileFormat").value("CSV"))
                .andExpect(jsonPath("$.columns").isArray())
                .andReturn();

        JsonNode uploadJson = objectMapper.readTree(uploadResult.getResponse().getContentAsString());
        String datasetId = uploadJson.get("id").asText();
        assertThat(datasetId).startsWith("ds_");

        // Verify inferred columns from upload response
        JsonNode columns = uploadJson.get("columns");
        assertThat(columns.size()).isEqualTo(5);

        // 2. Preview API
        mockMvc.perform(get("/api/v1/datasets/{id}/preview", datasetId)
                        .param("limit", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datasetId").value(datasetId))
                .andExpect(jsonPath("$.datasetName").value("E-Commerce Orders"))
                .andExpect(jsonPath("$.zone").value("bronze"))
                .andExpect(jsonPath("$.owner").value("sales_analyst"))
                .andExpect(jsonPath("$.totalColumns").value(5))
                .andExpect(jsonPath("$.sampleRowCount").value(2))
                .andExpect(jsonPath("$.rows").isArray())
                .andExpect(jsonPath("$.rows[0].order_id").value("1001"))
                .andExpect(jsonPath("$.rows[0].customer_name").value("John Doe"))
                .andExpect(jsonPath("$.rows[1].order_id").value("1002"));

        // 3. Get Dataset Detail by ID
        mockMvc.perform(get("/api/v1/datasets/{id}", datasetId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(datasetId))
                .andExpect(jsonPath("$.name").value("E-Commerce Orders"))
                .andExpect(jsonPath("$.storagePath").isNotEmpty())
                .andExpect(jsonPath("$.checksum").isNotEmpty());

        // 4. List and filter by zone & owner
        mockMvc.perform(get("/api/v1/datasets")
                        .param("zone", "bronze")
                        .param("owner", "sales_analyst"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].name").value("E-Commerce Orders"))
                .andExpect(jsonPath("$.content[0].zone").value("bronze"));

        // 5. Search by keyword
        mockMvc.perform(get("/api/v1/datasets")
                        .param("search", "Commerce"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(datasetId));

        // 6. Search with non-matching filter
        mockMvc.perform(get("/api/v1/datasets")
                        .param("zone", "gold"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    @DisplayName("Return 404 for preview of non-existent dataset")
    void testPreviewNonExistentDatasetReturns404() throws Exception {
        mockMvc.perform(get("/api/v1/datasets/{id}/preview", "ds_nonexistent_9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(containsString("Dataset not found")));
    }

    @Test
    @DisplayName("Return 400 when uploading empty file")
    void testUploadEmptyFileReturns400() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.csv",
                "text/csv",
                new byte[0]
        );

        mockMvc.perform(multipart("/api/v1/datasets/upload")
                        .file(emptyFile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(containsString("empty")));
    }
}
