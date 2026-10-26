import {test,expect} from '@playwright/test';
import {readFileSync} from 'node:fs';
import {pathToFileURL} from 'node:url';
const fixture = name => JSON.parse(readFileSync(new URL(`../fixtures/${name}.json`,import.meta.url)));
const url=pathToFileURL(new URL('../dist/index.html',import.meta.url).pathname).href;
const render=async(page,model,extra={})=> page.evaluate(value=>window.ArtemisModelReview.renderBase64(btoa(String.fromCharCode(...new TextEncoder().encode(JSON.stringify(value))))),{model,diagramType:model.type,feedback:[],darkMode:false,...extra});
test.beforeEach(async({page})=>{
 page.remoteRequests=[];page.on('request',r=>{if(/^https?:/.test(r.url()))page.remoteRequests.push(r.url());});
 await page.addInitScript(()=>{window.selectionMessages=[]; window.modelErrors=[]; window.ArtemisModelBridge={selectElement:r=>window.selectionMessages.push(r),renderError:e=>window.modelErrors.push(e)}; window.webkit={messageHandlers:{modelSelection:{postMessage:r=>window.selectionMessages.push(r)}}};});
 await page.goto(url); await expect.poll(()=>page.evaluate(()=>window.ArtemisModelReview?.isReady)).toBe(true);
});
for (const name of ['class-diagram','activity-diagram','flowchart','petri-net','bpmn','use-case-diagram','component-diagram','deployment-diagram','object-diagram','communication-diagram','syntax-tree','reachability-graph','sfc']) {
 test(`${name} renders offline with real Apollon nodes and edges`,async({page})=>{
  const requests=page.remoteRequests;
  const model=fixture(name); expect((await render(page,model)).ok).toBe(true);
  await expect(page.locator('.react-flow__node')).toHaveCount(model.nodes.length);
  await expect(page.locator('.react-flow__edge')).toHaveCount(model.edges.length);
  await expect(page.locator('input,textarea,[contenteditable=true],.react-flow__controls')).toHaveCount(0);
  expect(requests).toEqual([]);
  await page.screenshot({path:`proof/${test.info().project.name}-${name}.png`});
 });
}
test('Unicode, selected element and relationship references use the native bridges',async({page})=>{
 const model=fixture('class-diagram'); model.nodes[1].data.name='Größe 😀 中文';
 const id=model.nodes[1].id, edge=model.edges[0].id;
 const feedback=[{reference:`Class:${id}`,credits:1},{reference:`Relationship:${edge}`,credits:-1}];
 await render(page,model,{feedback,selectedReference:`Class:${id}`,darkMode:true});
 await expect(page.getByText('Größe 😀 中文',{exact:true})).toBeVisible();
 await expect(page.locator(`[data-apollon-element-id="${id}"]`).first().locator('..')).toBeVisible();
 await page.getByText('Größe 😀 中文',{exact:true}).click({force:true});
 await expect.poll(()=>page.evaluate(()=>window.selectionMessages)).toContain(`Class:${id}`);
 const edgeSelector=`.react-flow__edge[data-id="${edge}"] .react-flow__edge-interaction`;
 await page.locator(edgeSelector).dispatchEvent('click');
 await expect.poll(()=>page.evaluate(()=>window.selectionMessages)).toContain(`Relationship:${edge}`);
 await page.screenshot({path:`proof/${test.info().project.name}-selected-dark.png`});
});
test('editing keys, drag, paste, double click and drop preserve node content and positions',async({page})=>{
 const model=fixture('class-diagram');await render(page,model);
 await expect(page.locator('.react-flow__node')).toHaveCount(model.nodes.length);
 const before=await page.locator('.react-flow__node').evaluateAll(nodes=>nodes.map(n=>[n.dataset.id,n.style.transform,n.textContent]));
 const node=page.locator('.react-flow__node').nth(1);await node.click({force:true});
 for(const key of ['Delete','Backspace','ArrowRight','Control+c','Control+v','Control+x','Control+z','Enter']) await page.keyboard.press(key);
 await node.dblclick({force:true});
 await node.dispatchEvent('paste');await node.dispatchEvent('drop');
 const box=await node.boundingBox();await page.mouse.move(box.x+15,box.y+15);await page.mouse.down();await page.mouse.move(box.x+85,box.y+65,{steps:5});await page.mouse.up();
 expect(await page.locator('.react-flow__node').evaluateAll(nodes=>nodes.map(n=>[n.dataset.id,n.style.transform,n.textContent]))).toEqual(before);
 await expect(page.locator('input,textarea,[contenteditable=true]')).toHaveCount(0);
});
test('invalid payload replaces the canvas with an accessible error and can recover',async({page})=>{
 await render(page,fixture('activity-diagram'));
 expect((await page.evaluate(()=>window.ArtemisModelReview.renderBase64('%%%'))).ok).toBe(false);
 await expect(page.getByRole('alert')).toBeVisible();await expect(page.locator('.react-flow__node')).toHaveCount(0);
 expect(await page.evaluate(()=>window.modelErrors.length)).toBe(1);
 await page.screenshot({path:`proof/${test.info().project.name}-error.png`});
 expect((await render(page,fixture('activity-diagram'))).ok).toBe(true);
 await expect(page.getByRole('alert')).toBeHidden();
});
test('pan and zoom change viewport while selection updates do not reload the diagram',async({page})=>{
 const model=fixture('activity-diagram');await render(page,model);
 const viewport=page.locator('.react-flow__viewport');await expect(page.locator('.react-flow__node')).toHaveCount(model.nodes.length);
 const before=await viewport.getAttribute('style');
 await page.mouse.move(400,300);await page.mouse.wheel(0,-150);
 await expect.poll(()=>viewport.getAttribute('style')).not.toBe(before);
 const afterZoom=await viewport.getAttribute('style');
 await page.mouse.move(20,20);await page.mouse.down();await page.mouse.move(70,60,{steps:5});await page.mouse.up();
 await expect.poll(()=>viewport.getAttribute('style')).not.toBe(afterZoom);
});

