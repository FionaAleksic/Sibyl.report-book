package org.sibyl.addons.reportbook;
public final class WorkerMainTest {
    public static void main(String[] args) {
        check(WorkerMain.respond("{\"method\":\"ping\"}").contains("\"ok\":true"));
        check(WorkerMain.respond("{\"method\":\"totalMinutes\",\"payload\":{\"minutes\":[15,45,90]}}")
            .contains("\"totalMinutes\":150"));
        check(WorkerMain.respond("{\"method\":\"totalMinutes\",\"payload\":{\"minutes\":[17]}}")
            .contains("\"ok\":false"));
        check(WorkerMain.respond("{\"method\":\"unknown\"}").contains("\"ok\":false"));
        System.out.println("WorkerMainTest PASS");
    }
    private static void check(boolean ok){if(!ok)throw new AssertionError();}
}
