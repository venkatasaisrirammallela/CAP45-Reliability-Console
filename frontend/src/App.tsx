import { useCallback, useEffect, useState } from "react";
import "./App.css";

type JsonObject = Record<string, unknown>;
type Service = { serviceId: string; serviceName: string; description?: string };
type ServiceRow = {
  service: Service;
  summary: JsonObject | null;
  budget: JsonObject | null;
  burn: JsonObject | null;
  decision: string;
  reason: string;
  errors: string[];
};

function objectOf(value: unknown): JsonObject | null {
  return value !== null && typeof value === "object" && !Array.isArray(value)
    ? (value as JsonObject) : null;
}
function numberOf(obj: JsonObject | null, ...keys: string[]): number | null {
  if (!obj) return null;
  for (const key of keys) {
    const value = obj[key];
    if (typeof value === "number" && Number.isFinite(value)) return value;
  }
  return null;
}
function pct(value: number | null) { return value === null ? "—" : `${value.toFixed(2)}%`; }
function count(value: number | null) { return value === null ? "—" : value.toFixed(2); }
function stringOf(obj: JsonObject | null, key: string): string | null {
  const value = obj?.[key];
  return typeof value === "string" ? value : null;
}
async function getJson(url: string): Promise<unknown> {
  const response = await fetch(url);
  if (!response.ok) throw new Error(`HTTP ${response.status} at ${url}`);
  return response.json();
}
async function loadService(service: Service): Promise<ServiceRow> {
  const id = encodeURIComponent(service.serviceId);
  const urls = [
    `/api/v1/sli/summary?serviceId=${id}`,
    `/api/v1/error-budget?serviceId=${id}`,
    `/api/v1/burn-rate?serviceId=${id}&hours=1`,
    `/api/v1/release-gate?serviceId=${id}`,
  ];
  const results = await Promise.allSettled(urls.map(getJson));
  const errors: string[] = [];
  const labels = ["SLI", "Error budget", "Burn rate", "Release history"];
  results.forEach((result, index) => {
    if (result.status === "rejected") {
      errors.push(`${labels[index]}: ${String(result.reason)}`);
    }
  });
  const value = (index: number): unknown => results[index].status === "fulfilled"
    ? results[index].value : null;
  const history = value(3);
  const latest = Array.isArray(history) && history.length ? objectOf(history[0]) : null;
  return {
    service,
    summary: objectOf(value(0)),
    budget: objectOf(value(1)),
    burn: objectOf(value(2)),
    decision: stringOf(latest, "decision") ?? "NOT EVALUATED",
    reason: stringOf(latest, "reason") ?? "No saved release decision",
    errors,
  };
}

