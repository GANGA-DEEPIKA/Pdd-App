// role-selection.js — Mirrors RoleSelectionScreen.kt
import { AppState, navigateTo, goBack } from '../app.js';

export function renderRoleSelection(container) {
  container.innerHTML = `
  <div class="screen active" style="background:linear-gradient(160deg,#1565C0 0%,#0D47A1 100%);min-height:100vh;display:flex;flex-direction:column;padding:24px;">
    <div style="display:flex;align-items:center;gap:8px;">
      <button class="back-btn back-btn-white" id="role-back">←</button>
    </div>
    <div style="flex:1;display:flex;flex-direction:column;align-items:center;justify-content:center;gap:0;">
      <div style="text-align:center;margin-bottom:40px;">
        <div style="font-size:60px;">🎓</div>
        <h2 style="color:white;font-size:28px;font-weight:800;margin-top:8px;">${AppState.collegeName}</h2>
        <p style="color:rgba(255,255,255,0.8);font-size:15px;margin-top:4px;">Select your role to continue</p>
      </div>
      <div style="width:100%;max-width:420px;display:flex;flex-direction:column;gap:16px;">
        <div class="role-card" id="role-student" data-role="student">
          <span style="font-size:36px;">🎓</span>
          <div>
            <div style="font-size:18px;font-weight:700;">Student</div>
            <div style="font-size:13px;opacity:0.7;margin-top:2px;">Submit & track complaints</div>
          </div>
          <span style="margin-left:auto;font-size:20px;">›</span>
        </div>
        <div class="role-card" id="role-staff" data-role="staff">
          <span style="font-size:36px;">👷</span>
          <div>
            <div style="font-size:18px;font-weight:700;">Staff</div>
            <div style="font-size:13px;opacity:0.7;margin-top:2px;">Manage & resolve issues</div>
          </div>
          <span style="margin-left:auto;font-size:20px;">›</span>
        </div>
        <div class="role-card" id="role-admin" data-role="admin">
          <span style="font-size:36px;">🏢</span>
          <div>
            <div style="font-size:18px;font-weight:700;">Admin</div>
            <div style="font-size:13px;opacity:0.7;margin-top:2px;">Oversee & control the system</div>
          </div>
          <span style="margin-left:auto;font-size:20px;">›</span>
        </div>
      </div>
    </div>
  </div>
  <style>
    .role-card {
      background: rgba(255,255,255,0.15);
      backdrop-filter: blur(8px);
      border: 1px solid rgba(255,255,255,0.3);
      border-radius: 20px;
      padding: 20px 24px;
      display: flex; align-items: center; gap: 16px;
      color: white; cursor: pointer;
      transition: all 0.2s ease;
    }
    .role-card:hover { background: rgba(255,255,255,0.25); transform: translateX(4px); }
  </style>`;

  document.getElementById('role-back').addEventListener('click', () => navigateTo('college'));
  document.getElementById('role-student').addEventListener('click', () => navigateTo('login'));
  document.getElementById('role-staff').addEventListener('click', () => navigateTo('staffLogin'));
  document.getElementById('role-admin').addEventListener('click', () => navigateTo('adminLogin'));
}
