// admin-dashboard.js — Mirrors AdminDashboardScreen.kt
import { AppState, navigateTo, goBack } from '../app.js';
import { auth } from '../firebase-config.js';
import { signOut } from 'https://www.gstatic.com/firebasejs/10.12.2/firebase-auth.js';

export function renderAdminDashboard(container) {
  const list = AppState.complaintList;
  const collegeName = AppState.currentUserData?.collegeName || AppState.collegeName;

  const total = list.length;
  const pending = list.filter(c => c.status === 'Pending' || c.status === 'Assigned' || (c.status||'').startsWith('Waiting')).length;
  const inProgress = list.filter(c => c.status === 'In Progress').length;
  const resolved = list.filter(c => c.status === 'Resolved').length;

  const adminActions = [
    { icon: '👥', label: 'Manage Students', sub: 'View, add or remove users', screen: 'manageStudents' },
    { icon: '👷', label: 'Manage Staff', sub: 'Approve/reject staff requests', screen: 'staffApproval' },
    { icon: '📈', label: 'System Analytics', sub: 'Generate performance reports', screen: 'performanceSummary' },
    { icon: '⚙️', label: 'Global Settings', sub: 'App-wide configuration', screen: 'globalSettings' },
  ];

  container.innerHTML = `
  <div class="screen active" style="min-height:100vh;display:flex;flex-direction:column;background:#F5F7FA;">
    <!-- Dark blue header -->
    <div style="background:#1A237E;padding:24px 20px 32px;">
      <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:12px;">
        <button class="back-btn back-btn-white" id="admin-back">←</button>
        <div style="background:rgba(255,255,255,0.15);padding:5px 12px;border-radius:8px;">
          <span style="color:white;font-size:11px;font-weight:700;">ADMIN: ${collegeName}</span>
        </div>
        <button id="admin-logout" style="background:rgba(255,255,255,0.15);border:none;color:white;padding:8px 12px;border-radius:16px;cursor:pointer;font-size:12px;font-weight:600;">Logout</button>
      </div>
      <h1 style="color:white;font-size:30px;font-weight:800;">Control Center</h1>
      <p style="color:rgba(255,255,255,0.65);font-size:13px;margin-top:4px;">Manage your campus complaint system</p>
    </div>
    <!-- Body -->
    <div style="padding:20px;overflow-y:auto;flex:1;padding-bottom:40px;">
      <h2 style="font-size:17px;font-weight:700;color:#555;margin-bottom:16px;">College Statistics</h2>
      <div class="admin-stat-grid">
        <div class="admin-stat-card" id="adm-total" style="background:rgba(21,101,192,0.08);cursor:pointer;">
          <div style="font-size:30px;font-weight:800;color:#1565C0;">${total}</div>
          <div style="font-size:12px;font-weight:600;color:#757575;">Total Tickets</div>
        </div>
        <div class="admin-stat-card" id="adm-pending" style="background:rgba(255,152,0,0.08);">
          <div style="font-size:30px;font-weight:800;color:#FF9800;">${pending}</div>
          <div style="font-size:12px;font-weight:600;color:#757575;">Pending</div>
        </div>
      </div>
      <div class="admin-stat-grid">
        <div class="admin-stat-card" id="adm-progress" style="background:rgba(123,31,162,0.08);">
          <div style="font-size:30px;font-weight:800;color:#7B1FA2;">${inProgress}</div>
          <div style="font-size:12px;font-weight:600;color:#757575;">In Progress</div>
        </div>
        <div class="admin-stat-card" id="adm-resolved" style="background:rgba(76,175,80,0.08);">
          <div style="font-size:30px;font-weight:800;color:#4CAF50;">${resolved}</div>
          <div style="font-size:12px;font-weight:600;color:#757575;">Resolved</div>
        </div>
      </div>
      <!-- Resolution rate bar -->
      ${total > 0 ? `
      <div style="background:white;border-radius:16px;padding:16px;margin-bottom:20px;box-shadow:0 1px 4px rgba(0,0,0,0.08);">
        <div style="display:flex;justify-content:space-between;margin-bottom:8px;">
          <span style="font-size:13px;font-weight:600;color:#555;">Resolution Rate</span>
          <span style="font-size:13px;font-weight:700;color:#4CAF50;">${Math.round(resolved/total*100)}%</span>
        </div>
        <div style="background:#F5F5F5;border-radius:8px;height:8px;">
          <div style="background:linear-gradient(90deg,#4CAF50,#81C784);border-radius:8px;height:8px;width:${Math.round(resolved/total*100)}%;transition:width 0.6s;"></div>
        </div>
      </div>` : ''}
      <h2 style="font-size:17px;font-weight:700;color:#555;margin-bottom:16px;">Administration</h2>
      ${adminActions.map(a => `
      <div class="action-row" id="adm-action-${a.screen}">
        <div class="action-row-icon">${a.icon}</div>
        <div>
          <div class="action-row-title">${a.label}</div>
          <div class="action-row-sub">${a.sub}</div>
        </div>
        <span style="margin-left:auto;color:#BDBDBD;font-size:18px;">›</span>
      </div>`).join('')}
    </div>
  </div>`;

  document.getElementById('admin-back').addEventListener('click', goBack);
  document.getElementById('adm-total').addEventListener('click', () => { AppState.filterType = 'all'; navigateTo('assignedComplaints'); });
  document.getElementById('adm-pending').addEventListener('click', () => { AppState.filterType = 'Pending'; navigateTo('assignedComplaints'); });
  document.getElementById('adm-progress').addEventListener('click', () => { AppState.filterType = 'In Progress'; navigateTo('assignedComplaints'); });
  document.getElementById('adm-resolved').addEventListener('click', () => { AppState.filterType = 'Resolved'; navigateTo('assignedComplaints'); });

  adminActions.forEach(a => {
    document.getElementById(`adm-action-${a.screen}`)?.addEventListener('click', () => navigateTo(a.screen));
  });

  document.getElementById('admin-logout').addEventListener('click', async () => {
    await signOut(auth);
    AppState.currentUserData = null;
    AppState.complaintList = [];
    AppState.screen = 'college';
    AppState.navigationStack = [];
    const { renderCurrentScreen } = await import('../app.js');
    renderCurrentScreen();
  });
}
