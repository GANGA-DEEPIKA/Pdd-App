// global-settings.js — Mirrors GlobalSettingsScreen.kt
import { AppState, goBack } from '../app.js';
import { showToast } from './utils.js';

export function renderGlobalSettings(container) {
  container.innerHTML = `
  <div class="screen active" style="min-height:100vh;display:flex;flex-direction:column;background:#F5F7FA;">
    <div class="app-bar app-bar-dark">
      <button class="back-btn back-btn-white" id="gs-back">←</button>
      <span class="app-bar-title" style="color:white;">Global Settings</span>
    </div>
    <div style="padding:20px;overflow-y:auto;flex:1;padding-bottom:60px;">
      <div class="card" style="margin-bottom:16px;">
        <h3 style="font-size:15px;font-weight:700;color:#555;margin-bottom:16px;">System Information</h3>
        ${settingRow('🏫 College', AppState.currentUserData?.collegeName || AppState.collegeName)}
        ${settingRow('👤 Admin', AppState.currentUserData?.name || 'Admin')}
        ${settingRow('📧 Email', AppState.currentUserData?.email || '')}
        ${settingRow('🎫 Total Complaints', String(AppState.complaintList.length))}
        ${settingRow('✅ Resolved', String(AppState.complaintList.filter(c=>c.status==='Resolved').length))}
      </div>
      <div class="card" style="margin-bottom:16px;">
        <h3 style="font-size:15px;font-weight:700;color:#555;margin-bottom:16px;">Preferences</h3>
        <div style="display:flex;align-items:center;justify-content:space-between;padding:12px 0;border-bottom:1px solid #F5F5F5;">
          <div>
            <div style="font-weight:600;font-size:14px;">Real-time Updates</div>
            <div style="font-size:12px;color:#757575;">Auto-refresh complaint data</div>
          </div>
          <div style="width:44px;height:24px;background:#4CAF50;border-radius:12px;position:relative;">
            <div style="position:absolute;right:2px;top:2px;width:20px;height:20px;background:white;border-radius:50%;"></div>
          </div>
        </div>
        <div style="display:flex;align-items:center;justify-content:space-between;padding:12px 0;">
          <div>
            <div style="font-weight:600;font-size:14px;">Firebase Project</div>
            <div style="font-size:12px;color:#757575;">campusfix-d8833</div>
          </div>
          <span style="font-size:12px;background:#E8F5E9;color:#2E7D32;padding:4px 8px;border-radius:8px;font-weight:600;">Connected ✅</span>
        </div>
      </div>
      <div class="card">
        <h3 style="font-size:15px;font-weight:700;color:#555;margin-bottom:16px;">About CampusFix</h3>
        ${settingRow('📱 Platform', 'Web Application')}
        ${settingRow('🔗 Backend', 'Firebase Firestore')}
        ${settingRow('🔒 Auth', 'Firebase Auth')}
        ${settingRow('🔄 Sync', 'Real-time (bi-directional)')}
        <div style="padding-top:12px;font-size:12px;color:#9E9E9E;text-align:center;">CampusFix Web App • Synced with Android App<br/>Powered by Firebase</div>
      </div>
    </div>
  </div>`;

  function settingRow(label, value) {
    return `<div style="display:flex;justify-content:space-between;align-items:center;padding:12px 0;border-bottom:1px solid #F5F5F5;">
      <span style="font-size:14px;color:#757575;">${label}</span>
      <span style="font-size:14px;font-weight:600;color:#333;max-width:60%;text-align:right;">${value||'—'}</span>
    </div>`;
  }

  document.getElementById('gs-back').addEventListener('click', goBack);
}
