// upload-proof.js — Mirrors UploadProofScreen.kt
import { AppState, goBack } from '../app.js';
import { uploadStaffProof } from './firestore-service.js';
import { showToast, getStatusBadgeClass } from './utils.js';

export function renderUploadProof(container) {
  const list = AppState.complaintList;
  const initialTicket = AppState.complaintData?.ticketId || '';
  let selectedTicket = initialTicket || '';
  let proofFile = null;

  function render() {
    const c = list.find(comp => comp.ticketId === selectedTicket) || null;
    container.innerHTML = `
    <div class="screen active" style="min-height:100vh;display:flex;flex-direction:column;background:#F5F7FA;">
      <div class="app-bar">
        <button class="back-btn" id="up-back">←</button>
        <span class="app-bar-title">Upload Proof</span>
      </div>
      <div style="padding:20px;overflow-y:auto;flex:1;padding-bottom:60px;">
        <div class="form-group">
          <label class="form-label">Select Complaint</label>
          <input list="up-complaint-list" id="up-ticket-select" class="form-input" value="${selectedTicket}" placeholder="Complaint Ticket ID">
          <datalist id="up-complaint-list">
            ${list.map(comp => `<option value="${comp.ticketId}">${comp.ticketId} — ${comp.title||'Untitled'}</option>`).join('')}
          </datalist>
        </div>
        ${c ? `
        <div class="card" style="margin-bottom:20px;">
          <div style="font-weight:700;font-size:15px;margin-bottom:4px;">${c.title}</div>
          <div style="font-size:13px;color:#757575;margin-bottom:8px;">${c.category} • ${c.location}</div>
          <span class="badge ${getStatusBadgeClass(c.status)}">${c.status}</span>
        </div>` : ''}
        <div style="margin-bottom:20px;">
          <label class="form-label">Proof Photo</label>
          <div class="attachment-box ${proofFile?'has-file':''}" id="proof-box" style="width:100%;height:120px;margin-top:8px;">
            <span style="font-size:32px;">${proofFile?'✅':'📷'}</span>
            <span class="attachment-label">${proofFile?proofFile.name:'Tap to attach proof photo'}</span>
          </div>
          <input type="file" id="proof-file-input" accept="image/*" style="display:none;" />
        </div>
        <div class="alert alert-info" style="margin-bottom:16px;">
          ℹ️ Uploading proof will automatically mark the complaint as <strong>Resolved</strong>.
        </div>
        <div id="up-error" class="alert alert-error alert-hidden"></div>
        <button id="up-submit-btn" class="btn btn-primary btn-lg">Upload &amp; Resolve</button>
      </div>
    </div>`;

    document.getElementById('up-back').addEventListener('click', goBack);

    document.getElementById('up-ticket-select')?.addEventListener('change', e => {
      selectedTicket = e.target.value;
      proofFile = null;
      render();
    });

    document.getElementById('proof-box')?.addEventListener('click', () => {
      if (proofFile) {
        if (confirm('Remove proof photo?')) { proofFile = null; render(); }
      } else {
        document.getElementById('proof-file-input').click();
      }
    });

    document.getElementById('proof-file-input')?.addEventListener('change', e => {
      if (e.target.files[0]) { proofFile = e.target.files[0]; render(); }
    });

    document.getElementById('up-submit-btn')?.addEventListener('click', async () => {
      const btn = document.getElementById('up-submit-btn');
      const errEl = document.getElementById('up-error');
      btn.textContent = 'Uploading…'; btn.disabled = true;
      try {
        await uploadStaffProof(selectedTicket, proofFile);
        showToast('Proof uploaded & complaint resolved! ✅', 'success');
        goBack();
      } catch (e) {
        errEl.textContent = e.message || 'Upload failed.';
        errEl.classList.remove('alert-hidden');
        btn.textContent = 'Upload & Resolve'; btn.disabled = false;
      }
    });
  }

  render();
}
