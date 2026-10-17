package de.nikoit.sibyl.addons;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/addons/Sibyl.report-book")
public class ReportBookController {
 public record Entry(long id,String ownerUsername,String entryDate,String title,String description,int minutesSpent,
     String status,String reviewer,String reviewFeedback,String updatedAt) {}
 public record EntryInput(String entryDate,String title,String description,Integer minutesSpent) {}
 public record ReviewInput(String decision,String feedback) {}
 private final JdbcTemplate db;
 private final AddonRegistry addons;
 private final AddonConfigurationService config;
 private final AuditService audit;
 public ReportBookController(JdbcTemplate db,AddonRegistry addons,AddonConfigurationService config,AuditService audit) {
  this.db=db;this.addons=addons;this.config=config;this.audit=audit;
 }
 private void check(Authentication auth){addons.accessible("Sibyl.report-book",auth);}
 private void admin(Authentication auth){check(auth);if(!addons.isAdmin(auth))
  throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Admin required");}
 private final String select="SELECT id,owner_username,entry_date,title,description,minutes_spent,status,reviewer,review_feedback,updated_at FROM sibyl_report_entries ";
 private List<Entry> query(String sql,Object...args){
  return db.query(sql,(rs,i)->new Entry(rs.getLong(1),rs.getString(2),rs.getDate(3).toLocalDate().toString(),
   rs.getString(4),rs.getString(5),rs.getInt(6),rs.getString(7),rs.getString(8),rs.getString(9),
   rs.getTimestamp(10).toInstant().toString()),args);
 }
 private Entry load(long id){
  return query(select+"WHERE id=?",id).stream().findFirst()
   .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Entry not found"));
 }
 private void validate(EntryInput input){
  if(input==null||input.title()==null||input.title().isBlank()||input.title().length()>160||
    input.description()==null||input.description().isBlank()||input.description().length()>15000||
    input.minutesSpent()==null)
   throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid report entry");
  LocalDate date;
  try{date=LocalDate.parse(input.entryDate());}
  catch(DateTimeParseException|NullPointerException ex){throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid date");}
  if(date.isBefore(LocalDate.now().minusYears(4))||date.isAfter(LocalDate.now().plusYears(1)))
   throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Date out of range");
  if(!config.boolValue("Sibyl.report-book","allowFutureEntries")&&date.isAfter(LocalDate.now()))
   throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Future reports disabled");
  int minutes=input.minutesSpent();
  if(minutes<config.integerValue("Sibyl.report-book","minMinutes")||
     minutes>config.integerValue("Sibyl.report-book","maxMinutes"))
   throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Duration out of allowed range");
 }
 @GetMapping("/entries")
 public List<Entry> list(Authentication auth){
  check(auth);
  if(addons.isAdmin(auth))return query(select+"ORDER BY entry_date DESC,id DESC LIMIT 500");
  return query(select+"WHERE owner_username=? ORDER BY entry_date DESC,id DESC LIMIT 250",auth.getName());
 }
 @PostMapping("/entries")
 @Transactional
 public void create(@RequestBody EntryInput input,Authentication auth){
  check(auth);validate(input);
  db.update("INSERT INTO sibyl_report_entries(owner_username,entry_date,title,description,minutes_spent) VALUES(?,?,?,?,?)",
   auth.getName(),java.sql.Date.valueOf(input.entryDate()),input.title().trim(),input.description(),input.minutesSpent());
  audit.record(auth.getName(),"REPORT_CREATE",input.entryDate());
 }
 @PutMapping("/entries/{id}")
 @Transactional
 public void update(@PathVariable long id,@RequestBody EntryInput input,Authentication auth){
  check(auth);validate(input);
  Entry previous=load(id);
  if(!previous.ownerUsername().equals(auth.getName())||!List.of("DRAFT","RETURNED").contains(previous.status()))
   throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Only your editable reports can be changed");
  db.update("UPDATE sibyl_report_entries SET entry_date=?,title=?,description=?,minutes_spent=?,status='DRAFT',review_feedback=NULL,reviewer=NULL,reviewed_at=NULL WHERE id=?",
   java.sql.Date.valueOf(input.entryDate()),input.title().trim(),input.description(),input.minutesSpent(),id);
  audit.record(auth.getName(),"REPORT_EDIT",Long.toString(id));
 }
 @DeleteMapping("/entries/{id}")
 @Transactional
 public void delete(@PathVariable long id,Authentication auth){
  check(auth);Entry e=load(id);
  if(!e.ownerUsername().equals(auth.getName())||!List.of("DRAFT","RETURNED").contains(e.status()))
   throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Report is locked");
  db.update("DELETE FROM sibyl_report_entries WHERE id=?",id);
  audit.record(auth.getName(),"REPORT_DELETE",Long.toString(id));
 }
 @PostMapping("/entries/{id}/submit")
 @Transactional
 public void submit(@PathVariable long id,Authentication auth){
  check(auth);Entry e=load(id);
  if(!e.ownerUsername().equals(auth.getName())||!List.of("DRAFT","RETURNED").contains(e.status()))
   throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Cannot submit this report");
  db.update("UPDATE sibyl_report_entries SET status='SUBMITTED',reviewer=NULL,review_feedback=NULL,reviewed_at=NULL WHERE id=?",id);
  audit.record(auth.getName(),"REPORT_SUBMIT",Long.toString(id));
 }
 @PostMapping("/entries/{id}/review")
 @Transactional
 public void review(@PathVariable long id,@RequestBody ReviewInput input,Authentication auth){
  admin(auth);Entry e=load(id);
  if(e.ownerUsername().equalsIgnoreCase(auth.getName()))
   throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Self review denied");
  if(!"SUBMITTED".equals(e.status())||input==null||
    !List.of("APPROVED","RETURNED").contains(input.decision())||
    (input.feedback()!=null&&input.feedback().length()>1000)||
    ("RETURNED".equals(input.decision())&&(input.feedback()==null||input.feedback().isBlank())))
   throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid review");
  db.update("UPDATE sibyl_report_entries SET status=?,reviewer=?,review_feedback=?,reviewed_at=CURRENT_TIMESTAMP WHERE id=?",
    input.decision(),auth.getName(),input.feedback()==null?"":input.feedback().trim(),id);
  audit.record(auth.getName(),"REPORT_REVIEW",id+":"+input.decision());
 }
}
