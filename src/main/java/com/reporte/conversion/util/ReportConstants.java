package com.reporte.conversion.util;

public final class ReportConstants {

    // Constructor privado para evitar que se instancie la clase (Principio Utility Class)
    private ReportConstants() {
        throw new UnsupportedOperationException("Clase de utilidades");
    }

    // Rutas de Directorios
    public static final String PATH_INPUT = "C:/reportes/csv";
    public static final String PATH_OUTPUT = "C:/reportes/txt";
    public static final String PATH_LOGS = "C:/reportes/txt/log";

    // Formatos de Archivos
    public static final String FILE_NAME_CSV_BASE = "reporte_general";
    public static final String FILE_NAME_TXT_FINAL = "reporte_general.txt";
    public static final String FILE_NAME_LOG = "log_reporte_general.txt";

    // Configuración Técnica
    public static final String CHARSET_WINDOWS = "windows-1252";
    public static final String DATE_PATTERN = "dd-MM-yyyy";

    // Extensiones
    public static final String EXT_CSV = ".csv";
    public static final String EXT_TXT = ".txt";
}