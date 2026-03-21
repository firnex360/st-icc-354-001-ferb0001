# Reservas de Laboratorio - EICT

SPA en Vanilla JS para gestionar reservas de laboratorio con protocolos de distanciamiento social.

## Caracteristicas

- Vista principal con reservas activas.
- Modal de alta de reserva con validaciones de negocio.
- Vista de registros pasados filtrada por rango de fechas.
- Integracion con API Gateway + Lambda + DynamoDB.
- Lista para despliegue estatico en GitHub Pages.

## Estructura

- index.html
- styles.css
- js/main.js
- js/api.js
- js/state.js
- js/date-utils.js
- js/validation.js
- js/ui.js

## API

Endpoint base:

https://msbgyafru3.execute-api.us-east-1.amazonaws.com/reserves

### GET

- Sin query params: retorna todas las reservas.
- Con query params: `startDate` y `endDate` en ISO 8601 para filtrar rango en backend.

### POST

Payload JSON:

{
  "id": "string",
  "nombre": "string",
  "carrera": "string",
  "laboratorio": "Lab 1|Lab 2|Lab 3|Lab 4",
  "fechaReserva": "dd/MM/yyyy HH:mm:ss"
}

Validaciones aplicadas en frontend y backend:

- Minutos y segundos deben ser 00.
- Horario permitido: desde 08:00:00 hasta 21:00:00.

## Ejecucion local

Puedes abrir el proyecto con cualquier servidor estatico. Por ejemplo con VS Code Live Server o con una utilidad similar.

## Despliegue en GitHub Pages

1. Subir el contenido del repositorio a GitHub.
2. En Settings > Pages, seleccionar la rama principal (main) y la carpeta raiz (/).
3. Guardar configuracion y esperar la publicacion.

## Notas

- El backend devuelve headers CORS para `OPTIONS, GET, POST`.
- Si el backend responde un error de negocio (por ejemplo laboratorio lleno), la app muestra el mensaje directamente.
