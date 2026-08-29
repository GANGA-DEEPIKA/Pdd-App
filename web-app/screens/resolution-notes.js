// resolution-notes.js — Mirrors ResolutionNotesScreen.kt
import { AppState, goBack } from '../app.js';
import { addResolutionNote } from './firestore-service.js';
import { showToast, getStatusBadgeClass } from './utils.js';

export function renderResolutionNotes(container) {
  const list = AppState.complaintList;
  let selectedTicket = AppState.complaintData?.ticketId || '';

  function render() {
    const c = list.find(comp => comp.ticketId === selectedTicket) || null;
    container.innerHTML = `
    <div class="screen active" style="min-height:100vh;display:flex;flex-direction:column;background:#F5F7FA;">
      <div class="app-bar">
        <button class="back-btn" id="rn-back">←</button>
        <span class="app-bar-title">Resolution Notes</span>
      </div>
      <div style="padding:20px;overflow-y:auto;flex:1;padding-bottom:60px;">
        <div class="form-group">
          <label class="form-label">Select Complaint</label>
          <input list="rn-complaint-list" id="rn-ticket-select" class="form-input" value="${selectedTicket}" placeholder="Complaint Ticket ID">
          <datalist id="rn-complaint-list">
            ${list.map(comp => `<option value="${comp.ticketId}">${comp.ticketId} — ${comp.title||'Untitled'}</option>`).join('')}
          </datalist>
        </div>
        ${c ? `
        <div class="card" style="margin-bottom:20px;">
          <div style="font-weight:700;font-size:15px;margin-bottom:4px;">${c.title}</div>
          <div style="font-size:13px;color:#757575;margin-bottom:8px;">${c.category} • ${c.location}</div>
          <span class="badge ${getStatusBadgeClass(c.status)}">${c.status}</span>
          ${c.resolutionNote ? `<div style="margin-top:12px;background:#F5F7FA;border-radius:8px;padding:10px;font-size:13px;color:#333;"><strong>Existing note:</strong><br/>${c.resolutionNote}</div>` : ''}
        </div>` : ''}
        <div class="form-group">
          <label class="form-label">Resolution Note</label>
          <textarea id="rn-note" class="form-textarea" placeholder="Describe how the issue was resolved, parts used, actions taken…" rows="6">${(c && c.resolutionNote) || ''}</textarea>
        </div>
        <div id="rn-error" class="alert alert-error alert-hidden"></div>
        <button id="rn-save" class="btn btn-primary btn-lg">Save Note</button>
      </div>
    </div>`;

    document.getElementById('rn-back').addEventListener('click', goBack);
    document.getElementById('rn-ticket-select')?.addEventListener('change', e => { selectedTicket = e.target.value; render(); });
    document.getElementById('rn-save')?.addEventListener('click', async () => {
      const note = document.getElementById('rn-note').value.trim();
      const errEl = document.getElementById('rn-error');
      if (!note) { errEl.textContent = 'Please enter a resolution note.'; errEl.classList.remove('alert-hidden'); return; }
      const btn = document.getElementById('rn-save');
      btn.textContent = 'Saving…'; btn.disabled = true;
      try {
        await addResolutionNote(selectedTicket, note);
        showToast('Resolution note saved ✅', 'success');
        goBack();
      } catch (e) {
        errEl.textContent = e.message || 'Save failed.';
        errEl.classList.remove('alert-hidden');
        btn.textContent = 'Save Note'; btn.disabled = false;
      }
    });
  }
  render();
}
