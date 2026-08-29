// student-dashboard.js — Mirrors StudentDashboardScreen.kt
import { AppState, navigateTo, goBack } from '../app.js';
import { auth } from '../firebase-config.js';
import { signOut } from 'https://www.gstatic.com/firebasejs/10.12.2/firebase-auth.js';

export function renderStudentDashboard(container) {
  const list = AppState.complaintList;
  const active = list.filter(c => ['Pending','In Progress','Assigned'].includes(c.status) || (c.status||'').startsWith('Waiting')).length;
  const resolved = list.filter(c => c.status === 'Resolved').length;
  const total = list.length;

  const menuItems = [
    { icon: '📝', label: 'New Complaint', screen: 'newComplaint', color: '#E3F2FD' },
    { icon: '🔍', label: 'Track Status', screen: 'complaintStatus', color: '#E8F5E9' },
    { icon: '📚', label: 'History', screen: 'complaintHistory', filter: 'all', color: '#F3E5F5' },
    { icon: '🚨', label: 'Emergency', screen: 'emergencyComplaint', color: '#FFEBEE' },
    { icon: '🕵️', label: 'Anonymous', screen: 'anonymousComplaint', color: '#FFF3E0' },
    { icon: '🔔', label: 'Notifications', screen: 'notification', color: '#E3F2FD' },
    { icon: '⭐', label: 'Feedback', screen: 'feedback', color: '#FFFDE7' },
    { icon: '👤', label: 'My Profile', screen: 'studentProfile', color: '#F5F5F5' },
  ];

  const userName = AppState.currentUserData?.name || 'Student';

  container.innerHTML = `
  <div class="screen active bg-app" style="min-height:100vh;display:flex;flex-direction:column;">
    <div style="background:#1565C0;padding:24px 20px 32px;">
      <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:16px;">
        <div style="display:flex;align-items:center;gap:12px;">
          <button id="dash-back" style="background:none;border:none;color:white;font-size:24px;font-weight:bold;cursor:pointer;padding:0;">←</button>
          <div>
            <p style="color:rgba(255,255,255,0.8);font-size:13px;font-weight:500;">Welcome back,</p>
            <h1 style="color:white;font-size:26px;font-weight:800;margin-top:2px;">${userName} 👋</h1>
          </div>
        </div>
      </div>
      <p style="color:rgba(255,255,255,0.7);font-size:13px;margin-bottom:16px;">${AppState.collegeName}</p>
      <!-- Stats row -->
      <div style="display:grid;grid-template-columns:1fr 1fr 1fr;gap:10px;">
        <div class="mini-stat-card" id="dash-active" data-filter="active">
          <div style="font-size:28px;font-weight:800;color:#FF9800;">${active}</div>
          <div style="font-size:11px;font-weight:600;color:rgba(255,255,255,0.7);">Active</div>
        </div>
        <div class="mini-stat-card" id="dash-resolved" data-filter="Resolved">
          <div style="font-size:28px;font-weight:800;color:#4CAF50;">${resolved}</div>
          <div style="font-size:11px;font-weight:600;color:rgba(255,255,255,0.7);">Resolved</div>
        </div>
        <div class="mini-stat-card" id="dash-total" data-filter="all">
          <div style="font-size:28px;font-weight:800;color:white;">${total}</div>
          <div style="font-size:11px;font-weight:600;color:rgba(255,255,255,0.7);">Total</div>
        </div>
      </div>
    </div>
    <!-- Services grid -->
    <div style="padding:12px 20px 16px;flex:1;overflow:hidden;">
      <h2 style="font-size:16px;font-weight:700;margin-bottom:10px;">Services</h2>
      <div style="display:grid;grid-template-columns:repeat(4,1fr);gap:10px;">
        ${menuItems.map(item => `
        <div class="svc-card-compact" id="svc-${item.screen}" style="background:white;">
          <div class="service-icon-box" style="background:${item.color};width:40px;height:40px;font-size:20px;border-radius:10px;">${item.icon}</div>
          <span class="service-label">${item.label}</span>
        </div>`).join('')}
      </div>
    </div>
  </div>
  <style>
    .mini-stat-card { background:rgba(255,255,255,0.12);border-radius:14px;padding:12px 8px;text-align:center;cursor:pointer;transition:background 0.2s; }
    .mini-stat-card:hover { background:rgba(255,255,255,0.22); }
    .svc-card-compact {
      border-radius:16px;
      height:110px;
      display:flex;flex-direction:column;
      align-items:center;justify-content:center;gap:8px;
      cursor:pointer;
      box-shadow:0 1px 4px rgba(0,0,0,0.08);
      transition:transform 0.18s,box-shadow 0.18s;
    }
    .svc-card-compact:hover { transform:translateY(-2px);box-shadow:0 4px 12px rgba(0,0,0,0.13); }
    .svc-card-compact .service-label { font-size:12px;font-weight:700;color:#333;text-align:center; }
  </style>`;

  menuItems.forEach(item => {
    document.getElementById(`svc-${item.screen}`)?.addEventListener('click', () => {
      if (item.filter) AppState.filterType = item.filter;
      navigateTo(item.screen);
    });
  });

  document.getElementById('dash-active').addEventListener('click', () => { AppState.filterType = 'active'; navigateTo('complaintHistory'); });
  document.getElementById('dash-resolved').addEventListener('click', () => { AppState.filterType = 'Resolved'; navigateTo('complaintHistory'); });
  document.getElementById('dash-total').addEventListener('click', () => { AppState.filterType = 'all'; navigateTo('complaintHistory'); });

  document.getElementById('dash-back')?.addEventListener('click', async () => {
    await signOut(auth);
    AppState.currentUserData = null;
    AppState.complaintList = [];
    AppState.navigationStack = [];
    navigateTo('login', { title: 'Student Sign In', emailHint: 'student_name@college.edu' });
  });
}
