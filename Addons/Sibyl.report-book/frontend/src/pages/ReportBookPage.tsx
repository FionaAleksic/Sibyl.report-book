import {useCallback,useEffect,useState} from 'react';
import {request} from '../services/api';
import type {Credentials} from '../types/domain';
type Entry={id:number;ownerUsername:string;entryDate:string;title:string;description:string;minutesSpent:number;
 status:'DRAFT'|'SUBMITTED'|'APPROVED'|'RETURNED';reviewer:string|null;reviewFeedback:string|null;updatedAt:string};
type Draft={entryDate:string;title:string;description:string;minutesSpent:number};
const endpoint='/api/v1/addons/Sibyl.report-book/entries';
const dateNow=()=>new Date().toLocaleDateString('en-CA');
const empty=():Draft=>({entryDate:dateNow(),title:'',description:'',minutesSpent:60});
const explain=(e:unknown)=>e instanceof Error?e.message:'Anfrage fehlgeschlagen';
const status:Record<string,string>={DRAFT:'Entwurf',SUBMITTED:'Eingereicht',APPROVED:'Freigegeben',RETURNED:'Zurückgegeben'};
export default function ReportBookPage({credentials,admin}:{credentials:Credentials;admin:boolean}){
 const [entries,setEntries]=useState<Entry[]>([]);
 const [form,setForm]=useState<Draft>(empty);
 const [editing,setEditing]=useState<number|null>(null);
 const [feedback,setFeedback]=useState<Record<number,string>>({});
 const [busy,setBusy]=useState(false);
 const [error,setError]=useState('');
 const [notice,setNotice]=useState('');
 const load=useCallback(async()=>setEntries(await request<Entry[]>(endpoint,credentials)),[credentials]);
 useEffect(()=>{void load().catch(e=>setError(explain(e)))},[load]);
 async function run(job:()=>Promise<unknown>,done:string){
  setBusy(true);setError('');setNotice('');
  try{await job();await load();setNotice(done);return true}
  catch(e){setError(explain(e));return false}finally{setBusy(false)}
 }
 async function save(e:React.FormEvent){
  e.preventDefault();
  const ok=await run(()=>request(endpoint+(editing===null?'':'/'+editing),credentials,{
    method:editing===null?'POST':'PUT',body:JSON.stringify(form)}),'Berichtsheft-Eintrag gespeichert.');
  if(ok){setEditing(null);setForm(empty())}
 }
 const canEdit=(entry:Entry)=>entry.ownerUsername.toLowerCase()===credentials.username.toLowerCase()
  &&(entry.status==='DRAFT'||entry.status==='RETURNED');
 return <section className="resource-page"><div className="page-heading"><div>
  <div className="eyebrow">SIBYL / AUSBILDUNG</div><h1>Berichtsheft</h1>
  <p>Ausbildungsnachweise erfassen, einreichen und durch Ausbilder freigeben lassen.</p></div></div>
  {error&&<div role="alert" className="error">{error}</div>}{notice&&<div role="status" className="success-note">{notice}</div>}
  <div className="sibyl-addon-section"><h2>{editing===null?'Neuer Ausbildungsnachweis':'Ausbildungsnachweis bearbeiten'}</h2>
   <form onSubmit={e=>void save(e)} className="sibyl-report-form">
    <div className="sibyl-inline-fields"><label>Datum<input type="date" required value={form.entryDate}
      onChange={e=>setForm(x=>({...x,entryDate:e.target.value}))}/></label>
     <label>Dauer (Minuten)<input type="number" required min={1} max={1440} value={form.minutesSpent}
      onChange={e=>setForm(x=>({...x,minutesSpent:Number(e.target.value)}))}/></label></div>
    <label>Tätigkeit<input type="text" maxLength={160} required value={form.title} onChange={e=>setForm(x=>({...x,title:e.target.value}))}/></label>
    <label>Beschreibung<textarea rows={5} maxLength={15000} required value={form.description}
     onChange={e=>setForm(x=>({...x,description:e.target.value}))}/></label>
    <div className="sibyl-inline-fields"><button className="primary" disabled={busy}>Speichern</button>
     {editing!==null&&<button type="button" className="secondary" onClick={()=>{setEditing(null);setForm(empty())}}>Abbrechen</button>}</div>
   </form>
  </div>
  <div className="sibyl-addon-section"><h2>{admin?'Ausbildungsnachweise und Freigaben':'Meine Einträge'}</h2>
   <button type="button" className="secondary" onClick={()=>void load().catch(e=>setError(explain(e)))}>Aktualisieren</button>
   {entries.length===0?<p>Noch keine Einträge vorhanden.</p>:
    <div className="sibyl-report-items">{entries.map(entry=><article key={entry.id} className="sibyl-report-item">
     <div className="sibyl-inline-fields"><h3>{entry.title}</h3><span>{entry.entryDate} · {entry.minutesSpent} Min. · {status[entry.status]??entry.status}</span></div>
     {admin&&<p>Benutzer: {entry.ownerUsername}</p>}
     <p className="sibyl-report-description">{entry.description}</p>
     {entry.reviewFeedback&&<p><strong>Rückmeldung:</strong> {entry.reviewFeedback}</p>}
     {canEdit(entry)&&<div className="sibyl-inline-fields">
      <button className="secondary" disabled={busy} onClick={()=>{setEditing(entry.id);setForm({
       entryDate:entry.entryDate,title:entry.title,description:entry.description,minutesSpent:entry.minutesSpent});window.scrollTo({top:0,behavior:'smooth'})}}>Bearbeiten</button>
      <button className="primary" disabled={busy} onClick={()=>void run(
       ()=>request(endpoint+'/'+entry.id+'/submit',credentials,{method:'POST'}),'Eintrag eingereicht.')}>Einreichen</button>
      <button className="secondary" disabled={busy} onClick={()=>void run(
       ()=>request(endpoint+'/'+entry.id,credentials,{method:'DELETE'}),'Eintrag gelöscht.')}>Löschen</button>
     </div>}
     {admin&&entry.status==='SUBMITTED'&&entry.ownerUsername.toLowerCase()!==credentials.username.toLowerCase()&&
      <div className="sibyl-review"><label>Prüfvermerk<textarea rows={2} maxLength={1000}
       value={feedback[entry.id]??''} onChange={e=>setFeedback(x=>({...x,[entry.id]:e.target.value}))}/></label>
       <div className="sibyl-inline-fields">
       <button className="primary" disabled={busy} onClick={()=>void run(()=>request(endpoint+'/'+entry.id+'/review',credentials,{
         method:'POST',body:JSON.stringify({decision:'APPROVED',feedback:feedback[entry.id]??''})}),'Eintrag freigegeben.')}>Freigeben</button>
       <button className="secondary" disabled={busy||!(feedback[entry.id]??'').trim()} onClick={()=>void run(
        ()=>request(endpoint+'/'+entry.id+'/review',credentials,{method:'POST',
          body:JSON.stringify({decision:'RETURNED',feedback:feedback[entry.id]??''})}),'Eintrag zurückgegeben.')}>Zurückgeben</button>
       </div>
      </div>}
    </article>)}</div>}
  </div>
 </section>;
}
