// assigned-complaints.js — Mirrors AssignedComplaintsScreen.kt
import { AppState, navigateTo, goBack } from '../app.js';
import { getStatusBadgeClass } from './utils.js';

export function renderAssignedComplaints(container) {
  const filter = AppState.filterType || 'all';
  const allList = AppState.complaintList;

  let title, filteredList;
  if (filter === 'Resolved')       { title = 'All Tasks'; filteredList = allList.filter(c => c.status === 'Resolved'); }
  else if (filter === 'Pending')   { title = 'All Tasks'; filteredList = allList.filter(c => c.status === 'Pending' || c.status === 'Assigned' || (c.status||'').startsWith('Waiting')); }
  else if (filter === 'In Progress') { title = 'All Tasks'; filteredList = allList.filter(c => c.status === 'In Progress'); }
  else { title = 'All Tasks'; filteredList = allList; }

  const isAdmin = AppState.currentUserData?.role === 'admin';
  const nextScreen = isAdmin ? 'complaintStatus' : 'updateStatus';

  container.innerHTML = `
  <style>
    .ac-screen { min-height:100vh; display:flex; flex-direction:column; background:#F5F7FA; }
    .ac-appbar { display:flex; align-items:center; gap:12px; padding:16px; background:#fff; border-bottom:1px solid #EEEEEE; }
    .ac-back-btn { background:none; border:none; font-size:20px; cursor:pointer; color:#333; flex-shrink:0; }
    .ac-title { font-size:18px; font-weight:700; color:#111; }
    .ac-subtitle { font-size:13px; font-weight:400; color:#888; margin-top:2px; }

    .ac-list { padding:14px 14px 40px; overflow-y:auto; flex:1; }
    .ac-card { background:#fff; border-radius:14px; padding:16px; margin-bottom:14px; box-shadow:0 2px 10px rgba(0,0,0,0.07); cursor:pointer; transition:transform 0.15s; }
    .ac-card:active { transform:scale(0.98); }
    .ac-card-toprow { display:flex; justify-content:space-between; align-items:center; margin-bottom:10px; }
    .ac-ticket-chip { background:#E3F2FD; color:#1565C0; font-size:12px; font-weight:700; padding:5px 11px; border-radius:20px; letter-spacing:0.3px; }
    .ac-status-resolved { font-size:13px; font-weight:700; color:#2E7D32; }
    .ac-status-inprogress { font-size:13px; font-weight:700; color:#E65100; }
    .ac-status-pending { font-size:13px; font-weight:700; color:#1565C0; }
    .ac-card-title { font-size:16px; font-weight:700; color:#111; margin-bottom:8px; }
    .ac-meta-row { display:flex; align-items:center; gap:6px; font-size:13px; color:#555; margin-top:5px; }
    .ac-description { margin-top:12px; font-size:13px; color:#444; background:#F5F5F5; border-radius:8px; padding:10px 12px; line-height:1.5; }
    .ac-view-photo-btn { margin-top:12px; background:#F0F0F0; border:none; border-radius:8px; padding:8px 14px; font-size:13px; font-weight:600; color:#333; cursor:pointer; display:inline-flex; align-items:center; gap:6px; }
    .ac-view-photo-btn:hover { background:#E0E0E0; }
    .ac-empty { text-align:center; padding:60px 20px; color:#aaa; }
    .ac-empty-icon { font-size:48px; margin-bottom:12px; }
    .ac-empty-text { font-size:15px; font-weight:600; }
  </style>
  <div class="ac-screen">
    <div class="ac-appbar">
      <button class="ac-back-btn" id="ac-back">←</button>
      <div>
        <div class="ac-title">${title}</div>
        <div class="ac-subtitle">${filteredList.length} tasks found</div>
      </div>
    </div>

    <div class="ac-list">
      ${filteredList.length === 0 ? `
      <div class="ac-empty">
        <div class="ac-empty-icon">📋</div>
        <div class="ac-empty-text">No ${filter === 'all' ? '' : filter} tasks found</div>
      </div>` :
      filteredList.map(c => {
        const statusClass = c.status === 'Resolved' ? 'ac-status-resolved' : c.status === 'In Progress' ? 'ac-status-inprogress' : 'ac-status-pending';
        return `
        <div class="ac-card" data-ticket="${c.ticketId}">
          <div class="ac-card-toprow">
            <span class="ac-ticket-chip">${c.ticketId}</span>
            <span class="${statusClass}">${c.status || 'Pending'}</span>
          </div>
          <div class="ac-card-title">${c.title || 'Untitled'}</div>
          <div class="ac-meta-row"><span>📍</span>${c.location || 'Location not specified'}</div>
          <div class="ac-meta-row"><span>🏷️</span>${c.category || ''}</div>
          ${c.description ? `<div class="ac-description">${c.description}</div>` : ''}
          ${(c.photoUrl || c.imageUrl) ? `<button class="ac-view-photo-btn" onclick="event.stopPropagation();window.open('${c.photoUrl || c.imageUrl}','_blank')">📸 View Photo</button>` : ''}
        </div>`;
      }).join('')}
    </div>
  </div>`;

  document.getElementById('ac-back').addEventListener('click', goBack);


  document.querySelectorAll('.ac-card').forEach(item => {
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
