import { Component, ErrorInfo, FormEvent, ReactNode, useState } from "react";
import { NavLink, Route, Routes, useNavigate } from "react-router-dom";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  Activity,
  CalendarDays,
  ClipboardList,
  Dumbbell,
  FileClock,
  Home,
  LogOut,
  PackageCheck,
  PanelLeft,
  Plus,
  Search,
  Stethoscope,
  Users,
} from "lucide-react";
import { api, clearTokens, getAccessToken, saveTokens } from "./api/client";
import type { Appointment, Patient, Professional, Room, ClinicService } from "./api/types";
import { useCoreData } from "./hooks/useCoreData";
import { Badge, EmptyState, ErrorBlock, Field, LoadingBlock, PageHeader, Panel } from "./components/ui";

type CoreData = ReturnType<typeof useCoreData>;

const navItems = [
  { to: "/", label: "Dashboard", icon: Home },
  { to: "/clinic", label: "Clinic", icon: Stethoscope },
  { to: "/patients", label: "Patients", icon: Users },
  { to: "/agenda", label: "Agenda", icon: CalendarDays },
  { to: "/treatments", label: "Treatments", icon: ClipboardList },
  { to: "/packages", label: "Packages", icon: PackageCheck },
  { to: "/exercises", label: "Exercises", icon: Dumbbell },
  { to: "/audit", label: "Audit", icon: FileClock },
];

function today() {
  return new Date().toISOString().slice(0, 10);
}

function dateTimeLocal(daysAhead: number, hour: number, minute = 0) {
  const value = new Date();
  value.setDate(value.getDate() + daysAhead);
  value.setHours(hour, minute, 0, 0);
  const offset = value.getTimezoneOffset();
  return new Date(value.getTime() - offset * 60_000).toISOString().slice(0, 16);
}

function toInstant(value: string) {
  return new Date(value).toISOString();
}

