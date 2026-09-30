'use strict';

/* =====================================================================
   AutoRent · consola de operaciones
   Cliente de la API REST (Spring Boot). El backend serializa en
   snake_case: id_auto, precio_dia, fecha_inicio, tarjeta_credito...
   Los errores llegan como { fecha, estado, error, mensaje }.
   ===================================================================== */

/* ---------------------------------------------------------------------
   Utilidades DOM / HTML
   --------------------------------------------------------------------- */
const $ = (sel, root = document) => root.querySelector(sel);
const $$ = (sel, root = document) => [...root.querySelectorAll(sel)];

// Texto ya seguro: lo que se interpola dentro de html`` se escapa salvo que sea Raw
class Raw { constructor(s) { this.s = s; } }
const raw = s => new Raw(s);
const esc = v => String(v ?? '').replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
function toHtml(v) {
  if (v == null || v === false) return '';
  if (v instanceof Raw) return v.s;
  if (Array.isArray(v)) return v.map(toHtml).join('');
  return esc(v);
}
function html(strings, ...vals) {
  let out = strings[0];
  vals.forEach((v, i) => { out += toHtml(v) + strings[i + 1]; });
  return new Raw(out);
}
const paint = (el, r) => { el.innerHTML = r instanceof Raw ? r.s : esc(r); };
function attrs(obj) {
  return raw(Object.entries(obj)
    .filter(([, v]) => v !== undefined && v !== null && v !== false)
    .map(([k, v]) => v === true ? k : `${k}="${esc(v)}"`)
    .join(' '));
}

/* ---------------------------------------------------------------------
   Iconos (trazos simples, heredan el color del texto)
   --------------------------------------------------------------------- */
const ICONS = {
  grid: '<rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/>',
  car: '<path d="M3 13l2-5a2 2 0 0 1 2-1h10a2 2 0 0 1 2 1l2 5"/><rect x="3" y="13" width="18" height="5" rx="1.5"/><circle cx="7.5" cy="18" r="1.5"/><circle cx="16.5" cy="18" r="1.5"/>',
  key: '<path d="M21 2l-2 2m-7.61 7.61a5.5 5.5 0 1 1-7.778 7.778 5.5 5.5 0 0 1 7.777-7.777zm0 0L15.5 7.5m0 0l3 3L22 7l-3-3m-3.5 3.5L19 4"/>',
  users: '<path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/>',
  pin: '<path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/>',
  tag: '<path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/><line x1="7" y1="7" x2="7.01" y2="7"/>',
  plus: '<line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>',
  edit: '<path d="M12 20h9"/><path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"/>',
  trash: '<polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>',
  eye: '<path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/>',
  refresh: '<polyline points="23 4 23 10 17 10"/><polyline points="1 20 1 14 7 14"/><path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/>',
  more: '<circle cx="12" cy="12" r="1"/><circle cx="19" cy="12" r="1"/><circle cx="5" cy="12" r="1"/>',
  undo: '<polyline points="9 14 4 9 9 4"/><path d="M20 20v-7a4 4 0 0 0-4-4H4"/>',
  x: '<line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>',
  toggle: '<rect x="1" y="5" width="22" height="14" rx="7"/><circle cx="16" cy="12" r="3"/>',
  clock: '<circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/>',
  sun: '<circle cx="12" cy="12" r="4"/><line x1="12" y1="2" x2="12" y2="4"/><line x1="12" y1="20" x2="12" y2="22"/><line x1="4.93" y1="4.93" x2="6.34" y2="6.34"/><line x1="17.66" y1="17.66" x2="19.07" y2="19.07"/><line x1="2" y1="12" x2="4" y2="12"/><line x1="20" y1="12" x2="22" y2="12"/><line x1="4.93" y1="19.07" x2="6.34" y2="17.66"/><line x1="17.66" y1="6.34" x2="19.07" y2="4.93"/>',
  moon: '<path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"/>',
  check: '<polyline points="20 6 9 17 4 12"/>',
  alert: '<circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>',
  inbox: '<polyline points="22 12 16 12 14 15 10 15 8 12 2 12"/><path d="M5.45 5.11L2 12v6a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2v-6l-3.45-6.89A2 2 0 0 0 16.76 4H7.24a2 2 0 0 0-1.79 1.11z"/>',
  search: '<circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>',
};
const icon = (name, size = 18) => raw(`<svg class="icon" width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">${ICONS[name] || ''}</svg>`);

/* ---------------------------------------------------------------------
   Formato: dinero y fechas (las fechas se tratan como texto AAAA-MM-DD
   para evitar corrimientos por zona horaria)
   --------------------------------------------------------------------- */
const fmtCOP = n => new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 }).format(Number(n) || 0);
const MESES = ['ene', 'feb', 'mar', 'abr', 'may', 'jun', 'jul', 'ago', 'sep', 'oct', 'nov', 'dic'];
const pad = n => String(n).padStart(2, '0');
const isoDate = d => `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
const hoy = () => isoDate(new Date());
const parts = iso => iso.split('-').map(Number);
const addDays = (iso, n) => { const [y, m, d] = parts(iso); return isoDate(new Date(y, m - 1, d + n)); };
const diasEntre = (a, b) => {
  const [y1, m1, d1] = parts(a); const [y2, m2, d2] = parts(b);
  return Math.round((Date.UTC(y2, m2 - 1, d2) - Date.UTC(y1, m1 - 1, d1)) / 86400000);
};
const fmtFecha = iso => { if (!iso) return '—'; const [y, m, d] = parts(iso); return `${d} ${MESES[m - 1]} ${y}`; };
const precioFinal = a => Number(a.precio_dia) * (1 - (Number(a.oferta_porcentaje) || 0) / 100);
const normalizar = s => String(s ?? '').toLowerCase().normalize('NFD').replace(/[̀-ͯ]/g, '');

const PLACEHOLDER_IMG = 'data:image/svg+xml;charset=UTF-8,' + encodeURIComponent(
  `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 200 120"><rect width="200" height="120" fill="#232833"/>
   <path d="M35 78 L50 50 Q55 42 65 42 L135 42 Q145 42 150 50 L165 78" fill="none" stroke="#5b6472" stroke-width="4" stroke-linejoin="round"/>
   <rect x="28" y="76" width="144" height="18" rx="7" fill="#5b6472"/>
   <circle cx="60" cy="96" r="10" fill="#12151A" stroke="#97A0AC" stroke-width="3"/>
   <circle cx="140" cy="96" r="10" fill="#12151A" stroke="#97A0AC" stroke-width="3"/></svg>`);

/* ---------------------------------------------------------------------
   Cliente de la API
   --------------------------------------------------------------------- */
const STORAGE_KEY = 'autorent.apiBase';
// si la página la sirve el propio backend (puerto 8080) se usa la misma dirección
const DEFAULT_API = location.port === '8080' ? '' : 'http://localhost:8080';
let API_BASE = (() => {
  try { const v = localStorage.getItem(STORAGE_KEY); return v !== null ? v : DEFAULT_API; } catch (_) { return DEFAULT_API; }
})();

class ApiError extends Error {
  constructor(status, message) { super(message); this.status = status; }
}

// El mensaje de validación llega como "campo: texto; otroCampo: texto": se deja solo el texto
function parseError(data, status) {
  let msg = data && typeof data === 'object' ? (data.mensaje || data.message) : (typeof data === 'string' ? data : '');
  if (msg) msg = msg.split('; ').map(p => p.replace(/^[A-Za-z_]+: /, '')).join('. ');
  if (!msg) msg = status === 404 ? 'No se encontró el recurso.' : status >= 500 ? 'Error interno del servidor.' : `Error ${status}`;
  return msg;
}

let pendientes = 0;
function progreso(delta) {
  pendientes = Math.max(0, pendientes + delta);
  $('#progress').classList.toggle('on', pendientes > 0);
}

async function api(path, { method = 'GET', body } = {}) {
  progreso(1);
  // si el usuario cambia la dirección del backend mientras la petición está en vuelo,
  // su resultado se descarta para que no pise el estado de conexión ni los datos nuevos
  const base = API_BASE;
  const obsoleta = () => new ApiError(0, 'Petición descartada: cambió la dirección del backend.');
  let res;
  try {
    res = await fetch(base + path, {
      method,
      headers: body !== undefined ? { 'Content-Type': 'application/json' } : undefined,
      body: body !== undefined ? JSON.stringify(body) : undefined,
    });
  } catch (_) {
    if (base !== API_BASE) throw obsoleta();
    setConn(false);
    throw new ApiError(0, `No se pudo conectar con ${base || location.origin}.`);
  } finally {
    progreso(-1);
  }
  if (base !== API_BASE) throw obsoleta();
  setConn(true);
  if (res.status === 204) return null;
  const text = await res.text();
  let data = null;
  try { data = text ? JSON.parse(text) : null; } catch (_) { data = text; }
  if (!res.ok) throw new ApiError(res.status, parseError(data, res.status));
  return data;
}

function setConn(ok) {
  const el = $('#conn-status');
  el.classList.toggle('status--ok', ok);
  el.classList.toggle('status--err', !ok);
  $('.status__label', el).textContent = ok ? 'Conectado' : 'Sin conexión';
  const banner = $('#conn-banner');
  banner.hidden = ok;
  if (!ok) {
    $('.conn').open = true; // deja a mano el campo para corregir la dirección
    banner.textContent = `No se pudo conectar con ${API_BASE || location.origin}. Verificá que el backend esté corriendo y que la dirección de la barra lateral sea correcta (el backend debe permitir este origen por CORS).`;
  }
}

/* ---------------------------------------------------------------------
   Datos compartidos (se cargan una vez y se invalidan al modificar algo)
   --------------------------------------------------------------------- */
const store = { tiendas: [], categorias: [], clientes: [], autos: [], refsLoaded: false, autosLoaded: false };

async function loadRefs() {
  if (store.refsLoaded) return;
  const [tiendas, categorias, clientes] = await Promise.all([api('/tiendas'), api('/categorias'), api('/clientes')]);
  Object.assign(store, { tiendas: tiendas || [], categorias: categorias || [], clientes: clientes || [], refsLoaded: true });
}
async function loadAutosCache() {
  if (store.autosLoaded) return;
  store.autos = (await api('/autos/all')) || [];
  store.autosLoaded = true;
}
const invalidate = () => { store.refsLoaded = false; store.autosLoaded = false; };

const findBy = (list, key, id) => list.find(x => x[key] === Number(id));
const tiendaDe = id => findBy(store.tiendas, 'id_tienda', id);
const categoriaNombre = id => findBy(store.categorias, 'id_categoria', id)?.nombre || `Categoría ${id}`;
const clienteDe = id => findBy(store.clientes, 'id_cliente', id);
const autoDe = id => findBy(store.autos, 'id_auto', id);
const autoNombre = id => { const a = autoDe(id); return a && a.marca ? `${a.marca} ${a.modelo}` : `Auto #${id}`; };
const ciudades = () => [...new Set(store.tiendas.map(t => t.ciudad))].sort((a, b) => a.localeCompare(b, 'es'));

