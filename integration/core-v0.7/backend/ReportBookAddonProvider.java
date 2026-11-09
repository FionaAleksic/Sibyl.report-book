package de.nikoit.sibyl.addons;
import java.util.List;
import org.springframework.stereotype.Component;
@Component
public class ReportBookAddonProvider implements SibylAddonProvider {
 @Override public String type(){return "report-book";}
 @Override public List<AddonConfigurationService.Field> settings(){
  return List.of(
   new AddonConfigurationService.Field("minMinutes","Minuten je altem Eintrag (Minimum)","number",
       "Kompatibilität mit der vorherigen Einzel-Eintrag-Ansicht.","15",1,1440,List.of()),
   new AddonConfigurationService.Field("maxMinutes","Minuten je altem Eintrag (Maximum)","number",
       "Kompatibilität mit der vorherigen Einzel-Eintrag-Ansicht.","600",1,1440,List.of()),
   new AddonConfigurationService.Field("allowFutureEntries","Zukünftige Einträge erlauben","boolean",
       "Kompatibilität mit dem bisherigen Berichtsheft.","false",0,0,List.of()),
   new AddonConfigurationService.Field("traineeGroupDns","AD-Gruppen für Azubis","text",
       "Je Zeile eine AD-Gruppe (CN, etwa FIA, oder vollständiger DN). Mehrere Domänen möglich; keine festen Firmengruppen.",
       "",0,0,List.of()),
   new AddonConfigurationService.Field("trainerGroupDns","AD-Gruppen für Ausbilder","text",
       "Je Zeile eine AD-Gruppe (CN, etwa MA, oder vollständiger DN). Diese Konten dürfen erst nach Zuordnung prüfen.",
       "",0,0,List.of()));
 }
}
