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
import java.util.*;
import java.util.stream.Collectors;

public class ReportProcessor {
    private static final Logger log = LoggerFactory.getLogger(ReportProcessor.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(ReportConstants.DATE_PATTERN);

    public void execute() {
        LocalDate today = LocalDate.now();
        String dateYesterday = today.minusDays(1).format(DATE_FORMATTER);
        String dateToday = today.format(DATE_FORMATTER);

        Path inputPath = Paths.get(ReportConstants.PATH_INPUT, ReportConstants.FILE_NAME_CSV_BASE + dateYesterday + ReportConstants.EXT_CSV);
        Path outputPath = Paths.get(ReportConstants.PATH_OUTPUT, ReportConstants.FILE_NAME_TXT_FINAL);
        Path backupPath = Paths.get(ReportConstants.PATH_OUTPUT, ReportConstants.FILE_NAME_CSV_BASE + dateToday + ReportConstants.EXT_TXT);

        try {
            Files.createDirectories(Paths.get(ReportConstants.PATH_LOGS));
            Files.createDirectories(Paths.get(ReportConstants.PATH_INPUT));

            if (!Files.exists(inputPath)) {
                log.error("Archivo de entrada no encontrado: {}", inputPath);
                return;
            }

            log.info("Iniciando lectura de: {}", inputPath.getFileName());

            List<String> allLines = Files.readAllLines(inputPath, Charset.forName(ReportConstants.CHARSET_WINDOWS));
            if (allLines.isEmpty()) return;

            // 1. Mapear dinámicamente qué columna está en qué índice del CSV
            Map<ReportColumn, Integer> indexMap = mapCsvHeaders(allLines.get(0));

            // 2. Transformar datos
            List<String> txtLines = new ArrayList<>();
            txtLines.add(generateTxtHeader()); // Cabecera fija

            for (int i = 1; i < allLines.size(); i++) {
                txtLines.add(formatLine(allLines.get(i), indexMap));
            }

            // 3. Escribir en UTF-8
            byte[] finalBytes = String.join(System.lineSeparator(), txtLines).getBytes(StandardCharsets.UTF_8);
            Files.write(outputPath, finalBytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            Files.write(backupPath, finalBytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            log.info("Reporte generado exitosamente. Backup: {}", backupPath.getFileName());

        } catch (IOException e) {
            log.error("Error procesando archivos: {}", e.getMessage());
        }
    }

    private Map<ReportColumn, Integer> mapCsvHeaders(String headerLine) {
        Map<ReportColumn, Integer> map = new HashMap<>();
        String[] headers = headerLine.split(",");
        for (int i = 0; i < headers.length; i++) {
            int finalI = i;
            ReportColumn.findMatch(headers[i])
                    .ifPresent(col -> map.put(col, finalI));
        }
        return map;
    }

    private String generateTxtHeader() {
        StringBuilder sb = new StringBuilder();
        for (ReportColumn col : ReportColumn.values()) {
            sb.append(padRight(col.getColumnName(), col.getLength()));
        }
        return sb.toString();
    }

    private String formatLine(String csvLine, Map<ReportColumn, Integer> indexMap) {
        String[] fields = csvLine.split(",", -1);
        StringBuilder sb = new StringBuilder();

        for (ReportColumn col : ReportColumn.values()) {
            Integer index = indexMap.get(col);
            String value = (index != null && index < fields.length) ? fields[index].trim() : "";
            sb.append(padRight(value, col.getLength()));
        }
        return sb.toString();
    }

    private String padRight(String text, int length) {
        String safeText = (text == null) ? "" : text;
        return String.format("%-" + length + "." + length + "s", safeText);
    }
}