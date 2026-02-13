package com.reporte.conversion.processor;

import com.reporte.conversion.model.ReportColumn;
import com.reporte.conversion.util.ReportConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReportProcessor {
    private static final Logger log = LoggerFactory.getLogger(ReportProcessor.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(ReportConstants.DATE_PATTERN);

    public void execute() {
        // --- LÓGICA DE FECHAS ---
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        String dateYesterday = yesterday.format(DATE_FORMATTER);
        String dateToday = today.format(DATE_FORMATTER);

        // Archivo de entrada (Ayer)
        String inputFileName = ReportConstants.FILE_NAME_CSV_BASE + dateYesterday + ReportConstants.EXT_CSV;
        // Archivo de backup (Hoy)
        String backupFileName = ReportConstants.FILE_NAME_CSV_BASE + dateToday + ReportConstants.EXT_TXT;

        Path inputPath = Paths.get(ReportConstants.PATH_INPUT, inputFileName);
        Path outputPath = Paths.get(ReportConstants.PATH_OUTPUT, ReportConstants.FILE_NAME_TXT_FINAL);
        Path backupPath = Paths.get(ReportConstants.PATH_OUTPUT, backupFileName);

        try {
            createDirectories();

            if (!Files.exists(inputPath)) {
                log.error("Error: No existe el reporte de ayer: {}", inputPath);
                return;
            }

            log.info("Procesando CSV del día anterior ({})...", dateYesterday);

            // Leer con Windows-1252
            List<String> content = transformContent(inputPath);

            // Escribir con UTF-8
            writeOutputFiles(content, outputPath, backupPath);

            log.info("Reporte general actualizado y backup creado con fecha: {}", dateToday);

        } catch (IOException e) {
            log.error("Fallo crítico en el sistema de archivos: {}", e.getMessage());
        }
    }

    private List<String> transformContent(Path inputPath) throws IOException {
        Charset win1252 = Charset.forName(ReportConstants.CHARSET_WINDOWS);

        try (Stream<String> lines = Files.lines(inputPath, win1252)) {
            List<String> result = new ArrayList<>();

            // 1. Cabecera (Desde el Enum)
            result.add(generateHeader());

            // 2. Datos (Saltando cabecera del CSV)
            result.addAll(lines.skip(1)
                    .map(this::formatLine)
                    .collect(Collectors.toList()));

            return result;
        }
    }

    private String generateHeader() {
        StringBuilder sb = new StringBuilder();
        for (ReportColumn col : ReportColumn.values()) {
            sb.append(padRight(col.getColumnName(), col.getLength()));
        }
        return sb.toString();
    }

    private String formatLine(String csvLine) {
        String[] fields = csvLine.split(",", -1);
        StringBuilder sb = new StringBuilder();
        for (ReportColumn col : ReportColumn.values()) {
            // El enum ya sabe su posición, pero aquí usamos el índice del array
            int index = col.ordinal();
            String val = (index < fields.length) ? fields[index].trim() : "";
            sb.append(padRight(val, col.getLength()));
        }
        return sb.toString();
    }

    private String padRight(String text, int length) {
        if (text == null) text = "";
        return String.format("%-" + length + "." + length + "s", text);
    }

    private void writeOutputFiles(List<String> lines, Path out, Path backup) throws IOException {
        String finalString = String.join(System.lineSeparator(), lines);
        byte[] bytes = finalString.getBytes(StandardCharsets.UTF_8);

        // Sobrescribe el principal y crea/sobrescribe el backup del día
        Files.write(out, bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        Files.write(backup, bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private void createDirectories() throws IOException {
        Files.createDirectories(Paths.get(ReportConstants.PATH_LOGS));
        Files.createDirectories(Paths.get(ReportConstants.PATH_INPUT));
    }
}