# Sibyl.report-book v0.1.0-rc.3

Backend-Worker in eigenem Java-Prozess über AF_UNIX Socket, eigenständiges SQL-Migrationsasset und addon-eigener Installationsvertrag. UI kommuniziert über die authentifizierte Capability-Bridge; kein DB-Kennwort im Worker.

Scoped Datenbank sibyl_ext_report_book muss von einem berechtigten Administrator vorab bereitgestellt werden. Datenbanktabelle wird bei Installation angelegt; Speichern der Berichte durch Worker ist noch nicht möglich (DB-Broker fehlt).
