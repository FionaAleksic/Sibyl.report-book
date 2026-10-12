# Sibyl.report-book

Erstes eigenständiges Pre-Release 0.1.0-rc.1 für Ausbildungs-Wochenberichte. Funktionskern: Tätigkeiten mit Datum und Viertelstundenvalidierung erfassen, ISO-Kalenderwoche führen, Minuten summieren und als Markdown exportieren. Enthält Tests und den reproduzierbaren Java-21-Build: `bash scripts/build-release.sh`.

Der fachliche Bedarf ist aus der Nikolauspflege-Ausbildungsumgebung bekannt, die Implementierung ist jedoch **firmenneutral**. Dieses Modul hat keine AD-Pflicht; lokale Sibyl-Benutzer oder später AD-Identitäten können über den Core-Adapter zugeordnet werden.

**Aktuell nicht live aktivierbar:** RC.7 hat noch keine Plugin-Runtime. Die spätere Version benötigt Persistenz, Benutzer-/Ausbilder-Rollen, Freigabe-Workflow, Prüfprotokoll, UI und PDF-Ausgabe. Der RC.7-Addon-Katalog enthält dieses neue Repository zudem noch nicht; dazu ist ein gesondertes Core-Katalogupdate erforderlich. Keine Produktivdaten verändert.
