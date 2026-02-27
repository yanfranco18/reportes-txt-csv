#!/bin/bash

# =================================================================
# SCRIPT DE EJECUCIÓN DINÁMICA - GENERADOR DE REPORTES
# =================================================================

# 1. Configuración del JAR
# Cambia este nombre si el archivo JAR tiene un nombre distinto
JAR_NAME="reporte-csv-to-txt-1.0-SNAPSHOT"

# 2. Configuración de Directorios
PATH_BASE="C:/reportes"
PATH_INPUT="C:/reportes/csv"
PATH_OUTPUT="C:/reportes/txt"
PATH_LOGS="C:/reportes/log"

echo "---------------------------------------------------------"
echo " Ejecutando Generador de Reportes: $JAR_NAME"
echo "---------------------------------------------------------"

# 3. Validación de archivos críticos
if [ ! -f "$PATH_BASE/$JAR_NAME.jar" ]; then
    echo "ERROR: No se encuentra el archivo JAR en $PATH_BASE/$JAR_NAME.jar"
    exit 1
fi

if [ ! -f "$PATH_BASE/columns_$JAR_NAME.txt" ]; then
    echo "ERROR: No se encuentra el archivo de columnas columns_$JAR_NAME.txt"
    exit 1
fi

# 4. Ejecución de la Aplicación
java -Djar.name="$JAR_NAME" \
     -Dpath.base="$PATH_BASE" \
     -Dpath.input="$PATH_INPUT" \
     -Dpath.output="$PATH_OUTPUT" \
     -Dpath.logs="$PATH_LOGS" \
     -jar "$PATH_BASE/$JAR_NAME.jar"

echo "---------------------------------------------------------"
echo " Proceso finalizado localmente."
echo "---------------------------------------------------------"