test('theme and selected-reference updates retain the mounted model and viewport',async({page})=>{
 const model=fixture('activity-diagram');await render(page,model);
 await expect(page.locator('.react-flow__node')).toHaveCount(model.nodes.length);
 await page.evaluate(()=>document.querySelector('.react-flow__node').dataset.proof='same-mount');
 await render(page,model,{darkMode:true,selectedReference:model.nodes[0].id});
 await expect(page.locator('#canvas')).toHaveAttribute('data-theme','dark');
 await expect(page.locator('.react-flow__node').first()).toHaveAttribute('data-proof','same-mount');
});
test('CSP blocks network requests and arbitrary link navigation',async({page})=>{
 const before=page.url();
 const fetched=await page.evaluate(async()=>{try{await fetch('https://example.com/never-send-model-data');return true;}catch{return false;}});
 expect(fetched).toBe(false);
 await page.evaluate(()=>{const a=document.createElement('a');a.href='https://example.com';a.textContent='External';document.body.append(a);a.click();});
 expect(page.url()).toBe(before);
});
test('empty model shows a readable state, unsupported model and cyclic containment fail closed',async({page})=>{
 const empty={id:'empty',version:'4.2.0',type:'ClassDiagram',nodes:[],edges:[],assessments:{}};
 expect((await render(page,empty)).ok).toBe(true);await expect(page.locator('#status')).toContainText('no model elements');
 expect((await render(page,{type:'ClassDiagram',version:'not-supported'})).ok).toBe(false);
 const model=fixture('class-diagram');model.nodes[0].parentId=model.nodes[1].id;
 expect((await render(page,model)).ok).toBe(false);
});

test('selected read-only nodes never expose connection or resize handles',async({page})=>{
 const model=fixture('class-diagram');await render(page,model,{selectedReference:model.nodes[1].id});
 await expect(page.locator('.react-flow__node')).toHaveCount(model.nodes.length);
 expect(await page.locator('.react-flow__handle').evaluateAll(nodes=>nodes.every(node=>getComputedStyle(node).opacity==='0'))).toBe(true);
});

for(const name of ['legacy-v2-class','legacy-v3-class']) test(`${name} import preserves node, class-member and relationship references`,async({page})=>{
 const model=fixture(name);expect((await render(page,model,{feedback:[{reference:'Class:review-class-person',credits:1},{reference:'ClassAttribute:review-attribute-name',credits:1},{reference:'ClassUnidirectional:review-enrolls',credits:-1}],selectedReference:'Class:review-class-person'})).ok).toBe(true);
 await expect(page.locator('.react-flow__node')).toHaveCount(2);await expect(page.locator('.react-flow__edge')).toHaveCount(1);
 await page.getByText('+ name: String',{exact:true}).click({force:true});
 await expect.poll(()=>page.evaluate(()=>window.selectionMessages)).toContain('ClassAttribute:review-attribute-name');
 expect(page.remoteRequests).toEqual([]);
});
test('unsupported interface endpoint handle reports unavailable instead of silently losing a relationship',async({page})=>{
 const model=fixture('deployment-diagram-upstream');
 const result=await render(page,model);
 expect(result.ok).toBe(false);
 expect(result.error).toContain('unsupported connection handle');
 await expect(page.getByRole('alert')).toContainText('cannot be displayed completely');
 await expect(page.locator('.react-flow__node,.react-flow__edge')).toHaveCount(0);
 expect(await page.evaluate(()=>window.modelErrors)).toEqual([result.error]);
});
