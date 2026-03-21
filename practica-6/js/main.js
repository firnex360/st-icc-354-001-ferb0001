import { fetchReservations, createReservation } from "./api.js";
import {
  fromIsoDateInput,
  getReservationDate,
  isDateWithinInputRange,
  isReservationActive,
  toIsoRangeFromDateInput,
} from "./date-utils.js";
import { getState, setLoading, setMode, setPastRange, setReservations } from "./state.js";
import { validatePastRange, validateReservationPayload } from "./validation.js";
import {
  clearForm,
  clearFormError,
  closeModal,
  getReservationFormData,
  getUIElements,
  openModal,
  renderReservations,
  setButtonsDisabled,
  setFormError,
  setStatusMessage,
  setViewBadge,
  togglePastPanel,
} from "./ui.js";

const elements = getUIElements();

function sortByDateAscending(list) {
  return [...list].sort((a, b) => {
    const aDate = getReservationDate(a);
    const bDate = getReservationDate(b);

    if (!aDate && !bDate) {
      return 0;
    }

    if (!aDate) {
      return 1;
    }

    if (!bDate) {
      return -1;
    }

    return aDate.getTime() - bDate.getTime();
  });
}

function displayActiveReservations() {
  const { reservations } = getState();
  const activeReservations = sortByDateAscending(
    reservations.filter((reservation) => isReservationActive(reservation))
  );

  renderReservations(elements, activeReservations);
  setViewBadge(elements, "active");
  setStatusMessage(elements, `Reservas activas encontradas: ${activeReservations.length}`);
}

function displayPastReservations(reservations, fromDateInput, toDateInput) {
  const pastReservations = sortByDateAscending(
    reservations.filter((reservation) => {
      const reservationDate = getReservationDate(reservation);

      if (!reservationDate) {
        return false;
      }

      if (isReservationActive(reservation)) {
        return false;
      }

      return isDateWithinInputRange(reservationDate, fromDateInput, toDateInput);
    })
  );

  renderReservations(elements, pastReservations);
  setViewBadge(elements, "past");
  setStatusMessage(elements, `Registros pasados encontrados: ${pastReservations.length}`);
}

async function refreshActiveReservations() {
  try {
    setLoading(true);
    setButtonsDisabled(elements, true);
    setStatusMessage(elements, "Cargando reservas activas...");

    const reservations = await fetchReservations();
    setReservations(reservations);
    setMode("active");
    displayActiveReservations();
  } catch (error) {
    setStatusMessage(elements, error.message || "Error al cargar las reservas.", "error");
  } finally {
    setLoading(false);
    setButtonsDisabled(elements, false);
  }
}

async function handlePastFilter() {
  const from = elements.pastFromDate?.value || "";
  const to = elements.pastToDate?.value || "";

  const rangeError = validatePastRange(from, to);
  if (rangeError) {
    setStatusMessage(elements, rangeError, "error");
    return;
  }

  try {
    setLoading(true);
    setButtonsDisabled(elements, true);
    setStatusMessage(elements, "Cargando registros pasados...");

    const { startDate, endDate } = toIsoRangeFromDateInput(from, to);
    const reservations = await fetchReservations({ startDate, endDate });

    setReservations(reservations);
    setPastRange(from, to);
    setMode("past");
    displayPastReservations(reservations, from, to);
  } catch (error) {
    setStatusMessage(
      elements,
      error.message || "Error al cargar registros pasados.",
      "error"
    );
  } finally {
    setLoading(false);
    setButtonsDisabled(elements, false);
  }
}

async function handleCreateReservation(event) {
  event.preventDefault();

  const payload = getReservationFormData(elements);
  const validationError = validateReservationPayload(payload);

  if (validationError) {
    setFormError(elements, validationError);
    return;
  }

  try {
    clearFormError(elements);
    setButtonsDisabled(elements, true);
    setStatusMessage(elements, "Registrando reserva...");

    const result = await createReservation(payload);
    closeModal(elements);
    clearForm(elements);
    setStatusMessage(elements, result?.message || "Reserva creada exitosamente.", "success");

    await refreshActiveReservations();
  } catch (error) {
    setFormError(elements, error.message || "No se pudo crear la reserva.");
  } finally {
    setButtonsDisabled(elements, false);
  }
}

function bindDatePicker(displayInput, pickerInput) {
  if (!displayInput || !pickerInput) {
    return;
  }

  const openPicker = () => {
    if (typeof pickerInput.showPicker === "function") {
      pickerInput.showPicker();
      return;
    }
    pickerInput.focus();
  };

  displayInput.addEventListener("click", openPicker);
  displayInput.addEventListener("keydown", (event) => {
    if (event.key === "Enter" || event.key === " ") {
      event.preventDefault();
      openPicker();
    }
  });

  pickerInput.addEventListener("change", () => {
    displayInput.value = fromIsoDateInput(pickerInput.value);
  });
}

function bindEvents() {
  bindDatePicker(elements.pastFromDate, elements.pastFromDatePicker);
  bindDatePicker(elements.pastToDate, elements.pastToDatePicker);
  bindDatePicker(elements.inputFechaReservaDate, elements.inputFechaReservaDatePicker);

  elements.addReservationBtn?.addEventListener("click", () => {
    clearForm(elements);
    openModal(elements);
  });

  elements.cancelModalBtn?.addEventListener("click", () => {
    closeModal(elements);
    clearForm(elements);
  });

  elements.modal?.addEventListener("click", (event) => {
    if (event.target === elements.modal) {
      closeModal(elements);
      clearForm(elements);
    }
  });

  document.addEventListener("keydown", (event) => {
    if (event.key === "Escape") {
      closeModal(elements);
      clearForm(elements);
    }
  });

  elements.form?.addEventListener("submit", handleCreateReservation);

  elements.pastRecordsToggle?.addEventListener("click", () => {
    const shouldShow = elements.pastFilterPanel?.classList.contains("hidden");
    togglePastPanel(elements, Boolean(shouldShow));
  });

  elements.applyPastFilterBtn?.addEventListener("click", handlePastFilter);

  elements.showActiveBtn?.addEventListener("click", async () => {
    togglePastPanel(elements, false);
    elements.pastFromDate.value = "";
    elements.pastFromDatePicker.value = "";
    elements.pastToDate.value = "";
    elements.pastToDatePicker.value = "";
    setPastRange("", "");
    await refreshActiveReservations();
  });
}

async function initializeApp() {
  bindEvents();
  await refreshActiveReservations();
}

initializeApp();
