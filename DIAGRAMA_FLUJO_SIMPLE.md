# 📊 Diagrama de Flujo Global - Sistema CSV a TXT

## Flujo Principal de Ejecución

```
┌──────────────────────────────────────────────────────────────────────┐
│                        INICIO DEL PROGRAMA                           │
│                                                                       │
│                          Main.java                                   │
│                    (Punto de entrada)                                │
└────────────────────────┬─────────────────────────────────────────────┘
                         │
                         │ new ReportProcessorShell()
                         │
                         ▼
┌──────────────────────────────────────────────────────────────────────┐
│                   ReportProcessorShell.execute()                     │
│                 (Procesador dinámico principal)                      │
│                                                                       │
│  Invoca ──────────────────┬──────────────────────┬──────────────────┐
│                           │                      │                  │
│                           ▼                      ▼                  ▼
│                    ConfigLoader         ReportConstants     logback.xml
│                  (Parámetros JVM)      (Constantes globales) (Logging)
│                                                                       │
│  • getPathInput()          • PATH_INPUT                 Logger        │
│  • getPathOutput()         • PATH_OUTPUT              Configuración   │
│  • getPathLogs()           • PATH_LOGS                   Niveles      │
│  • getColumnsFilePath()    • FILE_NAME_*                             │
│  • getJarName()            • CHARSET_WINDOWS                         │
│                            • DATE_PATTERN                           │
│                                                                       │
└──────────────────────────────────────────────────────────────────────┘
                         │
                         │ Cargar columnas
                         ▼
┌──────────────────────────────────────────────────────────────────────┐
│              loadColumns(columnsPath)                                │
│                                                                       │
│  Lee: columns_reporte-csv-to-txt.txt                                │
│                                                                       │
│  Parsea cada línea:  "nombre,posición,largo"                        │
│                                                                       │
│  Retorna: List<ColumnConfig>                                        │
└────────────────────────┬─────────────────────────────────────────────┘
                         │
                         │ Validar y leer CSV
                         ▼
┌──────────────────────────────────────────────────────────────────────┐
│          transformContent(inputPath, columns)                        │
│                                                                       │
│  1. Leer archivo CSV (Windows-1252)                                  │
│  2. Mapear headers CSV a índices                                     │
│  3. Generar cabecera TXT (ancho fijo)                                │
│  4. Procesar cada línea:                                             │
│     ├─ Parsear CSV robustamente                                      │
│     ├─ Extraer valores por índice                                    │
│     ├─ Limpiar comillas                                              │
│     └─ Alinear a izquierda (padRight)                                │
│                                                                       │
│  Retorna: List<String> txtLines                                     │
└────────────────────────┬─────────────────────────────────────────────┘
                         │
                         │ Escribir archivos
                         ▼
┌──────────────────────────────────────────────────────────────────────┐
│            Escritura de Archivos (UTF-8)                             │
│                                                                       │
│  ├─► reporte_general.txt (Salida principal)                         │
│  │                                                                    │
│  └─► reporte_general_[HOY].txt (Backup)                             │
└────────────────────────┬─────────────────────────────────────────────┘
                         │
                         ▼
┌──────────────────────────────────────────────────────────────────────┐
│                      FIN DEL PROGRAMA                                │
│                    (Éxito o Error)                                   │
└──────────────────────────────────────────────────────────────────────┘
```

---

## Interacción de Clases

```
┌───────────────┐
│   Main.java   │
└───────┬───────┘
        │ new
        ▼
┌──────────────────────────┐
│ ReportProcessorShell     │
│     (Orquestador)        │
└───────┬──────────────────┘
        │
        ├─────────────────────────┬──────────────────────┬──────────────┐
        │                         │                      │              │
        ▼                         ▼                      ▼              ▼
    ┌─────────────┐      ┌─────────────────┐   ┌──────────────┐   ┌──────────┐
    │ ConfigLoader│      │ ReportConstants │   │ ColumnConfig │   │logback   │
    ├─────────────┤      ├─────────────────┤   ├──────────────┤   │.xml      │
    │ • getPath*()│      │ • PATH_*        │   │ • name       │   ├──────────┤
    │ • getJarName│      │ • FILE_NAME_*   │   │ • position   │   │Logging   │
    └─────────────┘      │ • CHARSET_*     │   │ • length     │   └──────────┘
                         │ • DATE_PATTERN  │   └──────────────┘
                         └─────────────────┘
```