function App() {
  const [rows, setRows] = useState<ServiceRow[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [selected, setSelected] = useState<string | null>(null);
  const [refreshKey, setRefreshKey] = useState(0);

  const refresh = useCallback(() => setRefreshKey(k => k + 1), []);

  useEffect(() => {
    let active = true;
    async function load() {
      setLoading(true);
      setError("");
      try {
        const data = await getJson("/api/v1/services");
        if (!Array.isArray(data)) throw new Error("Services API did not return an array");
        const services: Service[] = data.filter((s): s is Service => {
          const obj = objectOf(s);
          return typeof obj?.serviceId === "string" && typeof obj.serviceName === "string";
        });
        const next = await Promise.all(services.map(loadService));
        if (active) setRows(next);
      } catch (e) {
        if (active) { setRows([]); setError(e instanceof Error ? e.message : String(e)); }
      } finally {
        if (active) setLoading(false);
      }
    }
    void load();
    return () => { active = false; };
  }, [refreshKey]);

  const breached = rows.filter(r => r.budget?.exhausted === true).length;
  const openDetails = rows.find(r => r.service.serviceId === selected);

  return (
    <div className="dashboard">
      <header className="header">
        <h1>CAP-45 Reliability Console</h1>
        <p>All Services · API Reliability SLO &amp; Error-Budget Operations</p>
      </header>
      <main>
        <div className="heading-row">
          <div><h2>All Services Dashboard</h2><p>Live API metrics · release decisions are saved history</p></div>
          <button type="button" onClick={refresh} disabled={loading}>{loading ? "Loading…" : "Refresh"}</button>
        </div>
        <div className="overview-cards">
          <div className="overview-card"><span>Registered services</span><strong>{rows.length}</strong></div>
          <div className="overview-card"><span>Exhausted error budgets</span><strong>{breached}</strong></div>
          <div className="overview-card"><span>Blocked latest releases</span><strong>{rows.filter(r => r.decision === "BLOCK").length}</strong></div>
        </div>
        {error && <p className="error-message" role="alert">Unable to load services: {error}</p>}
        <div className="table-wrap">
          <table className="services-table">
            <thead><tr><th>Service</th><th>Availability SLI</th><th>Latency SLI</th><th>Budget remaining</th><th>Burn rate (1h)</th><th>Release gate</th><th>Details</th></tr></thead>
            <tbody>
              {rows.map(row => {
                const availability = numberOf(row.summary, "availabilityPercent", "availabilityPercentage", "availability");
                const latency = numberOf(row.summary, "latencyCompliancePercent", "latencyCompliancePercentage", "latencyPercent", "latencyPercentage");
                const budget = numberOf(row.budget, "remainingFailures");
                const burn = numberOf(row.burn, "burnRate");
                return <tr key={row.service.serviceId}>
                  <td><strong>{row.service.serviceName}</strong><small>{row.service.serviceId}</small></td>
                  <td>{pct(availability)}</td>
                  <td>{pct(latency)}</td>
                  <td className={budget !== null && budget < 0 ? "danger-text" : ""}>{count(budget)} failures</td>
                  <td>{burn === null ? "—" : `${burn.toFixed(2)}×`}</td>
                  <td><span className={`decision ${row.decision.toLowerCase().replace(/[^a-z]/g, "-")}`}>{row.decision}</span></td>
                  <td><button type="button" onClick={() => setSelected(selected === row.service.serviceId ? null : row.service.serviceId)}>{selected === row.service.serviceId ? "Hide" : "View"}</button></td>
                </tr>;
              })}
              {!loading && !error && rows.length === 0 && <tr><td colSpan={7}>No services registered yet.</td></tr>}
              {loading && <tr><td colSpan={7}>Loading all services and their metrics…</td></tr>}
            </tbody>
          </table>
        </div>
        {openDetails && <section className="info-panel detail-panel">
          <h2>{openDetails.service.serviceName} — Details</h2>
          <p>{openDetails.service.description ?? openDetails.service.serviceId}</p>
          <p><strong>Release gate:</strong> {openDetails.decision} — {openDetails.reason}</p>
          <p><strong>Error budget consumed:</strong> {pct(numberOf(openDetails.budget, "consumedPercent"))}</p>
          <p><strong>Allowed failures:</strong> {count(numberOf(openDetails.budget, "allowedFailures"))} · <strong>Actual failures:</strong> {count(numberOf(openDetails.budget, "actualFailures"))}</p>
          {openDetails.errors.length > 0 && <p className="error-message">Some endpoints failed: {openDetails.errors.join("; ")}</p>}
          <details><summary>View raw API responses (for field-name debugging)</summary><pre>{JSON.stringify({summary: openDetails.summary, budget: openDetails.budget, burn: openDetails.burn}, null, 2)}</pre></details>
        </section>}
        <section className="info-panel"><h2>System Status</h2><p>{loading ? "Fetching service metrics…" : error ? "Service list could not be loaded." : `Loaded ${rows.length} registered service(s).`}</p><p>“—” means the expected numeric field was not present in the API response, or its request failed. Click View to inspect details.</p></section>
      </main>
    </div>
  );
}
export default App;