/* ---------------------------------------------------------------------
   Avisos, modales y formularios
   --------------------------------------------------------------------- */
function toast(msg, type = 'ok') {
  const el = document.createElement('div');
  el.className = `toast toast--${type}`;
  paint(el, html`<span class="toast__icon">${icon(type === 'err' ? 'alert' : 'check', 17)}</span>
    <p>${msg}</p><button class="icon-btn" aria-label="Cerrar aviso">${icon('x', 16)}</button>`);
  const close = () => el.remove();
  $('button', el).addEventListener('click', close);
  $('#toasts').append(el);
  setTimeout(close, type === 'err' ? 8000 : 4500);
}

const modal = () => $('#modal');
function abrirModal(contenido, { onClose } = {}) {
  const dlg = modal();
  if (dlg.open) dlg.close();
  paint(dlg, contenido);
  $$('[data-close]', dlg).forEach(b => b.addEventListener('click', () => dlg.close()));
  if (onClose) dlg.addEventListener('close', onClose, { once: true });
  dlg.showModal();
  return dlg;
}

function fieldHtml(f, value) {
  const id = `f-${f.name}`;
  const v = value ?? '';
  let control;
  if (f.type === 'select') {
    control = html`<select ${attrs({ id, name: f.name, required: !!f.required })}>
      ${f.placeholder !== undefined ? html`<option value="">${f.placeholder}</option>` : ''}
      ${f.options.map(o => html`<option value="${o.value}" ${String(o.value) === String(v) ? raw('selected') : ''}>${o.label}</option>`)}
    </select>`;
  } else {
    control = html`<input ${attrs({
      id, name: f.name, type: f.type || 'text', value: v, required: !!f.required, readonly: !!f.readonly,
      placeholder: f.placeholder, min: f.min, max: f.max, step: f.step, maxlength: f.maxlength,
      pattern: f.pattern, title: f.title, list: f.list, autocomplete: 'off',
    })}>`;
  }
  return html`<div class="field ${f.full ? 'full' : ''}">
    <label for="${id}">${f.label}${f.required ? ' *' : ''}</label>
    ${control}
    ${f.hint ? html`<span class="field__hint">${f.hint}</span>` : ''}
  </div>`;
}

// Lee el formulario; los campos vacíos se omiten y los numéricos se convierten a Number
function collect(form, fields) {
  const out = {};
  for (const f of fields) {
    if (f.readonly) continue;
    const v = (form.elements[f.name]?.value ?? '').trim();
    if (v === '') continue;
    out[f.name] = (f.type === 'number' || f.numeric) ? Number(v) : v;
  }
  return out;
}

function abrirFormulario({ title, intro, fields, values = {}, submitLabel = 'Guardar', extra = null, onOpen = null, onSubmit }) {
  const dlg = abrirModal(html`<form class="modal__panel" novalidate>
    <div class="modal__head">
      <h2 id="modal-title">${title}</h2>
      <button type="button" class="icon-btn" data-close aria-label="Cerrar">${icon('x')}</button>
    </div>
    ${intro ? html`<p class="hint">${intro}</p>` : ''}
    <div class="modal__error" role="alert" hidden></div>
    <div class="form-grid">${fields.map(f => fieldHtml(f, values[f.name]))}</div>
    ${extra}
    <div class="modal__foot">
      <button type="button" class="btn btn-ghost" data-close>Cancelar</button>
      <button type="submit" class="btn btn-primary">${submitLabel}</button>
    </div>
  </form>`);
  const form = $('form', dlg);
  const errBox = $('.modal__error', dlg);
  const submit = $('button[type=submit]', dlg);
  form.addEventListener('submit', async ev => {
    ev.preventDefault();
    errBox.hidden = true;
    if (!form.reportValidity()) return;
    submit.disabled = true;
    const label = submit.textContent;
    submit.textContent = 'Guardando…';
    try {
      await onSubmit(collect(form, fields));
      dlg.close();
    } catch (e) {
      errBox.textContent = e.message;
      errBox.hidden = false;
    } finally {
      submit.disabled = false;
      submit.textContent = label;
    }
  });
  const primero = $('input:not([readonly]), select', form);
  if (primero) primero.focus();
  if (onOpen) onOpen(form);
  return dlg;
}

function abrirInfo({ title, body, footer }) {
  return abrirModal(html`<div class="modal__panel">
    <div class="modal__head">
      <h2 id="modal-title">${title}</h2>
      <button type="button" class="icon-btn" data-close aria-label="Cerrar">${icon('x')}</button>
    </div>
    ${body}
    <div class="modal__foot">${footer || html`<button type="button" class="btn btn-ghost" data-close>Cerrar</button>`}</div>
  </div>`);
}

function confirmar({ title, message, confirmLabel = 'Confirmar', danger = false }) {
  return new Promise(resolve => {
    // se resuelve una sola vez: directo desde los botones, y el evento close
    // queda como respaldo para Escape o clic en el fondo
    let resuelto = false;
    const fin = valor => { if (!resuelto) { resuelto = true; resolve(valor); } };
    const dlg = abrirModal(html`<div class="modal__panel">
      <div class="modal__head"><h2 id="modal-title">${title}</h2></div>
      <p>${message}</p>
      <div class="modal__foot">
        <button type="button" class="btn btn-ghost" data-close>Cancelar</button>
        <button type="button" class="btn ${danger ? 'btn-danger' : 'btn-primary'}" data-ok>${confirmLabel}</button>
      </div>
    </div>`, { onClose: () => fin(false) });
    $('[data-ok]', dlg).addEventListener('click', () => { fin(true); dlg.close(); });
    $('[data-close]', dlg).addEventListener('click', () => fin(false));
    $('[data-close]', dlg).focus();
  });
}

