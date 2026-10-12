'use strict';
const $=id=>document.getElementById(id);
const node=(tag,value)=>{const x=document.createElement(tag);x.textContent=String(value??'');return x;};
let reports=[],selected=null,entries=[],nextWorker=0;
function msg(text){$('status').textContent=text;}
const mondayOf=day=>{const d=new Date(day+'T12:00:00');d.setDate(d.getDate()-((d.getDay()+6)%7));return d.toISOString().slice(0,10);};
const today=new Date().toISOString().slice(0,10);
$('monday').value=mondayOf(today);$('day').value=today;
function render(){
 $('weeks').replaceChildren();
 for(const r of reports){
  const btn=node('button',r.value.week+' · '+r.value.state);btn.className='week'+(r.id===selected?.id?' active':'');
  btn.onclick=()=>open(r);$('weeks').append(btn);
 }
 $('entries').replaceChildren();
 for(const [i,e] of entries.entries()){
  const tr=document.createElement('tr');
  for(const s of [e.day,e.minutes,e.category,e.description])tr.append(node('td',s));
  const cell=document.createElement('td'),del=node('button','×');
  del.type='button';del.title='Eintrag entfernen';del.onclick=()=>{entries.splice(i,1);render();};
  cell.append(del);tr.append(cell);$('entries').append(tr);
 }
 $('total').textContent='Gesamt: '+entries.reduce((sum,x)=>sum+x.minutes,0)+' Minuten';
}
function open(record){
 selected=record;entries=structuredClone(record.value.entries||[]);
 $('monday').value=record.value.week;
 $('state').value=record.value.state||'DRAFT';
 $('day').value=record.value.week;render();
}
async function refresh(id){
 reports=await SibylBridge.list('weeks');
 selected=null;entries=[];$('monday').value=mondayOf(today);$('state').value='DRAFT';
 open(reports.find(r=>r.id===id)||reports[0]||{value:{week:mondayOf(today),state:'DRAFT',entries:[]}});
 if(!id&&!reports.length)selected=null;
}
$('new-week').onclick=()=>{
 selected=null;entries=[];$('monday').value=mondayOf(today);$('day').value=today;$('state').value='DRAFT';render();msg('Neuer Wochenbericht.');
};
$('add-entry').onclick=()=>{
 const day=$('day').value,minutes=Number($('minutes').value),start=$('monday').value;
 const category=$('category').value.trim(),description=$('description').value.trim();
 if(!start||mondayOf(start)!==start||!day||day<start||
   day>new Date(new Date(start+'T12:00:00').getTime()+6*86400000).toISOString().slice(0,10)||
   !Number.isInteger(minutes)||minutes<15||minutes>720||minutes%15!==0||!category||!description){
  msg('Ungültige Woche, Datum, Zeit oder Tätigkeitsbeschreibung.');return;}
 entries.push({day,minutes,category,description});render();msg('Tätigkeit hinzugefügt; zum Speichern bitte „Woche speichern“ wählen.');
};
$('save').onclick=async()=>{
 const week=$('monday').value;
 if(!week||mondayOf(week)!==week){msg('Woche muss an einem Montag beginnen.');return;}
 if(entries.some(e=>e.day<week||e.day>new Date(new Date(week+'T12:00:00').getTime()+6*86400000).toISOString().slice(0,10))){
  msg('Eine Tätigkeit liegt außerhalb der Berichtswoche.');return;}
 const value={week,entries,state:$('state').value};
 if(!selected&&reports.some(r=>r.value.week===week)){msg('Für diese Woche existiert bereits ein Bericht.');return;}
 try{
  const result=selected?await SibylBridge.update('weeks',selected.id,selected.version,value):
   await SibylBridge.create('weeks',value);
  await refresh(result.id);msg('Dauerhaft gespeichert · Version '+result.version);
 }catch(e){msg('Speichern fehlgeschlagen: '+e.message);}
};
$('archive').onclick=async()=>{
 if(!selected||!confirm('Bericht archivieren?'))return;
 try{await SibylBridge.archive('weeks',selected.id,selected.version);await refresh(null);msg('Bericht archiviert.')}
 catch(e){msg(e.message);}
};
$('export').onclick=()=>{
 let output='# Ausbildungsbericht – '+$('monday').value+'\n\n| Datum | Minuten | Fachbereich | Tätigkeit |\n|---|---:|---|---|\n';
 const clean=s=>String(s).replace(/\r?\n/g,' ').replace(/\|/g,'\\|');
 for(const e of entries)output+='| '+e.day+' | '+e.minutes+' | '+clean(e.category)+' | '+clean(e.description)+' |\n';
 $('markdown').value=output+'\nGesamt: '+entries.reduce((sum,e)=>sum+e.minutes,0)+' Minuten\n';
 msg('Markdown erzeugt. Die Ausgabe kann kopiert werden.');
};
$('calculate').onclick=()=>{
 const requestId=++nextWorker;
 parent.postMessage({sibyl:'invoke',requestId,method:'totalMinutes',
  payload:{minutes:entries.map(e=>e.minutes)}},'*');
 msg('Validiere über den isolierten Java-Worker …');
};
window.addEventListener('message',e=>{
 if(e.source!==parent||e.data?.sibyl!=='response'||e.data.requestId!==nextWorker)return;
 msg(e.data.error?'Backend: '+e.data.error:e.data.data?.ok?
  'Worker bestätigt '+e.data.data.totalMinutes+' Minuten.':
  'Worker: '+(e.data.data?.error||'Unbekannter Fehler'));
});
refresh(null).catch(e=>msg('Datenbank nicht verfügbar: '+e.message));