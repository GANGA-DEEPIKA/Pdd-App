// student-profile.js — Mirrors StudentProfileScreen.kt
import { AppState, navigateTo, goBack } from '../app.js';

export function renderStudentProfile(container) {
  const u = AppState.currentUserData || {};
  const initial = (u.name || 'U')[0].toUpperCase();
  const hasPhoto = u.profilePhoto && u.profilePhoto.startsWith('base64:');

  container.innerHTML = `
  <div class="screen active" style="min-height:100vh;display:flex;flex-direction:column;background:#F5F7FA;">
    <div class="app-bar">
      <button class="back-btn" id="sp-back">←</button>
      <span class="app-bar-title">My Profile</span>
      <button id="sp-edit" style="margin-left:auto;background:none;border:none;color:#1565C0;font-weight:700;font-size:14px;cursor:pointer;padding:8px 12px;border-radius:8px;">Edit ✏️</button>
    </div>
    <div style="padding:24px;overflow-y:auto;flex:1;padding-bottom:60px;display:flex;flex-direction:column;align-items:center;">
      <!-- Avatar -->
      <div style="width:90px;height:90px;border-radius:50%;background:${hasPhoto?'transparent':'#1565C0'};display:flex;align-items:center;justify-content:center;font-size:36px;font-weight:700;color:white;margin-bottom:16px;overflow:hidden;box-shadow:0 4px 16px rgba(21,101,192,0.3);">
        ${hasPhoto ? `<img src="data:image/jpeg;base64,${u.profilePhoto.replace('base64:','')}" style="width:100%;height:100%;object-fit:cover;" />` : initial}
      </div>
      <h2 style="font-size:22px;font-weight:800;">${u.name || 'Student'}</h2>
      <p style="font-size:14px;color:#757575;margin-top:4px;">${u.email || ''}</p>
      <span style="background:#E3F2FD;color:#1565C0;padding:4px 12px;border-radius:20px;font-size:12px;font-weight:700;margin-top:8px;">🎓 STUDENT</span>
      <div class="card" style="width:100%;max-width:480px;margin-top:24px;">
        <h3 style="font-size:15px;font-weight:700;color:#555;margin-bottom:16px;">Profile Information</h3>
        ${profileRow('🎓 College', u.collegeName)}
        ${u.regNo ? profileRow('🆔 Register No', u.regNo) : ''}
        ${u.year ? profileRow('📅 Year', u.year) : ''}
        ${u.phone ? profileRow('📱 Phone', u.phone) : ''}
        ${u.department ? profileRow('🏢 Department', u.department) : ''}
      </div>
      <!-- Logout Button -->
      <button id="sp-logout" style="width:100%;max-width:480px;margin-top:40px;background:white;border:1px solid #D32F2F;color:#D32F2F;font-size:16px;font-weight:700;padding:14px;border-radius:12px;cursor:pointer;transition:background 0.2s;">
        Logout from Device
      </button>
    </div>
  </div>`;

  function profileRow(label, value) {
    if (!value) return '';
    return `<div style="display:flex;align-items:center;justify-content:space-between;padding:12px 0;border-bottom:1px solid #F5F5F5;">
      <span style="font-size:14px;color:#757575;">${label}</span>
      <span style="font-size:14px;font-weight:600;color:#333;">${value}</span>
    </div>`;
  }

  document.getElementById('sp-back').addEventListener('click', goBack);
  document.getElementById('sp-edit').addEventListener('click', () => navigateTo('editProfile'));
  
  document.getElementById('sp-logout').addEventListener('click', async () => {
    const { auth } = await import('../firebase-config.js');
    const { signOut } = await import('https://www.gstatic.com/firebasejs/10.12.2/firebase-auth.js');
    await signOut(auth);
    AppState.currentUserData = null;
    AppState.complaintList = [];
    AppState.screen = 'college';
    AppState.navigationStack = [];
    const { renderCurrentScreen } = await import('../app.js');
    renderCurrentScreen();
  });
}