/* ---------------------------------------------------------------------
   Piezas de interfaz reutilizables
   --------------------------------------------------------------------- */
const cargando = () => html`<div class="loading" aria-label="Cargando"><div class="skeleton"></div><div class="skeleton" style="width:80%"></div><div class="skeleton" style="width:60%"></div></div>`;
const vacio = (titulo, texto, ic = 'inbox') => html`<div class="empty">
  <span class="empty__icon">${icon(ic, 26)}</span><strong>${titulo}</strong>${texto || ''}</div>`;

// círculo con las iniciales del nombre; el color sale del propio nombre para que sea estable
const AVATAR_COLORES = ['#D9822B', '#3E8E7E', '#5B7FD1', '#9A63C9', '#C9566E', '#6E9B3F', '#C2A12D', '#4B8FB8'];
function avatar(nombre) {
  const txt = String(nombre || '?').trim();
  const iniciales = txt.split(/\s+/).slice(0, 2).map(p => p[0]).join('').toUpperCase();
  let h = 0;
  for (const c of txt) h = (h * 31 + c.charCodeAt(0)) >>> 0;
  return html`<span class="avatar" style="background:${AVATAR_COLORES[h % AVATAR_COLORES.length]}" aria-hidden="true">${iniciales}</span>`;
}

const tarjetasFantasma = (n = 6) => html`${Array.from({ length: n }, () => html`<div class="sk-card">
  <div class="skeleton sk-card__img" style="border-radius:0"></div>
  <div class="sk-card__body"><div class="skeleton" style="width:70%;height:20px"></div>
  <div class="skeleton" style="width:50%"></div><div class="skeleton" style="width:40%;height:24px"></div></div></div>`)}`;
const tabla = (heads, rows) => html`<div class="table-wrap"><table class="table">
  <thead><tr>${heads.map(h => html`<th scope="col" class="${h.cls || ''}">${h.label ?? h}</th>`)}</tr></thead>
  <tbody>${rows}</tbody></table></div>`;
const btnIcono = (name, label, action, id, extra = {}) =>
  html`<button class="icon-btn ${extra.danger ? 'icon-btn--danger' : ''}" ${attrs({ 'data-action': action, 'data-id': id, 'aria-label': label, title: label, disabled: !!extra.disabled })}>${icon(name)}</button>`;

function fillSelect(sel, options, value) {
  paint(sel, html`${options.map(o => html`<option value="${o.value}">${o.label}</option>`)}`);
  sel.value = value ?? '';
  if (sel.value !== String(value ?? '')) sel.value = '';
}

function debounce(fn, ms = 200) {
  let t;
  return (...a) => { clearTimeout(t); t = setTimeout(() => fn(...a), ms); };
}

/* ---------------------------------------------------------------------
   Navegación
   --------------------------------------------------------------------- */
const VIEWS = {
  panel: { titulo: 'Panel', load: loadPanel },
  catalogo: { titulo: 'Catálogo de autos', load: loadCatalogo },
  alquileres: { titulo: 'Alquileres', load: loadAlquileres },
  clientes: { titulo: 'Clientes', load: loadClientes },
  tiendas: { titulo: 'Tiendas', load: loadTiendas },
  categorias: { titulo: 'Categorías', load: loadCategorias },
};
let current = 'panel';

