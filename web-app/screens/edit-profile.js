// edit-profile.js — Mirrors EditProfileScreen.kt
import { AppState, goBack } from '../app.js';
import { updateUserProfile } from './firestore-service.js';
import { showToast, resizeImageToBase64 } from './utils.js';

const YEARS = ['1st Year', '2nd Year', '3rd Year', '4th Year', 'PG'];

export function renderEditProfile(container) {
  const u = AppState.currentUserData || {};
  let newPhotoBase64 = null;

  container.innerHTML = `
  <div class="screen active" style="min-height:100vh;display:flex;flex-direction:column;background:#F5F7FA;">
    <div class="app-bar">
      <button class="back-btn" id="ep-back">←</button>
      <span class="app-bar-title">Edit Profile</span>
    </div>
    <div style="padding:20px;overflow-y:auto;flex:1;padding-bottom:60px;display:flex;flex-direction:column;align-items:center;">
      <!-- Photo picker -->
      <div style="position:relative;margin-bottom:20px;cursor:pointer;" id="ep-photo-picker">
        <div id="ep-avatar" style="width:90px;height:90px;border-radius:50%;background:#1565C0;display:flex;align-items:center;justify-content:center;font-size:36px;font-weight:700;color:white;overflow:hidden;box-shadow:0 4px 16px rgba(21,101,192,0.3);">
          ${u.profilePhoto && u.profilePhoto.startsWith('base64:')
            ? `<img src="data:image/jpeg;base64,${u.profilePhoto.replace('base64:','')}" style="width:100%;height:100%;object-fit:cover;" />`
            : (u.name||'U')[0].toUpperCase()}
        </div>
        <div style="position:absolute;bottom:0;right:0;width:28px;height:28px;background:#1565C0;border-radius:50%;display:flex;align-items:center;justify-content:center;font-size:14px;border:2px solid white;">📷</div>
      </div>
      <input type="file" id="ep-photo-input" accept="image/*" style="display:none;" />
      <div class="card" style="width:100%;max-width:480px;">
        <h3 style="font-size:15px;font-weight:700;color:#555;margin-bottom:16px;">Edit Information</h3>
        <div class="form-group">
          <label class="form-label">Full Name</label>
          <input type="text" id="ep-name" class="form-input" value="${u.name || ''}" />
        </div>
        <div class="form-group">
          <label class="form-label">Phone</label>
          <input type="tel" id="ep-phone" class="form-input" value="${u.phone || ''}" />
        </div>
        ${u.role === 'student' ? `
        <div class="form-group">
          <label class="form-label">Year</label>
          <select id="ep-year" class="form-select">
            ${YEARS.map(y => `<option value="${y}" ${u.year===y?'selected':''}>${y}</option>`).join('')}
          </select>
        </div>
        <div class="form-group">
          <label class="form-label">Register No</label>
          <input type="text" id="ep-regno" class="form-input" value="${u.regNo || ''}" />
        </div>` : ''}
        <div id="ep-error" class="alert alert-error alert-hidden"></div>
        <button id="ep-save" class="btn btn-primary btn-lg" style="margin-top:8px;">Save Changes</button>
      </div>
    </div>
  </div>`;

  document.getElementById('ep-back').addEventListener('click', goBack);

  // Photo picker
  document.getElementById('ep-photo-picker').addEventListener('click', () => {
    document.getElementById('ep-photo-input').click();
  });
  document.getElementById('ep-photo-input').addEventListener('change', async e => {
    if (e.target.files[0]) {
      newPhotoBase64 = await resizeImageToBase64(e.target.files[0]);
      const avatar = document.getElementById('ep-avatar');
      avatar.innerHTML = `<img src="data:image/jpeg;base64,${newPhotoBase64.replace('base64:','')}" style="width:100%;height:100%;object-fit:cover;" />`;
    }
  });

  document.getElementById('ep-save').addEventListener('click', async () => {
    const errEl = document.getElementById('ep-error');
    const btn = document.getElementById('ep-save');
    btn.textContent = 'Saving…'; btn.disabled = true;

    const updates = {
      name: document.getElementById('ep-name').value.trim(),
      phone: document.getElementById('ep-phone').value.trim(),
    };
    if (u.role === 'student') {
      updates.year = document.getElementById('ep-year')?.value || '';
      updates.regNo = document.getElementById('ep-regno')?.value.trim() || '';
    }
    if (newPhotoBase64) updates.profilePhoto = newPhotoBase64;

    try {
      await updateUserProfile(u.uid, updates);
      // Update local state
      AppState.currentUserData = { ...AppState.currentUserData, ...updates };
      showToast('Profile updated ✅', 'success');
      goBack();
    } catch (e) {
      errEl.textContent = e.message || 'Update failed.';
      errEl.classList.remove('alert-hidden');
      btn.textContent = 'Save Changes'; btn.disabled = false;
    }
  });
}
