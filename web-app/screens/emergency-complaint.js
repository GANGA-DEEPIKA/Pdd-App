// emergency-complaint.js — Mirrors EmergencyComplaintScreen.kt
import { AppState, goBack } from '../app.js';
import { submitComplaint } from './firestore-service.js';
import { showToast } from './utils.js';

export function renderEmergencyComplaint(container) {
  container.innerHTML = `
  <div class="screen active" style="min-height:100vh;display:flex;flex-direction:column;background:#F5F7FA;">
    <div class="app-bar" style="background:#B71C1C;">
      <button class="back-btn back-btn-white" id="ec-back">←</button>
      <span class="app-bar-title" style="color:white;">🚨 Emergency Report</span>
    </div>
    <div style="padding:20px;overflow-y:auto;flex:1;padding-bottom:60px;">
      <div style="background:#FFEBEE;border:2px solid #F44336;border-radius:16px;padding:16px;margin-bottom:20px;display:flex;gap:12px;align-items:flex-start;">
        <span style="font-size:24px;">🚨</span>
        <div>
          <div style="font-weight:700;color:#B71C1C;font-size:15px;">Emergency Priority</div>
          <div style="font-size:13px;color:#C62828;margin-top:2px;">This complaint will be flagged as high-priority and escalated immediately to the admin and available staff.</div>
        </div>
      </div>
      <div class="card" style="margin-bottom:16px;">
        <h3 style="font-size:15px;font-weight:700;color:#B71C1C;margin-bottom:16px;">Emergency Details</h3>
        <div class="form-group">
          <label class="form-label">Emergency Title *</label>
          <input type="text" id="em-title" class="form-input" placeholder="e.g. Electrical short circuit" />
        </div>
        <div class="form-group">
          <label class="form-label">Description</label>
          <textarea id="em-details" class="form-textarea" placeholder="Describe the emergency clearly…"></textarea>
        </div>
        <div class="form-group">
          <label class="form-label">Location *</label>
          <input type="text" id="em-location" class="form-input" placeholder="Where is the emergency?" />
        </div>
        <div class="form-group">
          <label class="form-label">Category</label>
          <select id="em-category" class="form-select">
            <option value="Electrical">Electrical</option>
            <option value="Plumbing">Plumbing</option>
            <option value="Security">Security</option>
            <option value="Fire/Safety">Fire/Safety</option>
            <option value="Medical">Medical</option>
            <option value="General Maintenance">General Maintenance</option>
          </select>
        </div>
      </div>
      <div id="em-error" class="alert alert-error alert-hidden"></div>
      <button id="em-submit" class="btn btn-lg" style="background:#D32F2F;color:white;border-radius:16px;">🚨 Submit Emergency Report</button>
    </div>
  </div>`;

  document.getElementById('ec-back').addEventListener('click', goBack);
  document.getElementById('em-submit').addEventListener('click', async () => {
    const title = document.getElementById('em-title').value.trim();
    const location = document.getElementById('em-location').value.trim();
    const errEl = document.getElementById('em-error');
    if (!title || !location) { errEl.textContent = 'Please fill required fields.'; errEl.classList.remove('alert-hidden'); return; }
    const btn = document.getElementById('em-submit');
    btn.textContent = 'Submitting…'; btn.disabled = true;
    errEl.classList.add('alert-hidden');
    try {
      await submitComplaint({
        title, details: document.getElementById('em-details').value.trim(), location,
        category: document.getElementById('em-category').value,
        priority: 'High', status: 'Pending',
        collegeName: AppState.currentUserData?.collegeName || AppState.collegeName,
      }, null, null);
      showToast('🚨 Emergency reported successfully!', 'success');
      goBack();
    } catch (e) {
      errEl.textContent = e.message || 'Submission failed.';
      errEl.classList.remove('alert-hidden');
      btn.textContent = '🚨 Submit Emergency Report'; btn.disabled = false;
    }
  });
}
