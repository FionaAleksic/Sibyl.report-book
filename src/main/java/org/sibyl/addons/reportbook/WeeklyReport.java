package org.sibyl.addons.reportbook;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Weekly training report domain core; no hidden write to a company system. */
public final class WeeklyReport {
    public static final String MODULE_ID="Sibyl.report-book";
    public static final String VERSION="0.1.0-rc.2";
    public record Entry(LocalDate day, int minutes, String category, String description) {
        public Entry {
            if(day == null || minutes <= 0 || minutes > 12 * 60 || minutes % 15 != 0 ||
                category == null || category.isBlank() || category.length() > 100 ||
                description == null || description.isBlank() || description.length() > 4000)
                throw new IllegalArgumentException("Invalid time or training activity");
        }
    }
    private final LocalDate monday;
    private final String traineeId;
    private final List<Entry> entries=new ArrayList<>();

    public WeeklyReport(LocalDate monday,String traineeId) {
        if(monday==null || monday.getDayOfWeek()!=DayOfWeek.MONDAY ||
           traineeId==null || !traineeId.matches("[A-Za-z0-9_.-]{1,128}"))
            throw new IllegalArgumentException("ISO-week Monday and trainee ID required");
        this.monday=monday;
        this.traineeId=traineeId;
    }
    public synchronized void add(Entry entry) {
        if(entry==null || entry.day().isBefore(monday) ||
           entry.day().isAfter(monday.plusDays(6)))
            throw new IllegalArgumentException("Entry not in training week");
        entries.add(entry);
    }
    public synchronized List<Entry> entries() {return List.copyOf(entries);}
    public synchronized int totalMinutes() {
        return entries.stream().mapToInt(Entry::minutes).sum();
    }
    public synchronized String exportMarkdown() {
        StringBuilder output=new StringBuilder("# Ausbildungsbericht – KW ")
            .append(monday.get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR))
            .append(" / ").append(monday.get(java.time.temporal.IsoFields.WEEK_BASED_YEAR))
            .append("\n\nAzubi-ID: ").append(traineeId)
            .append("\n\n| Datum | Minuten | Kategorie | Tätigkeit |\n")
            .append("|---|---:|---|---|\n");
        for(Entry e:entries) output.append("| ").append(e.day()).append(" | ")
            .append(e.minutes()).append(" | ").append(cell(e.category()))
            .append(" | ").append(cell(e.description())).append(" |\n");
        return output.append("\nGesamt: ").append(totalMinutes()).append(" Minuten\n").toString();
    }
    private static String cell(String input) {
        return input.replace("\r"," ").replace("\n"," ").replace("|","\\|");
    }
}
