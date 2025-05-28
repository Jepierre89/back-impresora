# Documentación de la API de Impresión Térmica

Esta documentación describe los endpoints disponibles en el sistema de impresión térmica.

## Endpoints

### 1. Obtener Lista de Impresoras
```http
GET /getprinters
```

Este endpoint devuelve una lista de todas las impresoras disponibles en el sistema.

**Respuesta:**
- Formato: JSON
- Contenido: Array de strings con los nombres de las impresoras
- Ejemplo:
```json
["HP LaserJet", "Epson TM-T20", "Zebra ZP 450"]
```

### 2. Imprimir Documento
```http
POST /imprimir
```

Este endpoint permite enviar comandos de impresión a una impresora térmica específica.

**Cuerpo de la Petición:**
```json
{
    "nombre_impresora": "string",
    "api_key": "string",
    "operaciones": [
        {
            "accion": "string",
            "datos": "string"
        }
    ]
}
```

**Acciones Disponibles:**
- `textalign`: Alineación del texto
  - Valores: "center", "left", "right"
- `pdf417`: Genera código PDF417
- `qr`: Genera código QR
- `cut`: Corta el papel
- `feed`: Avanza el papel
  - Valor: número de líneas a avanzar
- `bold`: Activa/desactiva negrita
  - Valores: "on", "off"
- `text`: Imprime texto

**Ejemplo de Uso:**
```json
{
    "nombre_impresora": "Epson TM-T20",
    "api_key": "tu-api-key",
    "operaciones": [
        {
            "accion": "textalign",
            "datos": "center"
        },
        {
            "accion": "text",
            "datos": "Hola Mundo"
        },
        {
            "accion": "cut",
            "datos": ""
        }
    ]
}
```

### 3. Abrir Monedero
```http
GET /abrir-monedero
```

Este endpoint envía un pulso eléctrico para abrir el monedero conectado a la impresora.

**Parámetros:**
- `printer`: Nombre de la impresora (requerido)

**Ejemplo:**
```http
GET /abrir-monedero?printer=Epson%20TM-T20
```

**Respuestas:**
- Éxito: "Monedero abierto correctamente"
- Error: Mensaje de error específico

## Notas Importantes

1. Todos los endpoints requieren que la impresora esté correctamente configurada en el sistema.
2. El endpoint de impresión soporta múltiples operaciones en una sola petición.
3. El monedero debe estar físicamente conectado a la impresora para que funcione el endpoint de apertura.
4. Se recomienda verificar la disponibilidad de la impresora antes de enviar comandos de impresión. 