async function route() {
  const name = (location.hash.replace(/^#\/?/, '') || 'panel').split('?')[0];
  current = VIEWS[name] ? name : 'panel';
  $$('.view').forEach(v => v.classList.toggle('active', v.id === `view-${current}`));
  $$('.nav-link').forEach(a => {
    const on = a.dataset.view === current;
    a.classList.toggle('active', on);
    if (on) a.setAttribute('aria-current', 'page'); else a.removeAttribute('aria-current');
  });
  document.title = `${VIEWS[current].titulo} · AutoRent`;
  await refreshCurrent();
}

async function refreshCurrent() {
  try {
    await loadRefs();
    await VIEWS[current].load();
  } catch (e) {
    if (e.status !== 0) toast(e.message, 'err'); // sin conexión ya se avisa en el banner
  }
}

function goTo(view) {
  if (current === view) return refreshCurrent();
  location.hash = `#/${view}`;
}

// Ejecuta una modificación, invalida los datos compartidos y vuelve a pintar la vista
async function mutar(fn, mensaje, despues = refreshCurrent) {
  const r = await fn();
  invalidate();
  if (mensaje) toast(mensaje, 'ok');
  await despues();
  return r;
}

/* =====================================================================
   PANEL
   ===================================================================== */
function kpi(href, ic, label, value, sub, tono = '') {
  return html`<a class="kpi ${tono ? `kpi--${tono}` : ''}" href="${href}">
    <span class="kpi__chip">${icon(ic, 20)}</span>
    <span class="kpi__label">${label}</span>
    <span class="kpi__value">${value}</span>
    <span class="kpi__sub">${sub || raw('&nbsp;')}</span>
  </a>`;
}

// Anillo de proporciones en SVG (sin librerías): partes = [{ valor, color, etiqueta }]
function donut(partes, centro, etiqueta) {
  const R = 46;
  const C = 2 * Math.PI * R;
  const visibles = partes.filter(p => p.valor > 0);
  const total = visibles.reduce((s, p) => s + p.valor, 0) || 1;
  const hueco = visibles.length > 1 ? 3 : 0;
  let acumulado = 0;
  const segmentos = visibles.map(p => {
    const largo = (p.valor / total) * C;
    const seg = html`<circle class="donut__seg" cx="60" cy="60" r="${R}" fill="none" stroke-width="12"
      style="stroke:${p.color}" stroke-dasharray="${Math.max(largo - hueco, 0)} ${C}" stroke-dashoffset="${-acumulado}"/>`;
    acumulado += largo;
    return seg;
  });
  return html`<div class="donut">
    <svg viewBox="0 0 120 120" role="img" aria-label="${etiqueta}">
      <circle class="donut__track" cx="60" cy="60" r="${R}" fill="none" stroke-width="12"/>${segmentos}
    </svg>
    <div class="donut__center"><span class="donut__value">${centro}</span><span class="donut__label">${etiqueta}</span></div>
  </div>`;
}

function saludo() {
  const h = new Date().getHours();
  return h < 12 ? 'Buenos días' : h < 19 ? 'Buenas tardes' : 'Buenas noches';
}

function pintarHero(resumen) {
  const fecha = new Date().toLocaleDateString('es-CO', { weekday: 'long', day: 'numeric', month: 'long' });
  paint($('#panel-hero'), html`
    <svg class="hero__art" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width=".45" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">${raw(ICONS.car)}</svg>
    <div class="hero__body">
      <span class="eyebrow">${fecha}</span>
      <h1>${saludo()}, <span>bienvenido</span> a AutoRent</h1>
      <p class="hero__sub">${resumen || 'Cargando el resumen de la operación…'}</p>
      <div class="actions">
        <button class="btn btn-primary" data-action="new-alquiler">${icon('plus')}Nuevo alquiler</button>
        <button class="btn btn-ghost" data-action="new-auto">${icon('car')}Registrar auto</button>
        <button class="btn btn-ghost" data-action="new-cliente">${icon('users')}Registrar cliente</button>
      </div>
    </div>`);
}

async function loadPanel() {
  pintarHero();
  paint($('#panel-flota'), cargando());
  paint($('#panel-categorias'), cargando());
  paint($('#panel-proximas'), cargando());
  paint($('#panel-ofertas'), cargando());
  const [autos, activos] = await Promise.all([api('/autos/all'), api('/alquileres/activos')]);
  store.autos = autos || [];
  store.autosLoaded = true;
  const total = store.autos.length;
  const disponibles = store.autos.filter(a => a.disponibilidad).length;
  const alquilados = total - disponibles;
  const listaActivos = activos || [];

  pintarHero(html`Hay <strong>${listaActivos.length} ${listaActivos.length === 1 ? 'alquiler activo' : 'alquileres activos'}</strong> y <strong>${disponibles} de ${total} autos</strong> disponibles para rentar hoy.`);

  paint($('#kpis'), html`
    ${kpi('#/catalogo', 'car', 'Autos en flota', total, `${disponibles} disponibles · ${alquilados} alquilados`)}
    ${kpi('#/alquileres', 'key', 'Alquileres activos', listaActivos.length, 'Autos que siguen fuera', 'green')}
    ${kpi('#/clientes', 'users', 'Clientes', store.clientes.length, 'Registrados', 'blue')}
    ${kpi('#/tiendas', 'pin', 'Tiendas', store.tiendas.length, `${ciudades().length} ${ciudades().length === 1 ? 'ciudad' : 'ciudades'}`, 'rose')}
    ${kpi('#/categorias', 'tag', 'Categorías', store.categorias.length, 'Clasifican el inventario')}`);

  // ---- estado de la flota (anillo) ----
  paint($('#panel-flota'), total
    ? html`<div class="flota">
        ${donut([
      { valor: disponibles, color: 'var(--success)' },
      { valor: alquilados, color: 'var(--accent)' },
    ], `${Math.round((disponibles / total) * 100)}%`, 'disponible')}
        <div class="legend">
          <div class="legend__item"><span class="legend__dot" style="background:var(--success)"></span>Disponibles<strong>${disponibles}</strong></div>
          <div class="legend__item"><span class="legend__dot" style="background:var(--accent)"></span>Alquilados<strong>${alquilados}</strong></div>
          <div class="legend__item"><span class="legend__dot" style="background:var(--border-strong)"></span>Total flota<strong>${total}</strong></div>
        </div>
      </div>`
    : vacio('Sin autos todavía', 'Registrá el primer auto para ver el estado de la flota.', 'car'));

  // ---- autos por categoría (barras) ----
  const porCategoria = store.categorias
    .map(c => ({ nombre: c.nombre, n: store.autos.filter(a => a.id_categoria === c.id_categoria).length }))
    .sort((a, b) => b.n - a.n);
  const maximo = Math.max(1, ...porCategoria.map(c => c.n));
  paint($('#panel-categorias'), porCategoria.length
    ? html`<div class="bars">${porCategoria.map(c => html`<div class="bar">
        <div class="bar__head"><span>${c.nombre}</span><span>${c.n} ${c.n === 1 ? 'auto' : 'autos'}</span></div>
        <div class="bar__track"><div class="bar__fill" style="width:${(c.n / maximo) * 100}%"></div></div>
      </div>`)}</div>`
    : vacio('Sin categorías', 'Creá una categoría para clasificar los autos.', 'tag'));

  const proximas = [...listaActivos].sort((a, b) => a.fecha_fin.localeCompare(b.fecha_fin)).slice(0, 6);
  paint($('#panel-proximas'), proximas.length
    ? html`<div class="mini-list">${proximas.map(a => {
      const dias = diasEntre(hoy(), a.fecha_fin);
      const cuando = dias === 0 ? 'hoy' : dias === 1 ? 'mañana' : `en ${dias} días`;
      const [, mes, dia] = parts(a.fecha_fin);
      return html`<div class="mini">
        <div class="mini__date"><strong>${dia}</strong><span>${MESES[mes - 1]}</span></div>
        <div class="mini__main">
          <div class="mini__title">${autoNombre(a.id_auto)}</div>
          <div class="mini__sub">${clienteDe(a.id_cliente)?.nombre || `Cliente #${a.id_cliente}`} · devuelve ${cuando}</div>
        </div>
        ${btnIcono('undo', 'Registrar devolución', 'return-alquiler', a.id_alquiler)}
      </div>`;
    })}</div>`
    : vacio('Sin alquileres activos', 'Cuando se registre un alquiler aparecerá acá.', 'key'));

  const ofertas = store.autos.filter(a => Number(a.oferta_porcentaje) > 0)
    .sort((a, b) => b.oferta_porcentaje - a.oferta_porcentaje).slice(0, 6);
  paint($('#panel-ofertas'), ofertas.length
    ? html`<div class="mini-list">${ofertas.map(a => html`<div class="mini">
        <img class="mini__thumb" data-fallback src="${a.imagen || PLACEHOLDER_IMG}" alt="" loading="lazy">
        <div class="mini__main">
          <div class="mini__title">${a.marca} ${a.modelo}</div>
          <div class="mini__sub">${fmtCOP(precioFinal(a))} / día</div>
        </div>
        <span class="badge badge--oferta">-${a.oferta_porcentaje}%</span>
      </div>`)}</div>`
    : vacio('Sin ofertas', 'Ningún auto tiene descuento activo.', 'tag'));
}

/* =====================================================================
   CATÁLOGO
   ===================================================================== */
const cat = { q: '', ciudad: '', categoria: '', orden: '', solo: false, lista: [] };

async function loadCatalogo() {
  fillSelect($('#cat-ciudad'), [{ value: '', label: 'Todas las ciudades' }, ...ciudades().map(c => ({ value: c, label: c }))], cat.ciudad);
  fillSelect($('#cat-categoria'), [{ value: '', label: 'Todas las categorías' }, ...store.categorias.map(c => ({ value: c.id_categoria, label: c.nombre }))], cat.categoria);
  $('#cat-q').value = cat.q;
  $('#cat-orden').value = cat.orden;
  $('#cat-solo').checked = cat.solo;
  paint($('#cat-grid'), tarjetasFantasma());

  if (cat.solo) {
    // HU-09: el backend filtra y devuelve únicamente autos disponibles
    const p = new URLSearchParams();
    if (cat.ciudad) p.set('ciudad', cat.ciudad);
    if (cat.categoria) p.set('id_categoria', cat.categoria);
    const qs = p.toString();
    cat.lista = (await api('/autos' + (qs ? `?${qs}` : ''))) || [];
  } else {
    const todos = (await api('/autos/all')) || [];
    store.autos = todos;
    store.autosLoaded = true;
    cat.lista = todos.filter(a => {
      if (cat.ciudad && normalizar(tiendaDe(a.id_tienda)?.ciudad) !== normalizar(cat.ciudad)) return false;
      if (cat.categoria && a.id_categoria !== Number(cat.categoria)) return false;
      return true;
    });
  }
  renderCatalogo();
}

function autoCard(a, indice = 0) {
  const t = tiendaDe(a.id_tienda);
  const oferta = Number(a.oferta_porcentaje) > 0;
  const libre = a.disponibilidad;
  return html`<article class="auto-card ${libre ? '' : 'is-off'}" style="--i:${Math.min(indice, 12)}">
    <div class="auto-card__media">
      <img data-fallback src="${a.imagen || PLACEHOLDER_IMG}" alt="${a.marca || ''} ${a.modelo || ''}" loading="lazy">
      <span class="badge ${libre ? 'badge--ok' : 'badge--no'} auto-card__state">${libre ? 'Disponible' : 'Alquilado'}</span>
      ${oferta ? html`<span class="badge badge--oferta auto-card__offer">-${a.oferta_porcentaje}%</span>` : ''}
    </div>
    <div class="auto-card__body">
      <div class="auto-card__top"><span class="chip">${categoriaNombre(a.id_categoria)}</span></div>
      <h3 class="auto-card__title">${a.marca} ${a.modelo}</h3>
      <p class="auto-card__loc">${icon('pin', 15)}${t ? `${t.nombre} · ${t.ciudad}` : 'Sin tienda'}</p>
      <div class="price-row">
        <span class="price ${oferta ? 'price--deal' : ''}">${fmtCOP(precioFinal(a))} <small>/ día</small></span>
        ${oferta ? html`<span class="price-old">${fmtCOP(a.precio_dia)}</span>` : ''}
      </div>
      <div class="auto-card__actions">
        <button class="btn btn-primary btn-sm" ${attrs({ 'data-action': 'rent-auto', 'data-id': a.id_auto, disabled: !libre, title: libre ? undefined : 'El auto está alquilado' })}>Alquilar</button>
        <button class="btn btn-ghost btn-sm" data-action="view-auto" data-id="${a.id_auto}">Detalle</button>
        <details class="menu">
          <summary class="icon-btn" aria-label="Más acciones" title="Más acciones">${icon('more')}</summary>
          <div class="menu__list">
            <button class="menu__item" data-action="edit-auto" data-id="${a.id_auto}">${icon('edit', 16)}Editar precio, oferta e imagen</button>
            <button class="menu__item" data-action="toggle-auto" data-id="${a.id_auto}">${icon('toggle', 16)}Marcar como ${libre ? 'no disponible' : 'disponible'}</button>
            <button class="menu__item menu__item--danger" data-action="delete-auto" data-id="${a.id_auto}">${icon('trash', 16)}Eliminar auto</button>
          </div>
        </details>
      </div>
    </div>
  </article>`;
}

function renderCatalogo() {
  let lista = [...cat.lista];
  const q = normalizar(cat.q.trim());
  if (q) lista = lista.filter(a => normalizar(`${a.marca} ${a.modelo}`).includes(q));
  const orden = {
    'precio-asc': (a, b) => precioFinal(a) - precioFinal(b),
    'precio-desc': (a, b) => precioFinal(b) - precioFinal(a),
    marca: (a, b) => `${a.marca} ${a.modelo}`.localeCompare(`${b.marca} ${b.modelo}`, 'es'),
  }[cat.orden] || ((a, b) => b.id_auto - a.id_auto);
  lista.sort(orden);
  $('#cat-count').textContent = `${lista.length} ${lista.length === 1 ? 'auto' : 'autos'}${cat.solo ? ' disponibles' : ''}`;
  paint($('#cat-grid'), lista.length
    ? html`${lista.map((a, i) => autoCard(a, i))}`
    : html`<div class="span-all">${vacio('No encontramos autos', 'Probá con otra ciudad, categoría o quitá el filtro de disponibles.', 'search')}</div>`);
}

function initCatalogo() {
  const onFiltro = () => {
    cat.ciudad = $('#cat-ciudad').value;
    cat.categoria = $('#cat-categoria').value;
    cat.solo = $('#cat-solo').checked;
    refreshCurrent();
  };
  $('#cat-ciudad').addEventListener('change', onFiltro);
  $('#cat-categoria').addEventListener('change', onFiltro);
  $('#cat-solo').addEventListener('change', onFiltro);
  $('#cat-orden').addEventListener('change', e => { cat.orden = e.target.value; renderCatalogo(); });
  $('#cat-q').addEventListener('input', debounce(e => { cat.q = e.target.value; renderCatalogo(); }, 150));
  $('#cat-filtros').addEventListener('submit', e => e.preventDefault());
  $('#cat-limpiar').addEventListener('click', () => {
    Object.assign(cat, { q: '', ciudad: '', categoria: '', orden: '', solo: false });
    refreshCurrent();
  });
}

/* --- acciones sobre autos --- */
function camposAuto() {
  return [
    { name: 'marca', label: 'Marca', required: true, maxlength: 50 },
    { name: 'modelo', label: 'Modelo', required: true, maxlength: 50 },
    { name: 'anio', label: 'Año', required: true, maxlength: 4, pattern: '\\d{4}', title: 'Cuatro dígitos, por ejemplo 2024', placeholder: '2024' },
    { name: 'placa', label: 'Placa', required: true, maxlength: 20, hint: 'Debe ser única.' },
    { name: 'precio_dia', label: 'Precio por día (COP)', type: 'number', required: true, min: 1, step: 'any' },
    { name: 'oferta_porcentaje', label: 'Oferta (%)', type: 'number', min: 0, max: 100, step: '0.5', hint: 'Opcional, de 0 a 100.' },
    { name: 'id_tienda', label: 'Tienda', type: 'select', numeric: true, required: true, placeholder: 'Seleccioná una tienda', options: store.tiendas.map(t => ({ value: t.id_tienda, label: `${t.nombre} · ${t.ciudad}` })) },
    { name: 'id_categoria', label: 'Categoría', type: 'select', numeric: true, required: true, placeholder: 'Seleccioná una categoría', options: store.categorias.map(c => ({ value: c.id_categoria, label: c.nombre })) },
    { name: 'imagen', label: 'URL de la imagen', type: 'url', maxlength: 2000, full: true, hint: 'Opcional.' },
  ];
}

function nuevoAuto() {
  if (!store.tiendas.length || !store.categorias.length) {
    toast('Primero necesitás al menos una tienda y una categoría.', 'err');
    return;
  }
  abrirFormulario({
    title: 'Registrar auto',
    intro: 'El auto se crea disponible, junto con su ficha comercial.',
    fields: camposAuto(),
    submitLabel: 'Registrar auto',
    onSubmit: v => mutar(() => api('/autos', { method: 'POST', body: v }), 'Auto registrado correctamente.'),
  });
}

async function verAuto(id) {
  const d = await api(`/autos/${id}`);
  const base = autoDe(id) || cat.lista.find(a => a.id_auto === Number(id)) || {};
  const t = tiendaDe(base.id_tienda);
  abrirInfo({
    title: `${d.marca} ${d.modelo}`,
    body: html`<img class="detail__img" data-fallback src="${base.imagen || PLACEHOLDER_IMG}" alt="${d.marca} ${d.modelo}">
      <dl class="detail">
        <div><dt>Año</dt><dd>${d.anio}</dd></div>
        <div><dt>Placa</dt><dd class="mono">${d.placa}</dd></div>
        <div><dt>Categoría</dt><dd>${base.id_categoria ? categoriaNombre(base.id_categoria) : '—'}</dd></div>
        <div><dt>Tienda</dt><dd>${t ? `${t.nombre}, ${t.ciudad}` : '—'}</dd></div>
        <div><dt>Precio por día</dt><dd>${fmtCOP(d.precio_dia)}</dd></div>
        <div><dt>Oferta</dt><dd>${Number(d.oferta_porcentaje) > 0 ? `${d.oferta_porcentaje} %` : 'Sin oferta'}</dd></div>
        <div><dt>Precio con oferta</dt><dd><strong>${fmtCOP(d.precio_con_oferta)}</strong></dd></div>
        <div><dt>Estado</dt><dd><span class="badge ${d.disponibilidad ? 'badge--ok' : 'badge--no'}">${d.disponibilidad ? 'Disponible' : 'No disponible'}</span></dd></div>
      </dl>`,
    footer: html`<button type="button" class="btn btn-ghost" data-close>Cerrar</button>
      ${d.disponibilidad ? html`<button type="button" class="btn btn-primary" data-action="rent-auto" data-id="${id}">Alquilar</button>` : ''}`,
  });
}

async function editarAuto(id) {
  await loadAutosCache();
  const a = autoDe(id) || cat.lista.find(x => x.id_auto === Number(id));
  if (!a) throw new Error('No se encontró el auto.');
  const editables = ['precio_dia', 'oferta_porcentaje', 'id_tienda', 'id_categoria', 'imagen'];
  abrirFormulario({
    title: `Editar ${a.marca} ${a.modelo}`,
    intro: 'Marca, modelo, año y placa no se modifican. Para quitar una oferta ponela en 0.',
    fields: camposAuto().filter(f => editables.includes(f.name)).map(f => ({ ...f, required: f.name === 'precio_dia' || f.name === 'id_tienda' || f.name === 'id_categoria' })),
    values: a,
    onSubmit: async v => {
      // solo se envía lo que realmente cambió respecto al auto actual
      const detalles = {};
      if (v.precio_dia !== undefined && v.precio_dia !== Number(a.precio_dia)) detalles.precio_dia = v.precio_dia;
      if (v.oferta_porcentaje !== undefined && v.oferta_porcentaje !== Number(a.oferta_porcentaje ?? NaN)) detalles.oferta_porcentaje = v.oferta_porcentaje;
      if (v.imagen !== undefined && v.imagen !== (a.imagen || '')) detalles.imagen = v.imagen;
      const cambiaUbicacion = v.id_tienda !== a.id_tienda || v.id_categoria !== a.id_categoria;
      if (!Object.keys(detalles).length && !cambiaUbicacion) throw new Error('No hay cambios para guardar.');
      await mutar(async () => {
        if (Object.keys(detalles).length) await api(`/autos/${id}`, { method: 'PUT', body: detalles });
        if (cambiaUbicacion) {
          await api(`/autos/update/${id}`, { method: 'PUT', body: { disponibilidad: a.disponibilidad, id_tienda: v.id_tienda, id_categoria: v.id_categoria } });
        }
      }, 'Auto actualizado.');
    },
  });
}

async function alternarDisponibilidad(id) {
  await loadAutosCache();
  const a = autoDe(id) || cat.lista.find(x => x.id_auto === Number(id));
  const nuevo = !a?.disponibilidad;
  await mutar(() => api(`/autos/${id}/disponibilidad`, { method: 'PATCH', body: { disponibilidad: nuevo } }),
    `El auto quedó ${nuevo ? 'disponible' : 'no disponible'}.`);
}

async function eliminarAuto(id) {
  await loadAutosCache();
  const a = autoDe(id) || cat.lista.find(x => x.id_auto === Number(id));
  const ok = await confirmar({
    title: 'Eliminar auto',
    message: `¿Eliminar ${a ? `${a.marca} ${a.modelo}` : `el auto #${id}`}? Se borra también su ficha. No se puede deshacer.`,
    confirmLabel: 'Eliminar', danger: true,
  });
  if (ok) await mutar(() => api(`/autos/${id}`, { method: 'DELETE' }), 'Auto eliminado.');
}

/* =====================================================================
   ALQUILERES
   ===================================================================== */
const alq = { tab: 'activos', clienteId: '' };

function estadoAlquiler(a) {
  if (a.estado === 'CERRADO') return html`<span class="badge badge--muted" title="Devuelto">Cerrado</span>`;
  if (a.fecha_fin < hoy()) return html`<span class="badge badge--no" title="La fecha de devolución ya pasó y el auto no fue devuelto">Sin devolver</span>`;
  if (a.fecha_inicio > hoy()) return html`<span class="badge badge--warn" title="Todavía no inicia">Reservado</span>`;
  return html`<span class="badge badge--ok" title="En curso">En curso</span>`;
}

function filasAlquiler(lista) {
  return [...lista].sort((a, b) => b.id_alquiler - a.id_alquiler).map(a => {
    const activo = a.estado === 'ACTIVO';
    const cancelable = activo && a.fecha_inicio > hoy();
    const dias = diasEntre(a.fecha_inicio, a.fecha_fin);
    return html`<tr>
      <td class="mono">#${a.id_alquiler}</td>
      <td class="nw">${clienteDe(a.id_cliente)?.nombre || `Cliente #${a.id_cliente}`}</td>
      <td class="nw">${autoNombre(a.id_auto)}</td>
      <td><span class="nw">${fmtFecha(a.fecha_inicio)}</span> → <span class="nw">${fmtFecha(a.fecha_fin)}</span><span class="sub">${dias} ${dias === 1 ? 'día' : 'días'}</span></td>
      <td><span class="nw">${a.ciudad_retirada}</span> → <span class="nw">${a.ciudad_devolucion}</span></td>
      <td class="num">${fmtCOP(a.precio_total)}</td>
      <td>${estadoAlquiler(a)}</td>
      <td class="col-actions">
        ${btnIcono('undo', 'Registrar devolución', 'return-alquiler', a.id_alquiler, { disabled: !activo })}
        ${btnIcono('trash', cancelable ? 'Cancelar alquiler' : 'Solo se puede cancelar antes de la fecha de retiro', 'cancel-alquiler', a.id_alquiler, { danger: true, disabled: !cancelable })}
      </td>
    </tr>`;
  });
}

async function loadAlquileres() {
  $$('.tab').forEach(t => t.setAttribute('aria-selected', String(t.dataset.tab === alq.tab)));
  const barra = $('#alq-toolbar');
  barra.hidden = alq.tab !== 'historial';
  if (alq.tab === 'historial') {
    fillSelect($('#alq-cliente'), [{ value: '', label: 'Elegí un cliente…' }, ...store.clientes.map(c => ({ value: c.id_cliente, label: `${c.nombre} · ${c.email}` }))], alq.clienteId);
  }
  const cont = $('#alq-tabla');
  paint(cont, cargando());
  await loadAutosCache();

  let lista;
  if (alq.tab === 'activos') lista = await api('/alquileres/activos');
  else if (alq.tab === 'todos') lista = await api('/alquileres/all');
  else {
    if (!alq.clienteId) { paint(cont, vacio('Elegí un cliente', 'Se mostrará su historial completo de alquileres.', 'users')); return; }
    lista = await api(`/alquileres?id_cliente=${encodeURIComponent(alq.clienteId)}`);
  }
  lista = lista || [];
  const heads = ['Nº', 'Cliente', 'Auto', 'Fechas', 'Ruta', { label: 'Total', cls: 'num' }, 'Estado', { label: 'Acciones', cls: 'col-actions' }];
  paint(cont, lista.length
    ? tabla(heads, filasAlquiler(lista))
    : vacio(alq.tab === 'activos' ? 'No hay alquileres activos' : 'Sin alquileres', alq.tab === 'historial' ? 'Este cliente todavía no tiene alquileres.' : '', 'key'));
}

async function nuevoAlquiler({ autoId } = {}) {
  await loadAutosCache();
  const libres = store.autos.filter(a => a.disponibilidad);
  if (!store.clientes.length) { toast('Primero registrá un cliente.', 'err'); return; }
  if (!libres.length) { toast('No hay autos disponibles en este momento.', 'err'); return; }
  const manana = addDays(hoy(), 1);
  const auto = autoId ? libres.find(a => a.id_auto === Number(autoId)) : null;

  abrirFormulario({
    title: 'Nuevo alquiler',
    intro: 'El precio total se calcula con el precio por día (con oferta) por la cantidad de días.',
    submitLabel: 'Registrar alquiler',
    values: { id_auto: auto?.id_auto, ciudad_retirada: auto ? tiendaDe(auto.id_tienda)?.ciudad : '', fecha_inicio: manana },
    fields: [
      { name: 'id_cliente', label: 'Cliente', type: 'select', numeric: true, required: true, full: true, placeholder: 'Seleccioná un cliente', options: store.clientes.map(c => ({ value: c.id_cliente, label: `${c.nombre} · ${c.email}` })) },
      { name: 'id_auto', label: 'Auto disponible', type: 'select', numeric: true, required: true, full: true, placeholder: 'Seleccioná un auto', options: libres.map(a => ({ value: a.id_auto, label: `${a.marca} ${a.modelo} — ${fmtCOP(precioFinal(a))}/día · ${tiendaDe(a.id_tienda)?.ciudad || ''}` })) },
      { name: 'fecha_inicio', label: 'Fecha de retiro', type: 'date', required: true, min: manana, hint: 'Debe ser posterior a hoy.' },
      { name: 'fecha_fin', label: 'Fecha de devolución', type: 'date', required: true, min: addDays(manana, 1) },
      { name: 'ciudad_retirada', label: 'Ciudad de retiro', required: true, maxlength: 50, list: 'lista-ciudades' },
      { name: 'ciudad_devolucion', label: 'Ciudad de devolución', required: true, maxlength: 50, list: 'lista-ciudades' },
    ],
    extra: html`<datalist id="lista-ciudades">${ciudades().map(c => html`<option value="${c}">`)}</datalist>
      <div class="estimate" id="alq-estimado" aria-live="polite">Elegí un auto y las fechas para ver el total estimado.</div>`,
    onOpen: form => {
      let retiroManual = false;
      const f = n => form.elements[n];
      const calcular = () => {
        const a = libres.find(x => x.id_auto === Number(f('id_auto').value));
        const ini = f('fecha_inicio').value, fin = f('fecha_fin').value;
        if (ini) {
          f('fecha_fin').min = addDays(ini, 1);
          if (fin && fin <= ini) f('fecha_fin').value = '';
        }
        const box = $('#alq-estimado', form);
        const dias = ini && f('fecha_fin').value ? diasEntre(ini, f('fecha_fin').value) : 0;
        if (a && dias > 0) {
          paint(box, html`Total estimado: <strong>${fmtCOP(precioFinal(a) * dias)}</strong> · ${dias} ${dias === 1 ? 'día' : 'días'} × ${fmtCOP(precioFinal(a))}`);
        } else {
          paint(box, 'Elegí un auto y las fechas para ver el total estimado.');
        }
      };
      f('id_auto').addEventListener('change', () => {
        const a = libres.find(x => x.id_auto === Number(f('id_auto').value));
        if (a && !retiroManual) f('ciudad_retirada').value = tiendaDe(a.id_tienda)?.ciudad || '';
        calcular();
      });
      f('ciudad_retirada').addEventListener('input', () => { retiroManual = true; });
      f('fecha_inicio').addEventListener('change', calcular);
      f('fecha_fin').addEventListener('change', calcular);
      calcular();
    },
    onSubmit: v => mutar(async () => {
      const creado = await api('/alquileres', { method: 'POST', body: v });
      toast(`Alquiler #${creado.id_alquiler} registrado. Total: ${fmtCOP(creado.precio_total)}.`, 'ok');
    }, null, async () => { alq.tab = 'activos'; await goTo('alquileres'); }),
  });
}

