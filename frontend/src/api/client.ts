import type {
  Appointment,
  AuditLog,
  AuthTokens,
  Clinic,
  ClinicDashboard,
  ClinicService,
  Exercise,
  ExercisePlan,
  Patient,
  PatientPackage,
  ProblemDetail,
  Professional,
  Room,
  SessionNote,
  Timeline,
  TreatmentEpisode,
  TreatmentPackage,
  UserProfile,
} from "./types";

const ACCESS_TOKEN_KEY = "cliniccore.accessToken";
const REFRESH_TOKEN_KEY = "cliniccore.refreshToken";
const AUTH_EXPIRED_EVENT = "cliniccore:auth-expired";

let refreshPromise: Promise<boolean> | null = null;

export function getAccessToken() {
  return localStorage.getItem(ACCESS_TOKEN_KEY);
}

export function getRefreshToken() {
  return localStorage.getItem(REFRESH_TOKEN_KEY);
}

export function saveTokens(tokens: AuthTokens) {
  localStorage.setItem(ACCESS_TOKEN_KEY, tokens.accessToken);
  localStorage.setItem(REFRESH_TOKEN_KEY, tokens.refreshToken);
}

export function clearTokens() {
  localStorage.removeItem(ACCESS_TOKEN_KEY);
  localStorage.removeItem(REFRESH_TOKEN_KEY);
}

export function subscribeAuthExpired(handler: () => void) {
  window.addEventListener(AUTH_EXPIRED_EVENT, handler);
  return () => window.removeEventListener(AUTH_EXPIRED_EVENT, handler);
}

function notifyAuthExpired() {
  window.dispatchEvent(new Event(AUTH_EXPIRED_EVENT));
}

async function parseResponse<T>(response: Response): Promise<T> {
  if (response.status === 204) {
    return undefined as T;
  }

  const text = await response.text();
  let payload: unknown;
  try {
    payload = text ? JSON.parse(text) : undefined;
  } catch {
    payload = undefined;
  }

  if (!response.ok) {
    const problem = payload as ProblemDetail | undefined;
    throw new Error(problem?.detail || problem?.title || `Request failed with ${response.status}`);
  }

  return payload as T;
}

function buildRequestInit(init: RequestInit = {}) {
  const headers = new Headers(init.headers);
  const token = getAccessToken();

  if (init.body && !headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json");
  }
  if (token) {
    headers.set("Authorization", `Bearer ${token}`);
  }

  return { ...init, headers };
}

async function refreshAccessToken() {
  const refreshToken = getRefreshToken();
  if (!refreshToken) {
    return false;
  }

  refreshPromise ??= (async () => {
    const response = await fetch("/api/auth/refresh", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: jsonBody({ refreshToken }),
    });

    if (!response.ok) {
      clearTokens();
      notifyAuthExpired();
      return false;
    }

    const tokens = await parseResponse<AuthTokens>(response);
    saveTokens(tokens);
    return true;
  })().finally(() => {
    refreshPromise = null;
  });

  return refreshPromise;
}

function shouldTryRefresh(path: string, response: Response) {
  return !path.startsWith("/api/auth/") && (response.status === 401 || response.status === 403);
}

async function request<T>(path: string, init: RequestInit = {}, retryOnUnauthorized = true): Promise<T> {
  const response = await fetch(path, buildRequestInit(init));

  if (retryOnUnauthorized && shouldTryRefresh(path, response) && (await refreshAccessToken())) {
    return request<T>(path, init, false);
  }

  if (retryOnUnauthorized && shouldTryRefresh(path, response)) {
    clearTokens();
    notifyAuthExpired();
  }

  return parseResponse<T>(response);
}

function jsonBody(value: unknown) {
  return JSON.stringify(value);
}

