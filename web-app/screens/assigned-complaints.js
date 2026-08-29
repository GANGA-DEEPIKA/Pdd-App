// assigned-complaints.js — Mirrors AssignedComplaintsScreen.kt
import { AppState, navigateTo, goBack } from '../app.js';
import { formatDate, getStatusBadgeClass, getComplaintBorderClass } from './utils.js';

export function renderAssignedComplaints(container) {
  const filter = AppState.filterType;
  const allList = AppState.complaintList;

  let title, filteredList;
  if (filter === 'Resolved') { title = 'Resolved Tasks'; filteredList = allList.filter(c => c.status === 'Resolved'); }
  else if (filter === 'Pending') { title = 'Pending Tasks'; filteredList = allList.filter(c => c.status === 'Pending' || c.status === 'Assigned' || (c.status||'').startsWith('Waiting')); }
  else if (filter === 'In Progress') { title = 'In Progress'; filteredList = allList.filter(c => c.status === 'In Progress'); }
  else { title = 'All Tasks'; filteredList = allList; }

  const isAdmin = AppState.currentUserData?.role === 'admin';
  const nextScreen = isAdmin ? 'complaintStatus' : 'updateStatus';

  container.innerHTML = `
  <div class="screen active" style="min-height:100vh;display:flex;flex-direction:column;background:#F5F7FA;">
    <div class="app-bar">
      <button class="back-btn" id="ac-back">←</button>
      <span class="app-bar-title">${title}</span>
      <span style="margin-left:auto;background:#E3F2FD;color:#1565C0;padding:4px 10px;border-radius:20px;font-size:12px;font-weight:700;">${filteredList.length}</span>
    </div>
    <!-- Filter tabs -->
    <div style="background:white;padding:0 12px;display:flex;gap:4px;box-shadow:0 1px 4px rgba(0,0,0,0.06);overflow-x:auto;white-space:nowrap;">
      ${['all','Pending','In Progress','Resolved'].map(f => `
      <button class="filter-tab${filter===f?' active-tab':''}" data-filter="${f}" style="flex-shrink:0;">${f==='all'?'All':f}</button>`).join('')}
    </div>
    <div style="padding:16px;overflow-y:auto;flex:1;padding-bottom:40px;">
      ${filteredList.length === 0 ? `
      <div class="empty-state">
        <div class="empty-state-icon">📋</div>
        <div class="empty-state-title">No tasks found</div>
        <div class="empty-state-sub">No ${filter === 'all' ? '' : filter} complaints assigned.</div>
      </div>` :
      filteredList.map(c => `
      <div class="complaint-item ${getComplaintBorderClass(c.status)}" data-ticket="${c.ticketId}">
        <div class="complaint-item-header">
          <div style="flex:1;min-width:0;">
            <div class="complaint-title" style="white-space:nowrap;overflow:hidden;text-overflow:ellipsis;">${c.title || 'Untitled'}</div>
            <div class="complaint-category">${c.category || ''} &nbsp;•&nbsp; ${c.location || ''}</div>
          </div>
          <span class="badge ${getStatusBadgeClass(c.status)}">${c.status || 'Pending'}</span>
        </div>
        <div class="complaint-meta">🎟 ${c.ticketId} &nbsp;•&nbsp; ${formatDate(c.createdAt)}</div>
        ${c.studentEmail && c.studentEmail !== 'anonymous@campus.edu' ? `<div class="complaint-meta" style="margin-top:2px;">👤 ${c.studentEmail}</div>` : ''}
      </div>`).join('')}
    </div>
  </div>
  <style>
    .filter-tab { background:none;border:none;padding:12px 14px;font-size:13px;font-weight:600;color:#757575;cursor:pointer;border-bottom:2px solid transparent;transition:all 0.2s; }
    .active-tab { color:#1565C0;border-bottom-color:#1565C0; }
  </style>`;

  document.getElementById('ac-back').addEventListener('click', goBack);

  document.querySelectorAll('.filter-tab').forEach(tab => {
    tab.addEventListener('click', () => {
      AppState.filterType = tab.dataset.filter;
      renderAssignedComplaints(container);
    });
  });

  document.querySelectorAll('.complaint-item').forEach(item => {
    item.addEventListener('click', () => {
      const ticket = item.dataset.ticket;
      const complaint = filteredList.find(c => c.ticketId === ticket);
      if (complaint) {
        AppState.complaintData = complaint;
        navigateTo(nextScreen);
      }
    });
  });
}
