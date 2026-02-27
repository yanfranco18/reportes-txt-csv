package com.reporte.conversion.model;

public class ColumnConfig {
    private final String name;
    private final int position;
    private final int length;

    public ColumnConfig(String name, int position, int length) {
        this.name = name;
        this.position = position;
        this.length = length;
    }

    public String getName() {
        return name;
    }

    public int getPosition() {
        return position;
    }

    public int getLength() {
        return length;
    }
}
