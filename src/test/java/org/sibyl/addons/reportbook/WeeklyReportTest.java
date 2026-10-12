package org.sibyl.addons.reportbook;
import java.time.*;
public final class WeeklyReportTest {
    public static void main(String[] args) {
        var report=new WeeklyReport(LocalDate.of(2026,10,12),"azubi-01");
        report.add(new WeeklyReport.Entry(LocalDate.of(2026,10,12),90,"Systemintegration","Netzwerke | DHCP"));
        report.add(new WeeklyReport.Entry(LocalDate.of(2026,10,13),60,"Dokumentation","Wiki"));
        check(report.totalMinutes()==150,"minutes");
        check(report.exportMarkdown().contains("Netzwerke \\| DHCP"),"safe Markdown");
        check(report.entries().size()==2,"entries");
        try {report.add(new WeeklyReport.Entry(LocalDate.of(2026,10,20),60,"Test","outside"));throw new AssertionError();}
        catch(IllegalArgumentException expected) {}
        try {new WeeklyReport.Entry(LocalDate.of(2026,10,12),17,"Test","invalid");throw new AssertionError();}
        catch(IllegalArgumentException expected) {}
        System.out.println("WeeklyReportTest PASS");
    }
    private static void check(boolean c,String m) {if(!c)throw new AssertionError(m);}
}
