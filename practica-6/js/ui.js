import { formatReservationDate, getReservationDate } from "./date-utils.js";

function safeText(value) {
  if (value === null || value === undefined) {
    return "";
  }
  return String(value);
}

export function getUIElements() {
  return {
    body: document.getElementById("reservationsBody"),
    statusMessage: document.getElementById("statusMessage"),
    viewBadge: document.getElementById("viewBadge"),
    pastFilterPanel: document.getElementById("pastFilterPanel"),
    pastRecordsToggle: document.getElementById("pastRecordsToggle"),
    pastFromDate: document.getElementById("pastFromDate"),
    pastToDate: document.getElementById("pastToDate"),
    applyPastFilterBtn: document.getElementById("applyPastFilterBtn"),
    showActiveBtn: document.getElementById("showActiveBtn"),
    addReservationBtn: document.getElementById("addReservationBtn"),
    modal: document.getElementById("reservationModal"),
    form: document.getElementById("reservationForm"),
    formError: document.getElementById("formError"),
    cancelModalBtn: document.getElementById("cancelModalBtn"),
    inputId: document.getElementById("inputId"),
    inputNombre: document.getElementById("inputNombre"),
    inputCarrera: document.getElementById("inputCarrera"),
    inputLaboratorio: document.getElementById("inputLaboratorio"),
    inputFechaReserva: document.getElementById("inputFechaReserva"),
  };
}

export function renderReservations(elements, reservations) {
  if (!elements.body) {
    return;
  }

  if (!Array.isArray(reservations) || reservations.length === 0) {
    elements.body.innerHTML =
      '<tr><td colspan="4">No hay reservas para mostrar.</td></tr>';
    return;
  }

  const rows = reservations
    .map((reservation) => {
      const date = getReservationDate(reservation);
      const displayDate = reservation.fechaReserva || formatReservationDate(date);

      return `
        <tr>
          <td>${safeText(reservation.id)}</td>
          <td>${safeText(reservation.nombre)}</td>
          <td>${safeText(reservation.laboratorio)}</td>
          <td>${safeText(displayDate)}</td>
        </tr>
      `;
    })
    .join("");

  elements.body.innerHTML = rows;
}

export function setStatusMessage(elements, message, type = "") {
  if (!elements.statusMessage) {
    return;
  }

  elements.statusMessage.textContent = message || "";
  elements.statusMessage.classList.remove("error", "success");

  if (type === "error") {
    elements.statusMessage.classList.add("error");
  }

  if (type === "success") {
    elements.statusMessage.classList.add("success");
  }
}

export function setViewBadge(elements, mode) {
  if (!elements.viewBadge) {
    return;
  }

  elements.viewBadge.textContent =
    mode === "past" ? "Mostrando: Registros Pasados" : "Mostrando: Activas";
}

export function openModal(elements) {
  if (!elements.modal) {
    return;
  }

  elements.modal.classList.remove("hidden");
  elements.inputId?.focus();
}

export function closeModal(elements) {
  if (!elements.modal) {
    return;
  }
  elements.modal.classList.add("hidden");
}

export function clearForm(elements) {
  elements.form?.reset();
  clearFormError(elements);
}

export function getReservationFormData(elements) {
  return {
    id: elements.inputId?.value.trim() || "",
    nombre: elements.inputNombre?.value.trim() || "",
    carrera: elements.inputCarrera?.value.trim() || "",
    laboratorio: elements.inputLaboratorio?.value || "",
    fechaReserva: elements.inputFechaReserva?.value.trim() || "",
  };
}

export function setFormError(elements, message) {
  if (!elements.formError) {
    return;
  }
  elements.formError.textContent = message || "";
}

export function clearFormError(elements) {
  setFormError(elements, "");
}

export function togglePastPanel(elements, shouldShow) {
  if (!elements.pastFilterPanel) {
    return;
  }

  elements.pastFilterPanel.classList.toggle("hidden", !shouldShow);
}

export function setButtonsDisabled(elements, disabled) {
  const targets = [
    elements.addReservationBtn,
    elements.applyPastFilterBtn,
    elements.showActiveBtn,
    elements.cancelModalBtn,
    elements.pastRecordsToggle,
  ];

  targets.forEach((button) => {
    if (button) {
      button.disabled = disabled;
    }
  });
}
