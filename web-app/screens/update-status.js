// update-status.js — Mirrors UpdateStatusScreen.kt
import { AppState, navigateTo, goBack } from '../app.js';
import { updateComplaintStatus } from './firestore-service.js';
import { showToast, getStatusBadgeClass, openAttachment } from './utils.js';

window.openAttachment = openAttachment;

const STATUSES = ['Assigned', 'In Progress', 'Resolved'];

export function renderUpdateStatus(container) {
  const list = AppState.complaintList;
  const initial = AppState.complaintData?.ticketId || '';

  let selectedTicket = initial || '';
  let selectedStatus = '';

  function getComplaint() { return list.find(c => c.ticketId === selectedTicket) || null; }

  function render() {
    const c = getComplaint();
    container.innerHTML = `
    <div class="screen active" style="min-height:100vh;display:flex;flex-direction:column;background:#F5F7FA;">
      <div class="app-bar">
        <button class="back-btn" id="us-back">←</button>
        <span class="app-bar-title">Update Status</span>
      </div>
      <div style="padding:20px;overflow-y:auto;flex:1;padding-bottom:60px;">
        <div class="form-group">
          <label class="form-label">Select Complaint</label>
          <input list="us-complaint-list" id="us-ticket-select" class="form-input" value="${selectedTicket}" placeholder="Complaint Ticket ID">
          <datalist id="us-complaint-list">
            ${list.map(comp => `<option value="${comp.ticketId}">${comp.ticketId} — ${comp.title||'Untitled'}</option>`).join('')}
          </datalist>
        </div>
        ${c ? `
        <div class="card" style="margin-bottom:20px;">
          <div style="font-weight:700;font-size:15px;margin-bottom:4px;">${c.title || 'Untitled'}</div>
          <div style="font-size:13px;color:#757575;margin-bottom:8px;">${c.category} • ${c.location}</div>
          <div style="display:flex;gap:8px;align-items:center;">
            <span style="font-size:13px;color:#757575;">Current Status:</span>
            <span class="badge ${getStatusBadgeClass(c.status)}">${c.status}</span>
          </div>
        </div>
        ${(c.photoUrl || c.videoUrl || c.voiceUrl) ? `
        <div style="margin-bottom:20px;">
          <div style="font-weight:700;font-size:15px;color:#555;margin-bottom:12px;">Attachments from Student</div>
          <div style="display:flex;gap:12px;">
            ${c.photoUrl ? `<button id="us-photo-btn" style="flex:1;background:#607D8B;color:white;border:none;border-radius:12px;padding:10px;font-size:13px;font-weight:600;cursor:pointer;">📸 Photo</button>` : ''}
            ${c.videoUrl ? `<button id="us-video-btn" style="flex:1;background:#607D8B;color:white;border:none;border-radius:12px;padding:10px;font-size:13px;font-weight:600;cursor:pointer;">🎥 Video</button>` : ''}
            ${c.voiceUrl ? `<button id="us-voice-btn" style="flex:1;background:#607D8B;color:white;border:none;border-radius:12px;padding:10px;font-size:13px;font-weight:600;cursor:pointer;">🎙 Voice</button>` : ''}
          </div>
        </div>
        ` : ''}
        ` : ''}
        <div class="form-group">
          <label class="form-label">New Status</label>
          <div style="display:flex;flex-direction:column;gap:10px;" id="status-radio-group">
            ${STATUSES.map(s => `
            <label style="display:flex;align-items:center;gap:10px;padding:12px 16px;background:${selectedStatus===s?'#E3F2FD':'white'};border:2px solid ${selectedStatus===s?'#1565C0':'#E0E0E0'};border-radius:12px;cursor:pointer;transition:all 0.2s;">
              <input type="radio" name="us-status" value="${s}" ${selectedStatus===s?'checked':''} style="accent-color:#1565C0;" />
              <span style="font-size:14px;font-weight:${selectedStatus===s?'700':'500'};color:${selectedStatus===s?'#1565C0':'#333'};">${s}</span>
            </label>`).join('')}
          </div>
        </div>
        <div id="us-error" class="alert alert-error alert-hidden"></div>
        <button id="us-update-btn" class="btn btn-primary btn-lg" ${!selectedStatus?'disabled':''}>Update Status</button>
      </div>
    </div>`;

    document.getElementById('us-back').addEventListener('click', goBack);

    document.getElementById('us-ticket-select')?.addEventListener('change', e => {
      selectedTicket = e.target.value;
      selectedStatus = '';
      render();
    });

    document.getElementById('us-photo-btn')?.addEventListener('click', () => { openAttachment(c.photoUrl, 'image'); });
    document.getElementById('us-video-btn')?.addEventListener('click', () => { openAttachment(c.videoUrl, 'video'); });
    document.getElementById('us-voice-btn')?.addEventListener('click', () => { openAttachment(c.voiceUrl, 'audio'); });

    document.querySelectorAll('input[name="us-status"]').forEach(radio => {
      radio.addEventListener('change', e => {
        selectedStatus = e.target.value;
        render();
      });
    });

    document.getElementById('us-update-btn')?.addEventListener('click', async () => {
      if (!selectedStatus || !selectedTicket) return;
      const btn = document.getElementById('us-update-btn');
      const errEl = document.getElementById('us-error');
      btn.textContent = 'Updating…'; btn.disabled = true;
      try {
        await updateComplaintStatus(selectedTicket, selectedStatus);
        showToast(`Status updated to "${selectedStatus}" ✅`, 'success');
        selectedStatus = '';
        goBack();
      } catch (e) {
        errEl.textContent = e.message || 'Update failed.';
        errEl.classList.remove('alert-hidden');
        btn.textContent = 'Update Status'; btn.disabled = false;
      }
    });
  }

  render();
}