async function devolverAlquiler(id) {
  const ok = await confirmar({
    title: 'Registrar devolución',
    message: `¿Registrar la devolución del alquiler #${id}? Se cierra el alquiler y el auto queda disponible.`,
    confirmLabel: 'Registrar devolución',
  });
  if (ok) await mutar(() => api(`/alquileres/${id}/devolucion`, { method: 'PUT' }), 'Devolución registrada. El auto quedó disponible.');
}

async function cancelarAlquiler(id) {
  const ok = await confirmar({
    title: 'Cancelar alquiler',
    message: `¿Cancelar el alquiler #${id}? Se elimina la reserva y el auto queda disponible.`,
    confirmLabel: 'Cancelar alquiler', danger: true,
  });
  if (ok) await mutar(() => api(`/alquileres/${id}`, { method: 'DELETE' }), 'Alquiler cancelado. El auto quedó disponible.');
}

function initAlquileres() {
  $('#alq-cliente').addEventListener('change', e => { alq.clienteId = e.target.value; refreshCurrent(); });
}

/* =====================================================================
   CLIENTES
   ===================================================================== */
const cli = { q: '' };

function loadClientes() {
  renderClientes();
}

function renderClientes() {
  const q = normalizar(cli.q.trim());
  const lista = store.clientes.filter(c => !q || normalizar(`${c.nombre} ${c.email} ${c.telefono}`).includes(q));
  const cont = $('#cli-tabla');
  if (!store.clientes.length) { paint(cont, vacio('Todavía no hay clientes', 'Registrá el primero con el botón de arriba.', 'users')); return; }
  if (!lista.length) { paint(cont, vacio('Sin resultados', 'Ningún cliente coincide con la búsqueda.', 'search')); return; }
  paint(cont, tabla(['Cliente', 'Email', 'Teléfono', 'Tarjeta', { label: 'Acciones', cls: 'col-actions' }],
    lista.map(c => html`<tr>
      <td><div class="cell-person">${avatar(c.nombre)}<div><strong>${c.nombre}</strong><span class="sub">ID ${c.id_cliente}</span></div></div></td>
      <td>${c.email}</td>
      <td class="mono">${c.telefono}</td>
      <td class="mono">${c.tarjeta_credito || '—'}</td>
      <td class="col-actions">
        ${btnIcono('clock', 'Ver historial de alquileres', 'historial-cliente', c.id_cliente)}
        ${btnIcono('edit', 'Editar contacto y tarjeta', 'edit-cliente', c.id_cliente)}
        ${btnIcono('trash', 'Eliminar cliente', 'delete-cliente', c.id_cliente, { danger: true })}
      </td>
    </tr>`)));
}

