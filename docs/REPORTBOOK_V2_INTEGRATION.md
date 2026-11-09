# Sibyl Berichtsheft v2 – Wochenbasierte Ausbildungsnachweise

**Stand:** 09.11.2026 · Entwicklungsstand auf `172.22.100.118:/home/fiona/sibyl-replatform` · **nicht produktiv deployed**

Dieses Feature verwendet den bestehenden Sibyl Core auf dem Proxmox-Paar (Frontend `172.22.120.250`, Backend `172.22.120.244`). Die Dateien unter `integration/core-v0.7/` sind die mit diesem Core kompilierten Integrationsquellen, **kein selbstständig installierbares Release-Paket**. Die bestehende Java/Spring-Boot-Addon-Brücke muss sie mitkompilieren.

## Verhalten

- Jeder authentifizierte Sibyl-Benutzer kann ein eigenes Berichtsheft auf Tages- oder Wochenbasis erstellen. Eine Woche beginnt am Montag, endet am Sonntag und gehört genau einem Benutzer.
- Tagesbasis: sieben Tage mit Tätigkeitsbeschreibung, Betrieb/Schule/Unterweisung/Urlaub/Krank/Feiertag/Sonstiges, Anwesenheit und Minuten. Wochenbasis: Wochenfreitext.
- Einreichen dürfen nur aktive Azubis mit zugewiesenem, aktivem Ausbilder.
- Nur der im Backend zugewiesene und aktivierte Ausbilder darf eingereichte Wochen des jeweiligen Azubis freigeben oder mit **Pflichtbegründung** zurückweisen.
- Nach Rückgabe kann der Azubi überarbeiten und erneut einreichen. Ein genehmigter Bericht wird nicht mehr verändert. Entwürfe sind privat; Prüfer sehen nur Berichte ihrer Zuordnung, und Adminrechte allein erlauben keinen Zugriff auf private Inhalte.
- Keine Selbstfreigabe. Ereignisse wie Einreichung, Änderung und Prüfentscheidung werden separat protokolliert.
- Die Admin-Ansicht enthält eine Liste der Azubis/Ausbilder, Zuordnung per Dropdown und Synchronisierung aus konfigurierten Active-Directory-Gruppen. Manuelle Konten/Zuordnungen werden bei einem AD-Sync nicht überschrieben.
- Der Reiter **Qualifikationen** wird bewusst nicht eingeführt.
- Datenhaltung in MySQL; die vorhandene V7-Tabelle `sibyl_report_entries` bleibt unangetastet.

## AD und universelle Konfiguration

Die Addon-Einstellungen in **Admin → Addon-Verwaltung → Berichtsheft** erhalten zwei Textfelder: `traineeGroupDns` und `trainerGroupDns`. Pro Zeile wird ein CN (z. B. `FIA`, `MA`) oder ein vollständiger Gruppen-DN hinterlegt; Firmennamen sind **nicht** hartkodiert. Bei der Synchronisierung werden nur explizit aktivierte AD-Verbindungen mit hinterlegten verschlüsselten Servicekontodaten verwendet. Die Abfrage nutzt LDAPS und rekursive Gruppenmitgliedschaft. Für mehrere AD-Domänen wird die angemeldete Identität von Sibyl verwendet. Überlappende Gruppenzugehörigkeit muss administrativ geprüft werden.

Die Code-Dokumentation und UI sind für ein universell einsetzbares System ausgelegt. Die tatsächlichen Gruppen in der Nikolauspflege müssen im Admin-Panel eingetragen und die erste Synchronisierung durchgeführt werden. Auch rein lokale Sibyl-Accounts können manuell als Azubi/Ausbilder markiert werden. Das Berichtsheft ist daher **nicht mehr von einer aktivierten AD-Abhängigkeit abhängig**.

## Einbau in aktuellen Entwicklungs-Core

Die Dateien aus `integration/core-v0.7/backend/` zuordnen:

- `ReportBookV2Controller.java`, `ReportBookDirectorySync.java`, `ReportBookAddonProvider.java` → `backend/src/main/java/de/nikoit/sibyl/addons/`
- `V8__report_book_workflow.sql` → `backend/src/main/resources/db/migration/`

Frontend aus `integration/core-v0.7/frontend/`:

- `ReportBookPage.tsx` → `frontend/src/pages/`
- `ReportBookAdminSettings.tsx` → `frontend/src/components/`
- `AdminAddonsPage.tsx` → `frontend/src/pages/`
- `api.ts` → `frontend/src/services/`
- `workflow.css` → `frontend/src/styles/`

**Wichtig:** Die Dateien `AdminAddonsPage.tsx`, `api.ts`, `workflow.css` sind Snapshots aus dem vorhandenen Entwicklungs-Core und nicht als pauschaler Ersatz für spätere Änderungen gedacht. In neuerer Version die Änderungen mergen, statt die Dateien blind zu überschreiben. Das alte Berichtsheft-Frontend wurde vor dem Austausch lokal gesichert.

## API

Alle Endpunkte liegen unter `/api/v1/addons/Sibyl.report-book/v2`:

- `GET /me`, `GET /weeks`, `POST /weeks`
- `GET /weeks/{id}`, `PUT /weeks/{id}`, `POST /weeks/{id}/submit`
- `GET /weeks/{id}/history`, `GET /review-queue`, `POST /weeks/{id}/review`
- `GET /admin/roster`, `PUT /admin/roster/{username}`
- `PUT /admin/assignment/{username}`, `DELETE /admin/assignment/{username}`
- `POST /admin/sync`

Nur Backend-verifizierte Authentifizierung, Rollen und Zuweisungen gelten. Optimistische Berichtsversionierung verhindert veraltete Schreiboperationen; Wochen in Prüfung oder nach Freigabe sind nicht editierbar.

## Teststatus

- `npm run build`: erfolgreich (TypeScript/Vite), Stand 09.11.2026.
- `mvn -B -ntp test package`: erfolgreich (Java 21/Spring Boot); sieben vorhandene Tests bestanden.
- Noch offen: eigener vollständiger isolierter MySQL-/AD-/RBAC-Integrationstest, Export/Import, PDF-Layoutprüfung und Fachabnahme.
- Ein automatisierter Smoke-Test mit einer isolierten MySQL-Testumgebung wurde nicht ausgeführt, weil die Remote-Ausführung von einer Sicherheitsprüfung blockiert wurde. **Keine** Umgehung, **keine** produktive Datenbankänderung.

## Vor einem Deployment

1. Die produktiven MySQL-Daten konsistent sichern und den Wiederherstellungsweg testen; ebenfalls Frontend, Backend-JAR, Environment-Datei und Verschlüsselungskey sichern.
2. V8-Flyway-Migration gegen eine Datenbankkopie prüfen. Flyway-Migrationen mit `CREATE TABLE` lassen sich nicht einfach mit einem JAR-Rollback entfernen.
3. Ein eigenes Release aus vollständig reproduzierbarem Core-Checkout erstellen und Artefakte per SHA-256 prüfen; **keine** ungeprüften Branch-ZIPs verwenden.
4. Vor Umschaltung API-Tests mit Azubi-, Ausbilder-, Nicht-Zugeordneten- und Administratorkonten ausführen. Besonderes Augenmerk auf Gruppensynchronisierung und Datenschutz.
5. Frontend `172.22.120.250` und Backend `172.22.120.244` nur nach freigegebenem SSH-Deploymentzugriff und verifizierter Sicherung aktualisieren.
6. Änderungen nach Tests über einen PR in `test` integrieren und eine neue versionierte Vorabveröffentlichung erstellen.

## Noch nicht enthalten

- PDF-Import des vorhandenen IHK-Exports und Übernahme früherer Berichtswochen.
- Datei-Anhänge in MySQL, umfangreiche Kommentar-Threads sowie automatischer PDF-Serverexport. Es gibt eine Druckansicht, die der Browser als PDF speichern kann.
- Elektronische Unterschriften, automatische Benachrichtigungen und IHK-spezifische Prüfungsbestätigungen.
- Eine Live-Synchronisierung aus echten AD-Gruppen oder eine bestätigte Installation auf `.120.250/.120.244`.

Referenzen: [IHK Region Stuttgart – Digitales Berichtsheft](https://www.ihk.de/stuttgart/fuer-unternehmen/fachkraefte-und-ausbildung/ausbildung/serviceportal-bildung-berichtsheft-4845240) und [IHK Digital – Handbuch](https://hilfe-ihkdigital.atlassian.net/wiki/spaces/SPB/pages/120127489/Nutzerhandbuch).