---

## Configuración y Constantes

```
ConfigLoader (Propiedades del JVM)
├─ jar.name = "reporte-csv-to-txt"
├─ path.base = "C:/reportes"
├─ path.input = "C:/reportes/csv"
├─ path.output = "C:/reportes/txt"
└─ path.logs = "C:/reportes/log"
         │
         ▼
ReportConstants (Constantes del Sistema)
├─ PATH_INPUT = "C:/reportes/csv"
├─ PATH_OUTPUT = "C:/reportes/txt"
├─ PATH_LOGS = "C:/reportes/txt/log"
├─ FILE_NAME_CSV_BASE = "reporte_general"
├─ FILE_NAME_TXT_FINAL = "reporte_general.txt"
├─ CHARSET_WINDOWS = "windows-1252"
├─ DATE_PATTERN = "dd-MM-yyyy"
├─ EXT_CSV = ".csv"
└─ EXT_TXT = ".txt"
         │
         ▼
Archivo de Configuración (columns_*.txt)
├─ id_producto,1,5
├─ descripcion,6,25
├─ fecha_ingreso,31,20
├─ peso,51,10
├─ altura,61,10
├─ tipo_producto,71,15
├─ material,86,15
└─ precio,101,15
         │
         ▼
ColumnConfig (Modelo de Datos)
├─ name: String (id_producto)
├─ position: int (1)
└─ length: int (5)
```

---

## Flujo de Transformación

```
Entrada CSV (Windows-1252)
     │
     ▼
┌─────────────────────────────────┐
│ ReportProcessorShell            │
│ .transformContent()             │
│                                 │
│ 1. Leer archivo                 │
│ 2. Parsear headers              │
│ 3. Mapear índices               │
│ 4. Generar cabecera TXT         │
│ 5. Procesar líneas de datos     │
│    ├─ parseCsvLine()            │
│    ├─ Extraer campos            │
│    ├─ Limpiar comillas          │
│    └─ padRight() (ancho fijo)   │
│                                 │
└─────────────────────────────────┘
     │
     ▼
Salida TXT (UTF-8, Ancho Fijo)
```

---

## Logging (logback.xml)

```
ReportProcessorShell.execute()
         │
         ├─► [INFO] Iniciando procesamiento...
         │
         ├─► Cargar columnas
         │   └─► [INFO] o [ERROR] según resultado
         │
         ├─► Leer CSV
         │   ├─► [INFO] Leyendo archivo...
         │   └─► [ERROR] si no existe
         │
         ├─► Transformar
         │   └─► [INFO] o [WARN] según resultado
         │
         ├─► Escribir archivos
         │   ├─► [INFO] Reporte generado en...
         │   └─► [INFO] Backup creado en...
         │
         └─► [ERROR] si hay excepción
```

---

## Estructura de Directorios

```
C:/reportes/
├─ csv/
│  └─ reporte_general_[AYER].csv      ◄── INPUT
├─ txt/
│  ├─ reporte_general.txt              ◄── OUTPUT
│  ├─ reporte_general_[HOY].txt        ◄── BACKUP
│  └─ log/
│     └─ (logs automáticos)
└─ columns_reporte-csv-to-txt.txt      ◄── CONFIGURACIÓN
```

---

## Resumen de Clases

| Clase | Responsabilidad |
|-------|-----------------|
| **Main** | Punto de entrada del programa |
| **ReportProcessorShell** | Orquestador principal, ejecuta todo el flujo |
| **ConfigLoader** | Carga parámetros del JVM y proporciona rutas |
| **ReportConstants** | Define constantes globales del sistema |
| **ColumnConfig** | Modelo que representa la estructura de una columna |
| **logback.xml** | Configuración de logging del sistema |

---

**Versión:** 1.0  
**Fecha:** 25/02/2025

