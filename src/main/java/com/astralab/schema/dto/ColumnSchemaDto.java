package com.astralab.schema.dto;

public class ColumnSchemaDto {
    private String columnName;
    private String sourceDataType;
    private String logicalDataType;
    private Integer ordinalPosition;
    private Boolean nullable;
    private String formatPattern;

    public ColumnSchemaDto() {
    }

    public ColumnSchemaDto(String columnName, String sourceDataType, String logicalDataType,
                           Integer ordinalPosition, Boolean nullable, String formatPattern) {
        this.columnName = columnName;
        this.sourceDataType = sourceDataType;
        this.logicalDataType = logicalDataType;
        this.ordinalPosition = ordinalPosition;
        this.nullable = nullable;
        this.formatPattern = formatPattern;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getSourceDataType() {
        return sourceDataType;
    }

    public void setSourceDataType(String sourceDataType) {
        this.sourceDataType = sourceDataType;
    }

    public String getLogicalDataType() {
        return logicalDataType;
    }

    public void setLogicalDataType(String logicalDataType) {
        this.logicalDataType = logicalDataType;
    }

    public Integer getOrdinalPosition() {
        return ordinalPosition;
    }

    public void setOrdinalPosition(Integer ordinalPosition) {
        this.ordinalPosition = ordinalPosition;
    }

    public Boolean getNullable() {
        return nullable;
    }

    public void setNullable(Boolean nullable) {
        this.nullable = nullable;
    }

    public String getFormatPattern() {
        return formatPattern;
    }

    public void setFormatPattern(String formatPattern) {
        this.formatPattern = formatPattern;
    }
}
