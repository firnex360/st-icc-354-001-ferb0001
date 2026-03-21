const BASE_URL = "https://msbgyafru3.execute-api.us-east-1.amazonaws.com/reserves";

async function parseJsonResponse(response) {
  const text = await response.text();
  if (!text) {
    return null;
  }

  try {
    return JSON.parse(text);
  } catch {
    return { message: "Respuesta inválida del servidor." };
  }
}

export async function fetchReservations({ startDate = "", endDate = "" } = {}) {
  const url = new URL(BASE_URL);

  if (startDate && endDate) {
    url.searchParams.set("startDate", startDate);
    url.searchParams.set("endDate", endDate);
  }

  const response = await fetch(url.toString(), {
    method: "GET",
    headers: {
      Accept: "application/json",
    },
  });

  const data = await parseJsonResponse(response);
  if (!response.ok) {
    throw new Error(data?.message || "No se pudo obtener la lista de reservas.");
  }

  return Array.isArray(data) ? data : [];
}

export async function createReservation(payload) {
  const response = await fetch(BASE_URL, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Accept: "application/json",
    },
    body: JSON.stringify(payload),
  });

  const data = await parseJsonResponse(response);
  if (!response.ok) {
    throw new Error(data?.message || "No se pudo crear la reserva.");
  }

  return data;
}
