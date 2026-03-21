const RESERVATION_REGEX = /^(\d{2})\/(\d{2})\/(\d{4})\s(\d{2}):(\d{2}):(\d{2})$/;
const DATE_ONLY_REGEX = /^(\d{2})\/(\d{2})\/(\d{4})$/;

function pad2(value) {
  return String(value).padStart(2, "0");
}

export function parseReservationDate(value) {
  if (!value || typeof value !== "string") {
    return null;
  }

  const trimmed = value.trim();
  const match = trimmed.match(RESERVATION_REGEX);
  if (!match) {
    return null;
  }

  const day = Number(match[1]);
  const month = Number(match[2]);
  const year = Number(match[3]);
  const hour = Number(match[4]);
  const minute = Number(match[5]);
  const second = Number(match[6]);

  const parsed = new Date(year, month - 1, day, hour, minute, second);

  const isExactDate =
    parsed.getFullYear() === year &&
    parsed.getMonth() === month - 1 &&
    parsed.getDate() === day &&
    parsed.getHours() === hour &&
    parsed.getMinutes() === minute &&
    parsed.getSeconds() === second;

  return isExactDate ? parsed : null;
}

export function formatReservationDate(date) {
  if (!(date instanceof Date) || Number.isNaN(date.getTime())) {
    return "";
  }

  const day = pad2(date.getDate());
  const month = pad2(date.getMonth() + 1);
  const year = date.getFullYear();
  const hour = pad2(date.getHours());
  const minute = pad2(date.getMinutes());
  const second = pad2(date.getSeconds());

  return `${day}/${month}/${year} ${hour}:${minute}:${second}`;
}

export function getReservationDate(reservation) {
  const fromFormatted = parseReservationDate(reservation?.fechaReserva);
  if (fromFormatted) {
    return fromFormatted;
  }

  if (reservation?.fechaISO) {
    const fromIso = new Date(reservation.fechaISO);
    if (!Number.isNaN(fromIso.getTime())) {
      return fromIso;
    }
  }

  return null;
}

export function isReservationActive(reservation, now = new Date()) {
  const reservationDate = getReservationDate(reservation);
  if (!reservationDate) {
    return false;
  }
  return reservationDate.getTime() >= now.getTime();
}

export function fromIsoDateInput(isoDateInput) {
  if (!isoDateInput || typeof isoDateInput !== "string") {
    return "";
  }

  const [year, month, day] = isoDateInput.split("-");
  if (!year || !month || !day) {
    return "";
  }

  return `${day}/${month}/${year}`;
}

export function parseDateOnlyInput(value) {
  if (!value || typeof value !== "string") {
    return null;
  }

  const trimmed = value.trim();
  const match = trimmed.match(DATE_ONLY_REGEX);
  if (!match) {
    return null;
  }

  const day = Number(match[1]);
  const month = Number(match[2]);
  const year = Number(match[3]);
  const parsed = new Date(year, month - 1, day, 0, 0, 0);

  const isExactDate =
    parsed.getFullYear() === year &&
    parsed.getMonth() === month - 1 &&
    parsed.getDate() === day;

  return isExactDate ? parsed : null;
}

export function toIsoRangeFromDateInput(fromDateInput, toDateInput) {
  if (!fromDateInput || !toDateInput) {
    return { startDate: "", endDate: "" };
  }

  const fromDate = parseDateOnlyInput(fromDateInput);
  const toDate = parseDateOnlyInput(toDateInput);

  if (!fromDate || !toDate) {
    return { startDate: "", endDate: "" };
  }

  const fromDateStart = new Date(
    fromDate.getFullYear(),
    fromDate.getMonth(),
    fromDate.getDate(),
    0,
    0,
    0
  );
  const toDateEnd = new Date(
    toDate.getFullYear(),
    toDate.getMonth(),
    toDate.getDate(),
    23,
    59,
    59
  );

  return {
    startDate: fromDateStart.toISOString(),
    endDate: toDateEnd.toISOString(),
  };
}

export function isDateWithinInputRange(reservationDate, fromDateInput, toDateInput) {
  if (!(reservationDate instanceof Date) || Number.isNaN(reservationDate.getTime())) {
    return false;
  }

  if (!fromDateInput || !toDateInput) {
    return true;
  }

  const fromDate = parseDateOnlyInput(fromDateInput);
  const toDate = parseDateOnlyInput(toDateInput);

  if (!fromDate || !toDate) {
    return false;
  }

  const fromDateStart = new Date(
    fromDate.getFullYear(),
    fromDate.getMonth(),
    fromDate.getDate(),
    0,
    0,
    0
  );
  const toDateEnd = new Date(
    toDate.getFullYear(),
    toDate.getMonth(),
    toDate.getDate(),
    23,
    59,
    59
  );

  return reservationDate >= fromDateStart && reservationDate <= toDateEnd;
}
