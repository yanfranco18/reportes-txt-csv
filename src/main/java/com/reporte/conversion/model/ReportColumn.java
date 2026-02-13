package com.reporte.conversion.model;

import java.util.Arrays;
import java.util.Optional;

public enum ReportColumn {
    ID_PRODUCTO("id_producto", 1, 5),
    DESCRIPCION("descripcion", 6, 25),
    FECHA_INGRESO("fecha_ingreso", 31, 20),
    PESO("peso", 51, 10),
    ALTURA("altura", 61, 10),
    TIPO_PRODUCTO("tipo_producto", 71, 15),
    MATERIAL("material", 86, 15);

    private final String columnName;
    private final int position;
    private final int length;

    ReportColumn(String columnName, int position, int length) {
        this.columnName = columnName;
        this.position = position;
        this.length = length;
    }

    /**
     * Busca la columna intentando hacer match primero con el columnName
     * y luego con el nombre de la variable (Enum name).
     */
    public static Optional<ReportColumn> findMatch(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) {
            return Optional.empty();
        }

        String searchKey = identifier.trim();

        return Arrays.stream(ReportColumn.values())
                .filter(col -> col.columnName.equalsIgnoreCase(searchKey) ||
                        col.name().equalsIgnoreCase(searchKey))
                .findFirst();
    }

    // Getters
    public String getColumnName() { return columnName; }
    public int getPosition() { return position; }
    public int getLength() { return length; }
}