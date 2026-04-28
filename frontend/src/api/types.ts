export type AuthTokens = {
  accessToken: string;
  refreshToken: string;
  expiresInSeconds: number;
  tokenType: "Bearer";
};

export type UserProfile = {
  id: string;
  clinicId: string;
  email: string;
  fullName: string;
  role: string;
};

export type Clinic = {
  id: string;
  name: string;
  legalName?: string;
  phone?: string;
  email?: string;
};

export type Professional = {
  id: string;
  firstName: string;
  lastName: string;
  email?: string;
  phone?: string;
  color?: string;
};

export type Room = {
  id: string;
  name: string;
};

export type ClinicService = {
  id: string;
  name: string;
  durationMinutes: number;
  price?: number;
};

export type Patient = {
  id: string;
  firstName: string;
  lastName: string;
  phone?: string;
  email?: string;
  birthDate?: string;
  administrativeNotes?: string;
  consentAccepted: boolean;
  active: boolean;
};

export type AppointmentStatus = "SCHEDULED" | "CANCELLED" | "COMPLETED" | "NO_SHOW";

export type Appointment = {
  id: string;
  patientId: string;
  professionalId: string;
  roomId: string;
  serviceId: string;
  startAt: string;
  endAt: string;
  status: AppointmentStatus;
  reason?: string;
  cancellationReason?: string;
};

export type TreatmentEpisode = {
  id: string;
  patientId: string;
  responsibleProfessionalId: string;
  title: string;
  startDate: string;
  endDate?: string;
  status: "OPEN" | "CLOSED";
};

export type SessionNote = {
  id: string;
  episodeId: string;
  appointmentId?: string;
  professionalId: string;
  sessionDate: string;
  painLevel?: number;
  treatedArea?: string;
  techniquesApplied?: string;
  observations?: string;
  nextRecommendation?: string;
};

export type Timeline = {
  patientId: string;
  episodes: Array<{
    episode: TreatmentEpisode;
    notes: SessionNote[];
  }>;
};

export type TreatmentPackage = {
  id: string;
  name: string;
  totalSessions: number;
  price?: number;
};

export type PatientPackage = {
  id: string;
  patientId: string;
  packageId: string;
  totalSessions: number;
  remainingSessions: number;
  paidAmount?: number;
  paymentMethod?: string;
  purchasedAt: string;
  active: boolean;
};

export type Exercise = {
  id: string;
  name: string;
  description?: string;
  videoUrl?: string;
  imageUrl?: string;
};

export type ExercisePlan = {
  id: string;
  patientId: string;
  professionalId: string;
  title: string;
  notes?: string;
  startsOn: string;
  items: Array<{
    id: string;
    exerciseId: string;
    series?: number;
    repetitions?: number;
    frequency?: string;
    notes?: string;
  }>;
};

export type ClinicDashboard = {
  appointmentsNextSevenDays: number;
  cancelledNextSevenDays: number;
  noShowsLastThirtyDays: number;
  activePatients: number;
  openEpisodes: number;
  activePackages: number;
  registeredIncome: number;
};

export type AuditLog = {
  id: string;
  clinicId?: string;
  actorUserId?: string;
  action: string;
  resourceType: string;
  resourceId?: string;
  details?: string;
  occurredAt: string;
};

export type ProblemDetail = {
  title?: string;
  detail?: string;
  status?: number;
};
