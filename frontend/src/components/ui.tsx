import type { PropsWithChildren, ReactNode } from "react";
import { AlertCircle, LoaderCircle } from "lucide-react";
import { usePreferences } from "../preferences/preferences";

export function PageHeader({
  title,
  eyebrow,
  actions,
}: {
  title: string;
  eyebrow: string;
  actions?: ReactNode;
}) {
  return (
    <header className="page-header">
      <div>
        <span className="eyebrow">{eyebrow}</span>
        <h1>{title}</h1>
      </div>
      {actions ? <div className="page-actions">{actions}</div> : null}
    </header>
  );
}

export function Panel({ title, children, action }: PropsWithChildren<{ title: string; action?: ReactNode }>) {
  return (
    <section className="panel">
      <div className="panel-heading">
        <h2>{title}</h2>
        {action}
      </div>
      {children}
    </section>
  );
}

export function EmptyState({ title, detail }: { title: string; detail?: string }) {
  return (
    <div className="empty-state">
      <span>{title}</span>
      {detail ? <small>{detail}</small> : null}
    </div>
  );
}

export function LoadingBlock() {
  const { t } = usePreferences();

  return (
    <div className="state-line">
      <LoaderCircle className="spin" size={16} />
      {t("Loading")}
    </div>
  );
}

export function ErrorBlock({ message }: { message: string }) {
  return (
    <div className="error-line">
      <AlertCircle size={16} />
      {message}
    </div>
  );
}

export function Field({
  label,
  children,
}: PropsWithChildren<{
  label: string;
}>) {
  return (
    <label className="field">
      <span>{label}</span>
      {children}
    </label>
  );
}

export function Badge({ children, tone = "neutral" }: PropsWithChildren<{ tone?: "neutral" | "good" | "warn" | "bad" }>) {
  return <span className={`badge badge-${tone}`}>{children}</span>;
}