function nuevoCliente() {
  abrirFormulario({
    title: 'Registrar cliente',
    submitLabel: 'Registrar cliente',
    fields: [
      { name: 'nombre', label: 'Nombre completo', required: true, maxlength: 100, full: true },
      { name: 'email', label: 'Email', type: 'email', required: true, maxlength: 150, full: true, hint: 'No se podrá cambiar después.' },
      { name: 'telefono', label: 'Teléfono', type: 'tel', required: true, maxlength: 20 },
      { name: 'tarjeta_credito', label: 'Tarjeta de crédito', required: true, maxlength: 20, pattern: '[0-9 \\-]{4,20}', title: 'Solo números', hint: 'Se guarda y nunca se muestra completa.' },
    ],
    onSubmit: v => mutar(() => api('/clientes', { method: 'POST', body: v }), 'Cliente registrado.'),
  });
}

function editarCliente(id) {
  const c = clienteDe(id);
  if (!c) throw new Error('No se encontró el cliente.');
  abrirFormulario({
    title: 'Editar cliente',
    intro: 'Solo se actualizan los campos que completes. El email no se puede cambiar.',
    values: { nombre: c.nombre, email: c.email, telefono: c.telefono },
    fields: [
      { name: 'nombre', label: 'Nombre', readonly: true, full: true },
      { name: 'email', label: 'Email', readonly: true, full: true },
      { name: 'telefono', label: 'Teléfono', type: 'tel', maxlength: 20 },
      { name: 'tarjeta_credito', label: 'Nueva tarjeta de crédito', maxlength: 20, pattern: '[0-9 \\-]{4,20}', title: 'Solo números', placeholder: `Actual: ${c.tarjeta_credito || '—'}`, hint: 'Dejala vacía para conservar la actual.' },
    ],
    onSubmit: async v => {
      const cambios = {};
      if (v.telefono && v.telefono !== c.telefono) cambios.telefono = v.telefono;
      if (v.tarjeta_credito) cambios.tarjeta_credito = v.tarjeta_credito;
      if (!Object.keys(cambios).length) throw new Error('No hay cambios para guardar.');
      await mutar(() => api(`/clientes/${id}`, { method: 'PUT', body: cambios }), 'Cliente actualizado.');
    },
  });
}

