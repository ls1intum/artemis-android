import {ApollonEditor, ApollonMode, importDiagram} from '@tumaet/apollon';
import '@tumaet/apollon/style.css';
import './viewer.css';
import {decodePayload,buildReferenceIndex,resolveElementId,feedbackHighlights,notifySelection} from './core.mjs';

const canvas=document.getElementById('canvas');
const status=document.getElementById('status');
let editor=null, current=null, index=null, modelKey=null, generation=0;
const compatibilityError='This model uses an unsupported connection handle and cannot be displayed completely.';
const reportError=(message='The model could not be displayed. Please reopen this result.')=>{
  generation++; editor?.destroy(); editor=null; current=null; index=null; modelKey=null;
  canvas.replaceChildren();status.textContent=message;status.setAttribute('role','alert');status.hidden=false;
  try { window.ArtemisModelBridge?.renderError?.(message); } catch {}
  try { window.webkit?.messageHandlers?.modelError?.postMessage({message}); } catch {}
  return {ok:false,error:message};
};
function validateModel(model) {
  if (!Array.isArray(model.nodes)||!Array.isArray(model.edges)||model.nodes.length>2000||model.edges.length>5000) throw Error('Invalid diagram.');
  const ids=new Set();
  for(const node of model.nodes) {
    if(!node || typeof node.id!=='string' || !node.id || ids.has(node.id) || !node.position || !Number.isFinite(node.position.x)||!Number.isFinite(node.position.y)||!Number.isFinite(node.width)||!Number.isFinite(node.height)||node.width<=0||node.height<=0||!node.data||typeof node.data!=='object') throw Error('Invalid element.');
    ids.add(node.id);
  }
  const parents=new Map(model.nodes.map(node=>[node.id,node.parentId]));
  const nodesById=new Map(model.nodes.map(node=>[node.id,node]));
  for(const node of model.nodes) {
    const visited=new Set([node.id]);let parent=node.parentId;
    while(parent) {if(!parents.has(parent)||visited.has(parent)) throw Error('Invalid containment.');visited.add(parent);parent=parents.get(parent);}
  }
  for(const edge of model.edges) {
    if(!edge||typeof edge.id!=='string'||!edge.id||ids.has(edge.id)||!ids.has(edge.source)||!ids.has(edge.target)) throw Error('Invalid relationship.');
    for(const endpoint of ['source','target']) {
      const node=nodesById.get(edge[endpoint]);
      const handle=edge[`${endpoint}Handle`];
      // Interface symbols expose four cardinal handles; React Flow otherwise silently omits invalid edges.
      if((node.type==='componentInterface'||node.type==='deploymentInterface') && handle!=null && !['top','right','bottom','left'].includes(handle)) throw Error(compatibilityError);
    }
    ids.add(edge.id);
  }
}
function updateSelection(reveal) {
  editor.setElementHighlights(feedbackHighlights(current.feedback,current.selectedReference,index));
  editor.revealAssessment(resolveElementId(current.selectedReference,index),{reveal});
}
function renderBase64(base64) {
  try {
    const payload=decodePayload(base64);
    const key=JSON.stringify([payload.diagramType,payload.model]);
    document.documentElement.dataset.theme=payload.darkMode?'dark':'light';
    canvas.dataset.theme=payload.darkMode?'dark':'light';
    if(key===modelKey && editor) {
      const changed=current.selectedReference!==payload.selectedReference;
      current=payload;index=buildReferenceIndex(editor.model,payload.feedback);
      updateSelection(changed);
      return {ok:true};
    }
    const model=importDiagram(payload.model);validateModel(model);
    // Type is exercise metadata; normalization is performed only on this newly decoded object.
    model.type=payload.diagramType;
    generation++;editor?.destroy();canvas.replaceChildren();
    current=payload;modelKey=key;index=buildReferenceIndex(model,payload.feedback);
    canvas.dataset.theme=payload.darkMode?'dark':'light';
    editor=new ApollonEditor(canvas,{
      model,type:payload.diagramType,mode:ApollonMode.Modelling,readonly:true,
      enablePopups:false,keyboardShortcuts:false,controls:[],scrollLock:false,
      collaborationEnabled:false,tags:false,dataTheme:payload.darkMode?'dark':'light'
    });
    status.hidden=model.nodes.length>0;
    status.setAttribute('role','status');status.textContent='This submission contains no model elements.';
    updateSelection(false);
    const ticket=generation;
    requestAnimationFrame(()=>requestAnimationFrame(()=>{
      if(ticket!==generation||!editor)return;
      editor.fitView({padding:0.16,maxZoom:1,minZoom:0.1,duration:0});
      if(current.selectedReference) updateSelection(true);
    }));
    return {ok:true};
  } catch(error) { return reportError(error?.message===compatibilityError?compatibilityError:undefined); }
}
// Keys and transfer gestures are blocked before Apollon/React Flow can mutate anything.
for(const type of ['keydown','keyup','beforeinput','paste','cut','drop','dragstart','contextmenu','dblclick']) {
  document.addEventListener(type,event=>{event.preventDefault();event.stopImmediatePropagation();},true);
}
document.addEventListener('click',event=>{
  if(event.target.closest?.('a')) {event.preventDefault();event.stopImmediatePropagation();}
},true);
canvas.addEventListener('click',event=>{
  if(!editor||!index)return;
  const target=event.target.closest?.('[data-apollon-element-id],.react-flow__node,.react-flow__edge');
  const id=target?.getAttribute('data-apollon-element-id')??target?.getAttribute('data-id');
  if(!id||!index.ids.has(id))return;
  current.selectedReference=index.originalById.get(id)??id;
  updateSelection(false);notifySelection(id,index,window);
});
window.ArtemisModelReview=Object.freeze({isReady:true,version:'1.0.0',apollonVersion:'5.3.1',renderBase64});
try {window.ArtemisModelBridge?.ready?.();} catch {}
try {window.webkit?.messageHandlers?.modelReady?.postMessage({version:'1.0.0',apollonVersion:'5.3.1'});} catch {}
