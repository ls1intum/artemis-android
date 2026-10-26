import test from 'node:test';
import assert from 'node:assert/strict';
import {decodePayload, buildReferenceIndex, resolveElementId, feedbackHighlights, notifySelection} from '../src/core.mjs';
const model = {version:'4.2.0',type:'ClassDiagram',nodes:[{id:'class-ä',data:{attributes:[{id:'attribute-1',name:'Größe 😀'}]}}],edges:[{id:'edge-1'}]};
const encode = value => Buffer.from(JSON.stringify(value),'utf8').toString('base64');
const payload = () => ({model,diagramType:'ClassDiagram',feedback:[{reference:'Class:class-ä',credits:1},{reference:'Association:edge-1',credits:-1}],selectedReference:'Class:class-ä',darkMode:true});
test('UTF-8 base64 preserves non-Latin names, identifiers and original feedback references', () => {
 const result=decodePayload(encode(payload()));
 assert.equal(result.model.nodes[0].data.attributes[0].name,'Größe 😀');
 assert.equal(result.feedback[0].reference,'Class:class-ä');
 assert.equal(result.selectedReference,'Class:class-ä');
});
test('malformed base64, invalid UTF-8, JSON and unsupported payloads fail closed',()=>{
 for(const value of ['%%%','/w==',encode(null),encode({}),encode({...payload(),model:[]}),encode({...payload(),diagramType:'not-a-diagram'}),encode({...payload(),feedback:{}}),Buffer.from('{').toString('base64')]) assert.throws(()=>decodePayload(value));
});
test('feedback references resolve nodes, members and relationships without rewriting identifiers',()=>{
 const index=buildReferenceIndex(model,payload().feedback);
 assert.equal(resolveElementId('Class:class-ä',index),'class-ä');
 assert.equal(resolveElementId('Association:edge-1',index),'edge-1');
 assert.equal(resolveElementId('attribute-1',index),'attribute-1');
 assert.equal(resolveElementId('Unknown:bad',index),null);
});
test('selection highlight overrides feedback color, and does not mutate source data',()=>{
 const input=payload(); const before=JSON.stringify(input);
 const index=buildReferenceIndex(model,input.feedback);
 const colors=feedbackHighlights(input.feedback,input.selectedReference,index);
 assert.equal(colors.get('class-ä'),'#2563eb');
 assert.equal(colors.get('edge-1'),'#b42318');
 assert.equal(JSON.stringify(input),before);
});
test('Android and iOS receive the full matched reference and raw unassessed identifiers',()=>{
 const android=[],ios=[];
 const host={ArtemisModelBridge:{selectElement:reference=>android.push(reference)},webkit:{messageHandlers:{modelSelection:{postMessage:value=>ios.push(value)}}}};
 const index=buildReferenceIndex(model,payload().feedback);
 notifySelection('class-ä',index,host); notifySelection('attribute-1',index,host); notifySelection('no-such-id',index,host);
 assert.deepEqual(android,['Class:class-ä','attribute-1']);
 assert.deepEqual(ios,[{reference:'Class:class-ä'},{reference:'attribute-1'}]);
});
test('missing or throwing native bridge never breaks the canvas',()=>{
 const index=buildReferenceIndex(model,[]);
 assert.doesNotThrow(()=>notifySelection('class-ä',index,{}));
 assert.doesNotThrow(()=>notifySelection('class-ä',index,{ArtemisModelBridge:{selectElement(){throw Error('gone')}}}));
});
