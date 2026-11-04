import { useEffect, useState } from "react";

const API = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

function App() {
  const [logs, setLogs] = useState([]);
  const [analysis, setAnalysis] = useState(null);
  const [selectedId, setSelectedId] = useState(null);
  const [file, setFile] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  async function loadLogs() {
    try {
      const response = await fetch(`${API}/api/logs`);
      if (!response.ok) throw new Error("Could not load logs");
      const data = await response.json();
      setLogs(data);
    } catch {
      setError("Cannot connect to the backend. Check that the API is running.");
    }
  }

  async function loadAnalysis(id) {
    setSelectedId(id);
    setAnalysis(null);
    setError("");

    try {
      const response = await fetch(`${API}/api/logs/${id}/analysis`);
      if (!response.ok) throw new Error("Analysis failed");
      setAnalysis(await response.json());
    } catch {
      setError("Could not load analysis for this log.");
    }
  }

  async function uploadLog(event) {
    event.preventDefault();
    if (!file) return;

    setLoading(true);
    setError("");

    try {
      const formData = new FormData();
      formData.append("file", file);

      const response = await fetch(`${API}/api/logs/upload`, {
        method: "POST",
        body: formData
      });

      if (!response.ok) {
        const body = await response.text();
        throw new Error(body || "Upload failed");
      }

      const uploaded = await response.json();
      setFile(null);
      event.target.reset();
      await loadLogs();
      await loadAnalysis(uploaded.id);
    } catch (err) {
      setError(err.message || "Upload failed. Please try again.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadLogs();
  }, []);

  const stats = analysis
    ? [
        { label: "Errors", value: analysis.errorCount, type: "error" },
        { label: "Warnings", value: analysis.warningCount, type: "warning" },
        { label: "Info", value: analysis.infoCount, type: "info" },
        { label: "Other", value: analysis.otherCount, type: "other" }
      ]
    : [
        { label: "Errors", value: "—", type: "error" },
        { label: "Warnings", value: "—", type: "warning" },
        { label: "Info", value: "—", type: "info" },
        { label: "Other", value: "—", type: "other" }
      ];

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <div className="brand-icon">L</div>
          <div>
            <strong>LogScope</strong>
            <span>ENTERPRISE MONITOR</span>
          </div>
        </div>

        <div className="nav-label">WORKSPACE</div>
        <div className="nav-item active">▦ &nbsp; Overview</div>
        <div className="nav-item">◷ &nbsp; Log Explorer</div>
        <div className="nav-item">⌘ &nbsp; AI Insights</div>

        <div className="sidebar-bottom">
          <div className="status-dot" />
          <span>Log analysis workspace</span>
        </div>
      </aside>

      <main className="main-content">
        <header className="topbar">
          <div>
            <div className="breadcrumb">Workspace / Overview</div>
            <h1>Log Analytics Dashboard</h1>
            <p>Monitor, inspect and analyze your application logs.</p>
          </div>
          <div className="system-status">
            <span className="status-dot" />
            Backend connected when available
          </div>
        </header>

        {error && <div className="error-banner">{error}</div>}

        <section className="upload-panel">
          <div>
            <h2>Upload log file</h2>
            <p>Analyze application logs in seconds. Supports .log and .txt files.</p>
          </div>

          <form onSubmit={uploadLog} className="upload-form">
            <input
              type="file"
              accept=".log,.txt"
              onChange={(e) => setFile(e.target.files[0])}
            />
            <button type="submit" disabled={!file || loading}>
              {loading ? "Uploading..." : "↑ Upload & Analyze"}
            </button>
          </form>
        </section>

        <section className="stats-grid">
          {stats.map((stat) => (
            <div className="stat-card" key={stat.label}>
              <div className={`stat-indicator ${stat.type}`} />
              <span>{stat.label}</span>
              <strong>{stat.value}</strong>
            </div>
          ))}
        </section>

        <section className="content-grid">
          <div className="panel logs-panel">
            <div className="panel-heading">
              <div>
                <h2>Uploaded logs</h2>
                <p>{logs.length} files in workspace</p>
              </div>
              <button className="refresh-button" onClick={loadLogs}>↻</button>
            </div>

            {logs.length === 0 ? (
              <div className="empty-state">
                <div className="empty-icon">⌁</div>
                <strong>No logs uploaded yet</strong>
                <p>Upload a log file to get started.</p>
              </div>
            ) : (
              <div className="log-list">
                {logs.map((log) => (
                  <button
                    className={`log-item ${selectedId === log.id ? "selected" : ""}`}
                    key={log.id}
                    onClick={() => loadAnalysis(log.id)}
                  >
                    <span className="file-icon">LOG</span>
                    <span className="log-name">
                      <strong>{log.filename}</strong>
                      <small>{new Date(log.uploadedAt).toLocaleString()}</small>
                    </span>
                    <span className="log-arrow">›</span>
                  </button>
                ))}
              </div>
            )}
          </div>

          <div className="panel analysis-panel">
            <div className="panel-heading">
              <div>
                <h2>Analysis summary</h2>
                <p>{analysis ? analysis.filename : "Select a log to inspect"}</p>
              </div>
              <span className="live-tag">PARSER</span>
            </div>

            {analysis ? (
              <>
                <div className="summary-numbers">
                  <div>
                    <span>Total lines</span>
                    <strong>{analysis.totalLines}</strong>
                  </div>
                  <div>
                    <span>Parsed entries</span>
                    <strong>{analysis.parsedEntries}</strong>
                  </div>
                </div>

                <div className="severity-bars">
                  {stats.map((stat) => {
                    const value = analysis[`${stat.type}Count`];
                    const total = analysis.parsedEntries || 1;
                    return (
                      <div className="bar-row" key={stat.label}>
                        <span>{stat.label}</span>
                        <div className="bar-track">
                          <div
                            className={`bar-fill ${stat.type}`}
                            style={{ width: `${(value / total) * 100}%` }}
                          />
                        </div>
                        <strong>{value}</strong>
                      </div>
                    );
                  })}
                </div>
              </>
            ) : (
              <div className="empty-state compact">
                <div className="empty-icon">⌕</div>
                <strong>Nothing to analyze</strong>
                <p>Choose an uploaded file to see its analysis.</p>
              </div>
            )}
          </div>
        </section>

        <section className="panel entries-panel">
          <div className="panel-heading">
            <div>
              <h2>Log entries</h2>
              <p>Parsed events and severity levels</p>
            </div>
            {analysis && <span className="entry-count">{analysis.entries.length} entries</span>}
          </div>

          {analysis?.entries?.length ? (
            <div className="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>LINE</th>
                    <th>TIMESTAMP</th>
                    <th>LEVEL</th>
                    <th>MESSAGE</th>
                  </tr>
                </thead>
                <tbody>
                  {analysis.entries.map((entry, index) => (
                    <tr key={`${entry.lineNumber}-${index}`}>
                      <td>{entry.lineNumber}</td>
                      <td>{entry.timestamp || "—"}</td>
                      <td>
                        <span className={`level-pill ${entry.level.toLowerCase()}`}>
                          {entry.level}
                        </span>
                      </td>
                      <td className="message-cell">{entry.message}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <div className="empty-state compact">
              <p>Log entries will appear here after analysis.</p>
            </div>
          )}
        </section>

        <footer>LogScope · Enterprise Log Analyzer</footer>
      </main>
    </div>
  );
}

export default App;
