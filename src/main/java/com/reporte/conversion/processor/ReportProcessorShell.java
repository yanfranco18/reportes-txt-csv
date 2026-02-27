package com.reporte.conversion.processor;

import com.reporte.conversion.model.ColumnConfig;
import com.reporte.conversion.util.ConfigLoader;
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

public class ReportProcessorShell {
    private static final Logger log = LoggerFactory.getLogger(ReportProcessorShell.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(ReportConstants.DATE_PATTERN);

    public void execute() {
        log.info("Iniciando procesamiento dinámico (Shell Edition)");

        // Obtener configuraciones centralizadas desde ConfigLoader
        String pathInput = ConfigLoader.getPathInput();
        String pathOutput = ConfigLoader.getPathOutput();
        String pathLogs = ConfigLoader.getPathLogs();
        Path columnsPath = Paths.get(ConfigLoader.getColumnsFilePath());

        LocalDate today = LocalDate.now();
        String dateYesterday = today.minusDays(1).format(DATE_FORMATTER);
        String dateToday = today.format(DATE_FORMATTER);

        Path inputPath = Paths.get(pathInput,
                ReportConstants.FILE_NAME_CSV_BASE + dateYesterday + ReportConstants.EXT_CSV);
        Path outputPath = Paths.get(pathOutput, ReportConstants.FILE_NAME_TXT_FINAL);
        Path backupPath = Paths.get(pathOutput,
                ReportConstants.FILE_NAME_CSV_BASE + dateToday + ReportConstants.EXT_TXT);

        try {
            // Asegurar directorios
            Files.createDirectories(Paths.get(pathLogs));
            Files.createDirectories(Paths.get(pathInput));
            Files.createDirectories(Paths.get(pathOutput));

            // 1. Cargar Columnas
            List<ColumnConfig> columns = loadColumns(columnsPath);
            if (columns.isEmpty()) {
                log.error("No se pudieron cargar las columnas desde: {}", columnsPath);
                return;
            }

            if (!Files.exists(inputPath)) {
                log.error("Archivo de entrada no encontrado: {}", inputPath);
                return;
            }

            log.info("Leyendo archivo origen: {}", inputPath.getFileName());

            // 2. Transformar Contenido
            List<String> txtLines = transformContent(inputPath, columns);

            // 3. Escribir Resultados
            byte[] finalBytes = String.join(System.lineSeparator(), txtLines).getBytes(StandardCharsets.UTF_8);
            Files.write(outputPath, finalBytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            Files.write(backupPath, finalBytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            log.info("Reporte generado exitosamente en: {}", outputPath);
            log.info("Backup creado: {}", backupPath.getFileName());

        } catch (Exception e) {
            log.error("Error en el procesamiento dinámico: {}", e.getMessage(), e);
        }
    }

    private List<ColumnConfig> loadColumns(Path path) throws IOException {
        if (!Files.exists(path)) {
            log.error("Archivo de configuración de columnas no existe: {}", path);
            return Collections.emptyList();
        }

        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        return lines.stream()
                .filter(line -> !line.trim().isEmpty())
                .map(line -> {
                    String[] parts = line.split(",");
                    if (parts.length >= 3) {
                        return new ColumnConfig(parts[0].trim(),
                                Integer.parseInt(parts[1].trim()),
                                Integer.parseInt(parts[2].trim()));
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private List<String> transformContent(Path inputPath, List<ColumnConfig> columns) throws IOException {
        Charset win1252 = Charset.forName(ReportConstants.CHARSET_WINDOWS);
        List<String> allLines = Files.readAllLines(inputPath, win1252);

        if (allLines.isEmpty()) {
            return Collections.emptyList();
        }

        // Mapear cabeceras a índices
        Map<String, Integer> indexMap = mapHeaders(allLines.get(0));

        List<String> result = new ArrayList<>();

        // Generar Cabecera TXT
        StringBuilder headerSb = new StringBuilder();
        for (ColumnConfig col : columns) {
            headerSb.append(padRight(col.getName(), col.getLength()));
        }
        result.add(headerSb.toString());

        // Procesar Datos
        for (int i = 1; i < allLines.size(); i++) {
            result.add(formatLine(allLines.get(i), indexMap, columns));
        }

        return result;
    }

    private Map<String, Integer> mapHeaders(String headerLine) {
        Map<String, Integer> map = new HashMap<>();
        List<String> headers = parseCsvLine(headerLine);
        for (int i = 0; i < headers.size(); i++) {
            map.put(headers.get(i).trim().toLowerCase(), i);
        }
        return map;
    }

    private String formatLine(String csvLine, Map<String, Integer> indexMap, List<ColumnConfig> columns) {
        List<String> fields = parseCsvLine(csvLine);
        StringBuilder sb = new StringBuilder();

        for (ColumnConfig col : columns) {
            Integer index = indexMap.get(col.getName().toLowerCase());
            String value = (index != null && index < fields.size()) ? fields.get(index).trim() : "";

            // Limpieza robusta de comillas
            value = value.replaceAll("^\"|\"$", "").trim();

            sb.append(padRight(value, col.getLength()));
        }
        return sb.toString();
    }

    /**
     * Parser manual de CSV para manejar comas dentro de comillas de forma robusta.
     */
    private List<String> parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        if (line == null || line.isEmpty())
            return result;

        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '\"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(currentField.toString());
                currentField.setLength(0);
            } else {
                currentField.append(c);
            }
        }
        result.add(currentField.toString());

        return result;
    }

    private String padRight(String text, int length) {
        String safeText = (text == null) ? "" : text;
        return String.format("%-" + length + "." + length + "s", safeText);
    }
}
