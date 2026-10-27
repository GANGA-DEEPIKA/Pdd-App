// update-status.js — Mirrors UpdateStatusScreen.kt
import { AppState, navigateTo, goBack } from '../app.js';
import { updateComplaintStatus } from './firestore-service.js';
import { showToast, openAttachment } from './utils.js';

window.openAttachment = openAttachment;

const STATUSES = ['Assigned', 'In Progress', 'Resolved'];

export function renderUpdateStatus(container) {
  const list = AppState.complaintList;
  const initial = AppState.complaintData?.ticketId || (list[0]?.ticketId || '');

  let selectedTicket = initial;
  let selectedStatus = '';

  function getComplaint() { return list.find(c => c.ticketId === selectedTicket) || null; }

  function render() {
    const c = getComplaint();

    container.innerHTML = `
    <style>
      .us-screen { min-height:100vh; display:flex; flex-direction:column; background:#F5F7FA; }
      .us-appbar { display:flex; align-items:center; padding:16px; background:#fff; border-bottom:1px solid #EEE; }
      .us-back-btn { background:none; border:none; font-size:20px; cursor:pointer; color:#333; margin-right:8px; }
      .us-title { font-size:17px; font-weight:700; color:#111; }
      .us-body { padding:20px 16px; overflow-y:auto; flex:1; padding-bottom:30px; }
      .us-section-label { font-size:14px; font-weight:700; color:#222; margin-bottom:10px; margin-top:20px; }
      .us-section-label:first-child { margin-top:0; }
      /* Ticket radio cards */
      .us-ticket-card { display:flex; align-items:center; gap:14px; background:#fff; border:2px solid #E0E0E0; border-radius:12px; padding:14px 16px; margin-bottom:10px; cursor:pointer; transition:border 0.15s, background 0.15s; }
      .us-ticket-card.selected { border-color:#1565C0; background:#EEF5FF; }
      .us-radio { width:20px; height:20px; border-radius:50%; border:2px solid #BDBDBD; display:flex; align-items:center; justify-content:center; flex-shrink:0; }
      .us-ticket-card.selected .us-radio { border-color:#1565C0; }
      .us-radio-inner { width:10px; height:10px; border-radius:50%; background:#1565C0; display:none; }
      .us-ticket-card.selected .us-radio-inner { display:block; }
      .us-ticket-id { font-size:14px; font-weight:700; color:#1565C0; }
      .us-ticket-title { font-size:12px; color:#777; margin-top:2px; }
      /* Complaint details box */
      .us-detail-box { background:#fff; border:1px solid #E0E0E0; border-radius:10px; padding:14px; font-size:14px; color:#333; min-height:48px; line-height:1.5; }
      /* Status toggle buttons */
      .us-status-row { display:flex; gap:10px; flex-wrap:wrap; }
      .us-status-btn { flex:1; min-width:90px; padding:12px 8px; border-radius:10px; border:1.5px solid #DDD; background:#fff; font-size:13px; font-weight:600; color:#555; cursor:pointer; transition:all 0.15s; text-align:center; }
      .us-status-btn.active { background:#1565C0; color:#fff; border-color:#1565C0; }
      /* Attachments */
      .us-attachment-btn { width:100%; background:#607D8B; color:#fff; border:none; border-radius:12px; padding:14px; font-size:14px; font-weight:600; cursor:pointer; display:flex; align-items:center; justify-content:center; gap:8px; }
      .us-attachment-btn:hover { background:#546E7A; }
      /* Confirm button */
      .us-confirm-btn { width:100%; background:#1565C0; color:#fff; border:none; border-radius:14px; padding:16px; font-size:16px; font-weight:700; cursor:pointer; margin-top:24px; transition:opacity 0.2s; }
      .us-confirm-btn:disabled { opacity:0.45; cursor:not-allowed; }
      .us-confirm-btn:not(:disabled):hover { background:#1976D2; }
    </style>
    <div class="us-screen">
      <div class="us-appbar">
        <button class="us-back-btn" id="us-back">←</button>
        <span class="us-title">Update Ticket Status</span>
      </div>
      <div class="us-body">

        <div class="us-section-label">Select a Ticket</div>
        ${list.map(comp => `
        <div class="us-ticket-card${selectedTicket === comp.ticketId ? ' selected' : ''}" data-tid="${comp.ticketId}">
          <div class="us-radio"><div class="us-radio-inner"></div></div>
          <div>
            <div class="us-ticket-id">${comp.ticketId}</div>
            <div class="us-ticket-title">${(comp.title || 'Untitled').toUpperCase()}</div>
          </div>
        </div>`).join('')}

        <div class="us-section-label">Complaint Details</div>
        <div class="us-detail-box">${c?.description || c?.title || '—'}</div>

        <div class="us-section-label">Choose New Status</div>
        <div class="us-status-row">
          ${STATUSES.map(s => `
          <button class="us-status-btn${selectedStatus === s ? ' active' : ''}" data-status="${s}">${s}</button>`).join('')}
        </div>

        ${(c?.photoUrl || c?.videoUrl || c?.voiceUrl) ? `
        <div class="us-section-label">Attachments from Student</div>
        <div style="display:flex;flex-direction:column;gap:10px;">
          ${c.photoUrl ? `<button class="us-attachment-btn" id="us-photo-btn">📸 Photo</button>` : ''}
          ${c.videoUrl ? `<button class="us-attachment-btn" id="us-video-btn">🎥 Video</button>` : ''}
          ${c.voiceUrl ? `<button class="us-attachment-btn" id="us-voice-btn">🎙 Voice</button>` : ''}
        </div>` : ''}

        <button class="us-confirm-btn" id="us-confirm-btn" ${!selectedStatus ? 'disabled' : ''}>Confirm Update</button>
        <div id="us-error" style="color:#C62828;font-size:13px;margin-top:10px;display:none;"></div>
      </div>
    </div>`;

    document.getElementById('us-back').addEventListener('click', goBack);

    // Ticket selection
    document.querySelectorAll('.us-ticket-card').forEach(card => {
      card.addEventListener('click', () => {
        selectedTicket = card.dataset.tid;
        selectedStatus = '';
        render();
      });
    });

    // Status buttons
    document.querySelectorAll('.us-status-btn').forEach(btn => {
      btn.addEventListener('click', () => {
        selectedStatus = btn.dataset.status;
        render();
      });
    });

    // Attachments
    document.getElementById('us-photo-btn')?.addEventListener('click', () => openAttachment(c.photoUrl, 'image'));
    document.getElementById('us-video-btn')?.addEventListener('click', () => openAttachment(c.videoUrl, 'video'));
    document.getElementById('us-voice-btn')?.addEventListener('click', () => openAttachment(c.voiceUrl, 'audio'));

    // Confirm
    document.getElementById('us-confirm-btn')?.addEventListener('click', async () => {
      if (!selectedStatus || !selectedTicket) return;
      const btn = document.getElementById('us-confirm-btn');
      const errEl = document.getElementById('us-error');
      btn.textContent = 'Updating…'; btn.disabled = true;
      try {
        await updateComplaintStatus(selectedTicket, selectedStatus);
        showToast(`Status updated to "${selectedStatus}" ✅`, 'success');
        goBack();
      } catch (e) {
        errEl.textContent = e.message || 'Update failed.';
        errEl.style.display = 'block';
        btn.textContent = 'Confirm Update'; btn.disabled = false;
      }
    });
  }

  render();
}
