package de.nikoit.sibyl.addons;
import java.util.List;
import org.springframework.stereotype.Component;
@Component
public class ReportBookAddonProvider implements SibylAddonProvider {
 @Override public String type(){return "report-book";}
 @Override public List<AddonConfigurationService.Field> settings(){
  return List.of(
   new AddonConfigurationService.Field("minMinutes","Minuten je Eintrag (Minimum)","number","Untergrenze für einen Ausbildungsnachweis.","15",1,1440,List.of()),
   new AddonConfigurationService.Field("maxMinutes","Minuten je Eintrag (Maximum)","number","Obergrenze für einen Ausbildungsnachweis.","600",1,1440,List.of()),
   new AddonConfigurationService.Field("allowFutureEntries","Zukünftige Einträge erlauben","boolean","Freischalten, wenn Nachweise im Voraus erfasst werden dürfen.","false",0,0,List.of()));
 }
}
