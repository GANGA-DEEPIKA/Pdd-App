// review-complaint.js — Mirrors ReviewComplaintScreen.kt
import { AppState, navigateTo, goBack } from '../app.js';
import { submitComplaint } from './firestore-service.js';
import { showToast } from './utils.js';

export function renderReviewComplaint(container) {
  const c = AppState.complaintData || {};

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
        ${row('Category', c.category)}
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
  </div>`;

  function row(label, value) {
    return `<div style="display:flex;justify-content:space-between;padding:10px 0;border-bottom:1px solid #F0F0F0;">
      <span style="font-size:13px;color:#757575;">${label}</span>
      <span style="font-size:13px;font-weight:600;color:#212121;text-align:right;max-width:60%;">${value || '—'}</span>
    </div>`;
  }

  document.getElementById('rv-back').addEventListener('click', goBack);
  document.getElementById('rv-edit').addEventListener('click', goBack);

  document.getElementById('rv-confirm').addEventListener('click', async () => {
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
  });
}
