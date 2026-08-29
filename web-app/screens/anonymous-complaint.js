// anonymous-complaint.js — Mirrors AnonymousComplaintScreen.kt
import { AppState, goBack } from '../app.js';
import { submitComplaint } from './firestore-service.js';
import { showToast, suggestCategory } from './utils.js';

export function renderAnonymousComplaint(container) {
  container.innerHTML = `
  <div class="screen active" style="min-height:100vh;display:flex;flex-direction:column;background:#F5F7FA;">
    <div class="app-bar">
      <button class="back-btn" id="an-back">←</button>
      <span class="app-bar-title">🕵️ Anonymous Report</span>
    </div>
    <div style="padding:20px;overflow-y:auto;flex:1;padding-bottom:60px;">
      <div style="background:#FFF3E0;border:2px solid #FF9800;border-radius:16px;padding:16px;margin-bottom:20px;display:flex;gap:12px;">
        <span style="font-size:24px;">🕵️</span>
        <div>
          <div style="font-weight:700;color:#E65100;font-size:15px;">Anonymous Submission</div>
          <div style="font-size:13px;color:#BF360C;margin-top:2px;">Your identity will be completely hidden. This complaint will show as "anonymous@campus.edu" in the system.</div>
        </div>
      </div>
      <div class="card" style="margin-bottom:16px;">
        <h3 style="font-size:15px;font-weight:700;color:#555;margin-bottom:16px;">Complaint Details</h3>
        <div class="form-group">
          <label class="form-label">Subject *</label>
          <input type="text" id="an-title" class="form-input" placeholder="Brief title of the issue" />
        </div>
        <div class="form-group">
          <label class="form-label">Description</label>
          <textarea id="an-details" class="form-textarea" placeholder="Describe the issue…"></textarea>
        </div>
        <div id="an-cat-tag" class="category-tag" style="display:none;margin-bottom:12px;"></div>
        <div class="form-group">
          <label class="form-label">Location *</label>
          <input type="text" id="an-location" class="form-input" placeholder="Where is the issue?" />
        </div>
      </div>
      <div id="an-error" class="alert alert-error alert-hidden"></div>
      <button id="an-submit" class="btn btn-primary btn-lg">🕵️ Submit Anonymously</button>
    </div>
  </div>`;

  const titleInput = document.getElementById('an-title');
  const detailsInput = document.getElementById('an-details');
  const catTag = document.getElementById('an-cat-tag');

  function updateCat() {
    const text = `${titleInput.value} ${detailsInput.value}`.trim();
    if (text) {
      catTag.textContent = `Category: ${suggestCategory(text)}`;
      catTag.style.display = 'inline-block';
    } else catTag.style.display = 'none';
  }
  titleInput.addEventListener('input', updateCat);
  detailsInput.addEventListener('input', updateCat);

  document.getElementById('an-back').addEventListener('click', goBack);
  document.getElementById('an-submit').addEventListener('click', async () => {
    const title = titleInput.value.trim();
    const location = document.getElementById('an-location').value.trim();
    const errEl = document.getElementById('an-error');
    if (!title || !location) { errEl.textContent = 'Please fill required fields.'; errEl.classList.remove('alert-hidden'); return; }
    const btn = document.getElementById('an-submit');
    btn.textContent = 'Submitting…'; btn.disabled = true;
    errEl.classList.add('alert-hidden');
    try {
      await submitComplaint({
        title, details: detailsInput.value.trim(), location,
        category: suggestCategory(`${title} ${detailsInput.value}`),
        priority: 'Medium', status: 'Pending',
        collegeName: AppState.currentUserData?.collegeName || AppState.collegeName,
        studentUid: 'anonymous', // triggers anonymisation in service
      }, null, null);
      showToast('Anonymous complaint submitted ✅', 'success');
      goBack();
    } catch (e) {
      errEl.textContent = e.message || 'Submission failed.';
      errEl.classList.remove('alert-hidden');
      btn.textContent = '🕵️ Submit Anonymously'; btn.disabled = false;
    }
  });
}