function formatDateTime(value?: string) {
  if (!value) {
    return "-";
  }
  return new Intl.DateTimeFormat("en", {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(new Date(value));
}

function money(value?: number) {
  return new Intl.NumberFormat("en", { style: "currency", currency: "EUR" }).format(value ?? 0);
}

class AppErrorBoundary extends Component<{ children: ReactNode }, { message: string | null }> {
  state = { message: null };

  static getDerivedStateFromError(_error: Error) {
    return { message: _error.message };
  }

  componentDidCatch(error: Error, info: ErrorInfo) {
    console.error(error, info);
  }

  render() {
    if (this.state.message) {
      return (
        <main className="content">
          <Panel title="Application error">
            <ErrorBlock message={this.state.message} />
          </Panel>
        </main>
      );
    }

    return this.props.children;
  }
}

function patientName(patients: Patient[] | undefined, patientId: string) {
  const patient = patients?.find((item) => item.id === patientId);
  return patient ? `${patient.firstName} ${patient.lastName}` : "Unknown patient";
}

function professionalName(professionals: Professional[] | undefined, professionalId: string) {
  const professional = professionals?.find((item) => item.id === professionalId);
  return professional ? `${professional.firstName} ${professional.lastName}` : "Unknown professional";
}

function roomName(rooms: Room[] | undefined, roomId: string) {
  return rooms?.find((item) => item.id === roomId)?.name ?? "Unknown room";
}

function serviceName(services: ClinicService[] | undefined, serviceId: string) {
  return services?.find((item) => item.id === serviceId)?.name ?? "Unknown service";
}

function appointmentTone(status: Appointment["status"]) {
  if (status === "COMPLETED") return "good";
  if (status === "CANCELLED") return "bad";
  if (status === "NO_SHOW") return "warn";
  return "neutral";
}

function mutationError(error: unknown) {
  return error instanceof Error ? error.message : "Unexpected error";
}

function useRefresh(keys: string[]) {
  const queryClient = useQueryClient();
  return () => {
    keys.forEach((key) => {
      void queryClient.invalidateQueries({ queryKey: [key] });
    });
  };
}

export default function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(Boolean(getAccessToken()));

  if (!isAuthenticated) {
    return <LoginPage onLoggedIn={() => setIsAuthenticated(true)} />;
  }

  return (
    <AppErrorBoundary>
      <AuthenticatedApp onLogout={() => setIsAuthenticated(false)} />
    </AppErrorBoundary>
  );
}

function LoginPage({ onLoggedIn }: { onLoggedIn: () => void }) {
  const [email, setEmail] = useState("owner@cliniccore.local");
  const [password, setPassword] = useState("ChangeMe123!");
  const login = useMutation({
    mutationFn: () => api.login(email, password),
    onSuccess: (tokens) => {
      saveTokens(tokens);
      onLoggedIn();
    },
  });

  return (
    <main className="login-layout">
      <section className="login-panel">
        <div className="brand-mark">
          <Activity size={28} />
        </div>
        <span className="eyebrow">ClinicCore</span>
        <h1>Clinic operations</h1>
        <form
          className="stack-form"
          onSubmit={(event) => {
            event.preventDefault();
            login.mutate();
          }}
        >
          <Field label="Email">
            <input value={email} onChange={(event) => setEmail(event.target.value)} type="email" required />
          </Field>
          <Field label="Password">
            <input value={password} onChange={(event) => setPassword(event.target.value)} type="password" required />
          </Field>
          {login.error ? <ErrorBlock message={mutationError(login.error)} /> : null}
          <button className="primary-button" type="submit" disabled={login.isPending}>
            {login.isPending ? "Signing in..." : "Sign in"}
          </button>
        </form>
      </section>
    </main>
  );
}

function AuthenticatedApp({ onLogout }: { onLogout: () => void }) {
  const data = useCoreData(true);
  const navigate = useNavigate();

  function logout() {
    clearTokens();
    onLogout();
    navigate("/");
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="sidebar-brand">
          <Activity size={24} />
          <span>ClinicCore</span>
        </div>
        <nav className="nav-list">
          {navItems.map((item) => {
            const Icon = item.icon;
            return (
              <NavLink key={item.to} to={item.to} end={item.to === "/"}>
                <Icon size={18} />
                {item.label}
              </NavLink>
            );
          })}
        </nav>
        <button className="ghost-button sidebar-logout" onClick={logout} type="button">
          <LogOut size={16} />
          Sign out
        </button>
      </aside>

      <main className="content">
        <div className="topbar">
          <PanelLeft size={18} />
          <span>{data.clinic.data?.name ?? "Clinic workspace"}</span>
          {data.profile.data ? <Badge tone="good">{data.profile.data.role}</Badge> : null}
        </div>
        <Routes>
          <Route path="/" element={<DashboardPage data={data} />} />
          <Route path="/clinic" element={<ClinicPage data={data} />} />
          <Route path="/patients" element={<PatientsPage />} />
          <Route path="/agenda" element={<AgendaPage data={data} />} />
          <Route path="/treatments" element={<TreatmentsPage data={data} />} />
          <Route path="/packages" element={<PackagesPage data={data} />} />
          <Route path="/exercises" element={<ExercisesPage data={data} />} />
          <Route path="/audit" element={<AuditPage data={data} />} />
        </Routes>
      </main>
    </div>
  );
}

function DashboardPage({ data }: { data: CoreData }) {
  const dashboard = data.dashboard.data;
  const appointments = data.appointments.data ?? [];

  return (
    <>
      <PageHeader title="Dashboard" eyebrow="Today" />
      {data.dashboard.isLoading ? <LoadingBlock /> : null}
      {data.dashboard.error ? <ErrorBlock message={mutationError(data.dashboard.error)} /> : null}
      <section className="metric-grid">
        <Metric label="Next 7 days" value={dashboard?.appointmentsNextSevenDays ?? 0} />
        <Metric label="Active patients" value={dashboard?.activePatients ?? 0} />
        <Metric label="Open episodes" value={dashboard?.openEpisodes ?? 0} />
        <Metric label="Active packages" value={dashboard?.activePackages ?? 0} />
        <Metric label="Income" value={money(dashboard?.registeredIncome)} />
        <Metric label="No-shows 30d" value={dashboard?.noShowsLastThirtyDays ?? 0} />
      </section>
      <div className="two-column">
        <Panel title="Upcoming appointments">
          <AppointmentList appointments={appointments.slice(0, 6)} data={data} compact />
        </Panel>
        <Panel title="Clinic workload">
          <div className="list">
            <div className="list-row">
              <span>Cancelled next 7 days</span>
              <Badge tone={dashboard?.cancelledNextSevenDays ? "warn" : "good"}>
                {dashboard?.cancelledNextSevenDays ?? 0}
              </Badge>
            </div>
            <div className="list-row">
              <span>Open treatment episodes</span>
              <Badge>{dashboard?.openEpisodes ?? 0}</Badge>
            </div>
            <div className="list-row">
              <span>Active patient packages</span>
              <Badge>{dashboard?.activePackages ?? 0}</Badge>
            </div>
          </div>
        </Panel>
      </div>
    </>
  );
}

function Metric({ label, value }: { label: string; value: string | number }) {
  return (
    <div className="metric">
      <span>{label}</span>
      <strong>{value}</strong>
    </div>
  );
}

function ClinicPage({ data }: { data: CoreData }) {
  const refreshClinic = useRefresh(["professionals", "rooms", "services"]);
  const createProfessional = useMutation({ mutationFn: api.createProfessional, onSuccess: refreshClinic });
  const createRoom = useMutation({ mutationFn: api.createRoom, onSuccess: refreshClinic });
  const createService = useMutation({ mutationFn: api.createService, onSuccess: refreshClinic });

  function submitProfessional(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    createProfessional.mutate({
      userId: null,
      firstName: String(form.get("firstName")),
      lastName: String(form.get("lastName")),
      email: String(form.get("email")),
      phone: String(form.get("phone")),
      color: String(form.get("color")),
    });
    event.currentTarget.reset();
  }

  function submitRoom(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    createRoom.mutate({ name: String(form.get("name")) });
    event.currentTarget.reset();
  }

  function submitService(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    createService.mutate({
      name: String(form.get("name")),
      durationMinutes: Number(form.get("durationMinutes")),
      price: Number(form.get("price")),
    });
    event.currentTarget.reset();
  }

  return (
    <>
      <PageHeader title="Clinic setup" eyebrow={data.clinic.data?.name ?? "Workspace"} />
      <div className="three-column">
        <Panel title="Professionals">
          <form className="compact-form" onSubmit={submitProfessional}>
            <input name="firstName" placeholder="First name" required />
            <input name="lastName" placeholder="Last name" required />
            <input name="email" placeholder="Email" type="email" />
            <input name="phone" placeholder="Phone" />
            <input name="color" defaultValue="#0f766e" type="color" aria-label="Color" />
            <button type="submit">
              <Plus size={16} />
              Add
            </button>
          </form>
          <EntityList items={data.professionals.data ?? []} render={(item) => `${item.firstName} ${item.lastName}`} />
          {createProfessional.error ? <ErrorBlock message={mutationError(createProfessional.error)} /> : null}
        </Panel>
        <Panel title="Rooms">
          <form className="inline-form" onSubmit={submitRoom}>
            <input name="name" placeholder="Room name" required />
            <button type="submit">
              <Plus size={16} />
              Add
            </button>
          </form>
          <EntityList items={data.rooms.data ?? []} render={(item) => item.name} />
          {createRoom.error ? <ErrorBlock message={mutationError(createRoom.error)} /> : null}
        </Panel>
        <Panel title="Services">
          <form className="compact-form" onSubmit={submitService}>
            <input name="name" placeholder="Service name" required />
            <input name="durationMinutes" defaultValue={45} min={1} type="number" required />
            <input name="price" defaultValue={45} min={0} step="0.01" type="number" />
            <button type="submit">
              <Plus size={16} />
              Add
            </button>
          </form>
          <div className="list">
            {(data.services.data ?? []).map((item) => (
              <div className="list-row" key={item.id}>
                <span>{item.name}</span>
                <small>
                  {item.durationMinutes} min · {money(item.price)}
                </small>
              </div>
            ))}
          </div>
          {createService.error ? <ErrorBlock message={mutationError(createService.error)} /> : null}
        </Panel>
      </div>
    </>
  );
}

function EntityList<T extends { id: string }>({ items, render }: { items: T[]; render: (item: T) => string }) {
  return (
    <div className="list">
      {items.length === 0 ? <EmptyState title="No records yet" /> : null}
      {items.map((item) => (
        <div className="list-row" key={item.id}>
          <span>{render(item)}</span>
        </div>
      ))}
    </div>
  );
}

function PatientsPage() {
  const [search, setSearch] = useState("");
  const refreshPatients = useRefresh(["patients", "dashboard"]);
  const searchedPatients = useQuery({
    queryKey: ["patients", search],
    queryFn: () => api.patients(search),
  });
  const createPatient = useMutation({ mutationFn: api.createPatient, onSuccess: refreshPatients });

  function submitPatient(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    createPatient.mutate({
      firstName: String(form.get("firstName")),
      lastName: String(form.get("lastName")),
      phone: String(form.get("phone")),
      email: String(form.get("email")),
      birthDate: String(form.get("birthDate")),
      administrativeNotes: String(form.get("administrativeNotes")),
      consentAccepted: form.get("consentAccepted") === "on",
    });
    event.currentTarget.reset();
  }

  return (
    <>
      <PageHeader title="Patients" eyebrow="Administrative record" />
      <div className="two-column">
        <Panel title="Create patient">
          <form className="stack-form" onSubmit={submitPatient}>
            <div className="form-grid">
              <Field label="First name">
                <input name="firstName" required />
              </Field>
              <Field label="Last name">
                <input name="lastName" required />
              </Field>
              <Field label="Phone">
                <input name="phone" />
              </Field>
              <Field label="Email">
                <input name="email" type="email" />
              </Field>
              <Field label="Birth date">
                <input name="birthDate" type="date" />
              </Field>
            </div>
            <Field label="Administrative notes">
              <textarea name="administrativeNotes" rows={4} />
            </Field>
            <label className="checkbox-line">
              <input name="consentAccepted" type="checkbox" defaultChecked />
              Consent accepted
            </label>
            <button className="primary-button" type="submit">Create patient</button>
            {createPatient.error ? <ErrorBlock message={mutationError(createPatient.error)} /> : null}
          </form>
        </Panel>
        <Panel
          title="Patient directory"
          action={
            <div className="search-box">
              <Search size={16} />
              <input value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Search" />
            </div>
          }
        >
          <div className="table-list">
            {(searchedPatients.data ?? []).map((patient) => (
              <div className="table-row" key={patient.id}>
                <div>
                  <strong>
                    {patient.firstName} {patient.lastName}
                  </strong>
                  <small>{patient.email || patient.phone || "No contact data"}</small>
                </div>
                <Badge tone={patient.consentAccepted ? "good" : "warn"}>
                  {patient.consentAccepted ? "Consent" : "Pending"}
                </Badge>
              </div>
            ))}
            {searchedPatients.data?.length === 0 ? <EmptyState title="No patients found" /> : null}
          </div>
        </Panel>
      </div>
    </>
  );
}

function AgendaPage({ data }: { data: CoreData }) {
  const refreshAgenda = useRefresh(["appointments", "dashboard", "auditLogs"]);
  const createAppointment = useMutation({ mutationFn: api.createAppointment, onSuccess: refreshAgenda });
  const complete = useMutation({ mutationFn: api.completeAppointment, onSuccess: refreshAgenda });
  const cancel = useMutation({ mutationFn: (id: string) => api.cancelAppointment(id, "Cancelled from the web app"), onSuccess: refreshAgenda });
  const noShow = useMutation({ mutationFn: api.markNoShow, onSuccess: refreshAgenda });

  function submitAppointment(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    createAppointment.mutate({
      patientId: String(form.get("patientId")),
      professionalId: String(form.get("professionalId")),
      roomId: String(form.get("roomId")),
      serviceId: String(form.get("serviceId")),
      startAt: toInstant(String(form.get("startAt"))),
      endAt: toInstant(String(form.get("endAt"))),
      reason: String(form.get("reason")),
    });
  }

  return (
    <>
      <PageHeader title="Agenda" eyebrow="Scheduling" />
      <div className="two-column agenda-layout">
        <Panel title="Create appointment">
          <form className="stack-form" onSubmit={submitAppointment}>
            <SelectField label="Patient" name="patientId" items={data.patients.data ?? []} render={patientNameOption} />
            <SelectField label="Professional" name="professionalId" items={data.professionals.data ?? []} render={professionalNameOption} />
            <SelectField label="Room" name="roomId" items={data.rooms.data ?? []} render={(item) => item.name} />
            <SelectField label="Service" name="serviceId" items={data.services.data ?? []} render={(item) => `${item.name} · ${item.durationMinutes} min`} />
            <div className="form-grid">
              <Field label="Start">
                <input name="startAt" type="datetime-local" defaultValue={dateTimeLocal(1, 10)} required />
              </Field>
              <Field label="End">
                <input name="endAt" type="datetime-local" defaultValue={dateTimeLocal(1, 10, 45)} required />
              </Field>
            </div>
            <Field label="Reason">
              <input name="reason" placeholder="Session reason" />
            </Field>
            <button className="primary-button" type="submit">Create appointment</button>
            {createAppointment.error ? <ErrorBlock message={mutationError(createAppointment.error)} /> : null}
          </form>
        </Panel>
        <Panel title="Appointments">
          <AppointmentList
            appointments={data.appointments.data ?? []}
            data={data}
            actions={(appointment) => (
              <div className="row-actions">
                <button type="button" onClick={() => complete.mutate(appointment.id)} disabled={appointment.status !== "SCHEDULED"}>
                  Complete
                </button>
                <button type="button" onClick={() => noShow.mutate(appointment.id)} disabled={appointment.status !== "SCHEDULED"}>
                  No-show
                </button>
                <button type="button" onClick={() => cancel.mutate(appointment.id)} disabled={appointment.status !== "SCHEDULED"}>
                  Cancel
                </button>
              </div>
            )}
          />
        </Panel>
      </div>
    </>
  );
}

function AppointmentList({
  appointments,
  data,
  compact = false,
  actions,
}: {
  appointments: Appointment[];
  data: CoreData;
  compact?: boolean;
  actions?: (appointment: Appointment) => ReactNode;
}) {
  if (appointments.length === 0) {
    return <EmptyState title="No appointments" />;
  }

  return (
    <div className="timeline-list">
      {appointments.map((appointment) => (
        <div className="timeline-item" key={appointment.id}>
          <time>{formatDateTime(appointment.startAt)}</time>
          <div>
            <strong>{patientName(data.patients.data, appointment.patientId)}</strong>
            <small>
              {professionalName(data.professionals.data, appointment.professionalId)} · {roomName(data.rooms.data, appointment.roomId)} ·{" "}
              {serviceName(data.services.data, appointment.serviceId)}
            </small>
            {!compact && appointment.reason ? <p>{appointment.reason}</p> : null}
          </div>
          <Badge tone={appointmentTone(appointment.status)}>{appointment.status}</Badge>
          {actions ? actions(appointment) : null}
        </div>
      ))}
    </div>
  );
}

function TreatmentsPage({ data }: { data: CoreData }) {
  const [patientId, setPatientId] = useState("");
  const [episodeId, setEpisodeId] = useState("");
  const refreshTreatments = useRefresh(["dashboard", "auditLogs"]);
  const timeline = useQuery({
    queryKey: ["timeline", patientId],
    queryFn: () => api.timeline(patientId),
    enabled: Boolean(patientId),
  });
  const openEpisode = useMutation({
    mutationFn: (payload: { responsibleProfessionalId: string; title: string; startDate: string }) => api.openEpisode(patientId, payload),
    onSuccess: (episode) => {
      setEpisodeId(episode.id);
      refreshTreatments();
      void timeline.refetch();
    },
  });
  const addNote = useMutation({
    mutationFn: (payload: Parameters<typeof api.addSessionNote>[1]) => api.addSessionNote(episodeId, payload),
    onSuccess: () => {
      refreshTreatments();
      void timeline.refetch();
    },
  });
  const closeEpisode = useMutation({
    mutationFn: (id: string) => api.closeEpisode(id, today()),
    onSuccess: () => {
      refreshTreatments();
      void timeline.refetch();
    },
  });

  const episodes = timeline.data?.episodes.map((line) => line.episode) ?? [];

  function submitEpisode(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    openEpisode.mutate({
      responsibleProfessionalId: String(form.get("responsibleProfessionalId")),
      title: String(form.get("title")),
      startDate: String(form.get("startDate")),
    });
    event.currentTarget.reset();
  }

  function submitNote(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    addNote.mutate({
      appointmentId: String(form.get("appointmentId")) || undefined,
      professionalId: String(form.get("professionalId")),
      sessionDate: String(form.get("sessionDate")),
      painLevel: Number(form.get("painLevel")),
      treatedArea: String(form.get("treatedArea")),
      techniquesApplied: String(form.get("techniquesApplied")),
      observations: String(form.get("observations")),
      nextRecommendation: String(form.get("nextRecommendation")),
    });
    event.currentTarget.reset();
  }

  return (
    <>
      <PageHeader title="Treatments" eyebrow="Clinical timeline" />
      <div className="two-column">
        <Panel title="Episode and note">
          <div className="stack-form">
            <SelectField label="Patient" name="patientPicker" value={patientId} onChange={setPatientId} items={data.patients.data ?? []} render={patientNameOption} />
          </div>
          <form className="stack-form section-divider" onSubmit={submitEpisode}>
            <SelectField label="Responsible professional" name="responsibleProfessionalId" items={data.professionals.data ?? []} render={professionalNameOption} />
            <Field label="Episode title">
              <input name="title" placeholder="Lumbar pain" required />
            </Field>
            <Field label="Start date">
              <input name="startDate" type="date" defaultValue={today()} required />
            </Field>
            <button className="primary-button" type="submit" disabled={!patientId}>Open episode</button>
            {openEpisode.error ? <ErrorBlock message={mutationError(openEpisode.error)} /> : null}
          </form>
          <form className="stack-form section-divider" onSubmit={submitNote}>
            <SelectField label="Episode" name="episodeId" value={episodeId} onChange={setEpisodeId} items={episodes} render={(item) => `${item.title} · ${item.status}`} />
            <SelectField label="Professional" name="professionalId" items={data.professionals.data ?? []} render={professionalNameOption} />
            <SelectField label="Appointment" name="appointmentId" optional items={data.appointments.data ?? []} render={(item) => `${formatDateTime(item.startAt)} · ${item.status}`} />
            <Field label="Session date">
              <input name="sessionDate" type="date" defaultValue={today()} required />
            </Field>
            <Field label="Pain level">
              <input name="painLevel" type="number" min={0} max={10} defaultValue={5} />
            </Field>
            <Field label="Treated area">
              <input name="treatedArea" placeholder="Lumbar area" />
            </Field>
            <Field label="Techniques">
              <textarea name="techniquesApplied" rows={3} />
            </Field>
            <Field label="Observations">
              <textarea name="observations" rows={4} />
            </Field>
            <Field label="Next recommendation">
              <textarea name="nextRecommendation" rows={3} />
            </Field>
            <button className="primary-button" type="submit" disabled={!episodeId}>Add note</button>
            {addNote.error ? <ErrorBlock message={mutationError(addNote.error)} /> : null}
          </form>
        </Panel>
        <Panel title="Timeline">
          {!patientId ? <EmptyState title="Select a patient" /> : null}
          {timeline.isLoading ? <LoadingBlock /> : null}
          <div className="timeline-list">
            {timeline.data?.episodes.map((line) => (
              <div className="timeline-item timeline-item-wide" key={line.episode.id}>
                <time>{line.episode.startDate}</time>
                <div>
                  <strong>{line.episode.title}</strong>
                  <small>{professionalName(data.professionals.data, line.episode.responsibleProfessionalId)}</small>
                  {line.notes.map((note) => (
                    <p key={note.id}>
                      {note.sessionDate} · pain {note.painLevel ?? "-"} · {note.observations || "No observations"}
                    </p>
                  ))}
                </div>
                <Badge tone={line.episode.status === "OPEN" ? "good" : "neutral"}>{line.episode.status}</Badge>
                {line.episode.status === "OPEN" ? (
                  <button type="button" onClick={() => closeEpisode.mutate(line.episode.id)}>
                    Close
                  </button>
                ) : null}
              </div>
            ))}
          </div>
        </Panel>
      </div>
    </>
  );
}

function PackagesPage({ data }: { data: CoreData }) {
  const [patientId, setPatientId] = useState("");
  const refreshPackages = useRefresh(["packages", "dashboard", "auditLogs"]);
  const patientPackages = useQuery({
    queryKey: ["patientPackages", patientId],
    queryFn: () => api.patientPackages(patientId),
    enabled: Boolean(patientId),
  });
  const createPackage = useMutation({ mutationFn: api.createPackage, onSuccess: refreshPackages });
  const assignPackage = useMutation({
    mutationFn: (payload: { packageId: string; paidAmount?: number; paymentMethod?: string }) => api.assignPackage(patientId, payload),
    onSuccess: () => {
      refreshPackages();
      void patientPackages.refetch();
    },
  });
  const consume = useMutation({
    mutationFn: api.consumePackageSession,
    onSuccess: () => {
      refreshPackages();
      void patientPackages.refetch();
    },
  });

  function submitPackage(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    createPackage.mutate({
      name: String(form.get("name")),
      totalSessions: Number(form.get("totalSessions")),
      price: Number(form.get("price")),
    });
    event.currentTarget.reset();
  }

  function submitAssign(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    assignPackage.mutate({
      packageId: String(form.get("packageId")),
      paidAmount: Number(form.get("paidAmount")),
      paymentMethod: String(form.get("paymentMethod")),
    });
  }

  return (
    <>
      <PageHeader title="Packages" eyebrow="Internal payments" />
      <div className="two-column">
        <Panel title="Package catalog">
          <form className="compact-form" onSubmit={submitPackage}>
            <input name="name" placeholder="Bono 5 sesiones" required />
            <input name="totalSessions" type="number" min={1} defaultValue={5} required />
            <input name="price" type="number" min={0} step="0.01" defaultValue={200} />
            <button type="submit">
              <Plus size={16} />
              Add
            </button>
          </form>
          <div className="list">
            {(data.packages.data ?? []).map((item) => (
              <div className="list-row" key={item.id}>
                <span>{item.name}</span>
                <small>
                  {item.totalSessions} sessions · {money(item.price)}
                </small>
              </div>
            ))}
          </div>
        </Panel>
        <Panel title="Patient package">
          <form className="stack-form" onSubmit={submitAssign}>
            <SelectField label="Patient" name="patientPicker" value={patientId} onChange={setPatientId} items={data.patients.data ?? []} render={patientNameOption} />
            <SelectField label="Package" name="packageId" items={data.packages.data ?? []} render={(item) => `${item.name} · ${item.totalSessions} sessions`} />
            <Field label="Paid amount">
              <input name="paidAmount" type="number" min={0} step="0.01" defaultValue={120} />
            </Field>
            <Field label="Payment method">
              <input name="paymentMethod" defaultValue="CARD" />
            </Field>
            <button className="primary-button" type="submit" disabled={!patientId}>Assign package</button>
          </form>
          <div className="list section-divider">
            {(patientPackages.data ?? []).map((item) => (
              <div className="list-row" key={item.id}>
                <span>{item.remainingSessions} / {item.totalSessions} sessions</span>
                <button type="button" onClick={() => consume.mutate(item.id)} disabled={item.remainingSessions <= 0}>
                  Consume
                </button>
              </div>
            ))}
          </div>
        </Panel>
      </div>
    </>
  );
}

function ExercisesPage({ data }: { data: CoreData }) {
  const [patientId, setPatientId] = useState("");
  const refreshExercises = useRefresh(["exercises", "auditLogs"]);
  const patientPlans = useQuery({
    queryKey: ["exercisePlans", patientId],
    queryFn: () => api.exercisePlans(patientId),
    enabled: Boolean(patientId),
  });
  const createExercise = useMutation({ mutationFn: api.createExercise, onSuccess: refreshExercises });
  const assignPlan = useMutation({
    mutationFn: (payload: Parameters<typeof api.assignExercisePlan>[1]) => api.assignExercisePlan(patientId, payload),
    onSuccess: () => {
      refreshExercises();
      void patientPlans.refetch();
    },
  });

  function submitExercise(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    createExercise.mutate({
      name: String(form.get("name")),
      description: String(form.get("description")),
      videoUrl: String(form.get("videoUrl")),
      imageUrl: String(form.get("imageUrl")),
    });
    event.currentTarget.reset();
  }

  function submitPlan(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    assignPlan.mutate({
      professionalId: String(form.get("professionalId")),
      title: String(form.get("title")),
      notes: String(form.get("notes")),
      startsOn: String(form.get("startsOn")),
      items: [
        {
          exerciseId: String(form.get("exerciseId")),
          series: Number(form.get("series")),
          repetitions: Number(form.get("repetitions")),
          frequency: String(form.get("frequency")),
          notes: String(form.get("itemNotes")),
        },
      ],
    });
  }

  return (
    <>
      <PageHeader title="Exercises" eyebrow="Patient plans" />
      <div className="two-column">
        <Panel title="Exercise catalog">
          <form className="stack-form" onSubmit={submitExercise}>
            <Field label="Name">
              <input name="name" placeholder="Glute bridge" required />
            </Field>
            <Field label="Description">
              <textarea name="description" rows={3} />
            </Field>
            <Field label="Video URL">
              <input name="videoUrl" type="url" />
            </Field>
            <Field label="Image URL">
              <input name="imageUrl" type="url" />
            </Field>
            <button className="primary-button" type="submit">Create exercise</button>
          </form>
          <EntityList items={data.exercises.data ?? []} render={(item) => item.name} />
        </Panel>
        <Panel title="Assign plan">
          <form className="stack-form" onSubmit={submitPlan}>
            <SelectField label="Patient" name="patientPicker" value={patientId} onChange={setPatientId} items={data.patients.data ?? []} render={patientNameOption} />
            <SelectField label="Professional" name="professionalId" items={data.professionals.data ?? []} render={professionalNameOption} />
            <SelectField label="Exercise" name="exerciseId" items={data.exercises.data ?? []} render={(item) => item.name} />
            <Field label="Title">
              <input name="title" defaultValue="Home plan" required />
            </Field>
            <Field label="Starts on">
              <input name="startsOn" type="date" defaultValue={today()} required />
            </Field>
            <div className="form-grid">
              <Field label="Series">
                <input name="series" type="number" min={1} defaultValue={3} />
              </Field>
              <Field label="Reps">
                <input name="repetitions" type="number" min={1} defaultValue={12} />
              </Field>
            </div>
            <Field label="Frequency">
              <input name="frequency" defaultValue="4 days/week" />
            </Field>
            <Field label="Notes">
              <textarea name="notes" rows={3} />
            </Field>
            <Field label="Item notes">
              <textarea name="itemNotes" rows={3} />
            </Field>
            <button className="primary-button" type="submit" disabled={!patientId}>Assign plan</button>
          </form>
          <div className="list section-divider">
            {(patientPlans.data ?? []).map((plan) => (
              <div className="list-row" key={plan.id}>
                <span>{plan.title}</span>
                <small>{plan.items.length} exercise</small>
              </div>
            ))}
          </div>
        </Panel>
      </div>
    </>
  );
}

function AuditPage({ data }: { data: CoreData }) {
  return (
    <>
      <PageHeader title="Audit" eyebrow="Clinical access trace" />
      <Panel title="Recent events">
        <div className="audit-list">
          {(data.auditLogs.data ?? []).map((log) => (
            <div className="audit-row" key={log.id}>
              <time>{formatDateTime(log.occurredAt)}</time>
              <strong>{log.action}</strong>
              <span>{log.resourceType}</span>
              <small>{log.details}</small>
            </div>
          ))}
          {data.auditLogs.data?.length === 0 ? <EmptyState title="No audit events yet" /> : null}
        </div>
      </Panel>
    </>
  );
}

function SelectField<T extends { id: string }>({
  label,
  name,
  items,
  render,
  value,
  onChange,
  optional = false,
}: {
  label: string;
  name: string;
  items: T[];
  render: (item: T) => string;
  value?: string;
  onChange?: (value: string) => void;
  optional?: boolean;
}) {
  return (
    <Field label={label}>
      <select name={name} value={value} onChange={(event) => onChange?.(event.target.value)} required={!optional}>
        <option value="">{optional ? "None" : "Select"}</option>
        {items.map((item) => (
          <option key={item.id} value={item.id}>
            {render(item)}
          </option>
        ))}
      </select>
    </Field>
  );
}

function patientNameOption(patient: Patient) {
  return `${patient.firstName} ${patient.lastName}`;
}

function professionalNameOption(professional: Professional) {
  return `${professional.firstName} ${professional.lastName}`;
}
