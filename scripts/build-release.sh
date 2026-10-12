#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
rm -rf build dist
mkdir -p build/classes build/tests build/stage dist
javac --release 21 -Xlint:all -d build/classes src/main/java/org/sibyl/addons/reportbook/WeeklyReport.java src/main/java/org/sibyl/addons/reportbook/WorkerMain.java
javac --release 21 -Xlint:all -cp build/classes -d build/tests src/test/java/org/sibyl/addons/reportbook/WeeklyReportTest.java src/test/java/org/sibyl/addons/reportbook/WorkerMainTest.java
java -ea -cp build/classes:build/tests org.sibyl.addons.reportbook.WeeklyReportTest
java -ea -cp build/classes:build/tests org.sibyl.addons.reportbook.WorkerMainTest
jar --create --file dist/sibyl-reportbook-0.1.0-rc.4.jar -C build/classes .
cp module.json deployment.json config.schema.json LICENSE README.md build/stage/
mkdir -p build/stage/backend build/stage/migrations
cp migrations/*.sql build/stage/migrations/
jar --create --file build/stage/backend/worker.jar --main-class org.sibyl.addons.reportbook.WorkerMain -C build/classes .
cp -r ui build/stage/
node --check ui/bridge.js
node --check ui/report.js
cp dist/sibyl-reportbook-0.1.0-rc.4.jar build/stage/
(cd build/stage && sha256sum module.json deployment.json config.schema.json ui/index.html ui/bridge.js ui/report.js ui/report.css migrations/V001__training_reports.sql backend/worker.jar sibyl-reportbook-0.1.0-rc.4.jar > SHA256SUMS.txt)
(cd build/stage && zip -q -X -r ../../dist/Sibyl.report-book-v0.1.0-rc.4.zip .)
python3 - <<'PY'
import zipfile,json,hashlib
p='dist/Sibyl.report-book-v0.1.0-rc.4.zip'
with zipfile.ZipFile(p) as z:
 m=json.loads(z.read('module.json'))
 assert m['id']=='Sibyl.report-book' and m['version']=='0.1.0-rc.4'
 for line in z.read('SHA256SUMS.txt').decode().splitlines():
  sha,name=line.split(maxsplit=1);assert hashlib.sha256(z.read(name)).hexdigest()==sha
print('Release archive verified')
PY
(cd dist && sha256sum Sibyl.report-book-v0.1.0-rc.4.zip > Sibyl.report-book-v0.1.0-rc.4.zip.sha256)
echo 'BUILD_OK Sibyl.report-book v0.1.0-rc.4'
