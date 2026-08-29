// escalate-complaint.js — Mirrors EscalateComplaintScreen.kt
import { AppState, goBack } from '../app.js';
import { escalateComplaint } from './firestore-service.js';
import { showToast, getStatusBadgeClass } from './utils.js';

export function renderEscalateComplaint(container) {
  const list = AppState.complaintList.filter(c => c.status !== 'Resolved' && c.status !== 'Escalated');
  let selectedTicket = AppState.complaintData?.ticketId || '';

  function render() {
    const c = list.find(comp => comp.ticketId === selectedTicket) || null;
    container.innerHTML = `
    <div class="screen active" style="min-height:100vh;display:flex;flex-direction:column;background:#F5F7FA;">
      <div class="app-bar">
        <button class="back-btn" id="esc-back">←</button>
        <span class="app-bar-title">Escalate Complaint</span>
      </div>
      <div style="padding:20px;overflow-y:auto;flex:1;padding-bottom:60px;">
        <div style="background:#FFF3E0;border:2px solid #FF9800;border-radius:16px;padding:16px;margin-bottom:20px;display:flex;gap:12px;">
          <span style="font-size:24px;">⬆️</span>
          <div>
            <div style="font-weight:700;color:#E65100;">Escalate to Admin</div>
            <div style="font-size:13px;color:#BF360C;margin-top:2px;">Use this when a complaint needs admin attention or has been unresolved for too long.</div>
          </div>
        </div>
        ${list.length === 0 ? `<div class="empty-state"><div class="empty-state-icon">✅</div><div class="empty-state-title">No Active Complaints</div><div class="empty-state-sub">All complaints are resolved or already escalated.</div></div>` : `
        <div class="form-group">
          <label class="form-label">Select Complaint</label>
          <input list="complaint-list" id="esc-ticket-select" class="form-input" value="${selectedTicket}" placeholder="Complaint Ticket ID">
          <datalist id="complaint-list">
            ${list.map(comp => `<option value="${comp.ticketId}">${comp.ticketId} — ${comp.title||'Untitled'}</option>`).join('')}
          </datalist>
        </div>
        ${c ? `
        <div class="card" style="margin-bottom:20px;">
          <div style="font-weight:700;font-size:15px;margin-bottom:4px;">${c.title}</div>
          <div style="font-size:13px;color:#757575;margin-bottom:8px;">${c.category} • ${c.location}</div>
          <span class="badge ${getStatusBadgeClass(c.status)}">${c.status}</span>
        </div>` : ''}
        <div class="form-group">
          <label class="form-label">Reason for Escalation *</label>
          <textarea id="esc-reason" class="form-textarea" placeholder="Why is this being escalated? (e.g. Parts not available, requires admin authorization…)" rows="5"></textarea>
        </div>
        <div id="esc-error" class="alert alert-error alert-hidden"></div>
        <button id="esc-submit" class="btn btn-lg" style="background:#E65100;color:white;border-radius:16px;">⬆️ Escalate to Admin</button>`}
      </div>
    </div>`;

    document.getElementById('esc-back').addEventListener('click', goBack);
    document.getElementById('esc-ticket-select')?.addEventListener('change', e => { selectedTicket = e.target.value; render(); });
    document.getElementById('esc-submit')?.addEventListener('click', async () => {
      const reason = document.getElementById('esc-reason').value.trim();
      const errEl = document.getElementById('esc-error');
      if (!reason) { errEl.textContent = 'Please provide a reason for escalation.'; errEl.classList.remove('alert-hidden'); return; }
      const btn = document.getElementById('esc-submit');
      btn.textContent = 'Escalating…'; btn.disabled = true;
      try {
        await escalateComplaint(selectedTicket, reason);
        showToast('Complaint escalated to admin ⬆️', 'success');
        goBack();
      } catch (e) {
        errEl.textContent = e.message || 'Escalation failed.';
        errEl.classList.remove('alert-hidden');
        btn.textContent = '⬆️ Escalate to Admin'; btn.disabled = false;
      }
    });
  }
  render();
}
