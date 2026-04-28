import { createContext, PropsWithChildren, useContext, useEffect, useMemo, useState } from "react";

export type AppTheme = "light" | "dark";
export type AppLanguage = "en" | "es";

type PreferencesContextValue = {
  theme: AppTheme;
  language: AppLanguage;
  setTheme: (theme: AppTheme) => void;
  setLanguage: (language: AppLanguage) => void;
  t: (key: string) => string;
};

const THEME_KEY = "cliniccore.theme";
const LANGUAGE_KEY = "cliniccore.language";

const es: Record<string, string> = {
  "Application error": "Error de la aplicación",
  "Clinic operations": "Operativa clínica",
  Email: "Email",
  Password: "Contraseña",
  "Signing in...": "Iniciando sesión...",
  "Sign in": "Iniciar sesión",
  "Sign out": "Cerrar sesión",
  Dashboard: "Panel",
  Clinic: "Clínica",
  Patients: "Pacientes",
  Agenda: "Agenda",
  Calendar: "Calendario",
  Treatments: "Tratamientos",
  Packages: "Bonos",
  Exercises: "Ejercicios",
  Audit: "Auditoría",
  "Clinic workspace": "Espacio de clínica",
  Today: "Hoy",
  "Next 7 days": "Próximos 7 días",
  "Active patients": "Pacientes activos",
  "Open episodes": "Episodios abiertos",
  "Active packages": "Bonos activos",
  Income: "Ingresos",
  "No-shows 30d": "No asistencias 30d",
  "Upcoming appointments": "Próximas citas",
  "Clinic workload": "Carga de clínica",
  "Cancelled next 7 days": "Canceladas próximos 7 días",
  "Open treatment episodes": "Episodios de tratamiento abiertos",
  "Active patient packages": "Bonos de paciente activos",
  "Clinic setup": "Configuración de clínica",
  Workspace: "Espacio",
  Professionals: "Profesionales",
  Rooms: "Salas",
  Services: "Servicios",
  "First name": "Nombre",
  "Last name": "Apellidos",
  Phone: "Teléfono",
  Color: "Color",
  Add: "Añadir",
  "Room name": "Nombre de sala",
  "Service name": "Nombre del servicio",
  "No records yet": "Sin registros todavía",
  "Administrative record": "Ficha administrativa",
  "Create patient": "Crear paciente",
  "Birth date": "Fecha de nacimiento",
  "Administrative notes": "Observaciones administrativas",
  "Consent accepted": "Consentimiento aceptado",
  "Patient directory": "Directorio de pacientes",
  Search: "Buscar",
  "No contact data": "Sin datos de contacto",
  Consent: "Consentimiento",
  Pending: "Pendiente",
  "No patients found": "No se encontraron pacientes",
  Scheduling: "Agenda",
  "Calendar view": "Vista de calendario",
  Previous: "Anterior",
  Next: "Siguiente",
  "Month calendar": "Calendario mensual",
  appointments: "citas",
  more: "más",
  "Day appointments": "Citas del día",
  "No appointments this day": "Sin citas este día",
  "Create appointment": "Crear cita",
  Patient: "Paciente",
  Professional: "Profesional",
  Room: "Sala",
  Service: "Servicio",
  Start: "Inicio",
  End: "Fin",
  Reason: "Motivo",
  "Session reason": "Motivo de la sesión",
  Appointments: "Citas",
  Complete: "Realizada",
  "No-show": "No asistida",
  Cancel: "Cancelar",
  "No appointments": "Sin citas",
  "Unknown patient": "Paciente desconocido",
  "Unknown professional": "Profesional desconocido",
  "Unknown room": "Sala desconocida",
  "Unknown service": "Servicio desconocido",
  SCHEDULED: "Programada",
  COMPLETED: "Realizada",
  CANCELLED: "Cancelada",
  NO_SHOW: "No asistida",
  "Clinical timeline": "Timeline clínico",
  "Episode and note": "Episodio y nota",
  "Select a patient": "Selecciona un paciente",
  "Responsible professional": "Profesional responsable",
  "Episode title": "Título del episodio",
  "Lumbar pain": "Dolor lumbar",
  "Start date": "Fecha de inicio",
  "Open episode": "Abrir episodio",
  Episode: "Episodio",
  Appointment: "Cita",
  None: "Ninguna",
  Select: "Selecciona",
  "Session date": "Fecha de sesión",
  "Pain level": "Dolor",
  "Treated area": "Zona tratada",
  Techniques: "Técnicas",
  Observations: "Observaciones",
  "Next recommendation": "Próxima recomendación",
  "Add note": "Añadir nota",
  Timeline: "Timeline",
  "No observations": "Sin observaciones",
  Close: "Cerrar",
  OPEN: "Abierto",
  CLOSED: "Cerrado",
  "Internal payments": "Pagos internos",
  "Package catalog": "Catálogo de bonos",
  sessions: "sesiones",
  "Patient package": "Bono de paciente",
  Package: "Bono",
  "Paid amount": "Importe pagado",
  "Payment method": "Método de pago",
  "Assign package": "Asignar bono",
  Consume: "Consumir",
  "Patient plans": "Planes del paciente",
  "Exercise catalog": "Catálogo de ejercicios",
  Name: "Nombre",
  Description: "Descripción",
  "Video URL": "URL de vídeo",
  "Image URL": "URL de imagen",
  "Glute bridge": "Puente de glúteo",
  "Create exercise": "Crear ejercicio",
  "Assign plan": "Asignar plan",
  Exercise: "Ejercicio",
  Title: "Título",
  "Home plan": "Plan domiciliario",
  "Starts on": "Empieza el",
  Series: "Series",
  Reps: "Repeticiones",
  Frequency: "Frecuencia",
  Notes: "Notas",
  "Item notes": "Notas del ejercicio",
  "4 days/week": "4 días/semana",
  "Clinical access trace": "Registro de accesos clínicos",
  "Recent events": "Eventos recientes",
  "No audit events yet": "Sin eventos de auditoría todavía",
  Loading: "Cargando",
  Theme: "Tema",
  "Light theme": "Tema claro",
  "Dark theme": "Tema oscuro",
  Language: "Idioma",
  English: "Inglés",
  Spanish: "Español",
};

const PreferencesContext = createContext<PreferencesContextValue | null>(null);

function readTheme(): AppTheme {
  const stored = localStorage.getItem(THEME_KEY);
  return stored === "dark" ? "dark" : "light";
}

function readLanguage(): AppLanguage {
  const stored = localStorage.getItem(LANGUAGE_KEY);
  return stored === "es" ? "es" : "en";
}

export function PreferencesProvider({ children }: PropsWithChildren) {
  const [theme, setThemeState] = useState<AppTheme>(readTheme);
  const [language, setLanguageState] = useState<AppLanguage>(readLanguage);

  useEffect(() => {
    document.documentElement.dataset.theme = theme;
    localStorage.setItem(THEME_KEY, theme);
  }, [theme]);

  useEffect(() => {
    document.documentElement.lang = language;
    localStorage.setItem(LANGUAGE_KEY, language);
  }, [language]);

  const value = useMemo<PreferencesContextValue>(
    () => ({
      theme,
      language,
      setTheme: setThemeState,
      setLanguage: setLanguageState,
      t: (key: string) => (language === "es" ? es[key] ?? key : key),
    }),
    [language, theme],
  );

  return <PreferencesContext.Provider value={value}>{children}</PreferencesContext.Provider>;
}

export function usePreferences() {
  const context = useContext(PreferencesContext);
  if (!context) {
    throw new Error("PreferencesProvider is missing");
  }
  return context;
}
