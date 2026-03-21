const state = {
  reservations: [],
  mode: "active",
  loading: false,
  pastRange: {
    from: "",
    to: "",
  },
};

export function getState() {
  return state;
}

export function setReservations(reservations) {
  state.reservations = Array.isArray(reservations) ? reservations : [];
}

export function setMode(mode) {
  state.mode = mode;
}

export function setLoading(isLoading) {
  state.loading = Boolean(isLoading);
}

export function setPastRange(from, to) {
  state.pastRange.from = from;
  state.pastRange.to = to;
}
