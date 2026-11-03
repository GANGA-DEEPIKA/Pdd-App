// review-complaint.js — Mirrors ReviewComplaintScreen.kt
import { AppState, navigateTo, goBack } from '../app.js';
import { submitComplaint, detectDuplicateComplaint } from './firestore-service.js';
import { showToast, getStatusBadgeClass } from './utils.js';

export function renderReviewComplaint(container) {
  const c = AppState.complaintData || {};
  let currentDuplicate = null;

  container.innerHTML = `
  <div class="screen active" style="min-height:100vh;display:flex;flex-direction:column;background:#F5F7FA;">
    <div class="app-bar">
      <button class="back-btn" id="rv-back">←</button>
      <span class="app-bar-title">Review Complaint</span>
    </div>
    <div style="padding:20px;overflow-y:auto;flex:1;padding-bottom:60px;">
      <div style="background:#E3F2FD;border-radius:16px;padding:16px;margin-bottom:20px;display:flex;gap:12px;align-items:flex-start;">
        <span style="font-size:24px;">ℹ️</span>
        <div>
          <div style="font-weight:700;color:#1565C0;font-size:14px;">Please review before submitting</div>
          <div style="font-size:13px;color:#1565C0;opacity:0.8;margin-top:2px;">Once submitted, you'll receive a ticket ID for tracking.</div>
        </div>
      </div>
      <div class="card" style="margin-bottom:16px;">
        <h3 style="font-size:15px;font-weight:700;color:#1565C0;margin-bottom:16px;">Complaint Details</h3>
        ${row('Subject', c.title)}
        ${row('Category', c.category || 'General Maintenance')}
        ${row('Department', c.department || 'Estate Maintenance Department')}
        ${row('Location', c.location)}
        ${row('Priority', c.priority || 'Medium')}
        ${row('College', c.collegeName)}
        ${c.details ? `
        <div style="margin-top:12px;">
          <div style="font-size:12px;color:#757575;margin-bottom:4px;">Description</div>
          <div style="background:#F5F7FA;padding:12px;border-radius:8px;font-size:14px;color:#333;line-height:1.5;">${c.details}</div>
        </div>` : ''}
      </div>
      <!-- Attachments preview -->
      <div class="card" style="margin-bottom:20px;">
        <h3 style="font-size:14px;font-weight:700;color:#555;margin-bottom:12px;">Attachments</h3>
        ${c._photoFile ? `<div style="font-size:13px;color:#2E7D32;">✅ Photo: ${c._photoFile.name}</div>` : '<div style="font-size:13px;color:#BDBDBD;">No photo</div>'}
        ${c._audioBlob ? `<div style="font-size:13px;color:#2E7D32;margin-top:4px;">✅ Voice recording attached</div>` : ''}
      </div>
      <div id="rv-error" class="alert alert-error alert-hidden"></div>
      <button id="rv-confirm" class="btn btn-primary btn-lg" style="margin-bottom:12px;">✅ Confirm &amp; Submit</button>
      <button id="rv-edit" class="btn btn-secondary btn-lg">✏️ Edit Complaint</button>
    </div>

    <!-- AI Duplicate Alert Modal -->
    <div id="duplicate-modal" class="modal-overlay center" style="display:none;padding:16px;">
      <div class="modal center-modal" style="box-shadow:0 12px 36px rgba(0,0,0,0.25);border:1px solid #FFE082;">
        <div style="display:flex;align-items:center;gap:12px;margin-bottom:12px;">
          <span style="font-size:32px;">⚠️</span>
          <div>
            <div style="font-size:18px;font-weight:800;color:#D84315;">Potential Duplicate Detected</div>
            <div id="dup-badge" style="font-size:12px;font-weight:700;background:#FFF3E0;color:#E65100;padding:2px 8px;border-radius:12px;display:inline-block;margin-top:2px;">AI Match: 85% Confidence</div>
          </div>
        </div>
        <p id="dup-reason" style="font-size:13px;color:#555;margin-bottom:16px;line-height:1.4;">
          A similar complaint has already been submitted for this location and category:
        </p>

        <!-- Preview of existing complaint -->
        <div style="background:#F8F9FA;border:1px solid #E0E0E0;border-radius:12px;padding:14px;margin-bottom:20px;">
          <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:8px;">
            <span style="font-size:11px;font-weight:700;color:#757575;letter-spacing:0.5px;">EXISTING COMPLAINT</span>
            <span id="dup-status" class="badge badge-warning" style="font-size:11px;">Pending</span>
          </div>
          <div style="font-size:12px;color:#616161;margin-bottom:2px;">Ticket ID: <strong id="dup-ticket-id" style="color:#1565C0;font-family:monospace;">CMP12345</strong></div>
          <div style="font-size:14px;font-weight:700;color:#212121;margin-bottom:4px;" id="dup-title">Broken Fan</div>
          <div style="font-size:12px;color:#555;margin-bottom:2px;">📍 Location: <span id="dup-location" style="font-weight:600;">Block A, Room 101</span></div>
          <div style="font-size:12px;color:#555;margin-bottom:6px;">🏷️ Category: <span id="dup-category" style="font-weight:600;">Electrical</span></div>
          <div id="dup-details-box" style="font-size:12px;color:#666;background:#FFF;padding:8px;border-radius:6px;border:1px solid #EEE;line-height:1.4;max-height:80px;overflow-y:auto;"></div>
        </div>

        <!-- Action buttons -->
        <div style="display:flex;flex-direction:column;gap:10px;">
          <button id="dup-view-btn" class="btn btn-secondary btn-lg" style="border:1.5px solid #1565C0;color:#1565C0;font-weight:700;">
            👁️ View Existing Complaint
          </button>
          <button id="dup-submit-anyway-btn" class="btn btn-primary btn-lg" style="background:#E65100;border:none;font-weight:700;">
            ⚠️ Submit Anyway
          </button>
          <button id="dup-cancel-btn" style="background:none;border:none;color:#757575;padding:8px;font-size:13px;font-weight:600;cursor:pointer;">
            ✏️ Edit My Complaint
          </button>
        </div>
      </div>
    </div>
  </div>`;

  function row(label, value) {
    return `<div style="display:flex;justify-content:space-between;padding:10px 0;border-bottom:1px solid #F0F0F0;">
      <span style="font-size:13px;color:#757575;">${label}</span>
      <span style="font-size:13px;font-weight:600;color:#212121;text-align:right;max-width:60%;">${value || '—'}</span>
    </div>`;
  }

  const dupModal = document.getElementById('duplicate-modal');
  const dupBadge = document.getElementById('dup-badge');
  const dupReason = document.getElementById('dup-reason');
  const dupTicketId = document.getElementById('dup-ticket-id');
  const dupStatus = document.getElementById('dup-status');
  const dupTitle = document.getElementById('dup-title');
  const dupLocation = document.getElementById('dup-location');
  const dupCategory = document.getElementById('dup-category');
  const dupDetailsBox = document.getElementById('dup-details-box');
  const dupViewBtn = document.getElementById('dup-view-btn');
  const dupSubmitAnywayBtn = document.getElementById('dup-submit-anyway-btn');
  const dupCancelBtn = document.getElementById('dup-cancel-btn');

  function showDuplicateAlert(dup) {
    currentDuplicate = dup;
    const match = dup.matchedComplaint;
    dupBadge.textContent = `🤖 AI Match: ${dup.confidence}% Confidence`;
    dupReason.textContent = dup.reason || 'A similar complaint with matching category, location, and description already exists:';
    dupTicketId.textContent = match.ticketId || 'N/A';
    dupStatus.textContent = match.status || 'Pending';
    dupStatus.className = `badge ${getStatusBadgeClass(match.status)}`;
    dupTitle.textContent = match.title || 'Complaint';
    dupLocation.textContent = match.location || '—';
    dupCategory.textContent = match.category || '—';
    dupDetailsBox.textContent = match.details || match.title || 'No additional details provided.';
    dupModal.style.display = 'flex';
  }

  function hideDuplicateAlert() {
    dupModal.style.display = 'none';
  }

  dupCancelBtn.addEventListener('click', hideDuplicateAlert);

  dupViewBtn.addEventListener('click', () => {
    hideDuplicateAlert();
    if (currentDuplicate && currentDuplicate.matchedComplaint) {
      AppState.complaintData = { ...currentDuplicate.matchedComplaint };
      navigateTo('complaintStatus');
    }
  });

  dupSubmitAnywayBtn.addEventListener('click', async () => {
    hideDuplicateAlert();
    await doSubmit();
  });

  document.getElementById('rv-back').addEventListener('click', goBack);
  document.getElementById('rv-edit').addEventListener('click', goBack);

  async function doSubmit() {
    const btn = document.getElementById('rv-confirm');
    const errEl = document.getElementById('rv-error');
    btn.textContent = 'Submitting…'; btn.disabled = true;
    errEl.classList.add('alert-hidden');
    try {
      const result = await submitComplaint(
        AppState.complaintData,
        AppState.complaintData._photoFile,
        AppState.complaintData._audioBlob
      );
      AppState.complaintData = { ...AppState.complaintData, ...result };
      showToast('Complaint submitted successfully! ✅', 'success');
      // Pop the reviewComplaint screen from the history stack so Back goes to dashboard
      const idx = AppState.navigationStack.lastIndexOf('newComplaint');
      if (idx >= 0) AppState.navigationStack.splice(idx, 1);
      navigateTo('complaintStatus');
    } catch (e) {
      errEl.textContent = e.message || 'Submission failed. Please try again.';
      errEl.classList.remove('alert-hidden');
      btn.textContent = '✅ Confirm & Submit'; btn.disabled = false;
    }
  }

  document.getElementById('rv-confirm').addEventListener('click', async () => {
    const btn = document.getElementById('rv-confirm');
    btn.textContent = 'Analyzing for duplicates… 🤖'; btn.disabled = true;

    try {
      const dup = await detectDuplicateComplaint(AppState.complaintData);
      if (dup && dup.isDuplicate && dup.matchedComplaint) {
        btn.textContent = '✅ Confirm & Submit'; btn.disabled = false;
        showDuplicateAlert(dup);
        return;
      }
    } catch (checkErr) {
      console.warn('AI duplicate check skipped:', checkErr);
    }

    await doSubmit();
  });
}
