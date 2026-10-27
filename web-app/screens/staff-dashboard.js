// staff-dashboard.js — Mirrors StaffDashboardScreen.kt
import { AppState, navigateTo, goBack } from '../app.js';
import { auth } from '../firebase-config.js';
import { signOut } from 'https://www.gstatic.com/firebasejs/10.12.2/firebase-auth.js';

export function renderStaffDashboard(container) {
  const list = AppState.complaintList;
  const u = AppState.currentUserData;

  const assigned = list.length;
  const pending = list.filter(c => c.status === 'In Progress').length;
  const resolved = list.filter(c => c.status === 'Resolved').length;

  const actions = [
    { icon: '📋', label: 'Assigned Complaints', sub: 'View your task list', screen: 'assignedComplaints' },
    { icon: '🔄', label: 'Update Status', sub: 'Change complaint progress', screen: 'updateStatus' },
    { icon: '📝', label: 'Resolution Notes', sub: 'Add resolution remarks', screen: 'resolutionNotes' },
    { icon: '📷', label: 'Upload Proof', sub: 'Photo/video evidence', screen: 'uploadProof' },
    { icon: '⬆️', label: 'Escalate Complaint', sub: 'Flag for admin review', screen: 'escalateComplaint' },
    { icon: '📊', label: 'My Performance', sub: 'View completion stats', screen: 'performanceSummary' },
  ];

  container.innerHTML = `
  <div class="screen active" style="min-height:100vh;display:flex;flex-direction:column;background:#F5F7FA;">
    <!-- Header -->
    <div style="background:linear-gradient(135deg,#1565C0,#0D47A1);padding:24px 20px 28px;">
      <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:20px;">
        <button class="back-btn back-btn-white" id="staff-back">←</button>
        <button id="staff-logout" style="background:rgba(255,255,255,0.15);border:none;color:white;padding:8px 14px;border-radius:20px;cursor:pointer;font-size:13px;font-weight:600;">Logout</button>
      </div>
      <div style="display:flex;align-items:center;gap:14px;">
        <div style="width:54px;height:54px;background:rgba(255,255,255,0.2);border-radius:50%;display:flex;align-items:center;justify-content:center;font-size:24px;">👷</div>
        <div>
          <h1 style="color:white;font-size:22px;font-weight:800;">${u?.name || 'Staff'}</h1>
          <p style="color:rgba(255,255,255,0.75);font-size:13px;margin-top:2px;">${u?.department || 'Department'} • ${u?.collegeName || AppState.collegeName}</p>
          <p style="color:rgba(255,255,255,0.6);font-size:12px;margin-top:2px;">${(u?.assignedCategories || []).join(', ') || 'No categories assigned'}</p>
        </div>
      </div>
      <!-- Stats -->
      <div style="display:grid;grid-template-columns:1fr 1fr 1fr;gap:10px;margin-top:20px;">
        <div class="mini-stat-card" id="sd-assigned" style="background:rgba(21, 101, 192, 0.18);border-radius:14px;padding:12px;text-align:center;cursor:pointer;">
          <div style="font-size:26px;font-weight:800;color:#64B5F6;min-height:32px;display:flex;align-items:center;justify-content:center;">${AppState.complaintsLoaded ? assigned : '<span class="stat-spinner" style="border-top-color:#64B5F6;"></span>'}</div>
          <div style="font-size:11px;color:rgba(255,255,255,0.8);margin-top:4px;">Assigned</div>
        </div>
        <div class="mini-stat-card" id="sd-pending" style="background:rgba(255,152,0,0.18);border-radius:14px;padding:12px;text-align:center;cursor:pointer;">
          <div style="font-size:26px;font-weight:800;color:#FFB74D;min-height:32px;display:flex;align-items:center;justify-content:center;">${AppState.complaintsLoaded ? pending : '<span class="stat-spinner" style="border-top-color:#FFB74D;"></span>'}</div>
          <div style="font-size:11px;color:rgba(255,255,255,0.8);margin-top:4px;">In-Progress</div>
        </div>
        <div class="mini-stat-card" id="sd-resolved" style="background:rgba(76,175,80,0.18);border-radius:14px;padding:12px;text-align:center;cursor:pointer;">
          <div style="font-size:26px;font-weight:800;color:#81C784;min-height:32px;display:flex;align-items:center;justify-content:center;">${AppState.complaintsLoaded ? resolved : '<span class="stat-spinner" style="border-top-color:#81C784;"></span>'}</div>
          <div style="font-size:11px;color:rgba(255,255,255,0.8);margin-top:4px;">Resolved</div>
        </div>
      </div>
    </div>
    <!-- Actions -->
    <div style="padding:20px;overflow-y:auto;flex:1;padding-bottom:40px;">
      <h2 style="font-size:17px;font-weight:700;margin-bottom:16px;color:#333;">Quick Actions</h2>
      ${actions.map(a => `
      <div class="action-row" id="staff-action-${a.screen}">
        <div class="action-row-icon">${a.icon}</div>
        <div>
          <div class="action-row-title">${a.label}</div>
          <div class="action-row-sub">${a.sub}</div>
        </div>
        <span style="margin-left:auto;color:#BDBDBD;font-size:18px;">›</span>
      </div>`).join('')}
    </div>
  </div>`;

  document.getElementById('staff-back').addEventListener('click', goBack);
  document.getElementById('sd-assigned').addEventListener('click', () => { AppState.filterType = 'all'; navigateTo('assignedComplaints'); });
  document.getElementById('sd-pending').addEventListener('click', () => { AppState.filterType = 'In Progress'; navigateTo('assignedComplaints'); });
  document.getElementById('sd-resolved').addEventListener('click', () => { AppState.filterType = 'Resolved'; navigateTo('assignedComplaints'); });

  actions.forEach(a => {
    document.getElementById(`staff-action-${a.screen}`)?.addEventListener('click', () => {
      AppState.filterType = 'all';
      navigateTo(a.screen);
    });
  });

  document.getElementById('staff-logout').addEventListener('click', async () => {
    await signOut(auth);
    AppState.currentUserData = null;
    AppState.complaintList = [];
    AppState.screen = 'college';
    AppState.navigationStack = [];
    const { renderCurrentScreen } = await import('../app.js');
    renderCurrentScreen();
  });
}
