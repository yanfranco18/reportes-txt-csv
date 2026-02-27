package com.reporte.conversion.util;

public final class ConfigLoader {

    private ConfigLoader() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static String getJarName() {
        return System.getProperty("jar.name", "reporte-csv-to-txt");
    }

    public static String getPathBase() {
        return System.getProperty("path.base", "C:/reportes");
    }

    public static String getPathInput() {
        return System.getProperty("path.input", getPathBase() + "/csv");
    }

    public static String getPathOutput() {
        return System.getProperty("path.output", getPathBase() + "/txt");
    }

    public static String getPathLogs() {
        return System.getProperty("path.logs", getPathBase() + "/log");
    }

    public static String getColumnsFilePath() {
        return getPathBase() + "/columns_" + getJarName() + ".txt";
    }
}
