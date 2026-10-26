import test from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import vm from 'node:vm';

const path = new URL('../../../feature/exercise-view/src/main/assets/model-review-android.js', import.meta.url);
function setup() {
  const frames = new Map(); let id = 0, observer, repairs = 0, zoom = 'matrix(0.4,0,0,0.4,0,0)';
  const style = {value:'inline', priority:'', getPropertyValue(){return this.value;}, getPropertyPriority(){return this.priority;}, setProperty(_,v,p){this.value=v;this.priority=p||'';},removeProperty(){this.value='';this.priority='';}};
  const svg = {style}; const viewport = {classList:{contains: n=>n==='react-flow__viewport'}};
  const canvas = {querySelector:()=>viewport,querySelectorAll:()=>[svg]};
  const fonts = {ready:Promise.resolve(),addEventListener(){},removeEventListener(){}};
  const document = {fonts,body:{get offsetHeight(){assert.equal(style.value,'none');repairs++;return 520;}}};
  const context = {window:{},document,requestAnimationFrame:fn=>{frames.set(++id,fn);return id;},cancelAnimationFrame:i=>frames.delete(i),getComputedStyle:()=>({transform:zoom}),MutationObserver:class{constructor(fn){observer=this;this.notify=fn;}observe(){}disconnect(){this.disconnected=true;}}};
  vm.runInNewContext(fs.readFileSync(path,'utf8'),context);
  const run = ()=>{const pending=[...frames.values()];frames.clear();pending.forEach(fn=>fn());};
  return {api:context.window.ArtemisNativeSvgRepair,canvas,svg,viewport,frames,run,observer:()=>observer,repairs:()=>repairs,setZoom:v=>zoom=v};
}

test('repairs SVG layout without changing authored display or scheduling its own mutation loop',()=>{
  const s=setup();s.api.attach(s.canvas);s.run();assert.equal(s.repairs(),1);assert.equal(s.svg.style.value,'inline');
  s.observer().notify([{type:'attributes',target:s.svg,attributeName:'style'}]);s.run();assert.equal(s.repairs(),1);
});
test('recomputes glyph metrics for changed zoom but preserves pan and selection-only layout',()=>{
  const s=setup();s.api.attach(s.canvas);s.run();s.setZoom('matrix(0.4,0,0,0.4,10,20)');s.observer().notify([{type:'attributes',target:s.viewport,attributeName:'style'}]);s.run();assert.equal(s.repairs(),1);
  s.setZoom('matrix(0.7,0,0,0.7,10,20)');s.observer().notify([{type:'attributes',target:s.viewport,attributeName:'style'}]);s.run();assert.equal(s.repairs(),2);
});
test('disposing disconnects the observer and cancels pending animation work',()=>{
  const s=setup();s.api.attach(s.canvas);s.api.dispose();s.run();assert.equal(s.repairs(),0);assert.equal(s.observer().disconnected,true);assert.equal(s.frames.size,0);
});
