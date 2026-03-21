import { parseDateOnlyInput, parseReservationDate } from "./date-utils.js";

export function validateReservationPayload(payload) {
  if (!payload.id?.trim()) {
    return "El campo ID es obligatorio.";
  }

  if (!payload.nombre?.trim()) {
    return "El campo Nombre es obligatorio.";
  }

  if (!payload.carrera?.trim()) {
    return "El campo Carrera es obligatorio.";
  }

  if (!payload.laboratorio?.trim()) {
    return "Debe seleccionar un Laboratorio.";
  }

  if (!payload.fechaReserva?.trim()) {
    return "El campo Fecha Reserva es obligatorio.";
  }

  const parsedDate = parseReservationDate(payload.fechaReserva);
  if (!parsedDate) {
    return "Fecha Reserva inválida. Use el formato dd/MM/yyyy HH:mm:ss.";
  }

  const minutes = parsedDate.getMinutes();
  const seconds = parsedDate.getSeconds();
  if (minutes !== 0 || seconds !== 0) {
    return "La reserva debe ser en hora exacta (mm:ss = 00:00).";
  }

  const hour = parsedDate.getHours();
  if (hour < 8 || hour > 21) {
    return "Horario permitido: 8:00 AM a 9:00 PM.";
  }

  return "";
}

export function validatePastRange(from, to) {
  if (!from || !to) {
    return "Debe seleccionar Desde y Hasta para filtrar registros pasados.";
  }

  const fromDate = parseDateOnlyInput(from);
  const toDate = parseDateOnlyInput(to);

  if (!fromDate || !toDate) {
    return "Rango inválido. Use formato dd/MM/yyyy.";
  }

  if (fromDate > toDate) {
    return "La fecha Desde no puede ser mayor que la fecha Hasta.";
  }

  return "";
}
