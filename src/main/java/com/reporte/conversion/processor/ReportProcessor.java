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

public class ReportProcessor {
    private static final Logger log = LoggerFactory.getLogger(ReportProcessor.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(ReportConstants.DATE_PATTERN);

    public void execute() {
        LocalDate today = LocalDate.now();
        String dateYesterday = today.minusDays(1).format(DATE_FORMATTER);
        String dateToday = today.format(DATE_FORMATTER);

        Path inputPath = Paths.get(ReportConstants.PATH_INPUT,
                ReportConstants.FILE_NAME_CSV_BASE + dateYesterday + ReportConstants.EXT_CSV);
        Path outputPath = Paths.get(ReportConstants.PATH_OUTPUT, ReportConstants.FILE_NAME_TXT_FINAL);
        Path backupPath = Paths.get(ReportConstants.PATH_OUTPUT,
                ReportConstants.FILE_NAME_CSV_BASE + dateToday + ReportConstants.EXT_TXT);

        try {
            // Asegurar rutas de directorios (Incluyendo la de logs desde constantes)
            Files.createDirectories(Paths.get(ReportConstants.PATH_LOGS));
            Files.createDirectories(Paths.get(ReportConstants.PATH_INPUT));
            Files.createDirectories(Paths.get(ReportConstants.PATH_OUTPUT));

            if (!Files.exists(inputPath)) {
                log.error("Archivo de entrada no encontrado en la ruta: {}", inputPath);
                return;
            }

            log.info("Leyendo archivo origen: {}", inputPath.getFileName());

            // Invocación del método de transformación
            List<String> txtLines = transformContent(inputPath);

            // Escritura en UTF-8
            byte[] finalBytes = String.join(System.lineSeparator(), txtLines).getBytes(StandardCharsets.UTF_8);

            Files.write(outputPath, finalBytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            Files.write(backupPath, finalBytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            log.info("Reporte '{}' generado exitosamente.", ReportConstants.FILE_NAME_TXT_FINAL);
            log.info("Backup creado: {}", backupPath.getFileName());

        } catch (IOException e) {
            log.error("Error crítico en el procesamiento: {}", e.getMessage());
        }
    }

    /**
     * Responsable de la lógica de transformación de formato CSV a Ancho Fijo.
     */
    private List<String> transformContent(Path inputPath) throws IOException {
        Charset win1252 = Charset.forName(ReportConstants.CHARSET_WINDOWS);
        List<String> allLines = Files.readAllLines(inputPath, win1252);

        if (allLines.isEmpty()) {
            log.warn("El archivo origen está vacío.");
            return Collections.emptyList();
        }

        // 1. Mapear índices usando ReportColumn.findMatch (Sincroniza CSV con el Enum)
        Map<ReportColumn, Integer> indexMap = mapCsvHeaders(allLines.get(0));

        List<String> result = new ArrayList<>();

        // 2. Generar Cabecera TXT
        result.add(generateTxtHeader());

        // 3. Procesar Líneas de Datos
        for (int i = 1; i < allLines.size(); i++) {
            result.add(formatLine(allLines.get(i), indexMap));
        }

        return result;
    }

    private Map<ReportColumn, Integer> mapCsvHeaders(String headerLine) {
        Map<ReportColumn, Integer> map = new HashMap<>();
        String[] headers = headerLine.split(ReportConstants.REG_EXP);

        for (int i = 0; i < headers.length; i++) {
            // Creamos una variable final que capture el valor de i para esta iteración
            final int currentIndex = i;
            String currentHeader = headers[i].trim();

            ReportColumn.findMatch(currentHeader)
                    .ifPresent(col -> map.put(col, currentIndex));
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
        String[] fields = csvLine.split(ReportConstants.REG_EXP, -1);
        StringBuilder sb = new StringBuilder();

        for (ReportColumn col : ReportColumn.values()) {
            Integer index = indexMap.get(col);
            String value = (index != null && index < fields.length) ? fields[index].trim() : "";

            // Limpieza de comillas si existen (ej: "-7,900.00" -> -7,900.00)
            if (value.startsWith("\"") && value.endsWith("\"")) {
                value = value.substring(1, value.length() - 1);
            }

            sb.append(padRight(value, col.getLength()));
        }
        return sb.toString();
    }

    private String padRight(String text, int length) {
        String safeText = (text == null) ? "" : text;
        // %-Ns rellena con espacios a la derecha, .N trunca si es más largo
        return String.format("%-" + length + "." + length + "s", safeText);
    }
}