async function eliminarCliente(id) {
  const ok = await confirmar({
    title: 'Eliminar cliente',
    message: `¿Eliminar a ${clienteDe(id)?.nombre || `el cliente #${id}`}? Si tiene alquileres registrados no se podrá eliminar.`,
    confirmLabel: 'Eliminar', danger: true,
  });
  if (ok) await mutar(() => api(`/clientes/${id}`, { method: 'DELETE' }), 'Cliente eliminado.');
}

function initClientes() {
  $('#cli-q').addEventListener('input', debounce(e => { cli.q = e.target.value; renderClientes(); }, 150));
}

/* =====================================================================
   TIENDAS
   ===================================================================== */
const tie = { ciudad: '' };

async function loadTiendas() {
  $('#tie-ciudad').value = tie.ciudad;
  const cont = $('#tie-tabla');
  paint(cont, cargando());
  // HU-03: el backend filtra por ciudad ignorando mayúsculas
  const lista = (await api('/tiendas' + (tie.ciudad ? `?ciudad=${encodeURIComponent(tie.ciudad)}` : ''))) || [];
  if (!tie.ciudad) { store.tiendas = lista; }
  paint(cont, lista.length
    ? tabla(['Tienda', 'Ciudad', 'Dirección', { label: 'Acciones', cls: 'col-actions' }],
      lista.map(t => html`<tr>
        <td><strong>${t.nombre}</strong><span class="sub">ID ${t.id_tienda}</span></td>
        <td>${t.ciudad}</td>
        <td>${t.direccion}</td>
        <td class="col-actions">
          ${btnIcono('car', 'Ver autos de esta ciudad', 'autos-tienda', t.id_tienda)}
          ${btnIcono('edit', 'Editar tienda', 'edit-tienda', t.id_tienda)}
          ${btnIcono('trash', 'Eliminar tienda', 'delete-tienda', t.id_tienda, { danger: true })}
        </td>
      </tr>`))
    : vacio(tie.ciudad ? 'No hay tiendas en esa ciudad' : 'Todavía no hay tiendas', tie.ciudad ? 'Probá con otra ciudad.' : 'Registrá la primera con el botón de arriba.', 'pin'));
}

const camposTienda = [
  { name: 'nombre', label: 'Nombre', required: true, full: true, maxlength: 70 },
  { name: 'ciudad', label: 'Ciudad', required: true, maxlength: 50 },
  { name: 'direccion', label: 'Dirección', required: true, maxlength: 70 },
];

function nuevaTienda() {
  abrirFormulario({
    title: 'Nueva tienda', fields: camposTienda, submitLabel: 'Registrar tienda',
    onSubmit: v => mutar(() => api('/tiendas', { method: 'POST', body: v }), 'Tienda registrada.'),
  });
}

