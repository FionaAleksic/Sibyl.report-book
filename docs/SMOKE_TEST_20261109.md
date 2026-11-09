# Sibyl ReportBook v2 – Testprotokoll 09.11.2026

Umgebung: isolierter MySQL-8.4-Container auf Entwicklungsserver `172.22.100.118` (nur Loopback), Java 21 Backend auf `127.0.0.1:18081`. Testkonten und Testpasswörter wurden zur Laufzeit generiert und nicht in das Repository übernommen.

## Ergebnis

**INTEGRATION_TEST_SUCCESS**. Alle 23 Prüfungen erfolgreich:

1. Isolierter MySQL-Container gestartet.
2. MySQL initialisiert und verbindungsbereit.
3. Spring-Backend startet, Flyway V1–V8 erfolgreich.
4. Lokale Administratoranmeldung.
5. Berichtsheft ohne AD-Zwang aktivierbar.
6. Azubi-Konto angelegt.
7. Ausbilder-Konto angelegt.
8. Azubi-Rolle gesetzt.
9. Ausbilder-Rolle gesetzt.
10. Ausbilder zugeordnet.
11. Azubi erstellt eigene Berichtswoche.
12. Zugewiesener Ausbilder kann Bericht lesen.
13. Nicht zugeordneter Administrator erhält 403 für private Inhalte.
14. Tagesbericht wird persistent gespeichert.
15. Azubi reicht Woche ein.
16. Eingereichte Woche kann nicht mehr bearbeitet werden.
17. Selbstfreigabe wird verweigert.
18. Zurückweisung ohne Begründung wird verweigert.
19. Ausbilder weist mit Begründung zurück.
20. Azubi korrigiert und speichert zurückgegebenen Bericht.
21. Azubi reicht Korrektur erneut ein.
22. Ausbilder gibt erneut eingereichte Woche frei; freigegebene Woche ist unveränderlich.
23. Prüf-/Einreichhistorie ist gespeichert und abrufbar.

Der Testcontainer und temporäre Backend-Prozess wurden beendet, **keine produktiven Daten verändert**.

## Weitere Tests

- `npm run build` TypeScript/Vite: bestanden, 09.11.2026.
- `mvn -B -ntp test package` Java21/Spring Boot: bestanden, 7 vorhandene Unit-Tests ohne Fehler.
- AD-Synchronisierung gegen echte AD-Servicekonten: noch offen.
- Benutzerabnahme und PDF-Layout: noch offen.
- Live-Deployment auf Frontend `.250` / Backend `.244`: noch offen, da Zugang abgewiesen bzw. TCP nicht erreichbar.

## Deployment-Quelle

Der Release-Build wurde **direkt auf `.118` kompiliert** und als `sibyl-direct-reportbook-v2-20261109.tar.gz` bereitgestellt. GitHub ist nur Backup/Versionshistorie und wird nicht zum Deployment verwendet.
