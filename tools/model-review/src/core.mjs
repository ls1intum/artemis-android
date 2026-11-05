export const DIAGRAM_TYPES = ['ClassDiagram','ObjectDiagram','ActivityDiagram','UseCaseDiagram','CommunicationDiagram','ComponentDiagram','DeploymentDiagram','PetriNet','ReachabilityGraph','SyntaxTree','Flowchart','BPMN','Sfc'];
const record = value => value !== null && typeof value === 'object' && !Array.isArray(value);
export function decodePayload(base64) {
  if (typeof base64 !== 'string' || base64.length === 0 || base64.length > 12 * 1024 * 1024 || base64.length % 4 !== 0 || !/^[A-Za-z0-9+/]*={0,2}$/.test(base64)) throw Error('Invalid model data.');
  const bytes = Uint8Array.from(atob(base64), char => char.charCodeAt(0));
  const payload = JSON.parse(new TextDecoder('utf-8', {fatal:true}).decode(bytes));
  if (!record(payload) || !record(payload.model) || !DIAGRAM_TYPES.includes(payload.diagramType) || (payload.feedback !== undefined && !Array.isArray(payload.feedback)) || (payload.selectedReference !== undefined && payload.selectedReference !== null && typeof payload.selectedReference !== 'string') || typeof payload.darkMode !== 'boolean') throw Error('Invalid model review payload.');
  payload.feedback ??= [];
  if (payload.feedback.some(item => !record(item) || (item.reference !== undefined && item.reference !== null && typeof item.reference !== 'string'))) throw Error('Invalid feedback data.');
  return payload;
}
export function buildReferenceIndex(model, feedback) {
  const ids = new Set();
  const collect = value => {
    if (!value || typeof value !== 'object') return;
    if (typeof value.id === 'string') ids.add(value.id);
    for (const item of Object.values(value)) if (item && typeof item === 'object') collect(item);
  };
  for (const node of model.nodes ?? []) collect(node);
  for (const edge of model.edges ?? []) collect(edge);
  const index = {ids, originalById:new Map()};
  for (const item of feedback) {
    const id = resolveElementId(item.reference, index);
    if (id && !index.originalById.has(id)) index.originalById.set(id, item.reference);
  }
  return index;
}
export function resolveElementId(reference, index) {
  if (typeof reference !== 'string') return null;
  if (index.ids.has(reference)) return reference;
  const separator = reference.indexOf(':');
  const suffix = separator >= 0 ? reference.slice(separator + 1) : '';
  return index.ids.has(suffix) ? suffix : null;
}
export function feedbackHighlights(feedback, selectedReference, index) {
  const highlights = new Map();
  for (const item of feedback) {
    const id = resolveElementId(item.reference, index);
    if (id) highlights.set(id, item.credits < 0 || item.positive === false ? '#b42318' : item.credits > 0 || item.positive === true ? '#15803d' : '#a16207');
  }
  const selected = resolveElementId(selectedReference, index);
  if (selected) highlights.set(selected, '#2563eb');
  return highlights;
}
export function notifySelection(id, index, host) {
  if (!index.ids.has(id)) return;
  const reference = index.originalById.get(id) ?? id;
  try { host.ArtemisModelBridge?.selectElement?.(reference); } catch {}
  try { host.webkit?.messageHandlers?.modelSelection?.postMessage({reference}); } catch {}
}