async function editarTienda(id) {
  const t = await api(`/tiendas/${id}`);
  abrirFormulario({
    title: 'Editar tienda', fields: camposTienda, values: t,
    onSubmit: v => mutar(() => api(`/tiendas/${id}`, { method: 'PUT', body: v }), 'Tienda actualizada.'),
  });
}

async function eliminarTienda(id) {
  const t = findBy(store.tiendas, 'id_tienda', id);
  const ok = await confirmar({
    title: 'Eliminar tienda',
    message: `¿Eliminar ${t ? `${t.nombre} (${t.ciudad})` : `la tienda #${id}`}? Si todavía tiene autos no se podrá eliminar.`,
    confirmLabel: 'Eliminar', danger: true,
  });
  if (ok) await mutar(() => api(`/tiendas/${id}`, { method: 'DELETE' }), 'Tienda eliminada.');
}

function initTiendas() {
  $('#tie-filtro').addEventListener('submit', e => { e.preventDefault(); tie.ciudad = $('#tie-ciudad').value.trim(); refreshCurrent(); });
  $('#tie-limpiar').addEventListener('click', () => { tie.ciudad = ''; refreshCurrent(); });
}

/* =====================================================================
   CATEGORÍAS
   ===================================================================== */
async function loadCategorias() {
  const cont = $('#catg-tabla');
  paint(cont, cargando());
  await loadAutosCache();
  const conteo = id => store.autos.filter(a => a.id_categoria === id).length;
  paint(cont, store.categorias.length
    ? tabla(['Categoría', 'Descripción', { label: 'Autos', cls: 'num' }, { label: 'Acciones', cls: 'col-actions' }],
      store.categorias.map(c => html`<tr>
        <td><strong>${c.nombre}</strong><span class="sub">ID ${c.id_categoria}</span></td>
        <td>${c.descripcion}</td>
        <td class="num">${conteo(c.id_categoria)}</td>
        <td class="col-actions">
          ${btnIcono('edit', 'Editar categoría', 'edit-categoria', c.id_categoria)}
          ${btnIcono('trash', 'Eliminar categoría', 'delete-categoria', c.id_categoria, { danger: true })}
        </td>
      </tr>`))
    : vacio('Todavía no hay categorías', 'Registrá la primera con el botón de arriba.', 'tag'));
}

const camposCategoria = [
  { name: 'nombre', label: 'Nombre', required: true, maxlength: 50, full: true },
  { name: 'descripcion', label: 'Descripción', required: true, full: true },
];

function nuevaCategoria() {
  abrirFormulario({
    title: 'Nueva categoría', fields: camposCategoria, submitLabel: 'Registrar categoría',
    onSubmit: v => mutar(() => api('/categorias', { method: 'POST', body: v }), 'Categoría registrada.'),
  });
}

async function editarCategoria(id) {
  const c = await api(`/categorias/${id}`);
  abrirFormulario({
    title: 'Editar categoría', fields: camposCategoria, values: c,
    onSubmit: v => mutar(() => api(`/categorias/${id}`, { method: 'PUT', body: v }), 'Categoría actualizada.'),
  });
}

async function eliminarCategoria(id) {
  const c = findBy(store.categorias, 'id_categoria', id);
  const ok = await confirmar({
    title: 'Eliminar categoría',
    message: `¿Eliminar ${c ? c.nombre : `la categoría #${id}`}? Si todavía tiene autos no se podrá eliminar.`,
    confirmLabel: 'Eliminar', danger: true,
  });
  if (ok) await mutar(() => api(`/categorias/${id}`, { method: 'DELETE' }), 'Categoría eliminada.');
}

/* =====================================================================
   Acciones (delegación de eventos: cualquier [data-action] del documento)
   ===================================================================== */
const ACTIONS = {
  refresh: () => { invalidate(); return refreshCurrent(); },

  'new-auto': nuevoAuto,
  'view-auto': d => verAuto(d.id),
  'rent-auto': d => nuevoAlquiler({ autoId: d.id }),
  'edit-auto': d => editarAuto(d.id),
  'toggle-auto': d => alternarDisponibilidad(d.id),
  'delete-auto': d => eliminarAuto(d.id),

  'new-alquiler': () => nuevoAlquiler(),
  'alq-tab': d => { alq.tab = d.tab; return refreshCurrent(); },
  'return-alquiler': d => devolverAlquiler(d.id),
  'cancel-alquiler': d => cancelarAlquiler(d.id),

  'new-cliente': nuevoCliente,
  'edit-cliente': d => editarCliente(d.id),
  'delete-cliente': d => eliminarCliente(d.id),
  'historial-cliente': d => { alq.tab = 'historial'; alq.clienteId = d.id; return goTo('alquileres'); },

  'new-tienda': nuevaTienda,
  'edit-tienda': d => editarTienda(d.id),
  'delete-tienda': d => eliminarTienda(d.id),
  'autos-tienda': d => {
    const t = findBy(store.tiendas, 'id_tienda', d.id);
    Object.assign(cat, { ciudad: t?.ciudad || '', categoria: '', q: '', solo: false });
    return goTo('catalogo');
  },

  'new-categoria': nuevaCategoria,
  'edit-categoria': d => editarCategoria(d.id),
  'delete-categoria': d => eliminarCategoria(d.id),
};

document.addEventListener('click', async ev => {
  // cierra los menús desplegables abiertos al hacer clic fuera
  $$('details.menu[open]').forEach(m => { if (!m.contains(ev.target)) m.removeAttribute('open'); });

  const el = ev.target.closest('[data-action]');
  if (!el || !ACTIONS[el.dataset.action]) return;
  el.closest('details.menu')?.removeAttribute('open');
  if (el.closest('dialog') && el.dataset.action === 'rent-auto') modal().close();
  const esBoton = el.tagName === 'BUTTON';
  if (esBoton) el.disabled = true;
  try {
    await ACTIONS[el.dataset.action](el.dataset, el);
  } catch (e) {
    if (e.status !== 0) toast(e.message, 'err');
  } finally {
    if (esBoton && el.isConnected) el.disabled = false;
  }
});

// si una imagen no carga se muestra el dibujo genérico
document.addEventListener('error', ev => {
  const img = ev.target;
  if (img instanceof HTMLImageElement && img.hasAttribute('data-fallback') && img.src !== PLACEHOLDER_IMG) img.src = PLACEHOLDER_IMG;
}, true);

// clic en el fondo oscuro cierra el modal
modal().addEventListener('click', ev => { if (ev.target === modal()) modal().close(); });

/* ---------------------------------------------------------------------
   Arranque
   --------------------------------------------------------------------- */
/* ---------------------------------------------------------------------
   Tema claro / oscuro (el guardado, o el del sistema la primera vez)
   --------------------------------------------------------------------- */
function aplicarTema(tema) {
  document.documentElement.setAttribute('data-theme', tema);
  const oscuro = tema === 'dark';
  $('.theme-toggle__icon').innerHTML = icon(oscuro ? 'sun' : 'moon', 17).s;
  $('.theme-toggle__label').textContent = oscuro ? 'Tema claro' : 'Tema oscuro';
  $('#theme-toggle').setAttribute('aria-label', oscuro ? 'Cambiar a tema claro' : 'Cambiar a tema oscuro');
}

function initTema() {
  aplicarTema(document.documentElement.getAttribute('data-theme') === 'light' ? 'light' : 'dark');
  $('#theme-toggle').addEventListener('click', () => {
    const nuevo = document.documentElement.getAttribute('data-theme') === 'dark' ? 'light' : 'dark';
    aplicarTema(nuevo);
    try { localStorage.setItem('autorent.theme', nuevo); } catch (_) { /* sin almacenamiento */ }
  });
}

function init() {
  $$('[data-icon]').forEach(el => { el.outerHTML = icon(el.dataset.icon).s; });
  initTema();

  const input = $('#api-base-input');
  input.value = API_BASE;
  input.addEventListener('change', () => {
    API_BASE = input.value.trim().replace(/\/$/, '');
    try { localStorage.setItem(STORAGE_KEY, API_BASE); } catch (_) { /* sin almacenamiento */ }
    invalidate();
    refreshCurrent();
  });

  initCatalogo();
  initAlquileres();
  initClientes();
  initTiendas();
  window.addEventListener('hashchange', route);
  route();
}

init();
