// staff-dashboard.js — Mirrors StaffDashboardScreen.kt
import { AppState, navigateTo, goBack } from '../app.js';
import { auth } from '../firebase-config.js';
import { signOut } from 'https://www.gstatic.com/firebasejs/10.12.2/firebase-auth.js';
import { groupComplaintsWithAI } from './ai-duplicate-engine.js';

export function renderStaffDashboard(container) {
  const list = AppState.complaintList || [];
  const u = AppState.currentUserData;

  const assigned = list.length;
  const pending = list.filter(c => c.status === 'In Progress').length;
  const resolved = list.filter(c => c.status === 'Resolved').length;

  const duplicateGroups = groupComplaintsWithAI(list);
  const totalAffectedStudents = duplicateGroups.reduce((acc, g) => acc + g.totalStudentsAffected, 0);
  const groupMap = new Map();
  duplicateGroups.forEach(g => {
    groupMap.set(g.mainComplaint.ticketId, g);
    g.linkedComplaints.forEach(lc => groupMap.set(lc.ticketId, g));
  });

  const actions = [
    { icon: '👥', label: 'AI Duplicate Groups', sub: `${duplicateGroups.length} duplicate group${duplicateGroups.length === 1 ? '' : 's'} detected (${totalAffectedStudents} students affected)`, screen: 'duplicateGroups' },
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
          <div style="display:inline-flex;align-items:center;gap:6px;background:rgba(255,255,255,0.22);padding:4px 10px;border-radius:12px;margin-top:6px;font-size:12px;font-weight:700;color:white;">
            🏷️ ${(u?.assignedCategories && u.assignedCategories.length > 0) ? u.assignedCategories.join(', ') + ' Staff' : (u?.department || 'General Maintenance Staff')}
          </div>
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
    <!-- Actions & Assigned Complaints -->
    <div style="padding:20px;overflow-y:auto;flex:1;padding-bottom:40px;">

      ${duplicateGroups.length > 0 ? `
      <!-- AI Duplicate Groups Banner -->
      <div id="sd-dup-banner" style="background:linear-gradient(135deg,#0D47A1,#1565C0);border-radius:16px;padding:16px 18px;color:white;margin-bottom:20px;display:flex;align-items:center;justify-content:space-between;box-shadow:0 4px 14px rgba(13,71,161,0.25);cursor:pointer;transition:transform 0.15s ease;">
        <div style="display:flex;align-items:center;gap:12px;">
          <div style="width:42px;height:42px;border-radius:12px;background:rgba(255,255,255,0.22);display:flex;align-items:center;justify-content:center;font-size:22px;">👥</div>
          <div>
            <div style="font-size:15px;font-weight:800;">${duplicateGroups.length} Duplicate Group${duplicateGroups.length === 1 ? '' : 's'} Detected</div>
            <div style="font-size:12px;opacity:0.9;">${totalAffectedStudents} students affected • Grouped automatically by AI</div>
          </div>
        </div>
        <span style="background:white;color:#1565C0;padding:6px 14px;border-radius:16px;font-size:12px;font-weight:800;">Review Groups ›</span>
      </div>
      ` : ''}

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

      <!-- Assigned Tasks with AI Category Tags -->
      <div style="margin-top:24px;">
        <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:12px;">
          <h2 style="font-size:17px;font-weight:700;color:#333;">Assigned Tasks (${list.length})</h2>
          <span style="font-size:13px;font-weight:700;color:#1565C0;cursor:pointer;" id="sd-view-all">View All ›</span>
        </div>
        ${list.length === 0 ? `
          <div style="background:white;border-radius:14px;padding:24px;text-align:center;color:#9E9E9E;box-shadow:0 1px 4px rgba(0,0,0,0.05);">
            <div style="font-size:28px;margin-bottom:6px;">📋</div>
            <div style="font-size:14px;font-weight:600;">No complaints currently assigned</div>
            <div style="font-size:12px;color:#BDBDBD;margin-top:2px;">New complaints matching your categories will appear here automatically.</div>
          </div>
        ` : list.slice(0, 5).map(c => {
          const matchedGroup = groupMap.get(c.ticketId);
          const isMain = matchedGroup && matchedGroup.mainComplaint.ticketId === c.ticketId;
          return `
          <div class="card sd-task-card" style="margin-bottom:12px;padding:16px;cursor:pointer;border-left:4px solid #1565C0;transition:transform 0.15s;" data-ticket="${c.ticketId}">
            <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:8px;">
              <div style="display:flex;align-items:center;gap:6px;">
                <span style="background:#E3F2FD;color:#1565C0;font-size:11px;font-weight:700;padding:3px 8px;border-radius:12px;">${c.ticketId}</span>
                ${isMain ? `<span style="background:#EDE7F6;color:#5E35B1;font-size:11px;font-weight:800;padding:3px 8px;border-radius:12px;">👥 ${matchedGroup.totalStudentsAffected} Students (Grouped)</span>` : ''}
              </div>
              <span class="badge ${c.status === 'Resolved' ? 'badge-resolved' : c.status === 'In Progress' ? 'badge-progress' : 'badge-assigned'}" style="font-size:11px;">${c.status || 'Assigned'}</span>
            </div>
            <div style="font-size:15px;font-weight:700;color:#212121;margin-bottom:6px;">${c.title || 'Untitled'}</div>
            <div style="display:flex;align-items:center;gap:10px;font-size:12px;color:#555;flex-wrap:wrap;">
              <span style="background:#E3F2FD;color:#1565C0;padding:3px 10px;border-radius:8px;font-weight:700;font-size:12px;">🏷️ ${c.category || 'General Maintenance'}</span>
              <span>📍 ${c.location || 'Campus'}</span>
            </div>
          </div>
        `; }).join('')}
      </div>
    </div>
  </div>`;

  document.getElementById('staff-back').addEventListener('click', goBack);
  document.getElementById('sd-dup-banner')?.addEventListener('click', () => { navigateTo('duplicateGroups'); });
  document.getElementById('sd-assigned').addEventListener('click', () => { AppState.filterType = 'all'; navigateTo('assignedComplaints'); });
  document.getElementById('sd-pending').addEventListener('click', () => { AppState.filterType = 'In Progress'; navigateTo('assignedComplaints'); });
  document.getElementById('sd-resolved').addEventListener('click', () => { AppState.filterType = 'Resolved'; navigateTo('assignedComplaints'); });

  actions.forEach(a => {
    document.getElementById(`staff-action-${a.screen}`)?.addEventListener('click', () => {
      AppState.filterType = 'all';
      navigateTo(a.screen);
    });
  });

  document.getElementById('sd-view-all')?.addEventListener('click', () => {
    AppState.filterType = 'all';
    navigateTo('assignedComplaints');
  });

  document.querySelectorAll('.sd-task-card').forEach(card => {
    card.addEventListener('click', () => {
      const ticket = card.dataset.ticket;
      const complaint = list.find(c => c.ticketId === ticket);
      if (complaint) {
        AppState.complaintData = complaint;
        navigateTo('updateStatus');
      }
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
