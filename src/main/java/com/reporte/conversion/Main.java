package com.reporte.conversion;

import com.reporte.conversion.processor.ReportProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        log.info("=== INICIANDO SISTEMA DE CONVERSIÓN CSV A TXT ===");

        try {
            ReportProcessor processor = new ReportProcessor();
            processor.execute();
        } catch (Exception e) {
            log.error("Error inesperado en la ejecución: {}", e.getMessage(), e);
            System.exit(1);
        }

        log.info("=== PROCESO FINALIZADO ===");
    }
}