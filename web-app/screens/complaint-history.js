// complaint-history.js — Mirrors ComplaintHistoryScreen.kt
import { AppState, navigateTo, goBack } from '../app.js';
import { formatDate, getStatusBadgeClass, getComplaintBorderClass } from './utils.js';

export function renderComplaintHistory(container) {
  const filter = AppState.filterType;
  const allList = AppState.complaintList;

  let title, filteredList;
  if (filter === 'Resolved') { title = 'Resolved Complaints'; filteredList = allList.filter(c => c.status === 'Resolved'); }
  else if (filter === 'active') { title = 'Active Complaints'; filteredList = allList.filter(c => c.status !== 'Resolved'); }
  else { title = 'All Complaints'; filteredList = allList; }

  container.innerHTML = `
  <div class="screen active" style="min-height:100vh;display:flex;flex-direction:column;background:#F5F7FA;">
    <div class="app-bar">
      <button class="back-btn" id="ch-back">←</button>
      <span class="app-bar-title">${title}</span>
      <span style="margin-left:auto;background:#E3F2FD;color:#1565C0;padding:4px 10px;border-radius:20px;font-size:12px;font-weight:700;">${filteredList.length}</span>
    </div>
    <!-- Filter tabs -->
    <div style="background:white;padding:0 16px;display:flex;gap:8px;box-shadow:0 1px 4px rgba(0,0,0,0.06);">
      ${['all','active','Resolved'].map(f => `
      <button class="filter-tab${AppState.filterType===f?' active-tab':''}" data-filter="${f}">${f==='all'?'All':f==='active'?'Active':'Resolved'}</button>`).join('')}
    </div>
    <div style="padding:16px;overflow-y:auto;flex:1;padding-bottom:40px;" id="history-list">
      ${filteredList.length === 0 ? `
      <div class="empty-state">
        <div class="empty-state-icon">📋</div>
        <div class="empty-state-title">No complaints found</div>
        <div class="empty-state-sub">${filter === 'Resolved' ? 'No resolved complaints yet.' : 'No complaints to show.'}</div>
      </div>` :
      filteredList.map(c => `
      <div class="complaint-item ${getComplaintBorderClass(c.status)}" data-ticket="${c.ticketId}">
        <div class="complaint-item-header">
          <div>
            <div class="complaint-title">${c.title || 'Untitled'}</div>
            <div class="complaint-category">${c.category || ''}</div>
          </div>
          <span class="badge ${getStatusBadgeClass(c.status)}">${c.status || 'Pending'}</span>
        </div>
        <div class="complaint-meta">📍 ${c.location || 'N/A'} &nbsp;•&nbsp; ${formatDate(c.createdAt)} &nbsp;•&nbsp; 🎟 ${c.ticketId}</div>
      </div>`).join('')}
    </div>
  </div>
  <style>
    .filter-tab { background:none;border:none;padding:12px 16px;font-size:14px;font-weight:600;color:#757575;cursor:pointer;border-bottom:2px solid transparent;transition:all 0.2s; }
    .active-tab { color:#1565C0;border-bottom-color:#1565C0; }
  </style>`;

  document.getElementById('ch-back').addEventListener('click', goBack);

  document.querySelectorAll('.filter-tab').forEach(tab => {
    tab.addEventListener('click', () => {
      AppState.filterType = tab.dataset.filter;
      renderComplaintHistory(container);
    });
  });

  document.querySelectorAll('.complaint-item').forEach(item => {
    item.addEventListener('click', () => {
      const ticket = item.dataset.ticket;
      const complaint = filteredList.find(c => c.ticketId === ticket);
      if (complaint) {
        AppState.complaintData = complaint;
        navigateTo('complaintStatus');
      }
    });
  });
}