export const api = {
  login: (email: string, password: string) =>
    request<AuthTokens>("/api/auth/login", {
      method: "POST",
      body: jsonBody({ email, password }),
    }),
  refresh: (refreshToken: string) =>
    request<AuthTokens>("/api/auth/refresh", {
      method: "POST",
      body: jsonBody({ refreshToken }),
    }, false),
  me: () => request<UserProfile>("/api/auth/me"),
  health: () => request<{ status: string }>("/actuator/health"),

  currentClinic: () => request<Clinic>("/api/clinics/current"),
  professionals: () => request<Professional[]>("/api/professionals"),
  createProfessional: (payload: Omit<Professional, "id"> & { userId?: string | null }) =>
    request<Professional>("/api/professionals", { method: "POST", body: jsonBody(payload) }),
  rooms: () => request<Room[]>("/api/rooms"),
  createRoom: (payload: { name: string }) => request<Room>("/api/rooms", { method: "POST", body: jsonBody(payload) }),
  services: () => request<ClinicService[]>("/api/services"),
  createService: (payload: { name: string; durationMinutes: number; price?: number }) =>
    request<ClinicService>("/api/services", { method: "POST", body: jsonBody(payload) }),

  patients: (search = "") => request<Patient[]>(`/api/patients${search ? `?search=${encodeURIComponent(search)}` : ""}`),
  patient: (patientId: string) => request<Patient>(`/api/patients/${patientId}`),
  createPatient: (payload: Omit<Patient, "id" | "active">) =>
    request<Patient>("/api/patients", { method: "POST", body: jsonBody(payload) }),
  updatePatient: (patientId: string, payload: Omit<Patient, "id" | "active">) =>
    request<Patient>(`/api/patients/${patientId}`, { method: "PATCH", body: jsonBody(payload) }),
  deactivatePatient: (patientId: string) => request<void>(`/api/patients/${patientId}`, { method: "DELETE" }),

  appointments: (from: string, to: string) =>
    request<Appointment[]>(`/api/appointments?from=${encodeURIComponent(from)}&to=${encodeURIComponent(to)}`),
  createAppointment: (payload: Omit<Appointment, "id" | "status" | "cancellationReason">) =>
    request<Appointment>("/api/appointments", { method: "POST", body: jsonBody(payload) }),
  rescheduleAppointment: (appointmentId: string, payload: Pick<Appointment, "professionalId" | "roomId" | "startAt" | "endAt">) =>
    request<Appointment>(`/api/appointments/${appointmentId}/reschedule`, { method: "PATCH", body: jsonBody(payload) }),
  cancelAppointment: (appointmentId: string, reason: string) =>
    request<Appointment>(`/api/appointments/${appointmentId}/cancel`, { method: "PATCH", body: jsonBody({ reason }) }),
  completeAppointment: (appointmentId: string) =>
    request<Appointment>(`/api/appointments/${appointmentId}/complete`, { method: "PATCH" }),
  markNoShow: (appointmentId: string) =>
    request<Appointment>(`/api/appointments/${appointmentId}/no-show`, { method: "PATCH" }),

  openEpisode: (patientId: string, payload: { responsibleProfessionalId: string; title: string; startDate: string }) =>
    request<TreatmentEpisode>(`/api/patients/${patientId}/episodes`, { method: "POST", body: jsonBody(payload) }),
  closeEpisode: (episodeId: string, endDate: string) =>
    request<TreatmentEpisode>(`/api/episodes/${episodeId}/close`, { method: "PATCH", body: jsonBody({ endDate }) }),
  addSessionNote: (episodeId: string, payload: Omit<SessionNote, "id" | "episodeId">) =>
    request<SessionNote>(`/api/episodes/${episodeId}/session-notes`, { method: "POST", body: jsonBody(payload) }),
  timeline: (patientId: string) => request<Timeline>(`/api/patients/${patientId}/timeline`),

  packages: () => request<TreatmentPackage[]>("/api/packages"),
  createPackage: (payload: Omit<TreatmentPackage, "id">) =>
    request<TreatmentPackage>("/api/packages", { method: "POST", body: jsonBody(payload) }),
  assignPackage: (patientId: string, payload: { packageId: string; paidAmount?: number; paymentMethod?: string }) =>
    request<PatientPackage>(`/api/patients/${patientId}/packages`, { method: "POST", body: jsonBody(payload) }),
  patientPackages: (patientId: string) => request<PatientPackage[]>(`/api/patients/${patientId}/packages`),
  consumePackageSession: (patientPackageId: string) =>
    request<PatientPackage>(`/api/patient-packages/${patientPackageId}/consume-session`, { method: "PATCH" }),

  exercises: () => request<Exercise[]>("/api/exercises"),
  createExercise: (payload: Omit<Exercise, "id">) =>
    request<Exercise>("/api/exercises", { method: "POST", body: jsonBody(payload) }),
  assignExercisePlan: (
    patientId: string,
    payload: {
      professionalId: string;
      title: string;
      notes?: string;
      startsOn: string;
      items: Array<{ exerciseId: string; series?: number; repetitions?: number; frequency?: string; notes?: string }>;
    },
  ) => request<ExercisePlan>(`/api/patients/${patientId}/exercise-plans`, { method: "POST", body: jsonBody(payload) }),
  exercisePlans: (patientId: string) => request<ExercisePlan[]>(`/api/patients/${patientId}/exercise-plans`),

  dashboard: () => request<ClinicDashboard>("/api/dashboard/clinic"),
  auditLogs: () => request<AuditLog[]>("/api/audit-logs"),
};
