package com.astralab.schema.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InferredSchemaResult {
    private List<ColumnSchemaDto> columns = new ArrayList<>();
    private List<Map<String, Object>> previewRows = new ArrayList<>();
    private long totalRowsEstimated = 0;

    public InferredSchemaResult() {
    }

    public InferredSchemaResult(List<ColumnSchemaDto> columns, List<Map<String, Object>> previewRows, long totalRowsEstimated) {
        this.columns = columns;
        this.previewRows = previewRows;
        this.totalRowsEstimated = totalRowsEstimated;
    }

    public List<ColumnSchemaDto> getColumns() {
        return columns;
    }

    public void setColumns(List<ColumnSchemaDto> columns) {
        this.columns = columns;
    }

    public List<Map<String, Object>> getPreviewRows() {
        return previewRows;
    }

    public void setPreviewRows(List<Map<String, Object>> previewRows) {
        this.previewRows = previewRows;
    }

    public long getTotalRowsEstimated() {
        return totalRowsEstimated;
    }

    public void setTotalRowsEstimated(long totalRowsEstimated) {
        this.totalRowsEstimated = totalRowsEstimated;
    }
}
