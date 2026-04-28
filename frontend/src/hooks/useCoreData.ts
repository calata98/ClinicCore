import { useQuery } from "@tanstack/react-query";
import { api } from "../api/client";

function rangeAroundToday() {
  const from = new Date();
  from.setDate(from.getDate() - 7);
  from.setHours(0, 0, 0, 0);

  const to = new Date();
  to.setDate(to.getDate() + 21);
  to.setHours(23, 59, 59, 999);

  return { from: from.toISOString(), to: to.toISOString() };
}

export function useCoreData(enabled: boolean) {
  const range = rangeAroundToday();

  const profile = useQuery({ queryKey: ["profile"], queryFn: api.me, enabled });
  const clinic = useQuery({ queryKey: ["clinic"], queryFn: api.currentClinic, enabled });
  const professionals = useQuery({ queryKey: ["professionals"], queryFn: api.professionals, enabled });
  const rooms = useQuery({ queryKey: ["rooms"], queryFn: api.rooms, enabled });
  const services = useQuery({ queryKey: ["services"], queryFn: api.services, enabled });
  const patients = useQuery({ queryKey: ["patients", ""], queryFn: () => api.patients(), enabled });
  const appointments = useQuery({
    queryKey: ["appointments", range.from, range.to],
    queryFn: () => api.appointments(range.from, range.to),
    enabled,
  });
  const packages = useQuery({ queryKey: ["packages"], queryFn: api.packages, enabled });
  const exercises = useQuery({ queryKey: ["exercises"], queryFn: api.exercises, enabled });
  const dashboard = useQuery({ queryKey: ["dashboard"], queryFn: api.dashboard, enabled });
  const auditLogs = useQuery({ queryKey: ["auditLogs"], queryFn: api.auditLogs, enabled });

  return {
    range,
    profile,
    clinic,
    professionals,
    rooms,
    services,
    patients,
    appointments,
    packages,
    exercises,
    dashboard,
    auditLogs,
  };
}
