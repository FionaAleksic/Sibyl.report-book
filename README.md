# Sibyl.report-book

**Test-Integration 0.7.0** – Ausbildungsnachweise für Sibyl System.

- Provider-ID: `Sibyl.report-book`, Typ: `report-book`.
- Abhängigkeit: `Sibyl.ad`.
- Daten: `sibyl_report_entries`.
- API: `/api/v1/addons/Sibyl.report-book/entries`, `/entries/{id}/submit`, `/entries/{id}/review`.
- Workflow: `DRAFT` → `SUBMITTED` → `APPROVED` oder `RETURNED`; zurückgegebene Einträge können überarbeitet werden.
- Berechtigungen: Benutzer sehen und ändern nur eigene Entwürfe; Administratoren können fremde eingereichte Nachweise freigeben/zurückgeben, keine Selbstfreigabe.
- Administrator-Konfiguration: `minMinutes`, `maxMinutes`, `allowFutureEntries`.

Code unter `Addons/Sibyl.report-book/`; wird mit Sibyl-Core kompiliert, **keine unabhängige GitHub-Release-Installation**. Migration der DB im Core (`V7`).

Stand: Entwicklungs-Build kompiliert; Live-Abnahme und Deployment stehen aus.
